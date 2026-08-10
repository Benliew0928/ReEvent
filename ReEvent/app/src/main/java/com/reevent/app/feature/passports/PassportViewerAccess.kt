package com.reevent.app.feature.passports

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole

data class PassportViewerAccess(
    val label: String,
    val explanation: String,
    val canFindPartnerMatches: Boolean
)

/** Display guidance only. Server RLS and lifecycle RPCs remain the permission boundary. */
object PassportViewerAccessPolicy {
    fun forViewer(user: User, resource: ResourceItem, transactions: List<CircularTransaction>): PassportViewerAccess {
        val resourceTransactions = transactions.filter { it.resourceId == resource.id }
        val activeHolding = resourceTransactions.any {
            it.receiverId == user.id && it.status in setOf(TransactionStatus.ACTIVE, TransactionStatus.RETURN_IN_PROGRESS)
        }
        val assignedPartner = resourceTransactions.any {
            it.partnerId == user.id && it.status !in setOf(TransactionStatus.COMPLETED, TransactionStatus.CANCELLED, TransactionStatus.REJECTED)
        }
        val requester = resourceTransactions.any {
            it.requesterId == user.id && it.status !in setOf(TransactionStatus.COMPLETED, TransactionStatus.CANCELLED, TransactionStatus.REJECTED)
        }

        return when {
            resource.ownerId == user.id -> PassportViewerAccess(
                label = if (user.role == UserRole.ORGANIZER) "Organiser and current owner" else "Current owner",
                explanation = if (user.role == UserRole.ORGANIZER) {
                    "You can manage this resource. Partner matching is available while it is active."
                } else {
                    "Your account is the current owner. Available lifecycle actions are still confirmed by the server."
                },
                canFindPartnerMatches = user.role == UserRole.ORGANIZER && resource.status == ResourceStatus.ACTIVE
            )
            activeHolding -> PassportViewerAccess(
                label = "Participant and current holder",
                explanation = "You can verify this passport. Return actions are available only when the live transaction allows them.",
                canFindPartnerMatches = false
            )
            assignedPartner -> PassportViewerAccess(
                label = "Assigned recovery partner",
                explanation = "You can inspect this passport and use the Partner Workbench for authorised lifecycle actions.",
                canFindPartnerMatches = false
            )
            requester -> PassportViewerAccess(
                label = "Request participant",
                explanation = "You can view the authorised passport record while your request is in progress.",
                canFindPartnerMatches = false
            )
            else -> PassportViewerAccess(
                label = "Authorised marketplace viewer",
                explanation = "This read-only passport shows public-safe resource and lifecycle information only.",
                canFindPartnerMatches = false
            )
        }
    }
}
