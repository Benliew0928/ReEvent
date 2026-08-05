package com.reevent.app.feature.matching

enum class CircularAction {
    REUSE,
    SHARE,
    RENT_OR_LEND,
    SELL_OR_DONATE,
    REPAIR,
    REFURBISH,
    TAKE_BACK,
    RECYCLE,
    DISPOSAL
}

data class RecommendationCandidate(
    val action: CircularAction,
    val score: Int,
    val compatibleProgrammeIds: List<String>,
    val explanation: String
)

data class RecommendationResult(
    val primary: RecommendationCandidate?,
    val alternatives: List<RecommendationCandidate>,
    val ineligibilityReason: String?
)
