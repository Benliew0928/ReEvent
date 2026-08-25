package com.reevent.app.ui

import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {
    @Test
    fun `organizer destinations cover the complete navigation bar`() {
        assertEquals(
            listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.MARKETPLACE,
                TopLevelDestination.EVENTS,
                TopLevelDestination.PARTNERS,
                TopLevelDestination.IMPACT,
            ),
            topLevelDestinations(UserRole.ORGANIZER),
        )
    }

    @Test
    fun `participant and partner destinations stay role specific`() {
        assertEquals(
            listOf(
                TopLevelDestination.RETURNS,
                TopLevelDestination.MARKETPLACE,
                TopLevelDestination.PARTNERS,
                TopLevelDestination.ACCOUNT,
            ),
            topLevelDestinations(UserRole.PARTICIPANT),
        )
        assertEquals(
            listOf(
                TopLevelDestination.WORKBENCH,
                TopLevelDestination.MARKETPLACE,
                TopLevelDestination.ACCOUNT,
            ),
            topLevelDestinations(UserRole.PARTNER),
        )
    }
}
