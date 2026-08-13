package com.reevent.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.feature.matching.CircularRecommendationEngine
import com.reevent.app.feature.matching.RecommendationCandidate
import com.reevent.app.ui.theme.ReEventTextSecondary
import kotlinx.coroutines.flow.flowOf

@Composable
fun MatchingLiveScreen(
    user: User,
    resourceId: String,
    onBack: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val event by (resource?.eventId?.let(viewModel::event) ?: flowOf(null)).collectAsState(null)
    val programmes by viewModel.programmes().collectAsState(emptyList())
    val recommendation = resource?.let { CircularRecommendationEngine.recommend(it, programmes, event?.venue.orEmpty()) }
    FeatureScaffold("Circular matches", "Back", onBack, viewModel) {
        when {
            resource == null -> {
                item { EmptyPanel("Resource not found", "Return to the passport and choose a current resource.") {} }
            }

            recommendation?.primary == null -> {
                item {
                    EmptyPanel(
                        "No eligible partner route",
                        recommendation?.ineligibilityReason ?: "Add a partner programme or refresh when you are online.",
                    ) {}
                }
            }

            else -> {
                item { MatchingInputsCard(resource = resource!!, eventLocation = event?.venue.orEmpty()) }
                item {
                    RecommendationRouteCard(
                        heading = "Recommended route",
                        candidate = recommendation.primary,
                        programmes = programmes,
                        resource = resource!!,
                        user = user,
                        onCreateHandover = { programme -> viewModel.createPartnerHandover(user, resource!!, programme) },
                    )
                }
                if (recommendation.alternatives.isNotEmpty()) {
                    item { Text("Alternative routes", style = MaterialTheme.typography.titleMedium) }
                    items(recommendation.alternatives, key = { it.action.name }) { candidate ->
                        RecommendationRouteCard(
                            heading = "Alternative",
                            candidate = candidate,
                            programmes = programmes,
                            resource = resource!!,
                            user = user,
                            onCreateHandover = { programme -> viewModel.createPartnerHandover(user, resource!!, programme) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationRouteCard(
    heading: String,
    candidate: RecommendationCandidate,
    programmes: List<com.reevent.app.core.model.CircularProgramme>,
    resource: ResourceItem,
    user: User,
    onCreateHandover: (com.reevent.app.core.model.CircularProgramme) -> Unit,
) {
    val compatible = programmes.filter { it.id in candidate.compatibleProgrammeIds }
    var selectedProgramme by remember { mutableStateOf<com.reevent.app.core.model.CircularProgramme?>(null) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(heading, style = MaterialTheme.typography.labelLarge, color = ReEventTextSecondary)
            Text(
                candidate.action.name
                    .lowercase()
                    .replace('_', ' ')
                    .replaceFirstChar(Char::titlecase),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(candidate.explanation)
            Text("Match score: ${candidate.score}", style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
            Text(
                "Programme capacity is confirmed by the server when you submit a request.",
                style = MaterialTheme.typography.bodySmall,
                color = ReEventTextSecondary,
            )
            compatible.forEach { programme ->
                Text(programme.name, fontWeight = FontWeight.SemiBold)
                Text(programme.location.ifBlank { "Location to be confirmed" }, color = ReEventTextSecondary)
                Button(
                    onClick = { selectedProgramme = programme },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = resource.ownerId == user.id && resource.status == ResourceStatus.ACTIVE && resource.quantity > 0,
                ) { Text("Request recovery") }
            }
        }
    }

    selectedProgramme?.let { programme ->
        AlertDialog(
            onDismissRequest = { selectedProgramme = null },
            title = { Text("Request recovery?") },
            text = {
                Text(
                    "Send ${ResourcePresentationRules.quantityLabel(
                        resource.quantity,
                        resource.unit,
                    )} of ${resource.title} to ${programme.name}. " +
                        "The partner will accept or decline the request, and the server will verify availability and capacity.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedProgramme = null
                    onCreateHandover(programme)
                }) { Text("Send request") }
            },
            dismissButton = { TextButton(onClick = { selectedProgramme = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MatchingInputsCard(
    resource: ResourceItem,
    eventLocation: String,
) {
    Card(Modifier.fillMaxWidth()) {
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
            Text("Event location: ${eventLocation.ifBlank { "Not specified" }}", color = ReEventTextSecondary)
        }
    }
}
