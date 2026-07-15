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
    version = 2,
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
    }
}
