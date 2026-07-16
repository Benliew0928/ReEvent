package com.reevent.app.core.network

import android.util.Log
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    suspend fun fetchAuthorisedSnapshot(): RemoteCoreSnapshot = authGateway.withConfiguredClient { client ->
        // Resource listings are the marketplace's required data. Other tables have narrower
        // RLS rules, so a denied or malformed optional row must never discard public resources.
        supervisorScope {
            val events = async { client.from("events").select().decodeList<EventRow>() }
            val resources = async { client.from("resource_items").select().decodeList<ResourceRow>() }
            val passports = async { client.from("resource_passports").select().decodeList<PassportRow>() }
            val programmes = async { client.from("circular_programmes").select().decodeList<ProgrammeRow>() }
            val transactions = async { client.from("circular_transactions").select().decodeList<TransactionRow>() }
            val impact = async { client.from("impact_records").select().decodeList<ImpactRow>() }
            val resourceRows = resources.await()
            val resourceModels = resourceRows.mapNotNull(ResourceRow::toDomainOrNull)
            if (resourceModels.size != resourceRows.size) {
                Log.w(TAG, "Skipped ${resourceRows.size - resourceModels.size} malformed marketplace resource row(s)")
            }
            RemoteCoreSnapshot(
                events.awaitOrEmpty().mapNotNull(EventRow::toDomainOrNull),
                resourceModels,
                passports.awaitOrEmpty().mapNotNull(PassportRow::toDomainOrNull),
                programmes.awaitOrEmpty().mapNotNull(ProgrammeRow::toDomainOrNull),
                transactions.awaitOrEmpty().mapNotNull(TransactionRow::toDomainOrNull),
                impact.awaitOrEmpty().mapNotNull(ImpactRow::toDomainOrNull)
            )
        }
    }

    @Serializable private data class EventRow(
        val id: String, @SerialName("owner_id") val ownerId: String, val name: String, val description: String,
        val venue: String, @SerialName("starts_at") val startsAt: String, @SerialName("ends_at") val endsAt: String,
        val status: String, val archived: Boolean, @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching { Event(id, ownerId, name, description, venue, millis(startsAt), millis(endsAt), status, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archived) }.getOrNull() }

    @Serializable private data class ResourceRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("owner_id") val ownerId: String,
        val title: String, val category: String, val material: String, val condition: String, val quantity: Int, val unit: String,
        val status: String, @SerialName("value_cents") val valueCents: Long, @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
        val archived: Boolean, @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) {
        fun toDomainOrNull() = runCatching {
            ResourceItem(
                id, eventId, ownerId, title, category, material,
                enumValue(condition, ResourceCondition.entries), quantity, unit,
                enumValue(status, ResourceStatus.entries), valueCents, imageUrls,
                millis(createdAt), millis(updatedAt), SyncState.SYNCED, archived
            )
        }.onFailure { Log.w(TAG, "Ignoring malformed resource row $id", it) }.getOrNull()
    }

    @Serializable private data class PassportRow(
        val id: String, @SerialName("resource_id") val resourceId: String, @SerialName("qr_payload") val qrPayload: String,
        val history: kotlinx.serialization.json.JsonElement, @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching { ResourcePassport(id, resourceId, qrPayload, json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), history), millis(createdAt), millis(updatedAt), SyncState.SYNCED) }.getOrNull() }

    @Serializable private data class ProgrammeRow(
        val id: String, @SerialName("partner_id") val partnerId: String, val name: String, @SerialName("programme_type") val type: String,
        @SerialName("accepted_materials") val acceptedMaterials: List<String> = emptyList(), val location: String, val active: Boolean,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching { CircularProgramme(id, partnerId, name, ProgrammeType.valueOf(type), acceptedMaterials, location, active, millis(createdAt), millis(updatedAt), SyncState.SYNCED) }.getOrNull() }

    @Serializable private data class TransactionRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("resource_id") val resourceId: String,
        @SerialName("sender_id") val senderId: String, @SerialName("receiver_id") val receiverId: String, @SerialName("partner_id") val partnerId: String? = null,
        @SerialName("transaction_type") val type: String, val status: String, val quantity: Int, val archived: Boolean,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching { CircularTransaction(id, eventId, resourceId, senderId, receiverId, partnerId, TransactionType.valueOf(type), TransactionStatus.valueOf(status), quantity, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archived) }.getOrNull() }

    @Serializable private data class ImpactRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("resource_id") val resourceId: String? = null,
        @SerialName("transaction_id") val transactionId: String? = null, @SerialName("material_diverted_kg") val materialDivertedKg: Double,
        @SerialName("emissions_avoided_kg") val emissionsAvoidedKg: Double, @SerialName("value_recovered_cents") val valueRecoveredCents: Long,
        @SerialName("calculated_at") val calculatedAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomainOrNull() = runCatching { ImpactRecord(id, eventId, resourceId, transactionId, materialDivertedKg, emissionsAvoidedKg, valueRecoveredCents, millis(calculatedAt), millis(updatedAt), SyncState.SYNCED) }.getOrNull() }

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
    }
}

private suspend fun <T> Deferred<List<T>>.awaitOrEmpty(): List<T> = runCatching { await() }.getOrDefault(emptyList())
