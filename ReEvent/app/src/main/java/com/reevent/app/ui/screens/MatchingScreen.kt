package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.ui.components.EditorialDetailHeader
import com.reevent.app.ui.components.EditorialDetailScaffold
import com.reevent.app.ui.components.EditorialEmptyState
import com.reevent.app.ui.components.EditorialNotice
import com.reevent.app.ui.components.EditorialSectionCard
import com.reevent.app.ui.components.EditorialTextAction
import com.reevent.app.ui.materials.MaterialFamilyIcon
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGold
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSectionTitleStyle
import com.reevent.app.ui.theme.HomeSupportingTextStyle
import kotlinx.coroutines.flow.flowOf

@Composable
fun MatchingLiveScreen(
    user: User,
    resourceId: String,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeatureViewModel = hiltViewModel(),
    discoveryViewModel: PartnerMapViewModel = hiltViewModel(),
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val event by (resource?.eventId?.let(viewModel::event) ?: flowOf(null)).collectAsState(null)
    val discovery by discoveryViewModel.state.collectAsState()
    val action by viewModel.action.collectAsState()
    var selected by remember { mutableStateOf<PartnerCandidate?>(null) }
    var confirmation by remember { mutableStateOf<PartnerCandidate?>(null) }

    LaunchedEffect(user.id, resourceId) {
        viewModel.refresh()
        discoveryViewModel.load(resourceId)
    }

    MatchingEditorialContent(
        user = user,
        resource = resource,
        eventLocation = event?.venue.orEmpty(),
        discovery = discovery.result,
        loading = discovery.loading || action.loading,
        error = discovery.error ?: action.error,
        notice = action.notice,
        modifier = modifier,
        onBack = onBack,
        onOpenMap = onOpenMap,
        onRetry = { discoveryViewModel.load(resourceId) },
        onCandidate = { selected = it },
    )

    val selectedCandidate = selected
    val currentResource = resource
    if (selectedCandidate != null && currentResource != null) {
        PartnerCandidateDetailDialog(
            candidate = selectedCandidate,
            resource = currentResource,
            eligibleResources = emptyList(),
            canRequest = currentResource.ownerId == user.id &&
                currentResource.status == ResourceStatus.ACTIVE &&
                currentResource.quantity > 0,
            onDismiss = { selected = null },
            onOpenPassport = {},
            onRequest = {
                selected = null
                confirmation = selectedCandidate
            },
        )
    }
    val candidateToConfirm = confirmation
    if (candidateToConfirm != null && currentResource != null) {
        PartnerRecoveryConfirmationDialog(
            candidate = candidateToConfirm,
            resource = currentResource,
            onDismiss = { confirmation = null },
            onConfirm = {
                confirmation = null
                viewModel.createPartnerHandover(user, currentResource, candidateToConfirm.programme)
            },
        )
    }
}

@Composable
internal fun MatchingEditorialContent(
    user: User,
    resource: ResourceItem?,
    eventLocation: String,
    discovery: PartnerDiscoveryResult,
    loading: Boolean,
    error: String?,
    notice: String?,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onRetry: () -> Unit,
    onCandidate: (PartnerCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorialDetailScaffold(
        selected = null,
        onNavigate = {},
        modifier = modifier,
        showNavigation = false,
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val horizontalPadding = if (maxWidth < 380.dp) 12.dp else 18.dp
            val columns = if (maxWidth >= 840.dp) GridCells.Fixed(2) else GridCells.Fixed(1)
            LazyVerticalGrid(
                columns = columns,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .widthIn(max = 1120.dp)
                    .testTag("matching_editorial_grid"),
                contentPadding = PaddingValues(
                    start = horizontalPadding,
                    top = padding.calculateTopPadding() + 14.dp,
                    end = horizontalPadding,
                    bottom = padding.calculateBottomPadding() + 28.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EditorialDetailHeader(
                        eyebrow = "Circular matches",
                        title = "Find the right route",
                        subtitle = "Compare eligible recovery programmes for this resource.",
                        onBack = onBack,
                        profileName = user.displayName,
                    )
                }

                if (resource == null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EditorialEmptyState(
                            title = "Resource unavailable",
                            detail = "Return to the passport and choose a resource that is available in this workspace.",
                            actionLabel = "Go back",
                            onAction = onBack,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    item { MatchingResourceCard(resource = resource, eventLocation = eventLocation) }
                    item {
                        MatchingRouteSummary(
                            count = discovery.candidates.size,
                            origin = discovery.origin?.displayAddress ?: resource.geoLocation?.displayAddress ?: eventLocation,
                            onOpenMap = onOpenMap,
                        )
                    }

                    if (loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            MatchingLoadingCard()
                        }
                    }
                    error?.let { message ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                EditorialNotice(message = message, isError = true, modifier = Modifier.fillMaxWidth())
                                EditorialTextAction(label = "Try again", onClick = onRetry, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    notice?.let { message ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EditorialNotice(message = message, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    if (!loading && error == null && discovery.candidates.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EditorialEmptyState(
                                title = "No eligible route yet",
                                detail = discovery.emptyReason(),
                                icon = Icons.Outlined.Route,
                                actionLabel = "Explore programmes on the map",
                                onAction = onOpenMap,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    if (discovery.candidates.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Eligible programmes",
                                style = HomeSectionTitleStyle,
                                color = HomeInk,
                            )
                        }
                    }
                    items(discovery.candidates, key = { it.programme.id }) { candidate ->
                        MatchingCandidateCard(
                            candidate = candidate,
                            onClick = { onCandidate(candidate) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (!discovery.serverAuthoritative) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EditorialNotice(
                                message = "Cached matches are shown. Ownership, programme rules and capacity are checked again by the server before a request is accepted.",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchingResourceCard(
    resource: ResourceItem,
    eventLocation: String,
    modifier: Modifier = Modifier,
) {
    EditorialSectionCard(modifier = modifier.fillMaxWidth(), featured = true) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(shape = CircleShape, color = HomePaper) {
                    MaterialFamilyIcon(
                        family = resource.materialFamily,
                        contentDescription = null,
                        modifier = Modifier.padding(13.dp).size(31.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("RESOURCE CONSIDERED", style = HomeSupportingTextStyle, color = HomeMuted)
                    Text(
                        resource.title,
                        style = HomeCardTitleStyle.copy(fontSize = 26.sp, lineHeight = 28.sp),
                        color = HomeInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            HorizontalDivider(color = HomeLine)
            MatchingFact("Material", resource.materialLabel)
            MatchingFact("Category", resource.category.ifBlank { "Not specified" })
            MatchingFact(
                "Condition",
                resource.condition.name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase),
            )
            MatchingFact(
                "Available",
                ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit),
            )
            MatchingFact(
                "Origin",
                resource.geoLocation?.displayAddress ?: eventLocation.ifBlank { "Not specified" },
            )
        }
    }
}

@Composable
private fun MatchingRouteSummary(
    count: Int,
    origin: String,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = HomeForest,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.Explore, contentDescription = null, tint = HomeSage, modifier = Modifier.size(30.dp))
            Text(
                text = count.toString(),
                color = Color.White,
                fontFamily = com.reevent.app.ui.theme.HomeEditorialFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 46.sp,
                lineHeight = 45.sp,
            )
            Text(
                text = if (count == 1) "eligible programme" else "eligible programmes",
                style = HomeBodyStyle,
                color = Color.White,
            )
            if (origin.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = HomeSage, modifier = Modifier.size(18.dp))
                    Text(origin, style = HomeSupportingTextStyle, color = HomeSage, modifier = Modifier.weight(1f))
                }
            }
            EditorialTextAction(
                label = "View routes on map",
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Outlined.Explore,
            )
        }
    }
}

@Composable
private fun MatchingCandidateCard(
    candidate: PartnerCandidate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.testTag("matching_candidate_${candidate.programme.id}"),
        shape = RoundedCornerShape(22.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = HomeMist) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null,
                        tint = HomeForest,
                        modifier = Modifier.padding(12.dp).size(26.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        candidate.programme.name,
                        style = HomeCardTitleStyle.copy(fontSize = 25.sp),
                        color = HomeInk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        candidate.programme.type.displayLabel(),
                        style = HomeSupportingTextStyle,
                        color = HomeMuted,
                    )
                }
                candidate.score?.let { MatchingScore(score = it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MatchingPill(candidate.distanceKm?.let { "%.1f km".format(it) } ?: "Distance —")
                MatchingPill(if (candidate.programme.pickupAvailable) "Pickup" else "Drop-off")
                candidate.programme.remainingCapacity?.let {
                    MatchingPill("${it.cleanNumber()} ${candidate.programme.unit.orEmpty()} left")
                }
            }
            candidate.reasons.take(3).forEach { reason ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = HomeForest,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(reason, style = HomeSupportingTextStyle, color = HomeMuted, modifier = Modifier.weight(1f))
                }
            }
            EditorialTextAction(
                label = "View details and request recovery",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MatchingScore(score: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (score >= 75) HomeSage else HomeMist,
        border = BorderStroke(1.dp, if (score >= 75) HomeGold else HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(score.toString(), color = HomeInk, fontFamily = HomeBodyFont, fontWeight = FontWeight.Bold)
            Text("MATCH", color = HomeMuted, fontFamily = HomeBodyFont, fontSize = 9.sp, letterSpacing = .6.sp)
        }
    }
}

@Composable
private fun MatchingPill(label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = CircleShape, color = HomeMist) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = HomeSupportingTextStyle.copy(fontSize = 12.sp),
            color = HomeForest,
            maxLines = 1,
        )
    }
}

@Composable
private fun MatchingFact(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, style = HomeSupportingTextStyle, color = HomeMuted, modifier = Modifier.weight(.35f))
        Text(value, style = HomeBodyStyle, color = HomeInk, modifier = Modifier.weight(.65f))
    }
}

@Composable
private fun MatchingLoadingCard(modifier: Modifier = Modifier) {
    EditorialSectionCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = HomeForest, strokeWidth = 2.dp)
            Text("Checking programme rules and capacity…", style = HomeBodyStyle, color = HomeMuted)
        }
    }
}

private fun PartnerDiscoveryResult.emptyReason(): String =
    exclusionCounts.entries
        .sortedByDescending(Map.Entry<String, Int>::value)
        .joinToString(" · ") { (reason, count) ->
            "${reason.lowercase().replace('_', ' ')}: $count"
        }
        .ifBlank { "No active programme currently satisfies this resource's material, quantity and location rules." }

private fun Double.cleanNumber(): String =
    if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(this)

