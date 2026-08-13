package com.reevent.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.model.User
import com.reevent.app.feature.impact.ImpactCalculator
import com.reevent.app.ui.TopLevelDestination
import kotlinx.coroutines.flow.flowOf

/**
 * Bridges the original visual composables to the repository-backed state. Empty repositories
 * deliberately render their original layout with empty states instead of invented production data.
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
    viewModel: FeatureViewModel = hiltViewModel(),
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
        onNavigate = { destination ->
            when (destination) {
                TopLevelDestination.EVENTS -> onManageEvents()

                TopLevelDestination.PARTNERS -> onPartnerMap()

                // Keep the original impact board reachable even before an event has synced data.
                TopLevelDestination.IMPACT -> onImpact(event?.id.orEmpty())

                TopLevelDestination.MARKETPLACE -> onMarketplace()

                TopLevelDestination.ACCOUNT -> onProfile()

                else -> Unit
            }
        },
        onProfile = onProfile,
        onMatch = { visualResources.firstOrNull()?.id?.let(onPassport) },
        title = event?.name ?: "Your event dashboard",
        subtitle =
            when {
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
        onResourceClick = { it.id?.let(onPassport) },
    )
}

@Composable
fun OrganizerImpactVisualScreen(
    user: User,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
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
        viewModel = viewModel,
    )
}

@Composable
fun ImpactVisualScreen(
    eventId: String,
    onNavigate: (TopLevelDestination) -> Unit,
    selectedScope: ImpactEventScope? = null,
    scopes: List<ImpactEventScope> = emptyList(),
    onScopeSelected: (String) -> Unit = {},
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val resources by viewModel.resources(eventId).collectAsState(emptyList())
    val transactions by viewModel.eventTransactions(eventId).collectAsState(emptyList())
    val records by viewModel.impact(eventId).collectAsState(emptyList())
    val summary =
        remember(resources, transactions, records) {
            ImpactCalculator.summarize(resources, transactions, records)
        }
    val rate = summary.recoveryRate

    ImpactScreen(
        onNavigate = onNavigate,
        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
        metrics = summary.toImpactMetrics(),
        recoveryRate = rate,
        recoveryLabel = rate?.let { "${(it * 100).toInt()}%" } ?: "—",
        chartValues = summary.chartValues,
        badge = summary.badge,
        unavailableEstimateReason = summary.unavailableEstimateReason,
        latestRecord = summary.latestRecord,
        selectedScope = selectedScope,
        scopes = scopes,
        onScopeSelected = onScopeSelected,
    )
}
