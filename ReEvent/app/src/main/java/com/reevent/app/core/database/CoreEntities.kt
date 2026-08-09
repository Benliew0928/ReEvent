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

@Entity(
    tableName = "events",
    primaryKeys = ["accountId", "id"],
    indices = [Index(value = ["accountId", "ownerId"])]
)
data class EventEntity(
    val id: String,
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
) {
    init { require(accountId.isNotBlank()) { "Event cache rows require an accountId" } }
}

@Entity(
    tableName = "resource_items",
    primaryKeys = ["accountId", "id"],
    indices = [
        Index(value = ["accountId", "eventId"]),
        Index(value = ["accountId", "ownerId"]),
        Index(value = ["accountId", "status"])
    ]
)
data class ResourceEntity(
    val id: String,
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
) {
    init { require(accountId.isNotBlank()) { "Resource cache rows require an accountId" } }
}

@Entity(
    tableName = "resource_passports",
    primaryKeys = ["accountId", "id"],
    indices = [Index(value = ["accountId", "resourceId"], unique = true)]
)
data class PassportEntity(
    val id: String,
    val accountId: String,
    val resourceId: String,
    val qrPayload: String,
    val historyJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: String
) {
    init { require(accountId.isNotBlank()) { "Passport cache rows require an accountId" } }
}

@Entity(
    tableName = "circular_programmes",
    primaryKeys = ["accountId", "id"],
    indices = [Index(value = ["accountId", "partnerId"])]
)
data class ProgrammeEntity(
    val id: String,
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
) {
    init { require(accountId.isNotBlank()) { "Programme cache rows require an accountId" } }
}

@Entity(
    tableName = "circular_transactions",
    primaryKeys = ["accountId", "id"],
    indices = [
        Index(value = ["accountId", "eventId"]),
        Index(value = ["accountId", "resourceId"]),
        Index(value = ["accountId", "senderId"]),
        Index(value = ["accountId", "receiverId"]),
        Index(value = ["accountId", "partnerId"])
    ]
)
data class TransactionEntity(
    val id: String,
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
) {
    init { require(accountId.isNotBlank()) { "Transaction cache rows require an accountId" } }
}

@Entity(
    tableName = "impact_records",
    primaryKeys = ["accountId", "id"],
    indices = [
        Index(value = ["accountId", "eventId"]),
        Index(value = ["accountId", "resourceId"]),
        Index(value = ["accountId", "transactionId"])
    ]
)
data class ImpactEntity(
    val id: String,
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
) {
    init { require(accountId.isNotBlank()) { "Impact cache rows require an accountId" } }
}

@Entity(
    tableName = "sync_outbox",
    indices = [Index(value = ["accountId", "tableName", "recordId"], unique = true)]
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
) {
    init { require(accountId.isNotBlank()) { "Outbox rows require an accountId" } }
}
