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
    @Test fun marketplace_request_blocks_owner_and_invalid_quantity() {
        val resource = resource(ownerId = "owner", quantity = 4)

        assertEquals(FailureReason.CONFLICT, TransactionWorkflow.validateMarketplaceRequest("owner", resource, 1))
        assertEquals(FailureReason.VALIDATION, TransactionWorkflow.validateMarketplaceRequest("buyer", resource, 0))
        assertEquals(FailureReason.VALIDATION, TransactionWorkflow.validateMarketplaceRequest("buyer", resource, 5))
        assertNull(TransactionWorkflow.validateMarketplaceRequest("buyer", resource, 4))
    }

    @Test fun marketplace_request_requires_available_resource() {
        val resource = resource(ownerId = "owner", status = ResourceStatus.RESERVED)

        assertEquals(FailureReason.CONFLICT, TransactionWorkflow.validateMarketplaceRequest("buyer", resource, 1))
    }

    @Test fun owner_actions_require_resource_owner_and_pending_requester() {
        val resource = resource(ownerId = "owner")
        val transaction = transaction(senderId = "buyer", receiverId = "owner")

        assertNull(TransactionWorkflow.validateOwnerAction("owner", resource, transaction))
        assertEquals(FailureReason.CONFLICT, TransactionWorkflow.validateOwnerAction("buyer", resource, transaction))
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validateOwnerAction("owner", resource, transaction.copy(senderId = "owner"))
        )
    }

    @Test fun transition_guards_match_mvp_status_flow() {
        val pending = transaction(status = TransactionStatus.PENDING)
        val accepted = transaction(status = TransactionStatus.ACCEPTED)
        val transit = transaction(status = TransactionStatus.IN_TRANSIT)
        val completed = transaction(status = TransactionStatus.COMPLETED)

        assertTrue(TransactionWorkflow.canApprove(pending))
        assertFalse(TransactionWorkflow.canApprove(accepted))
        assertTrue(TransactionWorkflow.canCancel(pending))
        assertTrue(TransactionWorkflow.canCancel(accepted))
        assertFalse(TransactionWorkflow.canCancel(completed))
        assertTrue(TransactionWorkflow.canMoveInTransit(accepted))
        assertFalse(TransactionWorkflow.canMoveInTransit(pending))
        assertTrue(TransactionWorkflow.canComplete(accepted))
        assertTrue(TransactionWorkflow.canComplete(transit))
        assertFalse(TransactionWorkflow.canComplete(pending))
    }

    @Test fun partner_handover_requires_owner_available_resource_and_compatible_active_programme() {
        val now = 1L
        val resource = resource(ownerId = "owner", quantity = 1)
        val programme = programme(ProgrammeType.REUSE, now)

        assertNull(TransactionWorkflow.validatePartnerHandover("owner", resource, programme))
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validatePartnerHandover("other-user", resource, programme)
        )
        assertEquals(
            FailureReason.CONFLICT,
            TransactionWorkflow.validatePartnerHandover("owner", resource.copy(status = ResourceStatus.RESERVED), programme)
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

    @Test fun cancellation_releases_only_reserved_resources() {
        assertEquals(
            ResourceStatus.AVAILABLE,
            TransactionWorkflow.resourceStatusAfterCancellation(ResourceStatus.RESERVED)
        )
        assertEquals(
            ResourceStatus.AVAILABLE,
            TransactionWorkflow.resourceStatusAfterCancellation(ResourceStatus.AVAILABLE)
        )
        assertEquals(
            ResourceStatus.HANDED_OVER,
            TransactionWorkflow.resourceStatusAfterCancellation(ResourceStatus.HANDED_OVER)
        )
    }

    @Test fun completion_status_and_programme_type_mapping_are_deterministic() {
        val now = 1L
        assertEquals(ResourceStatus.RECOVERED, TransactionWorkflow.resourceStatusAfterCompletion(TransactionType.RETURN))
        assertEquals(ResourceStatus.HANDED_OVER, TransactionWorkflow.resourceStatusAfterCompletion(TransactionType.RESALE))
        assertEquals(
            TransactionType.REPAIR,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.REPAIR, now))
        )
        assertEquals(
            TransactionType.RECYCLE,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.RECYCLE, now))
        )
        assertEquals(
            TransactionType.BUY_BACK,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.BUY_BACK, now))
        )
        assertEquals(
            TransactionType.DONATION,
            TransactionWorkflow.transactionTypeForProgramme(programme(ProgrammeType.REUSE, now))
        )
    }

    private fun resource(
        ownerId: String = "owner",
        quantity: Int = 2,
        status: ResourceStatus = ResourceStatus.AVAILABLE
    ) = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = ownerId,
        title = "Reusable signage",
        category = "Signage",
        material = "Acrylic",
        condition = ResourceCondition.GOOD,
        quantity = quantity,
        unit = "items",
        status = status,
        valueCents = 1000,
        imageUrls = emptyList(),
        createdAt = 1L,
        updatedAt = 1L
    )

    private fun transaction(
        senderId: String = "buyer",
        receiverId: String = "owner",
        status: TransactionStatus = TransactionStatus.PENDING
    ) = CircularTransaction(
        id = "transaction",
        eventId = "event",
        resourceId = "resource",
        senderId = senderId,
        receiverId = receiverId,
        partnerId = null,
        type = TransactionType.RESALE,
        status = status,
        quantity = 1,
        createdAt = 1L,
        updatedAt = 1L
    )

    private fun programme(type: ProgrammeType, now: Long) = CircularProgramme(
        id = "programme",
        partnerId = "partner",
        name = "Circular programme",
        type = type,
        acceptedMaterials = listOf("Acrylic"),
        location = "Kuala Lumpur",
        active = true,
        createdAt = now,
        updatedAt = now
    )
}
