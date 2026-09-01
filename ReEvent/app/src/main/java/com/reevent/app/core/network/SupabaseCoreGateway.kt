package com.reevent.app.core.network

import android.util.Log
import com.reevent.app.BuildConfig
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.DiscoverableEvent
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.PassportHistoryEntry
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.feature.passports.PassportQrPayload
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

data class RemoteCoreSnapshot(
    val events: List<Event>,
    val resources: List<ResourceItem>,
    val passports: List<ResourcePassport>,
    val programmes: List<CircularProgramme>,
    val transactions: List<CircularTransaction>,
    val impact: List<ImpactRecord>
)

/** All reads run with the signed-in user's JWT, so Supabase RLS remains authoritative. */
@Singleton
class SupabaseCoreGateway @Inject constructor(private val authGateway: SupabaseAuthGateway) {
    suspend fun fetchDiscoverableEvents(): List<DiscoverableEvent> = authGateway.withConfiguredClient { client ->
        client.postgrest.rpc("list_discoverable_events")
            .decodeList<DiscoverableEventRow>()
            .map { row ->
                DiscoverableEvent(
                    id = row.id,
                    name = row.name,
                    description = row.description,
                    eventType = row.eventType,
                    startsAt = millis(row.startsAt),
                    endsAt = millis(row.endsAt),
                    timezoneId = row.timezoneId,
                    venue = row.addressText,
                    recoveryTargetPercent = row.recoveryTargetPercent,
                )
            }
    }

    suspend fun fetchAuthorisedSnapshot(): RemoteCoreSnapshot = authGateway.withConfiguredClient { client ->
        // Resource listings are the marketplace's required data. Other tables have narrower
        // RLS rules, so a denied or malformed optional row must never discard public resources.
        supervisorScope {
            val events = async { client.from("events").select().decodeList<EventRow>() }
            val resources = async { client.from("resource_items").select().decodeList<ResourceRow>() }
            val resourcePhotos = async { client.from("resource_photos").select().decodeList<ResourcePhotoRow>() }
            val passports = async { client.from("resource_passports").select().decodeList<PassportRow>() }
            val passportEvents = async { client.from("passport_events").select().decodeList<PassportEventRow>() }
            val programmes = async { client.from("circular_programmes").select().decodeList<ProgrammeRow>() }
            val transactions = async { client.from("circular_transactions").select().decodeList<TransactionRow>() }
            val impact = async { client.from("impact_records").select().decodeList<ImpactRow>() }
            val listings = async { client.from("marketplace_listings").select().decodeList<MarketplaceListingRow>() }
            val resourceRows = resources.await()
            val publishedListingsByResource = listings.awaitOrEmpty()
                .mapNotNull(MarketplaceListingRow::toPublishedDomainOrNull)
                .associateBy { it.resourceId }
            val primaryPhotoPathsByResource = primaryResourcePhotoPaths(resourcePhotos.awaitOrEmpty())
            val resourceModels = resourceRows.mapNotNull { row ->
                row.toDomainOrNull(
                    listing = publishedListingsByResource[row.id]?.listing,
                    imagePaths = primaryPhotoPathsByResource[row.id].orEmpty()
                )
            }
            if (resourceModels.size != resourceRows.size) {
                Log.w(TAG, "Skipped ${resourceRows.size - resourceModels.size} malformed marketplace resource row(s)")
            }
            val historyByPassport = passportEvents.awaitOrEmpty()
                .mapNotNull(PassportEventRow::toDomainOrNull)
                .groupBy(PassportEventModel::passportId)
            RemoteCoreSnapshot(
                events.awaitOrEmpty().mapNotNull(EventRow::toDomainOrNull),
                resourceModels,
                passports.awaitOrEmpty().mapNotNull { row ->
                    row.toDomainOrNull(historyByPassport[row.id].orEmpty().map(PassportEventModel::entry))
                },
                programmes.awaitOrEmpty().mapNotNull(ProgrammeRow::toDomainOrNull),
                transactions.awaitOrEmpty().mapNotNull(TransactionRow::toDomainOrNull),
                impact.awaitOrEmpty().mapNotNull(ImpactRow::toDomainOrNull)
            )
        }
    }

    @Serializable private data class EventRow(
        val id: String, @SerialName("owner_id") val ownerId: String? = null, val name: String, val description: String,
        @SerialName("address_text") val addressText: String, @SerialName("starts_at") val startsAt: String, @SerialName("ends_at") val endsAt: String,
        val latitude: Double? = null, val longitude: Double? = null,
        @SerialName("event_type") val eventType: String? = null,
        @SerialName("timezone_id") val timezoneId: String? = null,
        @SerialName("expected_attendance") val expectedAttendance: Int? = null,
        @SerialName("recovery_target_percent") val recoveryTargetPercent: Double = 0.0,
        val status: String, @SerialName("archived_at") val archivedAt: String? = null,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching { Event(id, requireNotNull(ownerId), name, description, addressText, millis(startsAt), millis(endsAt), status, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archivedAt != null, geoLocation(addressText, latitude, longitude), eventType, timezoneId, expectedAttendance, recoveryTargetPercent) }.getOrNull() }

    @Serializable private data class DiscoverableEventRow(
        val id: String,
        val name: String,
        val description: String,
        @SerialName("event_type") val eventType: String,
        @SerialName("starts_at") val startsAt: String,
        @SerialName("ends_at") val endsAt: String,
        @SerialName("timezone_id") val timezoneId: String,
        @SerialName("address_text") val addressText: String,
        @SerialName("recovery_target_percent") val recoveryTargetPercent: Double,
    )

    @Serializable private data class ResourceRow(
        val id: String, @SerialName("origin_event_id") val eventId: String, @SerialName("current_owner_id") val ownerId: String? = null,
        val title: String, val category: String,
        @SerialName("material_family") val materialFamily: String,
        @SerialName("material_detail") val materialDetail: String? = null,
        val condition: String, val quantity: Double, val unit: String,
        @SerialName("address_text") val addressText: String? = null, val latitude: Double? = null, val longitude: Double? = null,
        @SerialName("reuse_count") val reuseCount: Int = 0,
        val status: String, @SerialName("archived_at") val archivedAt: String? = null,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) {
        fun toDomainOrNull(listing: MarketplaceListing?, imagePaths: List<String>) = runCatching {
            ResourceItem(
                id, eventId, requireNotNull(ownerId), title, category, MaterialFamily.valueOf(materialFamily), materialDetail,
                enumValue(condition, ResourceCondition.entries), quantity, unit,
                enumValue(status, ResourceStatus.entries), listing?.buyUnitPrice ?: listing?.rentUnitPrice ?: 0,
                imagePaths, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archivedAt != null, listing,
                geoLocation(addressText.orEmpty(), latitude, longitude),
                reuseCount,
            )
        }.onFailure { Log.w(TAG, "Ignoring malformed resource row $id", it) }.getOrNull()
    }

    @Serializable internal data class ResourcePhotoRow(
        @SerialName("resource_id") val resourceId: String,
        @SerialName("storage_path") val storagePath: String,
        @SerialName("sort_order") val sortOrder: Int
    )

    @Serializable private data class MarketplaceListingRow(
        val id: String,
        @SerialName("resource_id") val resourceId: String,
        @SerialName("allowed_actions") val allowedActions: List<String>,
        @SerialName("published_quantity") val publishedQuantity: Double,
        @SerialName("unit_coin_price_buy") val buyUnitPrice: Long? = null,
        @SerialName("unit_coin_price_rent") val rentUnitPrice: Long? = null,
        @SerialName("default_duration_days") val defaultDurationDays: Int? = null,
        val terms: String = "",
        val status: String
    ) {
        fun toPublishedDomainOrNull(): PublishedListing? =
            if (status != "PUBLISHED") null else runCatching {
                PublishedListing(
                    resourceId = resourceId,
                    listing = MarketplaceListing(
                        id = id,
                        allowedActions = allowedActions.map(TransactionType::valueOf),
                        publishedQuantity = publishedQuantity,
                        buyUnitPrice = buyUnitPrice,
                        rentUnitPrice = rentUnitPrice,
                        defaultDurationDays = defaultDurationDays,
                        terms = terms
                    )
                )
            }.onFailure { Log.w(TAG, "Ignoring malformed marketplace listing $id", it) }.getOrNull()
    }

    private data class PublishedListing(val resourceId: String, val listing: MarketplaceListing)

    @Serializable private data class PassportRow(
        val id: String, @SerialName("resource_id") val resourceId: String, @SerialName("public_token") val publicToken: String,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull(history: List<PassportHistoryEntry>) = runCatching {
        ResourcePassport(
            id,
            resourceId,
            // Keep the opaque token in the cache if the build has no verifier URL yet, but only
            // render/scan it after PassportQrPayload validates a canonical v1 HTTPS URL.
            PassportQrPayload.canonicalPayload(BuildConfig.PUBLIC_BASE_URL, publicToken) ?: publicToken,
            json.encodeToString(history),
            millis(createdAt),
            millis(updatedAt),
            SyncState.SYNCED
        )
    }.getOrNull() }

    @Serializable private data class PassportEventRow(
        val id: String,
        @SerialName("passport_id") val passportId: String,
        @SerialName("event_type") val eventType: String,
        @SerialName("actor_id") val actorId: String? = null,
        val quantity: Double? = null,
        val unit: String? = null,
        @SerialName("previous_condition") val previousCondition: String? = null,
        @SerialName("new_condition") val newCondition: String? = null,
        @SerialName("public_summary") val publicSummary: String,
        @SerialName("occurred_at") val occurredAt: String
    ) {
        fun toDomainOrNull() = runCatching {
            PassportEventModel(
                passportId = passportId,
                entry = PassportHistoryEntry(
                    occurredAt = millis(occurredAt),
                    action = eventType,
                    actorId = actorId ?: "server",
                    quantity = quantity,
                    unit = unit,
                    previousCondition = previousCondition?.let(ResourceCondition::valueOf),
                    newCondition = newCondition?.let(ResourceCondition::valueOf),
                    publicSummary = publicSummary
                )
            )
        }.onFailure { Log.w(TAG, "Ignoring malformed passport event $id", it) }.getOrNull()
    }

    private data class PassportEventModel(
        val passportId: String,
        val entry: PassportHistoryEntry
    )

    @Serializable private data class ProgrammeRow(
        val id: String, @SerialName("partner_id") val partnerId: String, val name: String, @SerialName("programme_type") val type: String,
        @SerialName("accepted_categories") val acceptedCategories: List<String> = emptyList(),
        @SerialName("accepted_material_families") val acceptedMaterialFamilies: List<String> = emptyList(),
        @SerialName("accepted_conditions") val acceptedConditions: List<String> = emptyList(),
        @SerialName("minimum_quantity") val minimumQuantity: Double? = null,
        @SerialName("maximum_quantity") val maximumQuantity: Double? = null,
        val unit: String? = null,
        @SerialName("remaining_capacity") val remainingCapacity: Double? = null,
        @SerialName("coin_direction") val coinDirection: String = CoinDirection.FREE.name,
        @SerialName("unit_coin_amount") val unitCoinAmount: Long? = null,
        @SerialName("pickup_available") val pickupAvailable: Boolean = false,
        @SerialName("address_text") val location: String, val latitude: Double? = null, val longitude: Double? = null,
        @SerialName("processing_method") val processingMethod: String = "", val terms: String = "", val active: Boolean,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching {
        CircularProgramme(
            id, partnerId, name, ProgrammeType.valueOf(type), acceptedMaterialFamilies.map(MaterialFamily::valueOf).toSet(), location, active,
            millis(createdAt), millis(updatedAt), SyncState.SYNCED, acceptedCategories,
            acceptedConditions.map(ResourceCondition::valueOf).toSet(), minimumQuantity, maximumQuantity,
            unit, remainingCapacity, CoinDirection.valueOf(coinDirection), unitCoinAmount, pickupAvailable,
            geoLocation(location, latitude, longitude), processingMethod, terms,
        )
    }.getOrNull() }

    @Serializable private data class TransactionRow(
        val id: String, @SerialName("origin_event_id") val eventId: String, @SerialName("resource_id") val resourceId: String,
        @SerialName("programme_id") val programmeId: String? = null,
        @SerialName("counter_resource_id") val counterResourceId: String? = null,
        @SerialName("requester_id") val requesterId: String? = null,
        @SerialName("sender_id") val senderId: String? = null, @SerialName("receiver_id") val receiverId: String? = null, @SerialName("partner_id") val partnerId: String? = null,
        @SerialName("transaction_type") val type: String, val status: String, val quantity: Double,
        @SerialName("approved_at") val approvedAt: String? = null,
        @SerialName("in_transit_at") val inTransitAt: String? = null,
        @SerialName("active_at") val activeAt: String? = null,
        @SerialName("return_started_at") val returnStartedAt: String? = null,
        @SerialName("completed_at") val completedAt: String? = null,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching {
        CircularTransaction(
            id, eventId, resourceId, requireNotNull(senderId), requireNotNull(receiverId), partnerId,
            TransactionType.valueOf(type), TransactionStatus.valueOf(status), quantity, millis(createdAt),
            millis(updatedAt), SyncState.SYNCED, false, requireNotNull(requesterId), counterResourceId,
            programmeId, approvedAt?.let(::millis), inTransitAt?.let(::millis), activeAt?.let(::millis),
            returnStartedAt?.let(::millis), completedAt?.let(::millis),
        )
    }.getOrNull() }

    @Serializable private data class ImpactRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("resource_id") val resourceId: String,
        @SerialName("transaction_id") val transactionId: String, @SerialName("transaction_type") val transactionType: String,
        @SerialName("completed_quantity") val completedQuantity: Double, val unit: String,
        @SerialName("material_diverted_kg") val materialDivertedKg: Double? = null,
        @SerialName("emissions_avoided_kg") val emissionsAvoidedKg: Double? = null,
        @SerialName("recoins_transferred") val recoinsTransferred: Long,
        @SerialName("recoins_rewarded") val recoinsRewarded: Long,
        @SerialName("calculated_at") val calculatedAt: String
    ) { fun toDomainOrNull() = runCatching { ImpactRecord(id, eventId, resourceId, transactionId, TransactionType.valueOf(transactionType), completedQuantity, unit, materialDivertedKg, emissionsAvoidedKg, recoinsTransferred, recoinsRewarded, millis(calculatedAt), SyncState.SYNCED) }.getOrNull() }

    private companion object {
        const val TAG = "ReEventCoreSync"
        val json = Json { ignoreUnknownKeys = true }
        fun millis(value: String): Long {
            // Android's desugared Instant parser rejects Supabase's otherwise valid UTC offset
            // form when it also contains variable-width fractional seconds. Canonicalise UTC to
            // `Z` before parsing so values such as `.42075+00:00` survive a refresh.
            val canonical = when {
                value.endsWith("+00:00") -> value.dropLast(6) + "Z"
                value.endsWith("+00") -> value.dropLast(3) + "Z"
                else -> value
            }
            return Instant.parse(canonical).toEpochMilli()
        }
        fun <T : Enum<T>> enumValue(value: String, entries: List<T>): T {
            val normalized = value.trim().uppercase().replace('-', '_').replace(' ', '_')
            return entries.firstOrNull { it.name == normalized }
                ?: error("Unsupported enum value: $value")
        }

        fun geoLocation(address: String, latitude: Double?, longitude: Double?): GeoLocation? =
            if (latitude != null && longitude != null) GeoLocation(address, latitude, longitude) else null
    }
}

internal fun primaryResourcePhotoPaths(
    rows: List<SupabaseCoreGateway.ResourcePhotoRow>
): Map<String, List<String>> = rows
    .filter { it.resourceId.isNotBlank() && it.storagePath.isNotBlank() }
    .groupBy(SupabaseCoreGateway.ResourcePhotoRow::resourceId)
    .mapValues { (_, photos) ->
        listOf(photos.maxWith(compareBy<SupabaseCoreGateway.ResourcePhotoRow> { it.sortOrder }.thenBy { it.storagePath }).storagePath)
    }

private suspend fun <T> Deferred<List<T>>.awaitOrEmpty(): List<T> = runCatching { await() }.getOrDefault(emptyList())
