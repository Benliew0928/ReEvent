package com.reevent.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.reevent.app.core.data.MarketplaceListingDraftRules
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.data.SyncCommandStatus
import com.reevent.app.core.data.TransactionLifecycleCardAction
import com.reevent.app.core.data.TransactionLifecyclePresentationRules
import com.reevent.app.core.data.TransactionLifecycleSyncFeedback
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.marketplace.MarketplaceDashboardViewModel
import com.reevent.app.ui.marketplace.MaterialCompassMarketplaceScreen
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventTextSecondary
import kotlinx.coroutines.flow.flowOf

@Composable
fun FocusedMarketplaceTransactionScreen(
    user: User,
    transactionId: String,
    onPassport: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id, transactionId) { viewModel.refresh() }
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val transaction = transactions.firstOrNull { it.id == transactionId }
    val resource by (transaction?.resourceId?.let(viewModel::resource) ?: flowOf(null)).collectAsState(null)
    val syncCommands by viewModel.pendingSyncCommands().collectAsState(emptyList())
    ReEventScaffold(
        selected = TopLevelDestination.MARKETPLACE,
        onNavigate = onNavigate,
        modifier = modifier,
    ) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Lifecycle request",
                    subtitle = "Focused from your priority inbox",
                    onBack = onBack,
                    onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
                )
            }
            if (transaction == null) {
                item {
                    EmptyMarketplacePanel(
                        "Request unavailable",
                        "This transaction is no longer available to this account. Refresh or return to the dashboard.",
                    )
                }
            } else {
                item {
                    TransactionCard(
                        user = user,
                        transaction = transaction,
                        resource = resource,
                        syncCommand = syncCommands.firstOrNull { it.transactionId == transaction.id },
                        onApprove = { viewModel.approveTransaction(user, transaction) },
                        onCancel = { viewModel.cancelTransaction(user, transaction) },
                        onComplete = { viewModel.completeTransaction(user, transaction) },
                        onInTransit = { viewModel.moveTransactionInTransit(user, transaction) },
                        onPassport = { onPassport(transaction.resourceId) },
                    )
                }
            }
        }
    }
}

@Composable
fun MarketplaceVisualScreen(
    user: User,
    onPassport: (String) -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
    dashboardViewModel: MarketplaceDashboardViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { dashboardViewModel.load(user) }
    val state by dashboardViewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    var selectedListing by remember { mutableStateOf<com.reevent.app.core.model.ResourceItem?>(null) }
    var selectedRequest by remember { mutableStateOf<Pair<com.reevent.app.core.model.ResourceItem, TransactionType>?>(null) }
    var publicationResource by remember { mutableStateOf<com.reevent.app.core.model.ResourceItem?>(null) }
    MaterialCompassMarketplaceScreen(
        user = user,
        state = state,
        onQuery = dashboardViewModel::setQuery,
        onFamily = dashboardViewModel::setMaterialFamily,
        onAction = dashboardViewModel::setAction,
        onCompassPage = dashboardViewModel::setCompassPage,
        onClearFilters = dashboardViewModel::clearFilters,
        onRefresh = dashboardViewModel::refresh,
        onNavigate = onNavigate,
        onListing = { selectedListing = it },
        onRequest = { resource, type -> selectedRequest = resource to type },
        onPassport = onPassport,
        onPublish = { publicationResource = it },
        onApprove = { viewModel.approveTransaction(user, it) },
        onCancel = { viewModel.cancelTransaction(user, it) },
        onComplete = { viewModel.completeTransaction(user, it) },
        onInTransit = { viewModel.moveTransactionInTransit(user, it) },
    )
    selectedListing?.let { resource ->
        MarketplaceListingDetailDialog(
            resource = resource,
            isOwner = resource.ownerId == user.id,
            onDismiss = { selectedListing = null },
            onOpenPassport = { selectedListing = null; onPassport(resource.id) },
            onRequest = {
                selectedListing = null
                resource.availableMarketplaceTypes().firstOrNull()?.let { selectedRequest = resource to it }
            },
        )
    }
    selectedRequest?.let { (resource, type) ->
        MarketplaceRequestDialog(
            resource = resource,
            initialType = type,
            onDismiss = { selectedRequest = null },
            onSubmit = { requestType, quantity ->
                viewModel.requestMarketplaceResource(user, resource, requestType, quantity)
                selectedRequest = null
            },
        )
    }
    publicationResource?.let { resource ->
        MarketplacePublishDialog(
            resource = resource,
            loading = action.loading,
            actionError = action.error,
            onDismiss = { publicationResource = null },
            onPublish = { draft ->
                viewModel.publishMarketplaceListing(user, resource, draft) { publicationResource = null }
            },
        )
    }
}

@Composable
private fun MarketplacePublishDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    loading: Boolean,
    actionError: String?,
    onDismiss: () -> Unit,
    onPublish: (MarketplaceListingDraft) -> Unit,
) {
    var selectedActions by remember(resource.id) { mutableStateOf(setOf(TransactionType.DONATE)) }
    var quantity by remember(resource.id) { mutableStateOf(ResourcePresentationRules.quantityNumber(resource.quantity)) }
    var buyPrice by remember(resource.id) { mutableStateOf("") }
    var rentPrice by remember(resource.id) { mutableStateOf("") }
    var durationDays by remember(resource.id) { mutableStateOf("") }
    var terms by remember(resource.id) { mutableStateOf("") }
    var submitted by remember(resource.id) { mutableStateOf(false) }
    val orderedActions =
        listOf(
            TransactionType.BORROW,
            TransactionType.RENT,
            TransactionType.BUY,
            TransactionType.DONATE,
            TransactionType.EXCHANGE,
        )
    val draft =
        MarketplaceListingDraft(
            allowedActions = selectedActions,
            publishedQuantity = quantity.toDoubleOrNull() ?: Double.NaN,
            buyUnitPrice = buyPrice.toLongOrNull().takeIf { TransactionType.BUY in selectedActions },
            rentUnitPrice = rentPrice.toLongOrNull().takeIf { TransactionType.RENT in selectedActions },
            defaultDurationDays =
                durationDays.toIntOrNull().takeIf {
                    selectedActions.any { action -> action == TransactionType.BORROW || action == TransactionType.RENT }
                },
            terms = terms,
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
                    color = ReEventTextSecondary,
                )
                Text("Available actions", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                orderedActions.chunked(2).forEach { actionRow ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        actionRow.forEach { action ->
                            FilterChip(
                                selected = action in selectedActions,
                                onClick = {
                                    selectedActions =
                                        if (action in selectedActions) {
                                            selectedActions - action
                                        } else {
                                            selectedActions + action
                                        }
                                },
                                label = { Text(action.displayLabel()) },
                                enabled = !loading,
                                modifier = Modifier.weight(1f),
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
                    modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                OutlinedTextField(
                    value = terms,
                    onValueChange = { typed -> if (typed.length <= MarketplaceListingDraftRules.MAX_TERMS_LENGTH + 1) terms = typed },
                    label = { Text("Listing terms (optional)") },
                    supportingText = {
                        if (showsErrors) {
                            validation.termsError?.let { Text(it) }
                        } else {
                            Text("${terms.length}/${MarketplaceListingDraftRules.MAX_TERMS_LENGTH}")
                        }
                    },
                    isError = showsErrors && validation.termsError != null,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showsErrors && actionError != null) {
                    Text(
                        "Publishing was not confirmed. Your values are kept; refresh Marketplace before trying again so a duplicate listing is not created.",
                        color = ReEventCoral,
                        style = MaterialTheme.typography.bodySmall,
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
                enabled = !loading,
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
        },
    )
}

@Composable
private fun MarketplaceListingDetailDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onOpenPassport: () -> Unit,
    onRequest: () -> Unit,
) {
    val listing = resource.marketplaceListing ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(resource.title) },
        text = { MarketplaceListingDetails(resource) },
        confirmButton = {
            if (isOwner) {
                TextButton(onClick = onDismiss) { Text("Close") }
            } else {
                TextButton(onClick = onRequest) { Text("Request resource") }
            }
        },
        dismissButton = { TextButton(onClick = onOpenPassport) { Text("Open passport") } },
    )
}

@Composable
internal fun MarketplaceListingDetails(
    resource: com.reevent.app.core.model.ResourceItem,
    modifier: Modifier = Modifier,
) {
    val listing = resource.marketplaceListing ?: return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "${ResourcePresentationRules.quantityLabel(listing.publishedQuantity, resource.unit)} available",
            style = MaterialTheme.typography.titleSmall,
            color = ReEventInk,
        )
        Text(
            "Condition: ${resource.condition.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)}",
            color = ReEventTextSecondary,
        )
        Text(
            "Material: ${resource.materialLabel} · Category: ${resource.category.ifBlank { "Uncategorised" }}",
            color = ReEventTextSecondary,
        )
        HorizontalDivider(color = ReEventLine)
        Text("Request terms", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
        Text("Actions: ${listing.allowedActions.joinToString { it.displayLabel() }}", color = ReEventTextSecondary)
        listing.defaultDurationDays?.let { days ->
            Text(
                "Borrow/rent default duration: $days day${if (days == 1) "" else "s"}. Final handover timing is confirmed after approval.",
                color = ReEventTextSecondary,
            )
        }
        listing.buyUnitPrice?.let { price ->
            Text("Buy: $price ReCoins per ${resource.unit}", color = ReEventTextSecondary)
        }
        listing.rentUnitPrice?.let { price ->
            Text("Rent: $price ReCoins per ${resource.unit}", color = ReEventTextSecondary)
        }
        Text(listing.terms.ifBlank { "No additional listing terms were supplied." }, color = ReEventTextSecondary)
    }
}

@Composable
private fun MarketplaceRequestDialog(
    resource: com.reevent.app.core.model.ResourceItem,
    initialType: TransactionType? = null,
    onDismiss: () -> Unit,
    onSubmit: (TransactionType, Double) -> Unit,
) {
    val allowedActions = resource.availableMarketplaceTypes()
    val maxQuantity = minOf(resource.quantity, resource.marketplaceListing?.publishedQuantity ?: 0.0)
    var type by rememberSaveable(resource.id, initialType) {
        mutableStateOf(initialType?.takeIf { it in allowedActions } ?: allowedActions.firstOrNull() ?: TransactionType.BORROW)
    }
    var quantity by rememberSaveable(resource.id) { mutableStateOf("1") }
    var submitted by rememberSaveable(resource.id) { mutableStateOf(false) }
    val quantityValue = quantity.toDoubleOrNull()
    // The server currently permits fractional marketplace quantities only for KG resources.
    val allowsFraction = resource.unit.equals("kg", ignoreCase = true)
    val valid =
        type in allowedActions && quantityValue != null && quantityValue > 0 && quantityValue <= maxQuantity &&
            (allowsFraction || quantityValue % 1.0 == 0.0)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request ${resource.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This creates a pending transaction for the owner to approve. The server rechecks listing availability before accepting it.",
                )
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
                                if (allowedActions.isEmpty()) {
                                    "This listing has no requestable actions."
                                } else if (!allowsFraction && quantityValue != null &&
                                    quantityValue % 1.0 != 0.0
                                ) {
                                    "This resource must be requested as whole ${resource.unit}."
                                } else {
                                    "Enter a quantity above 0 and no more than the published amount."
                                },
                            )
                        }
                    },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                submitted = true
                if (valid) onSubmit(type, checkNotNull(quantityValue))
            }) { Text("Submit request") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
internal fun TransactionCard(
    user: User,
    transaction: CircularTransaction,
    resource: com.reevent.app.core.model.ResourceItem?,
    syncCommand: SyncCommandStatus?,
    onApprove: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    onInTransit: () -> Unit,
    onPassport: () -> Unit,
) {
    val presentation = TransactionLifecyclePresentationRules.forViewer(user.id, transaction, syncCommand)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(resource?.title ?: "Resource ${transaction.resourceId.take(8)}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${transaction.type.displayLabel()} • ${ResourcePresentationRules.quantityLabel(
                            transaction.quantity,
                            resource?.unit ?: "items",
                        )}",
                        color = ReEventTextSecondary,
                    )
                }
                StatusChip(presentation.statusLabel, transaction.status.toUiColor())
            }
            Text(
                text = presentation.nextStep,
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventTextSecondary,
            )
            Text(
                text = "Next responsible: ${presentation.responsibleRole}",
                style = MaterialTheme.typography.labelMedium,
                color = ReEventInk,
            )
            when (presentation.syncFeedback) {
                TransactionLifecycleSyncFeedback.PENDING -> {
                    LifecycleSyncFeedbackPanel(
                        title = "Action waiting to sync",
                        detail = "Do not repeat the action. The card will refresh when the server processes the queued command.",
                        color = ReEventBlue,
                    )
                }

                TransactionLifecycleSyncFeedback.FAILED -> {
                    LifecycleSyncFeedbackPanel(
                        title = "Action needs retry",
                        detail = "The server did not confirm this change. Open Profile and choose Retry failed changes, then return here to refresh.",
                        color = ReEventCoral,
                    )
                }

                null -> {}
            }
            if (resource != null) {
                SecondaryActionButton("Open passport", onPassport, Modifier.fillMaxWidth())
            }
            presentation.primaryAction?.let { action ->
                val onAction =
                    when (action) {
                        TransactionLifecycleCardAction.APPROVE -> onApprove

                        TransactionLifecycleCardAction.BEGIN_HANDOVER -> onInTransit

                        TransactionLifecycleCardAction.CONFIRM_RECEIPT,
                        TransactionLifecycleCardAction.BEGIN_RETURN,
                        TransactionLifecycleCardAction.CONFIRM_RETURN,
                        -> onComplete

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
private fun LifecycleSyncFeedbackPanel(
    title: String,
    detail: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape =
            androidx.compose.foundation.shape
                .RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.10f),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = ReEventInk)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
        }
    }
}
