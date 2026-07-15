package com.reevent.app.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CoreDao {
    @Upsert suspend fun upsertUser(user: UserEntity)
    @Query("SELECT * FROM users WHERE id = :id") fun observeUser(id: String): Flow<UserEntity?>
    @Query("SELECT * FROM users WHERE id = :id") suspend fun user(id: String): UserEntity?

    @Upsert suspend fun upsertEvent(event: EventEntity)
    @Query("SELECT * FROM events WHERE accountId = :accountId AND ownerId = :ownerId AND archived = 0 ORDER BY startsAt DESC") fun observeEvents(accountId: String, ownerId: String): Flow<List<EventEntity>>
    @Query("SELECT * FROM events WHERE accountId = :accountId AND id = :id") fun observeEvent(accountId: String, id: String): Flow<EventEntity?>
    @Query("SELECT * FROM events WHERE accountId = :accountId AND id = :id") suspend fun event(accountId: String, id: String): EventEntity?
    @Query("UPDATE events SET archived = 1, syncState = 'PENDING', updatedAt = :updatedAt WHERE id = :id") suspend fun archiveEvent(id: String, updatedAt: Long)
    @Query("UPDATE events SET syncState = :state WHERE accountId = :accountId AND id = :id") suspend fun setEventSyncState(accountId: String, id: String, state: String)

    @Upsert suspend fun upsertResource(resource: ResourceEntity)
    @Query("SELECT * FROM resource_items WHERE accountId = :accountId AND eventId = :eventId AND archived = 0 ORDER BY updatedAt DESC") fun observeResources(accountId: String, eventId: String): Flow<List<ResourceEntity>>
    @Query("SELECT * FROM resource_items WHERE accountId = :accountId AND status = 'AVAILABLE' AND archived = 0 ORDER BY updatedAt DESC") fun observeMarketplace(accountId: String): Flow<List<ResourceEntity>>
    @Query("SELECT * FROM resource_items WHERE accountId = :accountId AND id = :id") fun observeResource(accountId: String, id: String): Flow<ResourceEntity?>
    @Query("SELECT * FROM resource_items WHERE accountId = :accountId AND id = :id") suspend fun resource(accountId: String, id: String): ResourceEntity?
    @Query("UPDATE resource_items SET archived = 1, syncState = 'PENDING', updatedAt = :updatedAt WHERE id = :id") suspend fun archiveResource(id: String, updatedAt: Long)
    @Query("UPDATE resource_items SET syncState = :state WHERE accountId = :accountId AND id = :id") suspend fun setResourceSyncState(accountId: String, id: String, state: String)

    @Upsert suspend fun upsertPassport(passport: PassportEntity)
    @Query("SELECT * FROM resource_passports WHERE accountId = :accountId AND resourceId = :resourceId LIMIT 1") fun observePassport(accountId: String, resourceId: String): Flow<PassportEntity?>
    @Query("SELECT * FROM resource_passports WHERE accountId = :accountId AND id = :id") suspend fun passport(accountId: String, id: String): PassportEntity?
    @Query("UPDATE resource_passports SET syncState = :state WHERE accountId = :accountId AND id = :id") suspend fun setPassportSyncState(accountId: String, id: String, state: String)

    @Upsert suspend fun upsertProgramme(programme: ProgrammeEntity)
    @Query("SELECT * FROM circular_programmes WHERE accountId = :accountId AND active = 1 ORDER BY name") fun observeProgrammes(accountId: String): Flow<List<ProgrammeEntity>>
    @Query("SELECT * FROM circular_programmes WHERE accountId = :accountId AND partnerId = :partnerId AND active = 1 ORDER BY name") fun observePartnerProgrammes(accountId: String, partnerId: String): Flow<List<ProgrammeEntity>>
    @Query("SELECT * FROM circular_programmes WHERE accountId = :accountId AND id = :id") suspend fun programme(accountId: String, id: String): ProgrammeEntity?
    @Query("UPDATE circular_programmes SET syncState = :state WHERE accountId = :accountId AND id = :id") suspend fun setProgrammeSyncState(accountId: String, id: String, state: String)

    @Upsert suspend fun upsertTransaction(transaction: TransactionEntity)
    @Query("SELECT * FROM circular_transactions WHERE accountId = :accountId AND (senderId = :userId OR receiverId = :userId OR partnerId = :userId) AND archived = 0 ORDER BY updatedAt DESC") fun observeTransactions(accountId: String, userId: String): Flow<List<TransactionEntity>>
    @Query("UPDATE circular_transactions SET archived = 1, syncState = 'PENDING', updatedAt = :updatedAt WHERE id = :id") suspend fun archiveTransaction(id: String, updatedAt: Long)
    @Query("SELECT * FROM circular_transactions WHERE accountId = :accountId AND id = :id") suspend fun transaction(accountId: String, id: String): TransactionEntity?
    @Query("UPDATE circular_transactions SET syncState = :state WHERE accountId = :accountId AND id = :id") suspend fun setTransactionSyncState(accountId: String, id: String, state: String)

    @Upsert suspend fun upsertImpact(impact: ImpactEntity)
    @Query("SELECT * FROM impact_records WHERE accountId = :accountId AND eventId = :eventId ORDER BY calculatedAt DESC") fun observeImpact(accountId: String, eventId: String): Flow<List<ImpactEntity>>
    @Query("SELECT * FROM impact_records WHERE accountId = :accountId AND id = :id") suspend fun impact(accountId: String, id: String): ImpactEntity?
    @Query("UPDATE impact_records SET syncState = :state WHERE accountId = :accountId AND id = :id") suspend fun setImpactSyncState(accountId: String, id: String, state: String)

    @Upsert suspend fun upsertOutbox(operation: SyncOperationEntity)
    @Query("SELECT * FROM sync_outbox ORDER BY updatedAt LIMIT :limit") suspend fun pendingOperations(limit: Int): List<SyncOperationEntity>
    @Query("DELETE FROM sync_outbox WHERE id = :id") suspend fun deleteOutbox(id: Long)
    @Query("UPDATE sync_outbox SET attempts = attempts + 1, lastError = :error, updatedAt = :updatedAt WHERE id = :id") suspend fun markOutboxFailed(id: Long, error: String, updatedAt: Long)
    @Query("DELETE FROM users WHERE id != :userId") suspend fun deleteOtherUsers(userId: String)
    @Query("DELETE FROM users") suspend fun clearUsers()
    @Query("DELETE FROM events WHERE accountId != :accountId") suspend fun clearOtherEvents(accountId: String)
    @Query("DELETE FROM resource_items WHERE accountId != :accountId") suspend fun clearOtherResources(accountId: String)
    @Query("DELETE FROM resource_passports WHERE accountId != :accountId") suspend fun clearOtherPassports(accountId: String)
    @Query("DELETE FROM circular_programmes WHERE accountId != :accountId") suspend fun clearOtherProgrammes(accountId: String)
    @Query("DELETE FROM circular_transactions WHERE accountId != :accountId") suspend fun clearOtherTransactions(accountId: String)
    @Query("DELETE FROM impact_records WHERE accountId != :accountId") suspend fun clearOtherImpact(accountId: String)
    @Query("DELETE FROM sync_outbox WHERE accountId != :accountId") suspend fun clearOtherOutbox(accountId: String)
    @Query("DELETE FROM events") suspend fun clearEvents()
    @Query("DELETE FROM resource_items") suspend fun clearResources()
    @Query("DELETE FROM resource_passports") suspend fun clearPassports()
    @Query("DELETE FROM circular_programmes") suspend fun clearProgrammes()
    @Query("DELETE FROM circular_transactions") suspend fun clearTransactions()
    @Query("DELETE FROM impact_records") suspend fun clearImpact()
    @Query("DELETE FROM sync_outbox") suspend fun clearOutbox()
}
