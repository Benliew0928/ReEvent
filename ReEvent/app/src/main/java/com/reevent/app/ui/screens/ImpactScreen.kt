package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reevent.app.R
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.feature.impact.ImpactBadge
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.EditorialDetailHeader
import com.reevent.app.ui.components.EditorialDetailScaffold
import com.reevent.app.ui.components.EditorialIconButton
import com.reevent.app.ui.components.EditorialNotice
import com.reevent.app.ui.components.EditorialSectionCard
import com.reevent.app.ui.components.NotificationDialog
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeEditorialFont
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ImpactEventScope(
    val id: String,
    val name: String,
)

@Composable
fun ImpactScreen(
    onNavigate: (TopLevelDestination) -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    metrics: List<ImpactMetric> = emptyList(),
    recoveryRate: Float? = null,
    recoveryLabel: String = "—",
    chartValues: List<Float> = emptyList(),
    badge: ImpactBadge? = null,
    unavailableEstimateReason: String? = null,
    latestRecord: ImpactRecord? = null,
    selectedScope: ImpactEventScope? = null,
    scopes: List<ImpactEventScope> = emptyList(),
    onScopeSelected: (String) -> Unit = {},
) {
    var selectingScope by rememberSaveable { mutableStateOf(false) }
    var showNotificationDialog by rememberSaveable { mutableStateOf(false) }

    EditorialDetailScaffold(
        selected = TopLevelDestination.IMPACT,
        onNavigate = onNavigate,
        modifier = modifier,
    ) { padding ->
        ReEventLazyColumn(
            paddingValues = padding,
            modifier = Modifier.testTag("impact_editorial_list"),
        ) {
            item {
                EditorialDetailHeader(
                    eyebrow = "Verified reporting",
                    title = "Impact, made visible",
                    subtitle = "A clear record of what stayed in use and what avoided disposal.",
                    onProfile = onProfile,
                    profileName = "Profile",
                    trailing = {
                        EditorialIconButton(
                            icon = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            onClick = { showNotificationDialog = true },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            selectedScope?.let { scope ->
                item {
                    ImpactScopeCard(
                        scope = scope,
                        canChange = scopes.size > 1,
                        onChange = { selectingScope = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                ImpactHeroCard(
                    recoveryRate = recoveryRate,
                    recoveryLabel = recoveryLabel,
                    detail = unavailableEstimateReason ?: if (metrics.isEmpty()) {
                        "Verified impact appears after the first completed recovery or handover."
                    } else {
                        "Completed circular routes are translated into reporting-ready outcomes."
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                ImpactSectionHeading(
                    title = "Recovery channels",
                    subtitle = "Verified completion mix across reuse, repair, donation and recycling.",
                )
            }
            item {
                RecoveryChannelsCard(
                    values = chartValues.ifEmpty { listOf(0f, 0f, 0f, 0f) },
                    unavailableReason = unavailableEstimateReason,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item {
                ImpactBadgeCard(badge = badge, modifier = Modifier.fillMaxWidth())
            }

            if (metrics.isNotEmpty()) {
                item { ImpactSectionHeading("Reporting measures", "Only authoritative or explicitly unavailable values are shown.") }
                item { ImpactMetricGrid(metrics = metrics, modifier = Modifier.fillMaxWidth()) }
            }

            latestRecord?.let { record ->
                item {
                    ImpactSectionHeading("Latest contribution", "The newest server-verified lifecycle outcome.")
                }
                item { LatestImpactContributionCard(record, modifier = Modifier.fillMaxWidth()) }
            }
        }
    }

    if (showNotificationDialog) {
        NotificationDialog(onDismiss = { showNotificationDialog = false })
    }

    if (selectingScope) {
        AlertDialog(
            onDismissRequest = { selectingScope = false },
            title = {
                Text(
                    text = "Choose reporting event",
                    style = HomeCardTitleStyle.copy(fontSize = 28.sp),
                    color = HomeInk,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    scopes.forEach { scope ->
                        Surface(
                            onClick = {
                                onScopeSelected(scope.id)
                                selectingScope = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (scope.id == selectedScope?.id) HomeSage else HomeMist,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = scope.name,
                                style = HomeBodyStyle,
                                color = HomeForest,
                                modifier = Modifier.padding(13.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectingScope = false }) {
                    Text("Close", color = HomeForest, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = HomePaper,
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun ImpactScopeCard(
    scope: ImpactEventScope,
    canChange: Boolean,
    onChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorialSectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = HomeMist) {
                Icon(
                    Icons.Outlined.Event,
                    contentDescription = null,
                    tint = HomeForest,
                    modifier = Modifier.padding(11.dp).size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("REPORTING SCOPE", style = HomeSupportingTextStyle.copy(fontSize = 11.sp, letterSpacing = .8.sp), color = HomeMuted)
                Text(scope.name, style = HomeCardTitleStyle.copy(fontSize = 23.sp), color = HomeInk)
            }
            if (canChange) {
                Surface(onClick = onChange, shape = CircleShape, color = HomeSage) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Change", style = HomeSupportingTextStyle, color = HomeForest)
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = HomeForest, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactHeroCard(
    recoveryRate: Float?,
    recoveryLabel: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(24.dp), color = HomeForest) {
        BoxWithConstraints {
            if (maxWidth < 420.dp) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    ImpactHeroCopy(detail = detail, modifier = Modifier.fillMaxWidth())
                    EditorialImpactRing(
                        progress = recoveryRate,
                        centerText = recoveryLabel,
                        modifier = Modifier.size(170.dp),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    ImpactHeroCopy(detail = detail, modifier = Modifier.weight(1f))
                    EditorialImpactRing(
                        progress = recoveryRate,
                        centerText = recoveryLabel,
                        modifier = Modifier.size(180.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactHeroCopy(detail: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            "RECOVERY PROGRESS",
            color = HomeSage,
            fontFamily = HomeBodyFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
        Text(
            "Every outcome counts.",
            color = Color.White,
            fontFamily = HomeEditorialFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 36.sp,
            lineHeight = 36.sp,
        )
        Text(detail, style = HomeBodyStyle, color = Color.White.copy(alpha = .82f))
        Surface(shape = CircleShape, color = HomeSage) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Verified, contentDescription = null, tint = HomeForest, modifier = Modifier.size(17.dp))
                Text("SDG 12 ALIGNED", style = HomeSupportingTextStyle.copy(fontSize = 11.sp), color = HomeForest)
            }
        }
    }
}

@Composable
private fun EditorialImpactRing(
    progress: Float?,
    centerText: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val origin = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = Color.White.copy(alpha = .22f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = origin,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            progress?.let {
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 360f * it.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = origin,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                centerText,
                color = Color.White,
                fontFamily = HomeEditorialFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 38.sp,
            )
            Text("recovered", style = HomeSupportingTextStyle, color = HomeSage)
        }
    }
}

@Composable
private fun ImpactSectionHeading(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = HomeSectionTitleStyle, color = HomeInk)
        Text(subtitle, style = HomeSupportingTextStyle, color = HomeMuted)
    }
}

@Composable
private fun RecoveryChannelsCard(
    values: List<Float>,
    unavailableReason: String?,
    modifier: Modifier = Modifier,
) {
    val labels = listOf("Reuse", "Repair", "Donation", "Recycle")
    val fills = listOf(HomeForest, HomeGold, Color(0xFF76917C), Color(0xFFA7B58E))
    EditorialSectionCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(128.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                values.take(4).forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Text("${(value.coerceIn(0f, 1f) * 100).toInt()}%", style = HomeSupportingTextStyle, color = HomeMuted)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((20 + value.coerceIn(0f, 1f) * 72).dp)
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                .background(fills[index]),
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEachIndexed { index, label ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(8.dp).background(fills[index], CircleShape))
                        Text(label, style = HomeSupportingTextStyle.copy(fontSize = 11.sp), color = HomeMuted)
                    }
                }
            }
            unavailableReason?.let { EditorialNotice(it, modifier = Modifier.fillMaxWidth()) }
        }
    }
}

@Composable
private fun ImpactBadgeCard(
    badge: ImpactBadge?,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = HomeSage) {
        Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
            if (badge == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Outlined.Verified, contentDescription = null, tint = HomeForest, modifier = Modifier.size(34.dp))
                    Text("Recovery badge pending", style = HomeCardTitleStyle.copy(fontSize = 27.sp), color = HomeInk)
                    Text(
                        "A badge appears after verified impact is recorded.",
                        style = HomeSupportingTextStyle,
                        color = HomeMuted,
                    )
                }
            } else {
                Image(
                    painter = painterResource(R.drawable.impact_badge_high_recovery),
                    contentDescription = "${badge.displayLabel()} badge",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
                    shape = CircleShape,
                    color = HomePaper.copy(alpha = .92f),
                ) {
                    Text(
                        badge.displayLabel(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = HomeBodyStyle,
                        color = HomeForest,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactMetricGrid(
    metrics: List<ImpactMetric>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val columns = if (maxWidth < 440.dp) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowMetrics.forEach { metric ->
                        ImpactMetricCard(metric = metric, modifier = Modifier.weight(1f))
                    }
                    repeat(columns - rowMetrics.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ImpactMetricCard(
    metric: ImpactMetric,
    modifier: Modifier = Modifier,
) {
    EditorialSectionCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                metric.value,
                color = HomeForest,
                fontFamily = HomeEditorialFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34.sp,
                lineHeight = 35.sp,
            )
            Text(metric.label, style = HomeBodyStyle, color = HomeInk)
            Text(metric.detail, style = HomeSupportingTextStyle, color = HomeMuted)
        }
    }
}

@Composable
private fun LatestImpactContributionCard(
    record: ImpactRecord,
    modifier: Modifier = Modifier,
) {
    EditorialSectionCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Surface(shape = CircleShape, color = HomeSage) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = HomeForest,
                        modifier = Modifier.padding(9.dp).size(20.dp),
                    )
                }
                Text("SERVER VERIFIED", style = HomeSupportingTextStyle.copy(fontSize = 11.sp, letterSpacing = .8.sp), color = HomeForest)
            }
            Text(
                text = "${record.transactionType.displayLabel()} — ${record.completedQuantity.formatImpactNumber()} ${record.unit}",
                style = HomeCardTitleStyle.copy(fontSize = 25.sp),
                color = HomeInk,
            )
            val outcomes = buildList {
                record.materialDivertedKg?.let { add("${it.formatImpactNumber()} kg diverted") }
                record.emissionsAvoidedKg?.let { add("${it.formatImpactNumber()} kg CO2e avoided") }
                if (record.recoinsTransferred > 0) add("${record.recoinsTransferred} ReCoins transferred")
                if (record.recoinsRewarded > 0) add("${record.recoinsRewarded} ReCoins rewarded")
            }
            Text(
                text = outcomes.ifEmpty {
                    listOf("No documented mass or emissions estimate was supplied for this outcome.")
                }.joinToString(" · "),
                style = HomeBodyStyle,
                color = HomeMuted,
            )
            Text(
                text = "Recorded ${record.calculatedAt.toImpactDateTime()}",
                style = HomeSupportingTextStyle,
                color = HomeMuted,
            )
        }
    }
}

private fun ImpactBadge.displayLabel(): String =
    when (this) {
        ImpactBadge.FIRST_RECOVERY -> "First recovery"
        ImpactBadge.CIRCULAR_STARTER -> "Circular starter"
        ImpactBadge.HIGH_RECOVERY -> "High recovery"
    }

private fun Double.formatImpactNumber(): String =
    if (this % 1.0 == 0.0) toLong().toString() else
        "%.3f".format(java.util.Locale.US, this).trimEnd('0').trimEnd('.')

private val impactDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm")

private fun Long.toImpactDateTime(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(impactDateTimeFormat)
