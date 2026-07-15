package com.reevent.app.core.model

/** Server-authorised role. A mobile client may display this value but never elevate it. */
enum class UserRole {
    ORGANIZER,
    PARTICIPANT,
    PARTNER
}

enum class ResourceCondition { NEW, GOOD, FAIR, NEEDS_REPAIR, RECYCLE_ONLY }
enum class ResourceStatus { DRAFT, AVAILABLE, RESERVED, HANDED_OVER, RECOVERED, ARCHIVED }
enum class TransactionType { RESALE, DONATION, REPAIR, RECYCLE, RETURN, BUY_BACK }
enum class TransactionStatus { PENDING, ACCEPTED, IN_TRANSIT, COMPLETED, CANCELLED }
enum class ProgrammeType { COLLECTION, REPAIR, REUSE, RECYCLE, BUY_BACK }
enum class SyncState { SYNCED, PENDING, FAILED }

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole?,
    val avatarUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

data class Event(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String,
    val venue: String,
    val startsAt: Long,
    val endsAt: Long,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val archived: Boolean = false
)

data class ResourceItem(
    val id: String,
    val eventId: String,
    val ownerId: String,
    val title: String,
    val category: String,
    val material: String,
    val condition: ResourceCondition,
    val quantity: Int,
    val unit: String,
    val status: ResourceStatus,
    val valueCents: Long,
    val imageUrls: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val archived: Boolean = false
)

data class ResourcePassport(
    val id: String,
    val resourceId: String,
    val qrPayload: String,
    val historyJson: String,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING
)

data class CircularProgramme(
    val id: String,
    val partnerId: String,
    val name: String,
    val type: ProgrammeType,
    val acceptedMaterials: List<String>,
    val location: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING
)

data class CircularTransaction(
    val id: String,
    val eventId: String,
    val resourceId: String,
    val senderId: String,
    val receiverId: String,
    val partnerId: String?,
    val type: TransactionType,
    val status: TransactionStatus,
    val quantity: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val archived: Boolean = false
)

data class ImpactRecord(
    val id: String,
    val eventId: String,
    val resourceId: String?,
    val transactionId: String?,
    val materialDivertedKg: Double,
    val emissionsAvoidedKg: Double,
    val valueRecoveredCents: Long,
    val calculatedAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING
)
