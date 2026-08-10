package com.reevent.app.core.data

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType

/** A display model only: server state and durable commands remain the lifecycle authority. */
data class TransactionLifecyclePresentation(
    val statusLabel: String,
    val nextStep: String,
    val responsibleRole: String,
    val primaryAction: TransactionLifecycleCardAction? = null,
    val primaryActionLabel: String? = null,
    val secondaryAction: TransactionLifecycleCardAction? = null,
    val secondaryActionLabel: String? = null,
    val syncFeedback: TransactionLifecycleSyncFeedback? = null
)

enum class TransactionLifecycleCardAction {
    APPROVE,
    BEGIN_HANDOVER,
    CONFIRM_RECEIPT,
    BEGIN_RETURN,
    CONFIRM_RETURN,
    CANCEL
}

enum class TransactionLifecycleSyncFeedback {
    PENDING,
    FAILED
}

object TransactionLifecyclePresentationRules {
    fun forViewer(
        viewerId: String,
        transaction: CircularTransaction,
        command: SyncCommandStatus? = null
    ): TransactionLifecyclePresentation {
        val base = TransactionLifecyclePresentation(
            statusLabel = transaction.status.displayLabel(),
            nextStep = transaction.nextStep(),
            responsibleRole = transaction.responsibleRole(),
            primaryAction = transaction.permittedPrimaryAction(viewerId),
            primaryActionLabel = transaction.permittedPrimaryAction(viewerId)?.labelFor(transaction),
            secondaryAction = TransactionLifecycleCardAction.CANCEL.takeIf {
                TransactionWorkflow.canCancel(viewerId, transaction)
            },
            secondaryActionLabel = TransactionLifecycleCardAction.CANCEL.labelFor(transaction)
        )
        return when (command?.syncState) {
            SyncState.PENDING -> base.copy(
                primaryAction = null,
                primaryActionLabel = null,
                secondaryAction = null,
                secondaryActionLabel = null,
                syncFeedback = TransactionLifecycleSyncFeedback.PENDING,
                nextStep = "Your authorised action is waiting to sync. The server state will update after it is processed.",
                responsibleRole = "ReEvent sync"
            )

            SyncState.FAILED -> base.copy(
                primaryAction = null,
                primaryActionLabel = null,
                secondaryAction = null,
                secondaryActionLabel = null,
                syncFeedback = TransactionLifecycleSyncFeedback.FAILED,
                nextStep = "This action was not confirmed. Open Profile and use Retry sync, then refresh this transaction.",
                responsibleRole = "You"
            )

            else -> base
        }
    }

    private fun CircularTransaction.permittedPrimaryAction(viewerId: String): TransactionLifecycleCardAction? = when {
        TransactionWorkflow.canApprove(viewerId, this) -> TransactionLifecycleCardAction.APPROVE
        TransactionWorkflow.canBeginHandover(viewerId, this) -> TransactionLifecycleCardAction.BEGIN_HANDOVER
        TransactionWorkflow.canConfirmReceipt(viewerId, this) -> TransactionLifecycleCardAction.CONFIRM_RECEIPT
        TransactionWorkflow.canBeginReturn(viewerId, this) -> TransactionLifecycleCardAction.BEGIN_RETURN
        TransactionWorkflow.canConfirmReturn(viewerId, this) -> TransactionLifecycleCardAction.CONFIRM_RETURN
        else -> null
    }

    private fun CircularTransaction.nextStep(): String = when (status) {
        TransactionStatus.REQUESTED -> if (partnerId != null) {
            "The assigned partner must accept or decline this recovery request."
        } else {
            "The resource owner must approve or decline this request."
        }

        TransactionStatus.APPROVED -> "The resource owner must begin the handover."
        TransactionStatus.IN_TRANSIT -> if (partnerId != null) {
            "The assigned partner must confirm recovery receipt."
        } else {
            "The receiving participant must confirm receipt."
        }

        TransactionStatus.ACTIVE -> if (type in returnableTypes) {
            "The current holder must begin the return when the resource is ready."
        } else {
            "The server will record the next authorised lifecycle outcome."
        }

        TransactionStatus.RETURN_IN_PROGRESS -> "The resource owner must confirm the returned item."
        TransactionStatus.COMPLETED -> "This lifecycle is complete. No further action is required."
        TransactionStatus.REJECTED -> "This request was declined. Create a new request only if terms change."
        TransactionStatus.CANCELLED -> "This request was cancelled. No further action is required."
    }

    private fun CircularTransaction.responsibleRole(): String = when (status) {
        TransactionStatus.REQUESTED -> if (partnerId != null) "Partner" else "Organiser"
        TransactionStatus.APPROVED, TransactionStatus.RETURN_IN_PROGRESS -> "Organiser"
        TransactionStatus.IN_TRANSIT -> if (partnerId != null) "Partner" else "Participant"
        TransactionStatus.ACTIVE -> if (type in returnableTypes) "Current holder" else "ReEvent server"
        TransactionStatus.COMPLETED, TransactionStatus.REJECTED, TransactionStatus.CANCELLED -> "No one"
    }

    private fun TransactionStatus.displayLabel(): String = name.lowercase()
        .replace('_', ' ')
        .replaceFirstChar(Char::titlecase)

    private fun TransactionLifecycleCardAction.labelFor(transaction: CircularTransaction): String = when (this) {
        TransactionLifecycleCardAction.APPROVE -> if (transaction.partnerId != null) "Accept recovery task" else "Approve request"
        TransactionLifecycleCardAction.BEGIN_HANDOVER -> "Begin handover"
        TransactionLifecycleCardAction.CONFIRM_RECEIPT -> if (transaction.partnerId != null) "Confirm recovery receipt" else "Confirm receipt"
        TransactionLifecycleCardAction.BEGIN_RETURN -> "Begin return"
        TransactionLifecycleCardAction.CONFIRM_RETURN -> "Confirm returned"
        TransactionLifecycleCardAction.CANCEL -> if (transaction.partnerId != null) "Decline recovery task" else "Cancel request"
    }

    private val returnableTypes = setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR)
}
