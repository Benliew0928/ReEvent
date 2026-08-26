package com.reevent.app.core.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.data.SignUpOutcome
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppEntry { LOADING, ONBOARDING, SIGN_IN, PASSWORD_RESET, DELETION_PENDING, COMPLETE_ROLE, ORGANIZER, PARTICIPANT, PARTNER }

data class SessionUiState(
    val entry: AppEntry = AppEntry.LOADING,
    val user: User? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferences: AppPreferences,
    private val coreSyncRepository: CoreSyncRepository
) : ViewModel() {
    private val restored = MutableStateFlow(false)

    val state: StateFlow<SessionUiState> = combine(
        preferences.onboardingComplete,
        authRepository.currentUser,
        restored,
        preferences.passwordRecoveryPending
    ) { onboardingComplete, user, hasRestored, passwordRecoveryPending ->
        SessionUiState(
            entry = sessionEntryFor(onboardingComplete, user, hasRestored, passwordRecoveryPending),
            user = user
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionUiState())

    init {
        // A normal sign-in happens after the one-time restore below has already completed.
        // Refresh for every newly authenticated account so its Room cache never starts empty.
        viewModelScope.launch {
            authRepository.currentUser
                .filterNotNull()
                .filterNot { it.deletionPending }
                .map { user -> user.id }
                .distinctUntilChanged()
                .collect { coreSyncRepository.refreshAuthorisedData() }
        }
        viewModelScope.launch {
            try {
                authRepository.restoreSession()
            } finally {
                // Cache refresh is optional. A failed or slow sync must never leave the app on
                // an unbounded startup screen.
                restored.value = true
            }
        }
    }

    fun completeOnboarding() = viewModelScope.launch { preferences.setOnboardingComplete(true) }
}

internal fun sessionEntryFor(
    onboardingComplete: Boolean,
    user: User?,
    hasRestored: Boolean,
    passwordRecoveryPending: Boolean
): AppEntry = when {
    !hasRestored -> AppEntry.LOADING
    user?.deletionPending == true -> AppEntry.DELETION_PENDING
    passwordRecoveryPending && user != null -> AppEntry.PASSWORD_RESET
    !onboardingComplete -> AppEntry.ONBOARDING
    user == null -> AppEntry.SIGN_IN
    user.role == null -> AppEntry.COMPLETE_ROLE
    user.role == UserRole.ORGANIZER -> AppEntry.ORGANIZER
    user.role == UserRole.PARTICIPANT -> AppEntry.PARTICIPANT
    else -> AppEntry.PARTNER
}

data class AuthUiState(
    val loading: Boolean = false,
    val error: FailureReason? = null,
    /** The first role request remains the only retryable choice after an uncertain response. */
    val pendingRoleAssignment: UserRole? = null,
    val resetRequested: Boolean = false,
    val confirmationRequired: Boolean = false,
    val confirmationEmail: String? = null,
    val confirmationResent: Boolean = false,
    val passwordUpdated: Boolean = false,
    val accountDeleted: Boolean = false,
    val accountDeletionBlocked: AccountDeletionBlock? = null,
    val accountDeletionPending: Boolean = false,
    val accountDeletionReauthenticationRequired: Boolean = false,
    val passwordReauthenticationUnavailable: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = mutableState

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true)
            mutableState.value = when (val result = authRepository.signIn(email, password)) {
                is AppResult.Success -> AuthUiState()
                is AppResult.Failure -> confirmationStateFor(result.reason, email)
            }
        }
    }
    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true)
            mutableState.value = when (val result = authRepository.signUp(email, password, displayName)) {
                is AppResult.Success -> when (result.value) {
                    SignUpOutcome.ConfirmationRequired -> AuthUiState(confirmationRequired = true, confirmationEmail = email.trim())
                    is SignUpOutcome.Authenticated -> AuthUiState()
                }
                is AppResult.Failure -> confirmationStateFor(result.reason, email)
            }
        }
    }
    fun signInWithGoogle() = submit { authRepository.startGoogleSignIn() }

    fun completeRole(role: UserRole) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true, pendingRoleAssignment = role)
            mutableState.value = when (val result = authRepository.completeRole(role)) {
                is AppResult.Success -> AuthUiState()
                is AppResult.Failure -> AuthUiState(error = result.reason, pendingRoleAssignment = role)
            }
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true)
            mutableState.value = when (val result = authRepository.requestPasswordReset(email)) {
                is AppResult.Success -> AuthUiState(resetRequested = true)
                is AppResult.Failure -> AuthUiState(error = result.reason)
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true)
            mutableState.value = when (val result = authRepository.updatePassword(newPassword)) {
                is AppResult.Success -> AuthUiState(passwordUpdated = true)
                is AppResult.Failure -> AuthUiState(error = result.reason)
            }
        }
    }

    fun finishPasswordRecovery() = viewModelScope.launch {
        authRepository.finishPasswordRecovery()
        mutableState.value = AuthUiState()
    }

    fun deleteAccount(currentPassword: String) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true)
            mutableState.value = when (val result = authRepository.deleteAccount(currentPassword)) {
                is AppResult.Success -> when (val outcome = result.value) {
                    AccountDeletionOutcome.Deleted -> AuthUiState(accountDeleted = true)
                    AccountDeletionOutcome.FinalizationPending -> AuthUiState(accountDeletionPending = true)
                    AccountDeletionOutcome.ReauthenticationRequired ->
                        AuthUiState(accountDeletionReauthenticationRequired = true)
                    AccountDeletionOutcome.PasswordReauthenticationUnavailable ->
                        AuthUiState(passwordReauthenticationUnavailable = true)
                    is AccountDeletionOutcome.Blocked -> AuthUiState(accountDeletionBlocked = outcome.reason)
                }
                is AppResult.Failure -> AuthUiState(error = result.reason)
            }
        }
    }

    fun clearFeedback() {
        mutableState.value = AuthUiState()
    }

    fun resendSignUpConfirmation(email: String) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true, confirmationRequired = true, confirmationEmail = email.trim())
            mutableState.value = when (val result = authRepository.resendSignUpConfirmation(email)) {
                is AppResult.Success -> AuthUiState(
                    confirmationRequired = true,
                    confirmationEmail = email.trim(),
                    confirmationResent = true
                )
                is AppResult.Failure -> AuthUiState(
                    error = result.reason,
                    confirmationRequired = true,
                    confirmationEmail = email.trim()
                )
            }
        }
    }

    fun signOut() = viewModelScope.launch { authRepository.signOut() }

    private fun submit(action: suspend () -> AppResult<*>) {
        viewModelScope.launch {
            mutableState.value = AuthUiState(loading = true)
            mutableState.value = when (val result = action()) {
                is AppResult.Success -> AuthUiState()
                is AppResult.Failure -> AuthUiState(error = result.reason)
            }
        }
    }

    private fun confirmationStateFor(reason: FailureReason, email: String): AuthUiState =
        if (reason == FailureReason.EMAIL_CONFIRMATION_REQUIRED) {
            AuthUiState(confirmationRequired = true, confirmationEmail = email.trim())
        } else {
            AuthUiState(error = reason)
        }
}
