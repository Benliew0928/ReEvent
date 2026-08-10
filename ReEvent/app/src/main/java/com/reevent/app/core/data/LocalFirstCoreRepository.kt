package com.reevent.app.core.data

import android.util.Log
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.config.AppEnvironment
import com.reevent.app.core.database.EventEntity
import com.reevent.app.core.database.ImpactEntity
import com.reevent.app.core.database.LifecycleCommandEntity
import com.reevent.app.core.database.PassportEntity
import com.reevent.app.core.database.ProgrammeEntity
import com.reevent.app.core.database.ResourceEntity
import com.reevent.app.core.database.SyncOperationEntity
import com.reevent.app.core.database.TransactionEntity
import com.reevent.app.core.database.toDomain
import com.reevent.app.core.database.toEntity
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.AllocationSide
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.sync.AccountSyncScheduler
import com.reevent.app.core.network.LifecycleCommandGateway
import com.reevent.app.core.network.LifecycleCommandPayload
import com.reevent.app.core.network.LifecycleCommandType
import com.reevent.app.core.network.SupabaseCoreGateway
import com.reevent.app.core.network.SupabaseMarketplaceListingGateway
import com.reevent.app.core.network.isTerminalLifecycleFailure
import com.reevent.app.core.network.lifecycleFailureReason
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room is the source of truth for all feature reads. Writes are indexed by owner/event in Room,
 * queued once per record, and scheduled for remote synchronisation without blocking the UI.
 */
@Singleton
class LocalFirstCoreRepository @Inject constructor(
    private val dao: CoreDao,
    private val syncScheduler: AccountSyncScheduler,
    private val accountScope: AccountScope,
    private val environment: AppEnvironment,
    private val remote: SupabaseCoreGateway,
    private val lifecycle: LifecycleCommandGateway,
    private val marketplaceListings: SupabaseMarketplaceListingGateway
) : EventRepository, ResourceRepository, MarketplaceListingRepository, PassportRepository, PartnerRepository, TransactionRepository, ImpactRepository, CoreSyncRepository {
    private val refreshMutex = Mutex()
    private val lifecycleMutex = Mutex()

    override fun observeOwnedEvents(ownerId: String): Flow<List<Event>> = dao.observeEvents(accountScope.requireId(), ownerId).map(List<EventEntity>::toEvents)
    override fun observeEvent(eventId: String): Flow<Event?> = dao.observeEvent(accountScope.requireId(), eventId).map { it?.toDomain() }

    override suspend fun saveEvent(event: Event): AppResult<Event> = persist(event, "events", event.id) { accountId ->
        dao.upsertEvent(event.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountId))
    }

    override suspend fun archiveEvent(eventId: String): AppResult<Unit> = persistUnit("events", eventId, "archive") { accountId ->
        dao.archiveEvent(accountId, eventId, System.currentTimeMillis())
    }

    override fun observeEventResources(eventId: String): Flow<List<ResourceItem>> = dao.observeResources(accountScope.requireId(), eventId).map(List<ResourceEntity>::toResources)
    override fun observeOwnedResources(ownerId: String): Flow<List<ResourceItem>> = dao.observeOwnedResources(accountScope.requireId(), ownerId).map(List<ResourceEntity>::toResources)
    override fun observeMarketplace(): Flow<List<ResourceItem>> = dao.observeMarketplace(accountScope.requireId()).map(List<ResourceEntity>::toResources)
    override fun observeResource(resourceId: String): Flow<ResourceItem?> = dao.observeResource(accountScope.requireId(), resourceId).map { it?.toDomain() }

    override suspend fun saveResource(resource: ResourceItem): AppResult<ResourceItem> = persist(resource, "resource_items", resource.id) { accountId ->
        dao.upsertResource(resource.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountId))
    }

    override suspend fun archiveResource(resourceId: String): AppResult<Unit> = persistUnit("resource_items", resourceId, "archive") { accountId ->
        dao.archiveResource(accountId, resourceId, System.currentTimeMillis())
    }

    override suspend fun publishListing(resource: ResourceItem, draft: MarketplaceListingDraft): AppResult<Unit> = try {
        val accountId = accountScope.requireId()
        if (resource.ownerId != accountId) return AppResult.Failure(FailureReason.CONFLICT)
        if (!MarketplaceListingDraftRules.validate(resource, draft).isValid) return AppResult.Failure(FailureReason.VALIDATION)
        if (!marketplaceListings.isConfigured()) return AppResult.Failure(FailureReason.CONFIGURATION)

        // Publication is an immediate protected RPC. It cannot safely be queued because an
        // interrupted response is ambiguous; the server's one-open-listing rule resolves it.
        val published = marketplaceListings.publish(resource.id, draft)
        dao.upsertResource(
            resource.copy(marketplaceListing = published, syncState = SyncState.SYNCED).toEntity(accountId)
        )
        when (val refreshed = refreshAuthorisedData()) {
            is AppResult.Success -> Unit
            is AppResult.Failure -> Log.w(TAG, "Listing published but snapshot refresh did not complete", refreshed.cause)
        }
        AppResult.Success(Unit)
    } catch (error: Throwable) {
        Log.e(TAG, "Marketplace listing publication failed", error)
        AppResult.Failure(FailureReason.SERVER, error)
    }

    override fun observePassport(resourceId: String): Flow<ResourcePassport?> = dao.observePassport(accountScope.requireId(), resourceId).map { it?.toDomain() }

    override fun observeProgrammes(partnerId: String?): Flow<List<CircularProgramme>> =
        (partnerId?.let { dao.observePartnerProgrammes(accountScope.requireId(), it) } ?: dao.observeProgrammes(accountScope.requireId())).map(List<ProgrammeEntity>::toProgrammes)

    override suspend fun saveProgramme(programme: CircularProgramme): AppResult<CircularProgramme> = persist(programme, "circular_programmes", programme.id) { accountId ->
        dao.upsertProgramme(programme.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountId))
    }

    override fun observeTransactions(userId: String): Flow<List<CircularTransaction>> = dao.observeTransactions(accountScope.requireId(), userId).map(List<TransactionEntity>::toTransactions)
    override fun observeEventTransactions(eventId: String): Flow<List<CircularTransaction>> = dao.observeEventTransactions(accountScope.requireId(), eventId).map(List<TransactionEntity>::toTransactions)
    override suspend fun requestMarketplace(
        resourceId: String,
        type: TransactionType,
        quantity: Double,
        counterResourceId: String?,
        reason: String?
    ) = executeLifecycle(
        type = LifecycleCommandType.REQUEST_MARKETPLACE,
        dedupePayload = LifecycleCommandPayload(
            resourceId = resourceId,
            transactionType = type.name,
            quantity = quantity,
            counterResourceId = counterResourceId,
            reason = reason
        )
    ) {
        LifecycleCommandPayload(
            resourceId = resourceId,
            listingId = lifecycle.resolvePublishedListingId(resourceId),
            transactionType = type.name,
            quantity = quantity,
            counterResourceId = counterResourceId,
            reason = reason
        )
    }

    override suspend fun requestProgramme(
        programmeId: String,
        resourceId: String,
        quantity: Double,
        reason: String?
    ) = executeLifecycle(
        LifecycleCommandType.REQUEST_PROGRAMME,
        LifecycleCommandPayload(
            programmeId = programmeId,
            resourceId = resourceId,
            quantity = quantity,
            reason = reason
        )
    )

    override suspend fun approve(transactionId: String) = transactionCommand(LifecycleCommandType.APPROVE, transactionId)
    override suspend fun reject(transactionId: String, reason: String) = transactionCommand(LifecycleCommandType.REJECT, transactionId, reason)
    override suspend fun cancel(transactionId: String, reason: String) = transactionCommand(LifecycleCommandType.CANCEL, transactionId, reason)
    override suspend fun beginHandover(transactionId: String, side: AllocationSide) = executeLifecycle(
        LifecycleCommandType.BEGIN_HANDOVER,
        LifecycleCommandPayload(transactionId = transactionId, resourceSide = side.name)
    )
    override suspend fun confirmReceipt(transactionId: String, side: AllocationSide) = executeLifecycle(
        LifecycleCommandType.CONFIRM_RECEIPT,
        LifecycleCommandPayload(transactionId = transactionId, resourceSide = side.name)
    )
    override suspend fun beginReturn(transactionId: String) = transactionCommand(LifecycleCommandType.BEGIN_RETURN, transactionId)
    override suspend fun confirmReturn(transactionId: String) = transactionCommand(LifecycleCommandType.CONFIRM_RETURN, transactionId)

    override fun observeImpact(eventId: String): Flow<List<ImpactRecord>> = dao.observeImpact(accountScope.requireId(), eventId).map(List<ImpactEntity>::toImpact)

    override suspend fun refreshAuthorisedData(): AppResult<Unit> = refreshMutex.withLock {
        try {
            val accountId = accountScope.requireId()
            // Retry only work already partitioned to this authenticated account and environment.
            syncScheduler.requestSync(accountId)
            val snapshot = remote.fetchAuthorisedSnapshot()
            // A sign-out or account switch can happen while a request is in flight. Never write
            // the response into a different account's cache.
            check(accountScope.accountId.value == accountId) { "The active account changed during refresh" }
            dao.applyAuthorisedSnapshot(
                accountId = accountId,
                events = snapshot.events.map { it.toEntity(accountId) },
                resources = snapshot.resources.map { it.toEntity(accountId) },
                passports = snapshot.passports.map { it.toEntity(accountId) },
                programmes = snapshot.programmes.map { it.toEntity(accountId) },
                transactions = snapshot.transactions.map { it.toEntity(accountId) },
                impact = snapshot.impact.map { it.toEntity(accountId) }
            )
            Log.i(
                TAG,
                "Authorised refresh complete: events=${snapshot.events.size}, resources=${snapshot.resources.size}, passports=${snapshot.passports.size}"
            )
            AppResult.Success(Unit)
        } catch (error: Throwable) {
            Log.e(TAG, "Authorised refresh failed", error)
            AppResult.Failure(FailureReason.SERVER, error)
        }
    }

    /**
     * The scheduler always attempts lifecycle commands before local record changes. Keep the
     * displayed order the same so a user never sees an action appear to overtake another one.
     */
    override fun observePendingSyncCommands(): Flow<List<SyncCommandStatus>> {
        val accountId = accountScope.requireId()
        return combine(
            dao.observePendingLifecycleCommands(environment.wireValue, accountId),
            dao.observePendingOperations(environment.wireValue, accountId)
        ) { lifecycleCommands, operations ->
            (lifecycleCommands.map { command ->
                val payload = runCatching {
                    lifecycleJson.decodeFromString<LifecycleCommandPayload>(command.payloadJson)
                }.getOrNull()
                SyncCommandStatus(
                    id = command.idempotencyKey,
                    queuePosition = 0,
                    title = command.toSyncCommandLabel(),
                    detail = "Authorised transaction action",
                    syncState = command.lastError?.let { SyncState.FAILED } ?: SyncState.PENDING,
                    attempts = command.attempts,
                    lastError = command.lastError,
                    transactionId = payload?.transactionId,
                    lifecycleCommandType = command.commandType
                )
            } + operations.map { operation ->
                SyncCommandStatus(
                    id = "outbox-${operation.id}",
                    queuePosition = 0,
                    title = operation.toSyncCommandLabel(),
                    detail = operation.toSyncCommandDetail(),
                    syncState = operation.lastError?.let { SyncState.FAILED } ?: SyncState.PENDING,
                    attempts = operation.attempts,
                    lastError = operation.lastError
                )
            }).mapIndexed { index, command -> command.copy(queuePosition = index + 1) }
        }
    }

    override suspend fun retryPendingSync(): AppResult<Unit> = try {
        syncScheduler.retryNow(accountScope.requireId())
        AppResult.Success(Unit)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.UNKNOWN, error)
    }

    private suspend fun <T> persist(value: T, table: String, id: String, action: suspend (String) -> Unit): AppResult<T> = try {
        val accountId = accountScope.requireId()
        action(accountId)
        enqueue(accountId, table, id, "upsert")
        AppResult.Success(value)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.UNKNOWN, error)
    }

    private suspend fun persistUnit(table: String, id: String, operation: String, action: suspend (String) -> Unit): AppResult<Unit> = try {
        val accountId = accountScope.requireId()
        action(accountId)
        enqueue(accountId, table, id, operation)
        AppResult.Success(Unit)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.UNKNOWN, error)
    }

    private suspend fun enqueue(accountId: String, table: String, recordId: String, operation: String) {
        dao.upsertOutbox(
            SyncOperationEntity(
                environment = environment.wireValue,
                tableName = table,
                accountId = accountId,
                recordId = recordId,
                operation = operation,
                payload = "{\"id\":\"$recordId\"}",
                updatedAt = System.currentTimeMillis()
            )
        )
        syncScheduler.requestSync(accountId)
    }

    private fun LifecycleCommandEntity.toSyncCommandLabel(): String = when (commandType) {
        LifecycleCommandType.REQUEST_MARKETPLACE.name -> "Send marketplace request"
        LifecycleCommandType.REQUEST_PROGRAMME.name -> "Send partner recovery request"
        LifecycleCommandType.APPROVE.name -> "Approve transaction"
        LifecycleCommandType.REJECT.name -> "Decline transaction"
        LifecycleCommandType.CANCEL.name -> "Cancel transaction"
        LifecycleCommandType.BEGIN_HANDOVER.name -> "Record handover"
        LifecycleCommandType.CONFIRM_RECEIPT.name -> "Confirm receipt"
        LifecycleCommandType.BEGIN_RETURN.name -> "Start return"
        LifecycleCommandType.CONFIRM_RETURN.name -> "Confirm return"
        else -> "Send authorised transaction action"
    }

    private fun SyncOperationEntity.toSyncCommandLabel(): String {
        val record = when (tableName) {
            "events" -> "event"
            "resource_items" -> "resource"
            "circular_programmes" -> "partner programme"
            else -> "record"
        }
        return if (operation == "archive") "Archive $record" else "Save $record"
    }

    private fun SyncOperationEntity.toSyncCommandDetail(): String = when (operation) {
        "archive" -> "The archived record will be removed from active views after sync."
        else -> "This local change is waiting for the server."
    }

    private suspend fun transactionCommand(
        type: LifecycleCommandType,
        transactionId: String,
        reason: String? = null
    ) = executeLifecycle(type, LifecycleCommandPayload(transactionId = transactionId, reason = reason))

    /**
     * Persists the exact UUID and payload before invoking a critical RPC. A transport failure,
     * process death, or lost response can therefore replay the server's idempotency record instead
     * of inventing a second request. Generic row sync never sees this queue.
     */
    private suspend fun executeLifecycle(
        type: LifecycleCommandType,
        dedupePayload: LifecycleCommandPayload,
        payloadProvider: suspend () -> LifecycleCommandPayload = { dedupePayload }
    ): AppResult<CircularTransaction> = lifecycleMutex.withLock {
        if (!lifecycle.isConfigured()) return@withLock AppResult.Failure(FailureReason.CONFIGURATION)
        val accountId = try {
            accountScope.requireId()
        } catch (error: Throwable) {
            return@withLock AppResult.Failure(FailureReason.UNAUTHENTICATED, error)
        }
        val environmentValue = environment.wireValue
        val dedupeKey = lifecycleDedupeKey(type, dedupePayload)
        var queued: LifecycleCommandEntity? = null

        return@withLock try {
            queued = dao.lifecycleCommand(environmentValue, accountId, dedupeKey)
            if (queued == null) {
                val newCommand = LifecycleCommandEntity(
                    idempotencyKey = UUID.randomUUID().toString(),
                    environment = environmentValue,
                    accountId = accountId,
                    dedupeKey = dedupeKey,
                    commandType = type.name,
                    payloadJson = lifecycleJson.encodeToString(payloadProvider()),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertLifecycleCommand(newCommand)
                queued = newCommand
            }

            syncScheduler.requestSync(accountId)
            val command = checkNotNull(queued)
            val transaction = lifecycle.execute(
                LifecycleCommandType.valueOf(command.commandType),
                lifecycleJson.decodeFromString<LifecycleCommandPayload>(command.payloadJson),
                command.idempotencyKey
            )
            check(accountScope.accountId.value == accountId) { "The active account changed during a lifecycle command" }
            dao.completeLifecycleCommand(
                transaction.toEntity(accountId),
                environmentValue,
                accountId,
                command.idempotencyKey
            )
            // Completion may also change resources, passports and impact. Refresh is best effort;
            // the authoritative transaction response is already cached above.
            runCatching { refreshAuthorisedData() }
            AppResult.Success(transaction)
        } catch (error: Throwable) {
            queued?.let { command ->
                if (accountScope.accountId.value == accountId) {
                    if (error.isTerminalLifecycleFailure()) {
                        dao.deleteLifecycleCommand(environmentValue, accountId, command.idempotencyKey)
                    } else {
                        dao.markLifecycleCommandFailed(
                            environmentValue,
                            accountId,
                            command.idempotencyKey,
                            error.message ?: "Lifecycle command failed",
                            System.currentTimeMillis()
                        )
                        syncScheduler.requestSync(accountId)
                    }
                }
            }
            AppResult.Failure(error.lifecycleFailureReason(), error)
        }
    }

    private companion object {
        const val TAG = "ReEventCoreSync"
        val lifecycleJson = Json { encodeDefaults = true; explicitNulls = true }

        fun lifecycleDedupeKey(type: LifecycleCommandType, payload: LifecycleCommandPayload): String {
            val canonical = type.name + ":" + lifecycleJson.encodeToString(payload)
            return MessageDigest.getInstance("SHA-256")
                .digest(canonical.toByteArray(Charsets.UTF_8))
                .joinToString("") { byte -> "%02x".format(byte) }
        }
    }
}

private fun List<EventEntity>.toEvents() = map(EventEntity::toDomain)
private fun List<ResourceEntity>.toResources() = map(ResourceEntity::toDomain)
private fun List<ProgrammeEntity>.toProgrammes() = map(ProgrammeEntity::toDomain)
private fun List<TransactionEntity>.toTransactions() = map(TransactionEntity::toDomain)
private fun List<ImpactEntity>.toImpact() = map(ImpactEntity::toDomain)
