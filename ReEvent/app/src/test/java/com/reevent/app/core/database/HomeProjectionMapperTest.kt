package com.reevent.app.core.database

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeProjectionMapperTest {
    @Test
    fun `resource reuse count survives room projection`() {
        val resource = ResourceItem(
            id = "resource",
            eventId = "event",
            ownerId = "owner",
            title = "Chair",
            category = "Furniture",
            materialFamily = com.reevent.app.core.model.MaterialFamily.WOOD,
            condition = ResourceCondition.GOOD,
            quantity = 4.0,
            unit = "units",
            status = ResourceStatus.ACTIVE,
            valueCents = 0,
            imageUrls = emptyList(),
            createdAt = 1,
            updatedAt = 2,
            syncState = SyncState.SYNCED,
            reuseCount = 7,
        )

        assertEquals(7, resource.toEntity("account").toDomain().reuseCount)
    }

    @Test
    fun `programme and lifecycle timestamps survive room projection`() {
        val transaction = CircularTransaction(
            id = "transaction",
            eventId = "event",
            resourceId = "resource",
            senderId = "sender",
            receiverId = "receiver",
            partnerId = "partner",
            type = TransactionType.REPAIR,
            status = TransactionStatus.COMPLETED,
            quantity = 4.0,
            createdAt = 1,
            updatedAt = 8,
            syncState = SyncState.SYNCED,
            requesterId = "sender",
            programmeId = "programme",
            approvedAt = 2,
            inTransitAt = 3,
            activeAt = 4,
            returnStartedAt = 5,
            completedAt = 6,
        )

        val projected = transaction.toEntity("account").toDomain()

        assertEquals("programme", projected.programmeId)
        assertEquals(2L, projected.approvedAt)
        assertEquals(3L, projected.inTransitAt)
        assertEquals(4L, projected.activeAt)
        assertEquals(5L, projected.returnStartedAt)
        assertEquals(6L, projected.completedAt)
    }
}
