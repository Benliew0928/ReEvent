package com.reevent.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.R
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.PartnerMatch
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.ResourceItem as VisualResourceItem
import com.reevent.app.ui.ResourceTone
import java.util.Locale
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
    onProfile: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    val event = events.firstOrNull()
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
    val records by viewModel.impact(eventId).collectAsState(emptyList())
    val rate = if (resources.isEmpty() || records.isEmpty()) null else {
        (records.mapNotNull { it.resourceId }.distinct().size.toFloat() / resources.size).coerceIn(0f, 1f)
    }

    ImpactScreen(
        onNavigate = onNavigate,
        metrics = records.toImpactMetrics(),
        recoveryRate = rate,
        recoveryLabel = rate?.let { "${(it * 100).toInt()}%" } ?: "—",
        chartValues = emptyList()
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
    MarketplaceScreen(
        onNavigate = onNavigate,
        resources = resources.map { it.toVisualResource() },
        onResourceClick = { it.id?.let(onPassport) }
    )
}

@Composable
fun PassportVisualScreen(
    resourceId: String,
    onMatch: (String) -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    PassportScreen(
        onNavigate = { screen ->
            when (screen) {
                ReEventScreen.AiMatch -> onMatch(resourceId)
                else -> onNavigate(screen)
            }
        },
        item = resource?.toVisualResource(),
        recoverySteps = resource?.let { listOf(it.toPassportRecoveryStep()) }.orEmpty()
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
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    ParticipantReturnScreen(onNavigate)
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
    PartnerWorkbenchScreen(
        onNavigate = onNavigate,
        hasIncomingLot = transactions.isNotEmpty(),
        hasProgramme = programmes.isNotEmpty()
    )
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
    id = id
)

private fun List<com.reevent.app.core.model.ResourceItem>.toDashboardMetrics(records: List<ImpactRecord>): List<ImpactMetric> {
    if (isEmpty() && records.isEmpty()) return emptyList()
    val recovered = count { it.status == ResourceStatus.RECOVERED || it.status == ResourceStatus.HANDED_OVER }
    val recoveryRate = if (isEmpty()) 0 else recovered * 100 / size
    return listOf(
        ImpactMetric("$recoveryRate%", "Recovery rate", "$recovered of $size tracked lots"),
        ImpactMetric("${records.sumOf { it.materialDivertedKg }.formatQuantity()} kg", "Materials diverted", "Verified impact records"),
        ImpactMetric(records.sumOf { it.valueRecoveredCents }.toMoney(), "Value recovered", "Resale, repair and buy-back")
    )
}

private fun List<ImpactRecord>.toImpactMetrics(): List<ImpactMetric> {
    if (isEmpty()) return emptyList()
    return listOf(
        ImpactMetric(sumOf { it.materialDivertedKg }.formatQuantity() + " kg", "Materials diverted", "Verified recovery records"),
        ImpactMetric(sumOf { it.emissionsAvoidedKg }.formatQuantity() + " kg", "Emissions avoided", "Estimated CO₂e avoided"),
        ImpactMetric(sumOf { it.valueRecoveredCents }.toMoney(), "Value recovered", "Resale, repair and buy-back")
    )
}

private fun List<com.reevent.app.core.model.ResourceItem>.toRecoverySteps(records: List<ImpactRecord>): List<RecoveryStep> {
    if (isEmpty() && records.isEmpty()) return emptyList()
    val available = count { it.status == ResourceStatus.AVAILABLE }
    val completed = count { it.status == ResourceStatus.RECOVERED || it.status == ResourceStatus.HANDED_OVER }
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
    this == ResourceStatus.AVAILABLE -> ResourceTone.Ready
    this == ResourceStatus.RECOVERED || this == ResourceStatus.HANDED_OVER -> ResourceTone.Recycle
    condition == ResourceCondition.NEEDS_REPAIR -> ResourceTone.Repair
    else -> ResourceTone.Hot
}

private fun ProgrammeType.toVisualTone() = when (this) {
    ProgrammeType.REPAIR -> ResourceTone.Repair
    ProgrammeType.RECYCLE -> ResourceTone.Recycle
    ProgrammeType.REUSE, ProgrammeType.COLLECTION -> ResourceTone.Ready
    ProgrammeType.BUY_BACK -> ResourceTone.Hot
}

private fun ResourceStatus.visualLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun Double.formatQuantity() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(Locale.US, this)
private fun Long.toMoney() = "RM %.2f".format(Locale.US, this / 100.0)
