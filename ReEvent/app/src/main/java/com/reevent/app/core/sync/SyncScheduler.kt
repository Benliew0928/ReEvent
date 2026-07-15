package com.reevent.app.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.EventEntity
import com.reevent.app.core.database.ImpactEntity
import com.reevent.app.core.database.PassportEntity
import com.reevent.app.core.database.ProgrammeEntity
import com.reevent.app.core.database.ResourceEntity
import com.reevent.app.core.database.TransactionEntity
import com.reevent.app.core.network.SupabaseAuthGateway
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Singleton
class SyncScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("reevent-core-sync", ExistingWorkPolicy.KEEP, request)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun coordinator(): SyncCoordinator
}

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val coordinator = EntryPointAccessors.fromApplication(applicationContext, SyncWorkerEntryPoint::class.java).coordinator()
        return when (coordinator.syncPending()) {
            SyncOutcome.SYNCED, SyncOutcome.NOT_CONFIGURED -> Result.success()
            SyncOutcome.RETRY -> Result.retry()
        }
    }
}

enum class SyncOutcome { SYNCED, NOT_CONFIGURED, RETRY }

@Singleton
class SyncCoordinator @Inject constructor(
    private val dao: CoreDao,
    private val gateway: SupabaseAuthGateway
) {
    suspend fun syncPending(): SyncOutcome {
        if (!gateway.isConfigured()) return SyncOutcome.NOT_CONFIGURED
        val operations = dao.pendingOperations(limit = 50)
        for (operation in operations) {
            try {
                if (operation.operation == "archive") gateway.archive(operation.tableName, operation.recordId)
                else {
                    val payload = payloadFor(operation.tableName, operation.accountId, operation.recordId)
                    if (payload != null) gateway.upsert(operation.tableName, payload)
                }
                dao.deleteOutbox(operation.id)
                setRecordSyncState(operation.tableName, operation.accountId, operation.recordId, "SYNCED")
            } catch (error: Throwable) {
                dao.markOutboxFailed(operation.id, error.message ?: "Remote sync failed", System.currentTimeMillis())
                setRecordSyncState(operation.tableName, operation.accountId, operation.recordId, "FAILED")
                return SyncOutcome.RETRY
            }
        }
        return SyncOutcome.SYNCED
    }

    private suspend fun payloadFor(table: String, accountId: String, id: String) = when (table) {
        "events" -> dao.event(accountId, id)?.toJson()
        "resource_items" -> dao.resource(accountId, id)?.toJson()
        "resource_passports" -> dao.passport(accountId, id)?.toJson()
        "circular_programmes" -> dao.programme(accountId, id)?.toJson()
        "circular_transactions" -> dao.transaction(accountId, id)?.toJson()
        "impact_records" -> dao.impact(accountId, id)?.toJson()
        else -> null
    }

    private suspend fun setRecordSyncState(table: String, accountId: String, id: String, state: String) = when (table) {
        "events" -> dao.setEventSyncState(accountId, id, state)
        "resource_items" -> dao.setResourceSyncState(accountId, id, state)
        "resource_passports" -> dao.setPassportSyncState(accountId, id, state)
        "circular_programmes" -> dao.setProgrammeSyncState(accountId, id, state)
        "circular_transactions" -> dao.setTransactionSyncState(accountId, id, state)
        "impact_records" -> dao.setImpactSyncState(accountId, id, state)
        else -> Unit
    }
}

private val syncJson = Json { ignoreUnknownKeys = true }
private fun time(value: Long) = Instant.ofEpochMilli(value).toString()
private fun JsonElement.safeJson() = this

private fun EventEntity.toJson() = buildJsonObject {
    put("id", id); put("owner_id", ownerId); put("name", name); put("description", description); put("venue", venue)
    put("starts_at", time(startsAt)); put("ends_at", time(endsAt)); put("status", status); put("archived", archived); put("updated_at", time(updatedAt))
}
private fun ResourceEntity.toJson() = buildJsonObject {
    put("id", id); put("event_id", eventId); put("owner_id", ownerId); put("title", title); put("category", category); put("material", material)
    put("condition", condition); put("quantity", quantity); put("unit", unit); put("status", status); put("value_cents", valueCents)
    put("image_urls", syncJson.parseToJsonElement(imageUrlsJson).safeJson()); put("archived", archived); put("updated_at", time(updatedAt))
}
private fun PassportEntity.toJson() = buildJsonObject {
    put("id", id); put("resource_id", resourceId); put("qr_payload", qrPayload); put("history", syncJson.parseToJsonElement(historyJson).safeJson()); put("updated_at", time(updatedAt))
}
private fun ProgrammeEntity.toJson() = buildJsonObject {
    put("id", id); put("partner_id", partnerId); put("name", name); put("programme_type", type); put("accepted_materials", syncJson.parseToJsonElement(acceptedMaterialsJson).safeJson())
    put("location", location); put("active", active); put("updated_at", time(updatedAt))
}
private fun TransactionEntity.toJson() = buildJsonObject {
    put("id", id); put("event_id", eventId); put("resource_id", resourceId); put("sender_id", senderId); put("receiver_id", receiverId)
    partnerId?.let { put("partner_id", it) }; put("transaction_type", type); put("status", status); put("quantity", quantity); put("archived", archived); put("updated_at", time(updatedAt))
}
private fun ImpactEntity.toJson() = buildJsonObject {
    put("id", id); put("event_id", eventId); resourceId?.let { put("resource_id", it) }; transactionId?.let { put("transaction_id", it) }
    put("material_diverted_kg", materialDivertedKg); put("emissions_avoided_kg", emissionsAvoidedKg); put("value_recovered_cents", valueRecoveredCents)
    put("calculated_at", time(calculatedAt)); put("updated_at", time(updatedAt))
}
