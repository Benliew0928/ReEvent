package com.reevent.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.BuildConfig
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.feature.passports.PassportQrPayload
import com.reevent.app.feature.passports.PassportViewerAccessPolicy
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventTextSecondary
import kotlinx.coroutines.flow.flowOf

@Composable
fun PassportVisualScreen(
    user: User,
    resourceId: String,
    onMatch: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val passport by viewModel.passport(resourceId).collectAsState(null)
    val viewerTransactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val event by (
        resource?.eventId?.let(viewModel::event)
            ?: flowOf<com.reevent.app.core.model.Event?>(null)
    ).collectAsState(null)
    val historySteps =
        resource
            ?.let { item ->
                passport?.historyJson?.toPassportHistorySteps(item.condition).orEmpty()
            }.orEmpty()
    val viewerAccess = resource?.let { PassportViewerAccessPolicy.forViewer(user, it, viewerTransactions) }
    val qrPresentation =
        passport?.qrPayload?.let {
            PassportQrPayload.renderablePayload(it, BuildConfig.PUBLIC_BASE_URL)
        }
    val qrPayload = (qrPresentation as? PassportQrPayload.RenderResult.Ready)?.payload
    val qrUnavailableMessage =
        when (qrPresentation) {
            null -> "QR code pending until the server issues this resource passport."
            is PassportQrPayload.RenderResult.Unavailable -> qrPresentation.message
            is PassportQrPayload.RenderResult.Ready -> null
        }
    PassportScreen(
        onBack = onBack,
        onNavigate = onNavigate,
        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
        onMatch = { onMatch(resourceId) },
        item = resource?.toVisualResource(user, event?.name, event?.venue),
        passportId = passport?.id,
        qrPayload = qrPayload,
        qrUnavailableMessage = qrUnavailableMessage,
        viewerAccess = viewerAccess,
        recommendedAction = resource?.recommendedAction(),
        recoverySteps = historySteps.ifEmpty { resource?.let { listOf(it.toPassportRecoveryStep()) }.orEmpty() },
        showMatchAction = viewerAccess?.canFindPartnerMatches == true,
    )
}

@Composable
fun PartnerMapVisualScreen(
    onNavigate: (TopLevelDestination) -> Unit,
    onOpenPassport: (String) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val programmes by viewModel.programmes().collectAsState(emptyList())
    val marketplaceResources by viewModel.marketplace().collectAsState(emptyList())
    PartnerMapScreen(
        onNavigate = onNavigate,
        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
        programmes = programmes,
        marketplaceResources = marketplaceResources,
        onOpenPassport = onOpenPassport,
    )
}

@Composable
fun ParticipantReturnVisualScreen(
    user: User,
    onScanResourceQr: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val action by viewModel.action.collectAsState()
    val returnTransaction =
        transactions.firstOrNull {
            it.type in setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR) &&
                it.status in setOf(TransactionStatus.ACTIVE, TransactionStatus.RETURN_IN_PROGRESS)
        }
    val displayTransaction =
        returnTransaction ?: transactions.firstOrNull {
            it.type in setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR) &&
                it.status == TransactionStatus.COMPLETED
        }
    val returnResource by (displayTransaction?.resourceId?.let(viewModel::resource) ?: flowOf(null)).collectAsState(null)
    val returnPassport by (returnTransaction?.resourceId?.let(viewModel::passport) ?: flowOf(null)).collectAsState(null)
    val returnQrPresentation =
        returnPassport?.qrPayload?.let {
            PassportQrPayload.renderablePayload(it, BuildConfig.PUBLIC_BASE_URL)
        }
    ParticipantReturnScreen(
        onNavigate = onNavigate,
        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
        onScanResourceQr = onScanResourceQr,
        transactions = transactions,
        returnResourceTitle = returnResource?.title,
        returnPassportAssigned = returnPassport != null,
        returnQrPayload = (returnQrPresentation as? PassportQrPayload.RenderResult.Ready)?.payload,
        returnQrUnavailableMessage =
            when (returnQrPresentation) {
                null -> "No return passport is assigned yet."
                is PassportQrPayload.RenderResult.Unavailable -> returnQrPresentation.message
                is PassportQrPayload.RenderResult.Ready -> null
            },
        returnStatus = displayTransaction?.status,
        returnActionError = action.error,
    )
}

@Composable
fun PartnerWorkbenchVisualScreen(
    user: User,
    onNavigate: (TopLevelDestination) -> Unit,
    onOpenPassport: (String) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val programmes by viewModel.programmes(user.id).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val syncCommands by viewModel.pendingSyncCommands().collectAsState(emptyList())
    val recoveryTasks =
        transactions.filter {
            it.partnerId == user.id && it.status !in
                setOf(
                    TransactionStatus.COMPLETED,
                    TransactionStatus.CANCELLED,
                    TransactionStatus.REJECTED,
                )
        }
    var editingProgramme by remember { mutableStateOf<CircularProgramme?>(null) }
    var creatingProgramme by rememberSaveable { mutableStateOf(false) }

    ReEventScaffold(selected = TopLevelDestination.WORKBENCH, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Partner workbench",
                    subtitle = "Manage programmes and assigned handovers",
                    onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
                )
            }
            item {
                PrimaryActionButton(
                    text = "Create circular programme",
                    onClick = { creatingProgramme = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                SecondaryActionButton(
                    text = "Open partner network",
                    onClick = { onNavigate(TopLevelDestination.PARTNERS) },
                    modifier = Modifier.fillMaxWidth(),
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
                    onDeactivate = { viewModel.deactivateProgramme(user, programme) },
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
                    onPassport = { onOpenPassport(transaction.resourceId) },
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
            },
        )
    }
    editingProgramme?.let { programme ->
        ProgrammeEditorDialog(
            programme = programme,
            onDismiss = { editingProgramme = null },
            onSave = { name, type, materials, location, active ->
                viewModel.saveProgramme(user, programme, name, type, materials, location, active)
                editingProgramme = null
            },
        )
    }
}

@Composable
private fun ProgrammeCard(
    programme: CircularProgramme,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(programme.name, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    Text(programme.type.displayLabel(), color = ReEventTextSecondary)
                }
                StatusChip(if (programme.active) "Active" else "Inactive", if (programme.active) ReEventGreen else ReEventTextSecondary)
            }
            Text(programme.location.ifBlank { "Location pending" }, color = ReEventTextSecondary)
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
    onSave: (String, ProgrammeType, List<String>, String, Boolean) -> Unit,
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
                OutlinedTextField(
                    name,
                    { name = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Programme name *") },
                    isError =
                        name.isNotBlank() && !valid,
                )
                ChoiceField("Type", type.displayLabel(), ProgrammeType.entries.map(ProgrammeType::displayLabel)) { selected ->
                    type = ProgrammeType.entries.first { it.displayLabel() == selected }
                }
                OutlinedTextField(materials, {
                    materials = it
                }, Modifier.fillMaxWidth(), label = { Text("Accepted materials") }, placeholder = { Text("Acrylic, Fabric") })
                OutlinedTextField(location, { location = it }, Modifier.fillMaxWidth(), label = { Text("Location") })
                ChoiceField("Status", if (active) "Active" else "Inactive", listOf("Active", "Inactive")) { active = it == "Active" }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(name, type, materials.split(","), location, active) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
