package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reevent.app.R
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.PartnerMatch
import com.reevent.app.ui.ReEventRole
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.ResourceTone
import com.reevent.app.ui.components.BrandLockup
import com.reevent.app.ui.components.FakeQrPanel
import com.reevent.app.ui.components.FormFieldPreview
import com.reevent.app.ui.components.HeroImageCard
import com.reevent.app.ui.components.InfoRow
import com.reevent.app.ui.components.LocationLine
import com.reevent.app.ui.components.MetricCard
import com.reevent.app.ui.components.MiniBarChart
import com.reevent.app.ui.components.PartnerLogoTile
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ProgressRing
import com.reevent.app.ui.components.QuickActionTile
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.RecoveryTimeline
import com.reevent.app.ui.components.ResourceCard
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.ScreenPadding
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.SectionTitle
import com.reevent.app.ui.components.SettingsRow
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.components.UploadPreviewCard
import com.reevent.app.ui.components.WarmChartColors
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCanvas
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventMuted
import com.reevent.app.ui.theme.ReEventPaper
import com.reevent.app.ui.theme.ReEventWarm
import com.reevent.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (ReEventScreen) -> Unit,
    title: String = "EcoCampus Open Day",
    subtitle: String = "Live recovery board",
    metrics: List<ImpactMetric> = emptyList(),
    resources: List<com.reevent.app.ui.ResourceItem> = emptyList(),
    recoverySteps: List<RecoveryStep> = emptyList(),
    hasEvent: Boolean = true,
    onManageEvents: (() -> Unit)? = null,
    onAddResource: (() -> Unit)? = null,
    onResourceClick: (com.reevent.app.ui.ResourceItem) -> Unit = { onNavigate(ReEventScreen.Passport) }
) {
    ReEventScaffold(selected = ReEventScreen.Home, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = title,
                    subtitle = subtitle,
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                HeroImageCard(
                    imageRes = R.drawable.onboarding_event_setup,
                    title = "Close-out starts before the event ends",
                    subtitle = "Tag resources, match partners, and publish reusable lots in one flow.",
                    chip = "Organizer mode"
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
                                }
                            )
                        }
                    }
                }
            } else {
                item {
                    HomeEmptyState(
                        title = if (hasEvent) "No resource summary yet" else "No event workspace yet",
                        detail = if (hasEvent) {
                            "Add the first resource lot to start a live recovery summary."
                        } else {
                            "Create an event before adding resources, partners, or recovery impact."
                        },
                        actionLabel = if (hasEvent) "Add a resource" else "Create an event",
                        onAction = if (hasEvent) onAddResource else onManageEvents
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
                        onClick = { if (hasEvent) onAddResource?.invoke() else onManageEvents?.invoke() }
                    )
                    QuickActionTile(
                        title = "Manage events",
                        detail = "Create, edit and switch event workspaces",
                        icon = Icons.Outlined.DateRange,
                        color = ReEventCoral,
                        onClick = { onManageEvents?.invoke() }
                    )
                    if (resources.isNotEmpty()) {
                        QuickActionTile(
                            title = "Run AI recovery match",
                            detail = "Rank reuse, repair and buy-back partners",
                            icon = Icons.Outlined.Star,
                            color = ReEventWarm,
                            onClick = { onNavigate(ReEventScreen.AiMatch) }
                        )
                    }
                    QuickActionTile(
                        title = "Open partner map",
                        detail = "See factories, repairers and collection points",
                        icon = Icons.Outlined.Map,
                        color = ReEventBlue,
                        onClick = { onNavigate(ReEventScreen.PartnerMap) }
                    )
                }
            }
            item {
                SectionTitle(title = "Circular workflow")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    if (recoverySteps.isEmpty()) {
                        EmptyWorkflowMessage(
                            message = if (hasEvent) {
                                "Your recovery activity will appear here once a resource lot is added."
                            } else {
                                "Create an event to begin a circular recovery workflow."
                            },
                            actionLabel = if (hasEvent) "Add a resource" else "Create an event",
                            onAction = if (hasEvent) onAddResource else onManageEvents
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
                    onAction = { onNavigate(ReEventScreen.Marketplace) }
                )
            }
            if (resources.isEmpty()) {
                item {
                    EmptyWorkflowMessage(
                        message = if (hasEvent) "No resource lots have been added yet." else "Your event's resource lots will appear here after it is created.",
                        actionLabel = if (hasEvent) "Add first resource" else "Create an event",
                        onAction = if (hasEvent) onAddResource else onManageEvents
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
    onAction: (() -> Unit)? = null
) {
    Surface(shape = RoundedCornerShape(18.dp), color = ReEventMintSoft) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            if (actionLabel != null && onAction != null) {
                SecondaryActionButton(actionLabel, onAction, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun HomeEmptyState(title: String, detail: String, actionLabel: String, onAction: (() -> Unit)?) {
    Surface(shape = RoundedCornerShape(20.dp), color = ReEventMintSoft, border = BorderStroke(1.dp, ReEventLine)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            if (onAction != null) PrimaryActionButton(actionLabel, onAction, Modifier.fillMaxWidth())
        }
    }
}

