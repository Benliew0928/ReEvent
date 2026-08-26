package com.reevent.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.model.User

@Composable
fun OrganizerHomeRouteScreen(
    user: User,
    onTarget: (HomeTarget) -> Unit,
    onProfile: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val stateFlow = remember(user.id) { viewModel.organizer(user) }
    val state by stateFlow.collectAsState(
        HomeDashboardMappers.organizer(user, emptyList(), null, emptyList(), emptyList(), emptyList()),
    )
    LaunchedEffect(user.id) { viewModel.refresh() }
    EditorialRoleHomeScreen(
        state = state,
        onScopeSelected = viewModel::selectEvent,
        onTarget = onTarget,
        onProfile = onProfile,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun ParticipantHomeRouteScreen(
    user: User,
    onTarget: (HomeTarget) -> Unit,
    onProfile: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val stateFlow = remember(user.id) { viewModel.participant(user) }
    val state by stateFlow.collectAsState(
        HomeDashboardMappers.participant(user, ParticipantActivityFilter.ALL, emptyList(), emptyList(), emptyList()),
    )
    LaunchedEffect(user.id) { viewModel.refresh() }
    EditorialRoleHomeScreen(
        state = state,
        onScopeSelected = viewModel::selectParticipantFilter,
        onTarget = onTarget,
        onProfile = onProfile,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun PartnerHomeRouteScreen(
    user: User,
    onTarget: (HomeTarget) -> Unit,
    onProfile: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val stateFlow = remember(user.id) { viewModel.partner(user) }
    val state by stateFlow.collectAsState(
        HomeDashboardMappers.partner(user, emptyList(), null, emptyList(), emptyList()),
    )
    LaunchedEffect(user.id) { viewModel.refresh() }
    EditorialRoleHomeScreen(
        state = state,
        onScopeSelected = viewModel::selectProgramme,
        onTarget = onTarget,
        onProfile = onProfile,
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}
