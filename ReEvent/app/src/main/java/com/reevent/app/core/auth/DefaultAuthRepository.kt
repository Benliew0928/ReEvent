package com.reevent.app.core.auth

import android.content.Intent
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.AccountScope
import com.reevent.app.core.data.AuthRepository
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.data.SignUpOutcome
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.toDomain
import com.reevent.app.core.database.toEntity
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.core.network.SupabaseAuthGateway
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

/**
 * The role RPC is deliberately idempotent for the originally requested role, but a different
 * role must never be accepted after the first choice has been frozen on the server.
 */
internal enum class RoleAssignmentState {
    UNASSIGNED,
    MATCHES_REQUEST,
    CONFLICTS_WITH_REQUEST
}

internal fun roleAssignmentStateFor(
    requestedRole: UserRole,
    assignedRole: UserRole?
): RoleAssignmentState = when (assignedRole) {
    null -> RoleAssignmentState.UNASSIGNED
    requestedRole -> RoleAssignmentState.MATCHES_REQUEST
    else -> RoleAssignmentState.CONFLICTS_WITH_REQUEST
}

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val gateway: SupabaseAuthGateway,
    private val dao: CoreDao,
    private val preferences: AppPreferences,
    private val accountScope: AccountScope,
    private val sessionCleaner: AccountSessionCleaner
) : AuthRepository {
    private val mutableCurrentUser = MutableStateFlow<User?>(null)
    // Session restoration and a PKCE callback both touch the same Supabase Auth client. Keep
    // them serialised so a cold-start restore cannot race the browser callback.
    private val sessionOperationMutex = Mutex()
    override val currentUser: Flow<User?> = mutableCurrentUser.asStateFlow()

    override suspend fun signUp(email: String, password: String, displayName: String): AppResult<SignUpOutcome> {
        val validation = validateCredentials(email, password)
        if (validation != null || displayName.isBlank()) return AppResult.Failure(FailureReason.VALIDATION)
        if (!gateway.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)
        beginFreshAuthentication()
        return when (val result = remoteCall { gateway.signUp(email.trim(), password, displayName.trim()) }) {
            is AppResult.Success -> {
                val user = result.value
            if (user == null) {
                AppResult.Success(SignUpOutcome.ConfirmationRequired)
            } else {
                val namedUser = user.copy(displayName = displayName.trim())
                persistAuthenticatedUser(namedUser)
                AppResult.Success(SignUpOutcome.Authenticated(namedUser))
            }
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun signIn(email: String, password: String): AppResult<User> {
        val validation = validateCredentials(email, password)
        if (validation != null) return AppResult.Failure(FailureReason.VALIDATION)
        return if (gateway.isConfigured()) {
            beginFreshAuthentication()
            runAuth { gateway.signIn(email.trim(), password) }
        } else {
            signInDemo(email, password)
        }
    }

    override suspend fun resendSignUpConfirmation(email: String): AppResult<Unit> {
        if (!isValidEmail(email)) return AppResult.Failure(FailureReason.VALIDATION)
        if (!gateway.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)
        return remoteCall { gateway.resendSignUpConfirmation(email.trim()) }
    }

    override suspend fun checkEmailExists(email: String): AppResult<Boolean> {
        if (!isValidEmail(email)) return AppResult.Failure(FailureReason.VALIDATION)
        if (!gateway.isConfigured()) return AppResult.Success(false)
        return remoteCall { gateway.checkEmailExists(email.trim()) }
    }

    override suspend fun startGoogleSignIn(): AppResult<Unit> {
        if (!gateway.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)
        beginFreshAuthentication()
        return remoteCall { gateway.startGoogleSignIn() }
    }

    override suspend fun handleOAuthCallback(intent: Intent): AppResult<User?> = sessionOperationMutex.withLock {
        val passwordRecovery = gateway.isPasswordResetCallback(intent)
        when (val result = remoteCall {
            gateway.handleDeepLink(intent)
            gateway.currentUserOrNull()
        }) {
            is AppResult.Success -> {
                if (result.value != null) persistAuthenticatedUser(result.value)
                preferences.setPasswordRecoveryPending(passwordRecovery && result.value != null)
                AppResult.Success(result.value)
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun completeRole(role: UserRole): AppResult<User> {
        val localUser = mutableCurrentUser.value ?: return AppResult.Failure(FailureReason.UNAUTHENTICATED)
        if (localUser.deletionPending) return AppResult.Failure(FailureReason.CONFLICT)
        if (!gateway.isConfigured()) {
            val updated = localUser.copy(role = role, updatedAt = System.currentTimeMillis())
            persistAuthenticatedUser(updated)
            return AppResult.Success(updated)
        }
        return when (val remote = remoteCall { gateway.currentUserOrNull() }) {
            is AppResult.Failure -> remote
            is AppResult.Success -> {
                val serverUser = remote.value ?: return AppResult.Failure(FailureReason.UNAUTHENTICATED)
                if (serverUser.deletionPending) {
                    persistAuthenticatedUser(serverUser)
                    AppResult.Failure(FailureReason.CONFLICT)
                } else when (roleAssignmentStateFor(role, serverUser.role)) {
                    RoleAssignmentState.MATCHES_REQUEST -> {
                        // A previous response may have been lost after the RPC committed. The
                        // same role is safe to recover, and avoids presenting a false failure.
                        persistAuthenticatedUser(serverUser)
                        AppResult.Success(serverUser)
                    }
                    RoleAssignmentState.CONFLICTS_WITH_REQUEST -> {
                        // Do not silently enter a workspace for a role the caller did not
                        // request. The selection screen keeps the original request retry-only.
                        AppResult.Failure(FailureReason.CONFLICT)
                    }
                    RoleAssignmentState.UNASSIGNED -> when (val saved = remoteCall { gateway.saveRole(serverUser, role) }) {
                        is AppResult.Success -> {
                            persistAuthenticatedUser(saved.value)
                            AppResult.Success(saved.value)
                        }
                        is AppResult.Failure -> recoverCommittedRoleAssignment(role, saved)
                    }
                }
            }
        }
    }

    override suspend fun restoreSession(): AppResult<User?> = sessionOperationMutex.withLock {
        if (!gateway.isConfigured()) {
            val cached = preferences.cachedUserId.first()?.let { dao.user(it)?.toDomain() }
            cached?.let { accountScope.activate(it.id) }
            mutableCurrentUser.value = cached
            return AppResult.Success(cached)
        }
        return when (val result = remoteCall { gateway.currentUserOrNull() }) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val remote = result.value
                if (remote != null) persistAuthenticatedUser(remote)
                else {
                    // A Supabase-backed build must not restore a stale local profile after sign-out.
                    clearLocalAccountState()
                }
                AppResult.Success(remote)
            }
        }
    }

    override suspend fun requestPasswordReset(email: String): AppResult<Unit> =
        if (!isValidEmail(email)) AppResult.Failure(FailureReason.VALIDATION)
        else if (!gateway.isConfigured()) AppResult.Failure(FailureReason.CONFIGURATION)
        else remoteCall { gateway.requestPasswordReset(email.trim()) }

    override suspend fun updatePassword(newPassword: String): AppResult<Unit> =
        if (!PasswordRules.isValid(newPassword)) AppResult.Failure(FailureReason.VALIDATION)
        else if (!gateway.isConfigured()) AppResult.Failure(FailureReason.CONFIGURATION)
        else remoteCall { gateway.updatePassword(newPassword) }

    override suspend fun finishPasswordRecovery(): AppResult<Unit> = try {
        preferences.setPasswordRecoveryPending(false)
        AppResult.Success(Unit)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.SERVER, error)
    }

    override suspend fun deleteAccount(currentPassword: String): AppResult<AccountDeletionOutcome> {
        val localUser = mutableCurrentUser.value ?: return AppResult.Failure(FailureReason.UNAUTHENTICATED)
        if (currentPassword.isBlank()) return AppResult.Failure(FailureReason.VALIDATION)
        if (!gateway.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)

        return when (val result = remoteCall {
            // Do not call beginFreshAuthentication(): a wrong password must leave the user's
            // cached workspace intact. The protected server function proves this password belongs
            // to the caller identified by the JWT before it uses privileged APIs.
            gateway.deleteMyAccount(currentPassword)
        }) {
            is AppResult.Failure -> result
            is AppResult.Success -> when (result.value) {
                AccountDeletionOutcome.Deleted -> {
                    // The server acknowledgement is the only deletion-success signal. Now close
                    // workers, Room cache and the persisted access token without another network call.
                    clearLocalAccountState()
                    runCatching { gateway.clearSessionAfterAccountDeletion() }
                    AppResult.Success(AccountDeletionOutcome.Deleted)
                }
                AccountDeletionOutcome.FinalizationPending -> {
                    // Preparation has already removed role/workspace privileges. Persist a
                    // terminal local state immediately so this process cannot remain on a home
                    // screen while Storage/Auth finalisation is waiting for a safe retry.
                    persistAuthenticatedUser(
                        localUser.copy(
                            displayName = "Deletion pending",
                            role = null,
                            updatedAt = System.currentTimeMillis(),
                            deletionPending = true
                        )
                    )
                    AppResult.Success(AccountDeletionOutcome.FinalizationPending)
                }
                AccountDeletionOutcome.ReauthenticationRequired,
                AccountDeletionOutcome.PasswordReauthenticationUnavailable,
                is AccountDeletionOutcome.Blocked -> result
            }
        }
    }

    override suspend fun signOut(): AppResult<Unit> {
        // Close the local execution boundary before revoking the remote session so an already
        // scheduled worker cannot continue as the account being signed out.
        clearLocalAccountState()
        // A local sign-out must succeed even when the network cannot revoke the remote token.
        // The gateway clears the persisted Supabase session in its finally block.
        runCatching {
            if (gateway.isConfigured()) withTimeout(AUTH_REQUEST_TIMEOUT_MILLIS) { gateway.signOut() }
        }
        return AppResult.Success(Unit)
    }

    private suspend fun runAuth(action: suspend () -> User): AppResult<User> = when (val result = remoteCall(action)) {
        is AppResult.Success -> {
            persistAuthenticatedUser(result.value)
            AppResult.Success(result.value)
        }
        is AppResult.Failure -> result
    }

    /**
     * A timeout or response-decoding failure does not prove that Postgres rolled back the RPC.
     * Read the authoritative profile once before reporting failure; the RPC is atomic and the
     * same requested role is idempotent, so this safely recovers a committed first selection.
     */
    private suspend fun recoverCommittedRoleAssignment(
        requestedRole: UserRole,
        originalFailure: AppResult.Failure
    ): AppResult<User> = when (val remote = remoteCall { gateway.currentUserOrNull() }) {
        is AppResult.Failure -> originalFailure
        is AppResult.Success -> when (val serverUser = remote.value) {
            null -> originalFailure
            else -> when {
                serverUser.deletionPending -> {
                    persistAuthenticatedUser(serverUser)
                    AppResult.Failure(FailureReason.CONFLICT)
                }
                roleAssignmentStateFor(requestedRole, serverUser.role) == RoleAssignmentState.MATCHES_REQUEST -> {
                    persistAuthenticatedUser(serverUser)
                    AppResult.Success(serverUser)
                }
                roleAssignmentStateFor(requestedRole, serverUser.role) == RoleAssignmentState.CONFLICTS_WITH_REQUEST ->
                    AppResult.Failure(FailureReason.CONFLICT)
                else -> originalFailure
            }
        }
    }

    private suspend fun <T> remoteCall(action: suspend () -> T): AppResult<T> = try {
        AppResult.Success(withTimeout(AUTH_REQUEST_TIMEOUT_MILLIS) { action() })
    } catch (error: TimeoutCancellationException) {
        AppResult.Failure(FailureReason.OFFLINE, error)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        AppResult.Failure(failureReason(error), error)
    }

    private fun failureReason(error: Throwable): FailureReason = when (error) {
        is AuthWeakPasswordException -> FailureReason.VALIDATION
        is AuthRestException -> when {
            error.message?.contains("already registered", ignoreCase = true) == true -> FailureReason.ACCOUNT_ALREADY_EXISTS
            error.errorCode == AuthErrorCode.EmailNotConfirmed -> FailureReason.EMAIL_CONFIRMATION_REQUIRED
            error.errorCode == AuthErrorCode.InvalidCredentials || error.errorCode == AuthErrorCode.UserNotFound -> FailureReason.UNAUTHENTICATED
            error.errorCode == AuthErrorCode.OverEmailSendRateLimit || error.errorCode == AuthErrorCode.OverRequestRateLimit -> FailureReason.RATE_LIMITED
            else -> FailureReason.SERVER
        }
        else -> FailureReason.SERVER
    }

    private suspend fun persistAuthenticatedUser(user: User) {
        val previous = accountScope.accountId.value ?: preferences.cachedUserId.first()
        if (previous != null && previous != user.id) sessionCleaner.clear(previous)
        accountScope.activate(user.id)
        dao.upsertUser(user.toEntity())
        preferences.cacheAccount(user.id, user.role)
        mutableCurrentUser.value = user
    }

    /** Starts a new identity flow after closing and purging the previous account boundary. */
    private suspend fun beginFreshAuthentication() {
        clearLocalAccountState()
    }

    private suspend fun clearLocalAccountState() {
        val accountId = accountScope.accountId.value ?: preferences.cachedUserId.first()
        mutableCurrentUser.value = null
        preferences.setPasswordRecoveryPending(false)
        sessionCleaner.clear(accountId)
    }

    private suspend fun signInDemo(email: String, password: String): AppResult<User> {
        val normalisedEmail = email.trim().lowercase(Locale.ROOT)
        val role = demoRoles[normalisedEmail] ?: return AppResult.Failure(FailureReason.CONFIGURATION)
        if (password != demoPassword) return AppResult.Failure(FailureReason.UNAUTHENTICATED)
        val now = System.currentTimeMillis()
        val user = User(
            id = "demo-$role",
            email = normalisedEmail,
            displayName = role.name.lowercase().replaceFirstChar(Char::titlecase),
            role = role,
            createdAt = now,
            updatedAt = now
        )
        persistAuthenticatedUser(user)
        return AppResult.Success(user)
    }

    private fun validateCredentials(email: String, password: String): FailureReason? = when {
        !isValidEmail(email) -> FailureReason.VALIDATION
        !PasswordRules.isValid(password) -> FailureReason.VALIDATION
        else -> null
    }

    private fun isValidEmail(email: String): Boolean = email.trim().let { value ->
        value.length <= MAX_EMAIL_LENGTH && emailPattern.matches(value)
    }

    private companion object {
        const val AUTH_REQUEST_TIMEOUT_MILLIS = 15_000L
        const val MAX_EMAIL_LENGTH = 254
        const val demoPassword = "reeventdemo"
        val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        val demoRoles = mapOf(
            "organizer@reevent.demo" to UserRole.ORGANIZER,
            "participant@reevent.demo" to UserRole.PARTICIPANT,
            "partner@reevent.demo" to UserRole.PARTNER
        )
    }
}
