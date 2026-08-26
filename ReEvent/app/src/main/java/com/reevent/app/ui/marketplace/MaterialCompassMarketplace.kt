package com.reevent.app.ui.marketplace

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reevent.app.R
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.LocalResourcePhotoLoader
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.materials.MaterialFamilyIcon
import com.reevent.app.ui.materials.MaterialFamilyPickerField
import com.reevent.app.ui.screens.TransactionCard
import com.reevent.app.ui.screens.availableMarketplaceTypes
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeDeepForest
import com.reevent.app.ui.theme.HomeEditorialFont
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGold
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val compassPages = MaterialFamily.entries.chunked(4)

@Composable
fun MaterialCompassMarketplaceScreen(
    user: User,
    state: MarketplaceUiState,
    onQuery: (String) -> Unit,
    onFamily: (MaterialFamily?) -> Unit,
    onAction: (MarketplaceActionFilter) -> Unit,
    onCompassPage: (Int) -> Unit,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    onListing: (ResourceItem) -> Unit,
    onRequest: (ResourceItem, TransactionType) -> Unit,
    onPassport: (String) -> Unit,
    onPublish: (ResourceItem) -> Unit,
    onApprove: (CircularTransaction) -> Unit,
    onCancel: (CircularTransaction) -> Unit,
    onComplete: (CircularTransaction) -> Unit,
    onInTransit: (CircularTransaction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var activityExpanded by rememberSaveable { mutableStateOf(false) }
    ReEventScaffold(selected = TopLevelDestination.MARKETPLACE, onNavigate = onNavigate, modifier = modifier) { padding ->
        Box(Modifier.fillMaxSize().background(HomeCanvas)) {
            Image(
                painterResource(R.drawable.home_paper_texture), null, Modifier.fillMaxSize().alpha(.055f), contentScale = ContentScale.Crop,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 16.dp, 16.dp, padding.calculateBottomPadding() + 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    MarketplaceHeader(
                        user = user,
                        searchExpanded = searchExpanded,
                        query = state.filters.query,
                        onToggleSearch = { searchExpanded = !searchExpanded },
                        onQuery = onQuery,
                        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
                    )
                }
                if (state.isRefreshing) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = HomeForest, trackColor = HomeSage) }
                state.refreshError?.let { message ->
                    item {
                        Surface(shape = RoundedCornerShape(14.dp), color = HomeMist, border = BorderStroke(1.dp, HomeLine)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(message, Modifier.weight(1f), color = HomeMuted)
                                TextButton(onClick = onRefresh) { Text("Retry") }
                            }
                        }
                    }
                }
                item {
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        if (maxWidth >= 840.dp) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                                MaterialCompass(state.filters, onFamily, onCompassPage, Modifier.weight(.92f))
                                Column(Modifier.weight(1.08f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    MarketplaceControls(state, onAction, onFamily, onClearFilters)
                                    ResultHeading(state)
                                    ResourceRow(state.resources, user, onListing, onRequest, onPassport)
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                MaterialCompass(state.filters, onFamily, onCompassPage)
                                MarketplaceControls(state, onAction, onFamily, onClearFilters)
                                ResultHeading(state)
                                ResourceRow(state.resources, user, onListing, onRequest, onPassport)
                            }
                        }
                    }
                }
                if (user.role == UserRole.ORGANIZER) {
                    item { OrganizerPublishSection(state.publishableResources, onPublish, onNavigate) }
                }
                item {
                    MarketplaceActivity(
                        state, user, activityExpanded, { activityExpanded = !activityExpanded },
                        onApprove, onCancel, onComplete, onInTransit, onPassport,
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplaceHeader(
    user: User,
    searchExpanded: Boolean,
    query: String,
    onToggleSearch: () -> Unit,
    onQuery: (String) -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Find by material", Modifier.weight(1f), fontFamily = HomeEditorialFont, fontSize = 38.sp, lineHeight = 40.sp, color = HomeInk)
            Surface(Modifier.size(54.dp).clickable(onClick = onToggleSearch), RoundedCornerShape(16.dp), HomePaper, border = BorderStroke(1.dp, HomeLine)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Search, "Search marketplace", tint = HomeForest) }
            }
            Surface(Modifier.size(54.dp).clickable(onClick = onProfile), CircleShape, HomeSage) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.displayName.initials(), fontFamily = HomeEditorialFont, fontSize = 22.sp, color = HomeForest)
                }
            }
        }
        AnimatedVisibility(searchExpanded) {
            OutlinedTextField(
                query, onQuery, Modifier.fillMaxWidth(), label = { Text("Search resources") },
                placeholder = { Text("Title, category, family, or detail") }, singleLine = true,
            )
        }
    }
}

@Composable
private fun MaterialCompass(
    filters: MarketplaceFilters,
    onFamily: (MaterialFamily?) -> Unit,
    onPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val largeFont = LocalDensity.current.fontScale >= 1.4f
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = HomeMist.copy(alpha = .92f), border = BorderStroke(1.dp, HomeLine)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (largeFont) {
                MaterialFamilyPickerField(filters.materialFamily, onFamily, Modifier.fillMaxWidth(), "Browse all materials", allowAny = true)
            } else {
                var dragDistance by remember(filters.compassPage) { mutableFloatStateOf(0f) }
                CompassOrbit(
                    filters,
                    onFamily,
                    Modifier.fillMaxWidth().widthIn(max = 390.dp).pointerInput(filters.compassPage) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragDistance = 0f },
                            onDragEnd = {
                                if (dragDistance < -80f) onPage((filters.compassPage + 1).coerceAtMost(compassPages.lastIndex))
                                if (dragDistance > 80f) onPage((filters.compassPage - 1).coerceAtLeast(0))
                            },
                        ) { _, amount -> dragDistance += amount }
                    },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton({ onPage((filters.compassPage - 1).coerceAtLeast(0)) }, enabled = filters.compassPage > 0) {
                    Icon(Icons.Outlined.ChevronLeft, "Previous material page")
                }
                compassPages.indices.forEach { index ->
                    Surface(Modifier.size(if (index == filters.compassPage) 10.dp else 7.dp), CircleShape, if (index == filters.compassPage) HomeGold else HomeLine) {}
                }
                IconButton({ onPage((filters.compassPage + 1).coerceAtMost(compassPages.lastIndex)) }, enabled = filters.compassPage < compassPages.lastIndex) {
                    Icon(Icons.Outlined.ChevronRight, "Next material page")
                }
            }
            Text("Choose a material to explore what can circulate next.", fontFamily = HomeBodyFont, color = HomeMuted)
        }
    }
}

@Composable
private fun CompassOrbit(filters: MarketplaceFilters, onFamily: (MaterialFamily?) -> Unit, modifier: Modifier = Modifier) {
    val page = compassPages[filters.compassPage]
    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center; val radius = size.minDimension * .37f
            drawCircle(HomeLine, radius, c, style = Stroke(2f))
            drawCircle(HomeLine.copy(.7f), radius * .72f, c, style = Stroke(2f))
            drawLine(HomeLine, Offset(c.x, c.y - radius), Offset(c.x, c.y + radius), 1.5f)
            drawLine(HomeLine, Offset(c.x - radius, c.y), Offset(c.x + radius, c.y), 1.5f)
        }
        Surface(
            Modifier.size(128.dp).clickable { onFamily(null) }.semantics { role = Role.Button; contentDescription = "All resources" },
            CircleShape,
            color = HomeForest,
            border = BorderStroke(if (filters.materialFamily == null) 4.dp else 1.dp, if (filters.materialFamily == null) HomeGold else HomeDeepForest),
        ) {
            Box(contentAlignment = Alignment.Center) { Text("All\nresources", fontFamily = HomeEditorialFont, fontSize = 25.sp, lineHeight = 26.sp, color = Color.White) }
        }
        page.getOrNull(0)?.let { CompassNode(it, filters.materialFamily == it, { onFamily(it) }, Modifier.align(Alignment.TopCenter)) }
        page.getOrNull(1)?.let { CompassNode(it, filters.materialFamily == it, { onFamily(it) }, Modifier.align(Alignment.CenterEnd)) }
        page.getOrNull(2)?.let { CompassNode(it, filters.materialFamily == it, { onFamily(it) }, Modifier.align(Alignment.BottomCenter)) }
        page.getOrNull(3)?.let { CompassNode(it, filters.materialFamily == it, { onFamily(it) }, Modifier.align(Alignment.CenterStart)) }
    }
}

@Composable
private fun CompassNode(family: MaterialFamily, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(92.dp).clickable(onClick = onClick).semantics { role = Role.Button }, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(Modifier.size(64.dp), CircleShape, if (selected) HomeGold else HomePaper, border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) HomeGold else HomeLine)) {
            Box(contentAlignment = Alignment.Center) { MaterialFamilyIcon(family, Modifier.size(32.dp), if (selected) Color.White else HomeForest) }
        }
        Text(family.displayLabel, fontFamily = HomeEditorialFont, fontSize = 17.sp, color = HomeInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MarketplaceControls(
    state: MarketplaceUiState,
    onAction: (MarketplaceActionFilter) -> Unit,
    onFamily: (MaterialFamily?) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MarketplaceActionFilter.entries.forEach { action ->
                val selected = state.filters.action == action
                Surface(
                    Modifier.height(52.dp).clickable { onAction(action) }, RoundedCornerShape(28.dp),
                    if (selected) HomeForest else HomePaper,
                    border = BorderStroke(1.dp, if (selected) HomeForest else HomeLine),
                ) {
                    Row(Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(action.label, color = if (selected) Color.White else HomeInk, fontFamily = HomeBodyFont, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        MaterialFamilyPickerField(state.filters.materialFamily, onFamily, label = "All materials", allowAny = true)
        if (state.filters.query.isNotBlank() || state.filters.materialFamily != null || state.filters.action != MarketplaceActionFilter.ALL) {
            TextButton(onClick = onClear) { Text("Clear search and filters", color = HomeForest) }
        }
    }
}

@Composable
private fun ResultHeading(state: MarketplaceUiState, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(
            state.filters.materialFamily?.let { "Available in ${it.displayLabel.lowercase()}" } ?: "Available resources",
            Modifier.weight(1f), fontFamily = HomeEditorialFont, fontSize = 30.sp, lineHeight = 32.sp, color = HomeInk,
        )
        Text("${state.resultCount} ${if (state.resultCount == 1) "resource" else "resources"}", color = HomeMuted)
    }
}

@Composable
private fun ResourceRow(
    resources: List<MarketplaceResourceUi>,
    user: User,
    onListing: (ResourceItem) -> Unit,
    onRequest: (ResourceItem, TransactionType) -> Unit,
    onPassport: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (resources.isEmpty()) {
        Surface(modifier.fillMaxWidth(), RoundedCornerShape(18.dp), HomePaper, border = BorderStroke(1.dp, HomeLine)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nothing at this compass point", fontFamily = HomeEditorialFont, fontSize = 24.sp, color = HomeInk)
                Text("Try another family, action, or search phrase.", color = HomeMuted)
            }
        }
    } else {
        LazyRow(modifier, contentPadding = PaddingValues(end = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(resources, key = { it.resource.id }) { item -> MarketplacePhotoCard(item, user, onListing, onRequest, onPassport) }
        }
    }
}

@Composable
private fun MarketplacePhotoCard(
    item: MarketplaceResourceUi,
    user: User,
    onListing: (ResourceItem) -> Unit,
    onRequest: (ResourceItem, TransactionType) -> Unit,
    onPassport: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resource = item.resource
    Surface(modifier.width(286.dp).height(340.dp).clickable { onListing(resource) }, RoundedCornerShape(22.dp), HomePaper, border = BorderStroke(1.dp, HomeLine)) {
        Box(Modifier.fillMaxSize()) {
            MarketplacePhoto(resource, Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(HomePaper, HomePaper.copy(.91f), Color.Transparent))))
            Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(resource.title, Modifier.width(170.dp), fontFamily = HomeEditorialFont, fontSize = 27.sp, lineHeight = 29.sp, color = HomeInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} · ${resource.condition.name.lowercase().replace('_', ' ')}", color = HomeMuted)
                item.programmeFitLabel?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                if (item.isOwner) {
                    Surface(shape = RoundedCornerShape(20.dp), color = HomeSage) { Text("Your listing", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = HomeForest) }
                } else {
                    resource.availableMarketplaceTypes().forEach { type ->
                        AssistChip(onClick = { onRequest(resource, type) }, label = { Text(type.displayLabel()) })
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onPassport(resource.id) }) { Text("View passport", color = HomeInk) }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.ChevronRight, null, tint = HomeForest)
                }
            }
        }
    }
}

@Composable
private fun MarketplacePhoto(resource: ResourceItem, modifier: Modifier = Modifier) {
    val path = resource.imageUrls.firstOrNull()
    val loader = LocalResourcePhotoLoader.current
    val bitmap by produceState<Bitmap?>(null, path) {
        value = path?.let { loader(it) }?.let { bytes -> withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) } }
    }
    if (bitmap == null) {
        Box(modifier.background(Brush.linearGradient(listOf(HomeSage, HomeMist))), contentAlignment = Alignment.CenterEnd) {
            Surface(Modifier.padding(28.dp).size(116.dp), CircleShape, HomePaper.copy(.76f)) {
                Box(contentAlignment = Alignment.Center) { MaterialFamilyIcon(resource.materialFamily, Modifier.size(68.dp), HomeForest, resource.materialLabel) }
            }
        }
    } else {
        Image(bitmap!!.asImageBitmap(), resource.title, modifier, contentScale = ContentScale.Crop)
    }
}

@Composable
private fun OrganizerPublishSection(
    resources: List<ResourceItem>,
    onPublish: (ResourceItem) -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxWidth(), RoundedCornerShape(20.dp), HomeMist, border = BorderStroke(1.dp, HomeLine)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Ready to list", Modifier.weight(1f), fontFamily = HomeEditorialFont, fontSize = 26.sp, color = HomeInk)
                TextButton(onClick = { onNavigate(TopLevelDestination.EVENTS) }) { Text("List resource") }
            }
            if (resources.isEmpty()) Text("Add and sync an event resource, or all eligible resources are already listed.", color = HomeMuted)
            else resources.take(3).forEach { resource ->
                Surface(Modifier.fillMaxWidth().clickable { onPublish(resource) }, RoundedCornerShape(14.dp), HomePaper) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        MaterialFamilyIcon(resource.materialFamily, Modifier.size(30.dp))
                        Text(resource.title, Modifier.padding(start = 10.dp).weight(1f), color = HomeInk)
                        Text("Publish", color = HomeForest)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceActivity(
    state: MarketplaceUiState,
    user: User,
    expanded: Boolean,
    onToggle: () -> Unit,
    onApprove: (CircularTransaction) -> Unit,
    onCancel: (CircularTransaction) -> Unit,
    onComplete: (CircularTransaction) -> Unit,
    onInTransit: (CircularTransaction) -> Unit,
    onPassport: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier.fillMaxWidth().animateContentSize(), RoundedCornerShape(22.dp), HomeMist, border = BorderStroke(1.dp, HomeLine)) {
        Column {
            Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(46.dp), CircleShape, HomeSage) { Box(contentAlignment = Alignment.Center) { MaterialFamilyIcon(MaterialFamily.ORGANIC, Modifier.size(25.dp), contentDescription = null) } }
                Text("${state.activityTitle} · ${state.transactions.size} active", Modifier.padding(start = 12.dp).weight(1f), fontFamily = HomeEditorialFont, fontSize = 23.sp, color = HomeInk)
                Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, if (expanded) "Collapse activity" else "Expand activity")
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.transactions.isEmpty()) Text("No active requests or handovers.", Modifier.padding(8.dp), color = HomeMuted)
                    state.transactions.forEach { transaction ->
                        TransactionCard(
                            user, transaction, state.transactionResources[transaction.id],
                            state.syncCommands.firstOrNull { it.transactionId == transaction.id },
                            { onApprove(transaction) }, { onCancel(transaction) }, { onComplete(transaction) },
                            { onInTransit(transaction) }, { onPassport(transaction.resourceId) },
                        )
                    }
                }
            }
        }
    }
}

private fun String.initials(): String = trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "R" }
private fun TransactionType.displayLabel(): String = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
