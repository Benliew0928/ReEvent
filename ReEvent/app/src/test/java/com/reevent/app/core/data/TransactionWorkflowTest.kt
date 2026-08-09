package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionWorkflowTest {
    @Test
    fun marketplace_preflight_blocks_owner_inactive_resource_and_invalid_quantity() {
        val active = resource(ownerId = "owner", quantity = 4.0)

        assertEquals(FailureReason.CONFLICT, TransactionWorkflow.validateMarketplaceRequest("owner", active, 1))
        assertEquals(FailureReason.VALIDATION, TransactionWorkflow.validateMarketplaceRequest("buyer", active, 0))
        assertEquals(FailureReason.VALIDATION, TransactionWorkflow.validateMarketplaceRequest("buyer", active, 5))
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validateMarketplaceRequest(
                "buyer",
                active.copy(status = ResourceStatus.RECOVERY_IN_PROGRESS),
                1
            )
        )
        assertNull(TransactionWorkflow.validateMarketplaceRequest("buyer", active, 4))
    }

    @Test
    fun programme_preflight_requires_owner_active_resource_and_compatible_programme() {
        val resource = resource(ownerId = "owner", quantity = 1.0)
        val programme = programme(ProgrammeType.REPAIR)

        assertNull(TransactionWorkflow.validatePartnerHandover("owner", resource, programme))
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validatePartnerHandover("other-user", resource, programme)
        )
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validatePartnerHandover(
                "owner",
                resource.copy(status = ResourceStatus.RECOVERY_IN_PROGRESS),
                programme
            )
        )
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validatePartnerHandover("owner", resource, programme.copy(active = false))
        )
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validatePartnerHandover(
                "owner",
                resource,
                programme.copy(acceptedMaterials = listOf("Fabric"))
            )
        )
    }

    @Test
    fun listing_decision_and_handover_actions_are_actor_and_state_scoped() {
        val requested = transaction(
            senderId = "seller",
            receiverId = "buyer",
            requesterId = "buyer"
        )
        val approved = requested.copy(status = TransactionStatus.APPROVED)

        assertTrue(TransactionWorkflow.canApprove("seller", requested))
        assertFalse(TransactionWorkflow.canApprove("buyer", requested))
        assertFalse(TransactionWorkflow.canApprove("seller", approved))
        assertTrue(TransactionWorkflow.canCancel("buyer", requested))
        assertTrue(TransactionWorkflow.canCancel("seller", approved))
        assertFalse(TransactionWorkflow.canCancel("stranger", requested))
        assertTrue(TransactionWorkflow.canBeginHandover("seller", approved))
        assertFalse(TransactionWorkflow.canBeginHandover("buyer", approved))
    }

    @Test
    fun programme_decision_is_reserved_for_partner() {
        val requested = transaction(
            senderId = "owner",
            receiverId = "partner",
            partnerId = "partner",
            requesterId = "owner",
            type = TransactionType.REPAIR
        )

        assertTrue(TransactionWorkflow.canApprove("partner", requested))
        assertFalse(TransactionWorkflow.canApprove("owner", requested))
        assertTrue(TransactionWorkflow.canCancel("owner", requested))
        assertTrue(TransactionWorkflow.canCancel("partner", requested))
    }

    @Test
    fun receipt_and_return_actions_follow_the_frozen_actor_state_machine() {
        val temporary = transaction(
            senderId = "owner",
            receiverId = "borrower",
            requesterId = "borrower",
            type = TransactionType.BORROW
        )

        assertTrue(
            TransactionWorkflow.canConfirmReceipt(
                "borrower",
                temporary.copy(status = TransactionStatus.IN_TRANSIT)
            )
        )
        assertTrue(
            TransactionWorkflow.canBeginReturn(
                "borrower",
                temporary.copy(status = TransactionStatus.ACTIVE)
            )
        )
        assertTrue(
            TransactionWorkflow.canConfirmReturn(
                "owner",
                temporary.copy(status = TransactionStatus.RETURN_IN_PROGRESS)
            )
        )
        assertFalse(
            TransactionWorkflow.canBeginReturn(
                "borrower",
                temporary.copy(type = TransactionType.BUY, status = TransactionStatus.ACTIVE)
            )
        )
        assertFalse(
            TransactionWorkflow.canConfirmReturn(
                "borrower",
                temporary.copy(status = TransactionStatus.RETURN_IN_PROGRESS)
            )
        )
    }

    @Test
    fun terminal_transactions_expose_no_display_actions() {
        val completed = transaction(status = TransactionStatus.COMPLETED)

        assertFalse(TransactionWorkflow.canApprove("seller", completed))
        assertFalse(TransactionWorkflow.canCancel("buyer", completed))
        assertFalse(TransactionWorkflow.canBeginHandover("seller", completed))
        assertFalse(TransactionWorkflow.canConfirmReceipt("buyer", completed))
        assertFalse(TransactionWorkflow.canBeginReturn("buyer", completed))
        assertFalse(TransactionWorkflow.canConfirmReturn("seller", completed))
    }

    @Test
    fun programme_type_mapping_covers_the_frozen_contract() {
        assertEquals(
            TransactionType.REPAIR,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.REPAIR))
        )
        assertEquals(
            TransactionType.RECYCLE,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.RECYCLE))
        )
        assertEquals(
            TransactionType.BUY_BACK,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.BUY_BACK))
        )
    }

    private fun resource(
        ownerId: String = "owner",
        quantity: Double = 2.0,
        status: ResourceStatus = ResourceStatus.ACTIVE
    ) = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = ownerId,
        title = "Reusable signage",
        category = "Signage",
        material = "Acrylic",
        condition = ResourceCondition.GOOD,
        quantity = quantity,
        unit = "ITEM",
        status = status,
        valueCents = 1000,
        imageUrls = emptyList(),
        createdAt = 1L,
        updatedAt = 1L
    )

    private fun transaction(
        senderId: String = "seller",
        receiverId: String = "buyer",
        partnerId: String? = null,
        requesterId: String = "buyer",
        type: TransactionType = TransactionType.BUY,
        status: TransactionStatus = TransactionStatus.REQUESTED
    ) = CircularTransaction(
        id = "transaction",
        eventId = "event",
        resourceId = "resource",
        senderId = senderId,
        receiverId = receiverId,
        partnerId = partnerId,
        type = type,
        status = status,
        quantity = 1.0,
        createdAt = 1L,
        updatedAt = 1L,
        requesterId = requesterId
    )

    private fun programme(type: ProgrammeType) = CircularProgramme(
        id = "programme",
        partnerId = "partner",
        name = "Circular programme",
        type = type,
        acceptedMaterials = listOf("Acrylic"),
        location = "Kuala Lumpur",
        active = true,
        createdAt = 1L,
        updatedAt = 1L
    )
}
