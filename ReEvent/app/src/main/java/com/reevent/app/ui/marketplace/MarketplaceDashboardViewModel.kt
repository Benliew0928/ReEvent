package com.reevent.app.ui.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reevent.app.core.data.AppResult
import com.reevent.app.core.data.CoreSyncRepository
import com.reevent.app.core.data.PartnerRepository
import com.reevent.app.core.data.ResourceRepository
import com.reevent.app.core.data.SyncCommandStatus
import com.reevent.app.core.data.TransactionRepository
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.screens.availableMarketplaceTypes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MarketplaceActionFilter(val label: String, val transactionType: TransactionType?) {
    ALL("All", null), BORROW("Borrow", TransactionType.BORROW), RENT("Rent", TransactionType.RENT),
    BUY("Buy", TransactionType.BUY), DONATE("Donate", TransactionType.DONATE), EXCHANGE("Exchange", TransactionType.EXCHANGE),
}

data class MarketplaceFilters(
    val query: String = "",
    val materialFamily: MaterialFamily? = null,
    val action: MarketplaceActionFilter = MarketplaceActionFilter.ALL,
    val compassPage: Int = 0,
)

data class MarketplaceResourceUi(
    val resource: ResourceItem,
    val isOwner: Boolean,
    val programmeFits: List<String> = emptyList(),
) {
    val programmeFitLabel: String? get() = programmeFits.takeIf(List<String>::isNotEmpty)?.let { "Fits ${it.size} programme${if (it.size == 1) "" else "s"}" }
}

data class MarketplaceUiState(
    val role: UserRole? = null,
    val resources: List<MarketplaceResourceUi> = emptyList(),
    val publishableResources: List<ResourceItem> = emptyList(),
    val transactions: List<CircularTransaction> = emptyList(),
    val transactionResources: Map<String, ResourceItem> = emptyMap(),
    val syncCommands: List<SyncCommandStatus> = emptyList(),
    val filters: MarketplaceFilters = MarketplaceFilters(),
    val resultCount: Int = 0,
    val activityTitle: String = "Marketplace activity",
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
)

private data class MarketplaceBase(
    val marketplace: List<ResourceItem>,
    val owned: List<ResourceItem>,
    val transactions: List<CircularTransaction>,
)

internal data class RefreshState(val loading: Boolean = false, val error: String? = null)

internal fun mapMarketplaceDashboard(
    user: User,
    marketplace: List<ResourceItem>,
    owned: List<ResourceItem>,
    transactions: List<CircularTransaction>,
    programmes: List<CircularProgramme>,
    syncCommands: List<SyncCommandStatus>,
    filters: MarketplaceFilters,
    refresh: RefreshState = RefreshState(),
): MarketplaceUiState {
    val typed = filters.query.trim()
    val visible = marketplace.filter { resource ->
        val searchFields = listOf(resource.title, resource.category, resource.materialFamily.displayLabel, resource.materialDetail.orEmpty())
        (typed.isBlank() || searchFields.any { it.contains(typed, ignoreCase = true) }) &&
            (filters.materialFamily == null || resource.materialFamily == filters.materialFamily) &&
            (filters.action.transactionType == null || filters.action.transactionType in resource.availableMarketplaceTypes())
    }
    val activeProgrammes = programmes.filter(CircularProgramme::active)
    val resourcesById = (marketplace + owned).associateBy(ResourceItem::id)
    return MarketplaceUiState(
        role = user.role,
        resources = visible.map { resource ->
            MarketplaceResourceUi(
                resource = resource,
                isOwner = resource.ownerId == user.id,
                programmeFits = if (user.role == UserRole.PARTNER) activeProgrammes.filter { resource.fits(it) }.map(CircularProgramme::name) else emptyList(),
            )
        },
        publishableResources = if (user.role == UserRole.ORGANIZER) owned.filter { it.marketplaceListing == null && it.syncState == SyncState.SYNCED } else emptyList(),
        transactions = transactions.filterNot { it.status in setOf(TransactionStatus.CANCELLED, TransactionStatus.REJECTED) },
        transactionResources = transactions.mapNotNull { transaction -> resourcesById[transaction.resourceId]?.let { transaction.id to it } }.toMap(),
        syncCommands = syncCommands,
        filters = filters,
        resultCount = visible.size,
        activityTitle = when (user.role) {
            UserRole.ORGANIZER -> "Open marketplace activity"
            UserRole.PARTICIPANT -> "My activity"
            UserRole.PARTNER -> "Programme activity"
            null -> "Marketplace activity"
        },
        isRefreshing = refresh.loading,
        refreshError = refresh.error,
    )
}

private fun ResourceItem.fits(programme: CircularProgramme): Boolean =
    (programme.acceptedMaterialFamilies.isEmpty() || materialFamily in programme.acceptedMaterialFamilies) &&
        (programme.acceptedCategories.isEmpty() || programme.acceptedCategories.any { it.equals(category, true) }) &&
        (programme.acceptedConditions.isEmpty() || condition in programme.acceptedConditions) &&
        (programme.unit == null || programme.unit.equals(unit, true)) &&
        (programme.minimumQuantity == null || quantity >= programme.minimumQuantity) &&
        (programme.maximumQuantity == null || quantity <= programme.maximumQuantity) &&
        (programme.remainingCapacity == null || quantity <= programme.remainingCapacity)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceDashboardViewModel @Inject constructor(
    resources: ResourceRepository,
    transactions: TransactionRepository,
    partners: PartnerRepository,
    private val sync: CoreSyncRepository,
) : ViewModel() {
    private val user = MutableStateFlow<User?>(null)
    private val filters = MutableStateFlow(MarketplaceFilters())
    private val refresh = MutableStateFlow(RefreshState())

    private val data = user.flatMapLatest { current ->
        if (current == null) flowOf(MarketplaceBase(emptyList(), emptyList(), emptyList()))
        else combine(
            resources.observeMarketplace(),
            resources.observeOwnedResources(current.id),
            transactions.observeTransactions(current.id),
            ::MarketplaceBase,
        )
    }
    private val programmes = user.flatMapLatest { current ->
        if (current?.role == UserRole.PARTNER) partners.observeProgrammes(current.id) else flowOf(emptyList())
    }

    val state: StateFlow<MarketplaceUiState> = combine(
        user, data, programmes, sync.observePendingSyncCommands(),
        combine(filters, refresh, ::Pair),
    ) { current, base, programmeRows, commands, controls ->
        if (current == null) MarketplaceUiState()
        else mapMarketplaceDashboard(current, base.marketplace, base.owned, base.transactions, programmeRows, commands, controls.first, controls.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MarketplaceUiState())

    fun load(current: User) {
        if (user.value?.id != current.id) {
            filters.value = MarketplaceFilters()
            user.value = current
        }
        refresh()
    }

    fun refresh() {
        if (refresh.value.loading) return
        viewModelScope.launch {
            refresh.value = RefreshState(loading = true)
            refresh.value = when (sync.refreshAuthorisedData()) {
                is AppResult.Success -> RefreshState()
                is AppResult.Failure -> RefreshState(error = "Marketplace could not refresh. Cached results are still shown.")
            }
        }
    }

    fun setQuery(value: String) { filters.value = filters.value.copy(query = value.take(80)) }
    fun setMaterialFamily(value: MaterialFamily?) {
        filters.value = filters.value.copy(
            materialFamily = value,
            compassPage = value?.ordinal?.div(4) ?: filters.value.compassPage,
        )
    }
    fun setAction(value: MarketplaceActionFilter) { filters.value = filters.value.copy(action = value) }
    fun setCompassPage(value: Int) { filters.value = filters.value.copy(compassPage = value.coerceIn(0, 2)) }
    fun clearFilters() { filters.value = MarketplaceFilters(compassPage = filters.value.compassPage) }
}
