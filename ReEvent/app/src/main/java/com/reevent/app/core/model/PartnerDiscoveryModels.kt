package com.reevent.app.core.model

enum class PartnerOriginSource { DEVICE, RESOURCE, EVENT, NONE }

data class PartnerMapFilters(
    val materialFamily: MaterialFamily? = null,
    val programmeTypes: Set<ProgrammeType> = emptySet(),
    val maximumDistanceKm: Double? = null,
    val pickupOnly: Boolean = false,
)

data class PartnerDiscoveryRequest(
    val resourceId: String? = null,
    val deviceLocation: GeoLocation? = null,
    val filters: PartnerMapFilters = PartnerMapFilters(),
    val limit: Int = 100,
    val offset: Int = 0,
)

data class PartnerCandidate(
    val programme: CircularProgramme,
    val distanceKm: Double? = null,
    val score: Int? = null,
    val reasons: List<String> = emptyList(),
)

data class PartnerDiscoveryResult(
    val origin: GeoLocation? = null,
    val originSource: PartnerOriginSource = PartnerOriginSource.NONE,
    val candidates: List<PartnerCandidate> = emptyList(),
    val exclusionCounts: Map<String, Int> = emptyMap(),
    val nextOffset: Int? = null,
    val serverAuthoritative: Boolean = true,
)

data class PlaceSuggestion(
    val id: String,
    val label: String,
    val location: GeoLocation,
)

data class LegacyProgrammeDraft(
    val id: String,
    val partnerId: String,
    val name: String,
    val type: ProgrammeType,
    val acceptedMaterials: List<String>,
    val location: String,
)
