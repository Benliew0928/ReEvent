package com.reevent.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.LocalUserRole
import com.reevent.app.ui.theme.ReEventTheme

@Preview(name = "Participant · 360×800", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ParticipantCompactPreview() = HomePreview(HomeRole.PARTICIPANT, UserRole.PARTICIPANT)

@Preview(name = "Organiser · 430×1024", widthDp = 430, heightDp = 1024, showBackground = true)
@Composable
private fun OrganizerReferencePreview() = HomePreview(HomeRole.ORGANIZER, UserRole.ORGANIZER)

@Preview(name = "Partner · tablet rail", widthDp = 720, heightDp = 1024, showBackground = true)
@Composable
private fun PartnerTabletPreview() = HomePreview(HomeRole.PARTNER, UserRole.PARTNER)

@Preview(name = "Organiser · large font", widthDp = 430, heightDp = 1024, fontScale = 1.5f, showBackground = true)
@Composable
private fun OrganizerLargeFontPreview() = HomePreview(HomeRole.ORGANIZER, UserRole.ORGANIZER)

@Composable
private fun HomePreview(role: HomeRole, userRole: UserRole) {
    CompositionLocalProvider(LocalUserRole provides userRole) {
        ReEventTheme {
            EditorialRoleHomeScreen(
                state = previewState(role),
                onScopeSelected = {},
                onTarget = {},
                onProfile = {},
                onRefresh = {},
                onRetry = {},
            )
        }
    }
}

private fun previewState(role: HomeRole): HomeDashboardUiState {
    val roleCopy = when (role) {
        HomeRole.ORGANIZER -> Triple("Close the loop", "recovery progress", "Spring Makers Market")
        HomeRole.PARTICIPANT -> Triple("Keep the\nloop moving.", "your activity progress", "All activity")
        HomeRole.PARTNER -> Triple("Materials in\nmotion.", "workflow completed", "Circular Partner Programme")
    }
    return HomeDashboardUiState(
        role = role,
        displayName = "Alex Rivera",
        greeting = "Good morning, Alex",
        greetingSubtitle = when (role) {
            HomeRole.ORGANIZER -> "Let’s close this event the right way."
            HomeRole.PARTICIPANT -> "Thanks for keeping the loop going."
            HomeRole.PARTNER -> "Here’s your partner overview."
        },
        scopeLabel = roleCopy.third,
        scopes = listOf(HomeScopeOption("scope", roleCopy.third)),
        selectedScopeId = "scope",
        heroEyebrow = if (role == HomeRole.PARTICIPANT) null else "PROGRAMME OVERVIEW",
        heroTitle = roleCopy.first,
        heroBody = "Track handovers and keep valuable resources in motion.",
        progress = 0.68f,
        progressLabel = roleCopy.second,
        metrics = listOf(
            HomeMetric("42", "Requested", "awaiting review", HomeIcon.REQUEST),
            HomeMetric("28", "Approved", "ready for pickup", HomeIcon.CHECK),
            HomeMetric("16", "In transit", "on its way", HomeIcon.TRUCK),
        ),
        priorityTitle = if (role == HomeRole.PARTICIPANT) "Your next steps" else "Priority inbox",
        priorities = listOf(
            HomePriority("priority", "REVIEW", "Review repair assessment", "Wooden Chair · 12 units", HomeIcon.LEAF, HomeTarget.FocusProgrammeTransaction("transaction")),
            HomePriority("priority-2", "APPROVAL", "Approve recycle collection", "Mixed Plastics · 85 kg", HomeIcon.RECYCLE, HomeTarget.FocusProgrammeTransaction("transaction-2")),
        ),
        stripTitle = if (role == HomeRole.PARTNER) "Programme health" else "Your impact so far",
        stripMetrics = listOf(
            HomeMetric("68%", "Recovery rate", icon = HomeIcon.LEAF),
            HomeMetric("214 kg", "Materials diverted", icon = HomeIcon.RESOURCE),
            HomeMetric("1.3 t", "CO₂e avoided", icon = HomeIcon.IMPACT),
        ),
        quickLinks = listOf(
            HomeQuickLink("Programmes", "View and manage active programmes", HomeIcon.PROGRAMME, HomeTarget.Destination(TopLevelDestination.PROGRAMMES)),
            HomeQuickLink("Resources", "Guides and best practice", HomeIcon.RESOURCE, HomeTarget.Destination(TopLevelDestination.MARKETPLACE)),
            HomeQuickLink("Account", "Manage your profile", HomeIcon.ACCOUNT, HomeTarget.Destination(TopLevelDestination.ACCOUNT)),
        ),
    )
}
