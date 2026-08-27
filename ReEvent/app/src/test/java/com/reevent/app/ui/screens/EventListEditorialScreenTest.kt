package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EventListEditorialScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `empty Events page keeps create and profile callbacks while showing the editorial empty state`() {
        var createCount = 0
        var destination: TopLevelDestination? = null

        compose.setContent {
            ReEventTheme {
                EventListEditorialContent(
                    user = organizer(),
                    events = emptyList(),
                    onCreate = { createCount += 1 },
                    onOpen = {},
                    onNavigate = { destination = it },
                )
            }
        }

        compose.onNodeWithText("Your events").assertIsDisplayed()
        compose.onNodeWithText("Nothing on the calendar yet").assertIsDisplayed()
        compose.onNodeWithTag("events_benefits").assertExists()
        compose.onNodeWithText("Past").assertDoesNotExist()

        compose.onNodeWithTag("events_create").performClick()
        compose.onNodeWithTag("events_avatar").performClick()

        assertEquals(1, createCount)
        assertEquals(TopLevelDestination.ACCOUNT, destination)
    }

    @Test
    fun `populated Events page renders data driven card and preserves open callback`() {
        var openedEventId: String? = null
        val event = event()

        compose.setContent {
            ReEventTheme {
                EventListEditorialContent(
                    user = organizer(),
                    events = listOf(event),
                    onCreate = {},
                    onOpen = { openedEventId = it.id },
                    onNavigate = {},
                )
            }
        }

        compose.onNodeWithTag("events_heading").assertIsDisplayed()
        compose.onNodeWithText("Green Campus Fest").assertIsDisplayed()
        compose.onNodeWithText("KL Eco Park").assertIsDisplayed()
        compose.onNodeWithText("Live").assertIsDisplayed()
        compose.onNodeWithText("Past").assertDoesNotExist()

        compose.onNodeWithTag("event_card_${event.id}").performClick()

        assertEquals(event.id, openedEventId)
    }

    private fun organizer() =
        User(
            id = "organizer",
            email = "mia@example.com",
            displayName = "Mia Young",
            role = UserRole.ORGANIZER,
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
        )

    private fun event() =
        Event(
            id = "green-campus",
            ownerId = "organizer",
            name = "Green Campus Fest",
            description = "A campus recovery event.",
            venue = "KL Eco Park",
            startsAt = 1_724_160_000_000L,
            endsAt = 1_724_246_400_000L,
            status = "LIVE",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
            syncState = SyncState.SYNCED,
        )
}
