package com.reevent.app.feature.impact

enum class ImpactBadge { FIRST_RECOVERY, CIRCULAR_STARTER, HIGH_RECOVERY }

data class ImpactDashboardState(
    val recoveryRate: Float?,
    val reusedCount: Int,
    val repairedCount: Int,
    val donatedCount: Int,
    val recycledCount: Int,
    val materialDivertedKg: Double?,
    val emissionsAvoidedKg: Double?,
    val valueRecoveredCents: Long?,
    val chartValues: List<Float>,
    val badge: ImpactBadge?,
    val unavailableEstimateReason: String?
)
