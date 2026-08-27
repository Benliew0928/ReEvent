package com.reevent.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.reevent.app.R
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeDeepForest
import com.reevent.app.ui.theme.HomeEditorialFont
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGold
import com.reevent.app.ui.theme.HomeGreetingStyle
import com.reevent.app.ui.theme.HomeHeroTitleStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLabelStyle
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSectionTitleStyle
import com.reevent.app.ui.theme.HomeSupportingInk
import com.reevent.app.ui.theme.HomeSupportingTextStyle

@Composable
fun EditorialRoleHomeScreen(
    state: HomeDashboardUiState,
    onScopeSelected: (String) -> Unit,
    onTarget: (HomeTarget) -> Unit,
    onProfile: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ReEventScaffold(
        selected = TopLevelDestination.HOME,
        onNavigate = { onTarget(HomeTarget.Destination(it)) },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeCanvas),
        ) {
            Image(
                painter = painterResource(R.drawable.home_paper_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.055f),
            )
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 900.dp)
                    .fillMaxSize()
                    .testTag("home_dashboard_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    HomeHeader(
                        state = state,
                        onScopeSelected = onScopeSelected,
                        onProfile = onProfile,
                        onLeadingAction = {
                            if (state.role == HomeRole.ORGANIZER) {
                                onTarget(HomeTarget.Destination(TopLevelDestination.EVENTS))
                            } else {
                                onRefresh()
                            }
                        },
                    )
                }
                if (state.isRefreshing) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = HomeForest,
                            trackColor = HomeSage,
                        )
                    }
                }
                state.refreshError?.let { message ->
                    item { RefreshError(message = message, onRetry = onRetry) }
                }
                item { HomeHeroCard(state = state) }
                state.emptyState?.let { emptyState ->
                    item { HomeEmptyPanel(state = emptyState, onTarget = onTarget) }
                }
                item {
                    PrioritySection(
                        title = state.priorityTitle,
                        priorities = state.priorities,
                        onTarget = onTarget,
                    )
                }
                item {
                    HomeMetricStrip(
                        title = state.stripTitle,
                        metrics = state.stripMetrics,
                    )
                }
                item {
                    HomeQuickLinks(
                        links = state.quickLinks,
                        onTarget = onTarget,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    state: HomeDashboardUiState,
    onScopeSelected: (String) -> Unit,
    onProfile: () -> Unit,
    onLeadingAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                onClick = onLeadingAction,
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(16.dp),
                color = HomePaper,
                border = BorderStroke(1.dp, HomeLine),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (state.role == HomeRole.ORGANIZER) Icons.Outlined.CalendarMonth else Icons.Outlined.Refresh,
                        contentDescription = if (state.role == HomeRole.ORGANIZER) "Open events" else "Refresh dashboard",
                        tint = HomeForest,
                    )
                }
            }
            ScopeSelector(
                state = state,
                onScopeSelected = onScopeSelected,
                modifier = Modifier.weight(1f),
            )
            Surface(
                onClick = onProfile,
                modifier = Modifier.size(54.dp),
                shape = CircleShape,
                color = HomeSage,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials(state.displayName),
                        color = HomeInk,
                        fontFamily = HomeEditorialFont,
                        fontSize = 22.sp,
                        modifier = Modifier.semantics {
                            contentDescription = "Profile for ${state.displayName.ifBlank { "signed-in user" }}"
                        },
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.74f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(state.greeting, style = HomeGreetingStyle, color = HomeInk)
                Text(state.greetingSubtitle, style = HomeSupportingTextStyle, color = HomeSupportingInk)
            }
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(190.dp)
                    .height(112.dp)
                    .alpha(0.86f),
            )
        }
    }
}

@Composable
private fun ScopeSelector(
    state: HomeDashboardUiState,
    onScopeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("home_scope_selector"),
            shape = RoundedCornerShape(16.dp),
            color = HomePaper,
            border = BorderStroke(1.dp, HomeLine),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(HomeForest, CircleShape),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = state.scopeLabel,
                    modifier = Modifier.weight(1f),
                    color = HomeInk,
                    fontFamily = HomeBodyFont,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(Icons.Outlined.ExpandMore, contentDescription = "Choose dashboard scope", tint = HomeInk)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (state.scopes.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No scopes available") },
                    onClick = { expanded = false },
                    enabled = false,
                )
            }
            state.scopes.forEach { scope ->
                DropdownMenuItem(
                    text = { Text(scope.label) },
                    onClick = {
                        expanded = false
                        onScopeSelected(scope.id)
                    },
                    leadingIcon = if (scope.id == state.selectedScopeId) {
                        { Icon(Icons.Outlined.CheckCircle, contentDescription = null) }
                    } else null,
                )
            }
        }
    }
}

@Composable
private fun HomeHeroCard(
    state: HomeDashboardUiState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(HomeForest, HomeDeepForest, Color(0xFF006149)),
                ),
            )
            .testTag("home_hero"),
    ) {
        val stackHero = maxWidth < 350.dp
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            if (stackHero) {
                HeroCopy(state = state)
                HomeProgressRing(
                    progress = state.progress,
                    label = state.progressLabel,
                    modifier = Modifier
                        .size(184.dp)
                        .align(Alignment.CenterHorizontally),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeroCopy(state = state, modifier = Modifier.weight(1.15f))
                    HomeProgressRing(
                        progress = state.progress,
                        label = state.progressLabel,
                        modifier = Modifier
                            .weight(0.85f)
                            .aspectRatio(1f),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.35f)),
            )
            HeroMetrics(metrics = state.metrics)
        }
    }
}

@Composable
private fun HeroCopy(
    state: HomeDashboardUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        state.heroEyebrow?.let {
            Text(it, style = HomeLabelStyle, color = Color.White.copy(alpha = 0.9f))
        }
        Text(state.heroTitle, style = HomeHeroTitleStyle, color = Color.White)
        Text(state.heroBody, style = HomeBodyStyle, color = Color.White.copy(alpha = 0.92f))
    }
}

@Composable
private fun HomeProgressRing(
    progress: Float?,
    label: String,
    modifier: Modifier = Modifier,
) {
    val normalized = progress?.coerceIn(0f, 1f)
    Box(
        modifier = modifier.semantics {
            contentDescription = normalized?.let { "${(it * 100).toInt()} percent, $label" } ?: "$label unavailable"
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.065f
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (normalized != null) {
                drawArc(
                    color = Color(0xFFFFFCF3),
                    startAngle = -90f,
                    sweepAngle = 360f * normalized,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = normalized?.let { "${(it * 100).toInt()}%" } ?: "—",
                color = Color.White,
                fontFamily = HomeEditorialFont,
                fontSize = 43.sp,
                lineHeight = 46.sp,
            )
            Text(
                text = label,
                color = Color.White,
                style = HomeBodyStyle,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun HeroMetrics(
    metrics: List<HomeMetric>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < 330.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                metrics.forEach { metric -> HeroMetric(metric = metric) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                metrics.forEachIndexed { index, metric ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(72.dp)
                                .background(Color.White.copy(alpha = 0.28f)),
                        )
                    }
                    HeroMetric(metric = metric, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(
    metric: HomeMetric,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconBadge(icon = metric.icon, description = metric.label)
        Text(
            text = metric.value,
            color = Color.White,
            fontFamily = HomeEditorialFont,
            fontSize = 28.sp,
        )
        Text(
            text = metric.label,
            color = Color.White,
            fontFamily = HomeBodyFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 2,
        )
        metric.detail?.let {
            Text(it, color = HomeGold, fontFamily = HomeBodyFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PrioritySection(
    title: String,
    priorities: List<HomePriority>,
    onTarget: (HomeTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_priority_section"),
        shape = RoundedCornerShape(20.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        onClick(if (expanded) "Collapse priorities" else "Expand priorities") {
                            expanded = !expanded
                            true
                        }
                    }
                    .clickable { expanded = !expanded }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = HomeSectionTitleStyle, color = HomeInk, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = HomeSage) {
                    Text(
                        priorities.size.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = HomeInk,
                        fontFamily = HomeBodyFont,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = HomeInk,
                )
            }
            if (expanded) {
                if (priorities.isEmpty()) {
                    Text(
                        "Nothing needs your attention right now.",
                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                        style = HomeSupportingTextStyle,
                        color = HomeSupportingInk,
                    )
                } else {
                    priorities.forEachIndexed { index, priority ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(HomeLine),
                            )
                        }
                        PriorityRow(priority = priority, onTarget = onTarget)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityRow(
    priority: HomePriority,
    onTarget: (HomeTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = priority.target != null
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { priority.target?.let(onTarget) }
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconBadge(icon = priority.icon, description = priority.title, large = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Surface(shape = RoundedCornerShape(12.dp), color = if (enabled) HomeSage else HomeMist) {
                Text(
                    text = priority.badge,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = HomeLabelStyle,
                    color = if (enabled) HomeInk else HomeMuted,
                )
            }
            Text(priority.title, style = HomeCardTitleStyle, color = if (enabled) HomeInk else HomeMuted)
            Text(priority.detail, style = HomeSupportingTextStyle, color = HomeSupportingInk)
            priority.disabledReason?.let { Text(it, style = HomeLabelStyle, color = HomeGold) }
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = if (enabled) "Open ${priority.title}" else null,
            tint = if (enabled) HomeForest else HomeLine,
        )
    }
}

@Composable
private fun HomeMetricStrip(
    title: String,
    metrics: List<HomeMetric>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = HomeMist.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Text(title, style = HomeCardTitleStyle, color = HomeInk)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                if (maxWidth < 330.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        metrics.forEach { StripMetric(metric = it) }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        metrics.forEachIndexed { index, metric ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(58.dp)
                                        .background(HomeLine),
                                )
                            }
                            StripMetric(metric = metric, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StripMetric(
    metric: HomeMetric,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconBadge(icon = metric.icon, description = metric.label)
        Column {
            Text(metric.value, color = HomeInk, fontFamily = HomeEditorialFont, fontSize = 24.sp)
            Text(
                metric.label,
                color = HomeInk,
                fontFamily = HomeBodyFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
            )
        }
    }
}

@Composable
private fun HomeQuickLinks(
    links: List<HomeQuickLink>,
    onTarget: (HomeTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < 350.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                links.forEach { link -> QuickLink(link = link, onTarget = onTarget) }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                links.forEach { link ->
                    QuickLink(
                        link = link,
                        onTarget = onTarget,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickLink(
    link: HomeQuickLink,
    onTarget: (HomeTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { onTarget(link.target) },
        modifier = modifier.height(146.dp),
        shape = RoundedCornerShape(17.dp),
        color = HomePaper.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconBadge(icon = link.icon, description = link.title)
            Text(link.title, color = HomeInk, fontFamily = HomeEditorialFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(
                link.detail,
                color = HomeSupportingInk,
                style = HomeSupportingTextStyle.copy(fontSize = 12.sp, lineHeight = 15.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Open ${link.title}", tint = HomeForest)
        }
    }
}

@Composable
private fun HomeEmptyPanel(
    state: HomeEmptyState,
    onTarget: (HomeTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = HomeSage,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(state.title, style = HomeCardTitleStyle, color = HomeInk)
            Text(state.detail, style = HomeSupportingTextStyle, color = HomeSupportingInk)
            TextButton(onClick = { onTarget(state.target) }) {
                Text(state.actionLabel, color = HomeForest, fontFamily = HomeBodyFont, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun RefreshError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = HomeSage,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(message, modifier = Modifier.weight(1f), style = HomeBodyStyle, color = HomeInk)
            TextButton(onClick = onRetry) { Text("Retry", color = HomeForest) }
        }
    }
}

@Composable
private fun IconBadge(
    icon: HomeIcon,
    description: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val size = if (large) 58.dp else 42.dp
    Box(
        modifier = modifier
            .size(size)
            .background(HomeSage.copy(alpha = 0.95f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon.vector(),
            contentDescription = description,
            tint = HomeForest,
            modifier = Modifier.size(if (large) 29.dp else 23.dp),
        )
    }
}

private fun HomeIcon.vector(): ImageVector = when (this) {
    HomeIcon.EVENT -> Icons.Outlined.Event
    HomeIcon.TAG -> Icons.Outlined.Tag
    HomeIcon.LEAF -> Icons.Outlined.Eco
    HomeIcon.RECYCLE -> Icons.Outlined.Recycling
    HomeIcon.HANDOVER -> Icons.Outlined.Handshake
    HomeIcon.RETURN -> Icons.Outlined.Refresh
    HomeIcon.CHECK -> Icons.Outlined.CheckCircle
    HomeIcon.REQUEST -> Icons.AutoMirrored.Outlined.Assignment
    HomeIcon.TRUCK -> Icons.Outlined.LocalShipping
    HomeIcon.PASSPORT -> Icons.Outlined.Badge
    HomeIcon.QR -> Icons.Outlined.QrCodeScanner
    HomeIcon.RESOURCE -> Icons.Outlined.Inventory2
    HomeIcon.PARTNERS -> Icons.Outlined.Groups
    HomeIcon.ACCOUNT -> Icons.Outlined.AccountCircle
    HomeIcon.PROGRAMME -> Icons.Outlined.Dashboard
    HomeIcon.IMPACT -> Icons.Outlined.Insights
    HomeIcon.CAPACITY -> Icons.Outlined.Speed
}

private fun initials(displayName: String): String = displayName
    .trim()
    .split(Regex("\\s+"))
    .filter(String::isNotBlank)
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .joinToString("")
    .ifBlank { "ME" }
