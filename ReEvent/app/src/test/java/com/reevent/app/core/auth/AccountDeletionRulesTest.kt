package com.reevent.app.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDeletionRulesTest {
    @Test
    fun `requires the exact destructive phrase and a nonblank current password`() {
        val invalid = AccountDeletionRules.validate("delete my account", "")

        assertFalse(invalid.isValid)
        assertEquals("Type DELETE MY ACCOUNT exactly to continue.", invalid.confirmationError)
        assertEquals("Enter your current password to continue.", invalid.passwordError)
    }

    @Test
    fun `accepts the exact phrase and a nonblank password without imposing new-password strength rules`() {
        val validation = AccountDeletionRules.validate("DELETE MY ACCOUNT", "legacy")

        assertTrue(validation.isValid)
        assertNull(validation.confirmationError)
        assertNull(validation.passwordError)
    }

    @Test
    fun `maps only known safe server block statuses`() {
        assertEquals(AccountDeletionBlock.ACTIVE_TRANSACTIONS, accountDeletionBlockForServerStatus("BLOCKED_ACTIVE_TRANSACTIONS"))
        assertEquals(AccountDeletionBlock.UNSETTLED_COINS, accountDeletionBlockForServerStatus("BLOCKED_UNSETTLED_COINS"))
        assertNull(accountDeletionBlockForServerStatus("UNTRUSTED_SERVER_TEXT"))
    }
}
