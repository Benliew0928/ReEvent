package com.reevent.app.core.sync

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.reevent.app.core.config.AppEnvironment
import com.reevent.app.core.data.AccountScope
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.database.EventEntity
import com.reevent.app.core.database.LifecycleCommandEntity
import com.reevent.app.core.database.ReEventDatabase
import com.reevent.app.core.database.SyncOperationEntity
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.network.LifecycleCommandGateway
import com.reevent.app.core.network.LifecycleCommandPayload
import com.reevent.app.core.network.LifecycleCommandType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncCoordinatorIdentityTest {
    private lateinit var database: ReEventDatabase
    private lateinit var dao: CoreDao
    private lateinit var accountScope: AccountScope
    private lateinit var gateway: FakeSyncGateway
    private lateinit var lifecycleGateway: FakeLifecycleCommandGateway
    private lateinit var coordinator: SyncCoordinator

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ReEventDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.coreDao()
        accountScope = AccountScope()
        gateway = FakeSyncGateway()
        lifecycleGateway = FakeLifecycleCommandGateway()
        coordinator = SyncCoordinator(dao, gateway, lifecycleGateway, accountScope, AppEnvironment.LOCAL)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun wrongEnvironmentAccountOrAuthenticatedSubject_neverTouchesRemoteOrQueue() = runBlocking {
        seed(LOCAL, ACCOUNT_A)
        accountScope.activate(ACCOUNT_A)

        assertEquals(
            SyncOutcome.STALE_IDENTITY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.STAGING, ACCOUNT_A))
        )

        accountScope.activate(ACCOUNT_B)
        assertEquals(
            SyncOutcome.STALE_IDENTITY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        accountScope.activate(ACCOUNT_A)
        gateway.authenticatedAccountId = ACCOUNT_B
        assertEquals(
            SyncOutcome.STALE_IDENTITY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        gateway.authenticatedAccountId = null
        assertEquals(
            SyncOutcome.STALE_IDENTITY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        gateway.authenticatedAccountId = ACCOUNT_A
        gateway.environment = AppEnvironment.STAGING
        assertEquals(
            SyncOutcome.STALE_IDENTITY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        assertTrue(gateway.calls.isEmpty())
        assertEquals(1, dao.pendingOperations(LOCAL, ACCOUNT_A, 10).size)
    }

    @Test
    fun correctIdentity_syncsOnlyItsEnvironmentAndAccountPartition() = runBlocking {
        seed(LOCAL, ACCOUNT_A)
        seed(LOCAL, ACCOUNT_B)
        dao.upsertOutbox(outbox(STAGING, ACCOUNT_A))
        accountScope.activate(ACCOUNT_A)

        assertEquals(
            SyncOutcome.SYNCED,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        assertEquals(listOf("upsert:events:$SHARED_ID"), gateway.calls)
        assertTrue(dao.pendingOperations(LOCAL, ACCOUNT_A, 10).isEmpty())
        assertEquals(1, dao.pendingOperations(LOCAL, ACCOUNT_B, 10).size)
        assertEquals(1, dao.pendingOperations(STAGING, ACCOUNT_A, 10).size)
        assertEquals("SYNCED", dao.event(ACCOUNT_A, SHARED_ID)?.syncState)
        assertEquals("PENDING", dao.event(ACCOUNT_B, SHARED_ID)?.syncState)
    }

    @Test
    fun cancellationLeavesTheQueuePendingAndUnmodified() = runBlocking {
        seed(LOCAL, ACCOUNT_A)
        accountScope.activate(ACCOUNT_A)
        gateway.upsertFailure = CancellationException("worker cancelled")

        var cancellationObserved = false
        try {
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        } catch (_: CancellationException) {
            cancellationObserved = true
        }

        assertTrue(cancellationObserved)
        val pending = dao.pendingOperations(LOCAL, ACCOUNT_A, 10).single()
        assertEquals(0, pending.attempts)
        assertEquals(null, pending.lastError)
        assertEquals("PENDING", dao.event(ACCOUNT_A, SHARED_ID)?.syncState)
    }

    @Test
    fun sessionChangeDuringRemoteCallLeavesTheRowForTheCorrectAccount() = runBlocking {
        seed(LOCAL, ACCOUNT_A)
        accountScope.activate(ACCOUNT_A)
        gateway.afterUpsert = accountScope::clear

        assertEquals(
            SyncOutcome.STALE_IDENTITY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        assertEquals(1, gateway.calls.size)
        assertEquals(1, dao.pendingOperations(LOCAL, ACCOUNT_A, 10).size)
        assertEquals("PENDING", dao.event(ACCOUNT_A, SHARED_ID)?.syncState)
    }

    @Test
    fun lifecycleRetry_reusesItsDurableIdempotencyKeyAndCachesTheServerProjection() = runBlocking {
        val command = lifecycleCommand(LOCAL, ACCOUNT_A)
        dao.insertLifecycleCommand(command)
        accountScope.activate(ACCOUNT_A)
        lifecycleGateway.failure = RuntimeException("network unavailable")

        assertEquals(
            SyncOutcome.RETRY,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )
        val failed = dao.pendingLifecycleCommands(LOCAL, ACCOUNT_A, 10).single()
        assertEquals(1, failed.attempts)
        assertEquals(command.idempotencyKey, lifecycleGateway.keys.single())

        lifecycleGateway.failure = null
        assertEquals(
            SyncOutcome.SYNCED,
            coordinator.syncPending(SyncWorkIdentity(AppEnvironment.LOCAL, ACCOUNT_A))
        )

        assertEquals(listOf(command.idempotencyKey, command.idempotencyKey), lifecycleGateway.keys)
        assertTrue(dao.pendingLifecycleCommands(LOCAL, ACCOUNT_A, 10).isEmpty())
        assertEquals(TransactionStatus.APPROVED.name, dao.transaction(ACCOUNT_A, TRANSACTION_ID)?.status)
    }

    private suspend fun seed(environment: String, accountId: String) {
        dao.upsertEvent(event(accountId))
        dao.upsertOutbox(outbox(environment, accountId))
    }

    private fun event(accountId: String) = EventEntity(
        id = SHARED_ID,
        accountId = accountId,
        ownerId = accountId,
        name = "Event $accountId",
        description = "description",
        venue = "venue",
        startsAt = 1L,
        endsAt = 2L,
        status = "ACTIVE",
        createdAt = 1L,
        updatedAt = 2L,
        syncState = "PENDING",
        archived = false
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
        idempotencyKey = IDEMPOTENCY_KEY,
        environment = environment,
        accountId = accountId,
        dedupeKey = "dedupe",
        commandType = LifecycleCommandType.APPROVE.name,
        payloadJson = Json.encodeToString(LifecycleCommandPayload(transactionId = TRANSACTION_ID)),
        createdAt = 1L,
        updatedAt = 1L
    )

    private class FakeSyncGateway : SyncGateway {
        override var environment: AppEnvironment = AppEnvironment.LOCAL
        var authenticatedAccountId: String? = ACCOUNT_A
        var upsertFailure: Throwable? = null
        var afterUpsert: () -> Unit = {}
        val calls = mutableListOf<String>()

        override fun isConfigured(): Boolean = true
        override suspend fun authenticatedAccountIdOrNull(): String? = authenticatedAccountId

        override suspend fun upsert(table: String, payload: JsonObject) {
            upsertFailure?.let { throw it }
            calls += "upsert:$table:${payload["id"]?.toString()?.trim('"')}"
            afterUpsert()
        }

        override suspend fun archive(table: String, recordId: String) {
            calls += "archive:$table:$recordId"
        }
    }

    private class FakeLifecycleCommandGateway : LifecycleCommandGateway {
        var failure: Throwable? = null
        val keys = mutableListOf<String>()

        override fun isConfigured(): Boolean = true
        override suspend fun resolvePublishedListingId(resourceId: String): String = "listing-id"

        override suspend fun execute(
            type: LifecycleCommandType,
            payload: LifecycleCommandPayload,
            idempotencyKey: String
        ): CircularTransaction {
            keys += idempotencyKey
            failure?.let { throw it }
            return CircularTransaction(
                id = checkNotNull(payload.transactionId),
                eventId = "event-id",
                resourceId = "resource-id",
                senderId = "sender-id",
                receiverId = "receiver-id",
                partnerId = null,
                type = TransactionType.BUY,
                status = TransactionStatus.APPROVED,
                quantity = 1.0,
                createdAt = 1L,
                updatedAt = 2L,
                requesterId = ACCOUNT_A
            )
        }
    }

    private companion object {
        const val LOCAL = "local"
        const val STAGING = "staging"
        const val ACCOUNT_A = "account-a"
        const val ACCOUNT_B = "account-b"
        const val SHARED_ID = "shared-id"
        const val TRANSACTION_ID = "transaction-id"
        const val IDEMPOTENCY_KEY = "11111111-1111-1111-1111-111111111111"
    }
}
