package com.reevent.app.core.auth

/**
 * Client-side checks for the sign-up form. Authentication still validates the
 * credentials remotely; this prevents avoidable sign-up attempts locally.
 */
data class SignUpFormValidation(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmationError: String? = null
) {
    val isValid: Boolean
        get() = nameError == null && emailError == null && passwordError == null && confirmationError == null

    companion object {
        fun validate(
            displayName: String,
            email: String,
            password: String,
            confirmation: String
        ): SignUpFormValidation = SignUpFormValidation(
            nameError = when {
                displayName.trim().length < 2 -> "Enter a name with at least 2 characters."
                displayName.trim().length > 80 -> "Your name must be 80 characters or fewer."
                else -> null
            },
            emailError = if (email.trim().matches(EMAIL_PATTERN)) null else "Enter a valid email address.",
            passwordError = if (PasswordRules.isValid(password)) null else {
                "Use at least ${PasswordRules.MIN_LENGTH} characters."
            },
            confirmationError = if (PasswordRules.matchesConfirmation(password, confirmation)) null else {
                "Passwords do not match."
            }
        )

        private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}
