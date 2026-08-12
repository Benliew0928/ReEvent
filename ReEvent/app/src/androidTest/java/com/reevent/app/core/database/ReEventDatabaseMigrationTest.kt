package com.reevent.app.core.database

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReEventDatabaseMigrationTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ReEventDatabase::class.java
    )

    @After
    fun cleanUp() {
        context.deleteDatabase(V1_DATABASE)
        context.deleteDatabase(V2_DATABASE)
        context.deleteDatabase(V3_DATABASE)
        context.deleteDatabase(V4_DATABASE)
        context.deleteDatabase(V5_DATABASE)
    }

    @Test
    fun migrate2To5_preservesEventsAndPurgesPreReleaseLifecycleProjections() {
        helper.createDatabase(V2_DATABASE, 2).use { database ->
            attributedAndBlankV2Rows.forEach(database::execSQL)
        }

        helper.runMigrationsAndValidate(
            V2_DATABASE,
            5,
            true,
            ReEventDatabase.MIGRATION_2_3,
            ReEventDatabase.MIGRATION_3_4,
            ReEventDatabase.MIGRATION_4_5
        ).use { database ->
            assertEquals(1L, database.count("events"))
            assertEquals(ACCOUNT_A, database.singleString("SELECT accountId FROM events"))
            SERVER_PROJECTION_TABLES.forEach { table ->
                assertEquals("Expected pre-release rows to be purged from $table", 0L, database.count(table))
            }
            assertEquals(1L, database.count("sync_outbox"))
            assertEquals(ACCOUNT_A, database.singleString("SELECT accountId FROM sync_outbox"))
            assertEquals(LOCAL, database.singleString("SELECT environment FROM sync_outbox"))
            assertEquals(
                listOf("Event kept", "42", "FAILED", "1"),
                database.row("SELECT name, updatedAt, syncState, archived FROM events WHERE id = 'keep-event'")
            )
        }
    }

    @Test
    fun migrate1To5_discardsRowsWhoseAccountCannotBeEstablished() {
        createVersion1Database().use { database ->
            database.execSQL(
                "INSERT INTO users VALUES ('user-a', 'a@example.com', 'User A', 'ORGANISER', NULL, 1, 2)"
            )
            database.execSQL(
                "INSERT INTO events VALUES ('legacy-event', 'owner', 'Legacy', 'description', 'venue', 1, 2, 'ACTIVE', 1, 2, 'PENDING', 0)"
            )
            database.execSQL(
                "INSERT INTO resource_items VALUES ('legacy-resource', 'legacy-event', 'owner', 'Legacy resource', 'DECOR', 'PLASTIC', 'GOOD', 1, 'ITEM', 'AVAILABLE', 0, '[]', 1, 2, 'PENDING', 0)"
            )
            database.execSQL(
                "INSERT INTO sync_outbox (tableName, recordId, operation, payload, attempts, lastError, updatedAt) VALUES ('events', 'legacy-event', 'upsert', '{}', 0, NULL, 2)"
            )
        }

        helper.runMigrationsAndValidate(
            V1_DATABASE,
            5,
            true,
            ReEventDatabase.MIGRATION_1_2,
            ReEventDatabase.MIGRATION_2_3,
            ReEventDatabase.MIGRATION_3_4,
            ReEventDatabase.MIGRATION_4_5
        ).use { database ->
            assertEquals(1L, database.count("users"))
            ACCOUNT_SCOPED_TABLES.forEach { table ->
                assertEquals("Expected ambiguous rows to be removed from $table", 0L, database.count(table))
            }
            assertEquals(0L, database.count("sync_outbox"))
        }
    }

    @Test
    fun migrate3To5_preservesAllowedOutboxFieldsAndAssignsLocalEnvironment() {
        helper.createDatabase(V3_DATABASE, 3).use { database ->
            database.execSQL(
                """
                INSERT INTO sync_outbox (
                    tableName, accountId, recordId, operation, payload,
                    attempts, lastError, updatedAt
                ) VALUES (
                    'events', '$ACCOUNT_A', 'event-a', 'upsert', '{"id":"event-a"}',
                    3, 'previous failure', 42
                )
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(
            V3_DATABASE,
            5,
            true,
            ReEventDatabase.MIGRATION_3_4,
            ReEventDatabase.MIGRATION_4_5
        ).use { database ->
            assertEquals(
                listOf(LOCAL, ACCOUNT_A, "events", "event-a", "upsert", "3", "previous failure", "42"),
                database.row(
                    "SELECT environment, accountId, tableName, recordId, operation, attempts, lastError, updatedAt " +
                        "FROM sync_outbox"
                )
            )
        }
    }

    @Test
    fun migrate4To5_removesGenericLifecycleWritesAndAdoptsServerProjectionTypes() {
        helper.createDatabase(V4_DATABASE, 4).use { database ->
            database.execSQL(
                "INSERT INTO resource_items VALUES ('resource', '$ACCOUNT_A', 'event', 'owner', " +
                    "'Resource', 'DECOR', 'PLASTIC', 'GOOD', 2, 'ITEM', 'ACTIVE', 0, '[]', 1, 2, 'PENDING', 0)"
            )
            database.execSQL(
                "INSERT INTO resource_passports VALUES " +
                    "('passport', '$ACCOUNT_A', 'resource', 'payload', '[]', 1, 2, 'SYNCED')"
            )
            database.execSQL(
                "INSERT INTO circular_programmes VALUES " +
                    "('programme', '$ACCOUNT_A', 'partner', 'Programme', 'RECYCLE', '[]', 'location', 1, 1, 2, 'SYNCED')"
            )
            database.execSQL(
                "INSERT INTO circular_transactions VALUES " +
                    "('transaction', '$ACCOUNT_A', 'event', 'resource', 'sender', 'receiver', NULL, " +
                    "'RECYCLE', 'COMPLETED', 2, 1, 2, 'SYNCED', 0)"
            )
            database.execSQL(
                "INSERT INTO impact_records VALUES " +
                    "('impact', '$ACCOUNT_A', 'event', 'resource', 'transaction', 2.0, 3.0, 0, 1, 2, 'SYNCED')"
            )
            database.execSQL(
                "INSERT INTO sync_outbox " +
                    "(environment, tableName, accountId, recordId, operation, payload, attempts, lastError, updatedAt) " +
                    "VALUES ('$LOCAL', 'events', '$ACCOUNT_A', 'event', 'upsert', '{}', 0, NULL, 2)"
            )
            listOf(
                "resource_items" to "resource",
                "resource_passports" to "passport",
                "circular_programmes" to "programme",
                "circular_transactions" to "transaction",
                "impact_records" to "impact"
            ).forEach { (table, id) ->
                database.execSQL(
                    "INSERT INTO sync_outbox " +
                        "(environment, tableName, accountId, recordId, operation, payload, attempts, lastError, updatedAt) " +
                        "VALUES ('$LOCAL', '$table', '$ACCOUNT_A', '$id', 'upsert', '{}', 0, NULL, 2)"
                )
            }
        }

        helper.runMigrationsAndValidate(
            V4_DATABASE,
            5,
            true,
            ReEventDatabase.MIGRATION_4_5
        ).use { database ->
            SERVER_PROJECTION_TABLES.forEach { table ->
                assertEquals("Expected $table to be repopulated only by the server", 0L, database.count(table))
            }
            assertEquals(1L, database.count("sync_outbox"))
            assertEquals("events", database.singleString("SELECT tableName FROM sync_outbox"))
            assertEquals(
                "REAL",
                database.singleString("SELECT type FROM pragma_table_info('resource_items') WHERE name = 'quantity'")
            )
            assertEquals(
                "REAL",
                database.singleString("SELECT type FROM pragma_table_info('circular_transactions') WHERE name = 'quantity'")
            )
            assertEquals(
                "requesterId",
                database.singleString(
                    "SELECT name FROM pragma_table_info('circular_transactions') WHERE name = 'requesterId'"
                )
            )
            assertEquals(
                "recoinsTransferred",
                database.singleString(
                    "SELECT name FROM pragma_table_info('impact_records') WHERE name = 'recoinsTransferred'"
                )
            )
        }
    }

    @Test
    fun migrate5To6_preservesResourcesAndLifecycleCommandsWhileAddingMarketplaceProjection() {
        helper.createDatabase(V5_DATABASE, 5).use { database ->
            database.execSQL(
                """
                INSERT INTO resource_items (
                    id, accountId, eventId, ownerId, title, category, material, condition,
                    quantity, unit, status, valueCents, imageUrlsJson, createdAt, updatedAt,
                    syncState, archived
                ) VALUES (
                    'resource-v5', '$ACCOUNT_A', 'event-v5', '$ACCOUNT_A', 'Saved resource',
                    'DECOR', 'WOOD', 'GOOD', 2.5, 'KG', 'ACTIVE', 25,
                    '["owner/resources/resource-v5/primary"]', 10, 42, 'SYNCED', 0
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO lifecycle_commands (
                    idempotencyKey, environment, accountId, dedupeKey, commandType,
                    payloadJson, attempts, lastError, createdAt, updatedAt
                ) VALUES (
                    'command-v5', '$LOCAL', '$ACCOUNT_A', 'dedupe-v5', 'BEGIN_RETURN',
                    '{"transactionId":"transaction-v5"}', 2, 'offline', 11, 43
                )
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(
            V5_DATABASE,
            6,
            true,
            ReEventDatabase.MIGRATION_5_6
        ).use { database ->
            assertEquals(
                listOf(
                    "Saved resource",
                    "2.5",
                    "[\"owner/resources/resource-v5/primary\"]",
                    "42",
                    null,
                    "[]",
                    null,
                    ""
                ),
                database.nullableRow(
                    "SELECT title, quantity, imageUrlsJson, updatedAt, marketplaceListingId, " +
                        "marketplaceAllowedActionsJson, marketplacePublishedQuantity, marketplaceTerms " +
                        "FROM resource_items WHERE id = 'resource-v5'"
                )
            )
            assertEquals(
                listOf("command-v5", LOCAL, ACCOUNT_A, "BEGIN_RETURN", "2", "offline", "43"),
                database.row(
                    "SELECT idempotencyKey, environment, accountId, commandType, attempts, lastError, updatedAt " +
                        "FROM lifecycle_commands WHERE idempotencyKey = 'command-v5'"
                )
            )
            assertEquals(
                "REAL",
                database.singleString(
                    "SELECT type FROM pragma_table_info('resource_items') WHERE name = 'marketplacePublishedQuantity'"
                )
            )
        }
    }

    private fun createVersion1Database(): SupportSQLiteDatabase {
        context.deleteDatabase(V1_DATABASE)
        val callback = object : SupportSQLiteOpenHelper.Callback(1) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                version1Schema.forEach(db::execSQL)
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(V1_DATABASE)
                .callback(callback)
                .build()
        ).writableDatabase
    }

    private fun SupportSQLiteDatabase.count(table: String): Long =
        query("SELECT COUNT(*) FROM $table").use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getLong(0)
        }

    private fun SupportSQLiteDatabase.singleString(query: String): String =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getString(0)
        }

    private fun SupportSQLiteDatabase.row(query: String): List<String> =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            List(cursor.columnCount) { column -> cursor.getString(column) }
        }

    private fun SupportSQLiteDatabase.nullableRow(query: String): List<String?> =
        query(query).use { cursor ->
            assertTrue(cursor.moveToFirst())
            List(cursor.columnCount) { column -> if (cursor.isNull(column)) null else cursor.getString(column) }
        }

    private companion object {
        const val ACCOUNT_A = "account-a"
        const val LOCAL = "local"
        const val V1_DATABASE = "reevent-migration-v1"
        const val V2_DATABASE = "reevent-migration-v2"
        const val V3_DATABASE = "reevent-migration-v3"
        const val V4_DATABASE = "reevent-migration-v4"
        const val V5_DATABASE = "reevent-migration-v5"

        val ACCOUNT_SCOPED_TABLES = listOf(
            "events",
            "resource_items",
            "resource_passports",
            "circular_programmes",
            "circular_transactions",
            "impact_records"
        )

        val SERVER_PROJECTION_TABLES = listOf(
            "resource_items",
            "resource_passports",
            "circular_programmes",
            "circular_transactions",
            "impact_records"
        )

        val attributedAndBlankV2Rows = listOf(
            "INSERT INTO events VALUES ('keep-event', '$ACCOUNT_A', 'owner', 'Event kept', 'description', 'venue', 1, 2, 'ACTIVE', 1, 42, 'FAILED', 1)",
            "INSERT INTO events VALUES ('drop-event', '', 'owner', 'Event dropped', 'description', 'venue', 1, 2, 'ACTIVE', 1, 2, 'PENDING', 0)",
            "INSERT INTO resource_items VALUES ('keep-resource', '$ACCOUNT_A', 'keep-event', 'owner', 'Resource kept', 'DECOR', 'PLASTIC', 'GOOD', 2, 'ITEM', 'AVAILABLE', 0, '[]', 1, 2, 'SYNCED', 0)",
            "INSERT INTO resource_items VALUES ('drop-resource', '', 'drop-event', 'owner', 'Resource dropped', 'DECOR', 'PLASTIC', 'GOOD', 1, 'ITEM', 'AVAILABLE', 0, '[]', 1, 2, 'PENDING', 0)",
            "INSERT INTO resource_passports VALUES ('keep-passport', '$ACCOUNT_A', 'keep-resource', 'payload-a', '[]', 1, 2, 'SYNCED')",
            "INSERT INTO resource_passports VALUES ('drop-passport', '', 'drop-resource', 'payload-b', '[]', 1, 2, 'PENDING')",
            "INSERT INTO circular_programmes VALUES ('keep-programme', '$ACCOUNT_A', 'partner', 'Programme kept', 'RECYCLE', '[]', 'location', 1, 1, 2, 'SYNCED')",
            "INSERT INTO circular_programmes VALUES ('drop-programme', '', 'partner', 'Programme dropped', 'RECYCLE', '[]', 'location', 1, 1, 2, 'PENDING')",
            "INSERT INTO circular_transactions VALUES ('keep-transaction', '$ACCOUNT_A', 'keep-event', 'keep-resource', 'sender', 'receiver', NULL, 'RECYCLE', 'COMPLETED', 1, 1, 2, 'SYNCED', 0)",
            "INSERT INTO circular_transactions VALUES ('drop-transaction', '', 'drop-event', 'drop-resource', 'sender', 'receiver', NULL, 'RECYCLE', 'REQUESTED', 1, 1, 2, 'PENDING', 0)",
            "INSERT INTO impact_records VALUES ('keep-impact', '$ACCOUNT_A', 'keep-event', 'keep-resource', 'keep-transaction', 1.0, 1.5, 0, 1, 2, 'SYNCED')",
            "INSERT INTO impact_records VALUES ('drop-impact', '', 'drop-event', 'drop-resource', 'drop-transaction', 1.0, 1.5, 0, 1, 2, 'PENDING')",
            "INSERT INTO sync_outbox (tableName, accountId, recordId, operation, payload, attempts, lastError, updatedAt) VALUES ('events', '$ACCOUNT_A', 'keep-event', 'upsert', '{}', 0, NULL, 2)",
            "INSERT INTO sync_outbox (tableName, accountId, recordId, operation, payload, attempts, lastError, updatedAt) VALUES ('events', '', 'drop-event', 'upsert', '{}', 0, NULL, 2)"
        )

        val version1Schema = listOf(
            "CREATE TABLE users (id TEXT NOT NULL, email TEXT NOT NULL, displayName TEXT NOT NULL, role TEXT, avatarUrl TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, PRIMARY KEY(id))",
            "CREATE TABLE events (id TEXT NOT NULL, ownerId TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, venue TEXT NOT NULL, startsAt INTEGER NOT NULL, endsAt INTEGER NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL, archived INTEGER NOT NULL, PRIMARY KEY(id))",
            "CREATE INDEX index_events_ownerId ON events(ownerId)",
            "CREATE TABLE resource_items (id TEXT NOT NULL, eventId TEXT NOT NULL, ownerId TEXT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL, material TEXT NOT NULL, condition TEXT NOT NULL, quantity INTEGER NOT NULL, unit TEXT NOT NULL, status TEXT NOT NULL, valueCents INTEGER NOT NULL, imageUrlsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL, archived INTEGER NOT NULL, PRIMARY KEY(id))",
            "CREATE INDEX index_resource_items_eventId ON resource_items(eventId)",
            "CREATE INDEX index_resource_items_ownerId ON resource_items(ownerId)",
            "CREATE INDEX index_resource_items_status ON resource_items(status)",
            "CREATE TABLE resource_passports (id TEXT NOT NULL, resourceId TEXT NOT NULL, qrPayload TEXT NOT NULL, historyJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL, PRIMARY KEY(id))",
            "CREATE UNIQUE INDEX index_resource_passports_resourceId ON resource_passports(resourceId)",
            "CREATE TABLE circular_programmes (id TEXT NOT NULL, partnerId TEXT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, acceptedMaterialsJson TEXT NOT NULL, location TEXT NOT NULL, active INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL, PRIMARY KEY(id))",
            "CREATE INDEX index_circular_programmes_partnerId ON circular_programmes(partnerId)",
            "CREATE TABLE circular_transactions (id TEXT NOT NULL, eventId TEXT NOT NULL, resourceId TEXT NOT NULL, senderId TEXT NOT NULL, receiverId TEXT NOT NULL, partnerId TEXT, type TEXT NOT NULL, status TEXT NOT NULL, quantity INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL, archived INTEGER NOT NULL, PRIMARY KEY(id))",
            "CREATE INDEX index_circular_transactions_eventId ON circular_transactions(eventId)",
            "CREATE INDEX index_circular_transactions_resourceId ON circular_transactions(resourceId)",
            "CREATE INDEX index_circular_transactions_senderId ON circular_transactions(senderId)",
            "CREATE INDEX index_circular_transactions_receiverId ON circular_transactions(receiverId)",
            "CREATE INDEX index_circular_transactions_partnerId ON circular_transactions(partnerId)",
            "CREATE TABLE impact_records (id TEXT NOT NULL, eventId TEXT NOT NULL, resourceId TEXT, transactionId TEXT, materialDivertedKg REAL NOT NULL, emissionsAvoidedKg REAL NOT NULL, valueRecoveredCents INTEGER NOT NULL, calculatedAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL, PRIMARY KEY(id))",
            "CREATE INDEX index_impact_records_eventId ON impact_records(eventId)",
            "CREATE INDEX index_impact_records_resourceId ON impact_records(resourceId)",
            "CREATE INDEX index_impact_records_transactionId ON impact_records(transactionId)",
            "CREATE TABLE sync_outbox (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, tableName TEXT NOT NULL, recordId TEXT NOT NULL, operation TEXT NOT NULL, payload TEXT NOT NULL, attempts INTEGER NOT NULL, lastError TEXT, updatedAt INTEGER NOT NULL)",
            "CREATE UNIQUE INDEX index_sync_outbox_tableName_recordId ON sync_outbox(tableName, recordId)"
        )
    }
}
