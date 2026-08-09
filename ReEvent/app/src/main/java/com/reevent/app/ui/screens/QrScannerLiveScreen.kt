package com.reevent.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.theme.ReEventBackground
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventMuted
import com.reevent.app.ui.theme.ReEventPaper
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.flowOf

/**
 * CameraX provides the local preview while the bundled ML Kit model decodes only QR codes. A QR
 * can resolve while offline when its resource/passport has already been synchronised locally.
 */
@Composable
fun QrScannerLiveScreen(
    user: User,
    onOpenPassport: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scannerActive by rememberSaveable { mutableStateOf(false) }
    var rawPayload by rememberSaveable { mutableStateOf<String?>(null) }
    var resolvedResourceId by rememberSaveable { mutableStateOf<String?>(null) }
    var scanError by rememberSaveable { mutableStateOf<String?>(null) }
    var scannerSession by rememberSaveable { mutableIntStateOf(0) }
    val action by viewModel.action.collectAsState()
    val resource by (resolvedResourceId?.let(viewModel::resource) ?: flowOf(null)).collectAsState(null)
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        scannerActive = granted
        if (!granted) scanError = "Camera permission was not granted. Allow it to scan a resource QR code."
    }

    LaunchedEffect(Unit) { viewModel.refresh() }
    LaunchedEffect(rawPayload) {
        val payload = rawPayload ?: return@LaunchedEffect
        val resourceId = viewModel.resolvePassportPayload(payload)
        if (resourceId != null) {
            resolvedResourceId = resourceId
            scanError = null
            viewModel.recordPassportScan(user, resourceId)
        } else {
            scanError = if (payload.startsWith("reevent://passport/")) {
                "This passport is not available on this device. Connect to the internet, wait for refresh, then scan again."
            } else {
                "This is not a valid ReEvent resource QR code."
            }
            rawPayload = null
        }
    }

    fun startScanning() {
        scanError = null
        if (hasCameraPermission) scannerActive = true else cameraPermission.launch(Manifest.permission.CAMERA)
    }
    fun resetScanner() {
        rawPayload = null
        resolvedResourceId = null
        scanError = null
        scannerActive = true
        scannerSession += 1
    }

    if (scannerActive && hasCameraPermission && rawPayload == null && resolvedResourceId == null) {
        FullScreenQrScanner(
            key = scannerSession,
            onPayload = { payload -> rawPayload = payload },
            onCameraError = { message -> scannerActive = false; scanError = message },
            onCancel = onBack
        )
    } else {
    Surface(modifier = Modifier.fillMaxSize(), color = ReEventBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LogoMark(Modifier.size(42.dp))
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text("Scan resource QR", style = MaterialTheme.typography.headlineSmall)
                    Text("Verify a ReEvent passport", style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
                }
                SecondaryActionButton("Back", onBack)
            }

            when {
                resolvedResourceId != null && resource != null -> ScannedResourcePanel(
                    resource = resource!!,
                    allowedActions = user.scannerActionsFor(resource!!),
                    loading = action.loading,
                    actionNotice = action.notice,
                    actionError = action.error,
                    onAction = { lifecycleAction -> viewModel.applyLifecycleAction(user, resource!!, lifecycleAction) },
                    onOpenPassport = { onOpenPassport(resolvedResourceId!!) },
                    onScanAgain = ::resetScanner
                )

                resolvedResourceId != null -> ScannerNotice("Opening the saved passport…")

                rawPayload != null -> ScannerNotice("Verifying scanned QR code…")

                else -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ReEventMintSoft,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Camera scan", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "QR recognition works without internet after the app is installed. A passport must have been saved or refreshed on this device before it can open offline.",
                                color = ReEventMuted
                            )
                        }
                    }
                    PrimaryActionButton("Start QR scanner", ::startScanning, Modifier.fillMaxWidth())
                }
            }

            scanError?.let { ScannerErrorPanel(it, onRetry = ::startScanning) }
        }
    }
    }
}

@Composable
private fun FullScreenQrScanner(
    key: Int,
    onPayload: (String) -> Unit,
    onCameraError: (String) -> Unit,
    onCancel: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = ReEventBackground) {
        Box(Modifier.fillMaxSize()) {
            CameraQrPreview(
                key = key,
                onPayload = onPayload,
                onCameraError = onCameraError,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ReEventPaper,
                border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogoMark(Modifier.size(34.dp))
                    Text(
                        "Scan resource QR",
                        modifier = Modifier.padding(start = 8.dp).weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    SecondaryActionButton("Cancel", onCancel)
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                shape = RoundedCornerShape(16.dp),
                color = ReEventPaper,
                border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)
            ) {
                Text(
                    "Point the camera at a ReEvent resource QR code.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                    color = ReEventMuted
                )
            }
        }
    }
}

@Composable
private fun ScannerNotice(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ReEventMintSoft,
        border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)
    ) {
        Text(message, modifier = Modifier.padding(20.dp), color = ReEventMuted)
    }
}

@Composable
private fun ScannerErrorPanel(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ReEventPaper,
        border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message, color = MaterialTheme.colorScheme.error)
            SecondaryActionButton("Try scanning again", onRetry, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ScannedResourcePanel(
    resource: ResourceItem,
    allowedActions: List<ResourceLifecycleAction>,
    loading: Boolean,
    actionNotice: String?,
    actionError: String?,
    onAction: (ResourceLifecycleAction) -> Unit,
    onOpenPassport: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = ReEventMintSoft,
            border = androidx.compose.foundation.BorderStroke(1.dp, ReEventGreen)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Passport verified", style = MaterialTheme.typography.titleLarge)
                Text(resource.title, style = MaterialTheme.typography.titleMedium)
                Text("${resource.quantity} ${resource.unit} · ${resource.status.name.lowercase().replace('_', ' ')}", color = ReEventMuted)
            }
        }
        Text("Record lifecycle action", style = MaterialTheme.typography.titleMedium)
        if (loading) ScannerNotice("Saving action…")
        actionError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        actionNotice?.let { Text(it, color = ReEventGreen, style = MaterialTheme.typography.bodyMedium) }
        if (allowedActions.isEmpty()) {
            Text("You can verify this passport, but lifecycle actions are restricted to its organiser or return sender.", color = ReEventMuted)
        } else {
            allowedActions.forEachIndexed { index, lifecycleAction ->
                if (index == 0) {
                    PrimaryActionButton(lifecycleAction.label, { onAction(lifecycleAction) }, Modifier.fillMaxWidth())
                } else {
                    SecondaryActionButton(lifecycleAction.label, { onAction(lifecycleAction) }, Modifier.fillMaxWidth())
                }
            }
        }
        SecondaryActionButton("Open digital passport", onOpenPassport, Modifier.fillMaxWidth())
        SecondaryActionButton("Scan another code", onScanAgain, Modifier.fillMaxWidth())
    }
}

private fun User.scannerActionsFor(resource: ResourceItem): List<ResourceLifecycleAction> = when {
    role == UserRole.ORGANIZER && id == resource.ownerId -> ResourceLifecycleAction.entries
    role == UserRole.PARTICIPANT -> listOf(ResourceLifecycleAction.RETURN)
    else -> emptyList()
}

@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
private fun CameraQrPreview(
    key: Int,
    onPayload: (String) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(key) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val detector = remember(key) { AtomicBoolean(false) }
    val executor = remember(key) { Executors.newSingleThreadExecutor() }
    val providerFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    DisposableEffect(previewView, lifecycleOwner) {
        val active = AtomicBoolean(true)
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        providerFuture.addListener({
            if (!active.get()) return@addListener
            runCatching {
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage == null) {
                        imageProxy.close()
                    } else {
                        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(input)
                            .addOnSuccessListener(executor) { barcodes ->
                                val value = barcodes.firstOrNull()?.rawValue
                                if (value != null && detector.compareAndSet(false, true)) {
                                    mainExecutor.execute { onPayload(value) }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    }
                }
                val provider = providerFuture.get()
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
            }.onFailure { error ->
                mainExecutor.execute { onCameraError(error.message ?: "Unable to start the camera scanner.") }
            }
        }, mainExecutor)
        onDispose {
            active.set(false)
            scanner.close()
            executor.shutdown()
            if (providerFuture.isDone) runCatching { providerFuture.get().unbindAll() }
        }
    }

    Box(
        modifier = modifier
            .background(ReEventMintSoft, RoundedCornerShape(20.dp))
            .border(1.dp, ReEventLine, RoundedCornerShape(20.dp))
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier.align(Alignment.Center).size(220.dp).border(3.dp, ReEventGreen, RoundedCornerShape(22.dp))
        )
    }
}
