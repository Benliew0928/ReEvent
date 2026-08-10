package com.reevent.app.feature.matching

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.ResourceStatus

object CircularRecommendationEngine {
    fun recommend(
        resource: ResourceItem,
        programmes: List<CircularProgramme>,
        eventLocation: String = ""
    ): RecommendationResult {
        if (resource.status != ResourceStatus.ACTIVE) {
            return RecommendationResult(
                primary = null,
                alternatives = emptyList(),
                ineligibilityReason = "This resource is not available for a new recovery route."
            )
        }
        if (resource.quantity <= 0.0) {
            return RecommendationResult(
                primary = null,
                alternatives = emptyList(),
                ineligibilityReason = "This resource has no available quantity for a new recovery route."
            )
        }

        val rankedProgrammes = rankProgrammes(resource, programmes, eventLocation)
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
                    score = score(action, compatible.first(), resource, eventLocation),
                    compatibleProgrammeIds = compatible.map(CircularProgramme::id),
                    explanation = explanation(action, compatible.first(), resource, eventLocation)
                )
            }
        }

        return RecommendationResult(
            primary = candidates.firstOrNull(),
            alternatives = candidates.drop(1),
            ineligibilityReason = if (candidates.isEmpty()) {
                noMatchReason(resource, programmes)
            } else {
                null
            }
        )
    }

    fun rankProgrammes(
        resource: ResourceItem,
        programmes: List<CircularProgramme>,
        eventLocation: String = ""
    ): List<CircularProgramme> = programmes
        .asSequence()
        .filter(CircularProgramme::active)
        .filter { programme -> materialCompatibility(resource.material, programme) > 0 }
        .sortedWith(
            compareByDescending<CircularProgramme> { materialCompatibility(resource.material, it) }
                .thenByDescending { locationCompatibility(eventLocation, it.location) }
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

        ResourceCondition.END_OF_LIFE -> listOf(CircularAction.RECYCLE, CircularAction.DISPOSAL)
    }

    private fun programmeTypesFor(action: CircularAction): Set<ProgrammeType> = when (action) {
        CircularAction.REUSE,
        CircularAction.SHARE,
        CircularAction.RENT_OR_LEND,
        CircularAction.SELL_OR_DONATE -> setOf(ProgrammeType.REPAIR)

        CircularAction.REPAIR,
        CircularAction.REFURBISH -> setOf(ProgrammeType.REPAIR)
        CircularAction.TAKE_BACK -> setOf(ProgrammeType.BUY_BACK)
        CircularAction.RECYCLE -> setOf(ProgrammeType.RECYCLE, ProgrammeType.RECYCLE)
        CircularAction.DISPOSAL -> emptySet()
    }

    private fun materialCompatibility(material: String, programme: CircularProgramme): Int = when {
        programme.acceptedMaterials.isEmpty() -> 1
        material.isBlank() -> 0
        programme.acceptedMaterials.any { it.equals(material, ignoreCase = true) } -> 2
        else -> 0
    }

    private fun score(
        action: CircularAction,
        programme: CircularProgramme,
        resource: ResourceItem,
        eventLocation: String
    ): Int {
        val actionPriority = actionOrder(resource.condition).indexOf(action)
        val materialScore = materialCompatibility(resource.material, programme) * 10
        val locationScore = locationCompatibility(eventLocation, programme.location) * 4
        return 100 - (actionPriority * 5) + materialScore + locationScore
    }

    private fun explanation(
        action: CircularAction,
        programme: CircularProgramme,
        resource: ResourceItem,
        eventLocation: String
    ): String {
        val category = resource.category.ifBlank { "uncategorised resources" }
        val material = resource.material.ifBlank { "unspecified material" }
        val locationNote = when (locationCompatibility(eventLocation, programme.location)) {
            2 -> " Its service area matches the event location."
            1 -> " Its service area is related to the event location."
            else -> if (programme.location.isBlank()) " Its service area needs confirmation." else " Its service area is ${programme.location}."
        }
        return "${action.displayName()} is supported by ${programme.name} for ${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} of $category ($material, ${resource.condition.name.lowercase().replace('_', ' ')}).$locationNote"
    }

    private fun noMatchReason(resource: ResourceItem, programmes: List<CircularProgramme>): String = when {
        programmes.none(CircularProgramme::active) -> "No active partner programmes are available yet."
        resource.material.isBlank() && programmes.none { it.acceptedMaterials.isEmpty() } ->
            "This resource needs a material before a compatible partner can be selected."
        programmes.filter(CircularProgramme::active).none { materialCompatibility(resource.material, it) > 0 } ->
            "No active partner programme accepts ${resource.material.ifBlank { "this resource material" }}."
        else -> "No active partner programme supports this recovery route."
    }

    private fun locationCompatibility(eventLocation: String, programmeLocation: String): Int {
        val event = eventLocation.trim()
        val programme = programmeLocation.trim()
        if (event.isBlank() || programme.isBlank()) return 0
        return when {
            event.equals(programme, ignoreCase = true) -> 2
            event.contains(programme, ignoreCase = true) || programme.contains(event, ignoreCase = true) -> 1
            else -> 0
        }
    }

    private fun CircularAction.displayName(): String = name
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar(Char::titlecase)
}
