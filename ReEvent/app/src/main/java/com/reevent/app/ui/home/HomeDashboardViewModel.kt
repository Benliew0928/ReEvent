package com.reevent.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.EventRepository
import com.reevent.app.core.data.ImpactRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.PassportRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeDashboardViewModel @Inject constructor(
    private val events: EventRepository,
    private val resources: ResourceRepository,
    private val passports: PassportRepository,
    private val partners: PartnerRepository,
    private val transactions: TransactionRepository,
    private val impact: ImpactRepository,
    private val sync: CoreSyncRepository,
    private val preferences: AppPreferences,
) : ViewModel() {
    private val refreshState = MutableStateFlow(HomeRefreshState())
    private val participantFilter = MutableStateFlow(ParticipantActivityFilter.ALL)

    fun organizer(user: User): Flow<HomeDashboardUiState> =
        combine(events.observeOwnedEvents(user.id), preferences.lastOpenedEventId) { eventList, persistedId ->
            val selected = HomeDashboardMappers.selectEvent(eventList, persistedId)
            if (selected != null && selected.id != persistedId) persistEventSelection(selected.id)
            eventList to selected
        }.flatMapLatest { (eventList, selected) ->
            if (selected == null) {
                refreshState.combineWith { refresh ->
                    HomeDashboardMappers.organizer(user, eventList, null, emptyList(), emptyList(), emptyList(), refresh)
                }
            } else {
                combine(
                    resources.observeEventResources(selected.id),
                    transactions.observeEventTransactions(selected.id),
                    impact.observeImpact(selected.id),
                    refreshState,
                ) { eventResources, eventTransactions, eventImpact, refresh ->
                    HomeDashboardMappers.organizer(
                        user,
                        eventList,
                        selected,
                        eventResources,
                        eventTransactions,
                        eventImpact,
                        refresh,
                    )
                }
            }
        }

    fun participant(user: User): Flow<HomeDashboardUiState> =
        combine(transactions.observeTransactions(user.id), participantFilter) { activity, filter -> activity to filter }
            .flatMapLatest { (activity, filter) ->
                val resourceIds = activity.map(CircularTransaction::resourceId).toSet()
                combine(
                    observeResources(resourceIds),
                    observePassports(resourceIds),
                    refreshState,
                ) { activityResources, activityPassports, refresh ->
                    HomeDashboardMappers.participant(
                        user,
                        filter,
                        activity,
                        activityResources,
                        activityPassports,
                        refresh,
                    )
                }
            }

    fun partner(user: User): Flow<HomeDashboardUiState> =
        combine(partners.observeProgrammes(user.id), preferences.lastOpenedProgrammeId) { programmes, persistedId ->
            val selected = HomeDashboardMappers.selectProgramme(programmes, persistedId)
            if (selected != null && selected.id != persistedId) persistProgrammeSelection(selected.id)
            programmes to selected
        }.flatMapLatest { (programmes, selected) ->
            transactions.observeTransactions(user.id).flatMapLatest { activity ->
                val scoped = if (selected == null) emptyList() else activity.filter { it.programmeId == selected.id }
                val resourceIds = scoped.map(CircularTransaction::resourceId).toSet()
                combine(observeResources(resourceIds), refreshState) { activityResources, refresh ->
                    HomeDashboardMappers.partner(
                        user,
                        programmes,
                        selected,
                        activity,
                        activityResources,
                        refresh,
                    )
                }
            }
        }

    fun partnerPassportResources(user: User): Flow<List<ResourceItem>> =
        transactions.observeTransactions(user.id).flatMapLatest { activity ->
            observeResources(
                activity.filter { it.partnerId == user.id }.map(CircularTransaction::resourceId).toSet(),
            )
        }

    fun partnerPassports(user: User): Flow<List<ResourcePassport>> =
        transactions.observeTransactions(user.id).flatMapLatest { activity ->
            observePassports(
                activity.filter { it.partnerId == user.id }.map(CircularTransaction::resourceId).toSet(),
            )
        }

    fun selectEvent(eventId: String) = persistEventSelection(eventId)

    fun selectProgramme(programmeId: String) = persistProgrammeSelection(programmeId)

    fun selectParticipantFilter(id: String) {
        participantFilter.value = ParticipantActivityFilter.entries.firstOrNull { it.name == id }
            ?: ParticipantActivityFilter.ALL
    }

    fun refresh() {
        if (refreshState.value.isRefreshing) return
        viewModelScope.launch {
            refreshState.value = HomeRefreshState(isRefreshing = true)
            refreshState.value = when (sync.refreshAuthorisedData()) {
                is AppResult.Success -> HomeRefreshState()
                is AppResult.Failure -> HomeRefreshState(error = "Couldn’t refresh. Showing saved data.")
            }
        }
    }

    fun retry() = refresh()

    private fun observeResources(ids: Set<String>): Flow<List<ResourceItem>> =
        if (ids.isEmpty()) flowOf(emptyList()) else resources.observeResources(ids)

    private fun observePassports(ids: Set<String>): Flow<List<ResourcePassport>> =
        if (ids.isEmpty()) flowOf(emptyList()) else passports.observePassports(ids)

    private fun persistEventSelection(id: String) {
        viewModelScope.launch { preferences.setLastOpenedEvent(id) }
    }

    private fun persistProgrammeSelection(id: String) {
        viewModelScope.launch { preferences.setLastOpenedProgramme(id) }
    }
}

private fun <T> Flow<HomeRefreshState>.combineWith(transform: (HomeRefreshState) -> T): Flow<T> =
    map(transform)
