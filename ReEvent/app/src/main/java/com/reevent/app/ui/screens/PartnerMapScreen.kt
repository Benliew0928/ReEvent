package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.reevent.app.R
import com.reevent.app.BuildConfig
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerOriginSource
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.core.network.MapTilerHttpConfiguration
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.materials.MaterialFamilyPickerField
import com.reevent.app.ui.theme.ReEventBackground
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeDeepForest
import com.reevent.app.ui.theme.HomeEditorialFont
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeHeroTitleStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSupportingInk
import com.reevent.app.ui.theme.HomeSupportingTextStyle
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Feature.Companion.getStringProperty
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

private val distanceChoices = listOf("5 km" to 5.0, "15 km" to 15.0, "50 km" to 50.0, "Any" to null)

private enum class MapRoleExperience { ORGANIZER, PARTICIPANT }

private fun User.mapRoleExperience(): MapRoleExperience =
    if (role == UserRole.PARTICIPANT) MapRoleExperience.PARTICIPANT else MapRoleExperience.ORGANIZER

@Composable
fun PartnerMapScreen(
    user: User,
    state: PartnerMapUiState,
    resource: ResourceItem?,
    marketplaceResources: List<ResourceItem>,
    onNavigate: (TopLevelDestination) -> Unit,
    onProfile: () -> Unit,
    onBack: (() -> Unit)?,
    onMaterialChange: (MaterialFamily?) -> Unit,
    onToggleType: (ProgrammeType) -> Unit,
    onDistanceChange: (Double?) -> Unit,
    onPickupChange: (Boolean) -> Unit,
    onNearMe: () -> Unit,
    onPresentationChange: (PartnerMapPresentation) -> Unit,
    onSelectCandidate: (PartnerCandidate?) -> Unit,
    onMapLoading: () -> Unit,
    onMapLoaded: () -> Unit,
    onMapFailed: (String?) -> Unit,
    onOpenPassport: (String) -> Unit,
    onCreateHandover: (CircularProgramme) -> Unit,
    modifier: Modifier = Modifier,
    mapContent: (@Composable (PartnerMapUiState, (PartnerCandidate) -> Unit, Modifier) -> Unit)? = null,
) {
    var confirmation by remember { mutableStateOf<PartnerCandidate?>(null) }
    var showRefineFilters by rememberSaveable { mutableStateOf(false) }
    val experience = user.mapRoleExperience()
    ReEventScaffold(
        selected = TopLevelDestination.PARTNERS,
        onNavigate = onNavigate,
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
                modifier = Modifier.fillMaxSize().alpha(0.055f),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 1040.dp)
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EditorialMapHeader(
                    user = user,
                    resource = resource,
                    experience = experience,
                    onProfile = onProfile,
                )
                if (onBack != null) TextButton(onClick = onBack) { Text("Back to matching") }
                if (experience == MapRoleExperience.ORGANIZER) {
                    OrganizerMapStatusOverview(
                        onRefineSearch = { showRefineFilters = !showRefineFilters },
                    )
                } else {
                    MapQuickFilters(
                        experience = experience,
                        state = state,
                        onToggleType = onToggleType,
                        onPickupChange = onPickupChange,
                        onRefineSearch = { showRefineFilters = !showRefineFilters },
                    )
                }
                PartnerMapFilters(
                    state = state,
                    onMaterialChange = onMaterialChange,
                    onToggleType = onToggleType,
                    onDistanceChange = onDistanceChange,
                    onPickupChange = onPickupChange,
                    onNearMe = onNearMe,
                    showProgrammeTypes = true,
                    showPickupOnly = experience == MapRoleExperience.ORGANIZER,
                    visible = showRefineFilters,
                    onDismiss = { showRefineFilters = false },
                )
                if (state.loading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = HomeForest,
                        trackColor = HomeSage,
                    )
                }
                state.error?.let { NoticePanel(it) }
                state.mapError?.let { NoticePanel(it) }
                if (!state.result.serverAuthoritative) {
                    NoticePanel("Showing cached programmes. Eligibility will be checked again when you submit a request.")
                }
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val expanded = maxWidth >= 840.dp
                    if (expanded) {
                        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            PartnerMapContent(
                                state = state,
                                onSelect = onSelectCandidate,
                                mapContent = mapContent,
                                onLoading = onMapLoading,
                                onLoaded = onMapLoaded,
                                onFailed = onMapFailed,
                                modifier = Modifier.weight(1.1f).fillMaxHeight(),
                            )
                            Column(
                                modifier = Modifier.weight(0.9f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                MapSummaryPanel(
                                    experience = experience,
                                    state = state,
                                    onSelect = onSelectCandidate,
                                )
                                PartnerCandidateList(
                                    state = state,
                                    onSelect = onSelectCandidate,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    } else {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MapPresentationSwitch(
                                state = state,
                                onPresentationChange = onPresentationChange,
                            )
                            if (state.presentation == PartnerMapPresentation.MAP && state.mapError == null) {
                                if (experience == MapRoleExperience.ORGANIZER) {
                                    Box(Modifier.weight(1f).fillMaxWidth()) {
                                        PartnerMapContent(
                                            state = state,
                                            onSelect = onSelectCandidate,
                                            mapContent = mapContent,
                                            onLoading = onMapLoading,
                                            onLoaded = onMapLoaded,
                                            onFailed = onMapFailed,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        MapSummaryPanel(
                                            experience = experience,
                                            state = state,
                                            onSelect = onSelectCandidate,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(horizontal = 4.dp, vertical = 8.dp),
                                        )
                                    }
                                } else {
                                    PartnerMapContent(
                                        state = state,
                                        onSelect = onSelectCandidate,
                                        mapContent = mapContent,
                                        onLoading = onMapLoading,
                                        onLoaded = onMapLoaded,
                                        onFailed = onMapFailed,
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                    )
                                    MapSummaryPanel(
                                        experience = experience,
                                        state = state,
                                        onSelect = onSelectCandidate,
                                    )
                                }
                            } else {
                                PartnerCandidateList(state, onSelectCandidate, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }

    state.selectedCandidate?.let { candidate ->
        PartnerCandidateDetailDialog(
            candidate = candidate,
            resource = resource,
            eligibleResources = marketplaceResources.filter { it.isEligibleFor(candidate.programme) },
            canRequest = resource?.ownerId == user.id && resource.status == ResourceStatus.ACTIVE && resource.quantity > 0,
            onDismiss = { onSelectCandidate(null) },
            onOpenPassport = onOpenPassport,
            onRequest = if (state.isResourceContext) {
                {
                    onSelectCandidate(null)
                    confirmation = candidate
                }
            } else null,
        )
    }
    val candidateToConfirm = confirmation
    if (candidateToConfirm != null && resource != null) {
        PartnerRecoveryConfirmationDialog(
            candidate = candidateToConfirm,
            resource = resource,
            onDismiss = { confirmation = null },
            onConfirm = {
                confirmation = null
                onCreateHandover(candidateToConfirm.programme)
            },
        )
    }
}

@Composable
private fun EditorialMapHeader(
    user: User,
    resource: ResourceItem?,
    experience: MapRoleExperience,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (experience) {
        MapRoleExperience.ORGANIZER -> "Keep every stop moving"
        MapRoleExperience.PARTICIPANT -> "Return it, right here"
    }
    val subtitle = when {
        resource != null -> "Find a verified route for ${resource.title}."
        experience == MapRoleExperience.ORGANIZER -> "See recovery partners across your event."
        else -> "Find a verified place for your item."
    }
    val botanicalRes = if (experience == MapRoleExperience.PARTICIPANT) {
        R.drawable.map_botanical_partner
    } else {
        R.drawable.map_botanical_organizer
    }
    val botanicalWidth = if (experience == MapRoleExperience.PARTICIPANT) 300.dp else 260.dp
    val botanicalHeight = if (experience == MapRoleExperience.PARTICIPANT) 304.dp else 270.dp
    val mapScopeLabel = when (user.role) {
        UserRole.ORGANIZER -> "Event recovery map"
        UserRole.PARTICIPANT -> "Recovery locations"
        UserRole.PARTNER -> "My recovery hub"
        null -> "Recovery locations"
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(186.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = HomeForest)
                Text(mapScopeLabel, style = HomeSupportingTextStyle, color = HomeSupportingInk)
            }
            Surface(
                onClick = onProfile,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = HomeSage,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.displayName.mapInitials(),
                        color = HomeInk,
                        fontFamily = HomeEditorialFont,
                        fontSize = 22.sp,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 67.dp)
                .fillMaxWidth(0.58f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(title, style = HomeHeroTitleStyle, color = HomeInk)
            Text(subtitle, style = HomeSupportingTextStyle, color = HomeSupportingInk)
        }
        Image(
            painter = painterResource(botanicalRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopEnd,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(-1f)
                .padding(top = 48.dp)
                .width(botanicalWidth)
                .requiredHeight(botanicalHeight),
        )
    }
}

@Composable
private fun OrganizerMapStatusOverview(
    onRefineSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            border = BorderStroke(1.dp, HomeLine),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrganizerStatusItem(
                    label = "Pending\ncollection",
                    icon = Icons.Outlined.AccessTime,
                    modifier = Modifier.weight(1f),
                )
                OrganizerStatusItem(
                    label = "In\nprogress",
                    icon = Icons.Outlined.Sync,
                    modifier = Modifier.weight(1f),
                )
                OrganizerStatusItem(
                    label = "Recovered",
                    icon = Icons.Outlined.Recycling,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        TextButton(onClick = onRefineSearch) {
            Text("More filters", color = HomeForest, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun OrganizerStatusItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(38.dp),
            shape = CircleShape,
            color = HomeDeepForest,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp), tint = Color.White)
            }
        }
        Text(
            text = label,
            color = HomeInk,
            fontFamily = HomeEditorialFont,
            fontSize = 17.sp,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun MapQuickFilters(
    experience: MapRoleExperience,
    state: PartnerMapUiState,
    onToggleType: (ProgrammeType) -> Unit,
    onPickupChange: (Boolean) -> Unit,
    onRefineSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clearTypes = {
        state.filters.programmeTypes.forEach(onToggleType)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (experience) {
            MapRoleExperience.ORGANIZER -> {
                EditorialFilterChip(
                    label = "All",
                    selected = state.filters.programmeTypes.isEmpty(),
                    onClick = clearTypes,
                )
                ProgrammeType.entries.forEach { type ->
                    EditorialFilterChip(
                        label = type.name.humanize(),
                        selected = type in state.filters.programmeTypes,
                        onClick = { onToggleType(type) },
                    )
                }
            }

            MapRoleExperience.PARTICIPANT -> {
                EditorialFilterChip(
                    label = "All",
                    selected = state.filters.programmeTypes.isEmpty() && !state.filters.pickupOnly,
                    onClick = {
                        clearTypes()
                        if (state.filters.pickupOnly) onPickupChange(false)
                    },
                )
                EditorialFilterChip(
                    label = "Pickup available",
                    selected = state.filters.pickupOnly,
                    onClick = { onPickupChange(!state.filters.pickupOnly) },
                    leadingIcon = Icons.Outlined.MyLocation,
                )
            }
        }
        EditorialFilterChip(
            label = "More filters",
            selected = false,
            onClick = onRefineSearch,
        )
    }
}

@Composable
private fun EditorialFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = if (selected) HomeForest else HomePaper,
        border = BorderStroke(1.dp, if (selected) HomeForest else HomeLine),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = if (selected) Color.White else HomeForest,
                )
            }
            Text(
                label,
                color = if (selected) Color.White else HomeForest,
                fontFamily = HomeEditorialFont,
                fontSize = 17.sp,
            )
        }
    }
}

@Composable
private fun MapPresentationSwitch(
    state: PartnerMapUiState,
    onPresentationChange: (PartnerMapPresentation) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = state.presentation == PartnerMapPresentation.MAP,
            onClick = { onPresentationChange(PartnerMapPresentation.MAP) },
            enabled = state.mapError == null && BuildConfig.MAPTILER_API_KEY.isNotBlank(),
            label = { Text("Map") },
        )
        FilterChip(
            selected = state.presentation == PartnerMapPresentation.LIST,
            onClick = { onPresentationChange(PartnerMapPresentation.LIST) },
            label = { Text("Programme list") },
        )
    }
}

@Composable
private fun MapSummaryPanel(
    experience: MapRoleExperience,
    state: PartnerMapUiState,
    onSelect: (PartnerCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val candidates = state.result.candidates
    if (candidates.isEmpty()) return
    when (experience) {
        MapRoleExperience.ORGANIZER -> OrganizerMapSummary(
            candidates = candidates.take(3),
            onSelect = onSelect,
            modifier = modifier,
        )

        MapRoleExperience.PARTICIPANT -> ParticipantMapSummary(
            candidate = state.selectedCandidate ?: candidates.first(),
            onSelect = onSelect,
            modifier = modifier,
        )
    }
}

@Composable
private fun OrganizerMapSummary(
    candidates: List<PartnerCandidate>,
    onSelect: (PartnerCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = HomeDeepForest,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("${candidates.size} active recovery stops", style = HomeCardTitleStyle, color = Color.White)
            candidates.forEach { candidate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(candidate) }
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = HomeSage) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Recycling, contentDescription = null, tint = HomeForest, modifier = Modifier.size(19.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(candidate.programme.name, color = Color.White, maxLines = 1)
                        Text(candidate.summaryLabel(), color = HomeSage, style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Open ${candidate.programme.name}", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ParticipantMapSummary(
    candidate: PartnerCandidate,
    onSelect: (PartnerCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = HomeSage,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(46.dp), shape = CircleShape, color = HomePaper) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Eco, contentDescription = null, tint = HomeForest)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text("Nearest partner", color = HomeForest, style = MaterialTheme.typography.labelMedium)
                    Text(candidate.programme.name, color = HomeInk, style = HomeCardTitleStyle)
                }
            }
            Text(candidate.summaryLabel(), color = HomeSupportingInk, style = HomeSupportingTextStyle)
            Button(
                onClick = { onSelect(candidate) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
            ) {
                Text("View partner details")
                Icon(Icons.Outlined.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun PartnerMapContent(
    state: PartnerMapUiState,
    onSelect: (PartnerCandidate) -> Unit,
    mapContent: (@Composable (PartnerMapUiState, (PartnerCandidate) -> Unit, Modifier) -> Unit)?,
    onLoading: () -> Unit,
    onLoaded: () -> Unit,
    onFailed: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (mapContent == null) {
        PartnerMapPane(state, onSelect, onLoading, onLoaded, onFailed, modifier)
    } else {
        mapContent(state, onSelect, modifier)
    }
}

@Composable
private fun PartnerMapFilters(
    state: PartnerMapUiState,
    onMaterialChange: (MaterialFamily?) -> Unit,
    onToggleType: (ProgrammeType) -> Unit,
    onDistanceChange: (Double?) -> Unit,
    onPickupChange: (Boolean) -> Unit,
    onNearMe: () -> Unit,
    showProgrammeTypes: Boolean,
    showPickupOnly: Boolean,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("More filters", color = HomeForest, fontWeight = FontWeight.SemiBold)
                    Text("Hide", color = HomeMuted)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MaterialFamilyPickerField(
                    selected = state.filters.materialFamily,
                    onSelected = onMaterialChange,
                    modifier = Modifier.weight(1f),
                    label = "Material",
                    allowAny = true,
                )
                Button(onClick = onNearMe) {
                    Text(if (state.locationPermission == PartnerLocationPermission.PERMANENTLY_DENIED) "Location settings" else "Near me")
                }
            }
            if (showProgrammeTypes) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProgrammeType.entries.forEach { type ->
                        FilterChip(
                            selected = type in state.filters.programmeTypes,
                            onClick = { onToggleType(type) },
                            label = { Text(type.name.humanize()) },
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                distanceChoices.forEach { (label, value) ->
                    FilterChip(
                        selected = state.filters.maximumDistanceKm == value,
                        onClick = { onDistanceChange(value) },
                        enabled = state.result.origin != null || value == null,
                        label = { Text(label) },
                    )
                }
                if (showPickupOnly) {
                    Row(
                        modifier = Modifier.clickable(role = Role.Checkbox) { onPickupChange(!state.filters.pickupOnly) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = state.filters.pickupOnly, onCheckedChange = onPickupChange)
                        Text("Pickup only")
                    }
                }
            }
            Text(originLabel(state.result.originSource), style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
            if (state.locationPermission == PartnerLocationPermission.DENIED) {
                Text(
                    "Approximate location permission was denied. Saved resource/event coordinates remain in use; enable permission in system settings to retry Near me.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (state.locationPermission == PartnerLocationPermission.PERMANENTLY_DENIED) {
                Text(
                    "Approximate location is disabled for ReEvent. Open system settings to enable it; saved resource/event coordinates remain in use.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun PartnerMapPane(
    state: PartnerMapUiState,
    onSelect: (PartnerCandidate) -> Unit,
    onLoading: () -> Unit,
    onLoaded: () -> Unit,
    onFailed: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = BuildConfig.MAPTILER_API_KEY
    if (key.isBlank()) {
        MapFallbackPanel(
            "Map tiles are not configured. Add MAPTILER_API_KEY to supabase.local.properties; the programme list remains available.",
            modifier,
        )
        LaunchedEffect(Unit) { onFailed("MapTiler is not configured.") }
        return
    }
    val context = LocalContext.current
    val baseStyle = remember(context, key) {
        MapTilerHttpConfiguration.ensureInitialized(context)
        BaseStyle.Uri("https://api.maptiler.com/maps/streets-v2/style.json?key=$key")
    }
    val focus = state.result.origin ?: state.result.candidates.firstOrNull()?.programme?.geoLocation
    val cameraState = rememberCameraState(
        CameraPosition(
            target = Position(focus?.longitude ?: 101.9758, focus?.latitude ?: 4.2105),
            zoom = if (focus == null) 5.0 else 11.0,
        ),
    )
    LaunchedEffect(focus?.latitude, focus?.longitude) {
        if (focus != null) {
            cameraState.position = cameraState.position.copy(
                target = Position(focus.longitude, focus.latitude),
                zoom = if (state.filters.maximumDistanceKm == null) 10.0 else 11.5,
            )
        }
    }
    val markerJson = remember(state.result.candidates) { state.result.candidates.toGeoJson() }
    val selectedJson = remember(state.selectedCandidate) {
        state.selectedCandidate?.let { listOf(it).toGeoJson() } ?: "{\"type\":\"FeatureCollection\",\"features\":[]}"
    }
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .heightIn(min = 260.dp)
            .clip(RoundedCornerShape(26.dp))
            .semantics { contentDescription = "Interactive partner programme map with ${state.result.candidates.size} results" },
    ) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = baseStyle,
            cameraState = cameraState,
            onMapLoadFailed = onFailed,
            onMapLoadFinished = onLoaded,
        ) {
            // Sources must be remembered within MaplibreMap's style composition.
            // Creating them outside it makes LocalStyleNode unavailable and crashes
            // as soon as the map page enters composition.
            val markerSource = rememberGeoJsonSource(
                GeoJsonData.JsonString(markerJson),
                GeoJsonOptions(cluster = true, clusterRadius = 42, clusterMaxZoom = 15),
            )
            val selectedSource = rememberGeoJsonSource(GeoJsonData.JsonString(selectedJson))
            CircleLayer(
                id = "partner-clusters",
                source = markerSource,
                filter = feature.has("point_count"),
                color = const(ReEventGreenDeep),
                opacity = const(0.88f),
                radius = step(feature["point_count"].asNumber(), const(17.dp), 10 to const(22.dp), 50 to const(28.dp)),
                onClick = { features ->
                    features.firstOrNull { markerSource.isCluster(it) }?.let { cluster ->
                        coroutineScope.launch {
                            cameraState.animateTo(
                                cameraState.position.copy(
                                    target = (cluster.geometry as Point).coordinates,
                                    zoom = markerSource.getClusterExpansionZoom(cluster),
                                ),
                            )
                        }
                        ClickResult.Consume
                    } ?: ClickResult.Pass
                },
            )
            SymbolLayer(
                id = "partner-cluster-counts",
                source = markerSource,
                filter = feature.has("point_count"),
                textField = feature["point_count_abbreviated"].asString(),
                textColor = const(Color.White),
            )
            CircleLayer(
                id = "partner-points",
                source = markerSource,
                filter = !feature.has("point_count"),
                color = const(ReEventGreen),
                radius = const(10.dp),
                strokeWidth = const(3.dp),
                strokeColor = const(Color.White),
                onClick = { features ->
                    val id = features.firstOrNull()?.getStringProperty("programme_id")
                    state.result.candidates.firstOrNull { it.programme.id == id }?.let(onSelect)
                    if (id == null) ClickResult.Pass else ClickResult.Consume
                },
            )
            CircleLayer(
                id = "selected-partner-point",
                source = selectedSource,
                radius = const(15.dp),
                color = const(Color.Transparent),
                strokeWidth = const(4.dp),
                strokeColor = const(ReEventInk),
            )
        }
        if (state.mapLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        Surface(
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
            color = ReEventSurface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                "© MapTiler © OpenStreetMap contributors",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
    LaunchedEffect(Unit) { onLoading() }
}

@Composable
private fun PartnerCandidateList(
    state: PartnerMapUiState,
    onSelect: (PartnerCandidate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.result.candidates.isEmpty() && !state.loading) {
            item {
                val excluded = state.result.exclusionCounts.entries.joinToString { "${it.key.humanize()}: ${it.value}" }
                MapFallbackPanel(
                    if (excluded.isBlank()) "No active partner programmes match these filters." else "No eligible programme. Excluded — $excluded",
                )
            }
        }
        items(state.result.candidates, key = { it.programme.id }) { candidate ->
            PartnerCandidateCard(
                candidate = candidate,
                selected = candidate.programme.id == state.selectedCandidate?.programme?.id,
                onClick = { onSelect(candidate) },
            )
        }
        item {
            Text(
                "Programme locations are exact partner-provided business points. No route or directions are calculated.",
                style = MaterialTheme.typography.bodySmall,
                color = ReEventTextSecondary,
            )
        }
    }
}

@Composable
private fun PartnerCandidateCard(
    candidate: PartnerCandidate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val programme = candidate.programme
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "${programme.name}, ${programme.type.name.humanize()}, ${candidate.distanceKm?.let { "%.1f kilometres".format(it) } ?: "distance unavailable"}"
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) ReEventGreenDeep else ReEventLine),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(programme.name, style = HomeCardTitleStyle, color = HomeInk)
            Text(
                candidate.summaryLabel(),
                color = HomeSupportingInk,
                style = HomeSupportingTextStyle,
            )
            Text(programme.geoLocation?.displayAddress ?: programme.location, maxLines = 2, color = HomeInk)
            if (candidate.reasons.isNotEmpty()) Text(candidate.reasons.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun PartnerCandidate.summaryLabel(): String = listOf(
    programme.type.name.humanize(),
    distanceKm?.let { "%.1f km".format(it) } ?: "distance unavailable",
    if (programme.pickupAvailable) "Pickup available" else "Drop-off point",
).joinToString(" · ")

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PartnerCandidateDetailDialog(
    candidate: PartnerCandidate,
    resource: ResourceItem?,
    eligibleResources: List<ResourceItem>,
    canRequest: Boolean,
    onDismiss: () -> Unit,
    onOpenPassport: (String) -> Unit,
    onRequest: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val programme = candidate.programme
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = HomePaper,
        contentColor = HomeInk,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(horizontal = 22.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(programme.name, style = HomeCardTitleStyle, color = HomeInk)
            Text(candidate.summaryLabel(), color = HomeSupportingInk, style = HomeSupportingTextStyle)
            Text(programme.geoLocation?.displayAddress ?: programme.location, color = ReEventTextSecondary)
            HorizontalDivider(color = HomeLine)
            AcceptedRules(programme)
            Text(if (programme.pickupAvailable) "Partner pickup is available." else "Drop-off is required.")
            if (programme.processingMethod.isNotBlank()) Text("Processing: ${programme.processingMethod}")
            if (programme.terms.isNotBlank()) Text("Terms: ${programme.terms}")
            if (resource != null) {
                HorizontalDivider(color = HomeLine)
                Text("Why this matches", style = MaterialTheme.typography.titleSmall)
                candidate.reasons.forEach { Text("• $it") }
                candidate.score?.let { Text("Server match score: $it", color = ReEventTextSecondary) }
            } else {
                HorizontalDivider(color = HomeLine)
                Text("Eligible resource Passports", style = MaterialTheme.typography.titleSmall)
                if (eligibleResources.isEmpty()) {
                    Text("No active resource currently matches the published rules.", color = ReEventTextSecondary)
                } else {
                    eligibleResources.take(5).forEach { item ->
                        SecondaryActionButton(
                            text = "View passport: ${item.title}",
                            onClick = { onOpenPassport(item.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            if (onRequest != null) {
                Button(
                    onClick = onRequest,
                    enabled = canRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
                ) {
                    Text("Request recovery")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Cancel") }
            } else {
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Close") }
            }
        }
    }
}

@Composable
fun PartnerRecoveryConfirmationDialog(
    candidate: PartnerCandidate,
    resource: ResourceItem,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text("Request recovery?") },
        text = {
            Text(
                "Send ${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} of ${resource.title} to ${candidate.programme.name}. " +
                    "The partner can accept or decline; eligibility and capacity are checked again by the server.",
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Send request") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AcceptedRules(programme: CircularProgramme, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Accepted rules", style = MaterialTheme.typography.titleSmall)
        Text("Materials: ${programme.acceptedMaterialFamilies.map(MaterialFamily::displayLabel).ifEmpty { listOf("Any") }.joinToString()}")
        Text("Categories: ${programme.acceptedCategories.ifEmpty { listOf("Any") }.joinToString()}")
        Text("Conditions: ${programme.acceptedConditions.joinToString { it.name.humanize() }}")
        val range = listOfNotNull(
            programme.minimumQuantity?.let { "minimum $it" },
            programme.maximumQuantity?.let { "maximum $it" },
        ).joinToString(", ")
        if (range.isNotBlank()) Text("Quantity: $range ${programme.unit.orEmpty()}")
        programme.remainingCapacity?.let { Text("Remaining capacity: $it ${programme.unit.orEmpty()}") }
    }
}

@Composable
private fun NoticePanel(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = ReEventMintSoft, shape = RoundedCornerShape(12.dp)) {
        Text(text, Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
    }
}

@Composable
private fun MapFallbackPanel(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = ReEventBackground, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, ReEventLine)) {
        Text(text, Modifier.padding(14.dp), color = ReEventTextSecondary)
    }
}

private fun List<PartnerCandidate>.toGeoJson(): String = FeatureCollection(
    mapNotNull { candidate ->
        candidate.programme.geoLocation?.let { location ->
            Feature(
                id = JsonPrimitive(candidate.programme.id),
                geometry = Point(Position(location.longitude, location.latitude)),
                properties = mapOf(
                    "programme_id" to JsonPrimitive(candidate.programme.id),
                    "name" to JsonPrimitive(candidate.programme.name),
                ),
            )
        }
    },
).toJson()

private fun ResourceItem.isEligibleFor(programme: CircularProgramme): Boolean =
    status == ResourceStatus.ACTIVE && quantity > 0 &&
        (programme.acceptedMaterialFamilies.isEmpty() || materialFamily in programme.acceptedMaterialFamilies) &&
        (programme.acceptedCategories.isEmpty() || programme.acceptedCategories.any { it.equals(category, true) }) &&
        condition in programme.acceptedConditions &&
        (programme.unit == null || programme.unit.equals(unit, true)) &&
        (programme.minimumQuantity == null || quantity >= programme.minimumQuantity) &&
        (programme.maximumQuantity == null || quantity <= programme.maximumQuantity) &&
        (programme.remainingCapacity == null || quantity <= programme.remainingCapacity)

private fun originLabel(source: PartnerOriginSource): String = when (source) {
    PartnerOriginSource.DEVICE -> "Distance origin: current approximate location (not saved)"
    PartnerOriginSource.RESOURCE -> "Distance origin: resource override"
    PartnerOriginSource.EVENT -> "Distance origin: event location"
    PartnerOriginSource.NONE -> "No origin is available. Choose Near me to enable distance filters."
}

private fun String.humanize(): String = lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

private fun String.mapInitials(): String =
    trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
        .take(2)
        .joinToString(separator = "") { it.first().uppercase() }
        .ifBlank { "R" }
