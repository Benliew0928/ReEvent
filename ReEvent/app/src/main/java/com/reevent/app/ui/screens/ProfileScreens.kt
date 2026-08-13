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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Email
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
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMint
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
    val authState by viewModel.state.collectAsState()
    val syncCommands by syncViewModel.pendingSyncCommands().collectAsState(emptyList())
    val syncAction by syncViewModel.action.collectAsState()

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

    ReEventScaffold(selected = TopLevelDestination.ACCOUNT, onNavigate = onNavigate) { padding ->
        AccountScaffold(
            eyebrow = "YOUR ACCOUNT",
            title = "Account & workspace",
            subtitle = "Review your protected workspace, support options and account security.",
            onBack = onBack,
            modifier = Modifier.padding(padding),
        ) {
            AccountCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Avatar(user.displayName)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(user.displayName, style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                        Text(
                            "Signed in as ${user.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ReEventTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                HorizontalDivider(color = ReEventLine)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(ReEventMint),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = ReEventGreenDeep) }
                    Column {
                        Text("Protected role", style = MaterialTheme.typography.labelLarge, color = ReEventTextSecondary)
                        Text(
                            "${roleLabel(requireNotNull(user.role))} workspace",
                            style = MaterialTheme.typography.titleMedium,
                            color = ReEventInk,
                        )
                    }
                }
            }

            ProfileSectionLabel("Account data")
            AccountCard {
                Text("What is stored", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                Text(
                    "Your name, email and selected role keep this workspace separated. Your events, resources, requests and authorised transaction history are stored for the circular-event workflow.",
                    color = ReEventTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Passport QR codes do not show your email, account ID or private notes.",
                    color = ReEventGreenDeep,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }

            ProfileSectionLabel("Security")
            ProfileActionCard(
                icon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = ReEventGreenDeep) },
                title = "Reset password",
                description = "Send a secure reset link to ${user.email}.",
                onClick = {
                    viewModel.clearFeedback()
                    passwordResetMode = true
                },
            )

            ProfileSectionLabel("Sync status")
            SyncQueueCard(
                commands = syncCommands,
                retrying = syncAction.loading,
                onRetry = syncViewModel::retryPendingSync,
            )
            if (syncAction.error != null) {
                Text(
                    "The retry could not be scheduled. Check your connection and try again.",
                    color = ReEventCoral,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            ProfileSectionLabel("Help & privacy")
            AccountCard {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Email, contentDescription = null, tint = ReEventGreenDeep)
                    Text("Need support?", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                }
                Text(
                    "For this assignment build, contact the ReEvent project team through your course or team support channel. Include your account email, device details and a screenshot. Never send a password or reset link.",
                    color = ReEventTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider(color = ReEventLine)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = ReEventGreenDeep)
                    Text(
                        "This demo stores only the account and workflow data described above. ReCoins are assignment-only points with no cash value.",
                        color = ReEventTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            ProfileSectionLabel("Account removal")
            ProfileActionCard(
                icon = { Icon(Icons.Outlined.Info, contentDescription = null, tint = ReEventCoral) },
                title = "Delete account",
                description = "Re-authenticate, remove private media, then permanently sign out. Active work is protected.",
                onClick = {
                    viewModel.clearFeedback()
                    accountDeletionVisible = true
                },
            )

            Text(
                text = "For protection of people, events and partner data, role changes are handled by your organisation administrator.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                onClick = viewModel::signOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = BorderStroke(1.dp, ReEventCoral),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ReEventCoral),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sign out")
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = ReEventGreen,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ProfileActionCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ReEventLine),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(ReEventMint),
                contentAlignment = Alignment.Center,
            ) { icon() }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = ReEventInk)
                Text(description, style = MaterialTheme.typography.bodySmall, color = ReEventTextSecondary)
            }
        }
    }
}

@Composable
private fun AccountDeletionDialog(
    email: String,
    state: AuthUiState,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
) {
    // Do not use rememberSaveable for either input: the current password must never enter saved
    // instance state, and the destructive confirmation should disappear when this dialog closes.
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
