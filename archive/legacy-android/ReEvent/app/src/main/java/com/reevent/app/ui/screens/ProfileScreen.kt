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
fun ProfileScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = null, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Profile",
                    subtitle = "Organizer workspace and circular roles",
                    onBack = { onNavigate(ReEventScreen.Home) }
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventGreenDeep,
                    border = BorderStroke(1.dp, ReEventGreenDeep)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(ReEventWarm),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = ReEventGreenDeep,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aina Rahman",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Event sustainability lead",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Spacer(Modifier.height(8.dp))
                            StatusChip(text = "Organizer verified", color = ReEventWarm)
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsRow(
                        title = "Participant return mode",
                        detail = "Preview QR return, swap and reward screens",
                        icon = Icons.Outlined.Refresh,
                        onClick = { onNavigate(ReEventScreen.ParticipantReturn) }
                    )
                    SettingsRow(
                        title = "Partner workbench",
                        detail = "Preview factory buy-back and repair workflow",
                        icon = Icons.Outlined.Settings,
                        onClick = { onNavigate(ReEventScreen.PartnerWorkbench) }
                    )
                    SettingsRow(
                        title = "Back to dashboard",
                        detail = "Continue organizer prototype",
                        icon = Icons.Outlined.Home,
                        onClick = { onNavigate(ReEventScreen.Home) }
                    )
                }
            }
        }
    }
}

