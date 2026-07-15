package com.reevent.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val role: String?,
    val avatarUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "events", indices = [Index("ownerId")])
data class EventEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val ownerId: String,
    val name: String,
    val description: String,
    val venue: String,
    val startsAt: Long,
    val endsAt: Long,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String,
    val archived: Boolean
)

@Entity(tableName = "resource_items", indices = [Index("eventId"), Index("ownerId"), Index("status")])
data class ResourceEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val eventId: String,
    val ownerId: String,
    val title: String,
    val category: String,
    val material: String,
    val condition: String,
    val quantity: Int,
    val unit: String,
    val status: String,
    val valueCents: Long,
    val imageUrlsJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String,
    val archived: Boolean
)

@Entity(tableName = "resource_passports", indices = [Index(value = ["resourceId"], unique = true)])
data class PassportEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val resourceId: String,
    val qrPayload: String,
    val historyJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String
)

@Entity(tableName = "circular_programmes", indices = [Index("partnerId")])
data class ProgrammeEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val partnerId: String,
    val name: String,
    val type: String,
    val acceptedMaterialsJson: String,
    val location: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String
)

@Entity(tableName = "circular_transactions", indices = [Index("eventId"), Index("resourceId"), Index("senderId"), Index("receiverId"), Index("partnerId")])
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val eventId: String,
    val resourceId: String,
    val senderId: String,
    val receiverId: String,
    val partnerId: String?,
    val type: String,
    val status: String,
    val quantity: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String,
    val archived: Boolean
)

@Entity(tableName = "impact_records", indices = [Index("eventId"), Index("resourceId"), Index("transactionId")])
data class ImpactEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val eventId: String,
    val resourceId: String?,
    val transactionId: String?,
    val materialDivertedKg: Double,
    val emissionsAvoidedKg: Double,
    val valueRecoveredCents: Long,
    val calculatedAt: Long,
    val updatedAt: Long,
    val syncState: String
)

@Entity(
    tableName = "sync_outbox",
    indices = [Index(value = ["tableName", "recordId"], unique = true)]
)
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tableName: String,
    val accountId: String,
    val recordId: String,
    val operation: String,
    val payload: String,
    val attempts: Int = 0,
    val lastError: String? = null,
    val updatedAt: Long
)
