package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventTextSecondary
import kotlinx.coroutines.flow.flowOf

@Composable
fun MatchingLiveScreen(
    user: User,
    resourceId: String,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
    discoveryViewModel: PartnerMapViewModel = hiltViewModel(),
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val event by (resource?.eventId?.let(viewModel::event) ?: flowOf(null)).collectAsState(null)
    val discovery by discoveryViewModel.state.collectAsState()
    var selected by remember { mutableStateOf<PartnerCandidate?>(null) }
    var confirmation by remember { mutableStateOf<PartnerCandidate?>(null) }

    LaunchedEffect(user.id, resourceId) {
        viewModel.refresh()
        discoveryViewModel.load(resourceId)
    }

    FeatureScaffold("Circular matches", "Back", onBack, viewModel) {
        when {
            resource == null -> item { EmptyPanel("Resource not found", "Return to the passport and choose a current resource.") {} }
            else -> {
                item { MatchingInputsCard(resource = resource!!, eventLocation = event?.venue.orEmpty()) }
                item {
                    SecondaryActionButton(
                        text = "Open eligible programmes on map",
                        onClick = onOpenMap,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (discovery.loading) item { Text("Checking programme rules and capacity…", color = ReEventTextSecondary) }
                discovery.error?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
                if (!discovery.loading && discovery.result.candidates.isEmpty()) {
                    item {
                        EmptyPanel(
                            "No eligible partner route",
                            discovery.result.exclusionCounts.entries.joinToString(", ") { (reason, count) ->
                                "${reason.lowercase().replace('_', ' ')}: $count"
                            }.ifBlank { "No active programme currently satisfies this resource's rules." },
                        ) {}
                    }
                }
                items(discovery.result.candidates, key = { it.programme.id }) { candidate ->
                    MatchingCandidateCard(candidate = candidate, onClick = { selected = candidate })
                }
                if (!discovery.result.serverAuthoritative) {
                    item {
                        Text(
                            "Cached results are shown. The server rechecks ownership, eligibility and capacity before accepting a request.",
                            color = ReEventTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }

    val selectedCandidate = selected
    val currentResource = resource
    if (selectedCandidate != null && currentResource != null) {
        PartnerCandidateDetailDialog(
            candidate = selectedCandidate,
            resource = currentResource,
            eligibleResources = emptyList(),
            canRequest = currentResource.ownerId == user.id && currentResource.status == ResourceStatus.ACTIVE && currentResource.quantity > 0,
            onDismiss = { selected = null },
            onOpenPassport = {},
            onRequest = {
                selected = null
                confirmation = selectedCandidate
            },
        )
    }
    val candidateToConfirm = confirmation
    if (candidateToConfirm != null && currentResource != null) {
        PartnerRecoveryConfirmationDialog(
            candidate = candidateToConfirm,
            resource = currentResource,
            onDismiss = { confirmation = null },
            onConfirm = {
                confirmation = null
                viewModel.createPartnerHandover(user, currentResource, candidateToConfirm.programme)
            },
        )
    }
}

@Composable
private fun MatchingCandidateCard(
    candidate: PartnerCandidate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        border = BorderStroke(1.dp, ReEventLine),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(candidate.programme.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${candidate.programme.type.name.lowercase().replaceFirstChar(Char::titlecase)} · " +
                    (candidate.distanceKm?.let { "%.1f km".format(it) } ?: "distance unavailable"),
                color = ReEventTextSecondary,
            )
            candidate.score?.let { Text("Match score: $it", color = ReEventGreenDeep) }
            candidate.reasons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            Text("View details and request recovery", color = ReEventGreenDeep)
        }
    }
}

@Composable
private fun MatchingInputsCard(
    resource: ResourceItem,
    eventLocation: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Resource considered", style = MaterialTheme.typography.titleMedium)
            Text("Category: ${resource.category.ifBlank { "Not specified" }}", color = ReEventTextSecondary)
            Text("Material: ${resource.material.ifBlank { "Not specified" }}", color = ReEventTextSecondary)
            Text(
                "Condition: ${resource.condition.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)}",
                color = ReEventTextSecondary,
            )
            Text(
                "Available quantity: ${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)}",
                color = ReEventTextSecondary,
            )
            Text("Origin: ${resource.geoLocation?.displayAddress ?: eventLocation.ifBlank { "Not specified" }}", color = ReEventTextSecondary)
        }
    }
}
