package com.reevent.app.ui

import com.reevent.app.core.model.UserRole

/** Destinations that can be selected directly from an authenticated navigation bar. */
enum class TopLevelDestination {
    HOME,
    MARKETPLACE,
    EVENTS,
    PARTNERS,
    PROGRAMMES,
    IMPACT,
    ACCOUNT,
}

internal fun topLevelDestinations(role: UserRole): List<TopLevelDestination> =
    when (role) {
        UserRole.ORGANIZER -> {
            listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.MARKETPLACE,
                TopLevelDestination.EVENTS,
                TopLevelDestination.PARTNERS,
                TopLevelDestination.IMPACT,
            )
        }

        UserRole.PARTICIPANT -> {
            listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.MARKETPLACE,
                TopLevelDestination.PARTNERS,
                TopLevelDestination.ACCOUNT,
            )
        }

        UserRole.PARTNER -> {
            listOf(
                TopLevelDestination.HOME,
                TopLevelDestination.MARKETPLACE,
                TopLevelDestination.PROGRAMMES,
                TopLevelDestination.ACCOUNT,
            )
        }
    }
