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
fun SignInScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = null, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    BrandLockup(compact = true)
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ReEventInk
                    )
                    Text(
                        text = "Sign in to continue managing verified event resources and recovery routes.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ReEventMuted
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormFieldPreview(
                        label = "Email",
                        value = "organizer@reevent.demo",
                        icon = Icons.Outlined.Email
                    )
                    FormFieldPreview(
                        label = "Password",
                        value = "••••••••••",
                        icon = Icons.Outlined.Lock
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryActionButton(
                        text = "Sign in",
                        onClick = { onNavigate(ReEventScreen.Home) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SecondaryActionButton(
                        text = "Continue with Google",
                        onClick = { onNavigate(ReEventScreen.Home) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                TrustStrip()
            }
        }
    }
}

@Composable
fun HomeScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = ReEventScreen.Home, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "EcoCampus Open Day",
                    subtitle = "Live recovery board • 28 July 2026",
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
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        value = MockData.metrics[0].value,
                        label = MockData.metrics[0].label,
                        detail = MockData.metrics[0].detail,
                        color = ReEventGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                    AdaptiveTwoPane(
                        first = {
                            MetricCard(
                                value = MockData.metrics[1].value,
                                label = MockData.metrics[1].label,
                                detail = MockData.metrics[1].detail,
                                color = ReEventBlue,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        second = {
                            MetricCard(
                                value = MockData.metrics[2].value,
                                label = MockData.metrics[2].label,
                                detail = MockData.metrics[2].detail,
                                color = ReEventCoral,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
            }
            item {
                SectionTitle(title = "Fast actions")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionTile(
                        title = "Add resource lot",
                        detail = "Create item passport with photos and condition",
                        icon = Icons.Outlined.Add,
                        color = ReEventGreen,
                        onClick = { onNavigate(ReEventScreen.AddResource) }
                    )
                    QuickActionTile(
                        title = "Run AI recovery match",
                        detail = "Rank reuse, repair and buy-back partners",
                        icon = Icons.Outlined.Star,
                        color = ReEventWarm,
                        onClick = { onNavigate(ReEventScreen.AiMatch) }
                    )
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
                    RecoveryTimeline(modifier = Modifier.padding(16.dp))
                }
            }
            item {
                SectionTitle(
                    title = "High-value lots",
                    action = "See all",
                    onAction = { onNavigate(ReEventScreen.Marketplace) }
                )
            }
            items(MockData.resources.take(2)) { item ->
                ResourceCard(item = item, onClick = { onNavigate(ReEventScreen.Passport) })
            }
        }
    }
}

@Composable
fun MarketplaceScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = ReEventScreen.Marketplace, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Circular marketplace",
                    subtitle = "Reusable, repairable and recoverable event stock",
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                FormFieldPreview(
                    label = "Search available resources",
                    value = "Search by item, material or location",
                    icon = Icons.Outlined.Search
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChipPreview("All", true)
                    FilterChipPreview("Rent", false)
                    FilterChipPreview("Donate", false)
                    FilterChipPreview("Repair", false)
                    FilterChipPreview("Take-back", false)
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventGreenDeep,
                    border = BorderStroke(1.dp, ReEventGreenDeep)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = ReEventWarm,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Value-first sorting",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Lots are ranked by resale value, reuse likelihood and diversion impact.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }
            items(MockData.resources) { item ->
                ResourceCard(item = item, onClick = { onNavigate(ReEventScreen.Passport) })
            }
        }
    }
}

@Composable
fun AddResourceScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = ReEventScreen.AddResource, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Add resource lot",
                    subtitle = "Create a reusable passport before close-out",
                    onBack = { onNavigate(ReEventScreen.Home) },
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                UploadPreviewCard()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormFieldPreview("Item title", "Reusable Cup Crates", icon = Icons.Outlined.ShoppingBag)
                    FormFieldPreview("Category", "Food & beverage", icon = Icons.Outlined.Tune)
                    FormFieldPreview("Material", "PP reusable plastic", icon = Icons.Outlined.CheckCircle)
                    FormFieldPreview("Quantity", "320 units across 16 crates", icon = Icons.Outlined.Add)
                    FormFieldPreview("Unit", "Cups", icon = Icons.Outlined.ShoppingBag)
                    FormFieldPreview("Condition", "Clean, reusable, 6 cracked cups removed", icon = Icons.Outlined.CheckCircle)
                    FormFieldPreview("Available from", "28 July 2026, 6:30 PM", icon = Icons.Outlined.DateRange)
                    FormFieldPreview("Pickup point", "EcoCampus Hall loading bay", icon = Icons.Outlined.LocationOn)
                    FormFieldPreview("End-of-event plan", "Reuse first, partner buy-back fallback", icon = Icons.Outlined.Refresh)
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventMintSoft,
                    border = BorderStroke(1.dp, ReEventGreen.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusChip(text = "AI suggestion", color = ReEventGreen)
                        Text(
                            text = "Publish for reuse first, keep GreenCycle as fallback buy-back.",
                            style = MaterialTheme.typography.titleMedium,
                            color = ReEventInk
                        )
                        Text(
                            text = "Estimated recovered value: RM 480 resale or RM 145 material buy-back.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventMuted
                        )
                    }
                }
            }
            item {
                PrimaryActionButton(
                    text = "Generate passport",
                    icon = Icons.Outlined.CheckCircle,
                    onClick = { onNavigate(ReEventScreen.Passport) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PassportScreen(onNavigate: (ReEventScreen) -> Unit) {
    val item = MockData.resources.first()
    ReEventScaffold(selected = ReEventScreen.Marketplace, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Digital passport",
                    subtitle = "Verified route for ${item.title}",
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
                        Text(text = item.title, style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                        LocationLine(text = "${item.location} • ${item.owner}")
                        HorizontalDivider(color = ReEventLine)
                        InfoRow("Quantity", item.quantity)
                        InfoRow("Condition", "Reusable, cleaned, 98% complete")
                        InfoRow("Material", "PP reusable cup + stackable crate")
                        InfoRow("Current value", item.price)
                    }
                }
            }
            item {
                AdaptiveTwoPane(
                    first = { FakeQrPanel(modifier = Modifier.fillMaxWidth()) },
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
                                text = "RE-CUP-0728",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "Signed by organizer, available to partners and buyers.",
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
                    RecoveryTimeline(modifier = Modifier.padding(16.dp))
                }
            }
            item {
                PrimaryActionButton(
                    text = "Find partner matches",
                    icon = Icons.Outlined.Star,
                    onClick = { onNavigate(ReEventScreen.AiMatch) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AiMatchScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = ReEventScreen.AddResource, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "AI recovery match",
                    subtitle = "Best circular routes by value, distance and impact",
                    onBack = { onNavigate(ReEventScreen.Passport) },
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventGreenDeep,
                    border = BorderStroke(1.dp, ReEventGreenDeep)
                ) {
                    AdaptiveTwoPane(
                        modifier = Modifier.padding(18.dp),
                        stackedAlignment = Alignment.CenterHorizontally,
                        first = {
                        ProgressRing(
                            progress = 0.86f,
                            centerText = "86",
                            label = "score",
                            color = ReEventWarm,
                            modifier = Modifier.size(112.dp)
                        )
                        },
                        second = {
                        Column {
                            Text(
                                text = "Reuse-first route selected",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = "The cups should be listed for nearby organizers for 24 hours before material buy-back is triggered.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                        }
                    )
                }
            }
            items(MockData.matches) { match ->
                PartnerMatchCard(match = match, onClick = { onNavigate(ReEventScreen.PartnerMap) })
            }
            item {
                SecondaryActionButton(
                    text = "Open partner map",
                    icon = Icons.Outlined.Map,
                    onClick = { onNavigate(ReEventScreen.PartnerMap) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PartnerMapScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = ReEventScreen.PartnerMap, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Partner network",
                    subtitle = "Factories, repair partners and collection points",
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
                    Image(
                        painter = painterResource(R.drawable.map_partner_mock),
                        contentDescription = "Partner map",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = ReEventPaper.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, ReEventLine)
                    ) {
                        Text(
                            text = "8 partners within 10 km",
                            style = MaterialTheme.typography.labelLarge,
                            color = ReEventGreenDeep,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
            item {
                PartnerLogoTile()
            }
            items(MockData.matches) { match ->
                PartnerMatchCard(match = match, onClick = { onNavigate(ReEventScreen.PartnerWorkbench) })
            }
        }
    }
}

@Composable
fun ImpactScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = ReEventScreen.Impact, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Impact board",
                    subtitle = "Circular economy proof for reporting and marks",
                    onProfile = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    AdaptiveTwoPane(
                        modifier = Modifier.padding(18.dp),
                        stackedAlignment = Alignment.CenterHorizontally,
                        first = {
                        ProgressRing(
                            progress = 0.83f,
                            centerText = "83%",
                            label = "recovered",
                            modifier = Modifier.size(142.dp)
                        )
                        },
                        second = {
                        Column {
                            StatusChip(text = "SDG 12 aligned", color = ReEventGreen)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "The event avoided disposal by routing items to reuse, repair and remanufacturing.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = ReEventInk
                            )
                        }
                        }
                    )
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionTitle(title = "Recovery channels")
                        MiniBarChart(
                            values = listOf(0.84f, 0.56f, 0.72f, 0.43f),
                            colors = WarmChartColors
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            LegendDot("Reuse", ReEventGreen)
                            LegendDot("Repair", ReEventWarm)
                            LegendDot("Buy-back", ReEventBlue)
                        }
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(ReEventGreenDeep)
                ) {
                    Image(
                        painter = painterResource(R.drawable.impact_badge_high_recovery),
                        contentDescription = "High recovery badge",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MockData.metrics.forEachIndexed { index, metric ->
                        MetricCard(
                            value = metric.value,
                            label = metric.label,
                            detail = metric.detail,
                            color = WarmChartColors[index],
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

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

@Composable
fun ParticipantReturnScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = null, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Participant return",
                    subtitle = "Swap, return and reward flow",
                    onBack = { onNavigate(ReEventScreen.Profile) }
                )
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ReEventPaper,
                    border = BorderStroke(1.dp, ReEventLine)
                ) {
                    AdaptiveTwoPane(
                        modifier = Modifier.padding(16.dp),
                        first = { FakeQrPanel(modifier = Modifier.fillMaxWidth()) },
                        second = {
                        Column {
                            StatusChip(text = "Return pass", color = ReEventGreen)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Scan at exit booth",
                                style = MaterialTheme.typography.titleLarge,
                                color = ReEventInk
                            )
                            Text(
                                text = "Return cups, badges or bags to earn reward points and keep the event loop closed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ReEventMuted
                            )
                        }
                        }
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RewardStep("Return reusable cup", "+60 pts", ResourceTone.Ready)
                    RewardStep("Swap unwanted merch", "+45 pts", ResourceTone.Hot)
                    RewardStep("Donate fabric tote", "+35 pts", ResourceTone.Repair)
                }
            }
        }
    }
}

@Composable
fun PartnerWorkbenchScreen(onNavigate: (ReEventScreen) -> Unit) {
    ReEventScaffold(selected = null, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Partner workbench",
                    subtitle = "Factory and repair partner intake",
                    onBack = { onNavigate(ReEventScreen.PartnerMap) }
                )
            }
            item {
                PartnerLogoTile()
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
                        StatusChip(text = "Incoming lot", color = ReEventBlue)
                        Text(
                            text = "Acrylic signage batch",
                            style = MaterialTheme.typography.titleLarge,
                            color = ReEventInk
                        )
                        Text(
                            text = "18 boards, verified clean, ready for remanufacturing quote.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventMuted
                        )
                        HorizontalDivider(color = ReEventLine)
                        InfoRow("Offer", "RM 1.20/kg")
                        InfoRow("Pickup", "Tomorrow, 10:30 AM")
                        InfoRow("Output", "New modular signage sheets")
                    }
                }
            }
            item {
                PrimaryActionButton(
                    text = "Accept buy-back lot",
                    icon = Icons.Outlined.CheckCircle,
                    onClick = { onNavigate(ReEventScreen.Impact) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CompactProof(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, color = ReEventGreenDeep)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
        }
    }
}

@Composable
private fun TrustStrip() {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val items = listOf(
        "Universities" to "Event teams",
        "Community events" to "Local organisers",
        "Circular partners" to "Verified routes"
    )
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { (value, label) -> CompactProof(value, label, Modifier.fillMaxWidth()) }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { (value, label) -> CompactProof(value, label, Modifier.weight(1f)) }
        }
    }
}

/** Keeps paired cards legible on compact portrait screens and uses the wider layout elsewhere. */
@Composable
private fun AdaptiveTwoPane(
    modifier: Modifier = Modifier,
    stackedAlignment: Alignment.Horizontal = Alignment.Start,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    val stackVertically = LocalConfiguration.current.screenWidthDp < 420
    if (stackVertically) {
        Column(
            modifier = modifier,
            horizontalAlignment = stackedAlignment,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            first()
            second()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) { first() }
            Box(modifier = Modifier.weight(1f)) { second() }
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

@Composable
private fun FilterChipPreview(
    text: String,
    selected: Boolean
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) ReEventGreenDeep else ReEventPaper,
        border = BorderStroke(1.dp, if (selected) ReEventGreenDeep else ReEventLine)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Color.White else ReEventInk,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun PartnerMatchCard(
    match: PartnerMatch,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(match.tone.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = match.score,
                        style = MaterialTheme.typography.labelLarge,
                        color = match.tone.color,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = ReEventInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = match.type, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
                }
                StatusChip(text = match.tone.label, color = match.tone.color)
            }
            Text(text = match.detail, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallFact(match.distance, ReEventBlue)
                SmallFact(match.offer, ReEventGreen)
            }
        }
    }
}

@Composable
private fun SmallFact(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.14f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun LegendDot(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = ReEventMuted)
    }
}

@Composable
private fun RewardStep(
    title: String,
    points: String,
    tone: ResourceTone
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tone.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = tone.color
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ReEventInk,
                modifier = Modifier.weight(1f)
            )
            StatusChip(text = points, color = tone.color)
        }
    }
}
