package com.reevent.app.core.database

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
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

fun EventEntity.toDomain() = Event(id, ownerId, name, description, venue, startsAt, endsAt, status, createdAt, updatedAt, SyncState.valueOf(syncState), archived)
fun Event.toEntity(accountId: String) = EventEntity(id, accountId, ownerId, name, description, venue, startsAt, endsAt, status, createdAt, updatedAt, syncState.name, archived)

fun ResourceEntity.toDomain() = ResourceItem(
    id, eventId, ownerId, title, category, material, ResourceCondition.valueOf(condition), quantity, unit,
    ResourceStatus.valueOf(status), valueCents, coreJson.decodeFromString(imageUrlsJson), createdAt, updatedAt,
    SyncState.valueOf(syncState), archived
)
fun ResourceItem.toEntity(accountId: String) = ResourceEntity(
    id, accountId, eventId, ownerId, title, category, material, condition.name, quantity, unit, status.name, valueCents,
    coreJson.encodeToString(imageUrls), createdAt, updatedAt, syncState.name, archived
)

fun PassportEntity.toDomain() = ResourcePassport(id, resourceId, qrPayload, historyJson, createdAt, updatedAt, SyncState.valueOf(syncState))
fun ResourcePassport.toEntity(accountId: String) = PassportEntity(id, accountId, resourceId, qrPayload, historyJson, createdAt, updatedAt, syncState.name)

fun ProgrammeEntity.toDomain() = CircularProgramme(
    id, partnerId, name, ProgrammeType.valueOf(type), coreJson.decodeFromString(acceptedMaterialsJson), location,
    active, createdAt, updatedAt, SyncState.valueOf(syncState)
)
fun CircularProgramme.toEntity(accountId: String) = ProgrammeEntity(
    id, accountId, partnerId, name, type.name, coreJson.encodeToString(acceptedMaterials), location, active, createdAt,
    updatedAt, syncState.name
)

fun TransactionEntity.toDomain() = CircularTransaction(
    id, eventId, resourceId, senderId, receiverId, partnerId, TransactionType.valueOf(type),
    TransactionStatus.valueOf(status), quantity, createdAt, updatedAt, SyncState.valueOf(syncState), archived
)
fun CircularTransaction.toEntity(accountId: String) = TransactionEntity(
    id, accountId, eventId, resourceId, senderId, receiverId, partnerId, type.name, status.name, quantity, createdAt,
    updatedAt, syncState.name, archived
)

fun ImpactEntity.toDomain() = ImpactRecord(
    id, eventId, resourceId, transactionId, materialDivertedKg, emissionsAvoidedKg, valueRecoveredCents,
    calculatedAt, updatedAt, SyncState.valueOf(syncState)
)
fun ImpactRecord.toEntity(accountId: String) = ImpactEntity(
    id, accountId, eventId, resourceId, transactionId, materialDivertedKg, emissionsAvoidedKg, valueRecoveredCents,
    calculatedAt, updatedAt, syncState.name
)
