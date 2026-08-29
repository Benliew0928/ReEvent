package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionType
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class EventDetailEditorialScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `event actions retain their existing callbacks`() {
        var backCount = 0
        var manageCount = 0
        var addCount = 0
        var scanCount = 0
        var archiveCount = 0

        compose.setContent {
            ReEventTheme {
                EventDetailEditorialContent(
                    event = event(),
                    resources = listOf(resource()),
                    transactions = emptyList(),
                    onBack = { backCount += 1 },
                    onEditEvent = { manageCount += 1 },
                    onAddResource = { addCount += 1 },
                    onScanResourceQr = { scanCount += 1 },
                    onEditResource = {},
                    onOpenPassport = {},
                    onArchiveResource = {},
                    onArchiveEvent = { archiveCount += 1 },
                    onNavigate = {},
                    loadPhoto = { null },
                )
            }
        }

        compose.onNodeWithTag("event_detail_back").performClick()
        compose.onNodeWithTag("event_detail_manage").performClick()
        compose.onNodeWithTag("event_detail_add_resource").performClick()
        compose.onNodeWithTag("event_detail_scan_qr").performClick()
        compose.onNodeWithTag("event_detail_archive_event").performScrollTo().performClick()

        assertEquals(1, backCount)
        assertEquals(1, manageCount)
        assertEquals(1, addCount)
        assertEquals(1, scanCount)
        assertEquals(1, archiveCount)
    }

    @Test
    fun `resource row falls back to material icon and expands existing actions`() {
        var passportId: String? = null
        var editId: String? = null
        var archiveId: String? = null
        val item = resource()

        compose.setContent {
            ReEventTheme {
                EventDetailEditorialContent(
                    event = event(),
                    resources = listOf(item),
                    transactions = emptyList(),
                    onBack = {},
                    onEditEvent = {},
                    onAddResource = {},
                    onScanResourceQr = {},
                    onEditResource = { editId = it },
                    onOpenPassport = { passportId = it },
                    onArchiveResource = { archiveId = it },
                    onArchiveEvent = {},
                    onNavigate = {},
                    loadPhoto = { null },
                )
            }
        }

        compose.onNodeWithContentDescription("Wood material icon").assertExists()
        compose.onNodeWithTag("event_resource_expand_${item.id}").performClick()
        compose.onNodeWithTag("event_resource_actions_${item.id}").assertExists()
        compose.onNodeWithText("Passport").performClick()
        compose.onNodeWithText("Edit").performClick()
        compose.onNodeWithText("Archive").performClick()

        assertEquals(item.id, passportId)
        assertEquals(item.id, editId)
        assertEquals(item.id, archiveId)
    }

    @Test
    fun `inventory filters retain listing and lifecycle status choices`() {
        val available = resource(id = "available", title = "Folding tables")
        val listed = resource(id = "listed", title = "Extension cables", listed = true)
        val recovered = resource(id = "recovered", title = "Recovered boards", status = ResourceStatus.RECOVERED)

        compose.setContent {
            ReEventTheme {
                EventDetailEditorialContent(
                    event = event(),
                    resources = listOf(available, listed, recovered),
                    transactions = emptyList(),
                    onBack = {},
                    onEditEvent = {},
                    onAddResource = {},
                    onScanResourceQr = {},
                    onEditResource = {},
                    onOpenPassport = {},
                    onArchiveResource = {},
                    onArchiveEvent = {},
                    onNavigate = {},
                    loadPhoto = { null },
                )
            }
        }

        compose.onNodeWithTag("event_detail_filter_listed").performClick()
        compose.onNodeWithText("Extension cables").assertExists()
        compose.onNodeWithText("Folding tables").assertDoesNotExist()

        compose.onNodeWithTag("event_detail_more_filters").performClick()
        compose.onNodeWithText("Recovered").performClick()
        compose.onNodeWithText("Recovered boards").assertExists()
        compose.onNodeWithText("Extension cables").assertDoesNotExist()
    }

    private fun event() =
        Event(
            id = "event",
            ownerId = "organizer",
            name = "Community Repair Day",
            description = "A neighbourhood repair event.",
            venue = "Ipoh, Majlis Bandaraya Ipoh",
            startsAt = 1_792_800_000_000L,
            endsAt = 1_792_972_800_000L,
            status = "LIVE",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
            syncState = SyncState.SYNCED,
        )

    private fun resource(
        id: String = "wood-resource",
        title: String = "Folding tables",
        status: ResourceStatus = ResourceStatus.ACTIVE,
        listed: Boolean = false,
    ) =
        ResourceItem(
            id = id,
            eventId = "event",
            ownerId = "organizer",
            title = title,
            category = "Furniture",
            materialFamily = MaterialFamily.WOOD,
            materialDetail = "Wood",
            condition = ResourceCondition.GOOD,
            quantity = 4.0,
            unit = "items",
            status = status,
            valueCents = 0,
            imageUrls = emptyList(),
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_700_000_000_000L,
            syncState = SyncState.SYNCED,
            marketplaceListing =
                if (listed) {
                    MarketplaceListing(
                        id = "listing-$id",
                        publishedQuantity = 4.0,
                        allowedActions = listOf(TransactionType.BUY),
                        defaultDurationDays = 7,
                        buyUnitPrice = 10,
                        rentUnitPrice = null,
                        terms = "Collection by appointment",
                    )
                } else {
                    null
                },
        )
}
