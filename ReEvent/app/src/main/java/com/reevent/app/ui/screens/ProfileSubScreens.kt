package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.reevent.app.core.auth.AccountDeletionRules
import com.reevent.app.core.auth.AuthUiState
import com.reevent.app.core.model.User
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun PersonalInfoScreen(
    userDisplayName: String,
    phoneNumber: String?,
    gender: String?,
    onEditName: () -> Unit,
    onEditPhone: () -> Unit,
    onEditGender: () -> Unit,
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "Personal Information",
        subtitle = "View and update your personal identity details, phone number, and gender preferences.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = ReEventLine)

            PersonalInfoRow(
                label = "Full Name",
                value = userDisplayName,
                actionText = "Edit",
                onActionClick = onEditName,
            )

            HorizontalDivider(color = ReEventLine)

            PersonalInfoRow(
                label = "Phone Number",
                value = if (phoneNumber.isNullOrBlank()) "Not added" else phoneNumber,
                actionText = if (phoneNumber.isNullOrBlank()) "Add" else "Edit",
                onActionClick = onEditPhone,
            )

            HorizontalDivider(color = ReEventLine)

            PersonalInfoRow(
                label = "Gender",
                value = if (gender.isNullOrBlank()) "Not added" else gender,
                actionText = if (gender.isNullOrBlank()) "Add" else "Edit",
                onActionClick = onEditGender,
            )
        }
    }
}

@Composable
fun EditNameScreen(
    currentName: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(currentName) }

    AccountScaffold(
        headerTitle = "Edit Full Name",
        subtitle = "Update your profile display name.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HomeForest,
                    focusedLabelColor = HomeForest,
                ),
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onSave(name) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditPhoneScreen(
    currentPhone: String?,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
) {
    var phone by rememberSaveable { mutableStateOf(currentPhone ?: "") }
    val isAdd = currentPhone.isNullOrBlank()

    AccountScaffold(
        headerTitle = if (isAdd) "Add Phone Number" else "Edit Phone Number",
        subtitle = if (isAdd) "Add your mobile contact number to your profile." else "Update your mobile contact number.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HomeForest,
                    focusedLabelColor = HomeForest,
                ),
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onSave(phone) },
                enabled = phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
            ) {
                Text(if (isAdd) "Add Phone Number" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EditGenderScreen(
    currentGender: String?,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
) {
    val options = listOf("Male", "Women", "Private")
    var selected by rememberSaveable { mutableStateOf(currentGender ?: "Private") }
    val isAdd = currentGender.isNullOrBlank()

    AccountScaffold(
        headerTitle = if (isAdd) "Add Gender" else "Edit Gender",
        subtitle = "Select your gender preference.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = option }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selected == option) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = ReEventInk,
                    )
                    RadioButton(
                        selected = selected == option,
                        onClick = { selected = option },
                        colors = RadioButtonDefaults.colors(selectedColor = HomeForest),
                    )
                }
                if (index < options.size - 1) {
                    HorizontalDivider(color = ReEventLine)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onSave(selected) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
            ) {
                Text("Save Selection", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PersonalInfoRow(
    label: String,
    value: String,
    actionText: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (value == "Not added") ReEventTextSecondary else ReEventInk,
            )
        }
        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = HomeForest,
            )
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
        subtitle = "View your account role, registered email, and active workspace.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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

@Composable
fun AccountSecurityScreen(
    user: User,
    onResetPassword: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "Account Security",
        subtitle = "Manage your account authentication, password updates, and account deletion options.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = ReEventLine)

            SecurityNavigationRow(
                title = "Reset password",
                subtitle = "Request a password reset link sent to your registered email",
                onClick = onResetPassword,
            )

            HorizontalDivider(color = ReEventLine)

            SecurityNavigationRow(
                title = "Delete account",
                subtitle = "Permanently delete your ReEvent account and private data",
                onClick = onDeleteAccount,
            )
        }
    }
}

@Composable
fun DeleteAccountScreen(
    email: String,
    state: AuthUiState,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var confirmationPhrase by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val validation = AccountDeletionRules.validate(confirmationPhrase, currentPassword)

    AccountScaffold(
        headerTitle = "Delete Account",
        subtitle = "Permanently remove the sign-in for $email and clear private media stored under this account. This action cannot be undone.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                "Completed workflow history may be retained with your account identity de-identified. You cannot delete while you have active transactions, resources, listings, programmes, or unsettled holds.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )

            AccountTextField(
                value = confirmationPhrase,
                onValueChange = {
                    confirmationPhrase = it
                    submitted = false
                },
                label = "Type DELETE MY ACCOUNT",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                isError = submitted && validation.confirmationError != null,
                supportingText = if (submitted) validation.confirmationError else null,
            )

            AccountTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    submitted = false
                },
                label = "Current password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        )
                    }
                },
                isError = submitted && validation.passwordError != null,
                supportingText = if (submitted) validation.passwordError else null,
            )

            state.accountDeletionBlocked?.let { blocked ->
                Text(blocked.userMessage, color = ReEventCoral, style = MaterialTheme.typography.bodySmall)
            }
            if (state.accountDeletionReauthenticationRequired) {
                Text(
                    "That password did not re-authenticate this account.",
                    color = ReEventCoral,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (state.passwordReauthenticationUnavailable) {
                Text(
                    "Password re-authentication is unavailable for this sign-in provider. Contact the ReEvent project team for account removal.",
                    color = ReEventCoral,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (state.error != null) {
                Text(errorText(state.error), color = ReEventCoral, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    submitted = true
                    if (validation.isValid) onSubmit(currentPassword)
                },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReEventCoral, contentColor = Color.White),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Deleting…")
                } else {
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
        subtitle = "Customize your notifications below. Receive updates, alerts, and more. Don't worry, you can change these at any time.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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
            HorizontalDivider(color = ReEventLine)
            ToggleRow(
                title = "Do not receive any notifications",
                subtitle = "Mute all push notification alerts and updates",
                checked = allMuted,
                onCheckedChange = { muted ->
                    allMuted = muted
                    if (muted) {
                        eventUpdates = false
                        handovers = false
                        matchAlerts = false
                    } else {
                        eventUpdates = true
                        handovers = true
                        matchAlerts = true
                    }
                },
            )
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
        subtitle = "Manage newsletter and marketing email preferences.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            ToggleRow(
                title = "Marketing & Newsletters",
                subtitle = "Receive circular economy updates, feature announcements and newsletters via email.",
                checked = receiveMarketing,
                onCheckedChange = { receiveMarketing = it },
            )

            HorizontalDivider(color = ReEventLine)
            Spacer(Modifier.height(8.dp))

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

@Composable
fun HelpScreen(
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "Help",
        subtitle = "Instructions and support contacts for ReEvent users.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    AccountScaffold(
        headerTitle = "About",
        subtitle = "Circular economy mobile solution for sustainable event management.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text("ReEvent Platform", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
            Text("Version 1.0.0 (Build 2026)", style = MaterialTheme.typography.labelMedium, color = ReEventTextSecondary)
            HorizontalDivider(color = ReEventLine)
            Text(
                text = "ReEvent is a circular economy mobile solution designed for sustainable event management. It facilitates event resource passporting, material reuse, repair matching, and waste diversion proof.",
                style = MaterialTheme.typography.bodyMedium,
                color = ReEventTextSecondary,
            )
        }
    }
}

@Composable
private fun SecurityNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ReEventTextSecondary,
            modifier = Modifier.size(20.dp),
        )
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
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ReEventInk)
            Spacer(Modifier.height(2.dp))
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
