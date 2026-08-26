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
        SyncOperationEntity::class,
        LifecycleCommandEntity::class,
        LegacyProgrammeDraftEntity::class,
    ],
    version = 9,
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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE sync_outbox_environment_scoped (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        environment TEXT NOT NULL,
                        tableName TEXT NOT NULL,
                        accountId TEXT NOT NULL,
                        recordId TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        payload TEXT NOT NULL,
                        attempts INTEGER NOT NULL,
                        lastError TEXT,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO sync_outbox_environment_scoped (
                        id, environment, tableName, accountId, recordId, operation,
                        payload, attempts, lastError, updatedAt
                    )
                    SELECT
                        id, 'local', tableName, accountId, recordId, operation,
                        payload, attempts, lastError, updatedAt
                    FROM sync_outbox
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE sync_outbox")
                db.execSQL("ALTER TABLE sync_outbox_environment_scoped RENAME TO sync_outbox")
                db.execSQL(
                    "CREATE UNIQUE INDEX index_sync_outbox_environment_accountId_tableName_recordId " +
                        "ON sync_outbox(environment, accountId, tableName, recordId)"
                )
            }
        }

        /**
         * The release backend resets synthetic pre-release lifecycle rows in migration 0005.
         * Mirror that boundary locally, remove generic writes that the server now rejects, and
         * adopt quantity precision plus server actor fields for read-only projections.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "DELETE FROM sync_outbox WHERE tableName IN " +
                        "('resource_items', 'resource_passports', 'circular_programmes', 'circular_transactions', 'impact_records')"
                )
                db.execSQL("DELETE FROM resource_passports")
                db.execSQL("DELETE FROM circular_transactions")
                db.execSQL("DELETE FROM impact_records")
                db.execSQL("DELETE FROM circular_programmes")
                db.execSQL("DELETE FROM resource_items")

                db.execSQL(
                    """
                    CREATE TABLE resource_items_release (
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        ownerId TEXT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL,
                        material TEXT NOT NULL, condition TEXT NOT NULL, quantity REAL NOT NULL,
                        unit TEXT NOT NULL, status TEXT NOT NULL, valueCents INTEGER NOT NULL,
                        imageUrlsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, archived INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE resource_items")
                db.execSQL("ALTER TABLE resource_items_release RENAME TO resource_items")
                db.execSQL("CREATE INDEX index_resource_items_accountId_eventId ON resource_items(accountId, eventId)")
                db.execSQL("CREATE INDEX index_resource_items_accountId_ownerId ON resource_items(accountId, ownerId)")
                db.execSQL("CREATE INDEX index_resource_items_accountId_status ON resource_items(accountId, status)")

                db.execSQL(
                    """
                    CREATE TABLE circular_transactions_release (
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        resourceId TEXT NOT NULL, senderId TEXT NOT NULL, receiverId TEXT NOT NULL,
                        partnerId TEXT, requesterId TEXT NOT NULL, counterResourceId TEXT,
                        type TEXT NOT NULL, status TEXT NOT NULL, quantity REAL NOT NULL,
                        createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, archived INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE circular_transactions")
                db.execSQL("ALTER TABLE circular_transactions_release RENAME TO circular_transactions")
                db.execSQL("CREATE INDEX index_circular_transactions_accountId_eventId ON circular_transactions(accountId, eventId)")
                db.execSQL("CREATE INDEX index_circular_transactions_accountId_resourceId ON circular_transactions(accountId, resourceId)")
                db.execSQL("CREATE INDEX index_circular_transactions_accountId_senderId ON circular_transactions(accountId, senderId)")
                db.execSQL("CREATE INDEX index_circular_transactions_accountId_receiverId ON circular_transactions(accountId, receiverId)")
                db.execSQL("CREATE INDEX index_circular_transactions_accountId_partnerId ON circular_transactions(accountId, partnerId)")
                db.execSQL("CREATE INDEX index_circular_transactions_accountId_requesterId ON circular_transactions(accountId, requesterId)")

                db.execSQL(
                    """
                    CREATE TABLE impact_records_release (
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        resourceId TEXT NOT NULL, transactionId TEXT NOT NULL,
                        transactionType TEXT NOT NULL, completedQuantity REAL NOT NULL, unit TEXT NOT NULL,
                        materialDivertedKg REAL, emissionsAvoidedKg REAL,
                        recoinsTransferred INTEGER NOT NULL, recoinsRewarded INTEGER NOT NULL,
                        calculatedAt INTEGER NOT NULL, syncState TEXT NOT NULL,
                        PRIMARY KEY(accountId, id)
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE impact_records")
                db.execSQL("ALTER TABLE impact_records_release RENAME TO impact_records")
                db.execSQL("CREATE INDEX index_impact_records_accountId_eventId ON impact_records(accountId, eventId)")
                db.execSQL("CREATE INDEX index_impact_records_accountId_resourceId ON impact_records(accountId, resourceId)")
                db.execSQL("CREATE INDEX index_impact_records_accountId_transactionId ON impact_records(accountId, transactionId)")

                db.execSQL(
                    """
                    CREATE TABLE lifecycle_commands (
                        idempotencyKey TEXT NOT NULL, environment TEXT NOT NULL,
                        accountId TEXT NOT NULL, dedupeKey TEXT NOT NULL,
                        commandType TEXT NOT NULL, payloadJson TEXT NOT NULL,
                        attempts INTEGER NOT NULL, lastError TEXT,
                        createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(idempotencyKey)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX index_lifecycle_commands_environment_accountId_dedupeKey " +
                        "ON lifecycle_commands(environment, accountId, dedupeKey)"
                )
                db.execSQL(
                    "CREATE INDEX index_lifecycle_commands_environment_accountId_createdAt " +
                        "ON lifecycle_commands(environment, accountId, createdAt)"
                )
            }
        }

        /** Caches only server-published marketplace terms needed for offline-safe browse and validation. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplaceListingId TEXT")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplaceAllowedActionsJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplacePublishedQuantity REAL")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplaceBuyUnitPrice INTEGER")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplaceRentUnitPrice INTEGER")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplaceDefaultDurationDays INTEGER")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN marketplaceTerms TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Adds complete map coordinates and programme terms while retiring unsafe legacy drafts. */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE events ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN location TEXT")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE resource_items ADD COLUMN longitude REAL")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS legacy_programme_drafts (
                        id TEXT NOT NULL, accountId TEXT NOT NULL, partnerId TEXT NOT NULL,
                        name TEXT NOT NULL, type TEXT NOT NULL, acceptedMaterialsJson TEXT NOT NULL,
                        location TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_legacy_programme_drafts_accountId_partnerId " +
                        "ON legacy_programme_drafts(accountId, partnerId)",
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO legacy_programme_drafts (
                        id, accountId, partnerId, name, type, acceptedMaterialsJson,
                        location, createdAt, updatedAt
                    )
                    SELECT id, accountId, partnerId, name, type, acceptedMaterialsJson,
                        location, createdAt, updatedAt
                    FROM circular_programmes
                    WHERE syncState <> 'SYNCED'
                    """.trimIndent(),
                )
                db.execSQL("DELETE FROM sync_outbox WHERE tableName = 'circular_programmes'")
                // Synced rows are a cache and will be repopulated with the complete server contract.
                db.execSQL("DELETE FROM circular_programmes")

                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN acceptedCategoriesJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN acceptedConditionsJson TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN minimumQuantity REAL")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN maximumQuantity REAL")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN unit TEXT")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN remainingCapacity REAL")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN coinDirection TEXT NOT NULL DEFAULT 'FREE'")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN unitCoinAmount INTEGER")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN pickupAvailable INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN longitude REAL")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN processingMethod TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE circular_programmes ADD COLUMN terms TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Projects existing server reuse and lifecycle fields into the offline cache. */
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE resource_items ADD COLUMN reuseCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE circular_transactions ADD COLUMN programmeId TEXT")
                db.execSQL("ALTER TABLE circular_transactions ADD COLUMN approvedAt INTEGER")
                db.execSQL("ALTER TABLE circular_transactions ADD COLUMN inTransitAt INTEGER")
                db.execSQL("ALTER TABLE circular_transactions ADD COLUMN activeAt INTEGER")
                db.execSQL("ALTER TABLE circular_transactions ADD COLUMN returnStartedAt INTEGER")
                db.execSQL("ALTER TABLE circular_transactions ADD COLUMN completedAt INTEGER")
                db.execSQL(
                    "CREATE INDEX index_circular_transactions_accountId_programmeId " +
                        "ON circular_transactions(accountId, programmeId)",
                )
            }
        }

        /** Replaces free-text material contracts with the canonical eleven-family catalogue. */
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE resource_items_material_family (
                        id TEXT NOT NULL, accountId TEXT NOT NULL, eventId TEXT NOT NULL,
                        ownerId TEXT NOT NULL, title TEXT NOT NULL, category TEXT NOT NULL,
                        materialFamily TEXT NOT NULL, materialDetail TEXT,
                        condition TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL,
                        status TEXT NOT NULL, valueCents INTEGER NOT NULL,
                        imageUrlsJson TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, archived INTEGER NOT NULL,
                        marketplaceListingId TEXT, marketplaceAllowedActionsJson TEXT NOT NULL,
                        marketplacePublishedQuantity REAL, marketplaceBuyUnitPrice INTEGER,
                        marketplaceRentUnitPrice INTEGER, marketplaceDefaultDurationDays INTEGER,
                        marketplaceTerms TEXT NOT NULL, location TEXT, latitude REAL, longitude REAL,
                        reuseCount INTEGER NOT NULL,
                        PRIMARY KEY(accountId, id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO resource_items_material_family (
                        id, accountId, eventId, ownerId, title, category,
                        materialFamily, materialDetail, condition, quantity, unit, status,
                        valueCents, imageUrlsJson, createdAt, updatedAt, syncState, archived,
                        marketplaceListingId, marketplaceAllowedActionsJson, marketplacePublishedQuantity,
                        marketplaceBuyUnitPrice, marketplaceRentUnitPrice, marketplaceDefaultDurationDays,
                        marketplaceTerms, location, latitude, longitude, reuseCount
                    )
                    SELECT
                        id, accountId, eventId, ownerId, title, category,
                        ${legacyMaterialFamilySql("material")},
                        ${legacyMaterialDetailSql("material")},
                        condition, quantity, unit, status, valueCents, imageUrlsJson,
                        createdAt, updatedAt, syncState, archived, marketplaceListingId,
                        marketplaceAllowedActionsJson, marketplacePublishedQuantity,
                        marketplaceBuyUnitPrice, marketplaceRentUnitPrice,
                        marketplaceDefaultDurationDays, marketplaceTerms, location,
                        latitude, longitude, reuseCount
                    FROM resource_items
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE resource_items")
                db.execSQL("ALTER TABLE resource_items_material_family RENAME TO resource_items")
                db.execSQL("CREATE INDEX index_resource_items_accountId_eventId ON resource_items(accountId, eventId)")
                db.execSQL("CREATE INDEX index_resource_items_accountId_ownerId ON resource_items(accountId, ownerId)")
                db.execSQL("CREATE INDEX index_resource_items_accountId_status ON resource_items(accountId, status)")

                db.execSQL(
                    """
                    CREATE TABLE circular_programmes_material_family (
                        id TEXT NOT NULL, accountId TEXT NOT NULL, partnerId TEXT NOT NULL,
                        name TEXT NOT NULL, type TEXT NOT NULL,
                        acceptedMaterialFamiliesJson TEXT NOT NULL, location TEXT NOT NULL,
                        active INTEGER NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                        syncState TEXT NOT NULL, acceptedCategoriesJson TEXT NOT NULL,
                        acceptedConditionsJson TEXT NOT NULL, minimumQuantity REAL,
                        maximumQuantity REAL, unit TEXT, remainingCapacity REAL,
                        coinDirection TEXT NOT NULL, unitCoinAmount INTEGER,
                        pickupAvailable INTEGER NOT NULL, latitude REAL, longitude REAL,
                        processingMethod TEXT NOT NULL, terms TEXT NOT NULL,
                        PRIMARY KEY(accountId, id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO circular_programmes_material_family (
                        id, accountId, partnerId, name, type, acceptedMaterialFamiliesJson,
                        location, active, createdAt, updatedAt, syncState,
                        acceptedCategoriesJson, acceptedConditionsJson, minimumQuantity,
                        maximumQuantity, unit, remainingCapacity, coinDirection,
                        unitCoinAmount, pickupAvailable, latitude, longitude, processingMethod, terms
                    )
                    SELECT
                        id, accountId, partnerId, name, type,
                        ${legacyAcceptedFamiliesSql("acceptedMaterialsJson")},
                        location, active, createdAt, updatedAt, syncState,
                        acceptedCategoriesJson, acceptedConditionsJson, minimumQuantity,
                        maximumQuantity, unit, remainingCapacity, coinDirection,
                        unitCoinAmount, pickupAvailable, latitude, longitude, processingMethod, terms
                    FROM circular_programmes
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE circular_programmes")
                db.execSQL("ALTER TABLE circular_programmes_material_family RENAME TO circular_programmes")
                db.execSQL(
                    "CREATE INDEX index_circular_programmes_accountId_partnerId " +
                        "ON circular_programmes(accountId, partnerId)",
                )
            }
        }

        private fun legacyMaterialFamilySql(column: String): String {
            val value = "LOWER(TRIM($column))"
            return """
                CASE
                    WHEN $value IN ('wood','wooden','timber','plywood','bamboo','cork','mdf') THEN 'WOOD'
                    WHEN $value IN ('textile','textiles','fabric','canvas','cotton','wool','linen','polyester') THEN 'TEXTILES'
                    WHEN $value IN ('metal','steel','aluminium','aluminum','iron','copper','brass') THEN 'METAL'
                    WHEN $value IN ('plastic','plastics','acrylic','pet','pvc','polypropylene','pp','polystyrene') THEN 'PLASTIC'
                    WHEN $value IN ('paper','card','cardboard','paper and card','paper & card') THEN 'PAPER_CARD'
                    WHEN $value = 'glass' THEN 'GLASS'
                    WHEN $value IN ('ceramic','ceramics','porcelain','stoneware') THEN 'CERAMIC'
                    WHEN $value IN ('electrical','electronics','electronic','e-waste','ewaste','cable','cables','lighting') THEN 'ELECTRICAL_ELECTRONICS'
                    WHEN $value IN ('organic','food','compostable','plant','plants','foliage') THEN 'ORGANIC'
                    WHEN $value IN ('rubber','latex','silicone') THEN 'RUBBER'
                    ELSE 'MIXED_OTHER'
                END
            """.trimIndent()
        }

        private fun legacyMaterialDetailSql(column: String): String {
            val value = "LOWER(TRIM($column))"
            val genericAliases = listOf(
                "wood", "textile", "textiles", "metal", "plastic", "plastics", "paper",
                "paper and card", "paper & card", "glass", "ceramic", "ceramics", "electrical",
                "electronics", "electronic", "organic", "rubber", "mixed", "other",
            ).joinToString(",") { "'$it'" }
            return """
                CASE
                    WHEN TRIM($column) = '' THEN 'Unspecified material'
                    WHEN $value IN ($genericAliases) THEN NULL
                    ELSE SUBSTR(TRIM($column), 1, 120)
                END
            """.trimIndent()
        }

        private fun legacyAcceptedFamiliesSql(column: String): String {
            val value = "LOWER($column)"
            val families = listOf(
                "WOOD" to listOf("wood", "timber", "plywood", "bamboo", "cork", "mdf"),
                "TEXTILES" to listOf("textile", "fabric", "canvas", "cotton", "wool", "linen", "polyester"),
                "METAL" to listOf("metal", "steel", "aluminium", "aluminum", "iron", "copper", "brass"),
                "PLASTIC" to listOf("plastic", "acrylic", "pet", "pvc", "polypropylene", "polystyrene"),
                "PAPER_CARD" to listOf("paper", "cardboard", "card\""),
                "GLASS" to listOf("glass"),
                "CERAMIC" to listOf("ceramic", "porcelain", "stoneware"),
                "ELECTRICAL_ELECTRONICS" to listOf("electrical", "electronic", "e-waste", "ewaste", "cable", "lighting"),
                "ORGANIC" to listOf("organic", "food", "compostable", "plant", "foliage"),
                "RUBBER" to listOf("rubber", "latex", "silicone"),
            )
            fun match(aliases: List<String>) = aliases.joinToString(" OR ") { "$value LIKE '%$it%'" }
            val recognised = families.joinToString(" OR ") { (_, aliases) -> "(${match(aliases)})" }
            val entries = families.joinToString(" || ") { (family, aliases) ->
                "CASE WHEN ${match(aliases)} THEN '\"$family\",' ELSE '' END"
            }
            return """
                CASE
                    WHEN TRIM($column) IN ('', '[]') THEN '[]'
                    ELSE '[' || RTRIM(
                        $entries || CASE WHEN NOT ($recognised) THEN '"MIXED_OTHER",' ELSE '' END,
                        ','
                    ) || ']'
                END
            """.trimIndent()
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
