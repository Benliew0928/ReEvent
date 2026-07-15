package com.reevent.app.core.data

import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.EventEntity
import com.reevent.app.core.database.ImpactEntity
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
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.sync.SyncScheduler
import com.reevent.app.core.network.SupabaseCoreGateway
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room is the source of truth for all feature reads. Writes are indexed by owner/event in Room,
 * queued once per record, and scheduled for remote synchronisation without blocking the UI.
 */
@Singleton
class LocalFirstCoreRepository @Inject constructor(
    private val dao: CoreDao,
    private val syncScheduler: SyncScheduler,
    private val accountScope: AccountScope,
    private val remote: SupabaseCoreGateway
) : EventRepository, ResourceRepository, PassportRepository, PartnerRepository, TransactionRepository, ImpactRepository, CoreSyncRepository {

    override fun observeOwnedEvents(ownerId: String): Flow<List<Event>> = dao.observeEvents(accountScope.requireId(), ownerId).map(List<EventEntity>::toEvents)
    override fun observeEvent(eventId: String): Flow<Event?> = dao.observeEvent(accountScope.requireId(), eventId).map { it?.toDomain() }

    override suspend fun saveEvent(event: Event): AppResult<Event> = persist(event, "events", event.id) {
        dao.upsertEvent(event.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountScope.requireId()))
    }

    override suspend fun archiveEvent(eventId: String): AppResult<Unit> = persistUnit("events", eventId, "archive") {
        dao.archiveEvent(eventId, System.currentTimeMillis())
    }

    override fun observeEventResources(eventId: String): Flow<List<ResourceItem>> = dao.observeResources(accountScope.requireId(), eventId).map(List<ResourceEntity>::toResources)
    override fun observeMarketplace(): Flow<List<ResourceItem>> = dao.observeMarketplace(accountScope.requireId()).map(List<ResourceEntity>::toResources)
    override fun observeResource(resourceId: String): Flow<ResourceItem?> = dao.observeResource(accountScope.requireId(), resourceId).map { it?.toDomain() }

    override suspend fun saveResource(resource: ResourceItem): AppResult<ResourceItem> = persist(resource, "resource_items", resource.id) {
        dao.upsertResource(resource.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountScope.requireId()))
    }

    override suspend fun archiveResource(resourceId: String): AppResult<Unit> = persistUnit("resource_items", resourceId, "archive") {
        dao.archiveResource(resourceId, System.currentTimeMillis())
    }

    override fun observePassport(resourceId: String): Flow<ResourcePassport?> = dao.observePassport(accountScope.requireId(), resourceId).map { it?.toDomain() }
    override suspend fun savePassport(passport: ResourcePassport): AppResult<ResourcePassport> = persist(passport, "resource_passports", passport.id) {
        dao.upsertPassport(passport.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountScope.requireId()))
    }

    override fun observeProgrammes(partnerId: String?): Flow<List<CircularProgramme>> =
        (partnerId?.let { dao.observePartnerProgrammes(accountScope.requireId(), it) } ?: dao.observeProgrammes(accountScope.requireId())).map(List<ProgrammeEntity>::toProgrammes)

    override suspend fun saveProgramme(programme: CircularProgramme): AppResult<CircularProgramme> = persist(programme, "circular_programmes", programme.id) {
        dao.upsertProgramme(programme.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountScope.requireId()))
    }

    override fun observeTransactions(userId: String): Flow<List<CircularTransaction>> = dao.observeTransactions(accountScope.requireId(), userId).map(List<TransactionEntity>::toTransactions)
    override suspend fun saveTransaction(transaction: CircularTransaction): AppResult<CircularTransaction> = persist(transaction, "circular_transactions", transaction.id) {
        dao.upsertTransaction(transaction.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountScope.requireId()))
    }

    override suspend fun archiveTransaction(transactionId: String): AppResult<Unit> = persistUnit("circular_transactions", transactionId, "archive") {
        dao.archiveTransaction(transactionId, System.currentTimeMillis())
    }

    override fun observeImpact(eventId: String): Flow<List<ImpactRecord>> = dao.observeImpact(accountScope.requireId(), eventId).map(List<ImpactEntity>::toImpact)
    override suspend fun saveImpact(record: ImpactRecord): AppResult<ImpactRecord> = persist(record, "impact_records", record.id) {
        dao.upsertImpact(record.copy(syncState = com.reevent.app.core.model.SyncState.PENDING).toEntity(accountScope.requireId()))
    }

    override suspend fun refreshAuthorisedData(): AppResult<Unit> = try {
        val accountId = accountScope.requireId()
        val snapshot = remote.fetchAuthorisedSnapshot()
        snapshot.events.forEach { dao.upsertEvent(it.toEntity(accountId)) }
        snapshot.resources.forEach { dao.upsertResource(it.toEntity(accountId)) }
        snapshot.passports.forEach { dao.upsertPassport(it.toEntity(accountId)) }
        snapshot.programmes.forEach { dao.upsertProgramme(it.toEntity(accountId)) }
        snapshot.transactions.forEach { dao.upsertTransaction(it.toEntity(accountId)) }
        snapshot.impact.forEach { dao.upsertImpact(it.toEntity(accountId)) }
        AppResult.Success(Unit)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.SERVER, error)
    }

    private suspend fun <T> persist(value: T, table: String, id: String, action: suspend () -> Unit): AppResult<T> = try {
        action()
        enqueue(table, id, "upsert")
        AppResult.Success(value)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.UNKNOWN, error)
    }

    private suspend fun persistUnit(table: String, id: String, operation: String, action: suspend () -> Unit): AppResult<Unit> = try {
        action()
        enqueue(table, id, operation)
        AppResult.Success(Unit)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.UNKNOWN, error)
    }

    private suspend fun enqueue(table: String, recordId: String, operation: String) {
        dao.upsertOutbox(
            SyncOperationEntity(
                tableName = table,
                accountId = accountScope.requireId(),
                recordId = recordId,
                operation = operation,
                payload = "{\"id\":\"$recordId\"}",
                updatedAt = System.currentTimeMillis()
            )
        )
        syncScheduler.requestSync()
    }
}

private fun List<EventEntity>.toEvents() = map(EventEntity::toDomain)
private fun List<ResourceEntity>.toResources() = map(ResourceEntity::toDomain)
private fun List<ProgrammeEntity>.toProgrammes() = map(ProgrammeEntity::toDomain)
private fun List<TransactionEntity>.toTransactions() = map(TransactionEntity::toDomain)
private fun List<ImpactEntity>.toImpact() = map(ImpactEntity::toDomain)
