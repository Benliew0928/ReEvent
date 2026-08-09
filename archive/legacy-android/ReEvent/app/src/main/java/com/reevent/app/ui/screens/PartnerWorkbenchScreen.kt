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
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.TransactionStatus
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
fun PartnerWorkbenchScreen(
    onNavigate: (ReEventScreen) -> Unit,
    programmes: List<CircularProgramme> = emptyList(),
    transactions: List<CircularTransaction> = emptyList(),
    onCreateProgramme: () -> Unit = {}
) {
    ReEventScaffold(selected = ReEventScreen.PartnerWorkbench, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Partner workbench",
                    subtitle = "Factory and repair partner intake",
                    // The workbench is the partner's root page; use Account instead of a fake back path.
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            if (programmes.isNotEmpty()) item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ReEventMintSoft,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Active programmes", style = MaterialTheme.typography.labelLarge, color = ReEventGreenDeep)
                        Text(programmes.first().name, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                        Text(
                            "${programmes.count { it.active }} active programme${if (programmes.count { it.active } == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventMuted
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val incoming = transactions.firstOrNull { it.status != TransactionStatus.COMPLETED && it.status != TransactionStatus.CANCELLED }
                        if (incoming != null) {
                            StatusChip(text = "Incoming lot", color = ReEventBlue)
                            Text(
                                text = "${incoming.type.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)} handover",
                                style = MaterialTheme.typography.titleLarge,
                                color = ReEventInk
                            )
                            Text(
                                text = "${incoming.quantity} item${if (incoming.quantity == 1) "" else "s"} awaiting your programme decision.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ReEventMuted
                            )
                            HorizontalDivider(color = ReEventLine)
                            InfoRow("Status", incoming.status.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase))
                            InfoRow("Reference", incoming.id.take(8))
                            InfoRow("Programmes", programmes.count { it.active }.toString())
                        } else {
                            StatusChip(text = "No incoming lots", color = ReEventBlue)
                            Text(
                                text = "Your partner queue is clear",
                                style = MaterialTheme.typography.titleLarge,
                                color = ReEventInk
                            )
                            Text(
                                text = "New authorised handovers will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ReEventMuted
                            )
                        }
                    }
                }
            }
            item {
                SecondaryActionButton(
                    text = "Open partner network",
                    icon = Icons.Outlined.Map,
                    onClick = { onNavigate(ReEventScreen.PartnerMap) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (programmes.isEmpty()) item {
                PrimaryActionButton(
                    text = "Create circular programme",
                    icon = Icons.Outlined.Add,
                    onClick = onCreateProgramme,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (transactions.any { it.status != TransactionStatus.COMPLETED && it.status != TransactionStatus.CANCELLED }) item {
                PrimaryActionButton(
                    text = "Review incoming handover",
                    icon = Icons.Outlined.CheckCircle,
                    onClick = { onNavigate(ReEventScreen.PartnerMap) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

