package com.reevent.app.core.network

import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class LifecycleCommandType {
    REQUEST_MARKETPLACE,
    REQUEST_PROGRAMME,
    APPROVE,
    REJECT,
    CANCEL,
    BEGIN_HANDOVER,
    CONFIRM_RECEIPT,
    BEGIN_RETURN,
    CONFIRM_RETURN
}

/** Canonical payload persisted before an RPC is attempted. */
@Serializable
data class LifecycleCommandPayload(
    val resourceId: String? = null,
    val listingId: String? = null,
    val programmeId: String? = null,
    val transactionId: String? = null,
    val transactionType: String? = null,
    val quantity: Double? = null,
    val counterResourceId: String? = null,
    val reason: String? = null,
    val resourceSide: String? = null
)

interface LifecycleCommandGateway {
    fun isConfigured(): Boolean
    suspend fun resolvePublishedListingId(resourceId: String): String
    suspend fun execute(
        type: LifecycleCommandType,
        payload: LifecycleCommandPayload,
        idempotencyKey: String
    ): CircularTransaction
}

@Singleton
class SupabaseLifecycleCommandGateway @Inject constructor(
    private val authGateway: SupabaseAuthGateway
) : LifecycleCommandGateway {
    override fun isConfigured(): Boolean = authGateway.isConfigured()

    override suspend fun resolvePublishedListingId(resourceId: String): String =
        authGateway.withConfiguredClient { client ->
            client.from("marketplace_listings").select {
                filter {
                    eq("resource_id", resourceId)
                    eq("status", "PUBLISHED")
                }
            }.decodeSingle<ListingReference>().id
        }

    override suspend fun execute(
        type: LifecycleCommandType,
        payload: LifecycleCommandPayload,
        idempotencyKey: String
    ): CircularTransaction = when (type) {
        LifecycleCommandType.REQUEST_MARKETPLACE -> rpc(
            "request_listing_transaction",
            buildJsonObject {
                put("p_listing_id", payload.require(LifecycleCommandPayload::listingId, "listingId"))
                put("p_transaction_type", payload.require(LifecycleCommandPayload::transactionType, "transactionType"))
                put("p_quantity", payload.require(LifecycleCommandPayload::quantity, "quantity"))
                put("p_counter_resource_id", payload.counterResourceId?.let(::JsonPrimitive) ?: JsonNull)
                put("p_request_reason", payload.reason?.let(::JsonPrimitive) ?: JsonNull)
                put("p_idempotency_key", idempotencyKey)
            }
        )

        LifecycleCommandType.REQUEST_PROGRAMME -> rpc(
            "request_programme_transaction",
            buildJsonObject {
                put("p_programme_id", payload.require(LifecycleCommandPayload::programmeId, "programmeId"))
                put("p_resource_id", payload.require(LifecycleCommandPayload::resourceId, "resourceId"))
                put("p_quantity", payload.require(LifecycleCommandPayload::quantity, "quantity"))
                put("p_request_reason", payload.reason?.let(::JsonPrimitive) ?: JsonNull)
                put("p_idempotency_key", idempotencyKey)
            }
        )

        LifecycleCommandType.APPROVE -> transactionCommand("approve_transaction", payload, idempotencyKey)
        LifecycleCommandType.REJECT -> transactionCommand("reject_transaction", payload, idempotencyKey, requireReason = true)
        LifecycleCommandType.CANCEL -> transactionCommand("cancel_transaction", payload, idempotencyKey, requireReason = true)
        LifecycleCommandType.BEGIN_HANDOVER -> sideCommand("begin_transaction_handover", payload, idempotencyKey)
        LifecycleCommandType.CONFIRM_RECEIPT -> sideCommand("confirm_transaction_receipt", payload, idempotencyKey)
        LifecycleCommandType.BEGIN_RETURN -> transactionCommand("begin_transaction_return", payload, idempotencyKey)
        LifecycleCommandType.CONFIRM_RETURN -> transactionCommand("confirm_transaction_return", payload, idempotencyKey)
    }

    private suspend fun transactionCommand(
        name: String,
        payload: LifecycleCommandPayload,
        idempotencyKey: String,
        requireReason: Boolean = false
    ) = rpc(
        name,
        buildJsonObject {
            put("p_transaction_id", payload.require(LifecycleCommandPayload::transactionId, "transactionId"))
            if (requireReason) put("p_reason", payload.require(LifecycleCommandPayload::reason, "reason"))
            put("p_idempotency_key", idempotencyKey)
        }
    )

    private suspend fun sideCommand(
        name: String,
        payload: LifecycleCommandPayload,
        idempotencyKey: String
    ) = rpc(
        name,
        buildJsonObject {
            put("p_transaction_id", payload.require(LifecycleCommandPayload::transactionId, "transactionId"))
            put("p_resource_side", payload.require(LifecycleCommandPayload::resourceSide, "resourceSide"))
            put("p_idempotency_key", idempotencyKey)
        }
    )

    private suspend fun rpc(
        name: String,
        parameters: kotlinx.serialization.json.JsonObject
    ): CircularTransaction = authGateway.withConfiguredClient { client ->
        // All lifecycle commands return a single JSONB envelope, not a relation. Decoding it
        // as a list lets the database commit while the app incorrectly shows a failure.
        client.postgrest.rpc(name, parameters).decodeAs<RpcEnvelope>().transaction.toDomain()
    }

    private fun <T : Any> LifecycleCommandPayload.require(
        property: (LifecycleCommandPayload) -> T?,
        name: String
    ): T = checkNotNull(property(this)) { "Lifecycle command is missing $name" }

    @Serializable
    private data class ListingReference(val id: String)

    @Serializable
    private data class RpcEnvelope(val transaction: RpcTransaction)

    @Serializable
    private data class RpcTransaction(
        val id: String,
        @SerialName("origin_event_id") val eventId: String,
        @SerialName("resource_id") val resourceId: String,
        @SerialName("counter_resource_id") val counterResourceId: String? = null,
        @SerialName("requester_id") val requesterId: String,
        @SerialName("sender_id") val senderId: String,
        @SerialName("receiver_id") val receiverId: String,
        @SerialName("partner_id") val partnerId: String? = null,
        @SerialName("transaction_type") val type: String,
        val status: String,
        val quantity: Double,
        @SerialName("created_at") val createdAt: String,
        @SerialName("updated_at") val updatedAt: String
    ) {
        fun toDomain() = CircularTransaction(
            id = id,
            eventId = eventId,
            resourceId = resourceId,
            senderId = senderId,
            receiverId = receiverId,
            partnerId = partnerId,
            type = TransactionType.valueOf(type),
            status = TransactionStatus.valueOf(status),
            quantity = quantity,
            createdAt = millis(createdAt),
            updatedAt = millis(updatedAt),
            syncState = SyncState.SYNCED,
            requesterId = requesterId,
            counterResourceId = counterResourceId
        )
    }

    private companion object {
        fun millis(value: String): Long {
            val canonical = when {
                value.endsWith("+00:00") -> value.dropLast(6) + "Z"
                value.endsWith("+00") -> value.dropLast(3) + "Z"
                else -> value
            }
            return Instant.parse(canonical).toEpochMilli()
        }
    }
}

private val terminalLifecycleErrors = setOf(
    "ACTION_NOT_ALLOWED",
    "CATEGORY_NOT_ACCEPTED",
    "CONDITION_NOT_ACCEPTED",
    "COUNTER_RESOURCE_CHANGED",
    "COUNTER_RESOURCE_HAS_OPEN_LISTING",
    "COUNTER_RESOURCE_NOT_ALLOWED",
    "COUNTER_RESOURCE_NOT_OWNED",
    "COUNTER_RESOURCE_REQUIRED",
    "COUNTER_SIDE_NOT_ALLOWED",
    "CUSTODIAN_REQUIRED",
    "CUSTODIAN_RETURN_REQUIRED",
    "CUSTODY_ALLOCATION_MISSING",
    "DECISION_ACTOR_REQUIRED",
    "EXCHANGE_LOTS_CHANGED",
    "EXCHANGE_REQUIRES_WHOLE_AVAILABLE_LOTS",
    "FRACTIONAL_DISCRETE_QUANTITY",
    "HANDOVER_ACTOR_REQUIRED",
    "HANDOVER_CONFIRMATION_REQUIRED",
    "HANDOVER_NOT_IN_TRANSIT",
    "IDEMPOTENCY_KEY_REUSED",
    "INSUFFICIENT_RECOINS",
    "INVALID_QUANTITY",
    "LISTING_ACTION_REQUIRED",
    "LISTING_CHANGED",
    "LISTING_NOT_AVAILABLE",
    "LISTING_OWNER_MISMATCH",
    "MATERIAL_NOT_ACCEPTED",
    "ORIGINAL_OWNER_REQUIRED",
    "PARTNER_DECISION_REQUIRED",
    "PROGRAMME_CHANGED",
    "PROGRAMME_NOT_AVAILABLE",
    "QUANTITY_CONFLICT",
    "QUANTITY_NOT_ELIGIBLE",
    "QUANTITY_UNAVAILABLE",
    "RECEIPT_ACTOR_REQUIRED",
    "RESERVED_ALLOCATION_MISSING",
    "RESOURCE_NOT_OWNED",
    "RETURN_NOT_IN_PROGRESS",
    "SELF_DEALING_FORBIDDEN",
    "TERMINAL_REASON_REQUIRED",
    "TRANSACTION_ACTOR_REQUIRED",
    "TRANSACTION_NOT_APPROVED",
    "TRANSACTION_NOT_CANCELLABLE",
    "TRANSACTION_NOT_IN_TRANSIT",
    "TRANSACTION_NOT_REQUESTED",
    "TRANSACTION_NOT_RETURNABLE",
    "UNIT_NOT_ACCEPTED",
    "UNSUPPORTED_COMPLETION_TYPE"
)

private val validationLifecycleErrors = setOf(
    "ACTION_NOT_ALLOWED",
    "CATEGORY_NOT_ACCEPTED",
    "CONDITION_NOT_ACCEPTED",
    "COUNTER_RESOURCE_NOT_ALLOWED",
    "COUNTER_RESOURCE_REQUIRED",
    "EXCHANGE_REQUIRES_WHOLE_AVAILABLE_LOTS",
    "FRACTIONAL_DISCRETE_QUANTITY",
    "INVALID_QUANTITY",
    "LISTING_ACTION_REQUIRED",
    "MATERIAL_NOT_ACCEPTED",
    "QUANTITY_CONFLICT",
    "QUANTITY_NOT_ELIGIBLE",
    "QUANTITY_UNAVAILABLE",
    "TERMINAL_REASON_REQUIRED",
    "UNIT_NOT_ACCEPTED"
)

fun Throwable.isTerminalLifecycleFailure(): Boolean {
    val value = message.orEmpty()
    return terminalLifecycleErrors.any(value::contains) || "duplicate key" in value.lowercase()
}

fun Throwable.lifecycleFailureReason(): FailureReason {
    val value = message.orEmpty()
    return when {
        "AUTH_" in value || "PROFILE_REQUIRED" in value -> FailureReason.UNAUTHENTICATED
        validationLifecycleErrors.any(value::contains) -> FailureReason.VALIDATION
        terminalLifecycleErrors.any(value::contains) -> FailureReason.CONFLICT
        "CONFIG" in value -> FailureReason.CONFIGURATION
        else -> FailureReason.SERVER
    }
}
