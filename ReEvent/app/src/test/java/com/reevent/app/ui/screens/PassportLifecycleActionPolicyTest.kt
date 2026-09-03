package com.reevent.app.ui.screens

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PassportLifecycleActionPolicyTest {
    @Test
    fun `active organiser resource without a transaction only offers mark damaged`() {
        assertEquals(
            listOf(ResourceLifecycleAction.MARK_DAMAGED),
            PassportLifecycleActionPolicy.availableActions(organizer, resource(), emptyList()),
        )
        assertNull(PassportLifecycleActionPolicy.transactionFor(ResourceLifecycleAction.CHECK_OUT, organizer, emptyList()))
        assertNull(PassportLifecycleActionPolicy.transactionFor(ResourceLifecycleAction.RETURN, organizer, emptyList()))
    }

    @Test
    fun `approved owner transaction offers the handover action tied to that transaction`() {
        val transaction = transaction(status = TransactionStatus.APPROVED)

        val actions = PassportLifecycleActionPolicy.availableActions(organizer, resource(), listOf(transaction))

        assertTrue(ResourceLifecycleAction.CHECK_OUT in actions)
        assertEquals(transaction, PassportLifecycleActionPolicy.transactionFor(ResourceLifecycleAction.CHECK_OUT, organizer, listOf(transaction)))
        assertTrue(ResourceLifecycleAction.MARK_DAMAGED !in actions)
    }

    @Test
    fun `participant sees return only for an authorised active returnable transaction`() {
        val transaction = transaction(
            receiverId = participant.id,
            status = TransactionStatus.ACTIVE,
            type = TransactionType.BORROW,
        )

        assertEquals(
            listOf(ResourceLifecycleAction.RETURN),
            PassportLifecycleActionPolicy.availableActions(participant, resource(), listOf(transaction)),
        )
        assertEquals(transaction, PassportLifecycleActionPolicy.transactionFor(ResourceLifecycleAction.RETURN, participant, listOf(transaction)))
    }

    @Test
    fun `partner passport never exposes organiser or participant actions`() {
        val partner = User("partner", "partner@example.com", "Partner", UserRole.PARTNER, createdAt = 1L, updatedAt = 1L)
        val transaction = transaction(
            receiverId = partner.id,
            status = TransactionStatus.IN_TRANSIT,
            type = TransactionType.RECYCLE,
        ).copy(partnerId = partner.id)

        assertTrue(PassportLifecycleActionPolicy.availableActions(partner, resource(), listOf(transaction)).isEmpty())
    }

    private fun resource() = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = organizer.id,
        title = "Display board",
        category = "Signage",
        materialFamily = MaterialFamily.WOOD,
        condition = ResourceCondition.GOOD,
        quantity = 1.0,
        unit = "ITEM",
        status = ResourceStatus.ACTIVE,
        valueCents = 0,
        imageUrls = emptyList(),
        createdAt = 1L,
        updatedAt = 1L,
    )

    private fun transaction(
        receiverId: String = participant.id,
        status: TransactionStatus,
        type: TransactionType = TransactionType.DONATE,
    ) = CircularTransaction(
        id = "transaction",
        eventId = "event",
        resourceId = "resource",
        senderId = organizer.id,
        receiverId = receiverId,
        partnerId = null,
        type = type,
        status = status,
        quantity = 1.0,
        createdAt = 1L,
        updatedAt = 1L,
        requesterId = participant.id,
    )

    private companion object {
        val organizer = User("organizer", "organizer@example.com", "Organizer", UserRole.ORGANIZER, createdAt = 1L, updatedAt = 1L)
        val participant = User("participant", "participant@example.com", "Participant", UserRole.PARTICIPANT, createdAt = 1L, updatedAt = 1L)
    }
}
