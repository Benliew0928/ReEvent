package com.reevent.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.reevent.app.R
import com.reevent.app.ui.MockData
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.ResourceItem
import com.reevent.app.ui.ResourceTone
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCanvas
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventMuted
import com.reevent.app.ui.theme.ReEventPaper
import com.reevent.app.ui.theme.ReEventWarm

val ScreenPadding = 20.dp

@Composable
fun ReEventScaffold(
    selected: ReEventScreen?,
    onNavigate: (ReEventScreen) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = ReEventCanvas,
        bottomBar = {
            if (selected != null) {
                ReEventBottomBar(selected = selected, onNavigate = onNavigate)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ReEventCanvas),
            contentAlignment = Alignment.TopCenter
        ) {
            content(innerPadding)
        }
    }
}

@Composable
fun ReEventLazyColumn(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val horizontalPadding = when {
        configuration.screenWidthDp < 360 -> 12.dp
        configuration.screenWidthDp >= 840 -> 28.dp
        else -> ScreenPadding
    }

    LazyColumn(
        modifier = modifier
            .widthIn(max = 760.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = horizontalPadding,
            top = paddingValues.calculateTopPadding() + 14.dp,
            end = horizontalPadding,
            bottom = paddingValues.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
fun LogoMark(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 54.dp
) {
    Image(
        painter = painterResource(R.drawable.reevent_logo),
        contentDescription = "ReEvent logo",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 4)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun BrandLockup(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LogoMark(size = if (compact) 44.dp else 58.dp)
        Column {
            Text(
                text = "ReEvent",
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                color = ReEventGreenDeep
            )
            Text(
                text = "Circular event exchange",
                style = MaterialTheme.typography.labelMedium,
                color = ReEventMuted
            )
        }
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            SoftIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ReEventInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onProfile != null) {
            Spacer(Modifier.width(12.dp))
            SoftIconButton(
                icon = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                onClick = {}
            )
            Spacer(Modifier.width(8.dp))
            SoftIconButton(
                icon = Icons.Outlined.Person,
                contentDescription = "Profile",
                onClick = onProfile
            )
        }
    }
}

@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ReEventPaper)
            .border(1.dp, ReEventLine, RoundedCornerShape(14.dp)),
        colors = IconButtonDefaults.iconButtonColors(contentColor = ReEventGreenDeep)
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ReEventGreen,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 18.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ReEventLine),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ReEventPaper,
            contentColor = ReEventGreenDeep
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    detail: String,
    modifier: Modifier = Modifier,
    color: Color = ReEventGreen
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = ReEventInk
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventMuted
            )
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ReEventInk,
            modifier = Modifier.weight(1f)
        )
        if (action != null && onAction != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelLarge,
                color = ReEventGreen,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
fun HeroImageCard(
    @DrawableRes imageRes: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    chip: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(224.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ReEventGreenDeep)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ReEventGreenDeep.copy(alpha = 0.88f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
        ) {
            if (chip != null) {
                StatusChip(text = chip, color = ReEventWarm)
                Spacer(Modifier.height(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
fun ResourceCard(
    item: ResourceItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine),
        tonalElevation = 0.dp
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(154.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ReEventMintSoft),
                    contentScale = ContentScale.Crop
                )
                ResourceCardDetails(item)
            }
        } else {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(item.imageRes),
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(106.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ReEventMintSoft),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(14.dp))
                ResourceCardDetails(item, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ResourceCardDetails(
    item: ResourceItem,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(text = item.tone.label, color = item.tone.color)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = ReEventMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ReEventInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.owner,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReEventMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.price,
                        style = MaterialTheme.typography.titleMedium,
                        color = ReEventGreenDeep
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = item.quantity,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReEventMuted
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = item.impact,
                    style = MaterialTheme.typography.labelMedium,
                    color = ReEventBlue
                )
            }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
    }
}

@Composable
fun FormFieldPreview(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ReEventMintSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ReEventGreenDeep,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = ReEventMuted)
                Text(text = value, style = MaterialTheme.typography.bodyLarge, color = ReEventInk)
            }
        }
    }
}

@Composable
fun RecoveryTimeline(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MockData.recoverySteps.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(step.tone.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (index != MockData.recoverySteps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(42.dp)
                                .background(ReEventLine)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = ReEventInk,
                            modifier = Modifier.weight(1f)
                        )
                        StatusChip(text = step.status, color = step.tone.color)
                    }
                    Text(
                        text = step.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ReEventMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    centerText: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = ReEventGreen
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            drawArc(
                color = ReEventLine,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = centerText, style = MaterialTheme.typography.headlineMedium, color = color)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = ReEventMuted)
        }
    }
}

@Composable
fun FakeQrPanel(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Canvas(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val cells = 9
            val gap = size.minDimension * 0.025f
            val cell = (size.minDimension - gap * (cells - 1)) / cells
            for (row in 0 until cells) {
                for (col in 0 until cells) {
                    val anchor = (row < 3 && col < 3) ||
                        (row < 3 && col > 5) ||
                        (row > 5 && col < 3)
                    val pattern = ((row * 7 + col * 5 + row * col) % 4 == 0)
                    if (anchor || pattern) {
                        val color = if (anchor) ReEventGreenDeep else ReEventGreen
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(col * (cell + gap), row * (cell + gap)),
                            size = Size(cell, cell),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniBarChart(
    values: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((28 + value.coerceIn(0f, 1f) * 84).dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(colors[index % colors.size])
            )
        }
    }
}

@Immutable
private data class BottomDestination(
    val screen: ReEventScreen,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun ReEventBottomBar(
    selected: ReEventScreen,
    onNavigate: (ReEventScreen) -> Unit
) {
    val destinations = listOf(
        BottomDestination(ReEventScreen.Home, "Home", Icons.Outlined.Home),
        BottomDestination(ReEventScreen.Marketplace, "Market", Icons.Outlined.Search),
        BottomDestination(ReEventScreen.AddResource, "Add", Icons.Outlined.Add),
        BottomDestination(ReEventScreen.PartnerMap, "Partners", Icons.Outlined.Map),
        BottomDestination(ReEventScreen.Impact, "Impact", Icons.Outlined.BarChart)
    )

    Surface(
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine),
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            containerColor = ReEventPaper,
            tonalElevation = 0.dp
        ) {
            destinations.forEach { destination ->
                val active = selected == destination.screen
                NavigationBarItem(
                    selected = active,
                    onClick = { onNavigate(destination.screen) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ReEventGreenDeep,
                        selectedTextColor = ReEventGreenDeep,
                        indicatorColor = ReEventMintSoft,
                        unselectedIconColor = ReEventMuted,
                        unselectedTextColor = ReEventMuted
                    )
                )
            }
        }
    }
}

@Composable
fun PartnerLogoTile(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Image(
            painter = painterResource(R.drawable.partner_greencycle_logo),
            contentDescription = "GreenCycle partner logo",
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .height(84.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun QuickActionTile(
    title: String,
    detail: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = ReEventPaper,
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                Text(text = detail, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
            }
        }
    }
}

@Composable
fun UploadPreviewCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ReEventGreenDeep,
        border = BorderStroke(1.dp, ReEventGreenDeep.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ReEventWarm),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Camera",
                    tint = ReEventGreenDeep,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Add clear item photos",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Front, material label and any damaged areas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
fun LocationLine(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = ReEventMuted,
            modifier = Modifier.size(16.dp)
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = ReEventMuted)
    }
}

@Composable
fun SettingsRow(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Settings
) {
    QuickActionTile(
        title = title,
        detail = detail,
        icon = icon,
        color = ReEventGreen,
        onClick = onClick,
        modifier = modifier
    )
}

val WarmChartColors = listOf(ReEventGreen, ReEventWarm, ReEventBlue, ReEventCoral)
