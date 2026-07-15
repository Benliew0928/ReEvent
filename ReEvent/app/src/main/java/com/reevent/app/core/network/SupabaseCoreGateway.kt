package com.reevent.app.core.network

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
import kotlinx.coroutines.coroutineScope
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
        coroutineScope {
            val events = async { client.from("events").select().decodeList<EventRow>() }
            val resources = async { client.from("resource_items").select().decodeList<ResourceRow>() }
            val passports = async { client.from("resource_passports").select().decodeList<PassportRow>() }
            val programmes = async { client.from("circular_programmes").select().decodeList<ProgrammeRow>() }
            val transactions = async { client.from("circular_transactions").select().decodeList<TransactionRow>() }
            val impact = async { client.from("impact_records").select().decodeList<ImpactRow>() }
            RemoteCoreSnapshot(
                events.await().map(EventRow::toDomain),
                resources.await().map(ResourceRow::toDomain),
                passports.await().map(PassportRow::toDomain),
                programmes.await().map(ProgrammeRow::toDomain),
                transactions.await().map(TransactionRow::toDomain),
                impact.await().map(ImpactRow::toDomain)
            )
        }
    }

    @Serializable private data class EventRow(
        val id: String, @SerialName("owner_id") val ownerId: String, val name: String, val description: String,
        val venue: String, @SerialName("starts_at") val startsAt: String, @SerialName("ends_at") val endsAt: String,
        val status: String, val archived: Boolean, @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomain() = Event(id, ownerId, name, description, venue, millis(startsAt), millis(endsAt), status, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archived) }

    @Serializable private data class ResourceRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("owner_id") val ownerId: String,
        val title: String, val category: String, val material: String, val condition: String, val quantity: Int, val unit: String,
        val status: String, @SerialName("value_cents") val valueCents: Long, @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
        val archived: Boolean, @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomain() = ResourceItem(id, eventId, ownerId, title, category, material, ResourceCondition.valueOf(condition), quantity, unit, ResourceStatus.valueOf(status), valueCents, imageUrls, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archived) }

    @Serializable private data class PassportRow(
        val id: String, @SerialName("resource_id") val resourceId: String, @SerialName("qr_payload") val qrPayload: String,
        val history: kotlinx.serialization.json.JsonElement, @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomain() = ResourcePassport(id, resourceId, qrPayload, json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), history), millis(createdAt), millis(updatedAt), SyncState.SYNCED) }

    @Serializable private data class ProgrammeRow(
        val id: String, @SerialName("partner_id") val partnerId: String, val name: String, @SerialName("programme_type") val type: String,
        @SerialName("accepted_materials") val acceptedMaterials: List<String> = emptyList(), val location: String, val active: Boolean,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomain() = CircularProgramme(id, partnerId, name, ProgrammeType.valueOf(type), acceptedMaterials, location, active, millis(createdAt), millis(updatedAt), SyncState.SYNCED) }

    @Serializable private data class TransactionRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("resource_id") val resourceId: String,
        @SerialName("sender_id") val senderId: String, @SerialName("receiver_id") val receiverId: String, @SerialName("partner_id") val partnerId: String? = null,
        @SerialName("transaction_type") val type: String, val status: String, val quantity: Int, val archived: Boolean,
        @SerialName("created_at") val createdAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomain() = CircularTransaction(id, eventId, resourceId, senderId, receiverId, partnerId, TransactionType.valueOf(type), TransactionStatus.valueOf(status), quantity, millis(createdAt), millis(updatedAt), SyncState.SYNCED, archived) }

    @Serializable private data class ImpactRow(
        val id: String, @SerialName("event_id") val eventId: String, @SerialName("resource_id") val resourceId: String? = null,
        @SerialName("transaction_id") val transactionId: String? = null, @SerialName("material_diverted_kg") val materialDivertedKg: Double,
        @SerialName("emissions_avoided_kg") val emissionsAvoidedKg: Double, @SerialName("value_recovered_cents") val valueRecoveredCents: Long,
        @SerialName("calculated_at") val calculatedAt: String, @SerialName("updated_at") val updatedAt: String
    ) { fun toDomain() = ImpactRecord(id, eventId, resourceId, transactionId, materialDivertedKg, emissionsAvoidedKg, valueRecoveredCents, millis(calculatedAt), millis(updatedAt), SyncState.SYNCED) }

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
        fun millis(value: String) = Instant.parse(value).toEpochMilli()
    }
}
