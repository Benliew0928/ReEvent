package com.reevent.app.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoreDaoEventTransactionTest {
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
    fun eventTransactions_are_scoped_to_the_active_account_and_requested_event() = runBlocking {
        dao.upsertTransaction(transaction(id = "included", accountId = "account-a", eventId = "event-a"))
        dao.upsertTransaction(transaction(id = "other-event", accountId = "account-a", eventId = "event-b"))
        dao.upsertTransaction(transaction(id = "other-account", accountId = "account-b", eventId = "event-a"))
        dao.upsertTransaction(transaction(id = "archived", accountId = "account-a", eventId = "event-a", archived = true))

        val visible = dao.observeEventTransactions("account-a", "event-a").first()

        assertEquals(listOf("included"), visible.map(TransactionEntity::id))
    }

    private fun transaction(
        id: String,
        accountId: String,
        eventId: String,
        archived: Boolean = false
    ) = TransactionEntity(
        id = id,
        accountId = accountId,
        eventId = eventId,
        resourceId = "resource-id",
        senderId = "sender-id",
        receiverId = "receiver-id",
        partnerId = null,
        type = "RECYCLE",
        status = "COMPLETED",
        quantity = 1,
        createdAt = 1L,
        updatedAt = 1L,
        syncState = "SYNCED",
        archived = archived
    )
}
