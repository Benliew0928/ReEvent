package com.reevent.app.feature.passports

import java.net.URI
import java.util.UUID

/**
 * The only newly-generated QR contract is an HTTPS public-verifier URL. It carries an opaque
 * server token, never a resource UUID, account ID, email, or transaction details.
 */
object PassportQrPayload {
    private const val PATH_SEGMENT = "p"
    private const val VERSION_SEGMENT = "v1"
    private val tokenPattern = Regex("^[A-Za-z0-9_-]{22}$")

    sealed interface Validation {
        data class Canonical(val token: String) : Validation
        /** Read-only compatibility for old codes; new passports never generate this format. */
        data object Legacy : Validation
        data class Invalid(val message: String) : Validation
    }

    /** Returns a renderable v1 QR URL only when this build has a valid HTTPS verifier host. */
    fun canonicalPayload(publicBaseUrl: String, publicToken: String): String? {
        if (!tokenPattern.matches(publicToken)) return null
        val base = configuredBaseUri(publicBaseUrl) ?: return null
        val basePath = base.rawPath.orEmpty().trimEnd('/')
        return "${base.scheme}://${base.authority}$basePath/$PATH_SEGMENT/$VERSION_SEGMENT/$publicToken"
    }

    fun validate(payload: String, publicBaseUrl: String): Validation {
        val value = payload.trim()
        if (value != payload || value.isEmpty()) return Validation.Invalid("Use the complete ReEvent passport QR value.")
        if (value.startsWith("reevent://")) return validateLegacy(value)

        val configuredBase = configuredBaseUri(publicBaseUrl)
            ?: return Validation.Invalid("Passport verifier is not configured in this build. Configure PUBLIC_BASE_URL before scanning v1 codes.")
        val uri = parseUri(value) ?: return Validation.Invalid("This is not a valid ReEvent passport QR code.")
        val basePath = configuredBase.rawPath.orEmpty().trimEnd('/')
        val expectedPrefix = "$basePath/$PATH_SEGMENT/$VERSION_SEGMENT/"
        val token = uri.rawPath?.removePrefix(expectedPrefix)

        return if (
            uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals(configuredBase.host, ignoreCase = true) &&
            uri.port == configuredBase.port &&
            uri.userInfo == null && uri.rawQuery == null && uri.rawFragment == null &&
            uri.rawPath?.startsWith(expectedPrefix) == true &&
            token != null && tokenPattern.matches(token) &&
            uri.rawPath == "$expectedPrefix$token"
        ) {
            Validation.Canonical(token)
        } else {
            Validation.Invalid("This QR is not a supported ReEvent v1 passport URL.")
        }
    }

    private fun validateLegacy(value: String): Validation {
        val uri = parseUri(value) ?: return Validation.Invalid("This is not a valid ReEvent passport QR code.")
        val id = uri.rawPath?.removePrefix("/")
        return if (
            uri.scheme.equals("reevent", ignoreCase = true) &&
            uri.host.equals("passport", ignoreCase = true) &&
            uri.rawQuery == null && uri.rawFragment == null &&
            id != null && uri.rawPath == "/$id" && runCatching { UUID.fromString(id) }.isSuccess
        ) {
            Validation.Legacy
        } else {
            Validation.Invalid("This legacy ReEvent passport QR code is malformed.")
        }
    }

    private fun configuredBaseUri(publicBaseUrl: String): URI? = parseUri(publicBaseUrl.trimEnd('/'))?.takeIf { base ->
        base.scheme.equals("https", ignoreCase = true) &&
            base.host != null &&
            base.userInfo == null &&
            base.rawQuery == null &&
            base.rawFragment == null
    }

    private fun parseUri(value: String): URI? = runCatching { URI(value) }.getOrNull()
}
