package com.reevent.app.feature.impact

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImpactCalculatorTest {
    @Test
    fun summary_counts_only_completed_supported_channels() {
        val summary = ImpactCalculator.summarize(
            resources = listOf(resource("available"), resource("recovered", ResourceStatus.RECOVERED)),
            transactions = listOf(
                transaction("repair", TransactionType.REPAIR, TransactionStatus.COMPLETED),
                transaction("donation", TransactionType.DONATION, TransactionStatus.PENDING),
                transaction("recycle", TransactionType.RECYCLE, TransactionStatus.COMPLETED)
            ),
            records = listOf(record())
        )

        assertEquals(0.5f, summary.recoveryRate)
        assertEquals(1, summary.repairedCount)
        assertEquals(1, summary.recycledCount)
        assertEquals(0, summary.donatedCount)
        assertEquals(2.5, summary.materialDivertedKg)
        assertEquals(4.0, summary.emissionsAvoidedKg)
        assertEquals(500L, summary.valueRecoveredCents)
    }

    @Test
    fun empty_inputs_have_no_rate_badge_or_estimate() {
        val summary = ImpactCalculator.summarize(emptyList(), emptyList(), emptyList())

        assertNull(summary.recoveryRate)
        assertNull(summary.badge)
        assertNull(summary.materialDivertedKg)
    }

    private fun resource(id: String, status: ResourceStatus = ResourceStatus.AVAILABLE) = ResourceItem(
        id, "event", "owner", id, "Signage", "Acrylic", ResourceCondition.GOOD,
        1, "item", status, 0, emptyList(), NOW, NOW
    )

    private fun transaction(id: String, type: TransactionType, status: TransactionStatus) = CircularTransaction(
        id, "event", "resource", "owner", "partner", "partner", type, status, 1, NOW, NOW
    )

    private fun record() = ImpactRecord("impact", "event", "resource", "repair", 2.5, 4.0, 500, NOW, NOW)

    private companion object { const val NOW = 1L }
}
