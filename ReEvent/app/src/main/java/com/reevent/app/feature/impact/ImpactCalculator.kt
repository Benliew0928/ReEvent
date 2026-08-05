package com.reevent.app.feature.impact

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType

object ImpactCalculator {
    fun summarize(
        resources: List<ResourceItem>,
        transactions: List<CircularTransaction>,
        records: List<ImpactRecord>
    ): ImpactDashboardState {
        val completed = transactions.filter { it.status == TransactionStatus.COMPLETED }
        val reused = completed.count { it.type == TransactionType.RESALE || it.type == TransactionType.RETURN }
        val repaired = completed.count { it.type == TransactionType.REPAIR }
        val donated = completed.count { it.type == TransactionType.DONATION }
        val recycled = completed.count { it.type == TransactionType.RECYCLE }
        val recovered = resources.count { it.status == ResourceStatus.RECOVERED || it.status == ResourceStatus.HANDED_OVER }
        val rate = resources.takeIf { it.isNotEmpty() }?.let { recovered.toFloat() / it.size }
        val validRecords = records.filter { it.materialDivertedKg >= 0 && it.emissionsAvoidedKg >= 0 && it.valueRecoveredCents >= 0 }
        val hasRecords = validRecords.isNotEmpty()
        val channels = listOf(reused, repaired, donated, recycled)
        val channelTotal = channels.sum()
        val badge = when {
            recovered == 0 -> null
            rate != null && rate >= 0.75f -> ImpactBadge.HIGH_RECOVERY
            recovered >= 3 -> ImpactBadge.CIRCULAR_STARTER
            else -> ImpactBadge.FIRST_RECOVERY
        }
        return ImpactDashboardState(
            recoveryRate = rate,
            reusedCount = reused,
            repairedCount = repaired,
            donatedCount = donated,
            recycledCount = recycled,
            materialDivertedKg = validRecords.takeIf { hasRecords }?.sumOf(ImpactRecord::materialDivertedKg),
            emissionsAvoidedKg = validRecords.takeIf { hasRecords }?.sumOf(ImpactRecord::emissionsAvoidedKg),
            valueRecoveredCents = validRecords.takeIf { hasRecords }?.sumOf(ImpactRecord::valueRecoveredCents),
            chartValues = if (channelTotal == 0) List(4) { 0f } else channels.map { it.toFloat() / channelTotal },
            badge = badge,
            unavailableEstimateReason = if (completed.isNotEmpty() && !hasRecords) "No verified impact estimates are available." else null
        )
    }
}
