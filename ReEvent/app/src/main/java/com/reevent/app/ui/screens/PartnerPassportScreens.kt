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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.BuildConfig
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.LegacyProgrammeDraft
import com.reevent.app.core.model.MaterialCatalog
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.feature.passports.PassportQrPayload
import com.reevent.app.feature.passports.PassportViewerAccessPolicy
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.EditorialDetailHeader
import com.reevent.app.ui.components.EditorialDetailScaffold
import com.reevent.app.ui.components.EditorialEmptyState
import com.reevent.app.ui.components.EditorialNotice
import com.reevent.app.ui.components.EditorialSectionCard
import com.reevent.app.ui.components.EditorialStat
import com.reevent.app.ui.components.EditorialTextAction
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.materials.MaterialFamilyMultiSelectField
import com.reevent.app.ui.materials.MaterialFamilyIcon
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGreetingStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLabelStyle
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSupportingInk
import com.reevent.app.ui.theme.HomeSupportingTextStyle
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
    val action by viewModel.action.collectAsState()
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
        profileName = user.displayName,
        lifecycleActions = resource?.let { item ->
            PassportLifecycleActionPolicy.availableActions(user, item, viewerTransactions)
        }.orEmpty(),
        onLifecycleAction = resource?.let { item ->
            { lifecycleAction -> viewModel.applyLifecycleAction(user, item, lifecycleAction) }
        },
        lifecycleActionLoading = action.loading,
        lifecycleActionNotice = action.notice,
        lifecycleActionError = action.error,
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
fun PartnerProgrammesVisualScreen(
    user: User,
    onNavigate: (TopLevelDestination) -> Unit,
    onOpenPassport: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusedTransactionId: String? = null,
    startCreating: Boolean = false,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val programmes by viewModel.programmes(user.id).collectAsState(emptyList())
    val legacyDrafts by viewModel.legacyProgrammes(user.id).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val syncCommands by viewModel.pendingSyncCommands().collectAsState(emptyList())
    val action by viewModel.action.collectAsState()
    val recoveryTasks =
        transactions.filter {
            it.partnerId == user.id && it.status !in
                setOf(
                    TransactionStatus.COMPLETED,
                    TransactionStatus.CANCELLED,
                    TransactionStatus.REJECTED,
                )
        }.sortedByDescending { it.id == focusedTransactionId }
    val focusedTask = recoveryTasks.firstOrNull { it.id == focusedTransactionId }
    var editingProgramme by remember { mutableStateOf<CircularProgramme?>(null) }
    var creatingProgramme by rememberSaveable { mutableStateOf(startCreating) }
    var replacementLegacy by remember { mutableStateOf<LegacyProgrammeDraft?>(null) }

    EditorialDetailScaffold(
        selected = TopLevelDestination.PROGRAMMES,
        onNavigate = onNavigate,
        modifier = modifier,
    ) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                EditorialDetailHeader(
                    eyebrow = "Partner workspace",
                    title = "Circular programmes",
                    subtitle = if (focusedTransactionId == null) {
                        "Shape the routes that keep materials in motion."
                    } else {
                        "A priority task is open below, with its next authorised action."
                    },
                    onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
                    profileName = user.displayName,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ProgrammesOverviewCard(
                    activeProgrammes = programmes.count(CircularProgramme::active),
                    totalProgrammes = programmes.size,
                    taskCount = recoveryTasks.size,
                    onCreate = { creatingProgramme = true },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            action.error?.let { message ->
                item { EditorialNotice(message, modifier = Modifier.fillMaxWidth(), isError = true) }
            }
            action.notice?.let { message ->
                item { EditorialNotice(message, modifier = Modifier.fillMaxWidth()) }
            }
            if (focusedTransactionId != null) {
                if (focusedTask == null) {
                    item {
                        EditorialEmptyState(
                            title = "Programme task unavailable",
                            detail = "This task is complete or no longer authorised for this account.",
                            icon = Icons.Outlined.TaskAlt,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    item { ProgrammeSectionHeading("Priority task", "Opened from your partner inbox") }
                    item {
                        val resource by viewModel.resource(focusedTask.resourceId).collectAsState(null)
                        TransactionCard(
                            user = user,
                            transaction = focusedTask,
                            resource = resource,
                            syncCommand = syncCommands.firstOrNull { it.transactionId == focusedTask.id },
                            onApprove = { viewModel.approveTransaction(user, focusedTask) },
                            onCancel = { viewModel.cancelTransaction(user, focusedTask) },
                            onComplete = { viewModel.completeTransaction(user, focusedTask) },
                            onInTransit = { viewModel.moveTransactionInTransit(user, focusedTask) },
                            onPassport = { onOpenPassport(focusedTask.resourceId) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            if (programmes.isEmpty()) {
                item {
                    EditorialEmptyState(
                        title = "No programmes yet",
                        detail = "Create a programme so organisers can discover your circular services and request a route.",
                        icon = Icons.Outlined.Inventory2,
                        actionLabel = "Create a circular programme",
                        onAction = { creatingProgramme = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                item { ProgrammeSectionHeading("Your programmes", "${programmes.size} configured route${if (programmes.size == 1) "" else "s"}") }
            }
            items(programmes, key = { it.id }) { programme ->
                ProgrammeCard(
                    programme = programme,
                    onEdit = { editingProgramme = programme },
                    onDeactivate = { viewModel.deactivateProgramme(user, programme) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (legacyDrafts.isNotEmpty()) {
                item {
                    ProgrammeSectionHeading(
                        "Draft recovery",
                        "Older local inputs need a validated location before they can become programmes",
                    )
                }
                items(legacyDrafts, key = { "legacy-${it.id}" }) { legacy ->
                    LegacyProgrammeCard(
                        legacy = legacy,
                        onReplace = { replacementLegacy = legacy },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (recoveryTasks.isEmpty()) {
                item {
                    EditorialEmptyState(
                        title = "No assigned handovers",
                        detail = "Accepted marketplace and programme requests will appear here when your team has a lifecycle step to complete.",
                        icon = Icons.Outlined.TaskAlt,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                item { ProgrammeSectionHeading("Assigned recovery tasks", "${recoveryTasks.size} active") }
            }
            items(recoveryTasks.filterNot { it.id == focusedTask?.id }, key = { it.id }) { transaction ->
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
                    modifier = Modifier.fillMaxWidth(),
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
                    user, null, form.name, form.type, form.materialFamilies, form.categories, form.conditions,
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
                    user, programme, form.name, form.type, form.materialFamilies, form.categories, form.conditions,
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
                    user, null, form.name, form.type, form.materialFamilies, form.categories, form.conditions,
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
    modifier: Modifier = Modifier,
) {
    EditorialSectionCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Surface(shape = CircleShape, color = HomeSage) {
                    Icon(
                        imageVector = programme.type.programmeIcon(),
                        contentDescription = null,
                        tint = HomeForest,
                        modifier = Modifier.padding(12.dp).size(28.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        programme.type.displayLabel().uppercase(),
                        style = HomeSupportingTextStyle.copy(fontSize = 11.sp, letterSpacing = .8.sp),
                        color = HomeSupportingInk,
                    )
                    Text(
                        programme.name,
                        style = HomeCardTitleStyle.copy(fontSize = 26.sp, lineHeight = 28.sp),
                        color = HomeInk,
                    )
                }
                ProgrammeStatusPill(programme.active)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.LocationOn, null, tint = HomeForest, modifier = Modifier.size(19.dp))
                Text(
                    programme.location.ifBlank { "Location pending" },
                    style = HomeSupportingTextStyle,
                    color = HomeMuted,
                    modifier = Modifier.weight(1f),
                )
            }
            ProgrammeCapacityStrip(programme = programme, modifier = Modifier.fillMaxWidth())
            Text("MATERIAL ROUTES", style = HomeSupportingTextStyle.copy(fontSize = 11.sp, letterSpacing = .8.sp), color = HomeMuted)
            if (programme.acceptedMaterialFamilies.isEmpty()) {
                Surface(shape = CircleShape, color = HomeMist) {
                    Text(
                        "All material families",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        style = HomeSupportingTextStyle,
                        color = HomeForest,
                    )
                }
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    programme.acceptedMaterialFamilies.sortedBy(MaterialFamily::ordinal).forEach { family ->
                        Surface(shape = CircleShape, color = HomeMist, border = BorderStroke(1.dp, HomeLine)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                MaterialFamilyIcon(family, modifier = Modifier.size(17.dp), contentDescription = null)
                                Text(family.displayLabel, style = HomeSupportingTextStyle, color = HomeForest)
                            }
                        }
                    }
                }
            }
            PrimaryActionButton("Edit programme", onEdit, Modifier.fillMaxWidth())
            if (programme.active) {
                SecondaryActionButton("Deactivate programme", onDeactivate, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ProgrammesOverviewCard(
    activeProgrammes: Int,
    totalProgrammes: Int,
    taskCount: Int,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), color = HomeForest) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "PROGRAMME OVERVIEW",
                color = HomeSage,
                fontFamily = HomeBodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProgrammeOverviewStat(activeProgrammes.toString(), "Active", Modifier.weight(1f))
                ProgrammeOverviewStat(totalProgrammes.toString(), "Configured", Modifier.weight(1f))
                ProgrammeOverviewStat(taskCount.toString(), "Tasks", Modifier.weight(1f))
            }
            EditorialTextAction(
                label = "Create circular programme",
                onClick = onCreate,
                icon = Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProgrammeOverviewStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value,
            style = HomeGreetingStyle,
            color = Color.White,
        )
        Text(label, style = HomeSupportingTextStyle, color = HomeSage)
    }
}

@Composable
private fun ProgrammeSectionHeading(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = HomeGreetingStyle.copy(fontSize = 29.sp), color = HomeInk)
        Text(subtitle, style = HomeSupportingTextStyle, color = HomeMuted)
    }
}

@Composable
private fun ProgrammeStatusPill(
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (active) HomeSage else HomeMist,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(7.dp), shape = CircleShape, color = if (active) HomeForest else HomeSupportingInk) {}
            Text(
                if (active) "ACTIVE" else "INACTIVE",
                style = HomeSupportingTextStyle.copy(fontSize = 10.sp),
                color = HomeForest,
            )
        }
    }
}

@Composable
private fun ProgrammeCapacityStrip(
    programme: CircularProgramme,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(15.dp), color = HomeMist) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EditorialStat(
                value = programme.remainingCapacity?.programmeNumber() ?: "—",
                label = programme.unit?.let { "${it.lowercase()} remaining" } ?: "Capacity not set",
                modifier = Modifier.weight(1f),
            )
            Box(modifier = Modifier.size(width = 1.dp, height = 44.dp).background(HomeLine))
            EditorialStat(
                value = if (programme.pickupAvailable) "Yes" else "No",
                label = "Pickup available",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LegacyProgrammeCard(
    legacy: LegacyProgrammeDraft,
    onReplace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorialSectionCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text("LOCAL DRAFT", style = HomeLabelStyle, color = HomeSupportingInk)
            Text(legacy.name, style = HomeCardTitleStyle.copy(fontSize = 25.sp), color = HomeInk)
            Text(
                "Old location: ${legacy.location.ifBlank { "Not supplied" }}",
                style = HomeSupportingTextStyle,
                color = HomeMuted,
            )
            EditorialNotice(
                message = "This input is inactive and cannot be uploaded until its rules and exact location are validated.",
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryActionButton("Create replacement", onReplace, Modifier.fillMaxWidth())
        }
    }
}

private fun ProgrammeType.programmeIcon() = when (this) {
    ProgrammeType.REPAIR -> Icons.Outlined.Build
    ProgrammeType.RECYCLE -> Icons.Outlined.Recycling
    ProgrammeType.BUY_BACK -> Icons.Outlined.Paid
}

private fun Double.programmeNumber(): String =
    if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

private data class ProgrammeForm(
    val name: String,
    val type: ProgrammeType,
    val materialFamilies: Set<MaterialFamily>,
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
    var materialFamilies by remember(editorKey) {
        mutableStateOf(
            programme?.acceptedMaterialFamilies
                ?: legacy?.acceptedMaterials?.map { MaterialCatalog.resolveLegacy(it).family }?.toSet()
                ?: emptySet(),
        )
    }
    var categories by rememberSaveable(editorKey) { mutableStateOf(programme?.acceptedCategories?.joinToString(", ").orEmpty()) }
    var conditions by remember(editorKey) { mutableStateOf(programme?.acceptedConditions ?: ResourceCondition.entries.toSet()) }
    var minimum by rememberSaveable(editorKey) { mutableStateOf(programme?.minimumQuantity?.toString().orEmpty()) }
    var maximum by rememberSaveable(editorKey) { mutableStateOf(programme?.maximumQuantity?.toString().orEmpty()) }
    var unit by rememberSaveable(editorKey) { mutableStateOf(programme?.unit.orEmpty()) }
    var capacity by rememberSaveable(editorKey) { mutableStateOf(programme?.remainingCapacity?.toString().orEmpty()) }
    var pickup by rememberSaveable(editorKey) { mutableStateOf(programme?.pickupAvailable ?: false) }
    var coinDirection by rememberSaveable(editorKey) { mutableStateOf(programme?.coinDirection ?: CoinDirection.FREE) }
    var coinAmount by rememberSaveable(editorKey) { mutableStateOf(programme?.unitCoinAmount?.toString().orEmpty()) }
    var geoLocation by remember(editorKey) { mutableStateOf(programme?.geoLocation) }
    var processing by rememberSaveable(editorKey) { mutableStateOf(programme?.processingMethod.orEmpty()) }
    var terms by rememberSaveable(editorKey) { mutableStateOf(programme?.terms.orEmpty()) }
    var active by rememberSaveable(editorKey) { mutableStateOf(programme?.active ?: false) }
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
    val validCoinDirections = when (type) {
        ProgrammeType.REPAIR -> listOf(CoinDirection.FREE, CoinDirection.OWNER_PAYS_PARTNER)
        ProgrammeType.RECYCLE, ProgrammeType.BUY_BACK -> listOf(CoinDirection.FREE, CoinDirection.PARTNER_PAYS_OWNER)
    }
    val activationReady = name.trim().isNotBlank() && conditions.isNotEmpty() && geoLocation != null &&
        processing.isNotBlank() && terms.isNotBlank() && numericValuesValid && rangeValid && unitRequired && coinValid && directionValid
    val canSave = name.trim().isNotBlank() && name.length <= 120 && numericValuesValid && rangeValid && unitRequired &&
        coinValid && directionValid && (!active || activationReady)
    val activationMissing = buildList {
        if (name.isBlank()) add("programme name")
        if (conditions.isEmpty()) add("accepted condition")
        if (geoLocation == null) add("business location")
        if (processing.isBlank()) add("processing method")
        if (terms.isBlank()) add("programme terms")
        if (!numericValuesValid || !rangeValid) add("valid quantities")
        if (!unitRequired) add("quantity unit")
        if (!coinValid || !directionValid) add("valid ReCoin terms")
    }
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HomeForest,
        unfocusedBorderColor = HomeLine,
        focusedLabelColor = HomeForest,
        unfocusedLabelColor = HomeSupportingInk,
        cursorColor = HomeForest,
        focusedContainerColor = HomePaper,
        unfocusedContainerColor = HomePaper,
        errorContainerColor = HomePaper,
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .imePadding()
                .testTag("programme_editor_dialog"),
            shape = RoundedCornerShape(30.dp),
            color = HomeCanvas,
            border = BorderStroke(1.dp, HomeLine),
            shadowElevation = 14.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, top = 20.dp, end = 14.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("PARTNER PROGRAMME", style = HomeLabelStyle, color = HomeForest)
                        Text(
                            if (programme == null) "Create a programme" else "Edit programme",
                            style = HomeGreetingStyle,
                            color = HomeInk,
                        )
                        Text(
                            "Set clear eligibility, capacity and handover terms for organisers.",
                            style = HomeSupportingTextStyle,
                            color = HomeSupportingInk,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close programme editor", tint = HomeForest)
                    }
                }

                HorizontalDivider(color = HomeLine)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = HomePaper,
                        border = BorderStroke(1.dp, HomeLine),
                    ) {
                        Text(
                            if (active) {
                                "Publishing is on. Complete every required field before the programme can go live."
                            } else {
                                "Saving as a draft. Add a name now, then complete the remaining details before publishing."
                            },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            style = HomeSupportingTextStyle,
                            color = HomeInk,
                        )
                    }

                    if (legacy != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = HomePaper,
                            border = BorderStroke(1.dp, HomeLine),
                        ) {
                            Text(
                                "Prefilled from the legacy input at ${legacy.location.ifBlank { "an unspecified location" }}. This replacement remains inactive until reviewed.",
                                modifier = Modifier.padding(14.dp),
                                style = HomeSupportingTextStyle,
                                color = HomeSupportingInk,
                            )
                        }
                    }

                    ProgrammeFormSection(
                        step = "01",
                        title = "Programme basics",
                        helper = "Give organisers a recognisable name and choose the service you provide.",
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(120) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Programme name *") },
                            placeholder = { Text("e.g. Community textile repair") },
                            supportingText = { Text("${name.length}/120") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors,
                            isError = active && name.isBlank(),
                        )
                        ProgrammeChoiceLabel("Programme type *")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ProgrammeType.entries.forEach { option ->
                                EditorialChoiceChip(
                                    label = option.displayLabel(),
                                    selected = type == option,
                                    onClick = {
                                        type = option
                                        val allowed = when (option) {
                                            ProgrammeType.REPAIR -> setOf(CoinDirection.FREE, CoinDirection.OWNER_PAYS_PARTNER)
                                            ProgrammeType.RECYCLE, ProgrammeType.BUY_BACK -> setOf(CoinDirection.FREE, CoinDirection.PARTNER_PAYS_OWNER)
                                        }
                                        if (coinDirection !in allowed) {
                                            coinDirection = CoinDirection.FREE
                                            coinAmount = ""
                                        }
                                    },
                                )
                            }
                        }
                    }

                    ProgrammeFormSection(
                        step = "02",
                        title = "Accepted resources",
                        helper = "Leave materials or categories blank when the programme accepts any suitable item.",
                    ) {
                        MaterialFamilyMultiSelectField(materialFamilies, { materialFamilies = it }, Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = categories,
                            onValueChange = { categories = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Accepted categories") },
                            placeholder = { Text("Decor, furniture, signage") },
                            supportingText = { Text("Separate categories with commas; blank means any category.") },
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors,
                        )
                        ProgrammeChoiceLabel("Accepted conditions *")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ResourceCondition.entries.forEach { condition ->
                                EditorialChoiceChip(
                                    label = condition.displayLabel(),
                                    selected = condition in conditions,
                                    onClick = {
                                        conditions = if (condition in conditions) conditions - condition else conditions + condition
                                    },
                                )
                            }
                        }
                        if (conditions.isEmpty()) {
                            Text(
                                "Select at least one condition before publishing.",
                                style = HomeSupportingTextStyle,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    ProgrammeFormSection(
                        step = "03",
                        title = "Quantity and pickup",
                        helper = "Set optional intake limits. If any quantity is entered, its unit becomes required.",
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = minimum,
                                onValueChange = { minimum = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Minimum") },
                                placeholder = { Text("Optional") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                isError = minimum.isNotBlank() && parsedMinimum?.let { it <= 0 } != false,
                            )
                            OutlinedTextField(
                                value = maximum,
                                onValueChange = { maximum = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Maximum") },
                                placeholder = { Text("Optional") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                isError = maximum.isNotBlank() && (parsedMaximum?.let { it <= 0 } != false || !rangeValid),
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Unit${if (unitRequired) "" else " *"}") },
                                placeholder = { Text("items, kg") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                isError = !unitRequired,
                            )
                            OutlinedTextField(
                                value = capacity,
                                onValueChange = { capacity = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Capacity") },
                                placeholder = { Text("Optional") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                isError = capacity.isNotBlank() && parsedCapacity?.let { it < 0 } != false,
                            )
                        }
                        if (!numericValuesValid || !rangeValid || !unitRequired) {
                            Text(
                                when {
                                    !rangeValid -> "Maximum quantity must be greater than or equal to minimum quantity."
                                    !unitRequired -> "Add a unit for the quantity or ReCoin values entered."
                                    else -> "Minimum and maximum must be above 0; capacity must be 0 or more."
                                },
                                style = HomeSupportingTextStyle,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { pickup = !pickup },
                            shape = RoundedCornerShape(16.dp),
                            color = HomePaper,
                            border = BorderStroke(1.dp, HomeLine),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Partner pickup available", style = HomeBodyStyle, color = HomeInk)
                                    Text("Turn on when your team can collect resources from the organiser.", style = HomeSupportingTextStyle, color = HomeSupportingInk)
                                }
                                Switch(checked = pickup, onCheckedChange = { pickup = it })
                            }
                        }
                    }

                    ProgrammeFormSection(
                        step = "04",
                        title = "ReCoin arrangement",
                        helper = "Choose who pays whom. Available options change with the programme type.",
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            validCoinDirections.forEach { option ->
                                EditorialChoiceChip(
                                    label = option.displayLabel(),
                                    selected = coinDirection == option,
                                    onClick = {
                                        coinDirection = option
                                        if (option == CoinDirection.FREE) coinAmount = ""
                                    },
                                )
                            }
                        }
                        Text(coinDirection.explanation(), style = HomeSupportingTextStyle, color = HomeSupportingInk)
                        if (coinDirection != CoinDirection.FREE) {
                            OutlinedTextField(
                                value = coinAmount,
                                onValueChange = { coinAmount = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("ReCoin per ${unit.ifBlank { "unit" }} *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors,
                                isError = !coinValid,
                                supportingText = {
                                    if (!coinValid) Text("Enter a whole number above 0.")
                                },
                            )
                        }
                    }

                    ProgrammeFormSection(
                        step = "05",
                        title = "Location and service details",
                        helper = "These details help organisers understand the handover before requesting it.",
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { choosingLocation = true },
                            shape = RoundedCornerShape(16.dp),
                            color = HomePaper,
                            border = BorderStroke(1.dp, if (active && geoLocation == null) MaterialTheme.colorScheme.error else HomeLine),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = HomeForest, modifier = Modifier.size(24.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        if (geoLocation == null) "Choose business location *" else "Business location",
                                        style = HomeBodyStyle,
                                        color = HomeInk,
                                    )
                                    Text(
                                        geoLocation?.displayAddress ?: "Set the exact point organisers should use.",
                                        style = HomeSupportingTextStyle,
                                        color = HomeSupportingInk,
                                    )
                                }
                                Text(if (geoLocation == null) "Choose" else "Change", style = HomeSupportingTextStyle, color = HomeForest)
                            }
                        }
                        geoLocation?.let {
                            Text(
                                "${"%.6f".format(it.latitude)}, ${"%.6f".format(it.longitude)}",
                                style = HomeSupportingTextStyle,
                                color = HomeSupportingInk,
                            )
                        }
                        OutlinedTextField(
                            value = processing,
                            onValueChange = { processing = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Processing method *") },
                            placeholder = { Text("Explain how resources are repaired, recycled or bought back") },
                            minLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors,
                            isError = active && processing.isBlank(),
                        )
                        OutlinedTextField(
                            value = terms,
                            onValueChange = { terms = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Programme terms *") },
                            placeholder = { Text("Collection hours, preparation rules and handover requirements") },
                            minLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            colors = fieldColors,
                            isError = active && terms.isBlank(),
                        )
                    }

                    ProgrammeFormSection(
                        step = "06",
                        title = "Publishing status",
                        helper = "Drafts stay private. Published programmes can appear in matching and partner discovery.",
                    ) {
                        if (legacy == null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { active = !active },
                                shape = RoundedCornerShape(16.dp),
                                color = HomePaper,
                                border = BorderStroke(1.dp, if (active) HomeForest else HomeLine),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Publish programme now", style = HomeBodyStyle, color = HomeInk)
                                        Text(
                                            if (active) "The programme will be visible after saving." else "Keep this off to save an incomplete draft.",
                                            style = HomeSupportingTextStyle,
                                            color = HomeSupportingInk,
                                        )
                                    }
                                    Switch(checked = active, onCheckedChange = { active = it })
                                }
                            }
                        } else {
                            Text("This legacy replacement will be saved as an inactive draft.", style = HomeSupportingTextStyle, color = HomeSupportingInk)
                        }
                        if (active && activationMissing.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                            ) {
                                Text(
                                    "Before publishing, complete: ${activationMissing.joinToString()}.",
                                    modifier = Modifier.padding(12.dp),
                                    style = HomeSupportingTextStyle,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = HomeLine)
                Surface(color = HomePaper) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = HomeForest, style = HomeBodyStyle)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            if (!canSave) {
                                Text(
                                    when {
                                        name.isBlank() -> "Add a programme name to continue."
                                        active && !activationReady -> "Complete the publishing requirements above."
                                        else -> "Review the highlighted fields."
                                    },
                                    style = HomeSupportingTextStyle,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        Button(
                            enabled = canSave,
                            onClick = {
                                onSave(
                                    ProgrammeForm(
                                        name = name,
                                        type = type,
                                        materialFamilies = materialFamilies,
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
                            modifier = Modifier.testTag("programme_editor_save"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HomeForest,
                                contentColor = Color.White,
                                disabledContainerColor = HomeLine,
                                disabledContentColor = HomeSupportingInk,
                            ),
                        ) {
                            Text(if (active) "Publish" else "Save draft", style = HomeBodyStyle)
                        }
                    }
                }
            }
        }
    }

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

@Composable
private fun ProgrammeFormSection(
    step: String,
    title: String,
    helper: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = HomeSage.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HomeForest,
                ) {
                    Text(
                        step,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        style = HomeLabelStyle,
                        color = Color.White,
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = HomeCardTitleStyle, color = HomeInk)
                    Text(helper, style = HomeSupportingTextStyle, color = HomeSupportingInk)
                }
            }
            content()
        }
    }
}

@Composable
private fun ProgrammeChoiceLabel(text: String) {
    Text(text, style = HomeLabelStyle, color = HomeForest)
}

@Composable
private fun EditorialChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = HomeSupportingTextStyle) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = HomePaper,
            labelColor = HomeInk,
            selectedContainerColor = HomeForest,
            selectedLabelColor = Color.White,
        ),
    )
}

private fun ResourceCondition.displayLabel(): String = name
    .lowercase()
    .replace('_', ' ')
    .replaceFirstChar(Char::titlecase)

private fun CoinDirection.displayLabel(): String = when (this) {
    CoinDirection.FREE -> "Free"
    CoinDirection.OWNER_PAYS_PARTNER -> "Owner pays partner"
    CoinDirection.PARTNER_PAYS_OWNER -> "Partner pays owner"
}

private fun CoinDirection.explanation(): String = when (this) {
    CoinDirection.FREE -> "No ReCoins are exchanged for this service."
    CoinDirection.OWNER_PAYS_PARTNER -> "The resource owner pays the partner for each accepted unit."
    CoinDirection.PARTNER_PAYS_OWNER -> "The partner rewards the resource owner for each accepted unit."
}
