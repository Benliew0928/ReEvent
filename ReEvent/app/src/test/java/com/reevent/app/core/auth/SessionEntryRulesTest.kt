package com.reevent.app.core.auth

import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionEntryRulesTest {
    @Test
    fun `deletion pending is terminal before onboarding password recovery and role setup`() {
        val pending = user(role = null, deletionPending = true)

        assertEquals(
            AppEntry.DELETION_PENDING,
            sessionEntryFor(
                onboardingComplete = false,
                user = pending,
                hasRestored = true,
                passwordRecoveryPending = true
            )
        )
    }

    @Test
    fun `ordinary roleless account still enters role completion`() {
        assertEquals(
            AppEntry.COMPLETE_ROLE,
            sessionEntryFor(
                onboardingComplete = true,
                user = user(role = null, deletionPending = false),
                hasRestored = true,
                passwordRecoveryPending = false
            )
        )
    }

    @Test
    fun `restoration gate remains first`() {
        assertEquals(
            AppEntry.LOADING,
            sessionEntryFor(
                onboardingComplete = true,
                user = user(UserRole.ORGANIZER, deletionPending = true),
                hasRestored = false,
                passwordRecoveryPending = false
            )
        )
    }

    private fun user(role: UserRole?, deletionPending: Boolean) = User(
        id = "user-id",
        email = "user@example.test",
        displayName = "User",
        role = role,
        createdAt = 1,
        updatedAt = 1,
        deletionPending = deletionPending
    )
}
