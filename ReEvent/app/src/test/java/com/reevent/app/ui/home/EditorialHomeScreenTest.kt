package com.reevent.app.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditorialHomeScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `organizer home exposes selector progress priorities and accessible profile`() {
        var selectedScope: String? = null
        var selectedTarget: HomeTarget? = null
        val state = populated(HomeRole.ORGANIZER).copy(
            scopeLabel = "Spring Makers Market",
            scopes = listOf(HomeScopeOption("event-a", "Spring Makers Market"), HomeScopeOption("event-b", "Autumn Market")),
            selectedScopeId = "event-a",
            heroTitle = "Close the loop",
        )
        setHome(state, onScopeSelected = { selectedScope = it }, onTarget = { selectedTarget = it })

        compose.onNodeWithText("Close the loop").assertIsDisplayed()
        compose.onNodeWithContentDescription("Profile for Alex Rivera").assertIsDisplayed()
        compose.onNodeWithTag("home_scope_selector").performClick()
        compose.onNodeWithText("Autumn Market").performClick()
        assertEquals("event-b", selectedScope)
        compose.onNodeWithTag("home_dashboard_list").performScrollToNode(hasTestTag("home_priority_section"))
        compose.onNodeWithText("Match wooden chairs").performClick()
        assertEquals(HomeTarget.MatchResource("resource"), selectedTarget)
    }

    @Test
    fun `participant home collapses next steps and keeps missing passport disabled`() {
        var selectedTarget: HomeTarget? = null
        val disabled = HomePriority(
            id = "passport",
            badge = "RETURN",
            title = "Show return passport",
            detail = "Present your passport",
            icon = HomeIcon.PASSPORT,
            target = null,
            disabledReason = "Passport is not available yet",
        )
        setHome(
            populated(HomeRole.PARTICIPANT).copy(
                heroTitle = "Keep the loop moving.",
                priorityTitle = "Your next steps",
                priorities = listOf(disabled),
            ),
            onTarget = { selectedTarget = it },
        )

        compose.onNodeWithTag("home_dashboard_list").performScrollToNode(hasTestTag("home_priority_section"))
        compose.onNodeWithText("Show return passport").performClick()
        assertEquals(null, selectedTarget)
        compose.onNodeWithText("Your next steps").performClick()
        compose.onNodeWithText("Show return passport").assertDoesNotExist()
    }

    @Test
    fun `partner home renders workflow language and retries inline refresh failure`() {
        var retries = 0
        setHome(
            populated(HomeRole.PARTNER).copy(
                heroTitle = "Materials in motion.",
                progressLabel = "workflow completed",
                refreshError = "Couldn’t refresh. Showing saved data.",
            ),
            onRetry = { retries++ },
        )

        compose.onNodeWithText("Materials in motion.").assertIsDisplayed()
        compose.onNodeWithTag("home_dashboard_list").performScrollToNode(hasText("workflow completed"))
        compose.onNodeWithText("workflow completed").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `role home displays honest empty action`() {
        val state = populated(HomeRole.PARTNER).copy(
            progress = null,
            emptyState = HomeEmptyState(
                "No circular programme yet",
                "Create a programme so organisers can discover your services.",
                "Create a programme",
                HomeTarget.CreateProgramme,
            ),
        )
        setHome(state)

        compose.onNodeWithTag("home_dashboard_list").performScrollToNode(hasTestTag("home_hero"))
        compose.onNodeWithContentDescription("workflow completed unavailable").assertIsDisplayed()
        compose.onNodeWithTag("home_dashboard_list").performScrollToNode(hasText("No circular programme yet"))
        compose.onNodeWithText("No circular programme yet").assertIsDisplayed()
    }

    private fun setHome(
        state: HomeDashboardUiState,
        onScopeSelected: (String) -> Unit = {},
        onTarget: (HomeTarget) -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        compose.setContent {
            ReEventTheme {
                EditorialRoleHomeScreen(
                    state = state,
                    onScopeSelected = onScopeSelected,
                    onTarget = onTarget,
                    onProfile = {},
                    onRefresh = {},
                    onRetry = onRetry,
                )
            }
        }
    }

    private fun populated(role: HomeRole) = HomeDashboardUiState(
        role = role,
        displayName = "Alex Rivera",
        greeting = "Good morning, Alex",
        greetingSubtitle = "Circular activity at a glance.",
        scopeLabel = "All activity",
        scopes = listOf(HomeScopeOption("all", "All activity")),
        selectedScopeId = "all",
        heroEyebrow = "OVERVIEW",
        heroTitle = "Close the loop",
        heroBody = "Every handover counts.",
        progress = 0.68f,
        progressLabel = if (role == HomeRole.PARTNER) "workflow completed" else "recovery progress",
        metrics = listOf(
            HomeMetric("4", "Active", icon = HomeIcon.HANDOVER),
            HomeMetric("2", "Ready", icon = HomeIcon.RETURN),
            HomeMetric("7", "Completed", icon = HomeIcon.CHECK),
        ),
        priorityTitle = "Priority inbox",
        priorities = listOf(
            HomePriority(
                id = "match",
                badge = "MATCH",
                title = "Match wooden chairs",
                detail = "Find the best recovery partner",
                icon = HomeIcon.RESOURCE,
                target = HomeTarget.MatchResource("resource"),
            ),
        ),
        stripTitle = "Your impact so far",
        stripMetrics = listOf(
            HomeMetric("68%", "Recovery", icon = HomeIcon.LEAF),
            HomeMetric("214 kg", "Diverted", icon = HomeIcon.RESOURCE),
            HomeMetric("1.3 t", "Avoided", icon = HomeIcon.IMPACT),
        ),
        quickLinks = listOf(
            HomeQuickLink("Resources", "Browse circular resources", HomeIcon.RESOURCE, HomeTarget.Destination(com.reevent.app.ui.TopLevelDestination.MARKETPLACE)),
        ),
    )
}
