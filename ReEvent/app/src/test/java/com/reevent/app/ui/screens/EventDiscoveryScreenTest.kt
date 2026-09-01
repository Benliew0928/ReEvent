package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.reevent.app.core.model.DiscoverableEvent
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class EventDiscoveryScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `published event list is read only and forwards selection`() {
        var selectedId: String? = null

        compose.setContent {
            ReEventTheme {
                EventDiscoveryListContent(
                    user = participant(),
                    events = listOf(event()),
                    onOpen = { selectedId = it },
                    onRefresh = {},
                    onBack = {},
                    onNavigate = {},
                )
            }
        }

        compose.onNodeWithText("Blood donation").assertIsDisplayed()
        compose.onNodeWithTag("discoverable_event_public-1").performClick()

        assertEquals("public-1", selectedId)
    }

    @Test
    fun `published event detail omits internal publication defaults`() {
        compose.setContent {
            ReEventTheme {
                EventDiscoveryDetailContent(
                    user = participant(),
                    event = event(),
                    onBack = {},
                    onNavigate = {},
                )
            }
        }

        compose.onNodeWithTag("discoverable_event_public_card").assertIsDisplayed()
        compose.onAllNodesWithText("RECOVERY GOAL").assertCountEquals(0)
        compose.onAllNodesWithText("Asia/Kuala_Lumpur", substring = true).assertCountEquals(0)
    }

    private fun participant() = User(
        id = "participant",
        email = "participant@example.test",
        displayName = "Participant",
        role = UserRole.PARTICIPANT,
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun event() = DiscoverableEvent(
        id = "public-1",
        name = "Blood donation",
        description = "Community donation drive",
        eventType = "COMMUNITY",
        startsAt = 1_793_001_600_000L,
        endsAt = 1_793_088_000_000L,
        timezoneId = "Asia/Kuala_Lumpur",
        venue = "Kampar, Malaysia",
        recoveryTargetPercent = 80.0,
    )
}
