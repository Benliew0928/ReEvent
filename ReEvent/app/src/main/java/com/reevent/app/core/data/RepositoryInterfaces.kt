package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
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
    fun observeMarketplace(): Flow<List<ResourceItem>>
    fun observeResource(resourceId: String): Flow<ResourceItem?>
    suspend fun saveResource(resource: ResourceItem): AppResult<ResourceItem>
    suspend fun archiveResource(resourceId: String): AppResult<Unit>
}

interface PassportRepository {
    fun observePassport(resourceId: String): Flow<ResourcePassport?>
    suspend fun savePassport(passport: ResourcePassport): AppResult<ResourcePassport>
}

interface PartnerRepository {
    fun observeProgrammes(partnerId: String? = null): Flow<List<CircularProgramme>>
    suspend fun saveProgramme(programme: CircularProgramme): AppResult<CircularProgramme>
}

interface TransactionRepository {
    fun observeTransactions(userId: String): Flow<List<CircularTransaction>>
    suspend fun saveTransaction(transaction: CircularTransaction): AppResult<CircularTransaction>
    suspend fun archiveTransaction(transactionId: String): AppResult<Unit>
}

interface ImpactRepository {
    fun observeImpact(eventId: String): Flow<List<ImpactRecord>>
    suspend fun saveImpact(record: ImpactRecord): AppResult<ImpactRecord>
}

/** Shared refresh boundary. Runtime screens use this rather than Supabase directly. */
interface CoreSyncRepository {
    suspend fun refreshAuthorisedData(): AppResult<Unit>
}

/** Uses Android's system picker; no broad media permission is required. */
interface MediaRepository {
    suspend fun uploadResourcePhoto(resourceId: String, uri: Uri): AppResult<String>
}
