package com.reevent.app.core.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import com.reevent.app.core.config.AppEnvironment
import com.reevent.app.core.data.AccountScope
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.EventEntity
import com.reevent.app.core.database.ProgrammeEntity
import com.reevent.app.core.database.ResourceEntity
import com.reevent.app.core.database.toEntity
import com.reevent.app.core.network.LifecycleCommandGateway
import com.reevent.app.core.network.LifecycleCommandPayload
import com.reevent.app.core.network.LifecycleCommandType
import com.reevent.app.core.network.isTerminalLifecycleFailure
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.decodeFromString
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val environment: AppEnvironment
) : AccountSyncScheduler {
    override fun requestSync(accountId: String) {
        enqueue(accountId, ExistingWorkPolicy.KEEP)
    }

    /** User initiated retry replaces a delayed WorkManager backoff with a fresh attempt. */
    override fun retryNow(accountId: String) {
        enqueue(accountId, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueue(accountId: String, policy: ExistingWorkPolicy) {
        val identity = SyncWorkIdentity(environment, accountId)
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(identity.toInputData())
            .build()
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(SyncWorkIdentity.LEGACY_WORK_NAME)
            enqueueUniqueWork(identity.uniqueWorkName, policy, request)
        }
    }

    override suspend fun cancelSync(accountId: String) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(SyncWorkIdentity(environment, accountId).uniqueWorkName).await()
        workManager.cancelUniqueWork(SyncWorkIdentity.LEGACY_WORK_NAME).await()
    }
}

interface AccountSyncScheduler {
    fun requestSync(accountId: String)
    fun retryNow(accountId: String)
    suspend fun cancelSync(accountId: String)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SyncWorkerEntryPoint {
    fun coordinator(): SyncCoordinator
}

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val identity = SyncWorkIdentity.from(inputData) ?: return Result.success()
        val coordinator = EntryPointAccessors.fromApplication(applicationContext, SyncWorkerEntryPoint::class.java).coordinator()
        return when (coordinator.syncPending(identity)) {
            SyncOutcome.SYNCED, SyncOutcome.NOT_CONFIGURED, SyncOutcome.STALE_IDENTITY -> Result.success()
            SyncOutcome.RETRY -> Result.retry()
        }
    }
}

enum class SyncOutcome { SYNCED, NOT_CONFIGURED, STALE_IDENTITY, RETRY }

@Singleton
class SyncCoordinator @Inject constructor(
    private val dao: CoreDao,
    private val gateway: SyncGateway,
    private val lifecycleGateway: LifecycleCommandGateway,
    private val accountScope: AccountScope,
    private val environment: AppEnvironment
) {
    private val syncMutex = Mutex()

    suspend fun syncPending(identity: SyncWorkIdentity): SyncOutcome = syncMutex.withLock {
        if (!gateway.isConfigured()) return SyncOutcome.NOT_CONFIGURED
        if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
        syncLifecycleCommands(identity)?.let { return it }
        val operations = dao.pendingOperations(
            environment = identity.environment.wireValue,
            accountId = identity.accountId,
            limit = 50
        )
        for (operation in operations) {
            if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
            try {
                if (operation.operation == "archive") gateway.archive(operation.tableName, operation.recordId)
                else {
                    val payload = payloadFor(operation.tableName, operation.accountId, operation.recordId)
                    gateway.upsert(operation.tableName, checkNotNull(payload) { "The queued record is no longer cached" })
                }
                if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
                dao.deleteOutbox(operation.environment, operation.accountId, operation.id)
                setRecordSyncState(operation.tableName, operation.accountId, operation.recordId, "SYNCED")
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
                dao.markOutboxFailed(
                    operation.environment,
                    operation.accountId,
                    operation.id,
                    error.message ?: "Remote sync failed",
                    System.currentTimeMillis()
                )
                setRecordSyncState(operation.tableName, operation.accountId, operation.recordId, "FAILED")
                return SyncOutcome.RETRY
            }
        }
        return SyncOutcome.SYNCED
    }

    /** Replays only typed RPC intents with their original server idempotency UUID. */
    private suspend fun syncLifecycleCommands(identity: SyncWorkIdentity): SyncOutcome? {
        val commands = dao.pendingLifecycleCommands(
            environment = identity.environment.wireValue,
            accountId = identity.accountId,
            limit = 50
        )
        for (command in commands) {
            if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
            try {
                val transaction = lifecycleGateway.execute(
                    LifecycleCommandType.valueOf(command.commandType),
                    lifecycleJson.decodeFromString<LifecycleCommandPayload>(command.payloadJson),
                    command.idempotencyKey
                )
                if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
                dao.completeLifecycleCommand(
                    transaction.toEntity(command.accountId),
                    command.environment,
                    command.accountId,
                    command.idempotencyKey
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                if (!matchesCurrentIdentity(identity)) return SyncOutcome.STALE_IDENTITY
                if (error.isTerminalLifecycleFailure()) {
                    dao.deleteLifecycleCommand(command.environment, command.accountId, command.idempotencyKey)
                    continue
                }
                dao.markLifecycleCommandFailed(
                    command.environment,
                    command.accountId,
                    command.idempotencyKey,
                    error.message ?: "Lifecycle command failed",
                    System.currentTimeMillis()
                )
                return SyncOutcome.RETRY
            }
        }
        return null
    }

    private suspend fun matchesCurrentIdentity(identity: SyncWorkIdentity): Boolean = try {
        identity.environment == environment &&
            gateway.environment == environment &&
            accountScope.accountId.value == identity.accountId &&
            gateway.authenticatedAccountIdOrNull() == identity.accountId
    } catch (error: CancellationException) {
        throw error
    } catch (_: Throwable) {
        false
    }

    private suspend fun payloadFor(table: String, accountId: String, id: String) = when (table) {
        "events" -> dao.event(accountId, id)?.toJson()
        "resource_items" -> dao.resource(accountId, id)?.toJson()
        "circular_programmes" -> dao.programme(accountId, id)?.toJson()
        else -> null
    }

    private suspend fun setRecordSyncState(table: String, accountId: String, id: String, state: String) = when (table) {
        "events" -> dao.setEventSyncState(accountId, id, state)
        "resource_items" -> dao.setResourceSyncState(accountId, id, state)
        "circular_programmes" -> dao.setProgrammeSyncState(accountId, id, state)
        else -> Unit
    }
}

private val syncJson = Json { ignoreUnknownKeys = true }
private val lifecycleJson = Json { ignoreUnknownKeys = false }
private fun time(value: Long) = Instant.ofEpochMilli(value).toString()
private fun JsonElement.safeJson() = this

private fun EventEntity.toJson() = buildJsonObject {
    put("id", id); put("owner_id", ownerId); put("name", name); put("description", description); put("address_text", venue)
    put("starts_at", time(startsAt)); put("ends_at", time(endsAt)); put("updated_at", time(updatedAt))
}
private fun ResourceEntity.toJson() = buildJsonObject {
    put("id", id); put("origin_event_id", eventId); put("created_by", ownerId); put("current_owner_id", ownerId)
    put("title", title); put("description", ""); put("category", category); put("material", material)
    put("condition", condition); put("quantity", quantity); put("unit", unit.uppercase()); put("status", status)
    put("updated_at", time(updatedAt))
}
private fun ProgrammeEntity.toJson() = buildJsonObject {
    put("id", id); put("partner_id", partnerId); put("name", name); put("programme_type", type); put("accepted_materials", syncJson.parseToJsonElement(acceptedMaterialsJson).safeJson())
    put("address_text", location); put("active", active); put("updated_at", time(updatedAt))
}
