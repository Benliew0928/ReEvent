package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reevent.app.R
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.HeroImageCard
import com.reevent.app.ui.components.MetricCard
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.QuickActionTile
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.RecoveryTimeline
import com.reevent.app.ui.components.ResourceCard
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.SectionTitle
import com.reevent.app.ui.theme.ReEventAmber
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun HomeScreen(
    onNavigate: (TopLevelDestination) -> Unit,
    onProfile: () -> Unit,
    onMatch: () -> Unit,
    title: String = "EcoCampus Open Day",
    subtitle: String = "Live recovery board",
    metrics: List<ImpactMetric> = emptyList(),
    resources: List<com.reevent.app.ui.ResourceCardModel> = emptyList(),
    recoverySteps: List<RecoveryStep> = emptyList(),
    hasEvent: Boolean = true,
    onManageEvents: (() -> Unit)? = null,
    onAddResource: (() -> Unit)? = null,
    onResourceClick: (com.reevent.app.ui.ResourceCardModel) -> Unit,
) {
    ReEventScaffold(selected = TopLevelDestination.HOME, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = title,
                    subtitle = subtitle,
                    onProfile = onProfile,
                )
            }
            item {
                HeroImageCard(
                    imageRes = R.drawable.onboarding_event_setup,
                    title = "Close-out starts before the event ends",
                    subtitle = "Tag resources, match partners, and publish reusable lots in one flow.",
                    chip = "Organizer mode",
                )
            }
            if (metrics.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        metrics.firstOrNull()?.let { metric ->
                            MetricCard(metric.value, metric.label, metric.detail, Modifier.fillMaxWidth(), ReEventGreen)
                        }
                        if (metrics.size > 1) {
                            AdaptiveTwoPane(
                                first = {
                                    val metric = metrics[1]
                                    MetricCard(metric.value, metric.label, metric.detail, Modifier.fillMaxWidth(), ReEventBlue)
                                },
                                second = {
                                    metrics.getOrNull(2)?.let { metric ->
                                        MetricCard(metric.value, metric.label, metric.detail, Modifier.fillMaxWidth(), ReEventCoral)
                                    }
                                },
                            )
                        }
                    }
                }
            } else {
                item {
                    HomeEmptyState(
                        title = if (hasEvent) "No resource summary yet" else "No event workspace yet",
                        detail =
                            if (hasEvent) {
                                "Add the first resource lot to start a live recovery summary."
                            } else {
                                "Create an event before adding resources, partners, or recovery impact."
                            },
                        actionLabel = if (hasEvent) "Add a resource" else "Create an event",
                        onAction = if (hasEvent) onAddResource else onManageEvents,
                    )
                }
            }
            item {
                SectionTitle(title = "Fast actions")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionTile(
                        title = if (hasEvent) "Add resource lot" else "Create your first event",
                        detail = if (hasEvent) "Create item passport with photos and condition" else "Set the event details before creating resource lots",
                        icon = Icons.Outlined.Add,
                        color = ReEventGreen,
                        onClick = { if (hasEvent) onAddResource?.invoke() else onManageEvents?.invoke() },
                    )
                    QuickActionTile(
                        title = "Manage events",
                        detail = "Create, edit and switch event workspaces",
                        icon = Icons.Outlined.DateRange,
                        color = ReEventCoral,
                        onClick = { onManageEvents?.invoke() },
                    )
                    if (resources.isNotEmpty()) {
                        QuickActionTile(
                            title = "Run AI recovery match",
                            detail = "Rank reuse, repair and buy-back partners",
                            icon = Icons.Outlined.Star,
                            color = ReEventAmber,
                            onClick = onMatch,
                        )
                    }
                    QuickActionTile(
                        title = "Open partner map",
                        detail = "See factories, repairers and collection points",
                        icon = Icons.Outlined.Map,
                        color = ReEventBlue,
                        onClick = { onNavigate(TopLevelDestination.PARTNERS) },
                    )
                }
            }
            item {
                SectionTitle(title = "Circular workflow")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventSurface,
                    border = BorderStroke(1.dp, ReEventLine),
                ) {
                    if (recoverySteps.isEmpty()) {
                        EmptyWorkflowMessage(
                            message =
                                if (hasEvent) {
                                    "Your recovery activity will appear here once a resource lot is added."
                                } else {
                                    "Create an event to begin a circular recovery workflow."
                                },
                            actionLabel = if (hasEvent) "Add a resource" else "Create an event",
                            onAction = if (hasEvent) onAddResource else onManageEvents,
                        )
                    } else {
                        RecoveryTimeline(modifier = Modifier.padding(16.dp), steps = recoverySteps)
                    }
                }
            }
            item {
                SectionTitle(
                    title = "High-value lots",
                    action = "See all",
                    onAction = { onNavigate(TopLevelDestination.MARKETPLACE) },
                )
            }
            if (resources.isEmpty()) {
                item {
                    EmptyWorkflowMessage(
                        message = if (hasEvent) "No resource lots have been added yet." else "Your event's resource lots will appear here after it is created.",
                        actionLabel = if (hasEvent) "Add first resource" else "Create an event",
                        onAction = if (hasEvent) onAddResource else onManageEvents,
                    )
                }
            }
            items(resources) { item ->
                ResourceCard(item = item, onClick = { onResourceClick(item) })
            }
        }
    }
}

@Composable
private fun EmptyWorkflowMessage(
    message: String = "Your recovery activity will appear here once resources are added.",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = ReEventMintSoft) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = ReEventTextSecondary)
            if (actionLabel != null && onAction != null) {
                SecondaryActionButton(actionLabel, onAction, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    title: String,
    detail: String,
    actionLabel: String,
    onAction: (() -> Unit)?,
) {
    Surface(shape = RoundedCornerShape(20.dp), color = ReEventMintSoft, border = BorderStroke(1.dp, ReEventLine)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = ReEventTextSecondary)
            if (onAction != null) PrimaryActionButton(actionLabel, onAction, Modifier.fillMaxWidth())
        }
    }
}
