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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.R
import com.reevent.app.core.data.ProgrammeMatcher
import com.reevent.app.core.data.TransactionWorkflow
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.PassportHistoryEntry
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.feature.impact.ImpactCalculator
import com.reevent.app.feature.impact.ImpactDashboardState
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
    val visualResources = resources.map { it.toVisualResource(event?.name, event?.venue) }

    HomeScreen(
        onNavigate = { screen ->
            when (screen) {
                ReEventScreen.AddResource -> event?.let { onAddResource(it.id) }
                    ?: viewModel.createEvent(user) { onAddResource(it.id) }
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
        onManageEvents = onManageEvents,
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
    ImpactVisualScreen(events.firstOrNull()?.id.orEmpty(), onNavigate, viewModel)
}

@Composable
fun ImpactVisualScreen(
    eventId: String,
    onNavigate: (ReEventScreen) -> Unit,
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
        unavailableEstimateReason = summary.unavailableEstimateReason
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
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var typeFilter by rememberSaveable { mutableStateOf("All actions") }
    var categoryFilter by rememberSaveable { mutableStateOf("All categories") }
    var selectedRequest by remember { mutableStateOf<com.reevent.app.core.model.ResourceItem?>(null) }
    val categories = listOf("All categories") + resources.map { it.category.ifBlank { "Uncategorised" } }.distinct().sorted()
    val visibleResources = resources.filter { resource ->
        val matchesQuery = query.trim().let { typed ->
            typed.isBlank() || listOf(resource.title, resource.category, resource.material).any { it.contains(typed, ignoreCase = true) }
        }
        val matchesCategory = categoryFilter == "All categories" || resource.category.ifBlank { "Uncategorised" } == categoryFilter
        val matchesAction = typeFilter == "All actions" || resource.suggestedMarketplaceTypes().any { it.displayLabel() == typeFilter }
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
                    ChoiceField("Action", typeFilter, listOf("All actions") + TransactionType.entries.map(TransactionType::displayLabel)) {
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
                    onApprove = { viewModel.approveTransaction(user, transaction) },
                    onCancel = { viewModel.cancelTransaction(user, transaction) },
                    onComplete = { viewModel.completeTransaction(user, transaction) },
                    onInTransit = { viewModel.moveTransactionInTransit(user, transaction) },
                    onPassport = { onPassport(transaction.resourceId) }
                )
            }
        }
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
}

@Composable
fun PassportVisualScreen(
    resourceId: String,
    onMatch: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val passport by viewModel.passport(resourceId).collectAsState(null)
    val event by (resource?.eventId?.let(viewModel::event)
        ?: flowOf<com.reevent.app.core.model.Event?>(null)).collectAsState(null)
    val historySteps = resource?.let { item ->
        passport?.historyJson?.toPassportHistorySteps(item.condition).orEmpty()
    }.orEmpty()
    PassportScreen(
        onBack = onBack,
        onNavigate = { screen ->
            when (screen) {
                ReEventScreen.AiMatch -> onMatch(resourceId)
                else -> onNavigate(screen)
            }
        },
        item = resource?.toVisualResource(event?.name, event?.venue),
        passportId = passport?.id,
        qrPayload = passport?.qrPayload,
        ownerId = resource?.ownerId,
        recommendedAction = resource?.recommendedAction(),
        recoverySteps = historySteps.ifEmpty { resource?.let { listOf(it.toPassportRecoveryStep()) }.orEmpty() }
    )
}

@Composable
fun PartnerMapVisualScreen(
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val programmes by viewModel.programmes().collectAsState(emptyList())
    val matches = programmes.filter(CircularProgramme::active).map(CircularProgramme::toPartnerMatch)
    PartnerMapScreen(
        onNavigate = onNavigate,
        matches = matches,
        partnerCountText = if (matches.isEmpty()) "No active partners" else "${matches.size} active partner${if (matches.size == 1) "" else "s"}",
        // Viewing a programme never opens a partner-only workbench for another role.
        onPartnerAccepted = {}
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
    ParticipantReturnScreen(onNavigate, onScanResourceQr, transactions)
}

@Composable
fun PartnerWorkbenchVisualScreen(
    user: User,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val programmes by viewModel.programmes(user.id).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
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
            if (transactions.isEmpty()) {
                item { EmptyMarketplacePanel("No assigned handovers", "Accepted marketplace and partner requests will appear here.") }
            } else {
                item { Text("Assigned handovers", style = MaterialTheme.typography.titleLarge, color = ReEventInk) }
            }
            items(transactions, key = { it.id }) { transaction ->
                val resource by viewModel.resource(transaction.resourceId).collectAsState(null)
                TransactionCard(
                    user = user,
                    transaction = transaction,
                    resource = resource,
                    onApprove = { viewModel.approveTransaction(user, transaction) },
                    onCancel = { viewModel.cancelTransaction(user, transaction) },
                    onComplete = { viewModel.completeTransaction(user, transaction) },
                    onInTransit = { viewModel.moveTransactionInTransit(user, transaction) },
                    onPassport = {}
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
    onRequest: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(resource.title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    Text("${resource.quantity} ${resource.unit} • ${resource.material.ifBlank { "Material pending" }}", color = ReEventMuted)
                }
                StatusChip(resource.status.visualLabel(), resource.status.toVisualTone(resource.condition).color)
            }
            Text(resource.category.ifBlank { "Uncategorised" }, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            Text("Suggested actions: ${resource.suggestedMarketplaceTypes().joinToString { it.displayLabel() }}", style = MaterialTheme.typography.bodyMedium)
            PrimaryActionButton("Open passport", onPassport, Modifier.fillMaxWidth())
            if (resource.ownerId == user.id) {
                Text("This is your own listing. Other users can request it from the marketplace.", color = ReEventMuted)
            } else {
                SecondaryActionButton("Request resource", onRequest, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MarketplaceRequestDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    onDismiss: () -> Unit,
    onSubmit: (TransactionType, Int) -> Unit
) {
    var type by rememberSaveable(resource.id) { mutableStateOf(resource.suggestedMarketplaceTypes().first()) }
    var quantity by rememberSaveable(resource.id) { mutableStateOf("1") }
    val quantityValue = quantity.toIntOrNull()
    val valid = quantityValue != null && quantityValue > 0 && quantityValue.toDouble() <= resource.quantity
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request ${resource.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This creates a pending transaction for the owner to approve.")
                ChoiceField("Action", type.displayLabel(), resource.suggestedMarketplaceTypes().map(TransactionType::displayLabel)) { selected ->
                    type = TransactionType.entries.first { it.displayLabel() == selected }
                }
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter(Char::isDigit).take(5) },
                    label = { Text("Quantity, max ${resource.quantity}") },
                    isError = quantity.isNotBlank() && !valid,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onSubmit(type, quantityValue ?: 1) }) { Text("Submit request") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun TransactionCard(
    user: User,
    transaction: CircularTransaction,
    resource: com.reevent.app.core.model.ResourceItem?,
    onApprove: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onInTransit: () -> Unit,
    onPassport: () -> Unit
) {
    val isSender = transaction.senderId == user.id
    val isReceiver = transaction.receiverId == user.id || transaction.partnerId == user.id
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(resource?.title ?: "Resource ${transaction.resourceId.take(8)}", style = MaterialTheme.typography.titleMedium)
                    Text("${transaction.type.displayLabel()} • ${transaction.quantity} item${if (transaction.quantity == 1.0) "" else "s"}", color = ReEventMuted)
                }
                StatusChip(transaction.status.displayLabel(), transaction.status.toUiColor())
            }
            Text(
                text = if (transaction.requesterId == user.id) "You requested this transaction." else "This transaction may need action from your workspace.",
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventMuted
            )
            if (resource != null) {
                SecondaryActionButton("Open passport", onPassport, Modifier.fillMaxWidth())
            }
            if (TransactionWorkflow.canApprove(user.id, transaction)) {
                PrimaryActionButton("Approve request", onApprove, Modifier.fillMaxWidth())
            }
            if (TransactionWorkflow.canBeginHandover(user.id, transaction)) {
                SecondaryActionButton("Begin handover", onInTransit, Modifier.fillMaxWidth())
            }
            if (
                TransactionWorkflow.canConfirmReceipt(user.id, transaction) ||
                TransactionWorkflow.canBeginReturn(user.id, transaction) ||
                TransactionWorkflow.canConfirmReturn(user.id, transaction)
            ) {
                val actionLabel = when {
                    TransactionWorkflow.canConfirmReceipt(user.id, transaction) -> "Confirm receipt"
                    TransactionWorkflow.canBeginReturn(user.id, transaction) -> "Begin return"
                    else -> "Confirm returned"
                }
                PrimaryActionButton(actionLabel, onComplete, Modifier.fillMaxWidth())
            }
            if ((isSender || isReceiver) && TransactionWorkflow.canCancel(user.id, transaction)) {
                SecondaryActionButton("Cancel request", onCancel, Modifier.fillMaxWidth())
            }
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
    eventName: String? = null,
    venue: String? = null
) = VisualResourceItem(
    title = title,
    owner = eventName ?: "Your workspace",
    category = category.ifBlank { "Uncategorised" },
    price = valueCents.takeIf { it > 0 }?.let { "RM %.2f".format(Locale.US, it / 100.0) } ?: "Value not set",
    quantity = "$quantity $unit",
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
    detail = "${quantity} $unit recorded as ${status.visualLabel().lowercase()}",
    status = status.visualLabel(),
    tone = status.toVisualTone(condition)
)

private val passportHistoryJson = Json { ignoreUnknownKeys = true }

private fun String.toPassportHistorySteps(condition: ResourceCondition): List<RecoveryStep> = runCatching {
    passportHistoryJson.decodeFromString(ListSerializer(PassportHistoryEntry.serializer()), this)
}.getOrDefault(emptyList()).sortedBy(PassportHistoryEntry::occurredAt).map { entry ->
    val transition = entry.previousCondition?.let { previous ->
        entry.newCondition?.let { next -> "Condition changed from ${previous.name} to ${next.name}" }
    } ?: entry.quantity?.let { value -> "$value ${entry.unit.orEmpty()} recorded" }
    RecoveryStep(
        title = entry.action,
        detail = listOfNotNull(entry.note, transition, "Actor ${entry.actorId.take(8)}").joinToString(" â€¢ "),
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

private fun CircularProgramme.toPartnerMatch() = PartnerMatch(
    name = name,
    type = type.name.lowercase().replaceFirstChar(Char::titlecase),
    score = "Active",
    distance = location.ifBlank { "Location pending" },
    offer = acceptedMaterials.takeIf { it.isNotEmpty() }?.joinToString() ?: "Materials pending",
    detail = "Authorised programme available for circular matching.",
    tone = type.toVisualTone()
)

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

private fun com.reevent.app.core.model.ResourceItem.suggestedMarketplaceTypes(): List<TransactionType> = when {
    condition == ResourceCondition.END_OF_LIFE -> listOf(TransactionType.DONATE)
    valueCents > 0 -> listOf(TransactionType.BUY, TransactionType.RENT)
    else -> listOf(TransactionType.DONATE, TransactionType.BORROW)
}

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
