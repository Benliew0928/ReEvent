package com.reevent.app.feature.passports

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PassportViewerAccessTest {
    @Test
    fun `active organiser owner can open partner matching`() {
        val access = PassportViewerAccessPolicy.forViewer(user("owner", UserRole.ORGANIZER), resource(), emptyList())

        assertEquals("Organiser and current owner", access.label)
        assertTrue(access.canFindPartnerMatches)
    }

    @Test
    fun `participant holder is described without an organiser action`() {
        val transaction = transaction(receiverId = "participant", status = TransactionStatus.ACTIVE)
        val access = PassportViewerAccessPolicy.forViewer(user("participant", UserRole.PARTICIPANT), resource(), listOf(transaction))

        assertEquals("Participant and current holder", access.label)
        assertFalse(access.canFindPartnerMatches)
    }

    @Test
    fun `assigned partner receives a recovery context without extra actions`() {
        val transaction = transaction(partnerId = "partner", receiverId = "partner")
        val access = PassportViewerAccessPolicy.forViewer(user("partner", UserRole.PARTNER), resource(), listOf(transaction))

        assertEquals("Assigned recovery partner", access.label)
        assertFalse(access.canFindPartnerMatches)
    }

    private fun user(id: String, role: UserRole) = User(id, "$id@example.com", id, role, createdAt = 1L, updatedAt = 1L)

    private fun resource() = ResourceItem(
        id = "resource", eventId = "event", ownerId = "owner", title = "Display board", category = "Signage",
        materialFamily = com.reevent.app.core.model.MaterialFamily.WOOD, condition = ResourceCondition.GOOD, quantity = 1.0, unit = "ITEM", status = ResourceStatus.ACTIVE,
        valueCents = 0, imageUrls = emptyList(), createdAt = 1L, updatedAt = 1L
    )

    private fun transaction(
        receiverId: String = "participant",
        partnerId: String? = null,
        status: TransactionStatus = TransactionStatus.REQUESTED
    ) = CircularTransaction(
        id = "transaction", eventId = "event", resourceId = "resource", senderId = "owner", receiverId = receiverId,
        partnerId = partnerId, type = TransactionType.REPAIR, status = status, quantity = 1.0, createdAt = 1L, updatedAt = 1L,
        requesterId = "owner"
    )
}
