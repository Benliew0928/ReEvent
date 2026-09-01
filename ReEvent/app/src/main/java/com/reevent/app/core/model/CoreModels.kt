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
enum class EventStatus { DRAFT, ACTIVE, COMPLETED, ARCHIVED }
enum class EventType { CONFERENCE, EXHIBITION, FESTIVAL, WORKSHOP, COMMUNITY, OTHER }
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
enum class CoinDirection { FREE, OWNER_PAYS_PARTNER, PARTNER_PAYS_OWNER }
enum class AllocationSide { PRIMARY, COUNTER }
enum class SyncState { SYNCED, PENDING, FAILED }

data class GeoLocation(
    val displayAddress: String,
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(displayAddress.isNotBlank()) { "Display address must not be blank" }
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }
}

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
    val archived: Boolean = false,
    val geoLocation: GeoLocation? = null,
    /** Server lifecycle contract fields. Drafts may leave these values incomplete. */
    val eventType: String? = null,
    val timezoneId: String? = null,
    val expectedAttendance: Int? = null,
    val recoveryTargetPercent: Double = 0.0,
)

/** Privacy-safe event projection returned by the authenticated discovery RPC. */
data class DiscoverableEvent(
    val id: String,
    val name: String,
    val description: String,
    val eventType: String,
    val startsAt: Long,
    val endsAt: Long,
    val timezoneId: String,
    val venue: String,
    val recoveryTargetPercent: Double,
)

data class ResourceItem(
    val id: String,
    val eventId: String,
    val ownerId: String,
    val title: String,
    val category: String,
    val materialFamily: MaterialFamily,
    val materialDetail: String? = null,
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
    val marketplaceListing: MarketplaceListing? = null,
    val geoLocation: GeoLocation? = null,
    /** Authoritative number of completed reuse cycles projected by the server. */
    val reuseCount: Int = 0,
) {
    val materialLabel: String get() = MaterialDescriptor(materialFamily, materialDetail).displayLabel
}

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
    val syncState: SyncState = SyncState.PENDING,
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
    val acceptedMaterialFamilies: Set<MaterialFamily>,
    val location: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncState = SyncState.PENDING,
    val acceptedCategories: List<String> = emptyList(),
    val acceptedConditions: Set<ResourceCondition> = ResourceCondition.entries.toSet(),
    val minimumQuantity: Double? = null,
    val maximumQuantity: Double? = null,
    val unit: String? = null,
    val remainingCapacity: Double? = null,
    val coinDirection: CoinDirection = CoinDirection.FREE,
    val unitCoinAmount: Long? = null,
    val pickupAvailable: Boolean = false,
    val geoLocation: GeoLocation? = null,
    val processingMethod: String = "",
    val terms: String = "",
  ) {
      fun hasValidProgrammeRules(): Boolean =
          name.trim().length in 1..120 &&
              acceptedConditions.isNotEmpty() &&
              (minimumQuantity == null || minimumQuantity > 0.0) &&
            (maximumQuantity == null || maximumQuantity > 0.0) &&
            (minimumQuantity == null || maximumQuantity == null || maximumQuantity >= minimumQuantity) &&
              (remainingCapacity == null || remainingCapacity >= 0.0) &&
              (unit != null || listOf(minimumQuantity, maximumQuantity, remainingCapacity, unitCoinAmount).all { it == null }) &&
              ((coinDirection == CoinDirection.FREE && unitCoinAmount == null) ||
                  (coinDirection != CoinDirection.FREE && unitCoinAmount != null && unitCoinAmount > 0)) &&
              when (type) {
                  ProgrammeType.REPAIR -> coinDirection in setOf(CoinDirection.FREE, CoinDirection.OWNER_PAYS_PARTNER)
                  ProgrammeType.RECYCLE, ProgrammeType.BUY_BACK -> coinDirection in setOf(CoinDirection.FREE, CoinDirection.PARTNER_PAYS_OWNER)
              }

      fun isActivationReady(): Boolean =
          hasValidProgrammeRules() &&
              geoLocation?.displayAddress?.isNotBlank() == true &&
              processingMethod.isNotBlank() &&
              terms.isNotBlank()
  }

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
    val counterResourceId: String? = null,
    val programmeId: String? = null,
    val approvedAt: Long? = null,
    val inTransitAt: Long? = null,
    val activeAt: Long? = null,
    val returnStartedAt: Long? = null,
    val completedAt: Long? = null,
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
