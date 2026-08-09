package com.reevent.app.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
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
        dao.upsertTransaction(transaction(ACCOUNT_A, quantity = 1))
        dao.upsertTransaction(transaction(ACCOUNT_B, quantity = 2))
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
        assertEquals(1, dao.transaction(ACCOUNT_A, SHARED_ID)?.quantity)
        assertEquals(2, dao.transaction(ACCOUNT_B, SHARED_ID)?.quantity)
        assertEquals(1.0, dao.impact(ACCOUNT_A, SHARED_ID)?.materialDivertedKg ?: -1.0, 0.0)
        assertEquals(2.0, dao.impact(ACCOUNT_B, SHARED_ID)?.materialDivertedKg ?: -1.0, 0.0)
        assertNull(dao.event("unknown-account", SHARED_ID))
    }

    @Test
    fun archiveMutations_changeOnlyTheRequestedAccountProjection() = runBlocking {
        listOf(ACCOUNT_A, ACCOUNT_B).forEach { accountId ->
            dao.upsertEvent(event(accountId, "Event $accountId"))
            dao.upsertResource(resource(accountId, "Resource $accountId"))
            dao.upsertTransaction(transaction(accountId, quantity = 1))
        }

        dao.archiveEvent(ACCOUNT_A, SHARED_ID, updatedAt = 20L)
        dao.archiveResource(ACCOUNT_A, SHARED_ID, updatedAt = 20L)
        dao.archiveTransaction(ACCOUNT_A, SHARED_ID, updatedAt = 20L)

        assertTrue(dao.event(ACCOUNT_A, SHARED_ID)!!.archived)
        assertTrue(dao.resource(ACCOUNT_A, SHARED_ID)!!.archived)
        assertTrue(dao.transaction(ACCOUNT_A, SHARED_ID)!!.archived)
        assertFalse(dao.event(ACCOUNT_B, SHARED_ID)!!.archived)
        assertFalse(dao.resource(ACCOUNT_B, SHARED_ID)!!.archived)
        assertFalse(dao.transaction(ACCOUNT_B, SHARED_ID)!!.archived)
    }

    @Test
    fun equivalentOutboxOperations_areUniqueAndMutablePerAccount() = runBlocking {
        dao.upsertOutbox(outbox(ACCOUNT_A))
        dao.upsertOutbox(outbox(ACCOUNT_B))
        var operations = dao.pendingOperations(limit = 10)
        assertEquals(2, operations.size)

        val accountAOperation = operations.single { it.accountId == ACCOUNT_A }
        val accountBOperation = operations.single { it.accountId == ACCOUNT_B }
        dao.markOutboxFailed(ACCOUNT_A, accountAOperation.id, "failed-a", updatedAt = 30L)
        dao.markOutboxFailed(ACCOUNT_A, accountBOperation.id, "must-not-change-b", updatedAt = 30L)

        operations = dao.pendingOperations(limit = 10)
        assertEquals(1, operations.single { it.accountId == ACCOUNT_A }.attempts)
        assertEquals(0, operations.single { it.accountId == ACCOUNT_B }.attempts)

        dao.deleteOutbox(ACCOUNT_A, accountBOperation.id)
        assertEquals(2, dao.pendingOperations(limit = 10).size)
        dao.deleteOutbox(ACCOUNT_A, accountAOperation.id)
        assertEquals(listOf(ACCOUNT_B), dao.pendingOperations(limit = 10).map { it.accountId })
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
        quantity = 1,
        unit = "ITEM",
        status = "AVAILABLE",
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

    private fun transaction(accountId: String, quantity: Int) = TransactionEntity(
        id = SHARED_ID,
        accountId = accountId,
        eventId = "event",
        resourceId = "resource",
        senderId = "sender",
        receiverId = "receiver",
        partnerId = null,
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
        materialDivertedKg = divertedKg,
        emissionsAvoidedKg = divertedKg,
        valueRecoveredCents = 0L,
        calculatedAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED"
    )

    private fun outbox(accountId: String) = SyncOperationEntity(
        accountId = accountId,
        tableName = "events",
        recordId = SHARED_ID,
        operation = "upsert",
        payload = "{}",
        updatedAt = 1L
    )

    private companion object {
        const val ACCOUNT_A = "account-a"
        const val ACCOUNT_B = "account-b"
        const val SHARED_ID = "shared-id"
        const val SHARED_RESOURCE_ID = "shared-resource-id"
    }
}
