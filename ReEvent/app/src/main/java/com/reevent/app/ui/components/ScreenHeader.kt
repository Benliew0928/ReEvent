package com.reevent.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onProfile: (() -> Unit)? = null,
    profileName: String = "",
    onNotificationClick: (() -> Unit)? = null,
) {
    var showNotificationDialog by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            SoftIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
            )
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ReEventInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onProfile != null) {
            Spacer(Modifier.width(12.dp))
            SoftIconButton(
                icon = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                onClick = {
                    if (onNotificationClick != null) {
                        onNotificationClick()
                    } else {
                        showNotificationDialog = true
                    }
                },
            )
            Spacer(Modifier.width(8.dp))
            ProfileAvatarButton(displayName = profileName, onClick = onProfile)
        }
    }

    if (showNotificationDialog) {
        NotificationDialog(onDismiss = { showNotificationDialog = false })
    }
}

@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ReEventSurface)
                .border(1.dp, ReEventLine, RoundedCornerShape(14.dp)),
        colors = IconButtonDefaults.iconButtonColors(contentColor = ReEventGreenDeep),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
