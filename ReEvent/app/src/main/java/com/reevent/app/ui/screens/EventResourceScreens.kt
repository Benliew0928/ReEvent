package com.reevent.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.R
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.data.blocksResourceArchive
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.MaterialCatalog
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.User
import com.reevent.app.feature.events.EventFormValidation
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.components.SyncStateChip
import com.reevent.app.ui.materials.MaterialFamilyPickerField
import com.reevent.app.ui.theme.ReEventAmber
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeEditorialFont
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGreetingStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSupportingInk
import com.reevent.app.ui.theme.HomeSupportingTextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Serializable
private data class ResourceDraft(
    val resourceId: String? = null,
    val title: String = "",
    val category: String = "",
    val materialFamily: String? = null,
    val materialDetail: String = "",
    /** Read once from pre-catalogue drafts, then rewritten as family/detail. */
    val material: String? = null,
    val quantity: String = "1",
    val unit: String = "items",
    val condition: String = ResourceCondition.GOOD.name,
    val value: String = "",
    val photoUri: String? = null,
    val useEventLocation: Boolean = true,
    val locationLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

private const val DRAFT_LOADING = "__draft_loading__"
private val resourceDraftJson = Json { ignoreUnknownKeys = true }

private fun createCameraUri(context: Context): Uri? =
    runCatching {
        val directory = File(context.cacheDir, "resource-photos").apply { mkdirs() }
        val file = File.createTempFile("resource_", ".jpg", directory)
        FileProvider.getUriForFile(context, "${context.packageName}.resourcephotos", file)
    }.getOrNull()

@Composable
fun AddResourceLiveScreen(
    user: User,
    eventId: String,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    initialResource: ResourceItem? = null,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val event by viewModel.event(eventId).collectAsState(null)
    var title by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.title.orEmpty()) }
    var category by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.category ?: resourceCategories.first()) }
    var materialFamily by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.materialFamily ?: MaterialFamily.WOOD) }
    var materialDetail by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.materialDetail.orEmpty()) }
    var quantity by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.quantity?.toString() ?: "1") }
    var unit by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.unit ?: resourceUnits.first()) }
    var condition by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.condition ?: ResourceCondition.GOOD) }
    var value by rememberSaveable(initialResource?.id) {
        mutableStateOf(
            initialResource
                ?.valueCents
                ?.let {
                    "%.2f".format(
                        java.util.Locale.US,
                        it / 100.0,
                    )
                }.orEmpty(),
        )
    }
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var useEventLocation by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.geoLocation == null) }
    var resourceLocation by remember(initialResource?.id) { mutableStateOf(initialResource?.geoLocation) }
    var choosingResourceLocation by remember { mutableStateOf(false) }
    var draftResourceId by rememberSaveable(initialResource?.id, eventId) { mutableStateOf<String?>(initialResource?.id) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var photoNotice by rememberSaveable { mutableStateOf<String?>(null) }
    val storedDraft by viewModel.resourceDraft(user.id, eventId).collectAsState(DRAFT_LOADING)
    var draftRestored by rememberSaveable(initialResource?.id, eventId) { mutableStateOf(initialResource != null) }
    LaunchedEffect(storedDraft, initialResource?.id) {
        if (initialResource == null && !draftRestored && storedDraft != DRAFT_LOADING) {
            draftRestored = true
            storedDraft?.let { saved ->
                runCatching { resourceDraftJson.decodeFromString(ResourceDraft.serializer(), saved) }.getOrNull()?.let { draft ->
                    draftResourceId = draft.resourceId
                    title = draft.title
                    category = draft.category.ifBlank { resourceCategories.first() }
                    val descriptor = draft.materialFamily
                        ?.let { runCatching { MaterialFamily.valueOf(it) }.getOrNull() }
                        ?.let { family -> MaterialCatalog.descriptor(family, draft.materialDetail) }
                        ?: MaterialCatalog.resolveLegacy(draft.material.orEmpty())
                    materialFamily = descriptor.family
                    materialDetail = descriptor.detail.orEmpty()
                    quantity = draft.quantity
                    unit = draft.unit.ifBlank { resourceUnits.first() }
                    condition = runCatching { ResourceCondition.valueOf(draft.condition) }.getOrDefault(ResourceCondition.GOOD)
                    value = draft.value
                    photoUri = draft.photoUri?.let(Uri::parse)
                    useEventLocation = draft.useEventLocation
                    resourceLocation = if (draft.latitude != null && draft.longitude != null) {
                        runCatching { GeoLocation(draft.locationLabel.orEmpty(), draft.latitude, draft.longitude) }.getOrNull()
                    } else null
                    photoNotice = "Restored your unfinished draft."
                }
            }
        }
    }
    LaunchedEffect(
        title, category, materialFamily, materialDetail, quantity, unit, condition, value, photoUri, useEventLocation,
        resourceLocation, draftResourceId, draftRestored, initialResource?.id,
    ) {
        if (initialResource == null && draftRestored) {
            viewModel.saveResourceDraft(
                user.id,
                eventId,
                resourceDraftJson.encodeToString(
                    ResourceDraft.serializer(),
                    ResourceDraft(
                        resourceId = draftResourceId,
                        title = title,
                        category = category,
                        materialFamily = materialFamily.name,
                        materialDetail = materialDetail,
                        quantity = quantity,
                        unit = unit,
                        condition = condition.name,
                        value = value,
                        photoUri = photoUri?.toString(),
                        useEventLocation = useEventLocation,
                        locationLabel = resourceLocation?.displayAddress,
                        latitude = resourceLocation?.latitude,
                        longitude = resourceLocation?.longitude,
                    ),
                ),
            )
        }
    }
    val quantityValue = quantity.toIntOrNull()
    val valueCents = value.toCentsOrNull()
    val titleError = submitted && title.trim().length < 2
    val materialValidation = MaterialCatalog.validate(materialFamily, materialDetail)
    val materialError = submitted && !materialValidation.isValid
    val quantityError = submitted && (quantityValue == null || quantityValue !in 1..10_000)
    val valueError = submitted && value.isNotBlank() && valueCents == null
    val formValid =
        title.trim().length >= 2 && materialValidation.isValid && quantityValue != null && quantityValue in 1..10_000 &&
            (value.isBlank() || valueCents != null)
    val action by viewModel.action.collectAsState()
    val context = LocalContext.current
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraCapture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
            if (saved) {
                pendingCameraUri?.let { photoUri = it }
                photoNotice = if (saved) "Photo ready to upload when you save." else null
            } else {
                photoNotice = "Camera was cancelled. Your existing photo was kept."
            }
            pendingCameraUri = null
        }
    val cameraPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                createCameraUri(context)?.let { uri ->
                    pendingCameraUri = uri
                    cameraCapture.launch(uri)
                }
                    ?: run { photoNotice = "Unable to prepare the camera photo." }
            } else {
                photoNotice = "Camera permission was denied. You can still select a photo."
            }
        }
    val photoPicker =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedUri ->
            // Cancellation must never discard an already selected photo.
            selectedUri?.let {
                photoUri = it
                photoNotice = "Photo ready to upload when you save."
            }
        }
    FeatureScaffold(
        title = if (initialResource == null) "Add a resource" else "Edit resource",
        actionLabel = "Back",
        onAction = onBack,
        viewModel = viewModel,
        selected = TopLevelDestination.EVENTS,
        onNavigate = onNavigate,
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Create a traceable item for this event. It is saved locally first and syncs when connected.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (initialResource == null) {
                    Text(
                        "Draft saves automatically on this device. It remains here if an upload fails.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReEventTextSecondary,
                    )
                }
                OutlinedTextField(title, {
                    title = it
                }, Modifier.fillMaxWidth(), label = {
                    Text("Resource name *")
                }, singleLine = true, isError = titleError, supportingText = { if (titleError) Text("Enter at least 2 characters.") })
                ResourceChoiceField("Category", category, resourceCategories) { category = it }
                MaterialFamilyPickerField(materialFamily, { selected -> selected?.let { materialFamily = it } }, Modifier.fillMaxWidth())
                OutlinedTextField(
                    materialDetail,
                    { materialDetail = it.take(MaterialCatalog.MAX_DETAIL_LENGTH) },
                    Modifier.fillMaxWidth(),
                    label = { Text(if (materialFamily == MaterialFamily.MIXED_OTHER) "Specific material *" else "Specific material (optional)") },
                    singleLine = true,
                    isError = materialError,
                    supportingText = {
                        if (materialError) Text(materialValidation.detailError.orEmpty())
                        else Text("${materialDetail.length}/${MaterialCatalog.MAX_DETAIL_LENGTH}")
                    },
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        quantity,
                        { quantity = it.filter(Char::isDigit).take(5) },
                        Modifier.weight(1f),
                        label = { Text("Quantity *") },
                        singleLine = true,
                        isError = quantityError,
                        supportingText = { if (quantityError) Text("1–10,000") },
                    )
                    Box(Modifier.weight(1f)) { ResourceChoiceField("Unit", unit, resourceUnits) { unit = it } }
                }
                ResourceChoiceField(
                    "Condition",
                    condition.toDisplayLabel(),
                    ResourceCondition.entries.map(ResourceCondition::toDisplayLabel),
                ) { selected ->
                    condition = ResourceCondition.entries.first { it.toDisplayLabel() == selected }
                }
                ResourceChoiceField(
                    "Location",
                    if (useEventLocation) "Use event location" else "Use resource override",
                    listOf("Use event location", "Use resource override"),
                ) { selected -> useEventLocation = selected == "Use event location" }
                if (useEventLocation) {
                    Text(
                        event?.geoLocation?.displayAddress ?: event?.venue?.ifBlank { "Event location is not configured yet." }
                            ?: "Event location is not configured yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ReEventTextSecondary,
                    )
                } else {
                    SecondaryActionButton(
                        if (resourceLocation == null) "Choose resource location" else "Adjust resource pin",
                        { choosingResourceLocation = true },
                        Modifier.fillMaxWidth(),
                    )
                    resourceLocation?.let {
                        Text(
                            "${it.displayAddress}\n${"%.6f".format(it.latitude)}, ${"%.6f".format(it.longitude)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReEventTextSecondary,
                        )
                    }
                }
                OutlinedTextField(
                    value,
                    { typed -> if (typed.matches(Regex("^\\d{0,7}(\\.\\d{0,2})?$"))) value = typed },
                    Modifier.fillMaxWidth(),
                    label = { Text("Estimated value (RM, optional)") },
                    singleLine = true,
                    isError = valueError,
                    supportingText = { if (valueError) Text("Use a valid amount, e.g. 12.50") },
                )
                Text("Photo (optional)", style = MaterialTheme.typography.titleSmall)
                if (photoUri == null) {
                    initialResource?.imageUrls?.firstOrNull()?.let { path ->
                        StoredResourcePhoto(path, viewModel)
                        Text(
                            "Current saved photo. Choose Replace photo only if you want to upload a new one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReEventTextSecondary,
                        )
                    }
                } else {
                    LocalPhotoPreview(
                        uri = photoUri!!,
                        onRemove = { photoUri = null },
                        removeLabel = if (initialResource == null) "Remove selected photo" else "Discard replacement",
                    )
                }
                photoNotice?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary) }
                if (photoUri != null && action.loading) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(16.dp),
                        color = ReEventMintSoft,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                            Text(
                                if (initialResource == null) "Uploading photo and saving resource…" else "Uploading replacement photo…",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                if (photoUri != null && action.error != null) {
                    Text(
                        "The photo has not been uploaded. Your selection is still kept here; check your connection and press Save to retry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                SecondaryActionButton(
                    if (photoUri == null) "Select photo" else "Replace photo",
                    { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    Modifier.fillMaxWidth(),
                )
                SecondaryActionButton(
                    "Take photo with camera",
                    {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            createCameraUri(context)?.let { uri ->
                                pendingCameraUri = uri
                                cameraCapture.launch(uri)
                            }
                                ?: run { photoNotice = "Unable to prepare the camera photo." }
                        } else {
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    Modifier.fillMaxWidth(),
                )
                PrimaryActionButton(
                    if (initialResource == null) "Save resource and passport" else "Save resource",
                    saveResource@{
                        submitted = true
                        if (!formValid) return@saveResource
                        val now = System.currentTimeMillis()
                        val resourceId = initialResource?.id ?: draftResourceId ?: UUID.randomUUID().toString()
                        if (initialResource == null) draftResourceId = resourceId
                        val resource =
                            ResourceItem(
                                resourceId,
                                eventId,
                                user.id,
                                title.trim(),
                                category,
                                materialFamily,
                                materialDetail.trim().takeIf(String::isNotBlank),
                                condition,
                                checkNotNull(quantityValue).toDouble(),
                                unit,
                                initialResource?.status ?: ResourceStatus.ACTIVE,
                                valueCents ?: 0,
                                initialResource?.imageUrls.orEmpty(),
                                initialResource?.createdAt ?: now,
                                now,
                                geoLocation = if (useEventLocation) null else resourceLocation,
                            )
                        if (initialResource == null) {
                            viewModel.saveResource(resource, photoUri) {
                                viewModel.clearResourceDraft(user.id, eventId)
                                onSaved(resourceId)
                            }
                        } else if (photoUri == null) {
                            viewModel.updateResource(resource) { onSaved(resourceId) }
                        } else {
                            viewModel.updateResource(resource, photoUri) { onSaved(resourceId) }
                        }
                    },
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }
    if (choosingResourceLocation) {
        LocationPickerDialog(
            initialLocation = resourceLocation ?: event?.geoLocation,
            onDismiss = { choosingResourceLocation = false },
            onSelected = {
                resourceLocation = it
                useEventLocation = false
                choosingResourceLocation = false
            },
            search = viewModel::searchPlaces,
            reverse = viewModel::reversePlace,
        )
    }
}

@Composable
fun ResourceEditorLiveScreen(
    user: User,
    eventId: String,
    resourceId: String,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val resource by viewModel.resource(resourceId).collectAsState(null)
    if (resource == null) {
        FeatureScaffold(
            title = "Edit resource",
            actionLabel = "Back",
            onAction = onBack,
            viewModel = viewModel,
            selected = TopLevelDestination.EVENTS,
            onNavigate = onNavigate,
        ) {
            item { EmptyPanel("Resource unavailable", "This item is not available in the current workspace.") {} }
        }
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
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    LaunchedEffect(user.id) { viewModel.refresh() }
    val events by viewModel.events(user.id).collectAsState(emptyList())
    val action by viewModel.action.collectAsState()
    EventListEditorialContent(
        user = user,
        events = events,
        error = action.error,
        notice = action.notice,
        loading = action.loading,
        onCreate = onCreate,
        onOpen = { event ->
            viewModel.selectEvent(event.id)
            onOpen(event.id)
        },
        onNavigate = onNavigate,
    )
}

/**
 * The Events landing page intentionally owns only presentation. Its event stream and callbacks
 * come from [EventListLiveScreen], so the editorial redesign does not change persistence,
 * selection, navigation, or lifecycle behaviour.
 */
@Composable
internal fun EventListEditorialContent(
    user: User,
    events: List<Event>,
    error: String? = null,
    notice: String? = null,
    loading: Boolean = false,
    onCreate: () -> Unit,
    onOpen: (Event) -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
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
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("events_editorial_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = padding.calculateTopPadding() + 14.dp,
                    end = 16.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    EventEditorialHeader(
                        user = user,
                        hasEvents = events.isNotEmpty(),
                        eventCount = events.size,
                        onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
                    )
                }
                if (loading) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("events_loading"),
                            color = HomeForest,
                            trackColor = HomeSage,
                        )
                    }
                }
                error?.let { message ->
                    item { EventEditorialFeedback(message = message, isError = true) }
                }
                notice?.let { message ->
                    item { EventEditorialFeedback(message = message, isError = false) }
                }
                if (events.isEmpty()) {
                    item { EventEmptyStateCard(onCreate = onCreate) }
                    item { EventBenefitsCard() }
                } else {
                    itemsIndexed(events, key = { _, event -> event.id }) { index, event ->
                        EventEditorialCard(
                            event = event,
                            featured = index % 2 == 0,
                            onOpen = { onOpen(event) },
                        )
                    }
                    item { EventCreateButton(label = "Create a new event", onClick = onCreate, outlined = true) }
                }
            }
        }
    }
}

@Composable
private fun EventEditorialHeader(
    user: User,
    hasEvents: Boolean,
    eventCount: Int,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(HomeForest, RoundedCornerShape(4.dp)),
                )
                Text(
                    text = if (hasEvents) {
                        "$eventCount ACTIVE EVENT${if (eventCount == 1) "" else "S"}"
                    } else {
                        "EVENTS OVERVIEW"
                    },
                    style = HomeSupportingTextStyle.copy(
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        letterSpacing = 0.9.sp,
                    ),
                    color = HomeSupportingInk,
                )
            }
            Surface(
                onClick = onProfile,
                modifier = Modifier
                    .size(54.dp)
                    .testTag("events_avatar"),
                shape = RoundedCornerShape(27.dp),
                color = HomeSage,
                tonalElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.eventsInitials(),
                        color = HomeInk,
                        fontFamily = HomeEditorialFont,
                        fontSize = 24.sp,
                        modifier = Modifier.testTag("events_avatar_initials"),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (hasEvents) 112.dp else 96.dp),
        ) {
            if (hasEvents) {
                Image(
                    painter = painterResource(R.drawable.events_list_botanical_cutout),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .width(148.dp)
                        .height(86.dp),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(if (hasEvents) 0.72f else 1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = if (hasEvents) "Events" else "Your events",
                    modifier = Modifier.testTag("events_heading"),
                    style = HomeGreetingStyle.copy(fontSize = 48.sp, lineHeight = 50.sp),
                    color = HomeInk,
                )
                Text(
                    text = if (hasEvents) {
                        "A clearer way to keep every plan moving."
                    } else {
                        "Start where every good recovery plan begins."
                    },
                    style = HomeSupportingTextStyle,
                    color = HomeSupportingInk,
                )
            }
        }
    }
}

@Composable
private fun EventEmptyStateCard(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("events_empty_state"),
        shape = RoundedCornerShape(24.dp),
        color = EventEmptyCardSurface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EventEmptyIllustration()
            Text(
                text = "Nothing on the calendar yet",
                style = HomeCardTitleStyle.copy(fontSize = 30.sp, lineHeight = 34.sp),
                color = HomeInk,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Create an event to plan resources, teams and recovery from day one.",
                style = HomeSupportingTextStyle,
                color = HomeSupportingInk,
                textAlign = TextAlign.Center,
            )
            EventCreateButton(label = "Create your first event", onClick = onCreate)
        }
    }
}

@Composable
private fun EventEmptyIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.events_empty_illustration),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
            .height(142.dp),
    )
}

@Composable
private fun EventBenefitsCard(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.events_benefits_panel),
        contentDescription = "What you can do: plan resources, invite partners, track recovery",
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(547f / 189f)
            .testTag("events_benefits"),
        contentScale = ContentScale.FillWidth,
    )
}

@Composable
private fun EventEditorialCard(
    event: Event,
    featured: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = event.editorialDate()
    val cardColor = if (featured) HomeForest else HomeSage
    val contentColor = if (featured) Color.White else HomeInk
    val detailColor = if (featured) Color(0xFFE1E7E5) else HomeSupportingInk
    val lineColor = if (featured) Color.White.copy(alpha = 0.35f) else HomeLine
    Surface(
        onClick = onOpen,
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
        border = if (featured) null else BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = date.day,
                    color = contentColor,
                    fontFamily = HomeEditorialFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 45.sp,
                    lineHeight = 43.sp,
                )
                Text(
                    text = date.month,
                    color = contentColor,
                    fontFamily = HomeEditorialFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    letterSpacing = 1.sp,
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(118.dp)
                    .background(lineColor),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Text(
                    text = event.name,
                    color = contentColor,
                    fontFamily = HomeEditorialFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 29.sp,
                    lineHeight = 30.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = detailColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = event.venue.ifBlank { "Venue to be confirmed" },
                        color = detailColor,
                        style = HomeSupportingTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                EventStatusPill(
                    label = event.status.editorialStatus(),
                    featured = featured,
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Open ${event.name}",
                tint = contentColor,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun EventStatusPill(label: String, featured: Boolean) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (featured) HomeSage else HomePaper,
        border = if (featured) null else BorderStroke(1.dp, HomeForest.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(start = 11.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(HomeForest, RoundedCornerShape(4.dp)),
            )
            Text(
                text = label,
                color = HomeForest,
                fontFamily = HomeBodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.45.sp,
            )
        }
    }
}

@Composable
private fun EventCreateButton(
    label: String,
    onClick: () -> Unit,
    outlined: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .testTag("events_create"),
        shape = RoundedCornerShape(18.dp),
        color = if (outlined) HomePaper else HomeForest,
        border = BorderStroke(1.dp, HomeForest),
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "+  $label",
                color = if (outlined) HomeForest else Color.White,
                fontFamily = HomeBodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
            )
        }
    }
}

@Composable
private fun EventEditorialFeedback(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isError) Color(0xFFFFE6E8) else HomeSage,
        border = BorderStroke(1.dp, if (isError) Color(0xFFE8B8BD) else HomeLine),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = HomeBodyStyle.copy(fontSize = 14.sp, lineHeight = 19.sp),
            color = if (isError) Color(0xFF8A2836) else HomeInk,
        )
    }
}

private data class EditorialEventDate(
    val day: String,
    val month: String,
)

private fun Event.editorialDate(): EditorialEventDate {
    val date = Instant.ofEpochMilli(startsAt).atZone(ZoneId.systemDefault())
    return EditorialEventDate(
        day = EVENT_DAY_FORMAT.format(date),
        month = EVENT_MONTH_FORMAT.format(date).uppercase(Locale.US),
    )
}

private fun String.editorialStatus(): String =
    lowercase(Locale.US).replace('_', ' ').replaceFirstChar(Char::titlecase)

private fun User.eventsInitials(): String =
    displayName
        .trim()
        .split(Regex("\\s+"))
        .filter(String::isNotBlank)
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "ME" }

private val EVENT_DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd", Locale.US)
private val EVENT_MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.US)
private val EventEmptyCardSurface = Color(0xFFF2F1E8)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorLiveScreen(
    user: User,
    eventId: String?,
    onSaved: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val existing by (eventId?.let(viewModel::event) ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(null)
    var name by rememberSaveable(eventId) { mutableStateOf("") }
    var description by rememberSaveable(eventId) { mutableStateOf("") }
    var venue by rememberSaveable(eventId) { mutableStateOf("") }
    var geoLocation by remember(eventId) { mutableStateOf<GeoLocation?>(null) }
    var choosingEventLocation by remember { mutableStateOf(false) }
    var startDate by rememberSaveable(eventId) { mutableStateOf("") }
    var endDate by rememberSaveable(eventId) { mutableStateOf("") }
    var datePickerTarget by rememberSaveable(eventId) { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable(eventId) { mutableStateOf(false) }
    LaunchedEffect(existing?.id) {
        existing?.let {
            name = it.name
            description = it.description
            venue = it.venue
            geoLocation = it.geoLocation
            startDate = EventFormValidation.dateText(it.startsAt)
            endDate = EventFormValidation.dateText(it.endsAt)
        }
    }
    val validation = EventFormValidation.validate(name, venue, startDate, endDate)
    val action by viewModel.action.collectAsState()
    val editorTitle = if (eventId == null) "Create an event" else "Edit event"
    val editorSubtitle = if (eventId == null) {
        "Set the details for a more circular gathering."
    } else {
        "Keep the details for your gathering up to date."
    }
    val editorialFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = HomeForest,
        unfocusedBorderColor = HomeLine,
        focusedLabelColor = HomeForest,
        unfocusedLabelColor = HomeSupportingInk,
        cursorColor = HomeForest,
        focusedContainerColor = HomePaper,
        unfocusedContainerColor = HomePaper,
    )
    ReEventScaffold(
        selected = TopLevelDestination.EVENTS,
        onNavigate = onNavigate,
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
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopEnd,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(250.dp)
                    .height(206.dp)
                    .alpha(0.13f),
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomStart,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(230.dp)
                    .height(188.dp)
                    .alpha(0.08f),
            )
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = padding.calculateTopPadding() + 24.dp,
                    end = 20.dp,
                    bottom = padding.calculateBottomPadding() + 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        Surface(
                            onClick = onBack,
                            modifier = Modifier.size(54.dp),
                            shape = RoundedCornerShape(27.dp),
                            color = HomePaper.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, HomeForest.copy(alpha = 0.7f)),
                            tonalElevation = 0.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = HomeForest,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(
                                text = editorTitle,
                                style = HomeGreetingStyle.copy(fontSize = 50.sp, lineHeight = 52.sp),
                                color = HomeForest,
                            )
                            Text(
                                text = editorSubtitle,
                                style = HomeBodyStyle.copy(fontSize = 20.sp, lineHeight = 27.sp),
                                color = HomeSupportingInk,
                            )
                        }
                        action.error?.let { message ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFFE6E8),
                                border = BorderStroke(1.dp, Color(0xFFE8B8BD)),
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(14.dp),
                                    style = HomeSupportingTextStyle,
                                    color = Color(0xFF8A2836),
                                )
                            }
                        }
                        action.notice?.let { message ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = HomeSage,
                                border = BorderStroke(1.dp, HomeLine),
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.padding(14.dp),
                                    style = HomeSupportingTextStyle,
                                    color = HomeInk,
                                )
                            }
                        }
                        if (action.loading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = HomeForest,
                                trackColor = HomeSage,
                            )
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            color = HomeSage.copy(alpha = 0.86f),
                            border = BorderStroke(1.dp, HomeLine),
                            tonalElevation = 0.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Text(
                                    text = "EVENT DETAILS",
                                    style = HomeSupportingTextStyle.copy(
                                        fontSize = 14.sp,
                                        letterSpacing = 1.5.sp,
                                    ),
                                    color = HomeForest,
                                )
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Event name *") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = editorialFieldColors,
                                    isError = submitted && validation.nameError != null,
                                    supportingText = {
                                        if (submitted) validation.nameError?.let { message -> Text(message) }
                                    },
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(142.dp),
                                    label = { Text("Description") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = editorialFieldColors,
                                )
                                Surface(
                                    onClick = { choosingEventLocation = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = HomePaper,
                                    border = BorderStroke(1.dp, HomeLine),
                                    tonalElevation = 0.dp,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            tint = HomeForest,
                                            modifier = Modifier.size(26.dp),
                                        )
                                        Text(
                                            text = geoLocation?.displayAddress ?: "Event location *",
                                            modifier = Modifier.weight(1f),
                                            style = HomeBodyStyle,
                                            color = HomeInk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.ChevronRight,
                                            contentDescription = "Choose event location",
                                            tint = HomeForest,
                                            modifier = Modifier.size(28.dp),
                                        )
                                    }
                                }
                                geoLocation?.let {
                                    Text(
                                        text = "${it.displayAddress}\n${"%.6f".format(it.latitude)}, ${"%.6f".format(it.longitude)}",
                                        style = HomeSupportingTextStyle,
                                        color = HomeSupportingInk,
                                    )
                                }
                                if (submitted && geoLocation == null) {
                                    Text(
                                        text = "Select an exact event location.",
                                        color = MaterialTheme.colorScheme.error,
                                        style = HomeSupportingTextStyle,
                                    )
                                }
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val stackDates = maxWidth < 330.dp
                                    if (stackDates) {
                                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                            EventDatePickerField(
                                                label = "Start date *",
                                                value = startDate,
                                                error = if (submitted) validation.startDateError else null,
                                                onClick = { datePickerTarget = "start" },
                                            )
                                            EventDatePickerField(
                                                label = "End date *",
                                                value = endDate,
                                                error = if (submitted) validation.endDateError else null,
                                                onClick = { datePickerTarget = "end" },
                                            )
                                        }
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            EventDatePickerField(
                                                label = "Start date *",
                                                value = startDate,
                                                error = if (submitted) validation.startDateError else null,
                                                onClick = { datePickerTarget = "start" },
                                                modifier = Modifier.weight(1f),
                                            )
                                            EventDatePickerField(
                                                label = "End date *",
                                                value = endDate,
                                                error = if (submitted) validation.endDateError else null,
                                                onClick = { datePickerTarget = "end" },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Button(
                            onClick = saveEvent@{
                                submitted = true
                                if (!validation.isValid || geoLocation == null) return@saveEvent
                                val now = System.currentTimeMillis()
                                val start = checkNotNull(EventFormValidation.parseDate(startDate))
                                val end = checkNotNull(EventFormValidation.parseDate(endDate))
                                val event =
                                    existing?.copy(
                                        name = name.trim(),
                                        description = description.trim(),
                                        venue = venue.trim(),
                                        geoLocation = geoLocation,
                                        startsAt = EventFormValidation.startOfDayMillis(start),
                                        endsAt = EventFormValidation.endOfDayMillis(end),
                                        updatedAt = now,
                                    ) ?: Event(
                                        UUID.randomUUID().toString(),
                                        user.id,
                                        name.trim(),
                                        description.trim(),
                                        venue.trim(),
                                        EventFormValidation.startOfDayMillis(start),
                                        EventFormValidation.endOfDayMillis(end),
                                        "DRAFT",
                                        now,
                                        now,
                                        geoLocation = geoLocation,
                                    )
                                viewModel.saveEvent(event, if (existing == null) "Event created" else "Event updated") { onSaved(it.id) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(62.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HomeForest,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(
                                text = if (eventId == null) "Create event" else "Save changes",
                                style = HomeBodyStyle.copy(fontSize = 18.sp),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                modifier = Modifier.size(30.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = HomeSage,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "i",
                                        color = HomeForest,
                                        fontFamily = HomeEditorialFont,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 20.sp,
                                    )
                                }
                            }
                            Text(
                                text = "All fields marked * are required before the event can be saved.",
                                modifier = Modifier.weight(1f),
                                style = HomeSupportingTextStyle,
                                color = HomeSupportingInk,
                            )
                        }
                        if (existing != null) {
                            Text(
                                text = "Archive is available from Event details, where you can review linked resources before removing the event from active views.",
                                style = HomeSupportingTextStyle,
                                color = HomeSupportingInk,
                            )
                        }
                    }
                }
            }
        }
    }
    datePickerTarget?.let { target ->
        val selectingStartDate = target == "start"
        val currentDateText = if (selectingStartDate) startDate else endDate
        val initialSelectedDateMillis = EventFormValidation.parseDate(currentDateText)
            ?.atStartOfDay(ZoneOffset.UTC)
            ?.toInstant()
            ?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { datePickerTarget = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                            val selectedDate = Instant.ofEpochMilli(selectedDateMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .toString()
                            if (selectingStartDate) {
                                startDate = selectedDate
                            } else {
                                endDate = selectedDate
                            }
                        }
                        datePickerTarget = null
                    },
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { datePickerTarget = null }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (choosingEventLocation) {
        LocationPickerDialog(
            initialLocation = geoLocation,
            onDismiss = { choosingEventLocation = false },
            onSelected = {
                geoLocation = it
                venue = it.displayAddress
                choosingEventLocation = false
            },
            search = viewModel::searchPlaces,
            reverse = viewModel::reversePlace,
        )
    }
}

@Composable
private fun EventDatePickerField(
    label: String,
    value: String,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 4.dp),
            style = HomeSupportingTextStyle,
            color = if (error == null) HomeForest else MaterialTheme.colorScheme.error,
            maxLines = 1,
        )
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            color = HomePaper,
            border = BorderStroke(1.dp, if (error == null) HomeLine else MaterialTheme.colorScheme.error),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = value.ifBlank { "YYYY-MM-DD" },
                    modifier = Modifier.weight(1f),
                    style = HomeSupportingTextStyle.copy(fontSize = 13.sp),
                    color = if (value.isBlank()) HomeSupportingInk else HomeInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Select $label",
                    tint = HomeForest,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        error?.let { message ->
            Text(
                text = message,
                style = HomeSupportingTextStyle.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.error,
            )
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
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val event by viewModel.event(eventId).collectAsState(null)
    val resources by viewModel.resources(eventId).collectAsState(emptyList())
    val transactions by viewModel.eventTransactions(eventId).collectAsState(emptyList())
    var searchQuery by rememberSaveable(eventId) { mutableStateOf("") }
    var statusFilter by rememberSaveable(eventId) { mutableStateOf("All statuses") }
    var archiveResourceId by rememberSaveable(eventId) { mutableStateOf<String?>(null) }
    var archiveEventConfirmation by rememberSaveable(eventId) { mutableStateOf(false) }
    val visibleResources =
        resources.filter { resource ->
            val matchesSearch =
                searchQuery.trim().let { query ->
                    query.isBlank() ||
                        listOf(resource.title, resource.category, resource.materialLabel).any { it.contains(query, ignoreCase = true) }
                }
            val matchesStatus = statusFilter == "All statuses" || resource.status.toDisplayLabel() == statusFilter
            matchesSearch && matchesStatus
        }
    FeatureScaffold(
        title = event?.name ?: "Event details",
        actionLabel = "Back",
        onAction = onBack,
        viewModel = viewModel,
        selected = TopLevelDestination.EVENTS,
        onNavigate = onNavigate,
    ) {
        item {
            Surface(
                Modifier.fillMaxWidth(),
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(20.dp),
                color = ReEventSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Event workspace", style = MaterialTheme.typography.labelLarge, color = ReEventGreen)
                    Text(
                        event?.description?.ifBlank { "No description yet" } ?: "Loading event…",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(event?.venue?.ifBlank { "Venue to be confirmed" } ?: "", color = ReEventTextSecondary)
                    event?.let {
                        Text(
                            "${EventFormValidation.dateText(it.startsAt)} to ${EventFormValidation.dateText(it.endsAt)}",
                            color = ReEventTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        SyncStateChip(it.syncState)
                    }
                    PrimaryActionButton("Add resource", onAddResource, Modifier.fillMaxWidth())
                    SecondaryActionButton("Scan resource QR", onScanResourceQr, Modifier.fillMaxWidth())
                    SecondaryActionButton("Edit event details", onEditEvent, Modifier.fillMaxWidth())
                    SecondaryActionButton("Archive this event", { archiveEventConfirmation = true }, Modifier.fillMaxWidth())
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
                    singleLine = true,
                )
                ResourceChoiceField(
                    label = "Filter by status",
                    selected = statusFilter,
                    options = listOf("All statuses") + ResourceStatus.entries.map(ResourceStatus::toDisplayLabel),
                    onSelected = { statusFilter = it },
                )
                if (searchQuery.isNotBlank() || statusFilter != "All statuses") {
                    SecondaryActionButton("Clear search and filter", {
                        searchQuery = ""
                        statusFilter = "All statuses"
                    }, Modifier.fillMaxWidth())
                }
            }
        }
        if (resources.isEmpty()) {
            item { EmptyPanel("No resources yet", "Add a resource to start this event's circular recovery flow.") {} }
        } else if (visibleResources.isEmpty()) {
            item { EmptyPanel("No matching resources", "Try a different name, material, or status.") {} }
        }
        items(visibleResources, key = ResourceItem::id) { resource ->
            val blockingTransactions = transactions.filter { it.blocksResourceArchive(resource.id) }
            Surface(
                Modifier.fillMaxWidth(),
                shape =
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(20.dp),
                color = ReEventSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(resource.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${ResourcePresentationRules.quantityLabel(
                            resource.quantity,
                            resource.unit,
                        )} · ${resource.category} · ${resource.condition.toDisplayLabel()}",
                        color = ReEventTextSecondary,
                    )
                    ResourceStatusSummary(resource.status, resource.syncState)
                    resource.imageUrls.firstOrNull()?.let { StoredResourcePhoto(it, viewModel) }
                    PrimaryActionButton("Open digital passport", { onOpenPassport(resource.id) }, Modifier.fillMaxWidth())
                    SecondaryActionButton("Edit resource", { onEditResource(resource.id) }, Modifier.fillMaxWidth())
                    if (blockingTransactions.isEmpty()) {
                        SecondaryActionButton("Archive resource", { archiveResourceId = resource.id }, Modifier.fillMaxWidth())
                    } else {
                        Text(
                            "Archive unavailable: ${blockingTransactions.size} active transaction${if (blockingTransactions.size == 1) "" else "s"} must be completed, rejected, or cancelled first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReEventTextSecondary,
                        )
                    }
                }
            }
        }
    }

    val archiveCandidate = resources.firstOrNull { it.id == archiveResourceId }
    archiveCandidate?.let { resource ->
        val blockingTransactions = transactions.filter { it.blocksResourceArchive(resource.id) }
        AlertDialog(
            onDismissRequest = { archiveResourceId = null },
            title = { Text(if (blockingTransactions.isEmpty()) "Archive resource?" else "Archive unavailable") },
            text = {
                Text(
                    if (blockingTransactions.isEmpty()) {
                        "${resource.title} will be removed from the active event inventory and marketplace. Its passport history stays as an archived record."
                    } else {
                        "This resource now has ${blockingTransactions.size} active transaction${if (blockingTransactions.size == 1) "" else "s"}. Complete, reject, or cancel ${if (blockingTransactions.size == 1) "it" else "them"} before archiving."
                    },
                )
            },
            confirmButton = {
                if (blockingTransactions.isEmpty()) {
                    TextButton(onClick = {
                        archiveResourceId = null
                        viewModel.archiveResource(resource.id, transactions) { }
                    }) { Text("Archive") }
                } else {
                    TextButton(onClick = { archiveResourceId = null }) { Text("Back") }
                }
            },
            dismissButton = {
                if (blockingTransactions.isEmpty()) {
                    TextButton(onClick = { archiveResourceId = null }) { Text("Cancel") }
                }
            },
        )
    }

    if (archiveEventConfirmation) {
        AlertDialog(
            onDismissRequest = { archiveEventConfirmation = false },
            title = { Text("Archive this event?") },
            text = {
                Text(
                    "${event?.name ?: "This event"} will be removed from active organiser views. Marketplace visibility is controlled per resource, so review and archive any linked resources shown on this page before continuing. Existing passport history and completed transactions remain as records.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    archiveEventConfirmation = false
                    viewModel.archiveEvent(eventId, onArchiveEvent)
                }) { Text("Archive event") }
            },
            dismissButton = {
                TextButton(onClick = { archiveEventConfirmation = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
internal fun ResourceStatusSummary(
    status: ResourceStatus,
    syncState: SyncState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        StatusChip(status.toDisplayLabel(), status.toUiColor())
        SyncStateChip(syncState)
        Text(
            "Status updates automatically when lifecycle actions are confirmed.",
            style = MaterialTheme.typography.bodySmall,
            color = ReEventTextSecondary,
        )
    }
}

private val resourceCategories = listOf("Event signage", "Furniture", "Food service", "Textiles", "Equipment", "Other")
private val resourceUnits = listOf("items", "sets", "kg", "boxes", "metres")

@Composable
private fun ResourceChoiceField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = {
            expanded = true
        }, modifier = Modifier.fillMaxWidth()) { Text("$label: $selected", modifier = Modifier.weight(1f)) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onSelected(option)
                    expanded = false
                })
            }
        }
    }
}

@Composable
private fun LocalPhotoPreview(
    uri: Uri,
    onRemove: () -> Unit,
    removeLabel: String,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value =
            withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
            }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (bitmap !=
                null
            ) {
                androidx.compose.foundation.Image(
                    bitmap!!.asImageBitmap(),
                    "Selected resource photo",
                    Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text("Photo selected. Preview is unavailable, but the image will still be uploaded when you save.")
            }
            SecondaryActionButton(removeLabel, onRemove, Modifier.fillMaxWidth())
        }
    }
}

/** Resource photos are stored in a private bucket, so they are fetched with the signed-in user's session. */
@Composable
private fun StoredResourcePhoto(
    path: String,
    viewModel: FeatureViewModel,
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, path) {
        val bytes = viewModel.resourcePhoto(path)
        value =
            bytes?.let { imageBytes ->
                withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) }
            }
    }
    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Saved resource photo",
            modifier = Modifier.fillMaxWidth().height(180.dp),
            contentScale = ContentScale.Crop,
        )
    } else {
        Text("Loading saved photo…", style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
    }
}

private fun ResourceCondition.toDisplayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

private fun ResourceStatus.toDisplayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

private fun ResourceStatus.toUiColor() =
    when (this) {
        ResourceStatus.ACTIVE -> ReEventGreen
        ResourceStatus.RECOVERY_IN_PROGRESS -> ReEventAmber
        ResourceStatus.RECOVERED -> ReEventBlue
        ResourceStatus.ARCHIVED -> ReEventCoral
        ResourceStatus.DRAFT -> ReEventTextSecondary
    }

private fun String.toCentsOrNull(): Long? {
    val amount = trim().toBigDecimalOrNull() ?: return null
    if (amount.signum() < 0 || amount.scale() > 2) return null
    return runCatching { amount.movePointRight(2).longValueExact() }.getOrNull()
}

@Composable internal fun FeatureScaffold(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    viewModel: FeatureViewModel,
    selected: TopLevelDestination? = null,
    onNavigate: (TopLevelDestination) -> Unit = {},
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    val action by viewModel.action.collectAsState()
    ReEventScaffold(selected = selected, onNavigate = onNavigate) { innerPadding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoMark(size = 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        Text("Live workspace", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(onClick = onAction) { Text(actionLabel) }
                }
                action.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp)) }
                action.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
                if (action.loading) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable private fun EventCard(
    event: Event,
    onAddResource: (String) -> Unit,
    onImpact: (String) -> Unit,
) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(event.name, style = MaterialTheme.typography.titleLarge)
        Text(event.venue.ifBlank { "Venue to be confirmed" })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAddResource(event.id) }) { Text("Add resource") }
            OutlinedButton(onClick = { onImpact(event.id) }) { Text("Impact") }
        }
    }
}

@Composable private fun ResourceLine(
    resource: ResourceItem,
    onClick: () -> Unit,
) = Card(Modifier.fillMaxWidth()) {
    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(resource.title, style = MaterialTheme.typography.titleMedium)
            Text("${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} • ${resource.status.name.lowercase()}")
        }
        OutlinedButton(onClick = onClick) { Text("View") }
    }
}

@Composable
internal fun EmptyPanel(
    title: String,
    detail: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail)
        actionLabel?.let { label ->
            Button(onClick = onAction, modifier = Modifier.fillMaxWidth()) { Text(label) }
        }
    }
}
