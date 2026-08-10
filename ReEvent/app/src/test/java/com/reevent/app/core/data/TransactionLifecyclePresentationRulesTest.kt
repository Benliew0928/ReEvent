package com.reevent.app.core.data

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionLifecyclePresentationRulesTest {
    @Test
    fun `organiser can approve a requested marketplace transaction`() {
        val presentation = TransactionLifecyclePresentationRules.forViewer("organiser", transaction())

        assertEquals("Requested", presentation.statusLabel)
        assertEquals("Organiser", presentation.responsibleRole)
        assertEquals(TransactionLifecycleCardAction.APPROVE, presentation.primaryAction)
        assertEquals("Approve request", presentation.primaryActionLabel)
        assertEquals(TransactionLifecycleCardAction.CANCEL, presentation.secondaryAction)
    }

    @Test
    fun `participant sees waiting guidance and can cancel their request`() {
        val presentation = TransactionLifecyclePresentationRules.forViewer("participant", transaction())

        assertNull(presentation.primaryAction)
        assertEquals("The resource owner must approve or decline this request.", presentation.nextStep)
        assertEquals(TransactionLifecycleCardAction.CANCEL, presentation.secondaryAction)
    }

    @Test
    fun `partner receipt has partner-specific label`() {
        val presentation = TransactionLifecyclePresentationRules.forViewer(
            "partner",
            transaction(status = TransactionStatus.IN_TRANSIT, partnerId = "partner", receiverId = "partner")
        )

        assertEquals("Partner", presentation.responsibleRole)
        assertEquals(TransactionLifecycleCardAction.CONFIRM_RECEIPT, presentation.primaryAction)
        assertEquals("Confirm recovery receipt", presentation.primaryActionLabel)
    }

    @Test
    fun `matching pending command blocks duplicate action without claiming success`() {
        val presentation = TransactionLifecyclePresentationRules.forViewer(
            "organiser",
            transaction(status = TransactionStatus.APPROVED),
            command = command(SyncState.PENDING)
        )

        assertEquals(TransactionLifecycleSyncFeedback.PENDING, presentation.syncFeedback)
        assertNull(presentation.primaryAction)
        assertEquals("ReEvent sync", presentation.responsibleRole)
    }

    @Test
    fun `failed command directs the user to profile retry`() {
        val presentation = TransactionLifecyclePresentationRules.forViewer(
            "organiser",
            transaction(status = TransactionStatus.APPROVED),
            command = command(SyncState.FAILED)
        )

        assertEquals(TransactionLifecycleSyncFeedback.FAILED, presentation.syncFeedback)
        assertNull(presentation.secondaryAction)
        assertEquals("You", presentation.responsibleRole)
    }

    @Test
    fun `completed transaction has no available action`() {
        val presentation = TransactionLifecyclePresentationRules.forViewer(
            "organiser",
            transaction(status = TransactionStatus.COMPLETED)
        )

        assertEquals("No one", presentation.responsibleRole)
        assertNull(presentation.primaryAction)
        assertNull(presentation.secondaryAction)
    }

    private fun transaction(
        status: TransactionStatus = TransactionStatus.REQUESTED,
        partnerId: String? = null,
        receiverId: String = "participant"
    ) = CircularTransaction(
        id = "transaction",
        eventId = "event",
        resourceId = "resource",
        senderId = "organiser",
        receiverId = receiverId,
        partnerId = partnerId,
        type = TransactionType.BORROW,
        status = status,
        quantity = 1.0,
        createdAt = 0,
        updatedAt = 0,
        requesterId = "participant"
    )

    private fun command(syncState: SyncState) = SyncCommandStatus(
        id = "command",
        queuePosition = 1,
        title = "Begin handover",
        detail = "Authorised transaction action",
        syncState = syncState,
        attempts = 1,
        transactionId = "transaction",
        lifecycleCommandType = "BEGIN_HANDOVER"
    )
}
