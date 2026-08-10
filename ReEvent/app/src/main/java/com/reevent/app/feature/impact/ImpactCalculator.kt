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
        val reused = completed.count { it.type == TransactionType.BORROW || it.type == TransactionType.RENT }
        val repaired = completed.count { it.type == TransactionType.REPAIR }
        val donated = completed.count { it.type == TransactionType.DONATE }
        val recycled = completed.count { it.type == TransactionType.RECYCLE }
        val recovered = resources.count { it.status == ResourceStatus.RECOVERED }
        val rate = resources.takeIf { it.isNotEmpty() }?.let { recovered.toFloat() / it.size }
        val completedTransactionIds = completed.map(CircularTransaction::id).toSet()
        val validRecords = records.filter {
            it.transactionId in completedTransactionIds &&
            (it.materialDivertedKg == null || it.materialDivertedKg.isFinite() && it.materialDivertedKg >= 0) &&
                (it.emissionsAvoidedKg == null || it.emissionsAvoidedKg.isFinite() && it.emissionsAvoidedKg >= 0) &&
                it.recoinsTransferred >= 0 && it.recoinsRewarded >= 0
        }
        val hasRecords = validRecords.isNotEmpty()
        val estimatedTransactionIds = validRecords
            .filter { it.materialDivertedKg != null || it.emissionsAvoidedKg != null }
            .map(ImpactRecord::transactionId)
            .toSet()
        val hasCompletedOutcomeWithoutEstimate = completed.any { it.id !in estimatedTransactionIds }
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
            materialDivertedKg = validRecords.mapNotNull(ImpactRecord::materialDivertedKg).takeIf { it.isNotEmpty() }?.sum(),
            emissionsAvoidedKg = validRecords.mapNotNull(ImpactRecord::emissionsAvoidedKg).takeIf { it.isNotEmpty() }?.sum(),
            recoinsTransferred = validRecords.takeIf { hasRecords }?.sumOf(ImpactRecord::recoinsTransferred),
            recoinsRewarded = validRecords.takeIf { hasRecords }?.sumOf(ImpactRecord::recoinsRewarded),
            chartValues = if (channelTotal == 0) List(4) { 0f } else channels.map { it.toFloat() / channelTotal },
            badge = badge,
            unavailableEstimateReason = when {
                completed.isEmpty() -> null
                estimatedTransactionIds.isEmpty() -> "No documented mass and factor are available for these completed outcomes."
                hasCompletedOutcomeWithoutEstimate -> "Some completed outcomes have no documented mass and factor."
                else -> null
            },
            latestRecord = validRecords.maxByOrNull(ImpactRecord::calculatedAt)
        )
    }
}
