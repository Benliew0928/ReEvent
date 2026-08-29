package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
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
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
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
        compose.onNodeWithTag("events_create").performClick()
        compose.onNodeWithTag("nav_home").performClick()
        compose.waitForIdle()

        assertEquals(1, createCount)
        assertEquals(TopLevelDestination.HOME, destination)
    }

    @Test
    fun `events list shows cards and forwards open callbacks`() {
        var selectedEvent: Event? = null

        compose.setContent {
            ReEventTheme {
                EventListEditorialContent(
                    user = organizer(),
                    events = listOf(event("evt-10", "Eco Summit")),
                    onCreate = {},
                    onOpen = { selectedEvent = it },
                    onNavigate = {},
                )
            }
        }

        compose.onNodeWithText("Eco Summit").assertIsDisplayed()
        compose.onNodeWithText("Ipoh City Hall").assertIsDisplayed()
        compose.onNodeWithTag("event_card_evt-10").performClick()

        assertEquals("evt-10", selectedEvent?.id)
    }

    private fun organizer() =
        User(
            id = "usr-1",
            email = "organizer@reevent.app",
            displayName = "City Event Team",
            role = UserRole.ORGANIZER,
            createdAt = 1000L,
            updatedAt = 1000L,
        )

    private fun event(id: String, name: String) =
        Event(
            id = id,
            ownerId = "usr-1",
            name = name,
            description = "Annual circular economy gathering.",
            venue = "Ipoh City Hall",
            startsAt = 1735689600000L,
            endsAt = 1735776000000L,
            status = "UPCOMING",
            createdAt = 1000L,
            updatedAt = 1000L,
            syncState = SyncState.SYNCED,
        )
}
