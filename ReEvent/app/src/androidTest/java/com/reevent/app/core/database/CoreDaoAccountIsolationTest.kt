package com.reevent.app.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreDaoAccountIsolationTest {
    private lateinit var database: ReEventDatabase
    private lateinit var dao: CoreDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ReEventDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.coreDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun sharedRecordIds_canCoexistWithoutOverwriteAcrossEveryCachedProjection() = runBlocking {
        dao.upsertEvent(event(ACCOUNT_A, "Event A"))
        dao.upsertEvent(event(ACCOUNT_B, "Event B"))
        dao.upsertResource(resource(ACCOUNT_A, "Resource A"))
        dao.upsertResource(resource(ACCOUNT_B, "Resource B"))
        dao.upsertPassport(passport(ACCOUNT_A, "payload-a"))
        dao.upsertPassport(passport(ACCOUNT_B, "payload-b"))
        dao.upsertProgramme(programme(ACCOUNT_A, "Programme A"))
        dao.upsertProgramme(programme(ACCOUNT_B, "Programme B"))
        dao.upsertTransaction(transaction(ACCOUNT_A, quantity = 1.0))
        dao.upsertTransaction(transaction(ACCOUNT_B, quantity = 2.0))
        dao.upsertImpact(impact(ACCOUNT_A, divertedKg = 1.0))
        dao.upsertImpact(impact(ACCOUNT_B, divertedKg = 2.0))

        dao.upsertEvent(event(ACCOUNT_A, "Event A updated"))

        assertEquals("Event A updated", dao.event(ACCOUNT_A, SHARED_ID)?.name)
        assertEquals("Event B", dao.event(ACCOUNT_B, SHARED_ID)?.name)
        assertEquals("Resource A", dao.resource(ACCOUNT_A, SHARED_ID)?.title)
        assertEquals("Resource B", dao.resource(ACCOUNT_B, SHARED_ID)?.title)
        assertEquals("payload-a", dao.passport(ACCOUNT_A, SHARED_ID)?.qrPayload)
        assertEquals("payload-b", dao.passport(ACCOUNT_B, SHARED_ID)?.qrPayload)
        assertEquals("Programme A", dao.programme(ACCOUNT_A, SHARED_ID)?.name)
        assertEquals("Programme B", dao.programme(ACCOUNT_B, SHARED_ID)?.name)
        assertEquals(1.0, dao.transaction(ACCOUNT_A, SHARED_ID)?.quantity)
        assertEquals(2.0, dao.transaction(ACCOUNT_B, SHARED_ID)?.quantity)
        assertEquals(1.0, dao.impact(ACCOUNT_A, SHARED_ID)?.materialDivertedKg ?: -1.0, 0.0)
        assertEquals(2.0, dao.impact(ACCOUNT_B, SHARED_ID)?.materialDivertedKg ?: -1.0, 0.0)
        assertNull(dao.event("unknown-account", SHARED_ID))
    }

    @Test
    fun archiveMutations_changeOnlyMutableEventAndResourceProjections() = runBlocking {
        listOf(ACCOUNT_A, ACCOUNT_B).forEach { accountId ->
            dao.upsertEvent(event(accountId, "Event $accountId"))
            dao.upsertResource(resource(accountId, "Resource $accountId"))
        }

        dao.archiveEvent(ACCOUNT_A, SHARED_ID, updatedAt = 20L)
        dao.archiveResource(ACCOUNT_A, SHARED_ID, updatedAt = 20L)

        assertTrue(dao.event(ACCOUNT_A, SHARED_ID)!!.archived)
        assertTrue(dao.resource(ACCOUNT_A, SHARED_ID)!!.archived)
        assertFalse(dao.event(ACCOUNT_B, SHARED_ID)!!.archived)
        assertFalse(dao.resource(ACCOUNT_B, SHARED_ID)!!.archived)
    }

    @Test
    fun equivalentOutboxOperations_areUniqueAndMutablePerEnvironmentAndAccount() = runBlocking {
        dao.upsertOutbox(outbox(LOCAL, ACCOUNT_A))
        dao.upsertOutbox(outbox(LOCAL, ACCOUNT_B))
        dao.upsertOutbox(outbox(STAGING, ACCOUNT_A))
        var operations = dao.pendingOperations(LOCAL, ACCOUNT_A, limit = 10)
        assertEquals(1, operations.size)

        val accountAOperation = operations.single()
        val accountBOperation = dao.pendingOperations(LOCAL, ACCOUNT_B, limit = 10).single()
        val stagingOperation = dao.pendingOperations(STAGING, ACCOUNT_A, limit = 10).single()
        dao.markOutboxFailed(LOCAL, ACCOUNT_A, accountAOperation.id, "failed-a", updatedAt = 30L)
        dao.markOutboxFailed(LOCAL, ACCOUNT_A, accountBOperation.id, "must-not-change-b", updatedAt = 30L)
        dao.markOutboxFailed(LOCAL, ACCOUNT_A, stagingOperation.id, "must-not-change-staging", updatedAt = 30L)

        operations = dao.pendingOperations(LOCAL, ACCOUNT_A, limit = 10)
        assertEquals(1, operations.single().attempts)
        assertEquals(0, dao.pendingOperations(LOCAL, ACCOUNT_B, limit = 10).single().attempts)
        assertEquals(0, dao.pendingOperations(STAGING, ACCOUNT_A, limit = 10).single().attempts)

        dao.deleteOutbox(LOCAL, ACCOUNT_A, accountBOperation.id)
        assertEquals(1, dao.pendingOperations(LOCAL, ACCOUNT_B, limit = 10).size)
        dao.deleteOutbox(LOCAL, ACCOUNT_A, accountAOperation.id)
        assertTrue(dao.pendingOperations(LOCAL, ACCOUNT_A, limit = 10).isEmpty())
        assertEquals(1, dao.pendingOperations(STAGING, ACCOUNT_A, limit = 10).size)
    }

    @Test
    fun legacyProgrammeDrafts_areIsolatedByAccountAndNeverShareDeletion() = runBlocking {
        dao.upsertLegacyProgrammeDraft(legacyDraft(ACCOUNT_A, "Draft A"))
        dao.upsertLegacyProgrammeDraft(legacyDraft(ACCOUNT_B, "Draft B"))

        assertEquals("Draft A", dao.observeLegacyProgrammeDrafts(ACCOUNT_A, "partner").first().single().name)
        assertEquals("Draft B", dao.observeLegacyProgrammeDrafts(ACCOUNT_B, "partner").first().single().name)

        dao.deleteLegacyProgrammeDraft(ACCOUNT_A, SHARED_ID)
        assertTrue(dao.observeLegacyProgrammeDrafts(ACCOUNT_A, "partner").first().isEmpty())
        assertEquals("Draft B", dao.observeLegacyProgrammeDrafts(ACCOUNT_B, "partner").first().single().name)
    }

    private fun event(accountId: String, name: String) = EventEntity(
        id = SHARED_ID,
        accountId = accountId,
        ownerId = "owner",
        name = name,
        description = "description",
        venue = "venue",
        startsAt = 1L,
        endsAt = 2L,
        status = "ACTIVE",
        createdAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED",
        archived = false
    )

    private fun resource(accountId: String, title: String) = ResourceEntity(
        id = SHARED_ID,
        accountId = accountId,
        eventId = "event",
        ownerId = "owner",
        title = title,
        category = "DECOR",
        material = "PLASTIC",
        condition = "GOOD",
        quantity = 1.0,
        unit = "ITEM",
        status = "ACTIVE",
        valueCents = 0L,
        imageUrlsJson = "[]",
        createdAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED",
        archived = false
    )

    private fun passport(accountId: String, payload: String) = PassportEntity(
        id = SHARED_ID,
        accountId = accountId,
        resourceId = SHARED_RESOURCE_ID,
        qrPayload = payload,
        historyJson = "[]",
        createdAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED"
    )

    private fun programme(accountId: String, name: String) = ProgrammeEntity(
        id = SHARED_ID,
        accountId = accountId,
        partnerId = "partner",
        name = name,
        type = "RECYCLE",
        acceptedMaterialsJson = "[]",
        location = "location",
        active = true,
        createdAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED"
    )

    private fun transaction(accountId: String, quantity: Double) = TransactionEntity(
        id = SHARED_ID,
        accountId = accountId,
        eventId = "event",
        resourceId = "resource",
        senderId = "sender",
        receiverId = "receiver",
        partnerId = null,
        requesterId = "sender",
        counterResourceId = null,
        type = "RECYCLE",
        status = "COMPLETED",
        quantity = quantity,
        createdAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED",
        archived = false
    )

    private fun impact(accountId: String, divertedKg: Double) = ImpactEntity(
        id = SHARED_ID,
        accountId = accountId,
        eventId = "event",
        resourceId = "resource",
        transactionId = "transaction",
        transactionType = "RECYCLE",
        completedQuantity = 1.0,
        unit = "KG",
        materialDivertedKg = divertedKg,
        emissionsAvoidedKg = divertedKg,
        recoinsTransferred = 0L,
        recoinsRewarded = 0L,
        calculatedAt = 1L,
        syncState = "SYNCED"
    )

    private fun outbox(environment: String, accountId: String) = SyncOperationEntity(
        environment = environment,
        accountId = accountId,
        tableName = "events",
        recordId = SHARED_ID,
        operation = "upsert",
        payload = "{}",
        updatedAt = 1L
    )

    private fun legacyDraft(accountId: String, name: String) = LegacyProgrammeDraftEntity(
        id = SHARED_ID,
        accountId = accountId,
        partnerId = "partner",
        name = name,
        type = "REPAIR",
        acceptedMaterialsJson = "[]",
        location = "old location",
        createdAt = 1L,
        updatedAt = 1L,
    )

    private companion object {
        const val ACCOUNT_A = "account-a"
        const val ACCOUNT_B = "account-b"
        const val LOCAL = "local"
        const val STAGING = "staging"
        const val SHARED_ID = "shared-id"
        const val SHARED_RESOURCE_ID = "shared-resource-id"
    }
}
