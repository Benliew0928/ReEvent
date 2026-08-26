package com.reevent.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.model.User
import com.reevent.app.feature.impact.ImpactCalculator
import com.reevent.app.ui.TopLevelDestination

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
