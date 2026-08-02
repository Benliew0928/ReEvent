package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType

object TransactionWorkflow {
    fun validateMarketplaceRequest(
        requesterId: String,
        resource: ResourceItem,
        quantity: Int
    ): FailureReason? = when {
        requesterId == resource.ownerId -> FailureReason.CONFLICT
        resource.status != ResourceStatus.AVAILABLE -> FailureReason.CONFLICT
        quantity !in 1..resource.quantity -> FailureReason.VALIDATION
        else -> null
    }

    fun validateOwnerAction(
        ownerId: String,
        resource: ResourceItem,
        transaction: CircularTransaction
    ): FailureReason? = when {
        resource.ownerId != ownerId -> FailureReason.CONFLICT
        transaction.receiverId != ownerId && transaction.partnerId != ownerId -> FailureReason.CONFLICT
        transaction.senderId == ownerId -> FailureReason.CONFLICT
        transaction.resourceId != resource.id -> FailureReason.CONFLICT
        else -> null
    }

    fun validatePartnerHandover(
        requesterId: String,
        resource: ResourceItem,
        programme: CircularProgramme
    ): FailureReason? = when {
        resource.ownerId != requesterId -> FailureReason.CONFLICT
        resource.status != ResourceStatus.AVAILABLE -> FailureReason.CONFLICT
        resource.quantity < 1 -> FailureReason.VALIDATION
        !programme.active -> FailureReason.CONFLICT
        !programme.acceptsMaterial(resource.material) -> FailureReason.CONFLICT
        else -> null
    }

    fun canApprove(transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.PENDING

    fun canCancel(transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.PENDING || transaction.status == TransactionStatus.ACCEPTED

    fun canMoveInTransit(transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.ACCEPTED

    fun canComplete(transaction: CircularTransaction): Boolean =
        transaction.status == TransactionStatus.ACCEPTED || transaction.status == TransactionStatus.IN_TRANSIT

    fun statusAfterApproval(): TransactionStatus = TransactionStatus.ACCEPTED
    fun statusAfterCancellation(): TransactionStatus = TransactionStatus.CANCELLED
    fun statusAfterInTransit(): TransactionStatus = TransactionStatus.IN_TRANSIT
    fun statusAfterCompletion(): TransactionStatus = TransactionStatus.COMPLETED

    fun resourceStatusAfterApproval(): ResourceStatus = ResourceStatus.RESERVED

    fun resourceStatusAfterCancellation(current: ResourceStatus): ResourceStatus =
        if (current == ResourceStatus.RESERVED) ResourceStatus.AVAILABLE else current

    fun resourceStatusAfterCompletion(type: TransactionType): ResourceStatus = when (type) {
        TransactionType.RETURN -> ResourceStatus.RECOVERED
        TransactionType.RESALE,
        TransactionType.DONATION,
        TransactionType.REPAIR,
        TransactionType.RECYCLE,
        TransactionType.BUY_BACK -> ResourceStatus.HANDED_OVER
    }

    fun transactionTypeForProgramme(programme: CircularProgramme): TransactionType = when (programme.type) {
        ProgrammeType.REPAIR -> TransactionType.REPAIR
        ProgrammeType.RECYCLE -> TransactionType.RECYCLE
        ProgrammeType.BUY_BACK -> TransactionType.BUY_BACK
        ProgrammeType.REUSE,
        ProgrammeType.COLLECTION -> TransactionType.DONATION
    }

    private fun CircularProgramme.acceptsMaterial(material: String): Boolean =
        acceptedMaterials.isEmpty() || acceptedMaterials.any { it.equals(material, ignoreCase = true) }
}
