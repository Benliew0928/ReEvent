package com.reevent.app.core.network

import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.data.GeocodingRepository
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryRequest
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.PartnerOriginSource
import com.reevent.app.core.model.PlaceSuggestion
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.SyncState
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.call.body
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabasePartnerGateway @Inject constructor(
    private val authGateway: SupabaseAuthGateway,
) {
    fun isConfigured(): Boolean = authGateway.isConfigured()

    suspend fun discover(request: PartnerDiscoveryRequest): PartnerDiscoveryResult =
        authGateway.withConfiguredClient { client ->
            val response = client.postgrest.rpc(
                "find_partner_programmes",
                buildJsonObject {
                    put("p_resource_id", request.resourceId?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_origin_latitude", request.deviceLocation?.latitude?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_origin_longitude", request.deviceLocation?.longitude?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_material_family", request.filters.materialFamily?.name?.let(::JsonPrimitive) ?: JsonNull)
                    put(
                        "p_programme_types",
                        if (request.filters.programmeTypes.isEmpty()) JsonNull else JsonArray(
                            request.filters.programmeTypes.sortedBy(ProgrammeType::name).map { JsonPrimitive(it.name) },
                        ),
                    )
                    put("p_max_distance_km", request.filters.maximumDistanceKm?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_pickup_only", request.filters.pickupOnly)
                    put("p_limit", request.limit)
                    put("p_offset", request.offset)
                },
            ).decodeSingle<DiscoveryResponse>()
            response.toDomain()
        }

    @Serializable
    private data class DiscoveryResponse(
        @SerialName("origin_source") val originSource: String = "NONE",
        @SerialName("origin_address") val originAddress: String? = null,
        @SerialName("origin_latitude") val originLatitude: Double? = null,
        @SerialName("origin_longitude") val originLongitude: Double? = null,
        val candidates: List<CandidateResponse> = emptyList(),
        @SerialName("exclusion_counts") val exclusionCounts: Map<String, Int> = emptyMap(),
        @SerialName("next_offset") val nextOffset: Int? = null,
    ) {
        fun toDomain(): PartnerDiscoveryResult {
            val source = runCatching { PartnerOriginSource.valueOf(originSource) }.getOrDefault(PartnerOriginSource.NONE)
            val label = originAddress?.takeIf(String::isNotBlank) ?: when (source) {
                PartnerOriginSource.DEVICE -> "Current approximate location"
                PartnerOriginSource.RESOURCE -> "Resource location"
                PartnerOriginSource.EVENT -> "Event location"
                PartnerOriginSource.NONE -> "Discovery origin"
            }
            return PartnerDiscoveryResult(
                origin = geoLocation(label, originLatitude, originLongitude),
                originSource = source,
                candidates = candidates.mapNotNull(CandidateResponse::toDomainOrNull),
                exclusionCounts = exclusionCounts,
                nextOffset = nextOffset,
            )
        }
    }

    @Serializable
    private data class CandidateResponse(
        val id: String,
        @SerialName("partner_id") val partnerId: String,
        val name: String,
        @SerialName("programme_type") val programmeType: String,
        @SerialName("accepted_categories") val acceptedCategories: List<String> = emptyList(),
        @SerialName("accepted_material_families") val acceptedMaterialFamilies: List<String> = emptyList(),
        @SerialName("accepted_conditions") val acceptedConditions: List<String> = emptyList(),
        @SerialName("minimum_quantity") val minimumQuantity: Double? = null,
        @SerialName("maximum_quantity") val maximumQuantity: Double? = null,
        val unit: String? = null,
        @SerialName("remaining_capacity") val remainingCapacity: Double? = null,
        @SerialName("coin_direction") val coinDirection: String,
        @SerialName("unit_coin_amount") val unitCoinAmount: Long? = null,
        @SerialName("pickup_available") val pickupAvailable: Boolean,
        @SerialName("address_text") val addressText: String,
        val latitude: Double,
        val longitude: Double,
        @SerialName("processing_method") val processingMethod: String,
        val terms: String,
        @SerialName("distance_km") val distanceKm: Double? = null,
        val score: Int? = null,
        val reasons: List<String> = emptyList(),
        @SerialName("created_at_ms") val createdAt: Long,
        @SerialName("updated_at_ms") val updatedAt: Long,
    ) {
        fun toDomainOrNull(): PartnerCandidate? = runCatching {
            PartnerCandidate(
                programme = CircularProgramme(
                    id = id,
                    partnerId = partnerId,
                    name = name,
                    type = ProgrammeType.valueOf(programmeType),
                    acceptedMaterialFamilies = acceptedMaterialFamilies.map(MaterialFamily::valueOf).toSet(),
                    location = addressText,
                    active = true,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    syncState = SyncState.SYNCED,
                    acceptedCategories = acceptedCategories,
                    acceptedConditions = acceptedConditions.map(ResourceCondition::valueOf).toSet(),
                    minimumQuantity = minimumQuantity,
                    maximumQuantity = maximumQuantity,
                    unit = unit,
                    remainingCapacity = remainingCapacity,
                    coinDirection = CoinDirection.valueOf(coinDirection),
                    unitCoinAmount = unitCoinAmount,
                    pickupAvailable = pickupAvailable,
                    geoLocation = GeoLocation(addressText, latitude, longitude),
                    processingMethod = processingMethod,
                    terms = terms,
                ),
                distanceKm = distanceKm,
                score = score,
                reasons = reasons,
            )
        }.getOrNull()
    }

    private companion object {
        fun geoLocation(address: String, latitude: Double?, longitude: Double?): GeoLocation? =
            if (latitude != null && longitude != null) GeoLocation(address, latitude, longitude) else null
    }
}

@Singleton
class SupabaseGeocodingRepository @Inject constructor(
    private val authGateway: SupabaseAuthGateway,
) : GeocodingRepository {
    override suspend fun search(query: String, proximity: GeoLocation?): AppResult<List<PlaceSuggestion>> {
        if (query.trim().length < 3) return AppResult.Failure(FailureReason.VALIDATION)
        return invoke(
            GeocodingRequest(
                operation = "forward",
                query = query.trim(),
                latitude = proximity?.latitude,
                longitude = proximity?.longitude,
            ),
        ).map { it.suggestions.mapNotNull(GeocodingSuggestionResponse::toDomainOrNull) }
    }

    override suspend fun reverse(location: GeoLocation): AppResult<PlaceSuggestion> =
        invoke(
            GeocodingRequest(
                operation = "reverse",
                latitude = location.latitude,
                longitude = location.longitude,
            ),
        ).let { result ->
            when (result) {
                is AppResult.Success -> result.value.suggestions.firstOrNull()?.toDomainOrNull()?.let { AppResult.Success(it) }
                    ?: AppResult.Failure(FailureReason.SERVER)
                is AppResult.Failure -> result
            }
        }

    private suspend fun invoke(request: GeocodingRequest): AppResult<GeocodingResponse> = try {
        if (!authGateway.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)
        val response = authGateway.withConfiguredClient { client ->
            client.functions.invoke("maptiler-geocode", request).body<GeocodingResponse>()
        }
        AppResult.Success(response)
    } catch (error: IOException) {
        AppResult.Failure(FailureReason.OFFLINE, error)
    } catch (error: Throwable) {
        AppResult.Failure(
            if (error.message?.contains("429") == true) FailureReason.RATE_LIMITED else FailureReason.SERVER,
            error,
        )
    }

    @Serializable
    private data class GeocodingRequest(
        val operation: String,
        val query: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    @Serializable
    private data class GeocodingResponse(
        val suggestions: List<GeocodingSuggestionResponse> = emptyList(),
    )

    @Serializable
    private data class GeocodingSuggestionResponse(
        val id: String,
        val label: String,
        val latitude: Double,
        val longitude: Double,
    ) {
        fun toDomainOrNull(): PlaceSuggestion? = runCatching {
            PlaceSuggestion(id, label, GeoLocation(label, latitude, longitude))
        }.getOrNull()
    }

    private inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }
}
