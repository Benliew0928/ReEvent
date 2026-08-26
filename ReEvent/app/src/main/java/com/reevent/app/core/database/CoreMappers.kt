package com.reevent.app.core.database

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.LegacyProgrammeDraft
import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import kotlinx.serialization.json.Json

private val coreJson = Json { ignoreUnknownKeys = true }

fun UserEntity.toDomain() = User(id, email, displayName, role?.let(UserRole::valueOf), avatarUrl, createdAt, updatedAt)
fun User.toEntity() = UserEntity(id, email, displayName, role?.name, avatarUrl, createdAt, updatedAt)

fun EventEntity.toDomain() = Event(
    id, ownerId, name, description, venue, startsAt, endsAt, status, createdAt, updatedAt,
    SyncState.valueOf(syncState), archived, geoLocation(venue, latitude, longitude),
)
fun Event.toEntity(accountId: String) = EventEntity(
    id, accountId, ownerId, name, description, venue, startsAt, endsAt, status, createdAt, updatedAt,
    syncState.name, archived, geoLocation?.latitude, geoLocation?.longitude,
)

fun ResourceEntity.toDomain() = ResourceItem(
    id, eventId, ownerId, title, category, material, ResourceCondition.valueOf(condition), quantity, unit,
    ResourceStatus.valueOf(status), valueCents, coreJson.decodeFromString(imageUrlsJson), createdAt, updatedAt,
    SyncState.valueOf(syncState), archived,
    marketplaceListingId?.let {
        MarketplaceListing(
            id = it,
            allowedActions = coreJson.decodeFromString(marketplaceAllowedActionsJson),
            publishedQuantity = checkNotNull(marketplacePublishedQuantity),
            buyUnitPrice = marketplaceBuyUnitPrice,
            rentUnitPrice = marketplaceRentUnitPrice,
            defaultDurationDays = marketplaceDefaultDurationDays,
            terms = marketplaceTerms
        )
    },
    geoLocation(location.orEmpty(), latitude, longitude), reuseCount,
)
fun ResourceItem.toEntity(accountId: String) = ResourceEntity(
    id, accountId, eventId, ownerId, title, category, material, condition.name, quantity, unit, status.name, valueCents,
    coreJson.encodeToString(imageUrls), createdAt, updatedAt, syncState.name, archived,
    marketplaceListing?.id,
    coreJson.encodeToString(marketplaceListing?.allowedActions.orEmpty()),
    marketplaceListing?.publishedQuantity,
    marketplaceListing?.buyUnitPrice,
    marketplaceListing?.rentUnitPrice,
    marketplaceListing?.defaultDurationDays,
    marketplaceListing?.terms.orEmpty(),
    geoLocation?.displayAddress,
    geoLocation?.latitude,
    geoLocation?.longitude,
    reuseCount,
)

fun PassportEntity.toDomain() = ResourcePassport(id, resourceId, qrPayload, historyJson, createdAt, updatedAt, SyncState.valueOf(syncState))
fun ResourcePassport.toEntity(accountId: String) = PassportEntity(id, accountId, resourceId, qrPayload, historyJson, createdAt, updatedAt, syncState.name)

fun ProgrammeEntity.toDomain() = CircularProgramme(
    id, partnerId, name, ProgrammeType.valueOf(type), coreJson.decodeFromString(acceptedMaterialsJson), location,
    active, createdAt, updatedAt, SyncState.valueOf(syncState),
    coreJson.decodeFromString(acceptedCategoriesJson),
    coreJson.decodeFromString<List<String>>(acceptedConditionsJson).map(ResourceCondition::valueOf).toSet(),
    minimumQuantity, maximumQuantity, unit, remainingCapacity, CoinDirection.valueOf(coinDirection),
    unitCoinAmount, pickupAvailable, geoLocation(location, latitude, longitude), processingMethod, terms,
)
fun CircularProgramme.toEntity(accountId: String) = ProgrammeEntity(
    id, accountId, partnerId, name, type.name, coreJson.encodeToString(acceptedMaterials), location, active, createdAt,
    updatedAt, syncState.name,
    coreJson.encodeToString(acceptedCategories),
    coreJson.encodeToString(acceptedConditions.map(ResourceCondition::name)),
    minimumQuantity, maximumQuantity, unit, remainingCapacity, coinDirection.name, unitCoinAmount,
    pickupAvailable, geoLocation?.latitude, geoLocation?.longitude, processingMethod, terms,
)

private fun geoLocation(address: String, latitude: Double?, longitude: Double?): GeoLocation? =
    if (latitude != null && longitude != null) GeoLocation(address, latitude, longitude) else null

fun LegacyProgrammeDraftEntity.toDomain() = LegacyProgrammeDraft(
    id = id,
    partnerId = partnerId,
    name = name,
    type = ProgrammeType.valueOf(type),
    acceptedMaterials = coreJson.decodeFromString(acceptedMaterialsJson),
    location = location,
)

fun TransactionEntity.toDomain() = CircularTransaction(
    id, eventId, resourceId, senderId, receiverId, partnerId, TransactionType.valueOf(type),
    TransactionStatus.valueOf(status), quantity, createdAt, updatedAt, SyncState.valueOf(syncState), archived,
    requesterId, counterResourceId, programmeId, approvedAt, inTransitAt, activeAt, returnStartedAt, completedAt,
)
fun CircularTransaction.toEntity(accountId: String) = TransactionEntity(
    id, accountId, eventId, resourceId, senderId, receiverId, partnerId, requesterId, counterResourceId,
    type.name, status.name, quantity, createdAt,
    updatedAt, syncState.name, archived, programmeId, approvedAt, inTransitAt, activeAt, returnStartedAt, completedAt,
)

fun ImpactEntity.toDomain() = ImpactRecord(
    id, eventId, resourceId, transactionId, TransactionType.valueOf(transactionType), completedQuantity, unit,
    materialDivertedKg, emissionsAvoidedKg, recoinsTransferred, recoinsRewarded, calculatedAt, SyncState.valueOf(syncState)
)
fun ImpactRecord.toEntity(accountId: String) = ImpactEntity(
    id, accountId, eventId, resourceId, transactionId, transactionType.name, completedQuantity, unit,
    materialDivertedKg, emissionsAvoidedKg, recoinsTransferred, recoinsRewarded, calculatedAt, syncState.name
)
