package com.reevent.app.ui.marketplace

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceDashboardMapperTest {
    @Test
    fun `search family and action filters use canonical material data`() {
        val state = mapMarketplaceDashboard(
            user(UserRole.PARTICIPANT),
            listOf(resource("wood", MaterialFamily.WOOD), resource("plastic", MaterialFamily.PLASTIC, "Acrylic")),
            emptyList(), emptyList(), emptyList(), emptyList(),
            MarketplaceFilters(query = "acrylic", materialFamily = MaterialFamily.PLASTIC, action = MarketplaceActionFilter.BUY),
        )
        assertEquals(listOf("plastic"), state.resources.map { it.resource.id })
        assertEquals(1, state.resultCount)
        assertEquals("My activity", state.activityTitle)
    }

    @Test
    fun `organiser ownership and publish ready resources stay authoritative`() {
        val owned = resource("owned", MaterialFamily.WOOD, owner = "user", listed = false)
        val listed = resource("listed", MaterialFamily.METAL, owner = "user")
        val state = mapMarketplaceDashboard(user(UserRole.ORGANIZER), listOf(listed), listOf(owned, listed), emptyList(), emptyList(), emptyList(), MarketplaceFilters())
        assertTrue(state.resources.single().isOwner)
        assertEquals(listOf("owned"), state.publishableResources.map(ResourceItem::id))
    }

    @Test
    fun `partner fit uses family and programme rules while cancelled activity is excluded`() {
        val item = resource("wood", MaterialFamily.WOOD)
        val programme = CircularProgramme(
            "programme", "user", "Wood repair", ProgrammeType.REPAIR, setOf(MaterialFamily.WOOD), "KL", true, 1, 1,
            acceptedConditions = setOf(ResourceCondition.GOOD),
        )
        val cancelled = CircularTransaction(
            "transaction", "event", item.id, "owner", "user", "user", TransactionType.REPAIR,
            TransactionStatus.CANCELLED, 1.0, 1, 1,
        )
        val state = mapMarketplaceDashboard(user(UserRole.PARTNER), listOf(item), emptyList(), listOf(cancelled), listOf(programme), emptyList(), MarketplaceFilters())
        assertEquals("Fits 1 programme", state.resources.single().programmeFitLabel)
        assertTrue(state.transactions.isEmpty())
        assertFalse(state.resources.single().isOwner)
    }

    private fun user(role: UserRole) = User("user", "user@example.test", "Alex Reed", role, createdAt = 1, updatedAt = 1)

    private fun resource(
        id: String,
        family: MaterialFamily,
        detail: String? = null,
        owner: String = "owner",
        listed: Boolean = true,
    ) = ResourceItem(
        id, "event", owner, id, "Furniture", family, detail, ResourceCondition.GOOD, 2.0, "ITEM",
        ResourceStatus.ACTIVE, 0, emptyList(), 1, 1, SyncState.SYNCED,
        marketplaceListing = if (listed) MarketplaceListing("listing-$id", listOf(TransactionType.BORROW, TransactionType.BUY), 2.0) else null,
    )
}
