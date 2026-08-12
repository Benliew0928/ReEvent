package com.reevent.app.core.auth

/**
 * UI-only checks for a destructive request. The password is deliberately only accepted as an
 * argument at submission time; callers must never store it in preferences, Room, or logs.
 */
object AccountDeletionRules {
    const val CONFIRMATION_PHRASE = "DELETE MY ACCOUNT"

    fun validate(confirmationPhrase: String, currentPassword: String): AccountDeletionValidation =
        AccountDeletionValidation(
            confirmationError = if (confirmationPhrase == CONFIRMATION_PHRASE) null
            else "Type $CONFIRMATION_PHRASE exactly to continue.",
            passwordError = if (currentPassword.isNotBlank()) null else "Enter your current password to continue."
        )
}

data class AccountDeletionValidation(
    val confirmationError: String? = null,
    val passwordError: String? = null
) {
    val isValid: Boolean get() = confirmationError == null && passwordError == null
}

sealed interface AccountDeletionOutcome {
    data object Deleted : AccountDeletionOutcome
    data object FinalizationPending : AccountDeletionOutcome
    data object ReauthenticationRequired : AccountDeletionOutcome
    data object PasswordReauthenticationUnavailable : AccountDeletionOutcome
    data class Blocked(val reason: AccountDeletionBlock) : AccountDeletionOutcome
}

/** Values returned by the protected server function; no raw server error is shown to a user. */
enum class AccountDeletionBlock(val userMessage: String) {
    ACTIVE_TRANSACTIONS("Finish, cancel, or have the other party resolve your active transactions before deleting this account."),
    ACTIVE_RESOURCES("Archive or transfer your active resources before deleting this account."),
    ACTIVE_EVENTS("Archive your active events before deleting this account."),
    OPEN_LISTINGS("Close your open marketplace listings before deleting this account."),
    ACTIVE_PROGRAMMES("Deactivate your active partner programmes before deleting this account."),
    UNSETTLED_COINS("Wait for your unsettled ReCoin hold to finish before deleting this account.")
}

internal fun accountDeletionBlockForServerStatus(status: String): AccountDeletionBlock? = when (status) {
    "BLOCKED_ACTIVE_TRANSACTIONS" -> AccountDeletionBlock.ACTIVE_TRANSACTIONS
    "BLOCKED_ACTIVE_RESOURCES" -> AccountDeletionBlock.ACTIVE_RESOURCES
    "BLOCKED_ACTIVE_EVENTS" -> AccountDeletionBlock.ACTIVE_EVENTS
    "BLOCKED_OPEN_LISTINGS" -> AccountDeletionBlock.OPEN_LISTINGS
    "BLOCKED_ACTIVE_PROGRAMMES" -> AccountDeletionBlock.ACTIVE_PROGRAMMES
    "BLOCKED_UNSETTLED_COINS" -> AccountDeletionBlock.UNSETTLED_COINS
    else -> null
}

internal fun accountDeletionOutcomeForServerStatus(status: String): AccountDeletionOutcome? = when (status) {
    "DELETED" -> AccountDeletionOutcome.Deleted
    "FINALIZATION_PENDING" -> AccountDeletionOutcome.FinalizationPending
    "FRESH_REAUTHENTICATION_REQUIRED" -> AccountDeletionOutcome.ReauthenticationRequired
    "PASSWORD_REAUTHENTICATION_UNAVAILABLE" -> AccountDeletionOutcome.PasswordReauthenticationUnavailable
    else -> accountDeletionBlockForServerStatus(status)?.let { AccountDeletionOutcome.Blocked(it) }
}
