package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.reevent.app.feature.passports.PassportViewerAccess
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.InfoRow
import com.reevent.app.ui.components.LocationLine
import com.reevent.app.ui.components.PrimaryActionButton
import com.reevent.app.ui.components.QrCodePanel
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.RecoveryTimeline
import com.reevent.app.ui.components.ResourcePhotoImage
import com.reevent.app.ui.components.ScreenHeader
import com.reevent.app.ui.components.SectionTitle
import com.reevent.app.ui.components.StatusChip
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

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
) {
    ReEventScaffold(selected = TopLevelDestination.MARKETPLACE, onNavigate = onNavigate) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                ScreenHeader(
                    title = "Digital passport",
                    subtitle = item?.let { "Verified route for ${it.title}" } ?: "Select a resource to view its verified route",
                    onBack = onBack,
                    onProfile = onProfile,
                )
            }
            item {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(ReEventMintSoft),
                ) {
                    if (item == null) {
                        Text(
                            text = "No resource selected",
                            style = MaterialTheme.typography.titleLarge,
                            color = ReEventTextSecondary,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        ResourcePhotoImage(item.photoPath, item.imageRes, item.title, Modifier.fillMaxSize())
                        StatusChip(
                            text = item.tone.label,
                            color = item.tone.color,
                            modifier =
                                Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp),
                        )
                    }
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventSurface,
                    border = BorderStroke(1.dp, ReEventLine),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (item == null) {
                            Text("Resource details will appear here.", color = ReEventTextSecondary)
                        } else {
                            Text(text = item.title, style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                            LocationLine(text = "${item.location} • ${item.owner}")
                            HorizontalDivider(color = ReEventLine)
                            InfoRow("Quantity", item.quantity)
                            InfoRow("Condition", item.tone.label)
                            InfoRow("Material", item.category)
                            InfoRow("Current value", item.price)
                            InfoRow("Your access", viewerAccess?.label ?: "Checking authorised access")
                            Text(
                                viewerAccess?.explanation ?: "This passport only shows information available to your signed-in account.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ReEventTextSecondary,
                            )
                            Text("Resource guidance", style = MaterialTheme.typography.bodyMedium, color = ReEventTextSecondary)
                            Text(
                                recommendedAction ?: "Review resource details",
                                style = MaterialTheme.typography.titleMedium,
                                color = ReEventInk,
                            )
                        }
                    }
                }
            }
            item {
                AdaptiveTwoPane(
                    first = {
                        if (item == null || qrPayload.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = ReEventMintSoft,
                            ) {
                                Text(
                                    text = qrUnavailableMessage ?: "QR code pending",
                                    color = ReEventTextSecondary,
                                    modifier = Modifier.padding(16.dp),
                                )
                            }
                        } else {
                            QrCodePanel(payload = qrPayload.orEmpty(), modifier = Modifier.fillMaxWidth())
                        }
                    },
                    second = {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = ReEventGreenDeep,
                        ) {
                            Column(
                                modifier = Modifier.padding(15.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Text(
                                    text = "Passport ID",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.68f),
                                )
                                Text(
                                    text = passportId ?: "Not generated",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                )
                                Text(
                                    text =
                                        if (passportId ==
                                            null
                                        ) {
                                            "A passport ID will be created when the resource is saved."
                                        } else {
                                            "Read-only verification record for authorised partners and buyers."
                                        },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.78f),
                                )
                            }
                        }
                    },
                )
            }
            item {
                SectionTitle(title = "Passport history")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ReEventSurface,
                    border = BorderStroke(1.dp, ReEventLine),
                ) {
                    if (recoverySteps.isEmpty()) {
                        Text("No recovery route has been recorded yet.", color = ReEventTextSecondary, modifier = Modifier.padding(16.dp))
                    } else {
                        RecoveryTimeline(modifier = Modifier.padding(16.dp), steps = recoverySteps)
                    }
                }
            }
            if (showMatchAction) {
                item {
                    PrimaryActionButton(
                        text = "Find partner matches",
                        icon = Icons.Outlined.Star,
                        onClick = { if (item != null) onMatch() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
