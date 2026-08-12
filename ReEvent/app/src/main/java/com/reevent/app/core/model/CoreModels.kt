package com.reevent.app.core.model

import kotlinx.serialization.Serializable

/** Server-authorised role. A mobile client may display this value but never elevate it. */
enum class UserRole {
    ORGANIZER,
    PARTICIPANT,
    PARTNER
}

enum class ResourceCondition { NEW, GOOD, FAIR, NEEDS_REPAIR, END_OF_LIFE }
enum class ResourceStatus { DRAFT, ACTIVE, RECOVERY_IN_PROGRESS, RECOVERED, ARCHIVED }
enum class TransactionType { BORROW, RENT, BUY, DONATE, EXCHANGE, REPAIR, RECYCLE, BUY_BACK }
enum class TransactionStatus {
    REQUESTED,
    APPROVED,
    IN_TRANSIT,
    ACTIVE,
    RETURN_IN_PROGRESS,
    COMPLETED,
    REJECTED,
    CANCELLED
}
enum class ProgrammeType { REPAIR, RECYCLE, BUY_BACK }
enum class AllocationSide { PRIMARY, COUNTER }
enum class SyncState { SYNCED, PENDING, FAILED }

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole?,
    val avatarUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    /** Server preparation is terminal; this account may only retry deletion or sign out. */
    val deletionPending: Boolean = false
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
    val quantity: Double,
    val unit: String,
    val status: ResourceStatus,
    val valueCents: Long,
    val imageUrls: List<String>,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val archived: Boolean = false,
    val marketplaceListing: MarketplaceListing? = null
)

/** Published marketplace terms supplied by the server, never inferred from resource condition. */
data class MarketplaceListing(
    val id: String,
    val allowedActions: List<TransactionType>,
    val publishedQuantity: Double,
    val buyUnitPrice: Long? = null,
    val rentUnitPrice: Long? = null,
    val defaultDurationDays: Int? = null,
    val terms: String = ""
)

/** Terms supplied by an organiser before the server creates a published marketplace listing. */
data class MarketplaceListingDraft(
    val allowedActions: Set<TransactionType>,
    val publishedQuantity: Double,
    val buyUnitPrice: Long? = null,
    val rentUnitPrice: Long? = null,
    val defaultDurationDays: Int? = null,
    val terms: String = ""
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

@Serializable
data class PassportHistoryEntry(
    val occurredAt: Long,
    val action: String,
    val actorId: String = "server",
    val quantity: Double? = null,
    val unit: String? = null,
    val previousCondition: ResourceCondition? = null,
    val newCondition: ResourceCondition? = null,
    val publicSummary: String
) {
    val previousStatus: ResourceStatus? get() = null
    val newStatus: ResourceStatus get() = when (action) {
        "RECYCLED", "REPAIRED", "RETURNED", "OWNERSHIP_TRANSFERRED" -> ResourceStatus.RECOVERED
        "ARCHIVED" -> ResourceStatus.ARCHIVED
        "RESERVED", "CHECKED_OUT", "RETURN_STARTED", "REPAIR_STARTED" -> ResourceStatus.RECOVERY_IN_PROGRESS
        else -> ResourceStatus.ACTIVE
    }
    val note: String get() = publicSummary
}

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
    val quantity: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val archived: Boolean = false,
    val requesterId: String = senderId,
    val counterResourceId: String? = null
)

data class ImpactRecord(
    val id: String,
    val eventId: String,
    val resourceId: String,
    val transactionId: String,
    val transactionType: TransactionType,
    val completedQuantity: Double,
    val unit: String,
    val materialDivertedKg: Double?,
    val emissionsAvoidedKg: Double?,
    val recoinsTransferred: Long,
    val recoinsRewarded: Long,
    val calculatedAt: Long,
    val syncState: SyncState = SyncState.PENDING
)
