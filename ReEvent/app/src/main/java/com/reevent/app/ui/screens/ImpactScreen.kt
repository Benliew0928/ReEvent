package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.reevent.app.R
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.feature.impact.ImpactBadge
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.MetricCard
import com.reevent.app.ui.components.MiniBarChart
import com.reevent.app.ui.components.ProgressRing
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SectionTitle
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.components.WarmChartColors
import com.reevent.app.ui.theme.ReEventAmber
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ImpactEventScope(
    val id: String,
    val name: String,
)

@Composable
fun ImpactScreen(
    onNavigate: (TopLevelDestination) -> Unit,
    onProfile: () -> Unit,
    metrics: List<ImpactMetric> = emptyList(),
    recoveryRate: Float? = null,
    recoveryLabel: String = "—",
    chartValues: List<Float> = emptyList(),
    badge: ImpactBadge? = null,
    unavailableEstimateReason: String? = null,
    latestRecord: ImpactRecord? = null,
    selectedScope: ImpactEventScope? = null,
    scopes: List<ImpactEventScope> = emptyList(),
    onScopeSelected: (String) -> Unit = {},
) {
    var selectingScope by rememberSaveable { mutableStateOf(false) }
    ReEventScaffold(selected = TopLevelDestination.IMPACT, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Impact board",
                    subtitle = "Circular economy proof for reporting and marks",
                    onProfile = onProfile,
                )
            }
            selectedScope?.let { scope ->
                item {
                    Surface(shape = RoundedCornerShape(18.dp), color = ReEventMintSoft, border = BorderStroke(1.dp, ReEventLine)) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Reporting scope", style = MaterialTheme.typography.labelMedium, color = ReEventTextSecondary)
                                Text(scope.name, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                            }
                            if (scopes.size > 1) TextButton(onClick = { selectingScope = true }) { Text("Change") }
                        }
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventSurface,
                    border = BorderStroke(1.dp, ReEventLine),
                ) {
                    AdaptiveTwoPane(
                        modifier = Modifier.padding(18.dp),
                        stackedAlignment = Alignment.CenterHorizontally,
                        first = {
                            ProgressRing(
                                progress = recoveryRate ?: 0f,
                                centerText = recoveryLabel,
                                label = "recovered",
                                modifier = Modifier.size(142.dp),
                            )
                        },
                        second = {
                            Column {
                                StatusChip(text = "SDG 12 aligned", color = ReEventGreen)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text =
                                        unavailableEstimateReason ?: if (metrics.isEmpty()) {
                                            "Impact will appear here after the first verified recovery or handover."
                                        } else {
                                            "The event avoided disposal by routing items to reuse, repair and remanufacturing."
                                        },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = ReEventInk,
                                )
                            }
                        },
                    )
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventSurface,
                    border = BorderStroke(1.dp, ReEventLine),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        SectionTitle(title = "Recovery channels")
                        MiniBarChart(
                            values = chartValues.ifEmpty { listOf(0f, 0f, 0f, 0f) },
                            colors = WarmChartColors,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LegendDot("Reuse", ReEventGreen)
                            LegendDot("Repair", ReEventAmber)
                            LegendDot("Donation", ReEventBlue)
                            LegendDot("Recycle", ReEventCoral)
                        }
                        unavailableEstimateReason?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
                        }
                    }
                }
            }
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(ReEventGreenDeep),
                ) {
                    if (badge == null) {
                        Text(
                            text = "A recovery badge will appear after verified impact is recorded.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.82f),
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.impact_badge_high_recovery),
                            contentDescription = "${badge.displayLabel()} badge",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            text = badge.displayLabel(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    metrics.forEachIndexed { index, metric ->
                        MetricCard(
                            value = metric.value,
                            label = metric.label,
                            detail = metric.detail,
                            color = WarmChartColors[index % WarmChartColors.size],
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            latestRecord?.let { record ->
                item { LatestImpactContributionCard(record) }
            }
        }
    }
    if (selectingScope) {
        AlertDialog(
            onDismissRequest = { selectingScope = false },
            title = { Text("Choose reporting event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    scopes.forEach { scope ->
                        TextButton(
                            onClick = {
                                onScopeSelected(scope.id)
                                selectingScope = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(scope.name, modifier = Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectingScope = false }) { Text("Close") } },
        )
    }
}

@Composable
private fun LatestImpactContributionCard(record: ImpactRecord) {
    Surface(shape = RoundedCornerShape(22.dp), color = ReEventSurface, border = BorderStroke(1.dp, ReEventLine)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("Latest verified contribution", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            Text(
                "${record.transactionType.displayLabel()} - ${record.completedQuantity.formatImpactNumber()} ${record.unit}",
                style = MaterialTheme.typography.bodyLarge,
                color = ReEventInk,
            )
            val outcomes =
                buildList {
                    record.materialDivertedKg?.let { add("${it.formatImpactNumber()} kg diverted") }
                    record.emissionsAvoidedKg?.let { add("${it.formatImpactNumber()} kg CO2e avoided") }
                    if (record.recoinsTransferred > 0) add("${record.recoinsTransferred} ReCoins transferred")
                    if (record.recoinsRewarded > 0) add("${record.recoinsRewarded} ReCoins rewarded")
                }
            Text(
                outcomes
                    .ifEmpty {
                        listOf(
                            "No documented mass/factor estimate was supplied for this completed outcome.",
                        )
                    }.joinToString(" | "),
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventTextSecondary,
            )
            Text(
                "Server record: ${record.calculatedAt.toImpactDateTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = ReEventTextSecondary,
            )
        }
    }
}

@Composable
private fun LegendDot(
    label: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color),
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = ReEventTextSecondary)
    }
}

private fun ImpactBadge.displayLabel(): String =
    when (this) {
        ImpactBadge.FIRST_RECOVERY -> "First recovery"
        ImpactBadge.CIRCULAR_STARTER -> "Circular starter"
        ImpactBadge.HIGH_RECOVERY -> "High recovery"
    }

private fun Double.formatImpactNumber(): String =
    if (this % 1.0 == 0.0) toLong().toString() else "%.3f".format(java.util.Locale.US, this).trimEnd('0').trimEnd('.')

private val impactDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm")

private fun Long.toImpactDateTime(): String = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(impactDateTimeFormat)
