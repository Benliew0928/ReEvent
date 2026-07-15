package com.reevent.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.EventRepository
import com.reevent.app.core.data.ImpactRepository
import com.reevent.app.core.data.MediaRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.PassportRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FeatureActionState(val loading: Boolean = false, val error: String? = null, val notice: String? = null)

@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val events: EventRepository,
    private val resources: ResourceRepository,
    private val passports: PassportRepository,
    private val partners: PartnerRepository,
    private val transactions: TransactionRepository,
    private val impact: ImpactRepository,
    private val sync: CoreSyncRepository,
    private val media: MediaRepository
) : ViewModel() {
    private val mutableAction = MutableStateFlow(FeatureActionState())
    val action: StateFlow<FeatureActionState> = mutableAction

    fun refresh() = launchAction("Data is up to date") { sync.refreshAuthorisedData() }
    fun events(ownerId: String): Flow<List<Event>> = events.observeOwnedEvents(ownerId)
    fun resources(eventId: String): Flow<List<ResourceItem>> = resources.observeEventResources(eventId)
    fun marketplace(): Flow<List<ResourceItem>> = resources.observeMarketplace()
    fun resource(id: String): Flow<ResourceItem?> = resources.observeResource(id)
    fun passport(resourceId: String): Flow<ResourcePassport?> = passports.observePassport(resourceId)
    fun programmes(partnerId: String? = null): Flow<List<CircularProgramme>> = partners.observeProgrammes(partnerId)
    fun transactions(userId: String): Flow<List<CircularTransaction>> = transactions.observeTransactions(userId)
    fun impact(eventId: String): Flow<List<ImpactRecord>> = impact.observeImpact(eventId)

    fun createEvent(user: User, onSaved: (Event) -> Unit) = launchAction("Event created") {
        val now = System.currentTimeMillis()
        val event = Event(UUID.randomUUID().toString(), user.id, "My circular event", "", "", now, now + 86_400_000L, "ACTIVE", now, now)
        when (val result = events.saveEvent(event)) {
            is AppResult.Success -> { onSaved(result.value); result }
            is AppResult.Failure -> result
        }
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

    fun createReturn(user: User, resource: ResourceItem) = launchAction("Return request created") {
        val now = System.currentTimeMillis()
        transactions.saveTransaction(
            CircularTransaction(UUID.randomUUID().toString(), resource.eventId, resource.id, user.id, resource.ownerId, null,
                com.reevent.app.core.model.TransactionType.RETURN, TransactionStatus.PENDING, 1, now, now)
        )
    }

    fun createProgramme(user: User) = launchAction("Programme added") {
        val now = System.currentTimeMillis()
        partners.saveProgramme(CircularProgramme(UUID.randomUUID().toString(), user.id, "New circular programme", ProgrammeType.REUSE, emptyList(), "", true, now, now))
    }

    fun saveImpact(record: ImpactRecord) = launchAction("Impact record saved") { impact.saveImpact(record) }

    private fun launchAction(success: String, block: suspend () -> AppResult<*>) {
        viewModelScope.launch {
        mutableAction.value = FeatureActionState(loading = true)
        mutableAction.value = when (val result = block()) {
            is AppResult.Success -> FeatureActionState(notice = success)
            is AppResult.Failure -> FeatureActionState(error = "Unable to complete this action. Check your connection and try again.")
        }
        }
    }
}
