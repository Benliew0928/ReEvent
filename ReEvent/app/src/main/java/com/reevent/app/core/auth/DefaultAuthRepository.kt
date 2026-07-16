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

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val gateway: SupabaseAuthGateway,
    private val dao: CoreDao,
    private val preferences: AppPreferences,
    private val accountScope: AccountScope
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

    override suspend fun startGoogleSignIn(): AppResult<Unit> {
        if (!gateway.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)
        beginFreshAuthentication()
        return remoteCall { gateway.startGoogleSignIn() }
    }

    override suspend fun handleOAuthCallback(intent: Intent): AppResult<User?> = sessionOperationMutex.withLock {
        when (val result = remoteCall {
            gateway.handleDeepLink(intent)
            gateway.currentUserOrNull()
        }) {
            is AppResult.Success -> {
                if (result.value != null) persistAuthenticatedUser(result.value)
                AppResult.Success(result.value)
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun completeRole(role: UserRole): AppResult<User> {
        val localUser = mutableCurrentUser.value ?: return AppResult.Failure(FailureReason.UNAUTHENTICATED)
        if (!gateway.isConfigured()) {
            val updated = localUser.copy(role = role, updatedAt = System.currentTimeMillis())
            persistAuthenticatedUser(updated)
            return AppResult.Success(updated)
        }
        return when (val remote = remoteCall { gateway.currentUserOrNull() }) {
            is AppResult.Failure -> remote
            is AppResult.Success -> {
                val serverUser = remote.value ?: return AppResult.Failure(FailureReason.UNAUTHENTICATED)
                if (serverUser.role != null) {
                    persistAuthenticatedUser(serverUser)
                    AppResult.Success(serverUser)
                } else when (val saved = remoteCall { gateway.saveRole(serverUser, role) }) {
                    is AppResult.Success -> {
                        persistAuthenticatedUser(saved.value)
                        AppResult.Success(saved.value)
                    }
                    is AppResult.Failure -> saved
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
                    preferences.clearAccount()
                    accountScope.clear()
                    mutableCurrentUser.value = null
                }
                AppResult.Success(remote)
            }
        }
    }

    override suspend fun requestPasswordReset(email: String): AppResult<Unit> =
        if (!isValidEmail(email)) AppResult.Failure(FailureReason.VALIDATION)
        else if (!gateway.isConfigured()) AppResult.Failure(FailureReason.CONFIGURATION)
        else remoteCall { gateway.requestPasswordReset(email.trim()) }

    override suspend fun signOut(): AppResult<Unit> {
        // A local sign-out must succeed even when the network cannot revoke the remote token.
        // The gateway clears the persisted Supabase session in its finally block.
        runCatching { if (gateway.isConfigured()) gateway.signOut() }
        clearLocalAccountState()
        return AppResult.Success(Unit)
    }

    private suspend fun runAuth(action: suspend () -> User): AppResult<User> = when (val result = remoteCall(action)) {
        is AppResult.Success -> {
            persistAuthenticatedUser(result.value)
            AppResult.Success(result.value)
        }
        is AppResult.Failure -> result
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
        val previous = accountScope.accountId.value
        if (previous != user.id) clearOtherAccountCache(user.id)
        accountScope.activate(user.id)
        dao.upsertUser(user.toEntity())
        preferences.cacheAccount(user.id, user.role)
        mutableCurrentUser.value = user
    }

    private suspend fun clearOtherAccountCache(accountId: String) {
        dao.deleteOtherUsers(accountId)
        dao.clearOtherEvents(accountId)
        dao.clearOtherResources(accountId)
        dao.clearOtherPassports(accountId)
        dao.clearOtherProgrammes(accountId)
        dao.clearOtherTransactions(accountId)
        dao.clearOtherImpact(accountId)
        dao.clearOtherOutbox(accountId)
    }

    /** Starts a new identity flow. Account-scoped feature data remains inaccessible until the
     * same account authenticates, and is deleted by [clearOtherAccountCache] on an account switch. */
    private suspend fun beginFreshAuthentication() {
        clearLocalAccountState()
    }

    private suspend fun clearLocalAccountState() {
        mutableCurrentUser.value = null
        accountScope.clear()
        dao.clearUsers()
        preferences.clearAccount()
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
        password.length < 8 -> FailureReason.VALIDATION
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
