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
fun OnboardingScreen(onNavigate: (ReEventScreen) -> Unit) {
    var selectedRole by rememberSaveable { mutableStateOf(ReEventRole.Organizer) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ReEventCanvas),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.onboarding_event_setup),
                    contentDescription = "Reusable event setup",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ReEventGreenDeep.copy(alpha = 0.1f),
                                    ReEventGreenDeep.copy(alpha = 0.62f)
                                )
                            )
                        )
                )
                BrandLockup(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = ScreenPadding, top = 16.dp)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(ScreenPadding)
                ) {
                    StatusChip(text = "SDG 12 circular events", color = ReEventWarm)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Turn event leftovers into the next event's supply chain.",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "A UI prototype for organizers, participants and manufacturing partners to reuse, repair, resell and recover event resources.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.84f)
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Circular events start before teardown",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ReEventInk
                )
                Text(
                    text = "Choose the role you want to preview. You can switch roles later from the profile screen.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ReEventMuted
                )
                ReEventRole.values().forEach { role ->
                    RoleOptionCard(role, selectedRole == role) { selectedRole = role }
                }
                PrimaryActionButton(
                    text = "Choose role",
                    icon = Icons.Outlined.Home,
                    onClick = { onNavigate(ReEventScreen.SignIn) },
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryActionButton(
                    text = "Already have an account? Sign in",
                    onClick = { onNavigate(ReEventScreen.SignIn) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    role: ReEventRole,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) ReEventMintSoft else ReEventPaper,
        border = BorderStroke(1.dp, if (selected) ReEventGreen.copy(alpha = 0.32f) else ReEventLine)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) ReEventGreen else ReEventLine),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = role.label.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) Color.White else ReEventMuted
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = role.label, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                Text(text = role.description, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Selected",
                    tint = ReEventGreen
                )
            }
        }
    }
}

