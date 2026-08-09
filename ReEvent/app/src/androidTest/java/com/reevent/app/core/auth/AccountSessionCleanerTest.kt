package com.reevent.app.core.auth

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reevent.app.core.config.AppEnvironment
import com.reevent.app.core.data.AccountScope
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.EventEntity
import com.reevent.app.core.database.ImpactEntity
import com.reevent.app.core.database.LifecycleCommandEntity
import com.reevent.app.core.database.PassportEntity
import com.reevent.app.core.database.ProgrammeEntity
import com.reevent.app.core.database.ReEventDatabase
import com.reevent.app.core.database.ResourceEntity
import com.reevent.app.core.database.SyncOperationEntity
import com.reevent.app.core.database.TransactionEntity
import com.reevent.app.core.database.UserEntity
import com.reevent.app.core.model.UserRole
import com.reevent.app.core.sync.AccountSyncScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountSessionCleanerTest {
    private lateinit var database: ReEventDatabase
    private lateinit var dao: CoreDao
    private lateinit var preferences: AppPreferences
    private lateinit var accountScope: AccountScope
    private lateinit var scheduler: FakeAccountSyncScheduler
    private lateinit var cleaner: AccountSessionCleaner

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, ReEventDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.coreDao()
        preferences = AppPreferences(context)
        preferences.clearAccount()
        accountScope = AccountScope()
        scheduler = FakeAccountSyncScheduler()
        cleaner = AccountSessionCleaner(
            dao,
            preferences,
            accountScope,
            scheduler,
            AppEnvironment.LOCAL
        )
    }

    @After
    fun tearDown() = runBlocking {
        preferences.clearAccount()
        database.close()
    }

    @Test
    fun clearCancelsAndPurgesOnlyTheActiveEnvironmentAccount() = runBlocking {
        seedAccount(ACCOUNT_A)
        seedAccount(ACCOUNT_B)
        dao.upsertOutbox(outbox(STAGING, ACCOUNT_A))
        dao.insertLifecycleCommand(lifecycleCommand(STAGING, ACCOUNT_A))
        preferences.cacheAccount(ACCOUNT_A, UserRole.ORGANIZER)
        preferences.saveResourceDraft(ACCOUNT_A, EVENT_ID, "draft-a")
        preferences.saveResourceDraft(ACCOUNT_B, EVENT_ID, "draft-b")
        accountScope.activate(ACCOUNT_A)

        cleaner.clear(ACCOUNT_A)

        assertEquals(listOf(ACCOUNT_A), scheduler.cancelledAccounts)
        assertNull(accountScope.accountId.value)
        assertNull(dao.user(ACCOUNT_A))
        assertNotNull(dao.user(ACCOUNT_B))
        assertAccountRowsRemoved(ACCOUNT_A)
        assertAccountRowsPresent(ACCOUNT_B)
        assertEquals(0, dao.pendingOperations(LOCAL, ACCOUNT_A, 10).size)
        assertEquals(1, dao.pendingOperations(LOCAL, ACCOUNT_B, 10).size)
        assertEquals(1, dao.pendingOperations(STAGING, ACCOUNT_A, 10).size)
        assertEquals(0, dao.pendingLifecycleCommands(LOCAL, ACCOUNT_A, 10).size)
        assertEquals(1, dao.pendingLifecycleCommands(LOCAL, ACCOUNT_B, 10).size)
        assertEquals(1, dao.pendingLifecycleCommands(STAGING, ACCOUNT_A, 10).size)
        assertNull(preferences.cachedUserId.first())
        assertNull(preferences.resourceDraft(ACCOUNT_A, EVENT_ID).first())
        assertEquals("draft-b", preferences.resourceDraft(ACCOUNT_B, EVENT_ID).first())
    }

    private suspend fun seedAccount(accountId: String) {
        dao.upsertUser(
            UserEntity(accountId, "$accountId@example.com", accountId, "ORGANIZER", null, 1L, 2L)
        )
        dao.upsertEvent(event(accountId))
        dao.upsertResource(resource(accountId))
        dao.upsertPassport(passport(accountId))
        dao.upsertProgramme(programme(accountId))
        dao.upsertTransaction(transaction(accountId))
        dao.upsertImpact(impact(accountId))
        dao.upsertOutbox(outbox(LOCAL, accountId))
        dao.insertLifecycleCommand(lifecycleCommand(LOCAL, accountId))
    }

    private suspend fun assertAccountRowsRemoved(accountId: String) {
        assertNull(dao.event(accountId, SHARED_ID))
        assertNull(dao.resource(accountId, SHARED_ID))
        assertNull(dao.passport(accountId, SHARED_ID))
        assertNull(dao.programme(accountId, SHARED_ID))
        assertNull(dao.transaction(accountId, SHARED_ID))
        assertNull(dao.impact(accountId, SHARED_ID))
    }

    private suspend fun assertAccountRowsPresent(accountId: String) {
        assertNotNull(dao.event(accountId, SHARED_ID))
        assertNotNull(dao.resource(accountId, SHARED_ID))
        assertNotNull(dao.passport(accountId, SHARED_ID))
        assertNotNull(dao.programme(accountId, SHARED_ID))
        assertNotNull(dao.transaction(accountId, SHARED_ID))
        assertNotNull(dao.impact(accountId, SHARED_ID))
    }

    private fun event(accountId: String) = EventEntity(
        SHARED_ID, accountId, accountId, "Event", "description", "venue",
        1L, 2L, "ACTIVE", 1L, 2L, "PENDING", false
    )

    private fun resource(accountId: String) = ResourceEntity(
        SHARED_ID, accountId, EVENT_ID, accountId, "Resource", "DECOR", "PLASTIC", "GOOD",
        1.0, "ITEM", "ACTIVE", 0L, "[]", 1L, 2L, "PENDING", false
    )

    private fun passport(accountId: String) = PassportEntity(
        SHARED_ID, accountId, RESOURCE_ID, "payload", "[]", 1L, 2L, "PENDING"
    )

    private fun programme(accountId: String) = ProgrammeEntity(
        SHARED_ID, accountId, "partner", "Programme", "RECYCLE", "[]", "location",
        true, 1L, 2L, "PENDING"
    )

    private fun transaction(accountId: String) = TransactionEntity(
        id = SHARED_ID,
        accountId = accountId,
        eventId = EVENT_ID,
        resourceId = RESOURCE_ID,
        senderId = accountId,
        receiverId = "receiver",
        partnerId = null,
        requesterId = accountId,
        counterResourceId = null,
        type = "RECYCLE",
        status = "REQUESTED",
        quantity = 1.0,
        createdAt = 1L,
        updatedAt = 2L,
        syncState = "PENDING",
        archived = false
    )

    private fun impact(accountId: String) = ImpactEntity(
        id = SHARED_ID,
        accountId = accountId,
        eventId = EVENT_ID,
        resourceId = RESOURCE_ID,
        transactionId = SHARED_ID,
        transactionType = "RECYCLE",
        completedQuantity = 1.0,
        unit = "KG",
        materialDivertedKg = 1.0,
        emissionsAvoidedKg = 1.0,
        recoinsTransferred = 0L,
        recoinsRewarded = 0L,
        calculatedAt = 1L,
        syncState = "PENDING"
    )

    private fun outbox(environment: String, accountId: String) = SyncOperationEntity(
        environment = environment,
        accountId = accountId,
        tableName = "events",
        recordId = SHARED_ID,
        operation = "upsert",
        payload = "{}",
        updatedAt = 2L
    )

    private fun lifecycleCommand(environment: String, accountId: String) = LifecycleCommandEntity(
        idempotencyKey = "$environment-$accountId-command",
        environment = environment,
        accountId = accountId,
        dedupeKey = "approve-transaction",
        commandType = "APPROVE",
        payloadJson = "{}",
        createdAt = 1L,
        updatedAt = 2L
    )

    private class FakeAccountSyncScheduler : AccountSyncScheduler {
        val cancelledAccounts = mutableListOf<String>()
        override fun requestSync(accountId: String) = Unit
        override suspend fun cancelSync(accountId: String) {
            cancelledAccounts += accountId
        }
    }

    private companion object {
        const val LOCAL = "local"
        const val STAGING = "staging"
        const val ACCOUNT_A = "account-a"
        const val ACCOUNT_B = "account-b"
        const val SHARED_ID = "shared-id"
        const val EVENT_ID = "event-id"
        const val RESOURCE_ID = "resource-id"
    }
}
