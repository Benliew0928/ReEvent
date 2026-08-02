package com.reevent.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.data.ProgrammeMatcher
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.theme.ReEventBackground
import com.reevent.app.ui.theme.ReEventAmber
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventMuted
import com.reevent.app.ui.theme.ReEventPaper
import java.util.UUID
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ResourceDraft(
    val title: String = "",
    val category: String = "",
    val material: String = "",
    val quantity: String = "1",
    val unit: String = "items",
    val condition: String = ResourceCondition.GOOD.name,
    val value: String = "",
    val photoUri: String? = null
)

private const val DRAFT_LOADING = "__draft_loading__"
private val resourceDraftJson = Json { ignoreUnknownKeys = true }

private fun createCameraUri(context: Context): Uri? = runCatching {
    val directory = File(context.cacheDir, "resource-photos").apply { mkdirs() }
    val file = File.createTempFile("resource_", ".jpg", directory)
    FileProvider.getUriForFile(context, "${context.packageName}.resourcephotos", file)
}.getOrNull()

@Composable
fun OrganizerHomeLiveScreen(
    user: User,
    onAddResource: (String) -> Unit,
    onPassport: (String) -> Unit,
    onImpact: (String) -> Unit,
    onMarketplace: () -> Unit,
    onPartnerMap: () -> Unit,
    onProfile: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    FeatureScaffold("Organiser workspace", "Account", onProfile, viewModel) {
        if (events.isEmpty()) {
            item {
                EmptyPanel(
                    title = "Create your first event",
                    detail = "Events organise the resources, handovers and impact you track.",
                    actionLabel = "Create event"
                ) {
                    viewModel.createEvent(user) { onAddResource(it.id) }
                }
            }
        } else {
            items(events, key = Event::id) { event ->
                val resources by viewModel.resources(event.id).collectAsState(emptyList())
                val impacts by viewModel.impact(event.id).collectAsState(emptyList())
                OrganizerOverview(
                    event = event,
                    resources = resources,
                    impacts = impacts,
                    onAddResource = { onAddResource(event.id) },
                    onPassport = onPassport,
                    onImpact = { onImpact(event.id) },
                    onMarketplace = onMarketplace,
                    onPartnerMap = onPartnerMap
                )
            }
        }
    }
}

@Composable
private fun OrganizerOverview(
    event: Event,
    resources: List<ResourceItem>,
    impacts: List<ImpactRecord>,
    onAddResource: () -> Unit,
    onPassport: (String) -> Unit,
    onImpact: () -> Unit,
    onMarketplace: () -> Unit,
    onPartnerMap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(event.name, style = MaterialTheme.typography.headlineSmall)
                Text(event.venue.ifBlank { "Venue to be confirmed" }, style = MaterialTheme.typography.bodyMedium)
                Text("Live circular recovery board", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardMetric("${resources.size}", "Resource lots", Modifier.weight(1f))
            DashboardMetric("${resources.count { it.status == ResourceStatus.AVAILABLE }}", "Available", Modifier.weight(1f))
            DashboardMetric("${impacts.sumOf { it.materialDivertedKg }.formatDashboard()}", "Kg diverted", Modifier.weight(1f))
        }
        Text("Fast actions", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onAddResource, modifier = Modifier.weight(1f)) { Text("Add resource") }
            OutlinedButton(onClick = onPartnerMap, modifier = Modifier.weight(1f)) { Text("Partner map") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onImpact, modifier = Modifier.weight(1f)) { Text("View impact") }
            OutlinedButton(onClick = onMarketplace, modifier = Modifier.weight(1f)) { Text("Marketplace") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Circular workflow", style = MaterialTheme.typography.titleMedium)
                WorkflowRow("Listed", resources.size)
                WorkflowRow("Available for matching", resources.count { it.status == ResourceStatus.AVAILABLE })
                WorkflowRow("Recovered or handed over", resources.count { it.status == ResourceStatus.RECOVERED || it.status == ResourceStatus.HANDED_OVER })
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Recent resource lots", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text("${resources.size} total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        if (resources.isEmpty()) {
            EmptyPanel("No resource lots yet", "Add a traceable resource to start the event recovery flow.", actionLabel = "Add resource", onAction = onAddResource)
        } else {
            resources.take(3).forEach { resource -> ResourceLine(resource) { onPassport(resource.id) } }
        }
    }
}

@Composable
private fun DashboardMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(value, style = MaterialTheme.typography.titleLarge); Text(label, style = MaterialTheme.typography.labelMedium) } }
}

@Composable
private fun WorkflowRow(label: String, count: Int) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, modifier = Modifier.weight(1f)); Text(count.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
}

private fun Double.formatDashboard(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

@Composable
fun AddResourceLiveScreen(
    user: User,
    eventId: String,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    initialResource: ResourceItem? = null,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    var title by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.title.orEmpty()) }
    var category by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.category ?: resourceCategories.first()) }
    var material by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.material.orEmpty()) }
    var quantity by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.quantity?.toString() ?: "1") }
    var unit by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.unit ?: resourceUnits.first()) }
    var condition by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.condition ?: ResourceCondition.GOOD) }
    var value by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.valueCents?.let { "%.2f".format(java.util.Locale.US, it / 100.0) }.orEmpty()) }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var photoNotice by rememberSaveable { mutableStateOf<String?>(null) }
    val storedDraft by viewModel.resourceDraft(user.id, eventId).collectAsState(DRAFT_LOADING)
    var draftRestored by rememberSaveable(initialResource?.id, eventId) { mutableStateOf(initialResource != null) }
    LaunchedEffect(storedDraft, initialResource?.id) {
        if (initialResource == null && !draftRestored && storedDraft != DRAFT_LOADING) {
            draftRestored = true
            storedDraft?.let { saved ->
                runCatching { resourceDraftJson.decodeFromString(ResourceDraft.serializer(), saved) }.getOrNull()?.let { draft ->
                    title = draft.title
                    category = draft.category.ifBlank { resourceCategories.first() }
                    material = draft.material
                    quantity = draft.quantity
                    unit = draft.unit.ifBlank { resourceUnits.first() }
                    condition = runCatching { ResourceCondition.valueOf(draft.condition) }.getOrDefault(ResourceCondition.GOOD)
                    value = draft.value
                    photoUri = draft.photoUri?.let(Uri::parse)
                    photoNotice = "Restored your unfinished draft."
                }
            }
        }
    }
    LaunchedEffect(title, category, material, quantity, unit, condition, value, photoUri, draftRestored, initialResource?.id) {
        if (initialResource == null && draftRestored) {
            viewModel.saveResourceDraft(
                user.id,
                eventId,
                resourceDraftJson.encodeToString(ResourceDraft.serializer(), ResourceDraft(title, category, material, quantity, unit, condition.name, value, photoUri?.toString()))
            )
        }
    }
    val quantityValue = quantity.toIntOrNull()
    val valueCents = value.toCentsOrNull()
    val titleError = submitted && title.trim().length < 2
    val materialError = submitted && material.trim().length < 2
    val quantityError = submitted && (quantityValue == null || quantityValue !in 1..10_000)
    val valueError = submitted && value.isNotBlank() && valueCents == null
    val formValid = title.trim().length >= 2 && material.trim().length >= 2 && quantityValue != null && quantityValue in 1..10_000 && (value.isBlank() || valueCents != null)
    val context = LocalContext.current
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraCapture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            pendingCameraUri?.let { photoUri = it }
            photoNotice = if (saved) "Photo ready to upload when you save." else null
        } else {
            photoNotice = "Camera was cancelled. Your existing photo was kept."
        }
        pendingCameraUri = null
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            createCameraUri(context)?.let { uri -> pendingCameraUri = uri; cameraCapture.launch(uri) }
                ?: run { photoNotice = "Unable to prepare the camera photo." }
        } else {
            photoNotice = "Camera permission was denied. You can still select a photo."
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedUri ->
        // Cancellation must never discard an already selected photo.
        selectedUri?.let { photoUri = it; photoNotice = "Photo ready to upload when you save." }
    }
    FeatureScaffold(
        title = if (initialResource == null) "Add a resource" else "Edit resource",
        actionLabel = "Back",
        onAction = onBack,
        viewModel = viewModel,
        selected = ReEventScreen.AddResource,
        onNavigate = onNavigate
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Create a traceable item for this event. It is saved locally first and syncs when connected.", style = MaterialTheme.typography.bodyMedium)
            if (initialResource == null) {
                Text(
                    "Draft saves automatically on this device. It remains here if an upload fails.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReEventMuted
                )
            }
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Resource name *") }, singleLine = true, isError = titleError, supportingText = { if (titleError) Text("Enter at least 2 characters.") })
            ResourceChoiceField("Category", category, resourceCategories) { category = it }
            OutlinedTextField(material, { material = it }, Modifier.fillMaxWidth(), label = { Text("Material *") }, singleLine = true, isError = materialError, supportingText = { if (materialError) Text("Enter at least 2 characters.") })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    quantity,
                    { quantity = it.filter(Char::isDigit).take(5) },
                    Modifier.weight(1f),
                    label = { Text("Quantity *") },
                    singleLine = true,
                    isError = quantityError,
                    supportingText = { if (quantityError) Text("1–10,000") }
                )
                Box(Modifier.weight(1f)) { ResourceChoiceField("Unit", unit, resourceUnits) { unit = it } }
            }
            ResourceChoiceField("Condition", condition.toDisplayLabel(), ResourceCondition.entries.map(ResourceCondition::toDisplayLabel)) { selected ->
                condition = ResourceCondition.entries.first { it.toDisplayLabel() == selected }
            }
            OutlinedTextField(
                value,
                { typed -> if (typed.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?$"))) value = typed },
                Modifier.fillMaxWidth(),
                label = { Text("Estimated value (RM, optional)") },
                singleLine = true,
                isError = valueError,
                supportingText = { if (valueError) Text("Use a valid amount, e.g. 12.50") }
            )
            Text("Photo (optional)", style = MaterialTheme.typography.titleSmall)
            photoUri?.let { LocalPhotoPreview(it, onRemove = { photoUri = null }) }
            photoNotice?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ReEventMuted) }
            SecondaryActionButton(
                if (photoUri == null) "Select photo" else "Replace photo",
                { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                Modifier.fillMaxWidth()
            )
            SecondaryActionButton(
                "Take photo with camera",
                {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        createCameraUri(context)?.let { uri -> pendingCameraUri = uri; cameraCapture.launch(uri) }
                            ?: run { photoNotice = "Unable to prepare the camera photo." }
                    } else {
                        cameraPermission.launch(Manifest.permission.CAMERA)
                    }
                },
                Modifier.fillMaxWidth()
            )
            PrimaryActionButton(
                if (initialResource == null) "Save resource and passport" else "Save resource",
                saveResource@{
                    submitted = true
                    if (!formValid) return@saveResource
                    val now = System.currentTimeMillis(); val resourceId = initialResource?.id ?: UUID.randomUUID().toString()
                    val resource = ResourceItem(resourceId, eventId, user.id, title.trim(), category, material.trim(), condition,
                        quantityValue!!, unit, initialResource?.status ?: ResourceStatus.AVAILABLE, valueCents ?: 0, initialResource?.imageUrls.orEmpty(), initialResource?.createdAt ?: now, now)
                    if (initialResource == null) {
                        val passport = viewModel.createPassport(resourceId, user.id, resource.status, now)
                        viewModel.saveResource(resource, passport, photoUri) { viewModel.clearResourceDraft(user.id, eventId); onSaved(resourceId) }
                    } else if (photoUri == null) {
                        viewModel.updateResource(resource) { onSaved(resourceId) }
                    } else {
                        viewModel.updateResource(resource, photoUri) { onSaved(resourceId) }
                    }
                },
                Modifier.fillMaxWidth()
            )
            }
        }
    }
}

@Composable
fun ResourceEditorLiveScreen(
    user: User,
    eventId: String,
    resourceId: String,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    if (resource == null) {
        FeatureScaffold("Edit resource", "Back", onBack, viewModel) { item { EmptyPanel("Resource unavailable", "This item is not available in the current workspace.") {} } }
    } else {
        AddResourceLiveScreen(user, eventId, { onSaved() }, onBack, onNavigate, resource, viewModel)
    }
}

@Composable
fun EventListLiveScreen(
    user: User,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    FeatureScaffold("Your events", "Back", onBack, viewModel) {
        item {
            Surface(color = ReEventMintSoft, shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Plan every resource before teardown", style = MaterialTheme.typography.titleMedium)
                    Text("Choose an event workspace to manage its inventory and recovery work.", style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
                }
            }
        }
        item { PrimaryActionButton("Create event", onCreate, Modifier.fillMaxWidth()) }
        if (events.isEmpty()) item { EmptyPanel("No events yet", "Create an event before adding resources or tracking recovery.") {} }
        items(events, key = Event::id) { event ->
            Surface(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), color = ReEventPaper, border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(event.name, style = MaterialTheme.typography.titleLarge)
                    Text(event.venue.ifBlank { "Venue to be confirmed" }, color = ReEventMuted)
                    StatusChip(event.status.lowercase().replaceFirstChar(Char::titlecase), ReEventGreen)
                    Text(event.description.ifBlank { "Add a description to help your team prepare." }, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
                    PrimaryActionButton("Open event", { viewModel.selectEvent(event.id); onOpen(event.id) }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun EventEditorLiveScreen(
    user: User,
    eventId: String?,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val existing by (eventId?.let(viewModel::event) ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(null)
    var name by rememberSaveable(eventId) { mutableStateOf("") }
    var description by rememberSaveable(eventId) { mutableStateOf("") }
    var venue by rememberSaveable(eventId) { mutableStateOf("") }
    var submitted by rememberSaveable(eventId) { mutableStateOf(false) }
    LaunchedEffect(existing?.id) {
        existing?.let { name = it.name; description = it.description; venue = it.venue }
    }
    val valid = name.trim().length >= 2
    FeatureScaffold(if (eventId == null) "Create event" else "Edit event", "Back", onBack, viewModel) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Event name *") }, isError = submitted && !valid, supportingText = { if (submitted && !valid) Text("Enter at least 2 characters.") })
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Description") })
            OutlinedTextField(venue, { venue = it }, Modifier.fillMaxWidth(), label = { Text("Venue") }, singleLine = true)
            PrimaryActionButton(if (eventId == null) "Create event" else "Save changes", saveEvent@{
                submitted = true
                if (!valid) return@saveEvent
                val now = System.currentTimeMillis()
                val event = existing?.copy(name = name.trim(), description = description.trim(), venue = venue.trim(), updatedAt = now)
                    ?: Event(UUID.randomUUID().toString(), user.id, name.trim(), description.trim(), venue.trim(), now, now + 86_400_000L, "ACTIVE", now, now)
                viewModel.saveEvent(event, if (existing == null) "Event created" else "Event updated") { onSaved(it.id) }
            }, Modifier.fillMaxWidth())
            if (existing != null) {
                SecondaryActionButton("Archive event", { viewModel.archiveEvent(existing!!.id, onBack) }, Modifier.fillMaxWidth())
            }
            }
        }
    }
}

@Composable
fun EventDetailLiveScreen(
    eventId: String,
    onEditEvent: () -> Unit,
    onAddResource: () -> Unit,
    onScanResourceQr: () -> Unit,
    onEditResource: (String) -> Unit,
    onOpenPassport: (String) -> Unit,
    onArchiveEvent: () -> Unit,
    onBack: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val event by viewModel.event(eventId).collectAsState(null)
    val resources by viewModel.resources(eventId).collectAsState(emptyList())
    var searchQuery by rememberSaveable(eventId) { mutableStateOf("") }
    var statusFilter by rememberSaveable(eventId) { mutableStateOf("All statuses") }
    val visibleResources = resources.filter { resource ->
        val matchesSearch = searchQuery.trim().let { query ->
            query.isBlank() || listOf(resource.title, resource.category, resource.material).any { it.contains(query, ignoreCase = true) }
        }
        val matchesStatus = statusFilter == "All statuses" || resource.status.toDisplayLabel() == statusFilter
        matchesSearch && matchesStatus
    }
    FeatureScaffold(event?.name ?: "Event details", "Back", onBack, viewModel) {
        item {
            Surface(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), color = ReEventPaper, border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Event workspace", style = MaterialTheme.typography.labelLarge, color = ReEventGreen)
                    Text(event?.description?.ifBlank { "No description yet" } ?: "Loading event…", style = MaterialTheme.typography.titleMedium)
                    Text(event?.venue?.ifBlank { "Venue to be confirmed" } ?: "", color = ReEventMuted)
                    PrimaryActionButton("Add resource", onAddResource, Modifier.fillMaxWidth())
                    SecondaryActionButton("Scan resource QR", onScanResourceQr, Modifier.fillMaxWidth())
                    SecondaryActionButton("Edit event details", onEditEvent, Modifier.fillMaxWidth())
                    SecondaryActionButton("Archive this event", { viewModel.archiveEvent(eventId, onArchiveEvent) }, Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search resources") },
                    placeholder = { Text("Name, category or material") },
                    singleLine = true
                )
                ResourceChoiceField(
                    label = "Filter by status",
                    selected = statusFilter,
                    options = listOf("All statuses") + ResourceStatus.entries.map(ResourceStatus::toDisplayLabel),
                    onSelected = { statusFilter = it }
                )
                if (searchQuery.isNotBlank() || statusFilter != "All statuses") {
                    SecondaryActionButton("Clear search and filter", { searchQuery = ""; statusFilter = "All statuses" }, Modifier.fillMaxWidth())
                }
            }
        }
        if (resources.isEmpty()) item { EmptyPanel("No resources yet", "Add a resource to start this event's circular recovery flow.") {} }
        else if (visibleResources.isEmpty()) item { EmptyPanel("No matching resources", "Try a different name, material, or status.") {} }
        items(visibleResources, key = ResourceItem::id) { resource ->
            Surface(Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp), color = ReEventPaper, border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(resource.title, style = MaterialTheme.typography.titleMedium)
                    Text("${resource.quantity} ${resource.unit} · ${resource.category} · ${resource.condition.toDisplayLabel()}", color = ReEventMuted)
                    StatusChip(resource.status.toDisplayLabel(), resource.status.toUiColor())
                    resource.imageUrls.firstOrNull()?.let { StoredResourcePhoto(it, viewModel) }
                    ResourceChoiceField("Update status", resource.status.toDisplayLabel(), ResourceStatus.entries.map(ResourceStatus::toDisplayLabel)) { selected ->
                        val status = ResourceStatus.entries.first { it.toDisplayLabel() == selected }
                        viewModel.updateResourceStatus(resource, status)
                    }
                    PrimaryActionButton("Open digital passport", { onOpenPassport(resource.id) }, Modifier.fillMaxWidth())
                    SecondaryActionButton("Edit resource", { onEditResource(resource.id) }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private val resourceCategories = listOf("Event signage", "Furniture", "Food service", "Textiles", "Equipment", "Other")
private val resourceUnits = listOf("items", "sets", "kg", "boxes", "metres")

@Composable
private fun ResourceChoiceField(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text("$label: $selected", modifier = Modifier.weight(1f)) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelected(option); expanded = false }) }
        }
    }
}

@Composable
private fun LocalPhotoPreview(uri: Uri, onRemove: () -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (bitmap != null) androidx.compose.foundation.Image(bitmap!!.asImageBitmap(), "Selected resource photo", Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
            else Text("Photo selected. Preview is unavailable, but the image will still be uploaded when you save.")
            SecondaryActionButton("Remove photo", onRemove, Modifier.fillMaxWidth())
        }
    }
}

/** Resource photos are stored in a private bucket, so they are fetched with the signed-in user's session. */
@Composable
private fun StoredResourcePhoto(path: String, viewModel: FeatureViewModel) {
    val bitmap by produceState<Bitmap?>(initialValue = null, path) {
        val bytes = viewModel.resourcePhoto(path)
        value = bytes?.let { imageBytes ->
            withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) }
        }
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Saved resource photo",
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        Text("Loading saved photo…", style = MaterialTheme.typography.bodySmall, color = ReEventMuted)
    }
}

private fun ResourceCondition.toDisplayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun ResourceStatus.toDisplayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)
private fun ResourceStatus.toUiColor() = when (this) {
    ResourceStatus.AVAILABLE -> ReEventGreen
    ResourceStatus.RESERVED -> ReEventAmber
    ResourceStatus.HANDED_OVER, ResourceStatus.RECOVERED -> ReEventBlue
    ResourceStatus.ARCHIVED -> ReEventCoral
    ResourceStatus.DRAFT -> ReEventMuted
}

private fun String.toCentsOrNull(): Long? {
    val amount = trim().toBigDecimalOrNull() ?: return null
    if (amount.signum() < 0 || amount.scale() > 2) return null
    return runCatching { amount.movePointRight(2).longValueExact() }.getOrNull()
}

@Composable
fun PassportLiveScreen(resourceId: String, onMatch: (String) -> Unit, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val passport by viewModel.passport(resourceId).collectAsState(null)
    FeatureScaffold("Digital passport", "Back", onBack, viewModel) {
        item {
            if (resource == null) EmptyPanel("Resource unavailable", "It may not be accessible for this workspace.") {}
            else {
                Text(resource!!.title, style = MaterialTheme.typography.headlineMedium)
                Text("${resource!!.quantity} ${resource!!.unit} • ${resource!!.material}")
                HorizontalDivider()
                Text("Passport code", fontWeight = FontWeight.Bold)
                Text(passport?.qrPayload ?: "Passport is syncing")
                Button(onClick = { onMatch(resourceId) }, modifier = Modifier.fillMaxWidth()) { Text("Find circular partners") }
            }
        }
    }
}

@Composable
fun MatchingLiveScreen(user: User, resourceId: String, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    val programmes by viewModel.programmes().collectAsState(emptyList())
    val matches = resource?.let { ProgrammeMatcher.rank(it, programmes) }.orEmpty()
    FeatureScaffold("Circular matches", "Back", onBack, viewModel) {
        if (matches.isEmpty()) item { EmptyPanel("No eligible partner yet", "Add a partner programme or refresh when you are online.") {} }
        items(matches, key = { it.id }) { programme ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(programme.name, style = MaterialTheme.typography.titleMedium)
                    Text(programme.location.ifBlank { "Location to be confirmed" })
                    Text("Accepts: ${programme.acceptedMaterials.ifEmpty { listOf("all materials") }.joinToString()}")
                    Text("Reason: material compatibility and active partner programme.")
                    Button(
                        onClick = { resource?.let { viewModel.createPartnerHandover(user, it, programme) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = resource != null && resource!!.ownerId == user.id && resource!!.status == ResourceStatus.AVAILABLE
                    ) {
                        Text("Create partner handover")
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceLiveScreen(user: User, onPassport: (String) -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val resources by viewModel.marketplace().collectAsState(emptyList())
    FeatureScaffold("Marketplace", "Account", onProfile, viewModel) {
        if (resources.isEmpty()) item { EmptyPanel("No public resources yet", "Resources marked available by organisers will appear here.") {} }
        items(resources, key = { it.id }) { ResourceLine(it) { onPassport(it.id) } }
    }
}

@Composable
fun ParticipantReturnLiveScreen(user: User, onMarketplace: () -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    val resources by viewModel.marketplace().collectAsState(emptyList())
    FeatureScaffold("Participant returns", "Account", onProfile, viewModel) {
        item { Button(onClick = onMarketplace, modifier = Modifier.fillMaxWidth()) { Text("Browse available resources") } }
        if (resources.isNotEmpty()) item { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Quick return", style = MaterialTheme.typography.titleMedium); Text(resources.first().title); Button(onClick = { viewModel.createReturn(user, resources.first()) }) { Text("Create return request") } } } }
        if (transactions.isEmpty()) item { EmptyPanel("No return activity", "Your requests will appear here with their handover status.") {} }
        items(transactions, key = { it.id }) { transaction -> Card(Modifier.fillMaxWidth()) { Text("${transaction.type.name.lowercase().replaceFirstChar(Char::titlecase)} • ${transaction.status.name}", Modifier.padding(16.dp)) } }
    }
}

@Composable
fun PartnerWorkbenchLiveScreen(user: User, onMap: () -> Unit, onProfile: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val programmes by viewModel.programmes(user.id).collectAsState(emptyList())
    val transactions by viewModel.transactions(user.id).collectAsState(emptyList())
    FeatureScaffold("Partner workbench", "Account", onProfile, viewModel) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { viewModel.createProgramme(user) }, modifier = Modifier.weight(1f)) { Text("Add programme") }; OutlinedButton(onClick = onMap, modifier = Modifier.weight(1f)) { Text("View matches") } } }
        if (programmes.isEmpty()) item { EmptyPanel("No programmes", "Create a programme so organisers can find your circular services.") {} }
        items(programmes, key = { it.id }) { programme -> Card(Modifier.fillMaxWidth()) { Text(programme.name, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium) } }
        if (transactions.isNotEmpty()) item { Text("Assigned handovers", style = MaterialTheme.typography.titleLarge) }
        items(transactions, key = { it.id }) { transaction -> Card(Modifier.fillMaxWidth()) { Text("${transaction.status.name} • ${transaction.quantity} item(s)", Modifier.padding(16.dp)) } }
    }
}

@Composable
fun PartnerMapLiveScreen(onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val programmes by viewModel.programmes().collectAsState(emptyList())
    FeatureScaffold("Partner network", "Back", onBack, viewModel) {
        if (programmes.isEmpty()) item { EmptyPanel("No active partners", "Partner programmes become visible once they are active.") {} }
        items(programmes, key = { it.id }) { programme -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(programme.name, style = MaterialTheme.typography.titleMedium); Text(programme.location.ifBlank { "Location pending" }) } } }
    }
}

@Composable
fun ImpactLiveScreen(eventId: String, onBack: () -> Unit, viewModel: FeatureViewModel = hiltViewModel()) {
    val records by viewModel.impact(eventId).collectAsState(emptyList())
    FeatureScaffold("Event impact", "Back", onBack, viewModel) {
        if (records.isEmpty()) item { EmptyPanel("No impact recorded", "Impact is added when a handover or recovery is completed.") {} }
        items(records, key = { it.id }) { record -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("${record.materialDivertedKg} kg diverted", style = MaterialTheme.typography.titleMedium); Text("${record.emissionsAvoidedKg} kg emissions avoided") } } }
    }
}

@Composable private fun FeatureScaffold(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    viewModel: FeatureViewModel,
    selected: ReEventScreen? = null,
    onNavigate: (ReEventScreen) -> Unit = {},
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val action by viewModel.action.collectAsState()
    ReEventScaffold(selected = selected, onNavigate = onNavigate) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) { LogoMark(size = 42.dp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineMedium); Text("Live workspace", style = MaterialTheme.typography.labelLarge) }; OutlinedButton(onClick = onAction) { Text(actionLabel) } }
                action.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
                action.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
                if (action.loading) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable private fun EventCard(event: Event, onAddResource: (String) -> Unit, onImpact: (String) -> Unit) = Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(event.name, style = MaterialTheme.typography.titleLarge); Text(event.venue.ifBlank { "Venue to be confirmed" }); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { onAddResource(event.id) }) { Text("Add resource") }; OutlinedButton(onClick = { onImpact(event.id) }) { Text("Impact") } } } }
@Composable private fun ResourceLine(resource: ResourceItem, onClick: () -> Unit) = Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(resource.title, style = MaterialTheme.typography.titleMedium); Text("${resource.quantity} ${resource.unit} • ${resource.status.name.lowercase()}") }; OutlinedButton(onClick = onClick) { Text("View") } } }
@Composable
private fun EmptyPanel(
    title: String,
    detail: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail)
        actionLabel?.let { label ->
            Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
    }
}
