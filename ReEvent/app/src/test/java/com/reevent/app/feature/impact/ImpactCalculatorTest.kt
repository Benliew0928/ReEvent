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
                transaction("donation", TransactionType.DONATE, TransactionStatus.REQUESTED),
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
        assertEquals(500L, summary.recoinsTransferred)
        assertEquals(25L, summary.recoinsRewarded)
    }

    @Test
    fun empty_inputs_have_no_rate_badge_or_estimate() {
        val summary = ImpactCalculator.summarize(emptyList(), emptyList(), emptyList())

        assertNull(summary.recoveryRate)
        assertNull(summary.badge)
        assertNull(summary.materialDivertedKg)
    }

    @Test
    fun summary_marks_completed_outcomes_without_a_documented_estimate() {
        val summary = ImpactCalculator.summarize(
            resources = listOf(resource("recovered", ResourceStatus.RECOVERED)),
            transactions = listOf(
                transaction("estimated", TransactionType.RECYCLE, TransactionStatus.COMPLETED),
                transaction("unavailable", TransactionType.REPAIR, TransactionStatus.COMPLETED)
            ),
            records = listOf(record().copy(transactionId = "estimated"))
        )

        assertEquals("Some completed outcomes have no documented mass and factor.", summary.unavailableEstimateReason)
    }

    @Test
    fun summary_excludes_non_finite_estimates() {
        val summary = ImpactCalculator.summarize(
            resources = listOf(resource("recovered", ResourceStatus.RECOVERED)),
            transactions = listOf(transaction("recycle", TransactionType.RECYCLE, TransactionStatus.COMPLETED)),
            records = listOf(record().copy(materialDivertedKg = Double.NaN))
        )

        assertNull(summary.materialDivertedKg)
        assertEquals("No documented mass and factor are available for these completed outcomes.", summary.unavailableEstimateReason)
    }

    @Test
    fun server_record_without_a_factor_keeps_recoins_but_does_not_invent_an_estimate() {
        val summary = ImpactCalculator.summarize(
            resources = listOf(resource("recovered", ResourceStatus.RECOVERED)),
            transactions = listOf(transaction("repair", TransactionType.REPAIR, TransactionStatus.COMPLETED)),
            records = listOf(record().copy(materialDivertedKg = null, emissionsAvoidedKg = null))
        )

        assertNull(summary.materialDivertedKg)
        assertNull(summary.emissionsAvoidedKg)
        assertEquals(500L, summary.recoinsTransferred)
        assertEquals("No documented mass and factor are available for these completed outcomes.", summary.unavailableEstimateReason)
    }

    @Test
    fun ignores_records_without_a_completed_transaction_and_keeps_the_latest_completed_record() {
        val summary = ImpactCalculator.summarize(
            resources = listOf(resource("recovered", ResourceStatus.RECOVERED)),
            transactions = listOf(
                transaction("older", TransactionType.REPAIR, TransactionStatus.COMPLETED),
                transaction("latest", TransactionType.RECYCLE, TransactionStatus.COMPLETED),
                transaction("pending", TransactionType.DONATE, TransactionStatus.REQUESTED)
            ),
            records = listOf(
                record().copy(id = "older-record", transactionId = "older", calculatedAt = 10L),
                record().copy(id = "latest-record", transactionId = "latest", calculatedAt = 20L),
                record().copy(id = "pending-record", transactionId = "pending", calculatedAt = 30L, recoinsTransferred = 99L)
            )
        )

        assertEquals("latest-record", summary.latestRecord?.id)
        assertEquals(1000L, summary.recoinsTransferred)
    }

    private fun resource(id: String, status: ResourceStatus = ResourceStatus.ACTIVE) = ResourceItem(
        id, "event", "owner", id, "Signage", "Acrylic", ResourceCondition.GOOD,
        1.0, "ITEM", status, 0, emptyList(), NOW, NOW
    )

    private fun transaction(id: String, type: TransactionType, status: TransactionStatus) = CircularTransaction(
        id, "event", "resource", "owner", "partner", "partner", type, status, 1.0, NOW, NOW
    )

    private fun record() = ImpactRecord(
        id = "impact",
        eventId = "event",
        resourceId = "resource",
        transactionId = "repair",
        transactionType = TransactionType.REPAIR,
        completedQuantity = 1.0,
        unit = "KG",
        materialDivertedKg = 2.5,
        emissionsAvoidedKg = 4.0,
        recoinsTransferred = 500,
        recoinsRewarded = 25,
        calculatedAt = NOW
    )

    private companion object { const val NOW = 1L }
}
