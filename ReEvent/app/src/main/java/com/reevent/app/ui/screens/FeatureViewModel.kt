package com.reevent.app.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reevent.app.BuildConfig
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.EventRepository
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.data.GeocodingRepository
import com.reevent.app.core.data.ImpactRepository
import com.reevent.app.core.data.MarketplaceListingRepository
import com.reevent.app.core.data.MediaRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.PassportRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.SyncCommandStatus
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.data.TransactionWorkflow
import com.reevent.app.core.data.blocksResourceArchive
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.PlaceSuggestion
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.DiscoverableEvent
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.feature.passports.PassportQrPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FeatureActionState(
    val loading: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
)

/** A scan never bypasses RLS: it is malformed, unavailable to this account/cache, or verified. */
sealed interface PassportScanResolution {
    data class Verified(
        val resourceId: String,
    ) : PassportScanResolution

    data object Unavailable : PassportScanResolution

    data class Malformed(
        val message: String,
    ) : PassportScanResolution
}

/** Actions available after an authenticated ReEvent QR scan. */
enum class ResourceLifecycleAction(
    val label: String,
) {
    CHECK_OUT("Check out item"),
    RETURN("Record return"),
    MARK_DAMAGED("Mark damaged"),
    REQUEST_REPAIR("Request repair"),
    TRANSFER("Record transfer"),
}

@HiltViewModel
class FeatureViewModel
    @Inject
    constructor(
        private val events: EventRepository,
        private val resources: ResourceRepository,
        private val passports: PassportRepository,
        private val partners: PartnerRepository,
        private val transactions: TransactionRepository,
        private val impact: ImpactRepository,
        private val marketplaceListings: MarketplaceListingRepository,
        private val sync: CoreSyncRepository,
        private val media: MediaRepository,
        private val preferences: AppPreferences,
        private val geocoding: GeocodingRepository,
    ) : ViewModel() {
        private val mutableAction = MutableStateFlow(FeatureActionState())
        val action: StateFlow<FeatureActionState> = mutableAction
        val selectedEventId: Flow<String?> = preferences.lastOpenedEventId

        /** Initial workspace refresh is best-effort: an offline server must not look like a failed user action. */
        fun refresh() {
            viewModelScope.launch { sync.refreshAuthorisedData() }
        }

        /** A verified Android App Link waits for one authoritative refresh before cache lookup. */
        suspend fun refreshForPassportLink(): AppResult<Unit> = sync.refreshAuthorisedData()

        fun events(ownerId: String): Flow<List<Event>> = events.observeOwnedEvents(ownerId)

        fun event(id: String): Flow<Event?> = events.observeEvent(id)

        fun discoverableEvents(): Flow<List<DiscoverableEvent>> = events.observeDiscoverableEvents()

        fun discoverableEvent(id: String): Flow<DiscoverableEvent?> = events.observeDiscoverableEvent(id)

        fun refreshDiscoverableEvents() = launchAction("Events refreshed") { events.refreshDiscoverableEvents() }

        fun resources(eventId: String): Flow<List<ResourceItem>> = resources.observeEventResources(eventId)

        fun ownedResources(ownerId: String): Flow<List<ResourceItem>> = resources.observeOwnedResources(ownerId)

        fun marketplace(): Flow<List<ResourceItem>> = resources.observeMarketplace()

        fun resource(id: String): Flow<ResourceItem?> = resources.observeResource(id)

        suspend fun searchPlaces(query: String, proximity: GeoLocation? = null): AppResult<List<PlaceSuggestion>> =
            geocoding.search(query, proximity)

        suspend fun reversePlace(location: GeoLocation): AppResult<PlaceSuggestion> = geocoding.reverse(location)

        fun passport(resourceId: String): Flow<ResourcePassport?> = passports.observePassport(resourceId)

        suspend fun resourcePhoto(path: String): ByteArray? =
            when (val result = media.downloadResourcePhoto(path)) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> null
            }

        fun programmes(partnerId: String? = null): Flow<List<CircularProgramme>> = partners.observeProgrammes(partnerId)

        fun legacyProgrammes(partnerId: String): Flow<List<com.reevent.app.core.model.LegacyProgrammeDraft>> =
            partners.observeLegacyProgrammeDrafts(partnerId)

        fun transactions(userId: String): Flow<List<CircularTransaction>> = transactions.observeTransactions(userId)

        fun eventTransactions(eventId: String): Flow<List<CircularTransaction>> = transactions.observeEventTransactions(eventId)

        fun impact(eventId: String): Flow<List<ImpactRecord>> = impact.observeImpact(eventId)

        fun pendingSyncCommands(): Flow<List<SyncCommandStatus>> = sync.observePendingSyncCommands()

        fun resourceDraft(
            userId: String,
            eventId: String,
        ): Flow<String?> = preferences.resourceDraft(userId, eventId)

        fun retryPendingSync() = launchAction("Sync retry requested") { sync.retryPendingSync() }

        fun saveResourceDraft(
            userId: String,
            eventId: String,
            draft: String,
        ) {
            viewModelScope.launch { preferences.saveResourceDraft(userId, eventId, draft) }
        }

        fun clearResourceDraft(
            userId: String,
            eventId: String,
        ) {
            viewModelScope.launch { preferences.clearResourceDraft(userId, eventId) }
        }

        fun createEvent(
            user: User,
            onSaved: (Event) -> Unit,
        ) = launchAction("Event created") {
            val now = System.currentTimeMillis()
            val event = Event(UUID.randomUUID().toString(), user.id, "My circular event", "", "", now, now + 86_400_000L, "DRAFT", now, now)
            when (val result = events.saveEvent(event)) {
                is AppResult.Success -> {
                    onSaved(result.value)
                    result
                }

                is AppResult.Failure -> {
                    result
                }
            }
        }

        fun saveEvent(
            event: Event,
            successMessage: String = "Event saved",
            onSaved: (Event) -> Unit = {},
        ) = launchAction(successMessage) {
            when (val result = events.saveEvent(event)) {
                is AppResult.Success -> {
                    selectEvent(result.value.id)
                    onSaved(result.value)
                    result
                }

                is AppResult.Failure -> {
                    result
                }
            }
        }

        fun publishEvent(
            eventId: String,
            onPublished: (Event) -> Unit = {},
        ) = launchAction("Event published") {
            when (val result = events.publishEvent(eventId)) {
                is AppResult.Success -> {
                    onPublished(result.value)
                    result
                }
                is AppResult.Failure -> result
            }
        }

        fun completeEvent(
            eventId: String,
            onCompleted: (Event) -> Unit = {},
        ) = launchAction("Event completed") {
            when (val result = events.completeEvent(eventId)) {
                is AppResult.Success -> {
                    onCompleted(result.value)
                    result
                }
                is AppResult.Failure -> result
            }
        }

        fun archiveEvent(
            eventId: String,
            onArchived: () -> Unit,
        ) = launchAction("Event archived") {
            when (val result = events.archiveEvent(eventId)) {
                is AppResult.Success -> {
                    onArchived()
                    result
                }

                is AppResult.Failure -> {
                    result
                }
            }
        }

        fun selectEvent(eventId: String) {
            viewModelScope.launch { preferences.setLastOpenedEvent(eventId) }
        }

        /** Resolves only canonical server-issued QR payloads already available through the authorised cache. */
        suspend fun resolvePassportPayload(
            payload: String,
            userId: String,
        ): PassportScanResolution {
            when (val validation = PassportQrPayload.validate(payload, BuildConfig.PUBLIC_BASE_URL)) {
                is PassportQrPayload.Validation.Invalid -> return PassportScanResolution.Malformed(validation.message)

                PassportQrPayload.Validation.Legacy,
                is PassportQrPayload.Validation.Canonical,
                -> Unit
            }
            // A participant's active handover is normally private and therefore absent from the
            // marketplace projection. Include their transaction resources before public listings.
            val privateTransactionResources =
                transactions.observeTransactions(userId).first().mapNotNull { transaction ->
                    resources.observeResource(transaction.resourceId).first()
                }
            val ownedResources =
                events.observeOwnedEvents(userId).first().flatMap { event ->
                    resources.observeEventResources(event.id).first()
                }
            val candidates = privateTransactionResources + ownedResources + resources.observeMarketplace().first()
            val resourceId =
                candidates.distinctBy(ResourceItem::id).firstNotNullOfOrNull { resource ->
                    val passport = passports.observePassport(resource.id).first()
                    resource.id.takeIf { passport?.qrPayload == payload }
                }
            return resourceId?.let(PassportScanResolution::Verified) ?: PassportScanResolution.Unavailable
        }

        fun saveResource(
            resource: ResourceItem,
            photo: Uri?,
            onSaved: () -> Unit,
        ) = launchAction("Resource saved; passport will be issued by the server") {
            when (val resourceResult = resources.saveResource(resource)) {
                is AppResult.Failure -> {
                    resourceResult
                }

                is AppResult.Success -> {
                    if (photo != null) {
                        when (val flushed = sync.syncPendingNow()) {
                            is AppResult.Failure -> return@launchAction flushed
                            is AppResult.Success -> Unit
                        }
                        when (val upload = media.uploadResourcePhoto(resource.id, photo)) {
                            is AppResult.Failure -> return@launchAction upload
                            is AppResult.Success -> sync.refreshAuthorisedData()
                        }
                    }
                    onSaved()
                    AppResult.Success(Unit)
                }
            }
        }

        fun updateResource(
            resource: ResourceItem,
            photo: Uri? = null,
            onSaved: () -> Unit,
        ) = launchAction("Resource updated") {
            when (val result = resources.saveResource(resource)) {
                is AppResult.Failure -> {
                    result
                }

                is AppResult.Success -> {
                    if (photo != null) {
                        when (val flushed = sync.syncPendingNow()) {
                            is AppResult.Failure -> return@launchAction flushed
                            is AppResult.Success -> Unit
                        }
                        when (val upload = media.uploadResourcePhoto(resource.id, photo)) {
                            is AppResult.Failure -> return@launchAction upload
                            is AppResult.Success -> sync.refreshAuthorisedData()
                        }
                    }
                    onSaved()
                    result
                }
            }
        }

        /** Scanner actions select an existing server transaction; a QR token never grants authority. */
        fun applyLifecycleAction(
            user: User,
            resource: ResourceItem,
            action: ResourceLifecycleAction,
        ) = launchAction("${action.label} recorded") {
            val transaction =
                transactions.observeTransactions(user.id).first().firstOrNull { it.resourceId == resource.id }
                    ?: return@launchAction AppResult.Failure(FailureReason.CONFLICT)
            when (action) {
                ResourceLifecycleAction.CHECK_OUT -> {
                    transactions.beginHandover(transaction.id)
                }

                ResourceLifecycleAction.RETURN -> {
                    when (transaction.status) {
                        TransactionStatus.ACTIVE -> transactions.beginReturn(transaction.id)
                        TransactionStatus.RETURN_IN_PROGRESS -> transactions.confirmReturn(transaction.id)
                        else -> AppResult.Failure(FailureReason.CONFLICT)
                    }
                }

                ResourceLifecycleAction.MARK_DAMAGED -> {
                    resources.saveResource(
                        resource.copy(condition = ResourceCondition.NEEDS_REPAIR, updatedAt = System.currentTimeMillis()),
                    )
                }

                ResourceLifecycleAction.REQUEST_REPAIR,
                ResourceLifecycleAction.TRANSFER,
                -> {
                    AppResult.Failure(FailureReason.CONFLICT)
                }
            }
        }

        /** Scanning is read-only; immutable passport history records verified lifecycle effects only. */
        fun recordPassportScan(
            user: User,
            resourceId: String,
        ) = launchAction("QR scan recorded") {
            if (resources.observeResource(resourceId).first() == null) {
                AppResult.Failure(FailureReason.CONFLICT)
            } else {
                AppResult.Success(Unit)
            }
        }

        fun archiveResource(
            resourceId: String,
            eventTransactions: List<CircularTransaction>,
            onArchived: () -> Unit,
        ) = launchAction("Resource archived") {
            // The screen should already disable this action. Check again here because a cached
            // event can change while the confirmation dialog is open; the server repeats the rule.
            if (eventTransactions.any { it.blocksResourceArchive(resourceId) }) {
                return@launchAction AppResult.Failure(FailureReason.CONFLICT)
            }
            when (val result = resources.archiveResource(resourceId)) {
                is AppResult.Success -> {
                    onArchived()
                    result
                }

                is AppResult.Failure -> {
                    result
                }
            }
        }

        fun createReturn(
            user: User,
            resource: ResourceItem,
        ) = launchAction("Return request created") {
            val transaction =
                transactions.observeTransactions(user.id).first().firstOrNull {
                    it.resourceId == resource.id && it.status == TransactionStatus.ACTIVE &&
                        it.type in setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR)
                } ?: return@launchAction AppResult.Failure(FailureReason.CONFLICT)
            transactions.beginReturn(transaction.id)
        }

        fun requestMarketplaceResource(
            user: User,
            resource: ResourceItem,
            type: TransactionType,
            quantity: Double,
        ) = launchAction("Marketplace request created") {
            TransactionWorkflow
                .validateMarketplaceListingRequest(
                    user.id,
                    resource,
                    type,
                    quantity,
                )?.let { return@launchAction AppResult.Failure(it) }
            transactions.requestMarketplace(resource.id, type, quantity)
        }

        fun publishMarketplaceListing(
            user: User,
            resource: ResourceItem,
            draft: MarketplaceListingDraft,
            onPublished: () -> Unit,
        ) = launchAction("Marketplace listing published") {
            if (user.role != UserRole.ORGANIZER || resource.ownerId != user.id) {
                return@launchAction AppResult.Failure(FailureReason.CONFLICT)
            }
            when (val result = marketplaceListings.publishListing(resource, draft)) {
                is AppResult.Success -> {
                    onPublished()
                    result
                }

                is AppResult.Failure -> {
                    result
                }
            }
        }

        fun createPartnerHandover(
            user: User,
            resource: ResourceItem,
            programme: CircularProgramme,
        ) = launchAction("Partner handover request created") {
            TransactionWorkflow.validatePartnerHandover(user.id, resource, programme)?.let {
                return@launchAction AppResult.Failure(it)
            }
            transactions.requestProgramme(programme.id, resource.id, resource.quantity)
        }

        fun approveTransaction(
            user: User,
            transaction: CircularTransaction,
        ) = launchAction("Request approved") {
            if (!TransactionWorkflow.canApprove(user.id, transaction)) {
                AppResult.Failure(FailureReason.CONFLICT)
            } else {
                transactions.approve(transaction.id)
            }
        }

        fun cancelTransaction(
            user: User,
            transaction: CircularTransaction,
        ) = launchAction("Request cancelled") {
            if (!TransactionWorkflow.canCancel(user.id, transaction)) {
                AppResult.Failure(FailureReason.CONFLICT)
            } else {
                transactions.cancel(transaction.id, "Cancelled by an authorised transaction actor")
            }
        }

        fun moveTransactionInTransit(
            user: User,
            transaction: CircularTransaction,
        ) = launchAction("Handover marked in transit") {
            if (!TransactionWorkflow.canBeginHandover(user.id, transaction)) {
                AppResult.Failure(FailureReason.CONFLICT)
            } else {
                transactions.beginHandover(transaction.id)
            }
        }

        fun completeTransaction(
            user: User,
            transaction: CircularTransaction,
        ) = launchAction("Transaction completed") {
            when {
                TransactionWorkflow.canConfirmReceipt(user.id, transaction) -> transactions.confirmReceipt(transaction.id)
                TransactionWorkflow.canBeginReturn(user.id, transaction) -> transactions.beginReturn(transaction.id)
                TransactionWorkflow.canConfirmReturn(user.id, transaction) -> transactions.confirmReturn(transaction.id)
                else -> AppResult.Failure(FailureReason.CONFLICT)
            }
        }

        fun createProgramme(user: User) =
            launchAction("Programme added") {
                val now = System.currentTimeMillis()
                partners.saveProgramme(
                    CircularProgramme(
                        UUID.randomUUID().toString(),
                        user.id,
                        "New circular programme",
                        ProgrammeType.REPAIR,
                        emptySet(),
                        "",
                        false,
                        now,
                        now,
                    ),
                )
            }

        fun saveProgramme(
            user: User,
            existing: CircularProgramme?,
            name: String,
            type: ProgrammeType,
            acceptedMaterialFamilies: Set<com.reevent.app.core.model.MaterialFamily>,
            acceptedCategories: List<String>,
            acceptedConditions: Set<com.reevent.app.core.model.ResourceCondition>,
            minimumQuantity: Double?,
            maximumQuantity: Double?,
            unit: String?,
            remainingCapacity: Double?,
            pickupAvailable: Boolean,
            coinDirection: com.reevent.app.core.model.CoinDirection,
            unitCoinAmount: Long?,
            geoLocation: GeoLocation?,
            processingMethod: String,
            terms: String,
            active: Boolean,
            legacyDraftId: String? = null,
        ) = launchAction(if (existing == null) "Programme added" else "Programme updated") {
            if (user.role != UserRole.PARTNER) {
                return@launchAction AppResult.Failure(FailureReason.VALIDATION)
            }
            val now = System.currentTimeMillis()
            val normalizedCategories = acceptedCategories.map(String::trim).filter(String::isNotBlank).distinctBy(String::lowercase)
            val programme =
                existing?.copy(
                    name = name.trim(),
                    type = type,
                    acceptedMaterialFamilies = acceptedMaterialFamilies,
                    location = geoLocation?.displayAddress.orEmpty(),
                    active = active,
                    updatedAt = now,
                    acceptedCategories = normalizedCategories,
                    acceptedConditions = acceptedConditions,
                    minimumQuantity = minimumQuantity,
                    maximumQuantity = maximumQuantity,
                    unit = unit?.trim()?.takeIf(String::isNotBlank),
                    remainingCapacity = remainingCapacity,
                    pickupAvailable = pickupAvailable,
                    coinDirection = coinDirection,
                    unitCoinAmount = unitCoinAmount,
                    geoLocation = geoLocation,
                    processingMethod = processingMethod.trim(),
                    terms = terms.trim(),
                ) ?: CircularProgramme(
                    id = UUID.randomUUID().toString(),
                    partnerId = user.id,
                    name = name.trim(),
                    type = type,
                    acceptedMaterialFamilies = acceptedMaterialFamilies,
                    location = geoLocation?.displayAddress.orEmpty(),
                    active = active,
                    createdAt = now,
                    updatedAt = now,
                    acceptedCategories = normalizedCategories,
                    acceptedConditions = acceptedConditions,
                    minimumQuantity = minimumQuantity,
                    maximumQuantity = maximumQuantity,
                    unit = unit?.trim()?.takeIf(String::isNotBlank),
                    remainingCapacity = remainingCapacity,
                    pickupAvailable = pickupAvailable,
                    coinDirection = coinDirection,
                    unitCoinAmount = unitCoinAmount,
                    geoLocation = geoLocation,
                    processingMethod = processingMethod.trim(),
                    terms = terms.trim(),
                )
            if (programme.partnerId != user.id) return@launchAction AppResult.Failure(FailureReason.CONFLICT)
            if (!programme.hasValidProgrammeRules()) return@launchAction AppResult.Failure(FailureReason.VALIDATION)
            if (programme.active && !programme.isActivationReady()) return@launchAction AppResult.Failure(FailureReason.VALIDATION)
            val saved = partners.saveProgramme(programme)
            if (saved is AppResult.Success && legacyDraftId != null) partners.discardLegacyProgrammeDraft(legacyDraftId)
            saved
        }

        fun deactivateProgramme(
            user: User,
            programme: CircularProgramme,
        ) = launchAction("Programme deactivated") {
            if (user.role != UserRole.PARTNER || programme.partnerId != user.id) {
                return@launchAction AppResult.Failure(FailureReason.CONFLICT)
            }
            partners.saveProgramme(programme.copy(active = false, updatedAt = System.currentTimeMillis()))
        }

        private fun launchAction(
            success: String,
            block: suspend () -> AppResult<*>,
        ) {
            viewModelScope.launch {
                // A button can receive more than one tap before its loading state is recomposed.
                // Only the first action is allowed to navigate or enqueue a write.
                if (mutableAction.value.loading) return@launch
                mutableAction.value = FeatureActionState(loading = true)
                mutableAction.value =
                    when (val result = block()) {
                        is AppResult.Success -> {
                            FeatureActionState(notice = success)
                        }

                        is AppResult.Failure -> {
                            FeatureActionState(
                                error = actionErrorText(result.reason),
                            )
                        }
                    }
            }
        }

        private fun actionErrorText(reason: FailureReason): String = when (reason) {
            FailureReason.OFFLINE -> "The connection timed out. Check your internet connection and try again."
            FailureReason.CONFIGURATION -> "This build is not connected to the ReEvent server."
            FailureReason.UNAUTHENTICATED -> "Your session has expired. Sign in again and retry."
            FailureReason.VALIDATION -> "Some of the entered details are not valid. Review them and try again."
            FailureReason.CONFLICT -> "This change is no longer allowed because the server state has changed. Refresh and try again."
            FailureReason.SERVER -> "The server could not process this change. Check the details and try again."
            else -> "Unable to complete this action. Please try again."
        }
    }
