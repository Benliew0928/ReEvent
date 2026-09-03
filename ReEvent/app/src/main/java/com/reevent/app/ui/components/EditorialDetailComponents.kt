package com.reevent.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reevent.app.R
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.HomeBodyFont
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeEditorialFont
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGreetingStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
import com.reevent.app.ui.theme.HomeSupportingTextStyle

/** Shared paper treatment for secondary screens without changing the global Material theme. */
@Composable
fun EditorialDetailScaffold(
    selected: TopLevelDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    showNavigation: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    ReEventScaffold(
        selected = selected,
        onNavigate = onNavigate,
        modifier = modifier,
        showBottomNavigation = showNavigation,
        navigationMode = if (showNavigation) {
            ReEventNavigationMode.TOP_LEVEL
        } else {
            ReEventNavigationMode.LARGE_SCREEN_ONLY
        },
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
            content(padding)
        }
    }
}

@Composable
fun EditorialDetailHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    profileName: String = "",
    onProfile: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            onBack?.let {
                EditorialIconButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    onClick = it,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(HomeForest, CircleShape),
                )
                Text(
                    text = eyebrow.uppercase(),
                    color = HomeMuted,
                    fontFamily = HomeBodyFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailing?.invoke()
            if (onProfile != null) {
                ProfileAvatarButton(displayName = profileName, onClick = onProfile)
            }
        }
        Text(
            text = title,
            style = HomeGreetingStyle.copy(fontSize = 42.sp, lineHeight = 43.sp),
            color = HomeInk,
        )
        Text(
            text = subtitle,
            style = HomeSupportingTextStyle.copy(fontSize = 16.sp, lineHeight = 22.sp),
            color = HomeMuted,
        )
    }
}

@Composable
fun EditorialIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = HomePaper,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(15.dp),
        color = containerColor,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, contentDescription = contentDescription, tint = HomeForest)
        }
    }
}

@Composable
fun EditorialSectionCard(
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (featured) 24.dp else 18.dp),
        color = if (featured) HomeSage else HomePaper,
        border = BorderStroke(1.dp, if (featured) HomeForest.copy(alpha = 0.12f) else HomeLine),
        tonalElevation = 0.dp,
        content = content,
    )
}

@Composable
fun EditorialEmptyState(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    EditorialSectionCard(modifier = modifier, featured = true) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(shape = CircleShape, color = HomePaper.copy(alpha = 0.82f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HomeForest,
                    modifier = Modifier.padding(12.dp).size(26.dp),
                )
            }
            Text(title, style = HomeCardTitleStyle.copy(fontSize = 25.sp), color = HomeInk)
            Text(detail, style = HomeBodyStyle, color = HomeMuted)
            if (actionLabel != null && onAction != null) {
                EditorialTextAction(label = actionLabel, onClick = onAction)
            }
        }
    }
}

@Composable
fun EditorialNotice(
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val container = if (isError) Color(0xFFFFE9E7) else HomeMist
    val content = if (isError) Color(0xFF8A2836) else HomeInk
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(1.dp, if (isError) Color(0xFFE8B8BD) else HomeLine),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = if (isError) Icons.Outlined.WarningAmber else Icons.Outlined.Info,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(20.dp),
            )
            Text(message, style = HomeSupportingTextStyle, color = content, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EditorialTextAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ChevronRight,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = HomeForest,
                fontFamily = HomeBodyFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
            Icon(icon, contentDescription = null, tint = HomeForest, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun EditorialExpandableCard(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    summary: @Composable () -> Unit,
    detail: @Composable () -> Unit,
) {
    EditorialSectionCard(modifier = modifier.animateContentSize()) {
        Column {
            summary()
            AnimatedVisibility(visible = expanded) { detail() }
        }
    }
}

@Composable
fun EditorialStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value,
            color = HomeInk,
            fontFamily = HomeEditorialFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 29.sp,
        )
        Text(label, color = HomeMuted, style = HomeSupportingTextStyle)
    }
}

@Composable
fun EditorialConfirmationDialog(
    title: String,
    detail: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Cancel",
    destructive: Boolean = false,
    showDismiss: Boolean = true,
) {
    val actionColor = if (destructive) Color(0xFF8A2836) else HomeForest
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                style = HomeCardTitleStyle.copy(fontSize = 28.sp, lineHeight = 30.sp),
                color = HomeInk,
            )
        },
        text = { Text(detail, style = HomeBodyStyle, color = HomeMuted) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = actionColor, fontFamily = HomeBodyFont, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (showDismiss) {
                TextButton(onClick = onDismiss) {
                    Text(dismissLabel, color = HomeMuted, fontFamily = HomeBodyFont, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = HomePaper,
        shape = RoundedCornerShape(24.dp),
    )
}
