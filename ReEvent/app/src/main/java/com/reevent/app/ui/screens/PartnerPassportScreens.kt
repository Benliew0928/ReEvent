package com.reevent.app.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.BuildConfig
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.LegacyProgrammeDraft
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
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
    user: User,
    resourceId: String? = null,
    onNavigate: (TopLevelDestination) -> Unit,
    onOpenPassport: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: FeatureViewModel = hiltViewModel(),
    mapViewModel: PartnerMapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by mapViewModel.state.collectAsState()
    val resource by (resourceId?.let(viewModel::resource) ?: flowOf(null)).collectAsState(null)
    val marketplaceResources by viewModel.marketplace().collectAsState(emptyList())
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            mapViewModel.locationGranted()
        } else {
            val permanent = context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) == false
            mapViewModel.locationDenied(permanent)
        }
    }
    LaunchedEffect(user.id, resourceId) {
        viewModel.refresh()
        mapViewModel.load(resourceId)
    }
    PartnerMapScreen(
        user = user,
        state = state,
        resource = resource,
        marketplaceResources = marketplaceResources,
        onNavigate = onNavigate,
        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
        onBack = onBack,
        onMaterialChange = mapViewModel::setMaterial,
        onToggleType = mapViewModel::toggleType,
        onDistanceChange = mapViewModel::setMaximumDistance,
        onPickupChange = mapViewModel::setPickupOnly,
        onNearMe = {
            if (state.locationPermission == PartnerLocationPermission.PERMANENTLY_DENIED) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                    },
                )
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mapViewModel.locationGranted()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
        onPresentationChange = mapViewModel::setPresentation,
        onSelectCandidate = mapViewModel::select,
        onMapLoading = mapViewModel::mapLoading,
        onMapLoaded = mapViewModel::mapLoaded,
        onMapFailed = mapViewModel::mapFailed,
        onOpenPassport = onOpenPassport,
        onCreateHandover = { programme ->
            resource?.let { viewModel.createPartnerHandover(user, it, programme) }
        },
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
    val legacyDrafts by viewModel.legacyProgrammes(user.id).collectAsState(emptyList())
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
    var replacementLegacy by remember { mutableStateOf<LegacyProgrammeDraft?>(null) }

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
            if (legacyDrafts.isNotEmpty()) {
                item {
                    Text("Legacy programme inputs", style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                }
                item {
                    Text(
                        "These incomplete local inputs were not uploaded. Create a fresh inactive replacement and select a validated exact point before activation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReEventTextSecondary,
                    )
                }
                items(legacyDrafts, key = { "legacy-${it.id}" }) { legacy ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(legacy.name, style = MaterialTheme.typography.titleMedium)
                            Text("Old location: ${legacy.location.ifBlank { "Not supplied" }}", color = ReEventTextSecondary)
                            Text("This record is inactive and cannot be uploaded.", style = MaterialTheme.typography.bodySmall)
                            PrimaryActionButton(
                                "Create replacement",
                                { replacementLegacy = legacy },
                                Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
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
            onSave = { form ->
                viewModel.saveProgramme(
                    user, null, form.name, form.type, form.materials, form.categories, form.conditions,
                    form.minimumQuantity, form.maximumQuantity, form.unit, form.remainingCapacity,
                    form.pickupAvailable, form.coinDirection, form.unitCoinAmount, form.geoLocation,
                    form.processingMethod, form.terms, form.active,
                )
                creatingProgramme = false
            },
            viewModel = viewModel,
        )
    }
    editingProgramme?.let { programme ->
        ProgrammeEditorDialog(
            programme = programme,
            onDismiss = { editingProgramme = null },
            onSave = { form ->
                viewModel.saveProgramme(
                    user, programme, form.name, form.type, form.materials, form.categories, form.conditions,
                    form.minimumQuantity, form.maximumQuantity, form.unit, form.remainingCapacity,
                    form.pickupAvailable, form.coinDirection, form.unitCoinAmount, form.geoLocation,
                    form.processingMethod, form.terms, form.active,
                )
                editingProgramme = null
            },
            viewModel = viewModel,
        )
    }
    replacementLegacy?.let { legacy ->
        ProgrammeEditorDialog(
            programme = null,
            legacy = legacy,
            onDismiss = { replacementLegacy = null },
            onSave = { form ->
                viewModel.saveProgramme(
                    user, null, form.name, form.type, form.materials, form.categories, form.conditions,
                    form.minimumQuantity, form.maximumQuantity, form.unit, form.remainingCapacity,
                    form.pickupAvailable, form.coinDirection, form.unitCoinAmount, form.geoLocation,
                    form.processingMethod, form.terms, false, legacy.id,
                )
                replacementLegacy = null
            },
            viewModel = viewModel,
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

private data class ProgrammeForm(
    val name: String,
    val type: ProgrammeType,
    val materials: List<String>,
    val categories: List<String>,
    val conditions: Set<ResourceCondition>,
    val minimumQuantity: Double?,
    val maximumQuantity: Double?,
    val unit: String?,
    val remainingCapacity: Double?,
    val pickupAvailable: Boolean,
    val coinDirection: CoinDirection,
    val unitCoinAmount: Long?,
    val geoLocation: GeoLocation?,
    val processingMethod: String,
    val terms: String,
    val active: Boolean,
)

@Composable
private fun ProgrammeEditorDialog(
    programme: CircularProgramme?,
    onDismiss: () -> Unit,
    onSave: (ProgrammeForm) -> Unit,
    viewModel: FeatureViewModel,
    modifier: Modifier = Modifier,
    legacy: LegacyProgrammeDraft? = null,
) {
    val editorKey = programme?.id ?: legacy?.id
    var name by rememberSaveable(editorKey) { mutableStateOf(programme?.name ?: legacy?.name.orEmpty()) }
    var type by rememberSaveable(editorKey) { mutableStateOf(programme?.type ?: legacy?.type ?: ProgrammeType.REPAIR) }
    var materials by rememberSaveable(editorKey) {
        mutableStateOf(programme?.acceptedMaterials?.joinToString(", ") ?: legacy?.acceptedMaterials?.joinToString(", ").orEmpty())
    }
    var categories by rememberSaveable(editorKey) { mutableStateOf(programme?.acceptedCategories?.joinToString(", ").orEmpty()) }
    var conditions by remember(programme?.id) { mutableStateOf(programme?.acceptedConditions ?: ResourceCondition.entries.toSet()) }
    var minimum by rememberSaveable(programme?.id) { mutableStateOf(programme?.minimumQuantity?.toString().orEmpty()) }
    var maximum by rememberSaveable(programme?.id) { mutableStateOf(programme?.maximumQuantity?.toString().orEmpty()) }
    var unit by rememberSaveable(programme?.id) { mutableStateOf(programme?.unit.orEmpty()) }
    var capacity by rememberSaveable(programme?.id) { mutableStateOf(programme?.remainingCapacity?.toString().orEmpty()) }
    var pickup by rememberSaveable(programme?.id) { mutableStateOf(programme?.pickupAvailable ?: false) }
    var coinDirection by rememberSaveable(programme?.id) { mutableStateOf(programme?.coinDirection ?: CoinDirection.FREE) }
    var coinAmount by rememberSaveable(programme?.id) { mutableStateOf(programme?.unitCoinAmount?.toString().orEmpty()) }
    var geoLocation by remember(programme?.id) { mutableStateOf(programme?.geoLocation) }
    var processing by rememberSaveable(programme?.id) { mutableStateOf(programme?.processingMethod.orEmpty()) }
    var terms by rememberSaveable(programme?.id) { mutableStateOf(programme?.terms.orEmpty()) }
    var active by rememberSaveable(programme?.id) { mutableStateOf(programme?.active ?: false) }
    var choosingLocation by remember { mutableStateOf(false) }

    fun decimalOrNull(value: String): Double? = value.trim().takeIf(String::isNotBlank)?.toDoubleOrNull()
    val numericValuesValid =
        (minimum.isBlank() || minimum.toDoubleOrNull()?.let { it > 0 } == true) &&
            (maximum.isBlank() || maximum.toDoubleOrNull()?.let { it > 0 } == true) &&
            (capacity.isBlank() || capacity.toDoubleOrNull()?.let { it >= 0 } == true)
    val parsedMinimum = decimalOrNull(minimum)
    val parsedMaximum = decimalOrNull(maximum)
    val parsedCapacity = decimalOrNull(capacity)
    val amount = coinAmount.trim().takeIf(String::isNotBlank)?.toLongOrNull()
    val rangeValid = parsedMinimum == null || parsedMaximum == null || parsedMaximum >= parsedMinimum
    val unitRequired = listOf(minimum, maximum, capacity, coinAmount).all(String::isBlank) || unit.isNotBlank()
    val coinValid = if (coinDirection == CoinDirection.FREE) coinAmount.isBlank() else amount?.let { it > 0 } == true
    val directionValid = when (type) {
        ProgrammeType.REPAIR -> coinDirection in setOf(CoinDirection.FREE, CoinDirection.OWNER_PAYS_PARTNER)
        ProgrammeType.RECYCLE, ProgrammeType.BUY_BACK -> coinDirection in setOf(CoinDirection.FREE, CoinDirection.PARTNER_PAYS_OWNER)
    }
    val activationReady = name.trim().isNotBlank() && conditions.isNotEmpty() && geoLocation != null &&
        processing.isNotBlank() && terms.isNotBlank() && numericValuesValid && rangeValid && unitRequired && coinValid && directionValid
    val canSave = name.trim().isNotBlank() && name.length <= 120 && numericValuesValid && rangeValid && unitRequired &&
        coinValid && directionValid && (!active || activationReady)

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(if (programme == null) "Create programme" else "Edit programme") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "Inactive drafts may be incomplete. Active programmes require every marked field and a validated business point.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReEventTextSecondary,
                )
                if (legacy != null) {
                    Text(
                        "Prefilled from the legacy input at ${legacy.location.ifBlank { "an unspecified location" }}. This replacement is forced inactive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReEventTextSecondary,
                    )
                }
                OutlinedTextField(
                    name,
                    { name = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Programme name *") },
                    isError = name.length > 120 || active && name.isBlank(),
                )
                ChoiceField("Type", type.displayLabel(), ProgrammeType.entries.map(ProgrammeType::displayLabel)) { selected ->
                    type = ProgrammeType.entries.first { it.displayLabel() == selected }
                }
                OutlinedTextField(
                    materials,
                    { materials = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Accepted materials") },
                    placeholder = { Text("Acrylic, Fabric; blank means any") },
                )
                OutlinedTextField(
                    categories,
                    { categories = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Accepted categories") },
                    placeholder = { Text("Decor, Furniture; blank means any") },
                )
                Text("Accepted conditions *", style = MaterialTheme.typography.labelLarge)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ResourceCondition.entries.forEach { condition ->
                        FilterChip(
                            selected = condition in conditions,
                            onClick = {
                                conditions = if (condition in conditions) conditions - condition else conditions + condition
                            },
                            label = { Text(condition.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)) },
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(minimum, { minimum = it }, Modifier.weight(1f), label = { Text("Minimum") })
                    OutlinedTextField(maximum, { maximum = it }, Modifier.weight(1f), label = { Text("Maximum") })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(unit, { unit = it }, Modifier.weight(1f), label = { Text("Unit") }, placeholder = { Text("pieces") })
                    OutlinedTextField(capacity, { capacity = it }, Modifier.weight(1f), label = { Text("Capacity") })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = pickup, onCheckedChange = { pickup = it })
                    Text("Partner pickup available")
                }
                ChoiceField(
                    "ReCoin direction",
                    coinDirection.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase),
                    CoinDirection.entries.map { it.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase) },
                ) { selected ->
                    coinDirection = CoinDirection.entries.first { it.name.equals(selected.replace(' ', '_'), ignoreCase = true) }
                    if (coinDirection == CoinDirection.FREE) coinAmount = ""
                }
                if (coinDirection != CoinDirection.FREE) {
                    OutlinedTextField(
                        coinAmount,
                        { coinAmount = it },
                        Modifier.fillMaxWidth(),
                        label = { Text("ReCoin per $unit *") },
                    )
                }
                SecondaryActionButton(
                    text = if (geoLocation == null) "Choose exact business location *" else "Adjust business pin",
                    onClick = { choosingLocation = true },
                    modifier = Modifier.fillMaxWidth(),
                )
                geoLocation?.let {
                    Text("${it.displayAddress}\n${"%.6f".format(it.latitude)}, ${"%.6f".format(it.longitude)}", color = ReEventTextSecondary)
                }
                OutlinedTextField(
                    processing,
                    { processing = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Processing method *") },
                    minLines = 2,
                )
                OutlinedTextField(
                    terms,
                    { terms = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Terms *") },
                    minLines = 2,
                )
                if (legacy == null) {
                    ChoiceField("Status", if (active) "Active" else "Inactive draft", listOf("Active", "Inactive draft")) {
                        active = it == "Active"
                    }
                } else {
                    Text("Status: Inactive replacement", color = ReEventTextSecondary)
                }
                if (active && !activationReady) {
                    Text(
                        "Complete name, condition, exact location, processing, terms, quantity/unit and ReCoin rules before activation.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (!numericValuesValid || !rangeValid || !unitRequired || !coinValid || !directionValid) {
                    Text(
                        "Minimum/maximum must be positive, capacity non-negative, quantified values require a unit, and ReCoin direction must suit the programme type.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        ProgrammeForm(
                            name = name,
                            type = type,
                            materials = materials.split(","),
                            categories = categories.split(","),
                            conditions = conditions,
                            minimumQuantity = parsedMinimum,
                            maximumQuantity = parsedMaximum,
                            unit = unit.takeIf(String::isNotBlank),
                            remainingCapacity = parsedCapacity,
                            pickupAvailable = pickup,
                            coinDirection = coinDirection,
                            unitCoinAmount = amount,
                            geoLocation = geoLocation,
                            processingMethod = processing,
                            terms = terms,
                            active = active,
                        ),
                    )
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )

    if (choosingLocation) {
        LocationPickerDialog(
            initialLocation = geoLocation,
            onDismiss = { choosingLocation = false },
            onSelected = {
                geoLocation = it
                choosingLocation = false
            },
            search = viewModel::searchPlaces,
            reverse = viewModel::reversePlace,
            initialQuery = legacy?.location.orEmpty(),
        )
    }
}
