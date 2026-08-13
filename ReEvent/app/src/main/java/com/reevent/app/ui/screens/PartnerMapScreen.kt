package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.SectionTitle
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary
import com.reevent.app.core.model.ResourceItem as CoreResourceItem

@Composable
fun PartnerMapScreen(
    onNavigate: (TopLevelDestination) -> Unit,
    onProfile: () -> Unit,
    programmes: List<CircularProgramme>,
    marketplaceResources: List<CoreResourceItem>,
    onOpenPassport: (String) -> Unit,
) {
    var selectedProgramme by remember { mutableStateOf<CircularProgramme?>(null) }
    var selectedMaterial by rememberSaveable { mutableStateOf<String?>(null) }
    val activeProgrammes = programmes.filter(CircularProgramme::active)
    val materials =
        activeProgrammes
            .flatMap(CircularProgramme::acceptedMaterials)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinctBy(String::lowercase)
            .sortedBy(String::lowercase)
    val visibleProgrammes =
        activeProgrammes.filter { programme ->
            selectedMaterial == null || programme.acceptedMaterials.any { it.equals(selectedMaterial, ignoreCase = true) }
        }
    ReEventScaffold(selected = TopLevelDestination.PARTNERS, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Partner network",
                    subtitle = "Factories, repair partners and collection points",
                    onProfile = onProfile,
                )
            }
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = ReEventMintSoft, border = BorderStroke(1.dp, ReEventLine)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Verified partner programmes", style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                        Text(
                            if (activeProgrammes.isEmpty()) {
                                "No active programmes are available yet."
                            } else {
                                "${visibleProgrammes.size} of ${activeProgrammes.size} active programmes match the selected material."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventTextSecondary,
                        )
                    }
                }
            }
            if (materials.isNotEmpty()) {
                item { SectionTitle("Filter by material") }
                item {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedMaterial == null,
                            onClick = { selectedMaterial = null },
                            label = { Text("All materials") },
                        )
                        materials.forEach { material ->
                            FilterChip(
                                selected = selectedMaterial.equals(material, ignoreCase = true),
                                onClick = { selectedMaterial = material },
                                label = { Text(material) },
                            )
                        }
                    }
                }
            }
            item { SectionTitle("Available programmes") }
            if (visibleProgrammes.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = ReEventMintSoft) {
                        Text(
                            text = if (activeProgrammes.isEmpty()) "No active partner programmes yet." else "No programmes accept ${selectedMaterial ?: "this material"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventTextSecondary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
            items(visibleProgrammes, key = CircularProgramme::id) { programme ->
                PartnerProgrammeCard(programme = programme, onClick = { selectedProgramme = programme })
            }
        }
    }

    selectedProgramme?.let { programme ->
        PartnerProgrammeDetailDialog(
            programme = programme,
            eligibleResources =
                marketplaceResources.filter { resource ->
                    resource.status == ResourceStatus.ACTIVE &&
                        programme.acceptedMaterials.any { it.equals(resource.material, ignoreCase = true) }
                },
            onDismiss = { selectedProgramme = null },
            onOpenPassport = { resourceId ->
                selectedProgramme = null
                onOpenPassport(resourceId)
            },
        )
    }
}

@Composable
private fun PartnerProgrammeCard(
    programme: CircularProgramme,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = ReEventSurface,
        border = BorderStroke(1.dp, ReEventLine),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(programme.name, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    Text(
                        programme.type.name
                            .lowercase()
                            .replaceFirstChar(Char::titlecase),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReEventTextSecondary,
                    )
                }
                StatusChip("Active", ReEventGreen)
            }
            Text(
                "Service area: ${programme.location.ifBlank { "Location pending" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventTextSecondary,
            )
            Text(
                "Accepts: ${programme.acceptedMaterials.takeIf(List<String>::isNotEmpty)?.joinToString() ?: "Materials pending"}",
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventInk,
            )
        }
    }
}

@Composable
private fun PartnerProgrammeDetailDialog(
    programme: CircularProgramme,
    eligibleResources: List<CoreResourceItem>,
    onDismiss: () -> Unit,
    onOpenPassport: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(programme.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "${programme.type.name.lowercase().replaceFirstChar(
                        Char::titlecase,
                    )} · ${programme.location.ifBlank { "Location pending" }}",
                    color = ReEventTextSecondary,
                )
                Text(
                    "Accepted materials: ${programme.acceptedMaterials
                        .takeIf(
                            List<String>::isNotEmpty,
                        )?.joinToString() ?: "Not specified"}",
                )
                HorizontalDivider(color = ReEventLine)
                Text("Eligible marketplace resources", style = MaterialTheme.typography.titleSmall)
                if (eligibleResources.isEmpty()) {
                    Text("No active marketplace resource currently matches this programme's materials.", color = ReEventTextSecondary)
                } else {
                    eligibleResources.take(3).forEach { resource ->
                        SecondaryActionButton("View passport: ${resource.title}", { onOpenPassport(resource.id) }, Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
