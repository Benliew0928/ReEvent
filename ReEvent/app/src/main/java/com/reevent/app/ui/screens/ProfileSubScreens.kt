package com.reevent.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.reevent.app.core.model.User
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun PersonalInfoDialog(
    user: User,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(user.displayName) }
    var phone by rememberSaveable { mutableStateOf("+60 12-345 6789") }
    var savedMessage by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DialogHeader(title = "Personal Information", onDismiss = onDismiss)

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        savedMessage = false
                    },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HomeForest,
                        focusedLabelColor = HomeForest,
                    ),
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        savedMessage = false
                    },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HomeForest,
                        focusedLabelColor = HomeForest,
                    ),
                )

                if (savedMessage) {
                    Text(
                        text = "Personal information updated successfully!",
                        style = MaterialTheme.typography.bodySmall,
                        color = HomeForest,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Button(
                    onClick = { savedMessage = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccountInfoDialog(
    user: User,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DialogHeader(title = "Account Information", onDismiss = onDismiss)

                InfoRow(label = "Account Role", value = "${roleLabel(requireNotNull(user.role))} Workspace")
                HorizontalDivider(color = ReEventLine)
                InfoRow(label = "Registered Email", value = user.email)
                HorizontalDivider(color = ReEventLine)
                InfoRow(label = "Account ID", value = user.id)
                HorizontalDivider(color = ReEventLine)
                InfoRow(label = "Status", value = "Verified Active")

                Spacer(Modifier.height(4.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Close", color = HomeForest, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccountSecurityDialog(
    user: User,
    onResetPassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DialogHeader(title = "Account Security", onDismiss = onDismiss)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HomeMist,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Outlined.Security, contentDescription = null, tint = HomeForest)
                        Column {
                            Text("Security Status", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                            Text("Password and auth token protected", style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
                        }
                    }
                }

                Button(
                    onClick = {
                        onDismiss()
                        onResetPassword()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
                ) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reset Password", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onDeleteAccount()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ReEventCoral),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ReEventCoral),
                ) {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PushNotificationDialog(
    onDismiss: () -> Unit,
) {
    var eventUpdates by rememberSaveable { mutableStateOf(true) }
    var handovers by rememberSaveable { mutableStateOf(true) }
    var matchAlerts by rememberSaveable { mutableStateOf(true) }
    var allMuted by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DialogHeader(title = "Push Notifications", onDismiss = onDismiss)

                ToggleRow(
                    title = "Event updates",
                    subtitle = "Notifications for event milestones & changes",
                    checked = !allMuted && eventUpdates,
                    onCheckedChange = {
                        eventUpdates = it
                        if (it) allMuted = false
                    },
                )
                HorizontalDivider(color = ReEventLine)
                ToggleRow(
                    title = "Resource handovers",
                    subtitle = "Alerts when items are scanned or handed over",
                    checked = !allMuted && handovers,
                    onCheckedChange = {
                        handovers = it
                        if (it) allMuted = false
                    },
                )
                HorizontalDivider(color = ReEventLine)
                ToggleRow(
                    title = "Match alerts",
                    subtitle = "Alerts when partner matches are found",
                    checked = !allMuted && matchAlerts,
                    onCheckedChange = {
                        matchAlerts = it
                        if (it) allMuted = false
                    },
                )

                Spacer(Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        allMuted = !allMuted
                        if (allMuted) {
                            eventUpdates = false
                            handovers = false
                            matchAlerts = false
                        } else {
                            eventUpdates = true
                            handovers = true
                            matchAlerts = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (allMuted) HomeForest else ReEventCoral,
                    ),
                ) {
                    Text(if (allMuted) "Unmute all notifications" else "Disable all push notifications", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmailNotificationDialog(
    onDismiss: () -> Unit,
) {
    var receiveMarketing by rememberSaveable { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DialogHeader(title = "Email Notifications", onDismiss = onDismiss)

                ToggleRow(
                    title = "Marketing & Newsletters",
                    subtitle = "Receive circular economy updates, feature announcements and newsletters via email.",
                    checked = receiveMarketing,
                    onCheckedChange = { receiveMarketing = it },
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
                ) {
                    Text("Save Preference", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HelpDialog(
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DialogHeader(title = "Help & Support", onDismiss = onDismiss)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(HomeMist),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = null, tint = HomeForest)
                    }
                    Text("Need support?", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                }

                Text(
                    text = "For this assignment build, contact the ReEvent project team through your course or team support channel. Include your account email, device details and a screenshot. Never send a password or reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReEventTextSecondary,
                )

                HorizontalDivider(color = ReEventLine)

                Text(
                    text = "This demo stores only the account and workflow data required for circular event management. ReCoins are assignment-only points with no cash value.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ReEventTextSecondary,
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Got it", color = HomeForest, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = HomePaper,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("About ReEvent", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = ReEventInk)
                    }
                }

                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(HomeMist),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = HomeForest, modifier = Modifier.size(32.dp))
                }

                Text("ReEvent Platform", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
                Text("Version 1.0.0 (Build 2026)", style = MaterialTheme.typography.labelMedium, color = ReEventTextSecondary)

                Text(
                    text = "ReEvent is a circular economy mobile solution designed for sustainable event management. It facilitates event resource passporting, material reuse, repair matching, and waste diversion proof.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReEventTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Close", color = HomeForest, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Outlined.Close, contentDescription = "Close", tint = ReEventInk)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = ReEventTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = ReEventInk)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = HomeForest,
            ),
        )
    }
}
