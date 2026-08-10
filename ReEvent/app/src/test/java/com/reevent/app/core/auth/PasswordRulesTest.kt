package com.reevent.app.core.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordRulesTest {
    @Test
    fun `password must have at least eight characters`() {
        assertFalse(PasswordRules.isValid("seven77"))
        assertTrue(PasswordRules.isValid("eight888"))
    }

    @Test
    fun `confirmation must exactly match the new password`() {
        assertTrue(PasswordRules.matchesConfirmation("new-pass", "new-pass"))
        assertFalse(PasswordRules.matchesConfirmation("new-pass", "new-pass "))
    }
}
