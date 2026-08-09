package com.reevent.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        EventEntity::class,
        ResourceEntity::class,
        PassportEntity::class,
        ProgrammeEntity::class,
        TransactionEntity::class,
        ImpactEntity::class,
        SyncOperationEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class ReEventDatabase : RoomDatabase() {
    abstract fun coreDao(): CoreDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf("events", "resource_items", "resource_passports", "circular_programmes", "circular_transactions", "impact_records").forEach {
                    db.execSQL("ALTER TABLE $it ADD COLUMN accountId TEXT NOT NULL DEFAULT ''")
                }
                db.execSQL("ALTER TABLE sync_outbox ADD COLUMN accountId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                rebuildAccountScopedTable(
                    db = db,
                    table = "events",
                    columns = "id, accountId, ownerId, name, description, venue, startsAt, endsAt, status, createdAt, updatedAt, syncState, archived",
                    definition = """
                        id TEXT NOT NULL, accountId TEXT NOT NULL, ownerId TEXT NOT NULL,
                        name TEXT NOT NULL, description TEXT NOT NULL, venue TEXT NOT NULL,
                        startsAt INTEGER NOT NULL, endsAt INTEGER NOT NULL, status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, archived INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    """,
                    indices = listOf(
                        "CREATE INDEX index_events_accountId_ownerId ON events(accountId, ownerId)"
                    )
                )
                rebuildAccountScopedTable(
                    db = db,
                    table = "resource_items",
                    columns = "id, accountId, eventId, ownerId, title, category, material, condition, quantity, unit, status, valueCents, imageUrlsJson, createdAt, updatedAt, syncState, archived",
                    definition = """
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        ownerId TEXT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL,
                        material TEXT NOT NULL, condition TEXT NOT NULL, quantity INTEGER NOT NULL,
                        unit TEXT NOT NULL, status TEXT NOT NULL, valueCents INTEGER NOT NULL,
                        imageUrlsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, archived INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    """,
                    indices = listOf(
                        "CREATE INDEX index_resource_items_accountId_eventId ON resource_items(accountId, eventId)",
                        "CREATE INDEX index_resource_items_accountId_ownerId ON resource_items(accountId, ownerId)",
                        "CREATE INDEX index_resource_items_accountId_status ON resource_items(accountId, status)"
                    )
                )
                rebuildAccountScopedTable(
                    db = db,
                    table = "resource_passports",
                    columns = "id, accountId, resourceId, qrPayload, historyJson, createdAt, updatedAt, syncState",
                    definition = """
                        id TEXT NOT NULL, accountId TEXT NOT NULL, resourceId TEXT NOT NULL,
                        qrPayload TEXT NOT NULL, historyJson TEXT NOT NULL, createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL,
                        PRIMARY KEY(accountId, id)
                    """,
                    indices = listOf(
                        "CREATE UNIQUE INDEX index_resource_passports_accountId_resourceId ON resource_passports(accountId, resourceId)"
                    )
                )
                rebuildAccountScopedTable(
                    db = db,
                    table = "circular_programmes",
                    columns = "id, accountId, partnerId, name, type, acceptedMaterialsJson, location, active, createdAt, updatedAt, syncState",
                    definition = """
                        id TEXT NOT NULL, accountId TEXT NOT NULL, partnerId TEXT NOT NULL,
                        name TEXT NOT NULL, type TEXT NOT NULL, acceptedMaterialsJson TEXT NOT NULL,
                        location TEXT NOT NULL, active INTEGER NOT NULL, createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL,
                        PRIMARY KEY(accountId, id)
                    """,
                    indices = listOf(
                        "CREATE INDEX index_circular_programmes_accountId_partnerId ON circular_programmes(accountId, partnerId)"
                    )
                )
                rebuildAccountScopedTable(
                    db = db,
                    table = "circular_transactions",
                    columns = "id, accountId, eventId, resourceId, senderId, receiverId, partnerId, type, status, quantity, createdAt, updatedAt, syncState, archived",
                    definition = """
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        resourceId TEXT NOT NULL, senderId TEXT NOT NULL, receiverId TEXT NOT NULL,
                        partnerId TEXT, type TEXT NOT NULL, status TEXT NOT NULL, quantity INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, archived INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    """,
                    indices = listOf(
                        "CREATE INDEX index_circular_transactions_accountId_eventId ON circular_transactions(accountId, eventId)",
                        "CREATE INDEX index_circular_transactions_accountId_resourceId ON circular_transactions(accountId, resourceId)",
                        "CREATE INDEX index_circular_transactions_accountId_senderId ON circular_transactions(accountId, senderId)",
                        "CREATE INDEX index_circular_transactions_accountId_receiverId ON circular_transactions(accountId, receiverId)",
                        "CREATE INDEX index_circular_transactions_accountId_partnerId ON circular_transactions(accountId, partnerId)"
                    )
                )
                rebuildAccountScopedTable(
                    db = db,
                    table = "impact_records",
                    columns = "id, accountId, eventId, resourceId, transactionId, materialDivertedKg, emissionsAvoidedKg, valueRecoveredCents, calculatedAt, updatedAt, syncState",
                    definition = """
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        resourceId TEXT, transactionId TEXT, materialDivertedKg REAL NOT NULL,
                        emissionsAvoidedKg REAL NOT NULL, valueRecoveredCents INTEGER NOT NULL,
                        calculatedAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncState TEXT NOT NULL,
                        PRIMARY KEY(accountId, id)
                    """,
                    indices = listOf(
                        "CREATE INDEX index_impact_records_accountId_eventId ON impact_records(accountId, eventId)",
                        "CREATE INDEX index_impact_records_accountId_resourceId ON impact_records(accountId, resourceId)",
                        "CREATE INDEX index_impact_records_accountId_transactionId ON impact_records(accountId, transactionId)"
                    )
                )

                db.execSQL("DELETE FROM sync_outbox WHERE TRIM(accountId) = ''")
                db.execSQL("DROP INDEX IF EXISTS index_sync_outbox_tableName_recordId")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_sync_outbox_accountId_tableName_recordId " +
                        "ON sync_outbox(accountId, tableName, recordId)"
                )
            }
        }

        private fun rebuildAccountScopedTable(
            db: SupportSQLiteDatabase,
            table: String,
            columns: String,
            definition: String,
            indices: List<String>
        ) {
            val replacement = "${table}_account_scoped"
            db.execSQL("CREATE TABLE $replacement (${definition.trimIndent()})")
            db.execSQL(
                "INSERT INTO $replacement ($columns) SELECT $columns FROM $table " +
                    "WHERE TRIM(accountId) <> ''"
            )
            db.execSQL("DROP TABLE $table")
            db.execSQL("ALTER TABLE $replacement RENAME TO $table")
            indices.forEach(db::execSQL)
        }
    }
}
