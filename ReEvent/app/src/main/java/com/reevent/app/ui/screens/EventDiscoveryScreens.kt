package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.R
import com.reevent.app.core.model.DiscoverableEvent
import com.reevent.app.core.model.User
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.ProfileAvatarButton
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGreetingStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSupportingInk
import com.reevent.app.ui.theme.HomeSupportingTextStyle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiscoverableEventListLiveScreen(
    user: User,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { viewModel.refreshDiscoverableEvents() }
    val events by viewModel.discoverableEvents().collectAsState(emptyList())
    val action by viewModel.action.collectAsState()
    EventDiscoveryListContent(
        user = user,
        events = events,
        loading = action.loading,
        error = action.error,
        notice = action.notice,
        onOpen = onOpen,
        onRefresh = viewModel::refreshDiscoverableEvents,
        onBack = onBack,
        onNavigate = onNavigate,
    )
}

@Composable
internal fun EventDiscoveryListContent(
    user: User,
    events: List<DiscoverableEvent>,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    error: String? = null,
    notice: String? = null,
    onOpen: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    ReEventScaffold(
        selected = TopLevelDestination.EVENTS,
        onNavigate = onNavigate,
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeCanvas),
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.home_paper_texture),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().alpha(0.055f),
            )
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("discoverable_events_list"),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = padding.calculateTopPadding() + 16.dp,
                    end = 20.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("EVENTS", style = HomeSupportingTextStyle.copy(letterSpacing = 1.2.sp), color = HomeForest)
                            Text("Find a circular event", style = HomeGreetingStyle.copy(fontSize = 40.sp, lineHeight = 44.sp), color = HomeInk)
                            Text("Published events shared by organisers.", style = HomeSupportingTextStyle, color = HomeSupportingInk)
                        }
                        ProfileAvatarButton(displayName = user.displayName, onClick = { onNavigate(TopLevelDestination.ACCOUNT) })
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.testTag("discoverable_events_back")) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                            Spacer(Modifier.width(6.dp))
                            Text("Back")
                        }
                        OutlinedButton(onClick = onRefresh, modifier = Modifier.testTag("discoverable_events_refresh")) {
                            Text("Refresh")
                        }
                    }
                }
                if (loading) {
                    item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth().testTag("discoverable_events_loading"), color = HomeForest, trackColor = HomeSage) }
                }
                error?.let { message ->
                    item { EventDiscoveryMessage(message, isError = true) }
                }
                notice?.let { message ->
                    item { EventDiscoveryMessage(message, isError = false) }
                }
                if (events.isEmpty() && !loading) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().testTag("discoverable_events_empty"),
                            shape = RoundedCornerShape(22.dp),
                            color = HomePaper.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, HomeLine),
                            tonalElevation = 0.dp,
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("No published events yet", style = HomeCardTitleStyle, color = HomeInk)
                                Text("When an organiser publishes an event, it will appear here with only its approved public details.", style = HomeSupportingTextStyle, color = HomeSupportingInk)
                            }
                        }
                    }
                } else {
                    items(events, key = DiscoverableEvent::id) { event ->
                        DiscoverableEventCard(event = event, onOpen = { onOpen(event.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoverableEventCard(
    event: DiscoverableEvent,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpen,
        modifier = modifier.fillMaxWidth().testTag("discoverable_event_${event.id}"),
        shape = RoundedCornerShape(22.dp),
        color = HomeForest,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(event.name, style = HomeCardTitleStyle.copy(fontSize = 28.sp, lineHeight = 31.sp), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                EventDiscoveryMetadata(Icons.Outlined.CalendarMonth, event.displayDateRange(), "Event dates")
                EventDiscoveryMetadata(Icons.Outlined.LocationOn, event.venue, "Event location")
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = "Open event", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun DiscoverableEventDetailLiveScreen(
    user: User,
    eventId: String,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(eventId) { viewModel.refreshDiscoverableEvents() }
    val event by viewModel.discoverableEvent(eventId).collectAsState(null)
    val action by viewModel.action.collectAsState()
    EventDiscoveryDetailContent(
        user = user,
        event = event,
        loading = action.loading,
        error = action.error,
        onBack = onBack,
        onNavigate = onNavigate,
    )
}

@Composable
internal fun EventDiscoveryDetailContent(
    user: User,
    event: DiscoverableEvent?,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    error: String? = null,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    ReEventScaffold(selected = TopLevelDestination.EVENTS, onNavigate = onNavigate, modifier = modifier) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HomeCanvas)
                .widthIn(max = 760.dp)
                .testTag("discoverable_event_detail"),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = padding.calculateTopPadding() + 16.dp,
                end = 20.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.testTag("discoverable_event_detail_back")) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                        Spacer(Modifier.width(6.dp))
                        Text("Back")
                    }
                    Spacer(Modifier.weight(1f))
                    ProfileAvatarButton(displayName = user.displayName, onClick = { onNavigate(TopLevelDestination.ACCOUNT) })
                }
            }
            if (loading) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = HomeForest, trackColor = HomeSage) }
            error?.let { message -> item { EventDiscoveryMessage(message, isError = true) } }
            if (event == null) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth().testTag("discoverable_event_unavailable"), shape = RoundedCornerShape(22.dp), color = HomePaper, border = BorderStroke(1.dp, HomeLine), tonalElevation = 0.dp) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Event unavailable", style = HomeCardTitleStyle, color = HomeInk)
                            Text("This event is no longer published or is not available in your current account.", style = HomeSupportingTextStyle, color = HomeSupportingInk)
                        }
                    }
                }
            } else {
                item {
                    Surface(modifier = Modifier.fillMaxWidth().testTag("discoverable_event_public_card"), shape = RoundedCornerShape(24.dp), color = HomeForest, tonalElevation = 0.dp) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                            Text("PUBLISHED EVENT", style = HomeSupportingTextStyle.copy(letterSpacing = 1.3.sp), color = HomeSage)
                            Text(event.name, style = HomeGreetingStyle.copy(fontSize = 42.sp, lineHeight = 44.sp), color = Color.White)
                            Text(event.description.ifBlank { "A circular event shared by the ReEvent community." }, style = HomeBodyStyle, color = Color.White.copy(alpha = 0.88f))
                            EventDiscoveryMetadata(Icons.Outlined.CalendarMonth, event.displayDateRange(), "Event dates", color = Color.White)
                            EventDiscoveryMetadata(Icons.Outlined.LocationOn, event.venue, "Event location", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDiscoveryMetadata(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
    color: Color = HomeSupportingInk,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = contentDescription, tint = color, modifier = Modifier.size(19.dp))
        Text(text, style = HomeSupportingTextStyle, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EventDiscoveryMessage(message: String, isError: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isError) Color(0xFFFFE6E8) else HomeSage,
        border = BorderStroke(1.dp, if (isError) Color(0xFFE8B8BD) else HomeLine),
        tonalElevation = 0.dp,
    ) {
        Text(message, modifier = Modifier.padding(14.dp), style = HomeSupportingTextStyle, color = if (isError) Color(0xFF8A2836) else HomeInk)
    }
}

private fun DiscoverableEvent.displayDateRange(): String {
    val zone = runCatching { ZoneId.of(timezoneId) }.getOrElse { ZoneId.of("UTC") }
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)
    return "${formatter.format(Instant.ofEpochMilli(startsAt).atZone(zone))} – ${formatter.format(Instant.ofEpochMilli(endsAt).atZone(zone))}"
}
