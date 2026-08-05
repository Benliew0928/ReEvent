package com.reevent.app.feature.matching

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus

object CircularRecommendationEngine {
    fun recommend(resource: ResourceItem, programmes: List<CircularProgramme>): RecommendationResult {
        if (resource.status != ResourceStatus.AVAILABLE) {
            return RecommendationResult(
                primary = null,
                alternatives = emptyList(),
                ineligibilityReason = "This resource is not available for a new recovery route."
            )
        }

        val rankedProgrammes = rankProgrammes(resource, programmes)
        val usedProgrammeTypes = mutableSetOf<ProgrammeType>()
        val candidates = actionOrder(resource.condition).mapNotNull { action ->
            val programmeTypes = programmeTypesFor(action)
            if (programmeTypes.isEmpty() || programmeTypes.any { it in usedProgrammeTypes }) {
                return@mapNotNull null
            }
            val compatible = rankedProgrammes.filter { it.type in programmeTypes }
            if (compatible.isEmpty()) {
                null
            } else {
                usedProgrammeTypes += programmeTypes
                RecommendationCandidate(
                    action = action,
                    score = score(action, compatible.first(), resource),
                    compatibleProgrammeIds = compatible.map(CircularProgramme::id),
                    explanation = explanation(action, compatible.first(), resource)
                )
            }
        }

        return RecommendationResult(
            primary = candidates.firstOrNull(),
            alternatives = candidates.drop(1),
            ineligibilityReason = if (candidates.isEmpty()) {
                "No active partner programme supports this recovery route."
            } else {
                null
            }
        )
    }

    fun rankProgrammes(resource: ResourceItem, programmes: List<CircularProgramme>): List<CircularProgramme> = programmes
        .asSequence()
        .filter(CircularProgramme::active)
        .filter { programme -> materialCompatibility(resource.material, programme) > 0 }
        .sortedWith(
            compareByDescending<CircularProgramme> { materialCompatibility(resource.material, it) }
                .thenBy { it.name.lowercase() }
                .thenBy(CircularProgramme::id)
        )
        .toList()

    private fun actionOrder(condition: ResourceCondition): List<CircularAction> = when (condition) {
        ResourceCondition.NEW,
        ResourceCondition.GOOD -> listOf(
            CircularAction.REUSE,
            CircularAction.SHARE,
            CircularAction.RENT_OR_LEND,
            CircularAction.SELL_OR_DONATE,
            CircularAction.REPAIR,
            CircularAction.REFURBISH,
            CircularAction.TAKE_BACK,
            CircularAction.RECYCLE,
            CircularAction.DISPOSAL
        )

        ResourceCondition.FAIR -> listOf(
            CircularAction.REUSE,
            CircularAction.REPAIR,
            CircularAction.REFURBISH,
            CircularAction.TAKE_BACK,
            CircularAction.RECYCLE,
            CircularAction.DISPOSAL
        )

        ResourceCondition.NEEDS_REPAIR -> listOf(
            CircularAction.REPAIR,
            CircularAction.REFURBISH,
            CircularAction.TAKE_BACK,
            CircularAction.RECYCLE,
            CircularAction.DISPOSAL
        )

        ResourceCondition.RECYCLE_ONLY -> listOf(CircularAction.RECYCLE, CircularAction.DISPOSAL)
    }

    private fun programmeTypesFor(action: CircularAction): Set<ProgrammeType> = when (action) {
        CircularAction.REUSE,
        CircularAction.SHARE,
        CircularAction.RENT_OR_LEND,
        CircularAction.SELL_OR_DONATE -> setOf(ProgrammeType.REUSE)

        CircularAction.REPAIR,
        CircularAction.REFURBISH -> setOf(ProgrammeType.REPAIR)
        CircularAction.TAKE_BACK -> setOf(ProgrammeType.BUY_BACK)
        CircularAction.RECYCLE -> setOf(ProgrammeType.RECYCLE, ProgrammeType.COLLECTION)
        CircularAction.DISPOSAL -> emptySet()
    }

    private fun materialCompatibility(material: String, programme: CircularProgramme): Int = when {
        programme.acceptedMaterials.isEmpty() -> 1
        material.isBlank() -> 0
        programme.acceptedMaterials.any { it.equals(material, ignoreCase = true) } -> 2
        else -> 0
    }

    private fun score(action: CircularAction, programme: CircularProgramme, resource: ResourceItem): Int {
        val actionPriority = actionOrder(resource.condition).indexOf(action)
        val materialScore = materialCompatibility(resource.material, programme) * 10
        return 100 - (actionPriority * 5) + materialScore
    }

    private fun explanation(
        action: CircularAction,
        programme: CircularProgramme,
        resource: ResourceItem
    ): String = "${action.displayName()} is supported by ${programme.name} for ${resource.material.ifBlank { "generic" }} materials."

    private fun CircularAction.displayName(): String = name
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar(Char::titlecase)
}
