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
import com.reevent.app.ui.MockData
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
fun PassportScreen(
    onNavigate: (ReEventScreen) -> Unit,
    item: com.reevent.app.ui.ResourceItem? = MockData.resources.firstOrNull(),
    recoverySteps: List<RecoveryStep> = MockData.recoverySteps
) {
    ReEventScaffold(selected = ReEventScreen.Marketplace, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Digital passport",
                    subtitle = item?.let { "Verified route for ${it.title}" } ?: "Select a resource to view its verified route",
                    onBack = { onNavigate(ReEventScreen.Marketplace) },
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(ReEventMintSoft)
                ) {
                    if (item == null) {
                        Text(
                            text = "No resource selected",
                            style = MaterialTheme.typography.titleLarge,
                            color = ReEventMuted,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Image(
                            painter = painterResource(item.imageRes),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        StatusChip(
                            text = item.tone.label,
                            color = item.tone.color,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (item == null) {
                            Text("Resource details will appear here.", color = ReEventMuted)
                        } else {
                            Text(text = item.title, style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                            LocationLine(text = "${item.location} • ${item.owner}")
                            HorizontalDivider(color = ReEventLine)
                            InfoRow("Quantity", item.quantity)
                            InfoRow("Condition", item.tone.label)
                            InfoRow("Material", item.category)
                            InfoRow("Current value", item.price)
                        }
                    }
                }
            }
            item {
                AdaptiveTwoPane(
                    first = {
                        if (item == null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = ReEventMintSoft
                            ) {
                                Text(
                                    text = "QR code pending",
                                    color = ReEventMuted,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            FakeQrPanel(modifier = Modifier.fillMaxWidth())
                        }
                    },
                    second = {
                        Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = ReEventGreenDeep
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Passport ID",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.68f)
                            )
                            Text(
                                text = item?.id ?: "Not generated",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = if (item == null) "A passport ID will be created when the resource is saved." else "Available to authorised partners and buyers.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                    }
                )
            }
            item {
                SectionTitle(title = "Recommended route")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    if (recoverySteps.isEmpty()) {
                        Text("No recovery route has been recorded yet.", color = ReEventMuted, modifier = Modifier.padding(16.dp))
                    } else {
                        RecoveryTimeline(modifier = Modifier.padding(16.dp), steps = recoverySteps)
                    }
                }
            }
            item {
                PrimaryActionButton(
                    text = "Find partner matches",
                    icon = Icons.Outlined.Star,
                    onClick = { if (item != null) onNavigate(ReEventScreen.AiMatch) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

