package com.reevent.app.feature.matching

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryRequest
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.PartnerOriginSource
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PartnerDiscoveryEngine {
    fun discover(
        programmes: List<CircularProgramme>,
        resource: ResourceItem?,
        eventLocation: GeoLocation?,
        requesterId: String,
        request: PartnerDiscoveryRequest,
    ): PartnerDiscoveryResult {
        val origin = request.deviceLocation ?: resource?.geoLocation ?: eventLocation
        val originSource = when {
            request.deviceLocation != null -> PartnerOriginSource.DEVICE
            resource?.geoLocation != null -> PartnerOriginSource.RESOURCE
            eventLocation != null -> PartnerOriginSource.EVENT
            else -> PartnerOriginSource.NONE
        }
        val exclusions = linkedMapOf<String, Int>()
        val eligible = programmes.mapNotNull { programme ->
            val distance = distanceKm(origin, programme.geoLocation)
            val exclusion = exclusionReason(programme, resource, requesterId, request, distance)
            if (exclusion != null) {
                exclusions[exclusion] = exclusions.getOrDefault(exclusion, 0) + 1
                null
            } else {
                PartnerCandidate(
                    programme = programme,
                    distanceKm = distance,
                    score = resource?.let { score(programme, it, distance, request.filters.pickupOnly) },
                    reasons = resource?.let { reasons(programme, it, distance) }.orEmpty(),
                )
            }
        }.sortedWith(
            compareByDescending<PartnerCandidate> { it.score ?: 0 }
                .thenBy { it.distanceKm == null }
                .thenBy { it.distanceKm ?: Double.MAX_VALUE }
                .thenBy { it.programme.name.lowercase() }
                .thenBy { it.programme.id },
        )
        val page = eligible.drop(request.offset).take(request.limit.coerceIn(1, 100))
        val nextOffset = (request.offset + page.size).takeIf { it < eligible.size }
        return PartnerDiscoveryResult(
            origin = origin,
            originSource = originSource,
            candidates = page,
            exclusionCounts = exclusions,
            nextOffset = nextOffset,
            serverAuthoritative = false,
        )
    }

    fun distanceKm(first: GeoLocation?, second: GeoLocation?): Double? {
        if (first == null || second == null) return null
        val firstLatitude = Math.toRadians(first.latitude)
        val secondLatitude = Math.toRadians(second.latitude)
        val latitudeDelta = Math.toRadians(second.latitude - first.latitude)
        val longitudeDelta = Math.toRadians(second.longitude - first.longitude)
        val a = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
            cos(firstLatitude) * cos(secondLatitude) *
            sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
        val bounded = a.coerceIn(0.0, 1.0)
        return 6_371.0088 * 2 * atan2(sqrt(bounded), sqrt(1 - bounded))
    }

    private fun exclusionReason(
        programme: CircularProgramme,
        resource: ResourceItem?,
        requesterId: String,
        request: PartnerDiscoveryRequest,
        distance: Double?,
    ): String? = when {
        !programme.active || programme.geoLocation == null -> "PROGRAMME_UNAVAILABLE"
        request.filters.programmeTypes.isNotEmpty() && programme.type !in request.filters.programmeTypes -> "TYPE_FILTERED"
        request.filters.pickupOnly && !programme.pickupAvailable -> "PICKUP_UNAVAILABLE"
        request.filters.maximumDistanceKm != null && (distance == null || distance > request.filters.maximumDistanceKm) -> "OUTSIDE_DISTANCE"
        request.filters.materialFamily != null && programme.acceptedMaterialFamilies.isNotEmpty() &&
            request.filters.materialFamily !in programme.acceptedMaterialFamilies -> "MATERIAL_NOT_ACCEPTED"
        resource == null -> null
        resource.ownerId != requesterId || resource.status != ResourceStatus.ACTIVE -> "RESOURCE_NOT_OWNED_OR_ACTIVE"
        programme.partnerId == requesterId -> "SELF_DEALING_FORBIDDEN"
        programme.acceptedMaterialFamilies.isNotEmpty() && resource.materialFamily !in programme.acceptedMaterialFamilies -> "MATERIAL_NOT_ACCEPTED"
        programme.acceptedCategories.isNotEmpty() && programme.acceptedCategories.none { it.equals(resource.category, ignoreCase = true) } -> "CATEGORY_NOT_ACCEPTED"
        resource.condition !in programme.acceptedConditions -> "CONDITION_NOT_ACCEPTED"
        programme.unit != null && !programme.unit.equals(resource.unit, ignoreCase = true) -> "UNIT_NOT_ACCEPTED"
        programme.minimumQuantity != null && resource.quantity < programme.minimumQuantity -> "BELOW_MINIMUM_QUANTITY"
        programme.maximumQuantity != null && resource.quantity > programme.maximumQuantity -> "ABOVE_MAXIMUM_QUANTITY"
        programme.remainingCapacity != null && resource.quantity > programme.remainingCapacity -> "CAPACITY_UNAVAILABLE"
        else -> null
    }

    private fun score(programme: CircularProgramme, resource: ResourceItem, distance: Double?, pickupRequested: Boolean): Int {
        val material = if (programme.acceptedMaterialFamilies.isEmpty()) 15 else 30
        val category = if (programme.acceptedCategories.isEmpty()) 10 else 20
        val distancePoints = when {
            distance == null || distance > 50 -> 0
            distance <= 5 -> 25
            distance <= 15 -> 18
            else -> 8
        }
        val capacity = when {
            programme.remainingCapacity == null || programme.remainingCapacity >= resource.quantity * 2 -> 15
            else -> 8
        }
        return material + category + distancePoints + capacity + if (pickupRequested && programme.pickupAvailable) 10 else 0
    }

    private fun reasons(programme: CircularProgramme, resource: ResourceItem, distance: Double?): List<String> = buildList {
        add(if (programme.acceptedMaterialFamilies.isEmpty()) "Accepts all materials" else "Accepts ${resource.materialLabel}")
        add(if (programme.acceptedCategories.isEmpty()) "Accepts all categories" else "Accepts ${resource.category}")
        add(distance?.let { "${"%.1f".format(it)} km away" } ?: "Distance unavailable")
        add(if (programme.pickupAvailable) "Pickup available" else "Drop-off required")
    }
}
