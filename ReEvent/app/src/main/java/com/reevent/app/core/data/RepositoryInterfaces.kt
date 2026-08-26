package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.LegacyProgrammeDraft
import com.reevent.app.core.model.PartnerDiscoveryRequest
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.PlaceSuggestion
import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.core.auth.AccountDeletionOutcome
import com.reevent.app.core.model.AllocationSide
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.Flow

sealed interface SignUpOutcome {
    data class Authenticated(val user: User) : SignUpOutcome
    data object ConfirmationRequired : SignUpOutcome
}

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, displayName: String): AppResult<SignUpOutcome>
    suspend fun signIn(email: String, password: String): AppResult<User>
    suspend fun resendSignUpConfirmation(email: String): AppResult<Unit>
    suspend fun startGoogleSignIn(): AppResult<Unit>
    suspend fun handleOAuthCallback(intent: Intent): AppResult<User?>
    suspend fun completeRole(role: UserRole): AppResult<User>
    suspend fun restoreSession(): AppResult<User?>
    suspend fun requestPasswordReset(email: String): AppResult<Unit>
    suspend fun checkEmailExists(email: String): AppResult<Boolean>
    suspend fun updatePassword(newPassword: String): AppResult<Unit>
    suspend fun finishPasswordRecovery(): AppResult<Unit>
    suspend fun deleteAccount(currentPassword: String): AppResult<AccountDeletionOutcome>
    suspend fun signOut(): AppResult<Unit>
}

interface EventRepository {
    fun observeOwnedEvents(ownerId: String): Flow<List<Event>>
    fun observeEvent(eventId: String): Flow<Event?>
    suspend fun saveEvent(event: Event): AppResult<Event>
    suspend fun archiveEvent(eventId: String): AppResult<Unit>
}

interface ResourceRepository {
    fun observeEventResources(eventId: String): Flow<List<ResourceItem>>
    fun observeOwnedResources(ownerId: String): Flow<List<ResourceItem>>
    fun observeMarketplace(): Flow<List<ResourceItem>>
    fun observeResource(resourceId: String): Flow<ResourceItem?>
    fun observeResources(resourceIds: Set<String>): Flow<List<ResourceItem>>
    suspend fun saveResource(resource: ResourceItem): AppResult<ResourceItem>
    suspend fun archiveResource(resourceId: String): AppResult<Unit>
}

interface PassportRepository {
    fun observePassport(resourceId: String): Flow<ResourcePassport?>
    fun observePassports(resourceIds: Set<String>): Flow<List<ResourcePassport>>
}

interface PartnerRepository {
    fun observeProgrammes(partnerId: String? = null): Flow<List<CircularProgramme>>
    fun observeLegacyProgrammeDrafts(partnerId: String): Flow<List<LegacyProgrammeDraft>>
    suspend fun discardLegacyProgrammeDraft(id: String): AppResult<Unit>
    suspend fun discoverProgrammes(request: PartnerDiscoveryRequest): AppResult<PartnerDiscoveryResult>
    suspend fun saveProgramme(programme: CircularProgramme): AppResult<CircularProgramme>
}

interface GeocodingRepository {
    suspend fun search(query: String, proximity: GeoLocation? = null): AppResult<List<PlaceSuggestion>>
    suspend fun reverse(location: GeoLocation): AppResult<PlaceSuggestion>
}

interface TransactionRepository {
    fun observeTransactions(userId: String): Flow<List<CircularTransaction>>
    fun observeEventTransactions(eventId: String): Flow<List<CircularTransaction>>
    suspend fun requestMarketplace(
        resourceId: String,
        type: com.reevent.app.core.model.TransactionType,
        quantity: Double,
        counterResourceId: String? = null,
        reason: String? = null
    ): AppResult<CircularTransaction>
    suspend fun requestProgramme(programmeId: String, resourceId: String, quantity: Double, reason: String? = null): AppResult<CircularTransaction>
    suspend fun approve(transactionId: String): AppResult<CircularTransaction>
    suspend fun reject(transactionId: String, reason: String): AppResult<CircularTransaction>
    suspend fun cancel(transactionId: String, reason: String): AppResult<CircularTransaction>
    suspend fun beginHandover(transactionId: String, side: AllocationSide = AllocationSide.PRIMARY): AppResult<CircularTransaction>
    suspend fun confirmReceipt(transactionId: String, side: AllocationSide = AllocationSide.PRIMARY): AppResult<CircularTransaction>
    suspend fun beginReturn(transactionId: String): AppResult<CircularTransaction>
    suspend fun confirmReturn(transactionId: String): AppResult<CircularTransaction>
}

interface ImpactRepository {
    fun observeImpact(eventId: String): Flow<List<ImpactRecord>>
}

/** Shared refresh boundary. Runtime screens use this rather than Supabase directly. */
interface CoreSyncRepository {
    suspend fun refreshAuthorisedData(): AppResult<Unit>
    /** Flushes the current account's queue in-process when a following operation needs its row. */
    suspend fun syncPendingNow(): AppResult<Unit>
    fun observePendingSyncCommands(): Flow<List<SyncCommandStatus>>
    suspend fun retryPendingSync(): AppResult<Unit>
}

/** Publication uses a protected server command rather than the generic local-record outbox. */
interface MarketplaceListingRepository {
    suspend fun publishListing(resource: ResourceItem, draft: MarketplaceListingDraft): AppResult<Unit>
}

/** Uses Android's system picker; no broad media permission is required. */
interface MediaRepository {
    suspend fun uploadResourcePhoto(resourceId: String, uri: Uri): AppResult<String>
    suspend fun downloadResourcePhoto(path: String): AppResult<ByteArray>
}
