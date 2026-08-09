package com.reevent.app.feature.matching

import com.reevent.app.core.model.ResourceCondition

data class PrototypeAssessmentResult(
    val suggestedAction: CircularAction,
    val confidenceLabel: String,
    val disclosure: String
)

object PrototypeAssessment {
    fun assess(
        category: String,
        material: String,
        condition: ResourceCondition
    ): PrototypeAssessmentResult? {
        if (category.isBlank() || material.isBlank()) return null
        val action = when (condition) {
            ResourceCondition.NEW,
            ResourceCondition.GOOD,
            ResourceCondition.FAIR -> CircularAction.REUSE
            ResourceCondition.NEEDS_REPAIR -> CircularAction.REPAIR
            ResourceCondition.RECYCLE_ONLY -> CircularAction.RECYCLE
        }
        return PrototypeAssessmentResult(
            suggestedAction = action,
            confidenceLabel = "Rule-based prototype",
            disclosure = "Prototype estimate from confirmed resource details"
        )
    }
}
