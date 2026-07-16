package com.reevent.app.core.network

import android.content.Intent
import com.reevent.app.BuildConfig
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put

@Serializable
private data class ProfilePayload(
    val id: String,
    val email: String,
    val display_name: String,
    val role: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

/** Browser-based OAuth keeps Google sign-in available on HMS devices without Google Play Services. */
@Singleton
class SupabaseAuthGateway @Inject constructor() {
    private val configured = BuildConfig.SUPABASE_URL.startsWith("https://") && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                host = "auth"
                scheme = "reevent"
                flowType = FlowType.PKCE
                defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
            }
            install(Postgrest)
            install(Storage)
        }
    }

    fun isConfigured(): Boolean = configured

    suspend fun signUp(email: String, password: String, displayName: String): User? = withClient {
        // signUpWith does not replace an existing session when email confirmation is enabled.
        // Clear it first so an older organiser account can never be mistaken for the new user.
        client.auth.clearSession()
        client.auth.signUpWith(Email, redirectUrl = AUTH_CALLBACK_URL) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("display_name", displayName) }
        }
        val authenticated = client.auth.currentUserOrNull() ?: return@withClient null
        if (!authenticated.email.equals(email, ignoreCase = true)) {
            client.auth.clearSession()
            error("The sign-up response did not create the requested account session")
        }
        currentUser()
    }

    suspend fun signIn(email: String, password: String): User = withClient {
        client.auth.clearSession()
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        currentUser()
    }

    suspend fun startGoogleSignIn() = withClient {
        // The browser callback must establish a new identity, never reuse a stale local session.
        client.auth.clearSession()
        client.auth.signInWith(Google, redirectUrl = AUTH_CALLBACK_URL)
    }

    suspend fun requestPasswordReset(email: String) = withClient {
        client.auth.resetPasswordForEmail(email, redirectUrl = AUTH_CALLBACK_URL)
    }

    suspend fun resendSignUpConfirmation(email: String) = withClient {
        client.auth.resendEmail(OtpType.Email.SIGNUP, email, redirectUrl = AUTH_CALLBACK_URL)
    }

    suspend fun signOut() = withClient {
        try {
            client.auth.signOut()
        } finally {
            client.auth.clearSession()
        }
    }

    suspend fun currentUserOrNull(): User? = withClientOrNull {
        if (client.auth.currentUserOrNull() == null) null else currentUser()
    }

    suspend fun saveRole(user: User, role: UserRole): User = withClient {
        val profile = client.postgrest
            .rpc("complete_profile_role", buildJsonObject { put("p_role", role.name) })
            .decodeSingle<ProfilePayload>()
        val serverRole = profile.role?.let(::parseRole) ?: error("Supabase did not return the assigned role")
        check(serverRole == role) { "Supabase returned an unexpected role" }
        user.withProfile(profile)
    }

    suspend fun handleDeepLink(intent: Intent) {
        if (
            configured &&
            intent.data?.scheme == AUTH_CALLBACK_SCHEME &&
            intent.data?.host == AUTH_CALLBACK_HOST &&
            intent.data?.path == AUTH_CALLBACK_PATH
        ) {
            val callback = requireNotNull(intent.data)
            callback.getQueryParameter("error")?.let { errorCode ->
                error(callback.getQueryParameter("error_description") ?: errorCode)
            }
            check(!callback.getQueryParameter("code").isNullOrBlank()) {
                "The authentication callback did not include an authorization code"
            }
            // Supabase-kt starts the PKCE exchange in its own coroutine. Await its completion
            // before asking for currentUser(), otherwise this method observes a null session and
            // leaves the user at the credentials screen even though OAuth completed successfully.
            suspendCancellableCoroutine { continuation ->
                client.handleDeeplinks(
                    intent = intent,
                    onSessionSuccess = {
                        if (continuation.isActive) continuation.resume(Unit)
                    },
                    onError = { error ->
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
                )
            }
        }
    }

    suspend fun upsert(table: String, payload: JsonObject) = withClient {
        client.from(table).upsert(payload)
    }

    suspend fun archive(table: String, recordId: String) = withClient {
        client.from(table).update(buildJsonObject { put("archived", true) }) {
            filter { eq("id", recordId) }
        }
    }

    internal suspend fun <T> withConfiguredClient(block: suspend (SupabaseClient) -> T): T {
        check(configured) { "Supabase is not configured" }
        client.auth.awaitInitialization()
        return block(client)
    }

    private suspend fun currentUser(): User {
        val authenticated = client.auth.currentUserOrNull() ?: error("No authenticated Supabase user is available")
        val profile = client.from("profiles").select {
            filter { eq("id", authenticated.id) }
        }.decodeSingleOrNull<ProfilePayload>() ?: client.postgrest
            .rpc("ensure_current_profile")
            .decodeSingle<ProfilePayload>()
        return authenticated.toUser(profile)
    }

    private fun io.github.jan.supabase.auth.user.UserInfo.toUser(profile: ProfilePayload): User {
        val now = System.currentTimeMillis()
        return User(
            id = id,
            email = email.orEmpty(),
            displayName = profile.display_name.ifBlank { email?.substringBefore('@').orEmpty() },
            role = profile.role?.let(::parseRole),
            avatarUrl = profile.avatar_url,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun User.withProfile(profile: ProfilePayload): User = copy(
        email = profile.email.ifBlank { email },
        displayName = profile.display_name.ifBlank { displayName },
        role = profile.role?.let(::parseRole),
        avatarUrl = profile.avatar_url,
        updatedAt = System.currentTimeMillis()
    )

    private fun parseRole(value: String): UserRole = UserRole.entries.firstOrNull { it.name == value }
        ?: error("Unsupported role returned by Supabase")

    private suspend inline fun <T> withClient(block: suspend () -> T): T {
        check(configured) { "Supabase is not configured" }
        client.auth.awaitInitialization()
        return block()
    }

    private suspend inline fun <T> withClientOrNull(block: suspend () -> T): T? {
        if (!configured) return null
        client.auth.awaitInitialization()
        return block()
    }

    private companion object {
        const val AUTH_CALLBACK_SCHEME = "reevent"
        const val AUTH_CALLBACK_HOST = "auth"
        const val AUTH_CALLBACK_PATH = "/callback"
        const val AUTH_CALLBACK_URL = "$AUTH_CALLBACK_SCHEME://$AUTH_CALLBACK_HOST$AUTH_CALLBACK_PATH"
    }
}
