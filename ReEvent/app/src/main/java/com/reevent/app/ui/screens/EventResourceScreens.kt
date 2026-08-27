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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.R
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.data.blocksResourceArchive
import com.reevent.app.core.model.CircularTransaction
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
import com.reevent.app.ui.components.ProfileAvatarButton
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.components.SyncStateChip
import com.reevent.app.ui.materials.MaterialFamilyIcon
import com.reevent.app.ui.materials.MaterialFamilyPickerField
import com.reevent.app.ui.theme.ReEventAmber
import com.reevent.app.ui.theme.ReEventBackground
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
    restoreDraft: Boolean = true,
) {
    val event by viewModel.event(eventId).collectAsState(null)
    var title by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.title.orEmpty()) }
    var category by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.category ?: resourceCategories.first()) }
    var materialFamily by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.materialFamily ?: MaterialFamily.WOOD) }
    var materialDetail by rememberSaveable(initialResource?.id) { mutableStateOf(initialResource?.materialDetail.orEmpty()) }
    var quantity by rememberSaveable(initialResource?.id) {
        mutableStateOf(initialResource?.quantity?.let(ResourcePresentationRules::quantityNumber) ?: "1")
    }
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
    var draftResourceId by rememberSaveable(initialResource?.id, eventId, restoreDraft) { mutableStateOf<String?>(initialResource?.id) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var photoNotice by rememberSaveable { mutableStateOf<String?>(null) }
    val storedDraft by viewModel.resourceDraft(user.id, eventId).collectAsState(DRAFT_LOADING)
    var draftRestored by rememberSaveable(initialResource?.id, eventId, restoreDraft) {
        mutableStateOf(initialResource != null || !restoreDraft)
    }
    LaunchedEffect(storedDraft, initialResource?.id, restoreDraft) {
        if (restoreDraft && initialResource == null && !draftRestored && storedDraft != DRAFT_LOADING) {
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
    val saveResource = saveResource@{
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
    }
    ResourceEditorEditorialScaffold(
        isNewResource = initialResource == null,
        action = action,
        onBack = onBack,
        onNavigate = onNavigate,
        onSave = saveResource,
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    "Create a traceable item for this event. It is saved locally first and syncs when connected.",
                    style = HomeBodyStyle,
                    color = HomeSupportingInk,
                )
                if (initialResource == null) {
                    Text(
                        "Draft saves automatically on this device. It remains here if an upload fails.",
                        style = HomeSupportingTextStyle,
                        color = HomeSupportingInk,
                    )
                }
                ResourceEditorSection(number = "1", title = "Resource details") {
                OutlinedTextField(title, {
                    title = it
                }, Modifier.fillMaxWidth(), label = {
                    Text("Resource name *")
                }, singleLine = true, isError = titleError, supportingText = { if (titleError) Text("Enter at least 2 characters.") })
                EditorialResourceChoiceField("Category", category, resourceCategories) { category = it }
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
                }
                ResourceEditorSection(number = "2", title = "Quantity & condition") {
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
                    Box(Modifier.weight(1f)) { EditorialResourceChoiceField("Unit", unit, resourceUnits) { unit = it } }
                }
                EditorialResourceChoiceField(
                    "Condition",
                    condition.toDisplayLabel(),
                    ResourceCondition.entries.map(ResourceCondition::toDisplayLabel),
                ) { selected ->
                    condition = ResourceCondition.entries.first { it.toDisplayLabel() == selected }
                }
                }
                ResourceEditorSection(number = "3", title = "Location & photo") {
                EditorialResourceChoiceField(
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
                    ResourceEditorSecondaryButton(
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
                ResourceEditorSecondaryButton(
                    if (photoUri == null) "Select photo" else "Replace photo",
                    { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    Modifier.fillMaxWidth(),
                )
                ResourceEditorSecondaryButton(
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
                }
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
internal fun ResourceEditorEditorialScaffold(
    isNewResource: Boolean,
    action: FeatureActionState,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    onSave: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    ReEventScaffold(
        selected = TopLevelDestination.EVENTS,
        onNavigate = onNavigate,
        showBottomNavigation = false,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReEventBackground),
        ) {
            Image(
                painter = painterResource(R.drawable.home_paper_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.15f),
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopEnd,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 54.dp)
                    .width(260.dp)
                    .height(218.dp)
                    .alpha(0.42f),
            )
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .testTag("resource_editor_editorial"),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = padding.calculateTopPadding() + 118.dp,
                    end = 20.dp,
                    bottom = padding.calculateBottomPadding() + 94.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                action.error?.let { message ->
                    item { ResourceEditorFeedback(message = message, isError = true) }
                }
                action.notice?.let { message ->
                    item { ResourceEditorFeedback(message = message, isError = false) }
                }
                if (action.loading) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = HomeForest,
                            trackColor = HomeSage,
                        )
                    }
                }
                content()
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .zIndex(1f),
                color = ReEventBackground.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
            ) {
                ResourceEditorPinnedHeader(
                    isNewResource = isNewResource,
                    onBack = onBack,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        top = padding.calculateTopPadding() + 10.dp,
                        end = 20.dp,
                        bottom = 12.dp,
                    ),
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .zIndex(1f),
                color = ReEventBackground.copy(alpha = 0.94f),
                shadowElevation = 10.dp,
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            top = 12.dp,
                            end = 20.dp,
                            bottom = padding.calculateBottomPadding() + 12.dp,
                        )
                        .height(58.dp)
                        .testTag("resource_editor_save"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeForest,
                        contentColor = Color.White,
                    ),
                ) {
                    if (action.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (isNewResource) "Save resource & create passport" else "Save resource",
                            style = HomeBodyStyle.copy(
                                fontSize = 18.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceEditorPinnedHeader(
    isNewResource: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .testTag("resource_editor_back"),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, HomeForest.copy(alpha = 0.74f)),
            tonalElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = HomeForest,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isNewResource) "NEW RESOURCE" else "EDIT RESOURCE",
                    modifier = Modifier.weight(1f),
                    style = HomeSupportingTextStyle.copy(fontSize = 13.sp, letterSpacing = 1.2.sp),
                    color = HomeForest,
                )
                if (isNewResource) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = HomeSage,
                        tonalElevation = 0.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = HomeForest,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Draft saved",
                                style = HomeSupportingTextStyle.copy(fontSize = 12.sp),
                                color = HomeForest,
                            )
                        }
                    }
                }
            }
            Text(
                text = if (isNewResource) "Add a resource" else "Edit resource",
                style = HomeGreetingStyle.copy(fontSize = 36.sp, lineHeight = 38.sp),
                color = HomeInk,
            )
        }
    }
}

@Composable
private fun ResourceEditorSection(
    number: String,
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, ReEventLine),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = HomeForest,
                    tonalElevation = 0.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = number,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    }
                }
                Text(
                    text = title,
                    style = HomeCardTitleStyle.copy(fontSize = 30.sp, lineHeight = 32.sp),
                    color = HomeInk,
                )
            }
            content()
        }
    }
}

@Composable
private fun EditorialResourceChoiceField(
    label: String,
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = ReEventSurface,
            border = BorderStroke(1.dp, ReEventLine),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = label,
                        style = HomeSupportingTextStyle.copy(fontSize = 15.sp),
                        color = HomeForest,
                    )
                    Text(
                        selected,
                        style = HomeBodyStyle.copy(fontSize = 17.sp, lineHeight = 22.sp),
                        color = HomeInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "Choose $label",
                    tint = HomeForest,
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ResourceEditorSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ReEventLine),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ReEventSurface,
            contentColor = HomeForest,
        ),
    ) {
        Text(
            text = text,
            style = HomeBodyStyle.copy(
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun ResourceEditorFeedback(
    message: String,
    isError: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isError) Color(0xFFFFE6E8) else HomeSage,
        border = BorderStroke(1.dp, if (isError) Color(0xFFE8B8BD) else HomeLine),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = HomeSupportingTextStyle,
            color = if (isError) Color(0xFF8A2836) else HomeInk,
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
            ProfileAvatarButton(
                displayName = user.displayName,
                onClick = onProfile,
                modifier = Modifier.testTag("events_avatar"),
            )
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
        selected = null,
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
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .padding(
                    start = 20.dp,
                    top = padding.calculateTopPadding() + 14.dp,
                    end = 20.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Surface(
                            onClick = onBack,
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            color = HomePaper.copy(alpha = 0.94f),
                            border = BorderStroke(1.dp, HomeForest.copy(alpha = 0.7f)),
                            tonalElevation = 0.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = HomeForest,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = editorTitle,
                                style = HomeGreetingStyle.copy(fontSize = 42.sp, lineHeight = 44.sp),
                                color = HomeForest,
                            )
                            Text(
                                text = editorSubtitle,
                                style = HomeBodyStyle.copy(fontSize = 16.sp, lineHeight = 21.sp),
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
                            shape = RoundedCornerShape(22.dp),
                            color = HomeSage.copy(alpha = 0.86f),
                            border = BorderStroke(1.dp, HomeLine),
                            tonalElevation = 0.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                                        .height(104.dp),
                                    label = { Text("Description") },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = editorialFieldColors,
                                )
                                Surface(
                                    onClick = { choosingEventLocation = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = HomePaper,
                                    border = BorderStroke(1.dp, HomeLine),
                                    tonalElevation = 0.dp,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.LocationOn,
                                            contentDescription = null,
                                            tint = HomeForest,
                                            modifier = Modifier.size(22.dp),
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
                                            modifier = Modifier.size(24.dp),
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
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HomeForest,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(
                                text = if (eventId == null) "Create event" else "Save changes",
                                style = HomeBodyStyle.copy(fontSize = 16.sp),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                modifier = Modifier.size(26.dp),
                                shape = RoundedCornerShape(13.dp),
                                color = HomeSage,
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "i",
                                        color = HomeForest,
                                        fontFamily = HomeEditorialFont,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
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
    val action by viewModel.action.collectAsState()
    var archiveResourceId by rememberSaveable(eventId) { mutableStateOf<String?>(null) }
    var archiveEventConfirmation by rememberSaveable(eventId) { mutableStateOf(false) }
    EventDetailEditorialContent(
        event = event,
        resources = resources,
        transactions = transactions,
        error = action.error,
        notice = action.notice,
        loading = action.loading,
        onBack = onBack,
        onEditEvent = onEditEvent,
        onAddResource = onAddResource,
        onScanResourceQr = onScanResourceQr,
        onEditResource = onEditResource,
        onOpenPassport = onOpenPassport,
        onArchiveResource = { archiveResourceId = it },
        onArchiveEvent = { archiveEventConfirmation = true },
        onNavigate = onNavigate,
        loadPhoto = viewModel::resourcePhoto,
    )

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

private enum class EventInventoryFilter {
    ALL,
    AVAILABLE,
    LISTED,
    RECOVERY_IN_PROGRESS,
    RECOVERED,
    ARCHIVED,
}

private fun EventInventoryFilter.displayLabel(): String =
    when (this) {
        EventInventoryFilter.ALL -> "All"
        EventInventoryFilter.AVAILABLE -> "Available"
        EventInventoryFilter.LISTED -> "Listed"
        EventInventoryFilter.RECOVERY_IN_PROGRESS -> "Recovery in progress"
        EventInventoryFilter.RECOVERED -> "Recovered"
        EventInventoryFilter.ARCHIVED -> "Archived"
    }

private fun EventInventoryFilter.matches(resource: ResourceItem): Boolean =
    when (this) {
        EventInventoryFilter.ALL -> true
        EventInventoryFilter.AVAILABLE -> resource.status == ResourceStatus.ACTIVE
        EventInventoryFilter.LISTED -> resource.marketplaceListing != null
        EventInventoryFilter.RECOVERY_IN_PROGRESS -> resource.status == ResourceStatus.RECOVERY_IN_PROGRESS
        EventInventoryFilter.RECOVERED -> resource.status == ResourceStatus.RECOVERED
        EventInventoryFilter.ARCHIVED -> resource.status == ResourceStatus.ARCHIVED
    }

@Composable
internal fun EventDetailEditorialContent(
    event: Event?,
    resources: List<ResourceItem>,
    transactions: List<CircularTransaction>,
    error: String? = null,
    notice: String? = null,
    loading: Boolean = false,
    onBack: () -> Unit,
    onEditEvent: () -> Unit,
    onAddResource: () -> Unit,
    onScanResourceQr: () -> Unit,
    onEditResource: (String) -> Unit,
    onOpenPassport: (String) -> Unit,
    onArchiveResource: (String) -> Unit,
    onArchiveEvent: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    loadPhoto: suspend (String) -> ByteArray?,
    modifier: Modifier = Modifier,
) {
    val eventKey = event?.id ?: "event-detail"
    var searchQuery by rememberSaveable(eventKey) { mutableStateOf("") }
    var searchExpanded by rememberSaveable(eventKey) { mutableStateOf(false) }
    var selectedFilter by rememberSaveable(eventKey) { mutableStateOf(EventInventoryFilter.ALL) }
    var moreFiltersExpanded by rememberSaveable(eventKey) { mutableStateOf(false) }
    var expandedResourceId by rememberSaveable(eventKey) { mutableStateOf<String?>(null) }
    val visibleResources =
        resources.filter { resource ->
            val matchesSearch =
                searchQuery.trim().let { query ->
                    query.isBlank() ||
                        listOf(resource.title, resource.category, resource.materialLabel).any {
                            it.contains(query, ignoreCase = true)
                        }
                }
            matchesSearch && selectedFilter.matches(resource)
        }
    val listedCount = resources.count { it.marketplaceListing != null }
    val availableCount = resources.count { it.status == ResourceStatus.ACTIVE }

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
                    .fillMaxSize()
                    .testTag("event_detail_editorial"),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 84.dp,
                    bottom = padding.calculateBottomPadding() + 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    EventDetailHeader(
                        event = event,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                error?.let { message ->
                    item {
                        EventDetailFeedback(
                            message = message,
                            isError = true,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
                notice?.let { message ->
                    item {
                        EventDetailFeedback(
                            message = message,
                            isError = false,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
                if (loading) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .testTag("event_detail_loading"),
                            color = HomeForest,
                            trackColor = HomeSage,
                        )
                    }
                }
                item {
                    EventDetailActionBand(
                        onAddResource = onAddResource,
                        onScanResourceQr = onScanResourceQr,
                    )
                }
                item {
                    EventInventoryMetricStrip(
                        resourceCount = resources.size,
                        listedCount = listedCount,
                        availableCount = availableCount,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item {
                    EventInventoryToolbar(
                        searchQuery = searchQuery,
                        searchExpanded = searchExpanded,
                        selectedFilter = selectedFilter,
                        moreFiltersExpanded = moreFiltersExpanded,
                        onSearchExpandedChange = { searchExpanded = it },
                        onSearchQueryChange = { searchQuery = it },
                        onFilterSelected = {
                            selectedFilter = it
                            moreFiltersExpanded = false
                        },
                        onMoreFiltersExpandedChange = { moreFiltersExpanded = it },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                when {
                    resources.isEmpty() -> item {
                        EventInventoryEmptyState(
                            title = "No resources yet",
                            detail = "Add a resource to start this event's circular recovery flow.",
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    visibleResources.isEmpty() -> item {
                        EventInventoryEmptyState(
                            title = "No matching resources",
                            detail = "Try a different name or filter.",
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }

                    else -> items(visibleResources, key = ResourceItem::id) { resource ->
                        EventInventoryResourceRow(
                            resource = resource,
                            blockingTransactions = transactions.filter { it.blocksResourceArchive(resource.id) },
                            expanded = expandedResourceId == resource.id,
                            loadPhoto = loadPhoto,
                            onToggleExpanded = {
                                expandedResourceId = if (expandedResourceId == resource.id) null else resource.id
                            },
                            onOpenPassport = { onOpenPassport(resource.id) },
                            onEditResource = { onEditResource(resource.id) },
                            onArchiveResource = { onArchiveResource(resource.id) },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
                item {
                    OutlinedButton(
                        onClick = onArchiveEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .padding(horizontal = 20.dp)
                            .testTag("event_detail_archive_event"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ReEventCoral),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = HomePaper.copy(alpha = 0.9f),
                            contentColor = ReEventCoral,
                        ),
                    ) {
                        Icon(Icons.Outlined.Archive, contentDescription = null, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Archive event", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .zIndex(1f),
                color = HomeCanvas.copy(alpha = 0.60f),
                tonalElevation = 0.dp,
            ) {
                EventDetailPinnedActions(
                    onBack = onBack,
                    onEditEvent = onEditEvent,
                    modifier = Modifier.padding(
                        start = 20.dp,
                        top = padding.calculateTopPadding() + 10.dp,
                        end = 20.dp,
                        bottom = 10.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EventDetailHeader(
    event: Event?,
    modifier: Modifier = Modifier,
) {
    val eventStatus = event?.status?.uppercase()?.takeIf(String::isNotBlank) ?: "LIVE"
    val eventName = event?.name ?: "Event details"
    val venue = event?.venue?.ifBlank { "Venue to be confirmed" } ?: "Loading venue…"
    val dateRange =
        event?.let {
            "${EventFormValidation.dateText(it.startsAt)}–${EventFormValidation.dateText(it.endsAt)}"
        } ?: "Loading dates…"
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "• $eventStatus EVENT",
            style = HomeSupportingTextStyle.copy(fontSize = 13.sp, letterSpacing = 1.1.sp),
            color = HomeForest,
        )
        Text(
            text = eventName,
            style = HomeGreetingStyle.copy(fontSize = 46.sp, lineHeight = 48.sp),
            color = HomeInk,
        )
        EventMetadataLine(Icons.Outlined.LocationOn, venue, "Event location")
        EventMetadataLine(Icons.Outlined.CalendarMonth, dateRange, "Event dates")
    }
}

@Composable
private fun EventDetailPinnedActions(
    onBack: () -> Unit,
    onEditEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .testTag("event_detail_back"),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, HomeForest.copy(alpha = 0.7f)),
            tonalElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = HomeForest,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(
            onClick = onEditEvent,
            modifier = Modifier.testTag("event_detail_manage"),
        ) {
            Text("Manage event", style = HomeSupportingTextStyle.copy(fontSize = 16.sp), color = HomeForest)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = HomeForest,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun EventMetadataLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = HomeSupportingInk,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = HomeSupportingInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EventDetailActionBand(
    onAddResource: () -> Unit,
    onScanResourceQr: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = HomeForest, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onAddResource,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .testTag("event_detail_add_resource"),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("Add resource", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(
                onClick = onScanResourceQr,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("event_detail_scan_qr"),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan resource QR",
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}

@Composable
private fun EventInventoryMetricStrip(
    resourceCount: Int,
    listedCount: Int,
    availableCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EventInventoryMetric(resourceCount.toString(), "Resources", Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(64.dp).background(HomeLine))
        EventInventoryMetric(listedCount.toString(), "Listed", Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(64.dp).background(HomeLine))
        EventInventoryMetric(availableCount.toString(), "Available", Modifier.weight(1f))
    }
}

@Composable
private fun EventInventoryMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = HomeGreetingStyle.copy(fontSize = 34.sp, lineHeight = 36.sp),
            color = HomeInk,
        )
        Text(text = label, style = HomeSupportingTextStyle, color = HomeSupportingInk)
    }
}

@Composable
private fun EventInventoryToolbar(
    searchQuery: String,
    searchExpanded: Boolean,
    selectedFilter: EventInventoryFilter,
    moreFiltersExpanded: Boolean,
    onSearchExpandedChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (EventInventoryFilter) -> Unit,
    onMoreFiltersExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Resource inventory",
                modifier = Modifier.weight(1f),
                style = HomeCardTitleStyle.copy(fontSize = 31.sp, lineHeight = 34.sp),
                color = HomeInk,
            )
            IconButton(
                onClick = { onSearchExpandedChange(!searchExpanded) },
                modifier = Modifier.testTag("event_detail_search"),
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "Search resources", tint = HomeInk)
            }
            Box {
                IconButton(
                    onClick = { onMoreFiltersExpandedChange(!moreFiltersExpanded) },
                    modifier = Modifier.testTag("event_detail_more_filters"),
                ) {
                    Icon(
                        Icons.Outlined.FilterList,
                        contentDescription = "Filter by status",
                        tint = if (selectedFilter in EventInventoryFilter.entries.drop(3)) HomeForest else HomeSupportingInk,
                    )
                }
                DropdownMenu(
                    expanded = moreFiltersExpanded,
                    onDismissRequest = { onMoreFiltersExpandedChange(false) },
                ) {
                    EventInventoryFilter.entries.drop(3).forEach { filter ->
                        DropdownMenuItem(
                            text = { Text(filter.displayLabel()) },
                            onClick = { onFilterSelected(filter) },
                        )
                    }
                }
            }
        }
        AnimatedVisibility(searchExpanded) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_detail_search_field"),
                label = { Text("Search resources") },
                placeholder = { Text("Name, category or material") },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onSearchQueryChange("")
                            onSearchExpandedChange(false)
                        },
                    ) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Close search")
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HomeForest,
                    unfocusedBorderColor = HomeLine,
                    focusedLabelColor = HomeForest,
                    cursorColor = HomeForest,
                    focusedContainerColor = HomePaper.copy(alpha = 0.92f),
                    unfocusedContainerColor = HomePaper.copy(alpha = 0.92f),
                ),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(EventInventoryFilter.ALL, EventInventoryFilter.AVAILABLE, EventInventoryFilter.LISTED).forEach { filter ->
                EventInventoryFilterChip(
                    label = filter.displayLabel(),
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    modifier = Modifier.testTag("event_detail_filter_${filter.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
private fun EventInventoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) HomeForest else HomeSage.copy(alpha = 0.7f),
        border = if (selected) null else BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = HomeSupportingTextStyle.copy(fontSize = 15.sp),
            color = if (selected) Color.White else HomeForest,
        )
    }
}

@Composable
private fun EventInventoryEmptyState(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HomePaper.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = HomeCardTitleStyle, color = HomeInk)
            Text(detail, style = HomeSupportingTextStyle, color = HomeSupportingInk)
        }
    }
}

@Composable
private fun EventInventoryResourceRow(
    resource: ResourceItem,
    blockingTransactions: List<CircularTransaction>,
    expanded: Boolean,
    loadPhoto: suspend (String) -> ByteArray?,
    onToggleExpanded: () -> Unit,
    onOpenPassport: () -> Unit,
    onEditResource: () -> Unit,
    onArchiveResource: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().testTag("event_resource_${resource.id}")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EventResourceThumbnail(resource = resource, loadPhoto = loadPhoto)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = HomeInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${ResourcePresentationRules.quantityLabel(resource.quantity, resource.unit)} · ${resource.materialLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HomeSupportingInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(resource.inventoryStatusLabel(), resource.inventoryStatusColor())
            IconButton(
                onClick = onToggleExpanded,
                modifier = Modifier.testTag("event_resource_expand_${resource.id}"),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = if (expanded) "Close ${resource.title} actions" else "Open ${resource.title} actions",
                    tint = HomeInk,
                )
            }
        }
        AnimatedVisibility(expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 82.dp, top = 4.dp, bottom = 12.dp)
                    .testTag("event_resource_actions_${resource.id}"),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventResourceActionButton("Passport", onOpenPassport, Modifier.weight(1f))
                    EventResourceActionButton("Edit", onEditResource, Modifier.weight(1f))
                    EventResourceActionButton(
                        label = "Archive",
                        onClick = onArchiveResource,
                        modifier = Modifier.weight(1f),
                        enabled = blockingTransactions.isEmpty(),
                        danger = true,
                    )
                }
                if (blockingTransactions.isNotEmpty()) {
                    Text(
                        text = "Archive unavailable until ${blockingTransactions.size} active transaction${if (blockingTransactions.size == 1) "" else "s"} is resolved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = HomeSupportingInk,
                    )
                }
            }
        }
        HorizontalDivider(color = HomeLine)
    }
}

@Composable
private fun EventResourceActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (danger) ReEventCoral else HomeLine),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = HomePaper.copy(alpha = 0.92f),
            contentColor = if (danger) ReEventCoral else HomeForest,
            disabledContentColor = HomeSupportingInk,
        ),
        contentPadding = PaddingValues(horizontal = 6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@Composable
private fun EventResourceThumbnail(
    resource: ResourceItem,
    loadPhoto: suspend (String) -> ByteArray?,
    modifier: Modifier = Modifier,
) {
    val photoPath = resource.imageUrls.firstOrNull()
    val bitmap by produceState<Bitmap?>(initialValue = null, photoPath) {
        value =
            photoPath?.let { path ->
                loadPhoto(path)?.let { bytes ->
                    withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                }
            }
    }
    Surface(
        modifier = modifier
            .size(68.dp)
            .testTag("event_resource_thumbnail_${resource.id}"),
        shape = RoundedCornerShape(12.dp),
        color = HomeSage.copy(alpha = 0.76f),
        tonalElevation = 0.dp,
    ) {
        if (bitmap == null) {
            Box(contentAlignment = Alignment.Center) {
                MaterialFamilyIcon(
                    family = resource.materialFamily,
                    modifier = Modifier.size(38.dp),
                    tint = HomeForest,
                    contentDescription = "${resource.materialLabel} material icon",
                )
            }
        } else {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "${resource.title} photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

private fun ResourceItem.inventoryStatusLabel(): String =
    when {
        marketplaceListing != null -> "Listed"
        status == ResourceStatus.ACTIVE -> "Available"
        else -> status.toDisplayLabel()
    }

private fun ResourceItem.inventoryStatusColor(): Color =
    when {
        marketplaceListing != null -> HomeForest
        status == ResourceStatus.ACTIVE -> HomeForest
        else -> status.toUiColor()
    }

@Composable
private fun EventDetailFeedback(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isError) Color(0xFFFFE6E8) else HomeSage,
        border = BorderStroke(1.dp, if (isError) Color(0xFFE8B8BD) else HomeLine),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = HomeSupportingTextStyle,
            color = if (isError) Color(0xFF8A2836) else HomeInk,
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
