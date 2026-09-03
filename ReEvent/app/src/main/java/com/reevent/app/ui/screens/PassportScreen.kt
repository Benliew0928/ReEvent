package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.reevent.app.R
import com.reevent.app.feature.passports.PassportViewerAccess
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.ProfileAvatarButton
import com.reevent.app.ui.components.QrCodePanel
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.ResourcePhotoImage
import com.reevent.app.ui.components.SecondaryActionButton
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeDeepForest
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGold
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLabelStyle
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSectionTitleStyle
import com.reevent.app.ui.theme.HomeSupportingTextStyle

@Composable
fun PassportScreen(
    onNavigate: (TopLevelDestination) -> Unit,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onMatch: () -> Unit,
    item: com.reevent.app.ui.ResourceCardModel? = null,
    passportId: String? = null,
    qrPayload: String? = null,
    qrUnavailableMessage: String? = null,
    viewerAccess: PassportViewerAccess? = null,
    recommendedAction: String? = null,
    recoverySteps: List<RecoveryStep> = emptyList(),
    showMatchAction: Boolean = false,
    profileName: String = "",
    lifecycleActions: List<ResourceLifecycleAction> = emptyList(),
    onLifecycleAction: ((ResourceLifecycleAction) -> Unit)? = null,
    lifecycleActionLoading: Boolean = false,
    lifecycleActionNotice: String? = null,
    lifecycleActionError: String? = null,
) {
    val hasIssuedPassport = item != null && passportId != null && !qrPayload.isNullOrBlank()
    var qrExpanded by remember(qrPayload) { mutableStateOf(false) }

    ReEventScaffold(selected = TopLevelDestination.MARKETPLACE, onNavigate = onNavigate) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(HomeCanvas),
        ) {
            Image(
                painter = painterResource(R.drawable.home_paper_texture),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = 0.15f,
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopEnd,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = 54.dp)
                        .width(260.dp)
                        .height(218.dp),
                alpha = 0.30f,
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopStart,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-74).dp, y = 8.dp)
                        .width(214.dp)
                        .height(178.dp)
                        .rotate(138f),
                alpha = 0.16f,
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.CenterStart,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = (-112).dp, y = 72.dp)
                        .width(224.dp)
                        .height(186.dp)
                        .rotate(112f),
                alpha = 0.12f,
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomStart,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-62).dp, y = 42.dp)
                        .width(236.dp)
                        .height(196.dp)
                        .rotate(-26f),
                alpha = 0.18f,
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomEnd,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 54.dp, y = 24.dp)
                        .width(188.dp)
                        .height(156.dp)
                        .rotate(74f),
                alpha = 0.12f,
            )
            Image(
                painter = painterResource(R.drawable.home_botanical_sprig),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alignment = Alignment.BottomEnd,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 58.dp, y = (-64).dp)
                        .width(220.dp)
                        .height(184.dp)
                        .rotate(42f),
                alpha = 0.16f,
            )
            LazyColumn(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                        .testTag("passport_editorial"),
                contentPadding =
                    PaddingValues(
                        start = 20.dp,
                        top = padding.calculateTopPadding() + 94.dp,
                        end = 20.dp,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item { PassportHero(item = item) }
                if (item != null && !hasIssuedPassport) {
                    item { PassportVerificationInProgressBanner() }
                }
                item {
                    PassportResourceDossier(
                        item = item,
                        viewerAccess = viewerAccess,
                        recommendedAction = recommendedAction,
                    )
                }
                item {
                    when {
                        item == null -> PassportEmptyState()
                        hasIssuedPassport ->
                            PassportVerifiedCard(
                                passportId = checkNotNull(passportId),
                                qrPayload = checkNotNull(qrPayload),
                                onQrClick = { qrExpanded = true },
                            )

                        else -> PassportPendingCard(message = qrUnavailableMessage)
                    }
                }
                item {
                    PassportHistory(recoverySteps = recoverySteps)
                }
                if (lifecycleActions.isNotEmpty() && onLifecycleAction != null) {
                    item {
                        PassportLifecycleActions(
                            actions = lifecycleActions,
                            loading = lifecycleActionLoading,
                            notice = lifecycleActionNotice,
                            error = lifecycleActionError,
                            onAction = onLifecycleAction,
                        )
                    }
                }
                if (hasIssuedPassport && showMatchAction) {
                    item {
                        PrimaryActionButton(
                            text = "Find partner matches",
                            icon = Icons.Outlined.Star,
                            onClick = onMatch,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Surface(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = 760.dp)
                        .fillMaxWidth(),
                color = HomeCanvas.copy(alpha = 0.94f),
                shadowElevation = 2.dp,
            ) {
                PassportPinnedHeader(
                    item = item,
                    onBack = onBack,
                    onProfile = onProfile,
                    profileName = profileName,
                    modifier =
                        Modifier.padding(
                            start = 20.dp,
                            top = padding.calculateTopPadding() + 10.dp,
                            end = 20.dp,
                            bottom = 12.dp,
                        ),
                )
            }
        }
    }

    if (qrExpanded && hasIssuedPassport) {
        PassportQrDialog(
            resourceTitle = checkNotNull(item).title,
            passportId = checkNotNull(passportId),
            qrPayload = checkNotNull(qrPayload),
            onDismiss = { qrExpanded = false },
        )
    }
}

@Composable
private fun PassportLifecycleActions(
    actions: List<ResourceLifecycleAction>,
    loading: Boolean,
    notice: String?,
    error: String?,
    onAction: (ResourceLifecycleAction) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HomePaper.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Resource actions", style = HomeSectionTitleStyle, color = HomeInk)
            Text(
                "Record an action for this resource.",
                style = HomeSupportingTextStyle,
                color = HomeMuted,
            )
            if (loading) {
                Text("Saving action…", style = HomeSupportingTextStyle, color = HomeForest)
            }
            error?.let { Text(it, style = HomeSupportingTextStyle, color = Color(0xFF8A2836)) }
            notice?.let { Text(it, style = HomeSupportingTextStyle, color = HomeForest) }
            actions.forEachIndexed { index, action ->
                val modifier = Modifier.fillMaxWidth().testTag("passport_lifecycle_${action.name.lowercase()}")
                if (index == 0) {
                    PrimaryActionButton(
                        text = action.label,
                        onClick = { onAction(action) },
                        modifier = modifier,
                    )
                } else {
                    SecondaryActionButton(
                        text = action.label,
                        onClick = { onAction(action) },
                        modifier = modifier,
                    )
                }
            }
        }
    }
}

@Composable
private fun PassportPinnedHeader(
    item: com.reevent.app.ui.ResourceCardModel?,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    profileName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("passport_back"),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, HomeLine),
            tonalElevation = 0.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = HomeForest,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Resource passport", style = HomeCardTitleStyle, color = HomeInk)
            Text(
                item?.let { "Verified route for ${it.title}" } ?: "Select a resource to view its verified route",
                style = HomeSupportingTextStyle,
                color = HomeMuted,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(12.dp))
        ProfileAvatarButton(
            displayName = profileName,
            onClick = onProfile,
            modifier = Modifier.testTag("passport_profile"),
        )
    }
}

@Composable
private fun PassportHero(item: com.reevent.app.ui.ResourceCardModel?) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(HomeSage),
    ) {
        if (item == null) {
            Text(
                text = "No resource selected",
                style = HomeSectionTitleStyle,
                color = HomeMuted,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            ResourcePhotoImage(item.photoPath, item.imageRes, item.title, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PassportResourceDossier(
    item: com.reevent.app.ui.ResourceCardModel?,
    viewerAccess: PassportViewerAccess?,
    recommendedAction: String?,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (item == null) {
                Text("Resource details will appear here.", style = HomeSupportingTextStyle, color = HomeMuted)
            } else {
                Text("Resource dossier", style = HomeSectionTitleStyle, color = HomeInk)
                PassportDossierMetric(Icons.Outlined.Inventory2, "Quantity", item.quantity)
                HorizontalDivider(color = HomeLine)
                PassportDossierCondition(
                    label = item.tone.label,
                    color = item.tone.color,
                )
                HorizontalDivider(color = HomeLine)
                PassportDossierMetric(Icons.Outlined.Layers, "Material", item.category)
                HorizontalDivider(color = HomeLine)
                PassportDossierMetric(Icons.Outlined.Sell, "Current value", item.price)
                HorizontalDivider(color = HomeLine)
                PassportDossierDetail(
                    icon = Icons.Outlined.Person,
                    label = "Your access",
                    value = viewerAccess?.label ?: "Checking authorised access",
                )
                HorizontalDivider(color = HomeLine)
                PassportDossierDetail(
                    icon = Icons.Outlined.Eco,
                    label = "Resource guidance",
                    value = recommendedAction ?: "Review resource details",
                )
            }
        }
    }
}

@Composable
private fun PassportDossierMetric(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = HomeMuted, modifier = Modifier.size(16.dp))
        Text(
            label,
            style = HomeSupportingTextStyle,
            color = HomeMuted,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            value,
            style = HomeBodyStyle,
            color = HomeInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PassportDossierCondition(
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = HomeMuted,
            modifier = Modifier.size(16.dp),
        )
        Text(
            "Condition",
            style = HomeSupportingTextStyle,
            color = HomeMuted,
            modifier = Modifier.weight(1f),
        )
        StatusChip(text = label, color = color)
    }
}

@Composable
private fun PassportDossierDetail(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = HomeMuted, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = HomeSupportingTextStyle, color = HomeMuted)
            Text(value, style = HomeBodyStyle, color = HomeInk)
        }
    }
}

@Composable
private fun PassportVerifiedCard(
    passportId: String,
    qrPayload: String,
    onQrClick: () -> Unit,
) {
    Surface(
        onClick = onQrClick,
        shape = RoundedCornerShape(20.dp),
        color = HomeDeepForest,
        modifier = Modifier.testTag("passport_qr_expand"),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = Color.White)
                    Text(
                        "PASSPORT VERIFIED",
                        style = HomeLabelStyle,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
                Text(passportId, style = HomeSectionTitleStyle, color = Color.White)
                Text(
                    "Read-only verification record for authorised partners and buyers.",
                    style = HomeSupportingTextStyle,
                    color = Color.White.copy(alpha = 0.82f),
                )
                Text(
                    "Tap the QR code to enlarge it.",
                    style = HomeLabelStyle,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(132.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White),
            ) {
                QrCodePanel(payload = qrPayload, modifier = Modifier.fillMaxSize(), qrSize = 88.dp)
            }
        }
    }
}

@Composable
private fun PassportPendingCard(message: String?) {
    Surface(
        modifier = Modifier.testTag("passport_pending_card"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFE9E7),
        border = BorderStroke(1.dp, Color(0xFFE8B8BD)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Color(0xFF8A2836))
                Text("Your resource is saved", style = HomeCardTitleStyle, color = HomeInk)
            }
            Text(
                "Passport ID and QR code are issued after server verification.",
                style = HomeSupportingTextStyle,
                color = HomeMuted,
            )
            Text(
                message ?: "QR code pending until the server issues this resource passport.",
                style = HomeSupportingTextStyle,
                color = HomeMuted,
            )
            StatusChip(text = "SYNC PENDING", color = Color(0xFF8A2836))
            HorizontalDivider(color = HomeLine)
            PassportPendingRow(
                title = "QR code — unavailable",
                detail = "Will be available after verification.",
            )
            PassportPendingRow(
                title = "Passport ID — Not generated",
                detail = "A passport ID will be created when the resource is saved.",
            )
        }
    }
}

@Composable
private fun PassportVerificationInProgressBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = HomeSage,
        border = BorderStroke(1.dp, HomeGold),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = HomeForest)
            Text(
                "VERIFICATION IN PROGRESS",
                style = HomeLabelStyle,
                color = HomeForest,
            )
        }
    }
}

@Composable
private fun PassportPendingRow(title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Lock, contentDescription = null, tint = HomeMuted)
        Column {
            Text(title, style = HomeBodyStyle, color = HomeInk)
            Text(detail, style = HomeSupportingTextStyle, color = HomeMuted)
        }
    }
}

@Composable
private fun PassportEmptyState() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HomeSage,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Text(
            "Select a resource to view its passport verification.",
            modifier = Modifier.padding(18.dp),
            style = HomeSupportingTextStyle,
            color = HomeMuted,
        )
    }
}

@Composable
private fun PassportHistory(recoverySteps: List<RecoveryStep>) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Recovery trail", style = HomeSectionTitleStyle, color = HomeInk)
            if (recoverySteps.isEmpty()) {
                Text("No recovery route has been recorded yet.", style = HomeSupportingTextStyle, color = HomeMuted)
            } else {
                recoverySteps.forEachIndexed { index, step ->
                    PassportRecoveryTrailStep(
                        step = step,
                        isFirst = index == 0,
                        showConnector = index != recoverySteps.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun PassportRecoveryTrailStep(
    step: RecoveryStep,
    isFirst: Boolean,
    showConnector: Boolean,
) {
    Row(verticalAlignment = Alignment.Top) {
        Column(
            modifier = Modifier.width(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isFirst) HomeForest else HomeSage),
                contentAlignment = Alignment.Center,
            ) {
                if (isFirst) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(9.dp).clip(CircleShape).background(HomeForest),
                    )
                }
            }
            if (showConnector) {
                Box(
                    modifier = Modifier.width(2.dp).height(28.dp).background(HomeForest.copy(alpha = 0.52f)),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    step.title,
                    style = HomeBodyStyle,
                    color = HomeInk,
                    modifier = Modifier.weight(1f),
                )
                Text(step.status, style = HomeLabelStyle, color = HomeMuted)
            }
            Text(step.detail, style = HomeSupportingTextStyle, color = HomeMuted)
        }
    }
}

@Composable
private fun PassportQrDialog(
    resourceTitle: String,
    passportId: String,
    qrPayload: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.testTag("passport_qr_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            border = BorderStroke(1.dp, HomeLine),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Passport QR", style = HomeSectionTitleStyle, color = HomeInk)
                        Text(resourceTitle, style = HomeSupportingTextStyle, color = HomeMuted)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("passport_qr_dialog_close")) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close QR preview", tint = HomeForest)
                    }
                }
                Text(passportId, style = HomeBodyStyle, color = HomeForest)
                QrCodePanel(payload = qrPayload, modifier = Modifier.fillMaxWidth(), qrSize = 280.dp)
            }
        }
    }
}
