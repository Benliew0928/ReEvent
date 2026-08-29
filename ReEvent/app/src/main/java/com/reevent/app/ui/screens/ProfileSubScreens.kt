package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.reevent.app.core.model.User
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun PersonalInfoScreen(
    user: User,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(user.displayName) }
    var phone by rememberSaveable { mutableStateOf("+60 12-345 6789") }
    var savedMessage by remember { mutableStateOf(false) }

    AccountScaffold(
        headerTitle = "Personal Information",
        title = "Personal Information",
        subtitle = "Update your full name and contact phone number.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        savedMessage = false
                    },
                    label = { Text("Full Name") },
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
fun AccountInfoScreen(
    user: User,
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "Account Information",
        title = "Account Information",
        subtitle = "View your account role, registered email, and active workspace.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoRow(label = "Account Role", value = "${roleLabel(requireNotNull(user.role))} Workspace")
                HorizontalDivider(color = ReEventLine)
                InfoRow(label = "Registered Email", value = user.email)
                HorizontalDivider(color = ReEventLine)
                InfoRow(label = "Account ID", value = user.id)
                HorizontalDivider(color = ReEventLine)
                InfoRow(label = "Status", value = "Verified Active")
            }
        }
    }
}

@Composable
fun AccountSecurityScreen(
    user: User,
    onResetPassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "Account Security",
        title = "Account Security",
        subtitle = "Manage your account authentication and security settings.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HomeMist,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Security Status", style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                        Text("Password and auth token protected", style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
                    }
                }

                Button(
                    onClick = onResetPassword,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
                ) {
                    Text("Reset Password", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDeleteAccount,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ReEventCoral),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ReEventCoral),
                ) {
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PushNotificationScreen(
    onBack: () -> Unit,
) {
    var eventUpdates by rememberSaveable { mutableStateOf(true) }
    var handovers by rememberSaveable { mutableStateOf(true) }
    var matchAlerts by rememberSaveable { mutableStateOf(true) }
    var allMuted by rememberSaveable { mutableStateOf(false) }

    AccountScaffold(
        headerTitle = "Push Notification",
        title = "Push Notification",
        subtitle = "Configure push notification categories and instant alerts.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
fun EmailNotificationScreen(
    onBack: () -> Unit,
) {
    var receiveMarketing by rememberSaveable { mutableStateOf(false) }

    AccountScaffold(
        headerTitle = "Email Notification",
        title = "Email Notification",
        subtitle = "Manage newsletter and marketing email preferences.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ToggleRow(
                    title = "Marketing & Newsletters",
                    subtitle = "Receive circular economy updates, feature announcements and newsletters via email.",
                    checked = receiveMarketing,
                    onCheckedChange = { receiveMarketing = it },
                )

                Button(
                    onClick = onBack,
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
fun HelpScreen(
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "Help",
        title = "Help",
        subtitle = "Instructions and support contacts for ReEvent users.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Need support?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
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
            }
        }
    }
}

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "About",
        title = "About",
        subtitle = "Circular economy mobile solution for sustainable event management.",
        onBack = onBack,
    ) {
        AccountCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("ReEvent Platform", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
                Text("Version 1.0.0 (Build 2026)", style = MaterialTheme.typography.labelMedium, color = ReEventTextSecondary)
                Text(
                    text = "ReEvent is a circular economy mobile solution designed for sustainable event management. It facilitates event resource passporting, material reuse, repair matching, and waste diversion proof.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReEventTextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
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
