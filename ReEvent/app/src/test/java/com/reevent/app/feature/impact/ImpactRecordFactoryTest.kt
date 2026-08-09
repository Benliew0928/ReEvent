package com.reevent.app.feature.impact

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImpactRecordFactoryTest {
    @Test
    fun completed_transaction_uses_its_uuid_for_one_repeatable_record() {
        val completed = transaction("11111111-1111-1111-1111-111111111111")

        val record = ImpactRecordFactory.create(completed, resource(), policy, now = 20L)

        assertEquals(completed.id, record?.id)
        assertEquals(completed.id, record?.transactionId)
        assertEquals(2.0, record?.materialDivertedKg)
        assertEquals(3.0, record?.emissionsAvoidedKg)
        assertEquals(1_000L, record?.valueRecoveredCents)
    }

    @Test
    fun missing_mass_or_factor_returns_unavailable_instead_of_a_false_estimate() {
        assertNull(ImpactRecordFactory.create(transaction(), resource(unit = "item"), policy, now = 20L))
        assertNull(ImpactRecordFactory.create(transaction(), resource(material = "Wood"), policy, now = 20L))
    }

    @Test
    fun pending_or_over_quantity_transaction_never_creates_an_estimate() {
        assertNull(ImpactRecordFactory.create(transaction(status = TransactionStatus.PENDING), resource(), policy, now = 20L))
        assertNull(ImpactRecordFactory.create(transaction(quantity = 3), resource(), policy, now = 20L))
    }

    private fun resource(
        material: String = "Acrylic",
        unit: String = "kg"
    ) = ResourceItem(
        id = "resource-id",
        eventId = "event-id",
        ownerId = "owner-id",
        title = "Test material",
        category = "Signage",
        material = material,
        condition = ResourceCondition.GOOD,
        quantity = 2,
        unit = unit,
        status = ResourceStatus.HANDED_OVER,
        valueCents = 1_000,
        imageUrls = emptyList(),
        createdAt = 1L,
        updatedAt = 1L
    )

    private fun transaction(
        id: String = "transaction-id",
        status: TransactionStatus = TransactionStatus.COMPLETED,
        quantity: Int = 2
    ) = CircularTransaction(
        id = id,
        eventId = "event-id",
        resourceId = "resource-id",
        senderId = "owner-id",
        receiverId = "partner-id",
        partnerId = "partner-id",
        type = TransactionType.RECYCLE,
        status = status,
        quantity = quantity,
        createdAt = 1L,
        updatedAt = 1L
    )

    private val policy = ImpactEstimatePolicy(
        mapOf(
            ImpactEstimateKey("acrylic", TransactionType.RECYCLE) to ImpactEstimateFactor(
                kgPerUnit = 1.0,
                co2eKgPerMaterialKg = 1.5,
                source = "test",
                versionOrAccessDate = "test"
            )
        )
    )
}
