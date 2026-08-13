package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionType
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FeatureSemanticsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `resource status is read only and explains automatic lifecycle updates`() {
        compose.setContent {
            ReEventTheme { ResourceStatusSummary(ResourceStatus.ACTIVE, SyncState.SYNCED) }
        }

        compose.onNodeWithText("Active").assertIsDisplayed()
        compose
            .onNodeWithText("Status updates automatically when lifecycle actions are confirmed.")
            .assertIsDisplayed()
        compose.onNodeWithText("Update status", substring = true).assertDoesNotExist()
    }

    @Test
    fun `marketplace details expose listing and resource fields without event context`() {
        compose.setContent {
            ReEventTheme { MarketplaceListingDetails(listedResource()) }
        }

        compose.onNodeWithText("4 items available").assertIsDisplayed()
        compose.onNodeWithText("Request terms").assertIsDisplayed()
        compose.onNodeWithText("Event context").assertDoesNotExist()
        compose
            .onNodeWithText("Event details are not currently available", substring = true)
            .assertDoesNotExist()
    }

    private fun listedResource(): ResourceItem {
        val now = 1_700_000_000_000L
        return ResourceItem(
            id = "resource",
            eventId = "private-event",
            ownerId = "owner",
            title = "Reusable display stands",
            category = "Exhibition",
            material = "Aluminium",
            condition = ResourceCondition.GOOD,
            quantity = 4.0,
            unit = "items",
            status = ResourceStatus.ACTIVE,
            valueCents = 12_000,
            imageUrls = emptyList(),
            createdAt = now,
            updatedAt = now,
            syncState = SyncState.SYNCED,
            marketplaceListing =
                MarketplaceListing(
                    id = "listing",
                    publishedQuantity = 4.0,
                    allowedActions = listOf(TransactionType.BUY, TransactionType.BORROW),
                    defaultDurationDays = 7,
                    buyUnitPrice = 30,
                    rentUnitPrice = null,
                    terms = "Collection by appointment",
                ),
        )
    }
}
