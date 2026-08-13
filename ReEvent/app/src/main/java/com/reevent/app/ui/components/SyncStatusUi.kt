package com.reevent.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.reevent.app.core.data.SyncCommandStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMint
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun SyncStateChip(syncState: SyncState) {
    val (label, color) =
        when (syncState) {
            SyncState.SYNCED -> "Synced" to ReEventGreen
            SyncState.PENDING -> "Pending sync" to ReEventBlue
            SyncState.FAILED -> "Sync failed" to ReEventCoral
        }
    StatusChip(label, color)
}

/** Shows only actual account-scoped queue rows; an empty queue means the local projection is synced. */
@Composable
fun SyncQueueCard(
    commands: List<SyncCommandStatus>,
    retrying: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val failedCommands = commands.filter { it.syncState == SyncState.FAILED }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (failedCommands.isEmpty()) ReEventSurface else ReEventMint),
        border = BorderStroke(1.dp, ReEventLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (commands.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ReEventGreenDeep)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("All changes synced", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                        Text(
                            "This account has no queued changes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReEventTextSecondary,
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (failedCommands.isEmpty()) Icons.Outlined.Sync else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = if (failedCommands.isEmpty()) ReEventBlue else ReEventCoral,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (failedCommands.isEmpty()) "${commands.size} change${if (commands.size == 1) "" else "s"} waiting to sync" else "${failedCommands.size} change${if (failedCommands.size == 1) "" else "s"} need attention",
                            style = MaterialTheme.typography.titleSmall,
                            color = ReEventInk,
                        )
                        Text(
                            "Queue steps run in the order shown for this account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ReEventTextSecondary,
                        )
                    }
                }
                commands.forEachIndexed { index, command ->
                    if (index > 0) HorizontalDivider(color = ReEventLine)
                    SyncCommandRow(command)
                }
                if (failedCommands.isNotEmpty()) {
                    Button(
                        onClick = onRetry,
                        enabled = !retrying,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ReEventGreenDeep, contentColor = Color.White),
                    ) { Text(if (retrying) "Requesting retry…" else "Retry failed changes") }
                }
            }
        }
    }
}

@Composable
private fun SyncCommandRow(command: SyncCommandStatus) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${command.queuePosition}.", style = MaterialTheme.typography.labelLarge, color = ReEventTextSecondary)
            Text(
                command.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventInk,
                fontWeight = FontWeight.Medium,
            )
            SyncStateChip(command.syncState)
        }
        Text(
            command.detail,
            style = MaterialTheme.typography.bodySmall,
            color = ReEventTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (command.attempts > 0) {
            Text(
                "Attempted ${command.attempts} time${if (command.attempts == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = ReEventTextSecondary,
            )
        }
        command.lastError?.takeIf(String::isNotBlank)?.let { error ->
            Text(
                "Last error: $error",
                style = MaterialTheme.typography.bodySmall,
                color = ReEventCoral,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
