package com.reevent.app.core.data

import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceListingDraftRulesTest {
    @Test
    fun `accepts a valid borrow and rent listing`() {
        val result = MarketplaceListingDraftRules.validate(
            resource(unit = "ITEM", quantity = 8.0),
            MarketplaceListingDraft(
                allowedActions = setOf(TransactionType.BORROW, TransactionType.RENT),
                publishedQuantity = 6.0,
                rentUnitPrice = 25,
                defaultDurationDays = 14,
                terms = "Return clean and ready for reuse."
            )
        )

        assertTrue(result.isValid)
    }

    @Test
    fun `requires action valid quantity and matching commercial terms`() {
        val result = MarketplaceListingDraftRules.validate(
            resource(unit = "BOX", quantity = 3.0),
            MarketplaceListingDraft(
                allowedActions = emptySet(),
                publishedQuantity = 2.5,
                buyUnitPrice = 10,
                rentUnitPrice = 4,
                defaultDurationDays = 10
            )
        )

        assertEquals("Select at least one available action.", result.actionError)
        assertEquals("BOX resources must be published as whole quantities.", result.quantityError)
        assertEquals("A Buy price is only allowed when Buy is selected.", result.buyPriceError)
        assertEquals("A Rent price is only allowed when Rent is selected.", result.rentPriceError)
        assertEquals("A duration is only used for Borrow or Rent.", result.durationError)
    }

    @Test
    fun `rejects a draft that exceeds resource quantity or is already listed`() {
        val listedResource = resource(quantity = 2.0).copy(
            marketplaceListing = MarketplaceListing("listing", listOf(TransactionType.DONATE), 2.0)
        )
        val result = MarketplaceListingDraftRules.validate(
            listedResource,
            MarketplaceListingDraft(setOf(TransactionType.DONATE), publishedQuantity = 3.0)
        )

        assertEquals("This resource already has an open marketplace listing.", result.resourceError)
        assertEquals("Published quantity cannot exceed the available resource quantity.", result.quantityError)
    }

    @Test
    fun `requires the resource to finish syncing before publication`() {
        val result = MarketplaceListingDraftRules.validate(
            resource().copy(syncState = SyncState.PENDING),
            MarketplaceListingDraft(setOf(TransactionType.DONATE), publishedQuantity = 1.0)
        )

        assertEquals("Wait for this resource to finish syncing before publishing it.", result.resourceError)
    }

    @Test
    fun `requires price and bounded duration for selected actions`() {
        val result = MarketplaceListingDraftRules.validate(
            resource(unit = "KG", quantity = 2.5),
            MarketplaceListingDraft(
                allowedActions = setOf(TransactionType.BUY, TransactionType.RENT),
                publishedQuantity = 2.25,
                defaultDurationDays = 366,
                terms = "x".repeat(MarketplaceListingDraftRules.MAX_TERMS_LENGTH + 1)
            )
        )

        assertEquals("Enter a Buy price in ReCoins.", result.buyPriceError)
        assertEquals("Enter a Rent price in ReCoins.", result.rentPriceError)
        assertEquals("Duration must be between 1 and 365 days.", result.durationError)
        assertEquals("Terms must be 2000 characters or fewer.", result.termsError)
    }

    private fun resource(unit: String = "ITEM", quantity: Double = 4.0) = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = "organiser",
        title = "Reusable display boards",
        category = "Signage",
        material = "Wood",
        condition = ResourceCondition.GOOD,
        quantity = quantity,
        unit = unit,
        status = ResourceStatus.ACTIVE,
        valueCents = 0,
        imageUrls = emptyList(),
        createdAt = 0,
        updatedAt = 0,
        syncState = SyncState.SYNCED
    )
}
