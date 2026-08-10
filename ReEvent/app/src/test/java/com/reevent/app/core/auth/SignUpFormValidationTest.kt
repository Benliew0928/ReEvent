package com.reevent.app.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignUpFormValidationTest {
    @Test
    fun accepts_a_complete_valid_form() {
        val result = SignUpFormValidation.validate(
            displayName = "Aisha Tan",
            email = "aisha@example.com",
            password = "secure123",
            confirmation = "secure123"
        )

        assertTrue(result.isValid)
    }

    @Test
    fun returns_each_actionable_field_error() {
        val result = SignUpFormValidation.validate(
            displayName = "A",
            email = "not-an-email",
            password = "short",
            confirmation = "different"
        )

        assertEquals("Enter a name with at least 2 characters.", result.nameError)
        assertEquals("Enter a valid email address.", result.emailError)
        assertEquals("Use at least 8 characters.", result.passwordError)
        assertEquals("Passwords do not match.", result.confirmationError)
    }

    @Test
    fun trims_name_and_email_before_validating() {
        val result = SignUpFormValidation.validate(
            displayName = "  Mei  ",
            email = "  mei@example.com ",
            password = "secure123",
            confirmation = "secure123"
        )

        assertTrue(result.isValid)
    }
}
