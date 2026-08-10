package com.reevent.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.R
import com.reevent.app.BuildConfig
import com.reevent.app.core.data.ProgrammeMatcher
import com.reevent.app.core.data.MarketplaceListingDraftRules
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.data.SyncCommandStatus
import com.reevent.app.core.data.TransactionLifecycleCardAction
import com.reevent.app.core.data.TransactionLifecyclePresentationRules
import com.reevent.app.core.data.TransactionLifecycleSyncFeedback
import com.reevent.app.core.data.TransactionWorkflow
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.PassportHistoryEntry
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.feature.events.EventFormValidation
import com.reevent.app.feature.impact.ImpactCalculator
import com.reevent.app.feature.impact.ImpactDashboardState
import com.reevent.app.feature.passports.PassportQrPayload
import com.reevent.app.feature.passports.PassportViewerAccessPolicy
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.PartnerMatch
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.ResourceItem as VisualResourceItem
import com.reevent.app.ui.ResourceTone
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.ResourceCard
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventMuted
import com.reevent.app.ui.theme.ReEventPaper
import java.util.Locale
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.flowOf

/**
 * Bridges the original visual composables to the repository-backed state. Empty repositories
 * deliberately render their original layout with empty states instead of falling back to MockData.
 */
@Composable
fun OrganizerHomeVisualScreen(
    user: User,
    onAddResource: (String) -> Unit,
    onPassport: (String) -> Unit,
    onImpact: (String) -> Unit,
    onMarketplace: () -> Unit,
    onPartnerMap: () -> Unit,
    onManageEvents: () -> Unit,
    onProfile: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    val selectedEventId by viewModel.selectedEventId.collectAsState(null)
    val event = events.firstOrNull { it.id == selectedEventId } ?: events.firstOrNull()
    LaunchedEffect(event?.id) { event?.id?.let(viewModel::selectEvent) }
    val resources by (event?.let { viewModel.resources(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val impact by (event?.let { viewModel.impact(it.id) } ?: flowOf(emptyList())).collectAsState(emptyList())
    val visualResources = resources.map { it.toVisualResource(user, event?.name, event?.venue) }

    HomeScreen(
        onNavigate = { screen ->
            when (screen) {
                ReEventScreen.AddResource -> event?.let { onAddResource(it.id) }
                    ?: onManageEvents()
                ReEventScreen.AiMatch -> visualResources.firstOrNull()?.id?.let(onPassport)
                ReEventScreen.PartnerMap -> onPartnerMap()
                // Keep the original impact board reachable even before an event has synced data.
                ReEventScreen.Impact -> onImpact(event?.id.orEmpty())
                ReEventScreen.Marketplace -> onMarketplace()
                ReEventScreen.Profile -> onProfile()
                else -> Unit
            }
        },
        title = event?.name ?: "Your event dashboard",
        subtitle = when {
            event == null -> "Create an event to begin tracking recovery"
            event.venue.isNotBlank() -> "Live recovery board • ${event.venue}"
            else -> "Event workspace ready for resources and recovery"
        },
        metrics = resources.toDashboardMetrics(impact),
        resources = visualResources.take(2),
        recoverySteps = resources.toRecoverySteps(impact),
        hasEvent = event != null,
        onManageEvents = onManageEvents,
        onAddResource = event?.let { { onAddResource(it.id) } },
        onResourceClick = { it.id?.let(onPassport) }
    )
}

/** A real organizer tab: it resolves or creates an event before displaying the live form. */
@Composable
fun OrganizerAddResourceVisualScreen(
    user: User,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by produceState<List<com.reevent.app.core.model.Event>?>(initialValue = null, user.id) {
        viewModel.events(user.id).collect { value = it }
    }
    val action by viewModel.action.collectAsState()
    val event = events?.firstOrNull()

    if (event == null) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (events == null) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading your organizer event...",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text("Create an event before adding a resource.", style = MaterialTheme.typography.bodyLarge)
                    Button(
                        onClick = { viewModel.createEvent(user) { } },
                        modifier = Modifier.padding(top = 16.dp),
                        enabled = !action.loading
                    ) { Text("Create event") }
                    action.error?.let { Text(it, modifier = Modifier.padding(top = 12.dp)) }
                }
            }
        }
    } else {
        AddResourceLiveScreen(
            user = user,
            eventId = event.id,
            onSaved = onSaved,
            onBack = onBack,
            onNavigate = onNavigate,
            viewModel = viewModel
        )
    }
}

@Composable
fun OrganizerImpactVisualScreen(
    user: User,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    val selectedEventId by viewModel.selectedEventId.collectAsState(null)
    val selectedEvent = events.firstOrNull { it.id == selectedEventId } ?: events.firstOrNull()
    LaunchedEffect(selectedEventId, selectedEvent?.id) {
        if (selectedEventId == null || events.none { it.id == selectedEventId }) selectedEvent?.let { viewModel.selectEvent(it.id) }
    }
    ImpactVisualScreen(
        eventId = selectedEvent?.id.orEmpty(),
        onNavigate = onNavigate,
        selectedScope = selectedEvent?.let { ImpactEventScope(it.id, it.name) },
        scopes = events.map { ImpactEventScope(it.id, it.name) },
        onScopeSelected = viewModel::selectEvent,
        viewModel = viewModel
    )
}

@Composable
fun ImpactVisualScreen(
    eventId: String,
    onNavigate: (ReEventScreen) -> Unit,
    selectedScope: ImpactEventScope? = null,
    scopes: List<ImpactEventScope> = emptyList(),
    onScopeSelected: (String) -> Unit = {},
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val resources by viewModel.resources(eventId).collectAsState(emptyList())
    val transactions by viewModel.eventTransactions(eventId).collectAsState(emptyList())
    val records by viewModel.impact(eventId).collectAsState(emptyList())
    val summary = remember(resources, transactions, records) {
        ImpactCalculator.summarize(resources, transactions, records)
    }
    val rate = summary.recoveryRate

    ImpactScreen(
        onNavigate = onNavigate,
        metrics = summary.toImpactMetrics(),
        recoveryRate = rate,
        recoveryLabel = rate?.let { "${(it * 100).toInt()}%" } ?: "—",
        chartValues = summary.chartValues,
        badge = summary.badge,
        unavailableEstimateReason = summary.unavailableEstimateReason,
        latestRecord = summary.latestRecord,
        selectedScope = selectedScope,
        scopes = scopes,
        onScopeSelected = onScopeSelected
    )
}

@Composable
fun MarketplaceVisualScreen(
    user: User,
    onPassport: (String) -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val resources by viewModel.marketplace().collectAsState(emptyList())
    val ownedResources by (if (user.role == com.reevent.app.core.model.UserRole.ORGANIZER) {
        viewModel.ownedResources(user.id)
    } else {
        flowOf(emptyList())
    }).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val syncCommands by viewModel.pendingSyncCommands().collectAsState(emptyList())
    val action by viewModel.action.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf("All actions") }
    var categoryFilter by rememberSaveable { mutableStateOf("All categories") }
    var selectedListing by remember { mutableStateOf<com.reevent.app.core.model.ResourceItem?>(null) }
    var selectedRequest by remember { mutableStateOf<com.reevent.app.core.model.ResourceItem?>(null) }
    var publicationResource by remember { mutableStateOf<com.reevent.app.core.model.ResourceItem?>(null) }
    val publishableResources = ownedResources.filter {
        it.marketplaceListing == null && it.syncState == com.reevent.app.core.model.SyncState.SYNCED
    }
    val categories = listOf("All categories") + resources.map { it.category.ifBlank { "Uncategorised" } }.distinct().sorted()
    val actions = listOf("All actions") + resources
        .flatMap { it.availableMarketplaceTypes() }
        .distinct()
        .sortedBy(TransactionType::displayLabel)
        .map(TransactionType::displayLabel)
    val visibleResources = resources.filter { resource ->
        val matchesQuery = query.trim().let { typed ->
            typed.isBlank() || listOf(resource.title, resource.category, resource.material).any { it.contains(typed, ignoreCase = true) }
        }
        val matchesCategory = categoryFilter == "All categories" || resource.category.ifBlank { "Uncategorised" } == categoryFilter
        val matchesAction = typeFilter == "All actions" || resource.availableMarketplaceTypes().any { it.displayLabel() == typeFilter }
        matchesQuery && matchesCategory && matchesAction
    }

    ReEventScaffold(selected = ReEventScreen.Marketplace, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Circular marketplace",
                    subtitle = "Request reusable, repairable, and recoverable event resources",
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            if (user.role == com.reevent.app.core.model.UserRole.ORGANIZER) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Publish your resources", style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                        Text(
                            "Choose one of your active resources and set the terms other accounts can request.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventMuted
                        )
                    }
                }
                if (publishableResources.isEmpty()) {
                    item {
                        val detail = if (ownedResources.isEmpty()) {
                            "Add and sync an active resource before publishing it to the marketplace."
                        } else if (ownedResources.any { it.marketplaceListing == null }) {
                            "Your active resource is still syncing. Wait until it shows Synced before publishing it."
                        } else {
                            "Every active resource currently has an open listing. Close one in the server workflow before publishing it again."
                        }
                        EmptyMarketplacePanel("No resource ready to publish", detail)
                    }
                } else {
                    items(publishableResources, key = { "publish-${it.id}" }) { resource ->
                        MarketplacePublicationResourceCard(
                            resource = resource,
                            onPublish = { publicationResource = resource }
                        )
                    }
                }
                item { HorizontalDivider(color = ReEventLine) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search") },
                        placeholder = { Text("Item, category, or material") },
                        singleLine = true
                    )
                    ChoiceField("Action", typeFilter, actions) {
                        typeFilter = it
                    }
                    ChoiceField("Category", categoryFilter, categories) { categoryFilter = it }
                    if (query.isNotBlank() || typeFilter != "All actions" || categoryFilter != "All categories") {
                        SecondaryActionButton(
                            text = "Clear search and filters",
                            onClick = { query = ""; typeFilter = "All actions"; categoryFilter = "All categories" },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            if (resources.isEmpty()) {
                item { EmptyMarketplacePanel("No available resources yet", "Resources marked available by organisers will appear here.") }
            } else if (visibleResources.isEmpty()) {
                item { EmptyMarketplacePanel("No matching resources", "Try a different search term, action, or category.") }
            }
            items(visibleResources, key = { it.id }) { resource ->
                MarketplaceResourceCard(
                    user = user,
                    resource = resource,
                    onPassport = { onPassport(resource.id) },
                    onViewListing = { selectedListing = resource },
                    onRequest = { selectedRequest = resource }
                )
            }
            if (transactions.isNotEmpty()) {
                item { Text("Requests and handovers", style = MaterialTheme.typography.titleLarge, color = ReEventInk) }
            }
            items(transactions, key = { it.id }) { transaction ->
                val resource by viewModel.resource(transaction.resourceId).collectAsState(null)
                TransactionCard(
                    user = user,
                    transaction = transaction,
                    resource = resource,
                    syncCommand = syncCommands.firstOrNull { it.transactionId == transaction.id },
                    onApprove = { viewModel.approveTransaction(user, transaction) },
                    onCancel = { viewModel.cancelTransaction(user, transaction) },
                    onComplete = { viewModel.completeTransaction(user, transaction) },
                    onInTransit = { viewModel.moveTransactionInTransit(user, transaction) },
                    onPassport = { onPassport(transaction.resourceId) }
                )
            }
        }
    }

    selectedListing?.let { resource ->
        val event by viewModel.event(resource.eventId).collectAsState(null)
        MarketplaceListingDetailDialog(
            resource = resource,
            event = event,
            isOwner = resource.ownerId == user.id,
            onDismiss = { selectedListing = null },
            onOpenPassport = { selectedListing = null; onPassport(resource.id) },
            onRequest = { selectedListing = null; selectedRequest = resource }
        )
    }

    selectedRequest?.let { resource ->
        MarketplaceRequestDialog(
            resource = resource,
            onDismiss = { selectedRequest = null },
            onSubmit = { type, quantity ->
                viewModel.requestMarketplaceResource(user, resource, type, quantity)
                selectedRequest = null
            }
        )
    }

    publicationResource?.let { resource ->
        MarketplacePublishDialog(
            resource = resource,
            loading = action.loading,
            actionError = action.error,
            onDismiss = { publicationResource = null },
            onPublish = { draft ->
                viewModel.publishMarketplaceListing(user, resource, draft) {
                    publicationResource = null
                }
            }
        )
    }
}

@Composable
fun PassportVisualScreen(
    user: User,
    resourceId: String,
    onMatch: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val passport by viewModel.passport(resourceId).collectAsState(null)
    val viewerTransactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val event by (resource?.eventId?.let(viewModel::event)
        ?: flowOf<com.reevent.app.core.model.Event?>(null)).collectAsState(null)
    val historySteps = resource?.let { item ->
        passport?.historyJson?.toPassportHistorySteps(item.condition).orEmpty()
    }.orEmpty()
    val viewerAccess = resource?.let { PassportViewerAccessPolicy.forViewer(user, it, viewerTransactions) }
    val qrPayload = passport?.qrPayload?.takeIf {
        PassportQrPayload.validate(it, BuildConfig.PUBLIC_BASE_URL) is PassportQrPayload.Validation.Canonical
    }
    val qrUnavailableMessage = when {
        passport == null -> "QR code pending until the server issues this resource passport."
        qrPayload == null -> "QR verifier is not configured for this build, or this is a legacy passport that must be reissued."
        else -> null
    }
    PassportScreen(
        onBack = onBack,
        onNavigate = { screen ->
            when (screen) {
                ReEventScreen.AiMatch -> onMatch(resourceId)
                else -> onNavigate(screen)
            }
        },
        item = resource?.toVisualResource(user, event?.name, event?.venue),
        passportId = passport?.id,
        qrPayload = qrPayload,
        qrUnavailableMessage = qrUnavailableMessage,
        viewerAccess = viewerAccess,
        recommendedAction = resource?.recommendedAction(),
        recoverySteps = historySteps.ifEmpty { resource?.let { listOf(it.toPassportRecoveryStep()) }.orEmpty() },
        showMatchAction = viewerAccess?.canFindPartnerMatches == true
    )
}

@Composable
fun PartnerMapVisualScreen(
    onNavigate: (ReEventScreen) -> Unit,
    onOpenPassport: (String) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val programmes by viewModel.programmes().collectAsState(emptyList())
    val marketplaceResources by viewModel.marketplace().collectAsState(emptyList())
    PartnerMapScreen(
        onNavigate = onNavigate,
        programmes = programmes,
        marketplaceResources = marketplaceResources,
        onOpenPassport = onOpenPassport
    )
}

@Composable
fun ParticipantReturnVisualScreen(
    user: User,
    onScanResourceQr: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val action by viewModel.action.collectAsState()
    val returnTransaction = transactions.firstOrNull {
        it.type in setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR) &&
            it.status in setOf(TransactionStatus.ACTIVE, TransactionStatus.RETURN_IN_PROGRESS)
    }
    val displayTransaction = returnTransaction ?: transactions.firstOrNull {
        it.type in setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR) &&
            it.status == TransactionStatus.COMPLETED
    }
    val returnResource by (displayTransaction?.resourceId?.let(viewModel::resource) ?: flowOf(null)).collectAsState(null)
    val returnPassport by (returnTransaction?.resourceId?.let(viewModel::passport) ?: flowOf(null)).collectAsState(null)
    ParticipantReturnScreen(
        onNavigate = onNavigate,
        onScanResourceQr = onScanResourceQr,
        transactions = transactions,
        returnResourceTitle = returnResource?.title,
        returnQrPayload = returnPassport?.qrPayload,
        returnStatus = displayTransaction?.status,
        returnActionError = action.error
    )
}

@Composable
fun PartnerWorkbenchVisualScreen(
    user: User,
    onNavigate: (ReEventScreen) -> Unit,
    onOpenPassport: (String) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val programmes by viewModel.programmes(user.id).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val syncCommands by viewModel.pendingSyncCommands().collectAsState(emptyList())
    val recoveryTasks = transactions.filter {
        it.partnerId == user.id && it.status !in setOf(
            TransactionStatus.COMPLETED,
            TransactionStatus.CANCELLED,
            TransactionStatus.REJECTED
        )
    }
    var editingProgramme by remember { mutableStateOf<CircularProgramme?>(null) }
    var creatingProgramme by rememberSaveable { mutableStateOf(false) }

    ReEventScaffold(selected = ReEventScreen.PartnerWorkbench, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Partner workbench",
                    subtitle = "Manage programmes and assigned handovers",
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                PrimaryActionButton(
                    text = "Create circular programme",
                    onClick = { creatingProgramme = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                SecondaryActionButton(
                    text = "Open partner network",
                    onClick = { onNavigate(ReEventScreen.PartnerMap) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (programmes.isEmpty()) {
                item { EmptyMarketplacePanel("No programmes yet", "Create a programme so organisers can find your circular services.") }
            } else {
                item { Text("Programmes", style = MaterialTheme.typography.titleLarge, color = ReEventInk) }
            }
            items(programmes, key = { it.id }) { programme ->
                ProgrammeCard(
                    programme = programme,
                    onEdit = { editingProgramme = programme },
                    onDeactivate = { viewModel.deactivateProgramme(user, programme) }
                )
            }
            if (recoveryTasks.isEmpty()) {
                item { EmptyMarketplacePanel("No assigned handovers", "Accepted marketplace and partner requests will appear here.") }
            } else {
                item { Text("Assigned recovery tasks", style = MaterialTheme.typography.titleLarge, color = ReEventInk) }
            }
            items(recoveryTasks, key = { it.id }) { transaction ->
                val resource by viewModel.resource(transaction.resourceId).collectAsState(null)
                TransactionCard(
                    user = user,
                    transaction = transaction,
                    resource = resource,
                    syncCommand = syncCommands.firstOrNull { it.transactionId == transaction.id },
                    onApprove = { viewModel.approveTransaction(user, transaction) },
                    onCancel = { viewModel.cancelTransaction(user, transaction) },
                    onComplete = { viewModel.completeTransaction(user, transaction) },
                    onInTransit = { viewModel.moveTransactionInTransit(user, transaction) },
                    onPassport = { onOpenPassport(transaction.resourceId) }
                )
            }
        }
    }

    if (creatingProgramme) {
        ProgrammeEditorDialog(
            programme = null,
            onDismiss = { creatingProgramme = false },
            onSave = { name, type, materials, location, active ->
                viewModel.saveProgramme(user, null, name, type, materials, location, active)
                creatingProgramme = false
            }
        )
    }
    editingProgramme?.let { programme ->
        ProgrammeEditorDialog(
            programme = programme,
            onDismiss = { editingProgramme = null },
            onSave = { name, type, materials, location, active ->
                viewModel.saveProgramme(user, programme, name, type, materials, location, active)
                editingProgramme = null
            }
        )
    }
}

@Composable
private fun MarketplaceResourceCard(
    user: User,
    resource: com.reevent.app.core.model.ResourceItem,
    onPassport: () -> Unit,
    onViewListing: () -> Unit,
    onRequest: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(resource.title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    Text("${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} • ${resource.material.ifBlank { "Material pending" }}", color = ReEventMuted)
                }
                StatusChip(resource.status.visualLabel(), resource.status.toVisualTone(resource.condition).color)
            }
            Text(resource.category.ifBlank { "Uncategorised" }, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            Text("Available actions: ${resource.availableMarketplaceTypes().joinToString { it.displayLabel() }}", style = MaterialTheme.typography.bodyMedium)
            PrimaryActionButton("View listing details", onViewListing, Modifier.fillMaxWidth())
            SecondaryActionButton("Open passport", onPassport, Modifier.fillMaxWidth())
            if (resource.ownerId == user.id) {
                Text("This is your own listing. Other users can request it from the marketplace.", color = ReEventMuted)
            } else {
                SecondaryActionButton("Request resource", onRequest, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MarketplacePublicationResourceCard(
    resource: com.reevent.app.core.model.ResourceItem,
    onPublish: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(resource.title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    Text(
                        "${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} · ${resource.category.ifBlank { "Uncategorised" }}",
                        color = ReEventMuted
                    )
                }
                StatusChip("Active", ReEventGreen)
            }
            Text(
                "Set request types, quantity, prices where needed, and reusable-resource terms.",
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventMuted
            )
            PrimaryActionButton("Publish to marketplace", onPublish, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MarketplacePublishDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    loading: Boolean,
    actionError: String?,
    onDismiss: () -> Unit,
    onPublish: (MarketplaceListingDraft) -> Unit
) {
    var selectedActions by remember(resource.id) { mutableStateOf(setOf(TransactionType.DONATE)) }
    var quantity by remember(resource.id) { mutableStateOf(ResourcePresentationRules.quantityNumber(resource.quantity)) }
    var buyPrice by remember(resource.id) { mutableStateOf("") }
    var rentPrice by remember(resource.id) { mutableStateOf("") }
    var durationDays by remember(resource.id) { mutableStateOf("") }
    var terms by remember(resource.id) { mutableStateOf("") }
    var submitted by remember(resource.id) { mutableStateOf(false) }
    val orderedActions = listOf(
        TransactionType.BORROW,
        TransactionType.RENT,
        TransactionType.BUY,
        TransactionType.DONATE,
        TransactionType.EXCHANGE
    )
    val draft = MarketplaceListingDraft(
        allowedActions = selectedActions,
        publishedQuantity = quantity.toDoubleOrNull() ?: Double.NaN,
        buyUnitPrice = buyPrice.toLongOrNull().takeIf { TransactionType.BUY in selectedActions },
        rentUnitPrice = rentPrice.toLongOrNull().takeIf { TransactionType.RENT in selectedActions },
        defaultDurationDays = durationDays.toIntOrNull().takeIf {
            selectedActions.any { action -> action == TransactionType.BORROW || action == TransactionType.RENT }
        },
        terms = terms
    )
    val validation = MarketplaceListingDraftRules.validate(resource, draft)
    val showsErrors = submitted

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Publish ${resource.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "The server rechecks ownership, available quantity, and all terms before publishing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReEventMuted
                )
                Text("Available actions", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                orderedActions.chunked(2).forEach { actionRow ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        actionRow.forEach { action ->
                            FilterChip(
                                selected = action in selectedActions,
                                onClick = {
                                    selectedActions = if (action in selectedActions) {
                                        selectedActions - action
                                    } else {
                                        selectedActions + action
                                    }
                                },
                                label = { Text(action.displayLabel()) },
                                enabled = !loading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (actionRow.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                if (showsErrors && validation.actionError != null) {
                    Text(validation.actionError, color = ReEventCoral, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { typed ->
                        if (typed.matches(Regex("^\\d{0,8}(\\.\\d{0,3})?$"))) quantity = typed
                    },
                    label = { Text("Quantity, up to ${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)}") },
                    isError = showsErrors && validation.quantityError != null,
                    supportingText = { if (showsErrors) validation.quantityError?.let { Text(it) } },
                    singleLine = true,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )
                if (TransactionType.BUY in selectedActions) {
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { typed -> if (typed.matches(Regex("^\\d{0,12}$"))) buyPrice = typed },
                        label = { Text("Buy price per ${resource.unit} (ReCoins)") },
                        isError = showsErrors && validation.buyPriceError != null,
                        supportingText = { if (showsErrors) validation.buyPriceError?.let { Text(it) } },
                        singleLine = true,
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (TransactionType.RENT in selectedActions) {
                    OutlinedTextField(
                        value = rentPrice,
                        onValueChange = { typed -> if (typed.matches(Regex("^\\d{0,12}$"))) rentPrice = typed },
                        label = { Text("Rent price per ${resource.unit} (ReCoins)") },
                        isError = showsErrors && validation.rentPriceError != null,
                        supportingText = { if (showsErrors) validation.rentPriceError?.let { Text(it) } },
                        singleLine = true,
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (selectedActions.any { it == TransactionType.BORROW || it == TransactionType.RENT }) {
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { typed -> if (typed.matches(Regex("^\\d{0,3}$"))) durationDays = typed },
                        label = { Text("Default Borrow/Rent duration (1–365 days)") },
                        isError = showsErrors && validation.durationError != null,
                        supportingText = { if (showsErrors) validation.durationError?.let { Text(it) } },
                        singleLine = true,
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = terms,
                    onValueChange = { typed -> if (typed.length <= MarketplaceListingDraftRules.MAX_TERMS_LENGTH + 1) terms = typed },
                    label = { Text("Listing terms (optional)") },
                    supportingText = {
                        if (showsErrors) validation.termsError?.let { Text(it) }
                        else Text("${terms.length}/${MarketplaceListingDraftRules.MAX_TERMS_LENGTH}")
                    },
                    isError = showsErrors && validation.termsError != null,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showsErrors && actionError != null) {
                    Text(
                        "Publishing was not confirmed. Your values are kept; refresh Marketplace before trying again so a duplicate listing is not created.",
                        color = ReEventCoral,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    submitted = true
                    if (validation.isValid) onPublish(draft)
                },
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Publish listing")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) { Text("Cancel") }
        }
    )
}

@Composable
private fun MarketplaceListingDetailDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    event: com.reevent.app.core.model.Event?,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onOpenPassport: () -> Unit,
    onRequest: () -> Unit
) {
    val listing = resource.marketplaceListing ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(resource.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${ResourcePresentationRules.quantityLabel(listing.publishedQuantity, resource.unit)} available", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                Text("Condition: ${resource.condition.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)}", color = ReEventMuted)
                Text("Material: ${resource.material.ifBlank { "Not specified" }} · Category: ${resource.category.ifBlank { "Uncategorised" }}", color = ReEventMuted)
                HorizontalDivider(color = ReEventLine)
                Text("Event context", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                Text(event?.name ?: "Event details are not currently available to this account.", color = ReEventMuted)
                Text(event?.venue?.ifBlank { "Location to be confirmed" } ?: "Location to be confirmed", color = ReEventMuted)
                event?.let {
                    Text(
                        "${EventFormValidation.dateText(it.startsAt)} to ${EventFormValidation.dateText(it.endsAt)}",
                        color = ReEventMuted
                    )
                }
                HorizontalDivider(color = ReEventLine)
                Text("Request terms", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                Text("Actions: ${listing.allowedActions.joinToString { it.displayLabel() }}", color = ReEventMuted)
                listing.defaultDurationDays?.let { days ->
                    Text("Borrow/rent default duration: $days day${if (days == 1) "" else "s"}. Final handover timing is confirmed after approval.", color = ReEventMuted)
                }
                listing.buyUnitPrice?.let { price -> Text("Buy: $price ReCoins per ${resource.unit}", color = ReEventMuted) }
                listing.rentUnitPrice?.let { price -> Text("Rent: $price ReCoins per ${resource.unit}", color = ReEventMuted) }
                Text(listing.terms.ifBlank { "No additional listing terms were supplied." }, color = ReEventMuted)
            }
        },
        confirmButton = {
            if (isOwner) {
                TextButton(onClick = onDismiss) { Text("Close") }
            } else {
                TextButton(onClick = onRequest) { Text("Request resource") }
            }
        },
        dismissButton = { TextButton(onClick = onOpenPassport) { Text("Open passport") } }
    )
}

@Composable
private fun MarketplaceRequestDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    onDismiss: () -> Unit,
    onSubmit: (TransactionType, Double) -> Unit
) {
    val allowedActions = resource.availableMarketplaceTypes()
    val maxQuantity = minOf(resource.quantity, resource.marketplaceListing?.publishedQuantity ?: 0.0)
    var type by rememberSaveable(resource.id) { mutableStateOf(allowedActions.firstOrNull() ?: TransactionType.BORROW) }
    var quantity by rememberSaveable(resource.id) { mutableStateOf("1") }
    var submitted by rememberSaveable(resource.id) { mutableStateOf(false) }
    val quantityValue = quantity.toDoubleOrNull()
    // The server currently permits fractional marketplace quantities only for KG resources.
    val allowsFraction = resource.unit.equals("kg", ignoreCase = true)
    val valid = type in allowedActions && quantityValue != null && quantityValue > 0 && quantityValue <= maxQuantity &&
        (allowsFraction || quantityValue % 1.0 == 0.0)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request ${resource.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This creates a pending transaction for the owner to approve. The server rechecks listing availability before accepting it.")
                if (allowedActions.isNotEmpty()) {
                    ChoiceField("Action", type.displayLabel(), allowedActions.map(TransactionType::displayLabel)) { selected ->
                        type = allowedActions.first { it.displayLabel() == selected }
                    }
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { typed ->
                        if (typed.matches(Regex("^\\d{0,5}(\\.\\d{0,3})?$"))) quantity = typed
                    },
                    label = { Text("Quantity, max ${ResourcePresentationRules.quantityLabel(maxQuantity, resource.unit)}") },
                    isError = submitted && !valid,
                    supportingText = {
                        if (submitted && !valid) {
                            Text(
                                if (allowedActions.isEmpty()) "This listing has no requestable actions."
                                else if (!allowsFraction && quantityValue != null && quantityValue % 1.0 != 0.0) "This resource must be requested as whole ${resource.unit}."
                                else "Enter a quantity above 0 and no more than the published amount."
                            )
                        }
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                submitted = true
                if (valid) onSubmit(type, checkNotNull(quantityValue))
            }) { Text("Submit request") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TransactionCard(
    user: User,
    transaction: CircularTransaction,
    resource: com.reevent.app.core.model.ResourceItem?,
    syncCommand: SyncCommandStatus?,
    onApprove: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onInTransit: () -> Unit,
    onPassport: () -> Unit
) {
    val presentation = TransactionLifecyclePresentationRules.forViewer(user.id, transaction, syncCommand)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(resource?.title ?: "Resource ${transaction.resourceId.take(8)}", style = MaterialTheme.typography.titleMedium)
                    Text("${transaction.type.displayLabel()} • ${ResourcePresentationRules.quantityLabel(transaction.quantity, resource?.unit ?: "items")}", color = ReEventMuted)
                }
                StatusChip(presentation.statusLabel, transaction.status.toUiColor())
            }
            Text(
                text = presentation.nextStep,
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventMuted
            )
            Text(
                text = "Next responsible: ${presentation.responsibleRole}",
                style = MaterialTheme.typography.labelMedium,
                color = ReEventInk
            )
            when (presentation.syncFeedback) {
                TransactionLifecycleSyncFeedback.PENDING -> LifecycleSyncFeedbackPanel(
                    title = "Action waiting to sync",
                    detail = "Do not repeat the action. The card will refresh when the server processes the queued command.",
                    color = ReEventBlue
                )

                TransactionLifecycleSyncFeedback.FAILED -> LifecycleSyncFeedbackPanel(
                    title = "Action needs retry",
                    detail = "The server did not confirm this change. Open Profile and choose Retry failed changes, then return here to refresh.",
                    color = ReEventCoral
                )

                null -> Unit
            }
            if (resource != null) {
                SecondaryActionButton("Open passport", onPassport, Modifier.fillMaxWidth())
            }
            presentation.primaryAction?.let { action ->
                val onAction = when (action) {
                    TransactionLifecycleCardAction.APPROVE -> onApprove
                    TransactionLifecycleCardAction.BEGIN_HANDOVER -> onInTransit
                    TransactionLifecycleCardAction.CONFIRM_RECEIPT,
                    TransactionLifecycleCardAction.BEGIN_RETURN,
                    TransactionLifecycleCardAction.CONFIRM_RETURN -> onComplete
                    TransactionLifecycleCardAction.CANCEL -> onCancel
                }
                PrimaryActionButton(checkNotNull(presentation.primaryActionLabel), onAction, Modifier.fillMaxWidth())
            }
            if (presentation.secondaryAction == TransactionLifecycleCardAction.CANCEL) {
                SecondaryActionButton(checkNotNull(presentation.secondaryActionLabel), onCancel, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LifecycleSyncFeedbackPanel(title: String, detail: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = ReEventInk)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = ReEventMuted)
        }
    }
}

@Composable
private fun ProgrammeCard(
    programme: CircularProgramme,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(programme.name, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    Text(programme.type.displayLabel(), color = ReEventMuted)
                }
                StatusChip(if (programme.active) "Active" else "Inactive", if (programme.active) ReEventGreen else ReEventMuted)
            }
            Text(programme.location.ifBlank { "Location pending" }, color = ReEventMuted)
            Text("Accepts: ${programme.acceptedMaterials.ifEmpty { listOf("all materials") }.joinToString()}")
            PrimaryActionButton("Edit programme", onEdit, Modifier.fillMaxWidth())
            if (programme.active) {
                SecondaryActionButton("Deactivate programme", onDeactivate, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProgrammeEditorDialog(
    programme: CircularProgramme?,
    onDismiss: () -> Unit,
    onSave: (String, ProgrammeType, List<String>, String, Boolean) -> Unit
) {
    var name by rememberSaveable(programme?.id) { mutableStateOf(programme?.name.orEmpty()) }
    var type by rememberSaveable(programme?.id) { mutableStateOf(programme?.type ?: ProgrammeType.REPAIR) }
    var materials by rememberSaveable(programme?.id) { mutableStateOf(programme?.acceptedMaterials?.joinToString(", ").orEmpty()) }
    var location by rememberSaveable(programme?.id) { mutableStateOf(programme?.location.orEmpty()) }
    var active by rememberSaveable(programme?.id) { mutableStateOf(programme?.active ?: true) }
    val valid = name.trim().length >= 2
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (programme == null) "Create programme" else "Edit programme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Programme name *") }, isError = name.isNotBlank() && !valid)
                ChoiceField("Type", type.displayLabel(), ProgrammeType.entries.map(ProgrammeType::displayLabel)) { selected ->
                    type = ProgrammeType.entries.first { it.displayLabel() == selected }
                }
                OutlinedTextField(materials, { materials = it }, Modifier.fillMaxWidth(), label = { Text("Accepted materials") }, placeholder = { Text("Acrylic, Fabric") })
                OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Location") })
                ChoiceField("Status", if (active) "Active" else "Inactive", listOf("Active", "Inactive")) { active = it == "Active" }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(name, type, materials.split(","), location, active) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ChoiceField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyMarketplacePanel(title: String, detail: String) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = ReEventMintSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ReEventGreenDeep)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
        }
    }
}

private fun com.reevent.app.core.model.ResourceItem.toVisualResource(
    viewer: User,
    eventName: String? = null,
    venue: String? = null
) = VisualResourceItem(
    title = title,
    owner = ResourcePresentationRules.ownerLabel(viewer.id, viewer.role, ownerId),
    category = category.ifBlank { "Uncategorised" },
    price = valueCents.takeIf { it > 0 }?.let { "RM %.2f".format(Locale.US, it / 100.0) } ?: "Value not set",
    quantity = ResourcePresentationRules.quantityLabel(quantity, unit),
    location = venue?.takeIf(String::isNotBlank) ?: "Location to be confirmed",
    impact = "${material.ifBlank { "Material pending" }} • ${status.visualLabel()}",
    tone = status.toVisualTone(condition),
    imageRes = R.drawable.resource_display_stand,
    photoPath = imageUrls.firstOrNull(),
    id = id
)

private fun List<com.reevent.app.core.model.ResourceItem>.toDashboardMetrics(records: List<ImpactRecord>): List<ImpactMetric> {
    if (isEmpty() && records.isEmpty()) return emptyList()
    val recovered = count { it.status == ResourceStatus.RECOVERED || it.status == ResourceStatus.RECOVERY_IN_PROGRESS }
    val recoveryRate = if (isEmpty()) 0 else recovered * 100 / size
    return listOf(
        ImpactMetric("$recoveryRate%", "Recovery rate", "$recovered of $size tracked lots"),
        ImpactMetric("${records.mapNotNull { it.materialDivertedKg }.sum().formatQuantity()} kg", "Materials diverted", "Verified impact records"),
        ImpactMetric("${records.sumOf { it.recoinsTransferred + it.recoinsRewarded }}", "ReCoins moved", "Transferred plus earned recognition")
    )
}

private fun List<ImpactRecord>.toImpactMetrics(): List<ImpactMetric> {
    if (isEmpty()) return emptyList()
    return listOf(
        ImpactMetric(mapNotNull { it.materialDivertedKg }.sum().formatQuantity() + " kg", "Materials diverted", "Verified recovery records"),
        ImpactMetric(mapNotNull { it.emissionsAvoidedKg }.sum().formatQuantity() + " kg", "Emissions avoided", "Estimated CO₂e avoided"),
        ImpactMetric(sumOf { it.recoinsTransferred + it.recoinsRewarded }.toString(), "ReCoins moved", "Transferred plus earned recognition")
    )
}

private fun ImpactDashboardState.toImpactMetrics(): List<ImpactMetric> {
    val completedOutcomes = reusedCount + repairedCount + donatedCount + recycledCount
    if (completedOutcomes == 0 && materialDivertedKg == null && emissionsAvoidedKg == null && recoinsTransferred == null && recoinsRewarded == null) {
        return emptyList()
    }
    return buildList {
        add(ImpactMetric("$completedOutcomes", "Completed outcomes", "Reuse, repair, donation, and recycling"))
        materialDivertedKg?.let {
            add(ImpactMetric(it.formatQuantity() + " kg", "Materials diverted", "Mass-based documented estimate"))
        }
        emissionsAvoidedKg?.let {
            add(ImpactMetric(it.formatQuantity() + " kg", "CO₂e avoided", "MVP demonstration estimate"))
        }
        recoinsTransferred?.let {
            add(ImpactMetric(it.toString(), "ReCoins transferred", "Completed server settlements"))
        }
        recoinsRewarded?.let {
            add(ImpactMetric(it.toString(), "ReCoins rewarded", "Versioned circular recognition"))
        }
    }
}

private fun List<com.reevent.app.core.model.ResourceItem>.toRecoverySteps(records: List<ImpactRecord>): List<RecoveryStep> {
    if (isEmpty() && records.isEmpty()) return emptyList()
    val available = count { it.status == ResourceStatus.ACTIVE }
    val completed = count { it.status == ResourceStatus.RECOVERED || it.status == ResourceStatus.RECOVERY_IN_PROGRESS }
    return listOf(
        RecoveryStep("Inventory tagged", "$size tracked resource lots", "$size", ResourceTone.Ready),
        RecoveryStep("Available for matching", "$available resource lots are available", "$available", ResourceTone.Hot),
        RecoveryStep("Recovered or handed over", "$completed completed routes", "$completed", ResourceTone.Recycle)
    )
}

private fun com.reevent.app.core.model.ResourceItem.toPassportRecoveryStep() = RecoveryStep(
    title = "Resource recorded",
    detail = "${ResourcePresentationRules.quantityLabel(quantity, unit)} recorded as ${status.visualLabel().lowercase()}",
    status = status.visualLabel(),
    tone = status.toVisualTone(condition)
)

private val passportHistoryJson = Json { ignoreUnknownKeys = true }

private fun String.toPassportHistorySteps(condition: ResourceCondition): List<RecoveryStep> = runCatching {
    passportHistoryJson.decodeFromString(ListSerializer(PassportHistoryEntry.serializer()), this)
}.getOrDefault(emptyList()).sortedByDescending(PassportHistoryEntry::occurredAt).map { entry ->
    val transition = entry.previousCondition?.let { previous ->
        entry.newCondition?.let { next -> "Condition changed from ${previous.name} to ${next.name}" }
    } ?: entry.quantity?.let { value ->
        entry.unit?.takeIf(String::isNotBlank)
            ?.let { unit -> "${ResourcePresentationRules.quantityLabel(value, unit)} recorded" }
            ?: "${ResourcePresentationRules.quantityNumber(value)} recorded"
    }
    RecoveryStep(
        title = entry.action.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase),
        detail = listOfNotNull(entry.note, transition).joinToString(" â€¢ "),
        status = entry.newStatus.visualLabel(),
        tone = entry.newStatus.toVisualTone(condition)
    )
}

private fun com.reevent.app.core.model.ResourceItem.recommendedAction() = when {
    status == ResourceStatus.ARCHIVED -> "No action needed â€” this resource is archived"
    status == ResourceStatus.RECOVERED || status == ResourceStatus.RECOVERY_IN_PROGRESS -> "Recovery route completed"
    condition == ResourceCondition.END_OF_LIFE -> "Send to a verified recycling partner"
    condition == ResourceCondition.NEEDS_REPAIR -> "Request a repair-partner assessment"
    status == ResourceStatus.RECOVERY_IN_PROGRESS -> "Prepare the reserved handover"
    status == ResourceStatus.ACTIVE -> "Match with a reuse partner"
    else -> "Review the resource status"
}

private fun ResourceStatus.toVisualTone(condition: ResourceCondition) = when {
    this == ResourceStatus.ACTIVE -> ResourceTone.Ready
    this == ResourceStatus.RECOVERED || this == ResourceStatus.RECOVERY_IN_PROGRESS -> ResourceTone.Recycle
    condition == ResourceCondition.NEEDS_REPAIR -> ResourceTone.Repair
    else -> ResourceTone.Hot
}

private fun ProgrammeType.toVisualTone() = when (this) {
    ProgrammeType.REPAIR -> ResourceTone.Repair
    ProgrammeType.RECYCLE -> ResourceTone.Recycle
    ProgrammeType.BUY_BACK -> ResourceTone.Hot
}

private fun com.reevent.app.core.model.ResourceItem.availableMarketplaceTypes(): List<TransactionType> =
    marketplaceListing?.allowedActions.orEmpty()

private fun ResourceStatus.visualLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun TransactionStatus.displayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun TransactionType.displayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun ProgrammeType.displayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun TransactionStatus.toUiColor() = when (this) {
    TransactionStatus.REQUESTED -> ReEventCoral
    TransactionStatus.APPROVED -> ReEventGreen
    TransactionStatus.IN_TRANSIT -> ReEventBlue
    TransactionStatus.ACTIVE -> ReEventGreen
    TransactionStatus.RETURN_IN_PROGRESS -> ReEventCoral
    TransactionStatus.COMPLETED -> ReEventGreenDeep
    TransactionStatus.REJECTED -> ReEventCoral
    TransactionStatus.CANCELLED -> ReEventMuted
}
private fun Double.formatQuantity() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(Locale.US, this)
private fun Long.toMoney() = "RM %.2f".format(Locale.US, this / 100.0)
