package com.reevent.app.feature.impact

import com.reevent.app.core.model.ImpactRecord

enum class ImpactBadge { FIRST_RECOVERY, CIRCULAR_STARTER, HIGH_RECOVERY }

data class ImpactDashboardState(
    val recoveryRate: Float?,
    val reusedCount: Int,
    val repairedCount: Int,
    val donatedCount: Int,
    val recycledCount: Int,
    val materialDivertedKg: Double?,
    val emissionsAvoidedKg: Double?,
    val recoinsTransferred: Long?,
    val recoinsRewarded: Long?,
    val chartValues: List<Float>,
    val badge: ImpactBadge?,
    val unavailableEstimateReason: String?,
    /** Most recently calculated server record among completed transactions in the viewed event. */
    val latestRecord: ImpactRecord?
)
