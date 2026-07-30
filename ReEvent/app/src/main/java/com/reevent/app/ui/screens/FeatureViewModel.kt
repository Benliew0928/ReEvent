package com.reevent.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.EventRepository
import com.reevent.app.core.data.ImpactRepository
import com.reevent.app.core.data.MediaRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.PassportRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.PassportHistoryEntry
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

data class FeatureActionState(val loading: Boolean = false, val error: String? = null, val notice: String? = null)

/** Actions available after an authenticated ReEvent QR scan. */
enum class ResourceLifecycleAction(val label: String) {
    CHECK_OUT("Check out item"),
    RETURN("Record return"),
    MARK_DAMAGED("Mark damaged"),
    REQUEST_REPAIR("Request repair"),
    TRANSFER("Record transfer")
}

@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val events: EventRepository,
    private val resources: ResourceRepository,
    private val passports: PassportRepository,
    private val partners: PartnerRepository,
    private val transactions: TransactionRepository,
    private val impact: ImpactRepository,
    private val sync: CoreSyncRepository,
    private val media: MediaRepository,
    private val preferences: AppPreferences
) : ViewModel() {
    private val passportHistoryJson = Json { ignoreUnknownKeys = true }
    private val mutableAction = MutableStateFlow(FeatureActionState())
    val action: StateFlow<FeatureActionState> = mutableAction
    val selectedEventId: Flow<String?> = preferences.lastOpenedEventId

    /** Initial workspace refresh is best-effort: an offline server must not look like a failed user action. */
    fun refresh() {
        viewModelScope.launch { sync.refreshAuthorisedData() }
    }
    fun events(ownerId: String): Flow<List<Event>> = events.observeOwnedEvents(ownerId)
    fun event(id: String): Flow<Event?> = events.observeEvent(id)
    fun resources(eventId: String): Flow<List<ResourceItem>> = resources.observeEventResources(eventId)
    fun marketplace(): Flow<List<ResourceItem>> = resources.observeMarketplace()
    fun resource(id: String): Flow<ResourceItem?> = resources.observeResource(id)
    fun passport(resourceId: String): Flow<ResourcePassport?> = passports.observePassport(resourceId)
    suspend fun resourcePhoto(path: String): ByteArray? = when (val result = media.downloadResourcePhoto(path)) {
        is AppResult.Success -> result.value
        is AppResult.Failure -> null
    }
    fun programmes(partnerId: String? = null): Flow<List<CircularProgramme>> = partners.observeProgrammes(partnerId)
    fun transactions(userId: String): Flow<List<CircularTransaction>> = transactions.observeTransactions(userId)
    fun impact(eventId: String): Flow<List<ImpactRecord>> = impact.observeImpact(eventId)
    fun resourceDraft(userId: String, eventId: String): Flow<String?> = preferences.resourceDraft(userId, eventId)

    fun saveResourceDraft(userId: String, eventId: String, draft: String) {
        viewModelScope.launch { preferences.saveResourceDraft(userId, eventId, draft) }
    }

    fun clearResourceDraft(userId: String, eventId: String) {
        viewModelScope.launch { preferences.clearResourceDraft(userId, eventId) }
    }

    fun createEvent(user: User, onSaved: (Event) -> Unit) = launchAction("Event created") {
        val now = System.currentTimeMillis()
        val event = Event(UUID.randomUUID().toString(), user.id, "My circular event", "", "", now, now + 86_400_000L, "ACTIVE", now, now)
        when (val result = events.saveEvent(event)) {
            is AppResult.Success -> { onSaved(result.value); result }
            is AppResult.Failure -> result
        }
    }

    fun saveEvent(event: Event, successMessage: String = "Event saved", onSaved: (Event) -> Unit = {}) = launchAction(successMessage) {
        when (val result = events.saveEvent(event)) {
            is AppResult.Success -> { selectEvent(result.value.id); onSaved(result.value); result }
            is AppResult.Failure -> result
        }
    }

    fun archiveEvent(eventId: String, onArchived: () -> Unit) = launchAction("Event archived") {
        when (val result = events.archiveEvent(eventId)) {
            is AppResult.Success -> { onArchived(); result }
            is AppResult.Failure -> result
        }
    }

    fun selectEvent(eventId: String) {
        viewModelScope.launch { preferences.setLastOpenedEvent(eventId) }
    }

    /**
     * The resource UUID is the stable item identity.  The passport gets its own UUID, while the
     * encoded value only contains the stable resource ID so it can be resolved after a restart.
     */
    fun createPassport(resourceId: String, actorId: String, status: ResourceStatus, createdAt: Long): ResourcePassport {
        val firstEntry = PassportHistoryEntry(
            occurredAt = createdAt,
            action = "Resource created",
            actorId = actorId,
            newStatus = status,
            note = "Resource and digital passport created"
        )
        return ResourcePassport(
            id = UUID.randomUUID().toString(),
            resourceId = resourceId,
            qrPayload = "reevent://passport/$resourceId",
            historyJson = passportHistoryJson.encodeToString(ListSerializer(PassportHistoryEntry.serializer()), listOf(firstEntry)),
            createdAt = createdAt,
            updatedAt = createdAt
        )
    }

    /** Resolves only QR values generated by this app and already available through the repository. */
    suspend fun resolvePassportPayload(payload: String): String? {
        val prefix = "reevent://passport/"
        val resourceId = payload.removePrefix(prefix)
        if (resourceId == payload || runCatching { UUID.fromString(resourceId) }.isFailure) return null
        val passport = passports.observePassport(resourceId).first() ?: return null
        return resourceId.takeIf { passport.qrPayload == payload && resources.observeResource(it).first() != null }
    }

    fun saveResource(resource: ResourceItem, passport: ResourcePassport, photo: Uri?, onSaved: () -> Unit) = launchAction("Resource and passport saved") {
        val resourceWithPhoto = when {
            photo == null -> resource
            else -> when (val upload = media.uploadResourcePhoto(resource.id, photo)) {
                is AppResult.Success -> resource.copy(imageUrls = listOf(upload.value))
                is AppResult.Failure -> return@launchAction upload
            }
        }
        when (val resourceResult = resources.saveResource(resourceWithPhoto)) {
            is AppResult.Failure -> resourceResult
            is AppResult.Success -> when (val passportResult = passports.savePassport(passport)) {
                is AppResult.Success -> { onSaved(); AppResult.Success(Unit) }
                is AppResult.Failure -> passportResult
            }
        }
    }

    fun updateResource(resource: ResourceItem, photo: Uri? = null, onSaved: () -> Unit) = launchAction("Resource updated") {
        val updated = when (photo) {
            null -> resource
            else -> when (val upload = media.uploadResourcePhoto(resource.id, photo)) {
                is AppResult.Success -> resource.copy(imageUrls = listOf(upload.value))
                is AppResult.Failure -> return@launchAction upload
            }
        }
        when (val result = resources.saveResource(updated)) {
            is AppResult.Success -> { onSaved(); result }
            is AppResult.Failure -> result
        }
    }

    /** Saves the displayed status and a matching, timestamped passport history entry together. */
    fun updateResourceStatus(resource: ResourceItem, newStatus: ResourceStatus, onSaved: () -> Unit = {}) = launchAction("Resource status updated") {
        if (resource.status == newStatus) return@launchAction AppResult.Success(Unit)
        val now = System.currentTimeMillis()
        when (val resourceResult = resources.saveResource(resource.copy(status = newStatus, updatedAt = now))) {
            is AppResult.Failure -> resourceResult
            is AppResult.Success -> {
                val passport = passports.observePassport(resource.id).first()
                if (passport == null) {
                    // Older resources created before passports were introduced can still change status.
                    onSaved()
                    AppResult.Success(Unit)
                } else {
                    val history = runCatching {
                        passportHistoryJson.decodeFromString(ListSerializer(PassportHistoryEntry.serializer()), passport.historyJson)
                    }.getOrDefault(emptyList())
                    val entry = PassportHistoryEntry(
                        occurredAt = now,
                        action = "Status updated",
                        actorId = resource.ownerId,
                        previousStatus = resource.status,
                        newStatus = newStatus,
                        note = "Changed from ${resource.status.name} to ${newStatus.name}"
                    )
                    when (val passportResult = passports.savePassport(
                        passport.copy(
                            historyJson = passportHistoryJson.encodeToString(
                                ListSerializer(PassportHistoryEntry.serializer()), history + entry
                            ),
                            updatedAt = now
                        )
                    )) {
                        is AppResult.Success -> { onSaved(); AppResult.Success(Unit) }
                        is AppResult.Failure -> passportResult
                    }
                }
            }
        }
    }

    /**
     * Applies the MVP check-out/return/repair/damage/transfer outcomes through repositories and
     * leaves one matching passport entry for the scanned resource.
     */
    fun applyLifecycleAction(user: User, resource: ResourceItem, action: ResourceLifecycleAction) = launchAction("${action.label} recorded") {
        val isOwnerOrganiser = user.role == UserRole.ORGANIZER && resource.ownerId == user.id
        if (action != ResourceLifecycleAction.RETURN && !isOwnerOrganiser) {
            return@launchAction AppResult.Failure(FailureReason.CONFLICT)
        }
        val now = System.currentTimeMillis()
        val nextStatus = when (action) {
            ResourceLifecycleAction.CHECK_OUT -> ResourceStatus.RESERVED
            ResourceLifecycleAction.RETURN -> ResourceStatus.RECOVERED
            ResourceLifecycleAction.TRANSFER -> ResourceStatus.HANDED_OVER
            ResourceLifecycleAction.MARK_DAMAGED, ResourceLifecycleAction.REQUEST_REPAIR -> resource.status
        }
        val nextCondition = when (action) {
            ResourceLifecycleAction.MARK_DAMAGED, ResourceLifecycleAction.REQUEST_REPAIR -> ResourceCondition.NEEDS_REPAIR
            else -> resource.condition
        }
        val updated = resource.copy(status = nextStatus, condition = nextCondition, updatedAt = now)
        when (val resourceResult = resources.saveResource(updated)) {
            is AppResult.Failure -> resourceResult
            is AppResult.Success -> {
                val transactionResult = when (action) {
                    ResourceLifecycleAction.CHECK_OUT -> saveLifecycleTransaction(user, resource, TransactionType.RESALE, TransactionStatus.PENDING, now)
                    ResourceLifecycleAction.RETURN -> saveLifecycleTransaction(user, resource, TransactionType.RETURN, TransactionStatus.COMPLETED, now)
                    ResourceLifecycleAction.REQUEST_REPAIR -> saveLifecycleTransaction(user, resource, TransactionType.REPAIR, TransactionStatus.PENDING, now)
                    ResourceLifecycleAction.TRANSFER -> saveLifecycleTransaction(user, resource, TransactionType.RESALE, TransactionStatus.COMPLETED, now)
                    ResourceLifecycleAction.MARK_DAMAGED -> AppResult.Success(Unit)
                }
                when (transactionResult) {
                    is AppResult.Failure -> transactionResult
                    is AppResult.Success -> appendPassportHistory(
                        resource = updated,
                        action = action.label,
                        actorId = user.id,
                        previousStatus = resource.status,
                        note = lifecycleNote(resource, updated, action),
                        occurredAt = now
                    )
                }
            }
        }
    }

    /** A successful scan is visible on the passport even if no lifecycle action follows it. */
    fun recordPassportScan(user: User, resourceId: String) = launchAction("QR scan recorded") {
        val resource = resources.observeResource(resourceId).first()
            ?: return@launchAction AppResult.Failure(FailureReason.CONFLICT)
        appendPassportHistory(
            resource = resource,
            action = "QR scanned",
            actorId = user.id,
            previousStatus = resource.status,
            note = "Passport verified from the device camera",
            occurredAt = System.currentTimeMillis()
        )
    }

    fun archiveResource(resourceId: String, onArchived: () -> Unit) = launchAction("Resource archived") {
        when (val result = resources.archiveResource(resourceId)) {
            is AppResult.Success -> { onArchived(); result }
            is AppResult.Failure -> result
        }
    }

    fun createReturn(user: User, resource: ResourceItem) = launchAction("Return request created") {
        val now = System.currentTimeMillis()
        transactions.saveTransaction(
            CircularTransaction(UUID.randomUUID().toString(), resource.eventId, resource.id, user.id, resource.ownerId, null,
                com.reevent.app.core.model.TransactionType.RETURN, TransactionStatus.PENDING, 1, now, now)
        )
    }

    private suspend fun saveLifecycleTransaction(
        user: User,
        resource: ResourceItem,
        type: com.reevent.app.core.model.TransactionType,
        status: TransactionStatus,
        now: Long
    ): AppResult<Unit> = when (val result = transactions.saveTransaction(
        CircularTransaction(
            id = UUID.randomUUID().toString(), eventId = resource.eventId, resourceId = resource.id,
            senderId = user.id, receiverId = resource.ownerId, partnerId = null, type = type,
            status = status, quantity = resource.quantity, createdAt = now, updatedAt = now
        )
    )) {
        is AppResult.Success -> AppResult.Success(Unit)
        is AppResult.Failure -> result
    }

    private suspend fun appendPassportHistory(
        resource: ResourceItem,
        action: String,
        actorId: String,
        previousStatus: ResourceStatus?,
        note: String,
        occurredAt: Long
    ): AppResult<Unit> {
        val passport = passports.observePassport(resource.id).first()
            ?: return AppResult.Failure(FailureReason.CONFLICT)
        val history = runCatching {
            passportHistoryJson.decodeFromString(ListSerializer(PassportHistoryEntry.serializer()), passport.historyJson)
        }.getOrDefault(emptyList())
        val entry = PassportHistoryEntry(
            occurredAt = occurredAt,
            action = action,
            actorId = actorId,
            previousStatus = previousStatus,
            newStatus = resource.status,
            note = note
        )
        return when (val result = passports.savePassport(
            passport.copy(
                historyJson = passportHistoryJson.encodeToString(
                    ListSerializer(PassportHistoryEntry.serializer()), history + entry
                ),
                updatedAt = occurredAt
            )
        )) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Failure -> result
        }
    }

    private fun lifecycleNote(before: ResourceItem, after: ResourceItem, action: ResourceLifecycleAction): String = when (action) {
        ResourceLifecycleAction.MARK_DAMAGED -> "Condition changed from ${before.condition.name} to ${after.condition.name}"
        ResourceLifecycleAction.REQUEST_REPAIR -> "Repair requested; condition changed from ${before.condition.name} to ${after.condition.name}"
        else -> "Status changed from ${before.status.name} to ${after.status.name}"
    }

    fun createProgramme(user: User) = launchAction("Programme added") {
        val now = System.currentTimeMillis()
        partners.saveProgramme(CircularProgramme(UUID.randomUUID().toString(), user.id, "New circular programme", ProgrammeType.REUSE, emptyList(), "", true, now, now))
    }

    fun saveImpact(record: ImpactRecord) = launchAction("Impact record saved") { impact.saveImpact(record) }

    private fun launchAction(success: String, block: suspend () -> AppResult<*>) {
        viewModelScope.launch {
            // A button can receive more than one tap before its loading state is recomposed.
            // Only the first action is allowed to navigate or enqueue a write.
            if (mutableAction.value.loading) return@launch
            mutableAction.value = FeatureActionState(loading = true)
            mutableAction.value = when (val result = block()) {
                is AppResult.Success -> FeatureActionState(notice = success)
                is AppResult.Failure -> FeatureActionState(error = "Unable to complete this action. Check your connection and try again.")
            }
        }
    }
}
