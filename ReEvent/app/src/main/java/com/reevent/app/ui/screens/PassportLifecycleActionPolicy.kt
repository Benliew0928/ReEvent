package com.reevent.app.ui.screens

import com.reevent.app.core.data.TransactionWorkflow
import com.reevent.app.core.data.blocksResourceArchive
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole

/**
 * Keeps passport controls aligned with the transaction that the server can actually accept.
 *
 * A resource passport is a read model: it must not present a generic "check out" or "return"
 * action when no matching server transaction exists. The server remains the authority; this
 * policy only avoids presenting actions that are guaranteed to fail.
 */
object PassportLifecycleActionPolicy {
    fun availableActions(
        user: User,
        resource: ResourceItem,
        viewerTransactions: List<CircularTransaction>,
    ): List<ResourceLifecycleAction> = buildList {
        val resourceTransactions = viewerTransactions.forResource(resource.id)
        if (transactionFor(ResourceLifecycleAction.CHECK_OUT, user, resourceTransactions) != null) {
            add(ResourceLifecycleAction.CHECK_OUT)
        }
        if (transactionFor(ResourceLifecycleAction.RETURN, user, resourceTransactions) != null) {
            add(ResourceLifecycleAction.RETURN)
        }
        if (canMarkDamaged(user, resource, viewerTransactions)) {
            add(ResourceLifecycleAction.MARK_DAMAGED)
        }
    }

    fun transactionFor(
        action: ResourceLifecycleAction,
        user: User,
        viewerTransactions: List<CircularTransaction>,
    ): CircularTransaction? = when (action) {
        ResourceLifecycleAction.CHECK_OUT ->
            viewerTransactions.firstOrNull { TransactionWorkflow.canBeginHandover(user.id, it) }

        ResourceLifecycleAction.RETURN ->
            viewerTransactions.firstOrNull { TransactionWorkflow.canBeginReturn(user.id, it) }
                ?: viewerTransactions.firstOrNull { TransactionWorkflow.canConfirmReturn(user.id, it) }

        ResourceLifecycleAction.MARK_DAMAGED,
        ResourceLifecycleAction.REQUEST_REPAIR,
        ResourceLifecycleAction.TRANSFER,
        -> null
    }

    fun canMarkDamaged(
        user: User,
        resource: ResourceItem,
        viewerTransactions: List<CircularTransaction>,
    ): Boolean =
        user.role == UserRole.ORGANIZER &&
            resource.ownerId == user.id &&
            resource.status == ResourceStatus.ACTIVE &&
            resource.condition !in setOf(ResourceCondition.NEEDS_REPAIR, ResourceCondition.END_OF_LIFE) &&
            viewerTransactions.none { it.blocksResourceArchive(resource.id) }

    private fun List<CircularTransaction>.forResource(resourceId: String): List<CircularTransaction> =
        filter { it.resourceId == resourceId }
}
