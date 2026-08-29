package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.auth.AccountDeletionRules
import com.reevent.app.core.auth.AuthUiState
import com.reevent.app.core.auth.AuthViewModel
import com.reevent.app.core.model.User
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SyncQueueCard
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun ProfileFlowScreen(
    user: User,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    syncViewModel: FeatureViewModel = hiltViewModel(),
) {
    var passwordResetMode by rememberSaveable { mutableStateOf(false) }
    var accountDeletionVisible by rememberSaveable { mutableStateOf(false) }

    var showPersonalInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showAccountInfoDialog by rememberSaveable { mutableStateOf(false) }
    var showSecurityDialog by rememberSaveable { mutableStateOf(false) }
    var showPushDialog by rememberSaveable { mutableStateOf(false) }
    var showEmailDialog by rememberSaveable { mutableStateOf(false) }
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }

    val authState by viewModel.state.collectAsState()
    val syncCommands by syncViewModel.pendingSyncCommands().collectAsState(emptyList())

    if (passwordResetMode) {
        PasswordResetRequestFlow(
            initialEmail = user.email,
            backLabel = "Back to account",
            onBack = {
                viewModel.clearFeedback()
                passwordResetMode = false
            },
            viewModel = viewModel,
        )
        return
    }

    if (accountDeletionVisible) {
        AccountDeletionDialog(
            email = user.email,
            state = authState,
            onDismiss = { accountDeletionVisible = false },
            onSubmit = viewModel::deleteAccount,
        )
    }

    if (showPersonalInfoDialog) {
        PersonalInfoDialog(user = user, onDismiss = { showPersonalInfoDialog = false })
    }
    if (showAccountInfoDialog) {
        AccountInfoDialog(user = user, onDismiss = { showAccountInfoDialog = false })
    }
    if (showSecurityDialog) {
        AccountSecurityDialog(
            user = user,
            onResetPassword = {
                viewModel.clearFeedback()
                passwordResetMode = true
            },
            onDeleteAccount = {
                viewModel.clearFeedback()
                accountDeletionVisible = true
            },
            onDismiss = { showSecurityDialog = false },
        )
    }
    if (showPushDialog) {
        PushNotificationDialog(onDismiss = { showPushDialog = false })
    }
    if (showEmailDialog) {
        EmailNotificationDialog(onDismiss = { showEmailDialog = false })
    }
    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    ReEventScaffold(selected = TopLevelDestination.ACCOUNT, onNavigate = onNavigate) { padding ->
        AccountScaffold(
            eyebrow = "ACCOUNT SETTINGS",
            title = "Profile",
            subtitle = "Manage your personal details, preferences, and account security.",
            onBack = onBack,
            modifier = Modifier.padding(padding),
        ) {
            // User Identity Header Card
            AccountCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Avatar(user.displayName)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ReEventInk,
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Section 1: Account Settings
            ProfileSectionLabel("Account Settings")
            AccountCard {
                ProfileNavigationRow(
                    title = "Personal Information",
                    onClick = { showPersonalInfoDialog = true },
                )
                HorizontalDivider(color = ReEventLine)
                ProfileNavigationRow(
                    title = "Account Information",
                    onClick = { showAccountInfoDialog = true },
                )
                HorizontalDivider(color = ReEventLine)
                ProfileNavigationRow(
                    title = "Account Security",
                    onClick = { showSecurityDialog = true },
                )
            }

            // Section 2: Preferences
            ProfileSectionLabel("Preferences")
            AccountCard {
                ProfileNavigationRow(
                    title = "Push Notification",
                    onClick = { showPushDialog = true },
                )
                HorizontalDivider(color = ReEventLine)
                ProfileNavigationRow(
                    title = "Email Notification",
                    onClick = { showEmailDialog = true },
                )
            }

            // Section 3: Support
            ProfileSectionLabel("Support")
            AccountCard {
                ProfileNavigationRow(
                    title = "Help",
                    onClick = { showHelpDialog = true },
                )
                HorizontalDivider(color = ReEventLine)
                ProfileNavigationRow(
                    title = "About",
                    onClick = { showAboutDialog = true },
                )
            }

            if (syncCommands.isNotEmpty()) {
                ProfileSectionLabel("Sync Queue")
                SyncQueueCard(
                    commands = syncCommands,
                    retrying = syncViewModel.action.collectAsState().value.loading,
                    onRetry = syncViewModel::retryPendingSync,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = viewModel::signOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.5.dp, ReEventCoral),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ReEventCoral),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = ReEventInk,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun ProfileNavigationRow(
    title: String,
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
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = ReEventInk,
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = ReEventTextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AccountDeletionDialog(
    email: String,
    state: AuthUiState,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    var confirmationPhrase by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val validation = AccountDeletionRules.validate(confirmationPhrase, currentPassword)

    AlertDialog(
        onDismissRequest = { if (!state.loading) onDismiss() },
        title = { Text("Delete this account?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This permanently removes the sign-in for $email and clears private media stored under this account. It cannot be undone.",
                    color = ReEventTextSecondary,
                )
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
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
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
                    icon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
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
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.loading) { Text("Cancel") }
        },
        confirmButton = {
            Button(
                onClick = {
                    submitted = true
                    if (validation.isValid) onSubmit(currentPassword)
                },
                enabled = !state.loading,
                colors = ButtonDefaults.buttonColors(containerColor = ReEventCoral, contentColor = Color.White),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Deleting")
                } else {
                    Text("Delete account")
                }
            }
        },
    )
}
