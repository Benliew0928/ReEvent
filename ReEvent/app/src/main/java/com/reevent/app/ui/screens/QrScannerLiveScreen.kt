package com.reevent.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.reevent.app.core.model.User
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventTextSecondary
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * CameraX provides the local preview while the bundled ML Kit model decodes only QR codes. A QR
 * can resolve while offline when its resource/passport has already been synchronised locally.
 */
@Composable
fun QrScannerLiveScreen(
    user: User,
    onOpenPassport: (String) -> Unit,
    onBack: () -> Unit,
    initialPayload: String? = null,
    viewModel: FeatureViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scannerActive by rememberSaveable { mutableStateOf(false) }
    var rawPayload by rememberSaveable(initialPayload) { mutableStateOf(initialPayload) }
    var scanError by rememberSaveable { mutableStateOf<String?>(null) }
    var scannerSession by rememberSaveable { mutableIntStateOf(0) }
    var manualPayload by rememberSaveable { mutableStateOf("") }
    var manualEntryVisible by rememberSaveable { mutableStateOf(false) }
    var resumeScannerOnReturn by rememberSaveable { mutableStateOf(false) }
    val cameraPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasCameraPermission = granted
            scannerActive = granted
            if (!granted) scanError = "Camera permission was not granted. Allow it to scan a resource QR code."
        }

    fun startScanning() {
        scanError = null
        manualEntryVisible = false
        if (hasCameraPermission) {
            scannerActive = true
            scannerSession += 1
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    val scannerLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(scannerLifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && resumeScannerOnReturn) {
                    resumeScannerOnReturn = false
                    startScanning()
                }
            }
        scannerLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { scannerLifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
        if (initialPayload == null) startScanning()
    }
    LaunchedEffect(rawPayload) {
        val payload = rawPayload ?: return@LaunchedEffect
        if (payload == initialPayload) viewModel.refreshForPassportLink()
        when (val result = viewModel.resolvePassportPayload(payload, user.id)) {
            is PassportScanResolution.Verified -> {
                scanError = null
                viewModel.recordPassportScan(user, result.resourceId)
                scannerActive = false
                rawPayload = null
                resumeScannerOnReturn = true
                onOpenPassport(result.resourceId)
            }

            PassportScanResolution.Unavailable -> {
                scanError =
                    "This is a valid ReEvent passport, but it is unavailable to this signed-in account or has not been refreshed on this device."
                rawPayload = null
            }

            is PassportScanResolution.Malformed -> {
                scanError = result.message
                rawPayload = null
            }
        }
    }

    fun resetScanner() {
        rawPayload = null
        scanError = null
        manualEntryVisible = false
        scannerActive = true
        scannerSession += 1
    }

    when {
        scannerActive && hasCameraPermission && rawPayload == null -> {
            FullScreenQrScanner(
                key = scannerSession,
                detectionPaused = manualEntryVisible,
                onPayload = { payload -> rawPayload = payload },
                onCameraError = { message ->
                    scannerActive = false
                    scanError = message
                },
                scanError = scanError,
                onBack = onBack,
                onManualCode = {
                    manualEntryVisible = true
                    scanError = null
                },
                onRetry = ::resetScanner,
            )
        }

        rawPayload != null -> ScannerProcessingScreen()

        else -> {
            ScannerFallbackScreen(
                scanError = scanError,
                onOpenManualCode = {
                    manualEntryVisible = true
                    scanError = null
                },
                onStartScanning = ::startScanning,
                onBack = onBack,
            )
        }
    }

    if (manualEntryVisible) {
        ManualCodeDialog(
            manualPayload = manualPayload,
            error = scanError,
            onManualPayloadChange = {
                manualPayload = it
                scanError = null
            },
            onVerify = {
                val entered = manualPayload.trim()
                if (entered.isBlank()) {
                    scanError = "Enter a ReEvent passport QR code first."
                } else {
                    rawPayload = entered
                }
            },
            onDismiss = {
                manualEntryVisible = false
                scanError = null
            },
        )
    }
}

@Composable
private fun FullScreenQrScanner(
    key: Int,
    detectionPaused: Boolean,
    onPayload: (String) -> Unit,
    onCameraError: (String) -> Unit,
    scanError: String?,
    onBack: () -> Unit,
    onManualCode: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        CameraQrPreview(
            key = key,
            analysisEnabled = !detectionPaused,
            onPayload = onPayload,
            onCameraError = onCameraError,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.24f)),
        )
        ScannerHeader(onBack = onBack, modifier = Modifier.align(Alignment.TopCenter))
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            QrScanFrame()
            Text(
                "Keep the QR code inside the frame",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        }
        ScannerControlSheet(
            scanError = scanError,
            onManualCode = onManualCode,
            onRetry = onRetry,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ScannerHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
        color = HomePaper,
    ) {
        Row(
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 42.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onBack,
                modifier = Modifier.size(44.dp).testTag("scanner_back"),
                shape = RoundedCornerShape(22.dp),
                color = HomePaper,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = HomeForest,
                    )
                }
            }
            LogoMark(Modifier.padding(start = 12.dp).size(42.dp))
            Text(
                "Scan resource",
                modifier = Modifier.padding(start = 12.dp).weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = HomeInk,
            )
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = HomeSage,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(HomeForest, RoundedCornerShape(4.dp)))
                    Text("Offline ready", style = MaterialTheme.typography.labelLarge, color = HomeForest)
                }
            }
        }
    }
}

@Composable
private fun ScannerControlSheet(
    scanError: String?,
    onManualCode: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = HomePaper,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 28.dp, top = 12.dp, end = 28.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally).width(44.dp).height(5.dp).background(HomeSage, RoundedCornerShape(3.dp)),
            )
            if (scanError == null) {
                ScannerManualCodeRow(onClick = onManualCode)
                androidx.compose.material3.HorizontalDivider(color = HomeLine)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(Icons.Outlined.Eco, contentDescription = null, tint = HomeForest)
                    Text(
                        "Saved passports can be verified offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HomeInk,
                    )
                }
            } else {
                Text(scanError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                SecondaryActionButton("Try scanning again", onRetry, Modifier.fillMaxWidth())
                ScannerManualCodeRow(onClick = onManualCode)
            }
        }
    }
}

@Composable
private fun ScannerManualCodeRow(
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(26.dp), color = HomeSage) {
                Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(Icons.Outlined.Keyboard, contentDescription = null, tint = HomeForest)
                }
            }
            Text(
                "Manual code",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = HomeInk,
            )
            androidx.compose.material3.Icon(Icons.Outlined.ChevronRight, contentDescription = "Enter code manually", tint = HomeForest)
        }
    }
}

@Composable
private fun ScannerFallbackScreen(
    scanError: String?,
    onOpenManualCode: () -> Unit,
    onStartScanning: () -> Unit,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(ReEventMintSoft)) {
        ScannerHeader(onBack = onBack, modifier = Modifier.align(Alignment.TopCenter))
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = HomePaper,
        ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 28.dp, top = 22.dp, end = 28.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    scanError ?: "Camera scanner is ready when permission is available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (scanError == null) ReEventTextSecondary else MaterialTheme.colorScheme.error,
                )
                SecondaryActionButton("Try scanning again", onStartScanning, Modifier.fillMaxWidth())
                ScannerManualCodeRow(onClick = onOpenManualCode)
            }
        }
    }
}

@Composable
private fun ManualCodeDialog(
    manualPayload: String,
    error: String?,
    onManualPayloadChange: (String) -> Unit,
    onVerify: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HomePaper,
        titleContentColor = HomeInk,
        textContentColor = ReEventTextSecondary,
        title = { Text("Enter passport code") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Use a ReEvent passport QR payload when camera scanning is unavailable.")
                OutlinedTextField(
                    value = manualPayload,
                    onValueChange = onManualPayloadChange,
                    label = { Text("Passport QR code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onVerify) {
                Text("Verify code", color = HomeForest)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = HomeForest)
            }
        },
    )
}

@Composable
private fun ScannerProcessingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(ReEventMintSoft),
        contentAlignment = Alignment.Center,
    ) {
        Text("Verifying scanned QR code…", style = MaterialTheme.typography.titleMedium, color = HomeInk)
    }
}

@Composable
private fun QrScanFrame() {
    val scanLineProgress by rememberInfiniteTransition(label = "qr_scan_line")
        .animateFloat(
            initialValue = 0.22f,
            targetValue = 0.78f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1_600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "qr_scan_line_progress",
        )

    Canvas(Modifier.size(248.dp)) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 44.dp.toPx()
        val edge = strokeWidth / 2
        val far = size.width - edge
        val bottom = size.height - edge

        fun corner(from: Offset, to: Offset) =
            drawLine(
                color = ReEventGreen,
                start = from,
                end = to,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )

        corner(Offset(edge, cornerLength), Offset(edge, edge))
        corner(Offset(edge, edge), Offset(cornerLength, edge))
        corner(Offset(far - cornerLength, edge), Offset(far, edge))
        corner(Offset(far, edge), Offset(far, cornerLength))
        corner(Offset(edge, bottom - cornerLength), Offset(edge, bottom))
        corner(Offset(edge, bottom), Offset(cornerLength, bottom))
        corner(Offset(far - cornerLength, bottom), Offset(far, bottom))
        corner(Offset(far, bottom - cornerLength), Offset(far, bottom))
        drawLine(
            color = ReEventGreen.copy(alpha = 0.92f),
            start = Offset(18.dp.toPx(), size.height * scanLineProgress),
            end = Offset(size.width - 18.dp.toPx(), size.height * scanLineProgress),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@androidx.annotation.OptIn(markerClass = [ExperimentalGetImage::class])
@Composable
private fun CameraQrPreview(
    key: Int,
    analysisEnabled: Boolean,
    onPayload: (String) -> Unit,
    onCameraError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView =
        remember(key) {
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_START
            }
        }
    val detector = remember(key) { AtomicBoolean(false) }
    val currentAnalysisEnabled by rememberUpdatedState(analysisEnabled)
    val executor = remember(key) { Executors.newSingleThreadExecutor() }
    val providerFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    DisposableEffect(previewView, lifecycleOwner) {
        val active = AtomicBoolean(true)
        val scanner =
            BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
            )
        providerFuture.addListener({
            if (!active.get()) return@addListener
            runCatching {
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analysis =
                    ImageAnalysis
                        .Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (!currentAnalysisEnabled || mediaImage == null) {
                        imageProxy.close()
                    } else {
                        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner
                            .process(input)
                            .addOnSuccessListener(executor) { barcodes ->
                                val value = barcodes.firstOrNull()?.rawValue
                                if (value != null && detector.compareAndSet(false, true)) {
                                    mainExecutor.execute { onPayload(value) }
                                }
                            }.addOnCompleteListener { imageProxy.close() }
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

    AndroidView(factory = { previewView }, modifier = modifier)
}
