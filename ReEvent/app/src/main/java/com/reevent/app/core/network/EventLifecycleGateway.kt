package com.reevent.app.core.network

import com.reevent.app.core.model.Event
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.SyncState
import io.github.jan.supabase.postgrest.postgrest
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Protected server commands for the organiser-owned event lifecycle. */
interface EventLifecycleGateway {
    fun isConfigured(): Boolean
    suspend fun publish(event: Event): Event
    suspend fun updateActive(event: Event): Event
    suspend fun complete(eventId: String): Event
    suspend fun archive(eventId: String): Event
}

@Singleton
class SupabaseEventLifecycleGateway @Inject constructor(
    private val authGateway: SupabaseAuthGateway,
) : EventLifecycleGateway {
    override fun isConfigured(): Boolean = authGateway.isConfigured()

    override suspend fun publish(event: Event): Event = authGateway.withConfiguredClient { client ->
        client.postgrest.rpc(
            "publish_event",
            eventPayload(event),
        ).decodeSingle<EventCommandEnvelope>().event.toDomain()
    }

    override suspend fun updateActive(event: Event): Event = authGateway.withConfiguredClient { client ->
        client.postgrest.rpc(
            "update_active_event",
            eventPayload(event),
        ).decodeSingle<EventCommandEnvelope>().event.toDomain()
    }

    override suspend fun complete(eventId: String): Event = command(
        name = "complete_event",
        eventId = eventId,
    )

    override suspend fun archive(eventId: String): Event = command(
        name = "archive_event",
        eventId = eventId,
    )

    private fun eventPayload(event: Event) = buildJsonObject {
        put("p_event_id", event.id)
        put("p_name", event.name)
        put("p_description", event.description)
        put("p_event_type", event.eventType)
        put("p_starts_at", Instant.ofEpochMilli(event.startsAt).toString())
        put("p_ends_at", Instant.ofEpochMilli(event.endsAt).toString())
        put("p_timezone_id", event.timezoneId)
        put("p_address_text", event.venue)
        put("p_latitude", event.geoLocation?.latitude)
        put("p_longitude", event.geoLocation?.longitude)
        put("p_expected_attendance", event.expectedAttendance)
        put("p_recovery_target_percent", event.recoveryTargetPercent)
        put("p_idempotency_key", UUID.randomUUID().toString())
    }

    private suspend fun command(name: String, eventId: String): Event = authGateway.withConfiguredClient { client ->
        client.postgrest.rpc(
            name,
            buildJsonObject {
                put("p_event_id", eventId)
                // A fresh key is safe because the RPC is state-idempotent: retrying a command
                // after a lost response returns the already-accepted terminal/current row.
                put("p_idempotency_key", UUID.randomUUID().toString())
            },
        ).decodeSingle<EventCommandEnvelope>().event.toDomain()
    }

    @Serializable
    private data class EventCommandEnvelope(
        val event: EventRow,
        val replayed: Boolean = false,
    )

    @Serializable
    private data class EventRow(
        val id: String,
        @SerialName("owner_id") val ownerId: String? = null,
        val name: String,
        val description: String,
        @SerialName("event_type") val eventType: String? = null,
        @SerialName("starts_at") val startsAt: String,
        @SerialName("ends_at") val endsAt: String,
        @SerialName("timezone_id") val timezoneId: String? = null,
        @SerialName("address_text") val addressText: String,
        val latitude: Double? = null,
        val longitude: Double? = null,
        @SerialName("expected_attendance") val expectedAttendance: Int? = null,
        @SerialName("recovery_target_percent") val recoveryTargetPercent: Double = 0.0,
        val status: String,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String,
        @SerialName("archived_at") val archivedAt: String? = null,
    ) {
        fun toDomain(): Event = Event(
            id = id,
            ownerId = requireNotNull(ownerId),
            name = name,
            description = description,
            venue = addressText,
            startsAt = millis(startsAt),
            endsAt = millis(endsAt),
            status = status,
            createdAt = millis(createdAt),
            updatedAt = millis(updatedAt),
            syncState = SyncState.SYNCED,
            archived = archivedAt != null,
            geoLocation = if (latitude != null && longitude != null) GeoLocation(addressText, latitude, longitude) else null,
            eventType = eventType,
            timezoneId = timezoneId,
            expectedAttendance = expectedAttendance,
            recoveryTargetPercent = recoveryTargetPercent,
        )

        private fun millis(value: String): Long {
            val canonical = when {
                value.endsWith("+00:00") -> value.dropLast(6) + "Z"
                value.endsWith("+00") -> value.dropLast(3) + "Z"
                else -> value
            }
            return Instant.parse(canonical).toEpochMilli()
        }
    }
}

fun Throwable.eventLifecycleFailureReason(): com.reevent.app.core.data.FailureReason {
    val value = message.orEmpty()
    return when {
        "AUTH_" in value || "PROFILE_REQUIRED" in value -> com.reevent.app.core.data.FailureReason.UNAUTHENTICATED
        "EVENT_NOT_DRAFT" in value || "EVENT_NOT_ACTIVE" in value || "EVENT_NOT_OWNED" in value ||
            "EVENT_OWNER_REQUIRED" in value || "EVENT_HAS_OPEN_TRANSACTIONS" in value -> com.reevent.app.core.data.FailureReason.CONFLICT
        "EVENT_" in value || "REQUIRED" in value || "TIMEZONE" in value -> com.reevent.app.core.data.FailureReason.VALIDATION
        "CONFIG" in value -> com.reevent.app.core.data.FailureReason.CONFIGURATION
        else -> com.reevent.app.core.data.FailureReason.SERVER
    }
}
