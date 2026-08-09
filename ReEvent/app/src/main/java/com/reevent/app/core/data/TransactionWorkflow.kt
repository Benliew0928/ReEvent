package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType

/**
 * Display-only command availability. PostgreSQL repeats every actor, state, quantity, and
 * eligibility check; these helpers are never an authority boundary.
 */
object TransactionWorkflow {
    fun validateMarketplaceRequest(requesterId: String, resource: ResourceItem, quantity: Int): FailureReason? = when {
        requesterId == resource.ownerId -> FailureReason.CONFLICT
        resource.status != ResourceStatus.ACTIVE -> FailureReason.CONFLICT
        quantity <= 0 || quantity.toDouble() > resource.quantity -> FailureReason.VALIDATION
        else -> null
    }

    fun validatePartnerHandover(requesterId: String, resource: ResourceItem, programme: CircularProgramme): FailureReason? = when {
        resource.ownerId != requesterId -> FailureReason.CONFLICT
        resource.status != ResourceStatus.ACTIVE -> FailureReason.CONFLICT
        resource.quantity <= 0.0 -> FailureReason.VALIDATION
        !programme.active -> FailureReason.CONFLICT
        !programme.acceptsMaterial(resource.material) -> FailureReason.CONFLICT
        else -> null
    }

    fun canApprove(userId: String, transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.REQUESTED && decisionActor(transaction) == userId

    fun canCancel(userId: String, transaction: CircularTransaction): Boolean =
        transaction.status in setOf(TransactionStatus.REQUESTED, TransactionStatus.APPROVED) &&
            userId in setOfNotNull(transaction.requesterId, decisionActor(transaction))

    fun canBeginHandover(userId: String, transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.APPROVED && transaction.senderId == userId

    fun canConfirmReceipt(userId: String, transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.IN_TRANSIT && transaction.receiverId == userId

    fun canBeginReturn(userId: String, transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.ACTIVE &&
            transaction.type in setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR) &&
            transaction.receiverId == userId

    fun canConfirmReturn(userId: String, transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.RETURN_IN_PROGRESS && transaction.senderId == userId

    fun transactionTypeForProgramme(programme: CircularProgramme): TransactionType = when (programme.type) {
        ProgrammeType.REPAIR -> TransactionType.REPAIR
        ProgrammeType.RECYCLE -> TransactionType.RECYCLE
        ProgrammeType.BUY_BACK -> TransactionType.BUY_BACK
    }

    private fun decisionActor(transaction: CircularTransaction): String = transaction.partnerId ?: transaction.senderId

    private fun CircularProgramme.acceptsMaterial(material: String): Boolean =
        acceptedMaterials.isEmpty() || acceptedMaterials.any { it.equals(material, ignoreCase = true) }
}
