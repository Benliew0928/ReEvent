package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.auth.AuthUiState
import com.reevent.app.core.auth.AuthViewModel
import com.reevent.app.core.auth.AccountDeletionRules
import com.reevent.app.core.auth.PasswordRules
import com.reevent.app.core.auth.SignUpFormValidation
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.ReEventScreen
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SyncQueueCard
import com.reevent.app.ui.theme.ReEventBackground
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventCoralSoft
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMint
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun OnboardingFlowScreen(onContinue: () -> Unit) {
    AccountScaffold(
        eyebrow = "REUSE WITH PURPOSE",
        title = "Circular events, ready for real work",
        subtitle = "A calm, secure place to keep materials moving from one event to the next."
    ) {
        CircularFeatureCard()
        PrimaryAccountButton(text = "Get started", loading = false, onClick = onContinue)
        Text(
            text = "Three workspaces. One trusted circular network.",
            modifier = Modifier.fillMaxWidth(),
            color = ReEventTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SignInFlowScreen(viewModel: AuthViewModel = hiltViewModel()) {
    var email by rememberSaveable { mutableStateOf("") }
    var passwordResetMode by rememberSaveable { mutableStateOf(false) }
    if (passwordResetMode) {
        PasswordResetRequestFlow(
            initialEmail = email,
            backLabel = "Back to sign in",
            onBack = {
                viewModel.clearFeedback()
                passwordResetMode = false
            },
            viewModel = viewModel
        )
        return
    }
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var registrationMode by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }
    var signUpSubmitted by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val submitLabel = if (registrationMode) "Create account" else "Sign in"
    val signUpValidation = SignUpFormValidation.validate(
        displayName = displayName,
        email = email,
        password = password,
        confirmation = passwordConfirmation
    )

    AccountScaffold(
        eyebrow = if (registrationMode) "YOUR CIRCULAR ACCOUNT" else "WELCOME BACK",
        title = if (registrationMode) "Create your account" else "Keep the good in motion",
        subtitle = if (registrationMode) {
            "Start with one role. Your data and permissions stay securely separated."
        } else {
            "Sign in to continue the circular flow of your events."
        }
    ) {
        AccountCard {
            if (registrationMode) {
                AccountTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        signUpSubmitted = false
                    },
                    label = "Your name",
                    icon = { Icon(Icons.Outlined.Person, null) },
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    isError = signUpSubmitted && signUpValidation.nameError != null,
                    supportingText = if (signUpSubmitted) signUpValidation.nameError else null
                )
            }
            AccountTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (registrationMode) signUpSubmitted = false
                },
                label = "Email address",
                icon = { Icon(Icons.Outlined.Email, null) },
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = registrationMode && signUpSubmitted && signUpValidation.emailError != null,
                supportingText = if (registrationMode && signUpSubmitted) signUpValidation.emailError else null
            )
            AccountTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (registrationMode) signUpSubmitted = false
                },
                label = "Password",
                icon = { Icon(Icons.Outlined.Lock, null) },
                keyboardType = KeyboardType.Password,
                imeAction = if (registrationMode) ImeAction.Next else ImeAction.Done,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                isError = registrationMode && signUpSubmitted && signUpValidation.passwordError != null,
                supportingText = if (registrationMode && signUpSubmitted) {
                    signUpValidation.passwordError
                } else if (registrationMode) {
                    "Use at least 8 characters."
                } else {
                    null
                }
            )
            if (registrationMode) {
                AccountTextField(
                    value = passwordConfirmation,
                    onValueChange = {
                        passwordConfirmation = it
                        signUpSubmitted = false
                    },
                    label = "Confirm password",
                    icon = { Icon(Icons.Outlined.Lock, null) },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (confirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmationVisible = !confirmationVisible }) {
                            Icon(
                                imageVector = if (confirmationVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (confirmationVisible) "Hide password confirmation" else "Show password confirmation"
                            )
                        }
                    },
                    isError = signUpSubmitted && signUpValidation.confirmationError != null,
                    supportingText = if (signUpSubmitted) signUpValidation.confirmationError else null
                )
            }
            PrimaryAccountButton(
                text = submitLabel,
                loading = state.loading,
                onClick = {
                    if (registrationMode) {
                        signUpSubmitted = true
                        if (signUpValidation.isValid) {
                            viewModel.signUp(email.trim(), password, displayName.trim())
                        }
                    } else {
                        viewModel.signIn(email, password)
                    }
                }
            )
            if (!registrationMode) {
                TextButton(
                    onClick = {
                        viewModel.clearFeedback()
                        passwordResetMode = true
                    },
                    enabled = !state.loading,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Forgot password?") }
            }
        }

        if (!registrationMode) {
            AuthDivider()
            GoogleAccountButton(loading = state.loading, onClick = viewModel::signInWithGoogle)
        }

        AccountMessage(state)
        if (state.confirmationRequired && registrationMode) {
            EmailConfirmationCard(
                email = state.confirmationEmail ?: email,
                loading = state.loading,
                onResend = { viewModel.resendSignUpConfirmation(state.confirmationEmail ?: email) },
                onSignIn = {
                    registrationMode = false
                    password = ""
                    passwordConfirmation = ""
                    signUpSubmitted = false
                }
            )
        }

        TextButton(
            onClick = {
                registrationMode = !registrationMode
                signUpSubmitted = false
                viewModel.clearFeedback()
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (registrationMode) "Already have an account? Sign in" else "New to ReEvent? Create an account")
        }
    }
}

@Composable
private fun PasswordResetRequestFlow(
    initialEmail: String,
    backLabel: String,
    onBack: () -> Unit,
    viewModel: AuthViewModel
) {
    var email by rememberSaveable(initialEmail) { mutableStateOf(initialEmail) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val emailError = submitted && !email.isPlausibleEmail()

    AccountScaffold(
        eyebrow = "ACCOUNT RECOVERY",
        title = "Reset your password",
        subtitle = "Enter your email and we will send a secure link if it belongs to a ReEvent account."
    ) {
        if (state.resetRequested) {
            PasswordResetEmailSentCard(
                email = email.trim(),
                loading = state.loading,
                onResend = { viewModel.requestPasswordReset(email) },
                onUseDifferentEmail = {
                    viewModel.clearFeedback()
                    email = ""
                    submitted = false
                },
                onBack = onBack,
                backLabel = backLabel
            )
        } else {
            AccountCard {
                AccountTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (submitted) submitted = false
                    },
                    label = "Email address",
                    icon = { Icon(Icons.Outlined.Email, null) },
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    isError = emailError,
                    supportingText = if (emailError) "Enter a valid email address." else "We never reveal whether an account exists."
                )
                PrimaryAccountButton("Send reset link", state.loading) {
                    submitted = true
                    if (email.isPlausibleEmail()) viewModel.requestPasswordReset(email)
                }
            }
            if (state.error != null) AccountMessage(state)
            TextButton(onClick = onBack, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                Text(backLabel)
            }
        }
    }
}

@Composable
fun PasswordRecoveryFlowScreen(viewModel: AuthViewModel = hiltViewModel()) {
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val passwordError = submitted && !PasswordRules.isValid(password)
    val confirmationError = submitted && !PasswordRules.matchesConfirmation(password, confirmation)

    AccountScaffold(
        eyebrow = "SECURE PASSWORD RECOVERY",
        title = if (state.passwordUpdated) "Password updated" else "Choose a new password",
        subtitle = if (state.passwordUpdated) {
            "Your ReEvent password has been changed successfully."
        } else {
            "This protected screen was opened from your recovery email. Choose a new password before continuing."
        }
    ) {
        if (state.passwordUpdated) {
            PasswordUpdatedCard(onContinue = viewModel::finishPasswordRecovery)
        } else {
            AccountCard {
                AccountTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (submitted) submitted = false
                    },
                    label = "New password",
                    icon = { Icon(Icons.Outlined.Lock, null) },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (passwordVisible) "Hide password" else "Show password")
                        }
                    },
                    isError = passwordError,
                    supportingText = "Use at least ${PasswordRules.MIN_LENGTH} characters."
                )
                AccountTextField(
                    value = confirmation,
                    onValueChange = {
                        confirmation = it
                        if (submitted) submitted = false
                    },
                    label = "Confirm new password",
                    icon = { Icon(Icons.Outlined.Lock, null) },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (confirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmationVisible = !confirmationVisible }) {
                            Icon(if (confirmationVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (confirmationVisible) "Hide password" else "Show password")
                        }
                    },
                    isError = confirmationError,
                    supportingText = if (confirmationError) "Passwords do not match." else null
                )
                PrimaryAccountButton("Update password", state.loading) {
                    submitted = true
                    if (PasswordRules.isValid(password) && PasswordRules.matchesConfirmation(password, confirmation)) {
                        viewModel.updatePassword(password)
                    }
                }
            }
            PasswordRecoveryError(state)
            TextButton(
                onClick = viewModel::signOut,
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancel recovery and sign in instead") }
        }
    }
}

@Composable
fun CompleteRoleFlowScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    AccountScaffold(
        eyebrow = "ONE FINAL STEP",
        title = "Choose your workspace",
        subtitle = "This controls what you can see and do. It is permanent in the mobile app, so choose the role that matches your work."
    ) {
        RoleOption(
            role = UserRole.ORGANIZER,
            icon = { Icon(Icons.Outlined.Apartment, null) },
            title = "Organiser",
            description = "Plan events, list resources, match partners and measure impact.",
            enabled = !state.loading,
            onClick = { viewModel.completeRole(UserRole.ORGANIZER) }
        )
        RoleOption(
            role = UserRole.PARTICIPANT,
            icon = { Icon(Icons.Outlined.Groups, null) },
            title = "Participant",
            description = "Return items, browse available resources and follow your exchanges.",
            enabled = !state.loading,
            onClick = { viewModel.completeRole(UserRole.PARTICIPANT) }
        )
        RoleOption(
            role = UserRole.PARTNER,
            icon = { Icon(Icons.Outlined.BusinessCenter, null) },
            title = "Circular partner",
            description = "Manage programmes and complete handovers assigned to your organisation.",
            enabled = !state.loading,
            onClick = { viewModel.completeRole(UserRole.PARTNER) }
        )
        AccountMessage(state)
    }
}

@Composable
fun ProfileFlowScreen(
    user: User,
    onBack: () -> Unit,
    onNavigate: (ReEventScreen) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    syncViewModel: FeatureViewModel = hiltViewModel()
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
            viewModel = viewModel
        )
        return
    }

    if (accountDeletionVisible) {
        AccountDeletionDialog(
            email = user.email,
            state = authState,
            onDismiss = { accountDeletionVisible = false },
            onSubmit = viewModel::deleteAccount
        )
    }

    ReEventScaffold(selected = ReEventScreen.Profile, onNavigate = onNavigate) { padding ->
        AccountScaffold(
            eyebrow = "YOUR ACCOUNT",
            title = "Account & workspace",
            subtitle = "Review your protected workspace, support options and account security.",
            onBack = onBack,
            modifier = Modifier.padding(padding)
        ) {
            AccountCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Avatar(user.displayName)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(user.displayName, style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                        Text("Signed in as ${user.email}", style = MaterialTheme.typography.bodyMedium, color = ReEventTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                HorizontalDivider(color = ReEventLine)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(ReEventMint),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = ReEventGreenDeep) }
                    Column {
                        Text("Protected role", style = MaterialTheme.typography.labelLarge, color = ReEventTextSecondary)
                        Text("${roleLabel(requireNotNull(user.role))} workspace", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                    }
                }
            }

            ProfileSectionLabel("Account data")
            AccountCard {
                Text("What is stored", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                Text(
                    "Your name, email and selected role keep this workspace separated. Your events, resources, requests and authorised transaction history are stored for the circular-event workflow.",
                    color = ReEventTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Passport QR codes do not show your email, account ID or private notes.",
                    color = ReEventGreenDeep,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
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
                }
            )

            ProfileSectionLabel("Sync status")
            SyncQueueCard(
                commands = syncCommands,
                retrying = syncAction.loading,
                onRetry = syncViewModel::retryPendingSync
            )
            if (syncAction.error != null) {
                Text(
                    "The retry could not be scheduled. Check your connection and try again.",
                    color = ReEventCoral,
                    style = MaterialTheme.typography.bodySmall
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
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(color = ReEventLine)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = ReEventGreenDeep)
                    Text(
                        "This demo stores only the account and workflow data described above. ReCoins are assignment-only points with no cash value.",
                        color = ReEventTextSecondary,
                        style = MaterialTheme.typography.bodySmall
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
                }
            )

            Text(
                text = "For protection of people, events and partner data, role changes are handled by your organisation administrator.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = viewModel::signOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                border = BorderStroke(1.dp, ReEventCoral),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ReEventCoral)
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
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ProfileActionCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(ReEventMint),
                contentAlignment = Alignment.Center
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
    onSubmit: (String) -> Unit
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
                    color = ReEventTextSecondary
                )
                Text(
                    "Completed workflow history may be retained with your account identity de-identified. You cannot delete while you have active transactions, resources, listings, programmes, or unsettled holds.",
                    color = ReEventTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                AccountTextField(
                    value = confirmationPhrase,
                    onValueChange = { confirmationPhrase = it; submitted = false },
                    label = "Type DELETE MY ACCOUNT",
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    isError = submitted && validation.confirmationError != null,
                    supportingText = if (submitted) validation.confirmationError else null
                )
                AccountTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; submitted = false },
                    label = "Current password",
                    icon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = submitted && validation.passwordError != null,
                    supportingText = if (submitted) validation.passwordError else null
                )
                state.accountDeletionBlocked?.let { blocked ->
                    Text(blocked.userMessage, color = ReEventCoral, style = MaterialTheme.typography.bodySmall)
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
                colors = ButtonDefaults.buttonColors(containerColor = ReEventCoral, contentColor = Color.White)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Deleting")
                } else Text("Delete account")
            }
        }
    )
}

@Composable
private fun AccountScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = ReEventBackground, modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 44.dp, y = 52.dp)
                    .size(172.dp)
                    .clip(CircleShape)
                    .background(ReEventMint.copy(alpha = 0.72f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.background(ReEventSurface, CircleShape)) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = ReEventInk)
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                    LogoMark(size = 56.dp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = ReEventGreen, fontWeight = FontWeight.Bold)
                    Text(title, style = MaterialTheme.typography.displaySmall, color = ReEventInk)
                    Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = ReEventTextSecondary)
                }
                content()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AccountCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ReEventLine)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content
        )
    }
}

@Composable
private fun AccountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = icon,
        trailingIcon = trailingIcon,
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        supportingText = supportingText?.let { { Text(it) } },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ReEventGreen,
            focusedLabelColor = ReEventGreen,
            cursorColor = ReEventGreen
        )
    )
}

@Composable
private fun PrimaryAccountButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !loading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ReEventGreen, contentColor = Color.White)
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Please wait")
        } else Text(text)
    }
}

@Composable
private fun GoogleAccountButton(loading: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !loading,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ReEventLine),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = ReEventSurface, contentColor = ReEventInk)
    ) {
        Text("G", style = MaterialTheme.typography.titleLarge, color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google")
    }
}

@Composable
private fun AuthDivider() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = ReEventLine)
        Text("or", color = ReEventTextSecondary, style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(modifier = Modifier.weight(1f), color = ReEventLine)
    }
}

@Composable
private fun EmailConfirmationCard(email: String, loading: Boolean, onResend: () -> Unit, onSignIn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventMint)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = ReEventGreenDeep)
                Text("Check your inbox", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            }
            Text("If this is a new address, confirm $email before signing in. If you already have a ReEvent account, sign in instead. For account security, we cannot disclose which case applies.", color = ReEventTextSecondary, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onSignIn, enabled = !loading) { Text("Go to sign in") }
                TextButton(onClick = onResend, enabled = !loading) { Text("Resend email") }
            }
        }
    }
}

@Composable
private fun PasswordResetEmailSentCard(
    email: String,
    loading: Boolean,
    onResend: () -> Unit,
    onUseDifferentEmail: () -> Unit,
    onBack: () -> Unit,
    backLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventMint)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = ReEventGreenDeep)
                Text("Check your inbox", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            }
            Text(
                "If $email has a ReEvent account, a password-reset link is on its way. Check spam too. The link opens a protected screen in this app.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "For security, this message is the same whether or not an account exists.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onUseDifferentEmail, enabled = !loading) { Text("Use another email") }
                TextButton(onClick = onResend, enabled = !loading) { Text("Resend link") }
            }
            TextButton(onClick = onBack, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text(backLabel)
            }
        }
    }
}

@Composable
private fun PasswordUpdatedCard(onContinue: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventMint)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ReEventGreenDeep)
                Text("Password changed", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            }
            Text(
                "Your new password is active. Continue to the workspace that belongs to this account.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            PrimaryAccountButton("Continue to ReEvent", loading = false, onClick = onContinue)
        }
    }
}

@Composable
private fun PasswordRecoveryError(state: AuthUiState) {
    val reason = state.error ?: return
    val message = when (reason) {
        FailureReason.UNAUTHENTICATED -> "This recovery link is invalid or has expired. Request a new password-reset email."
        FailureReason.VALIDATION -> "Choose a password with at least ${PasswordRules.MIN_LENGTH} characters and enter it the same way twice."
        else -> errorText(reason)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventCoralSoft)
    ) {
        Text(message, Modifier.padding(16.dp), color = ReEventCoral, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RoleOption(
    role: UserRole,
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventSurface),
        border = BorderStroke(1.dp, ReEventLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(roleColor(role)), contentAlignment = Alignment.Center) {
                icon()
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = ReEventTextSecondary)
            }
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ReEventGreen.copy(alpha = 0.55f))
        }
    }
}

@Composable
private fun CircularFeatureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventGreenDeep)
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(ReEventMint), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = ReEventGreenDeep)
            }
            Text("One resource can have many useful lives.", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Coordinate resources, handovers and impact without crossing workspace boundaries.", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.78f))
        }
    }
}

@Composable
private fun Avatar(name: String) {
    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(ReEventGreenDeep), contentAlignment = Alignment.Center) {
        Text(
            text = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "R" },
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

@Composable
private fun AccountMessage(state: AuthUiState) {
    val message = when {
        state.accountDeleted -> "Your account was deleted and this device has been signed out."
        state.resetRequested -> "Password-reset instructions were sent if this email has an account."
        state.confirmationResent -> "If confirmation is still required, a new email has been requested. Check your inbox and spam folder."
        state.error != null -> errorText(state.error)
        else -> null
    } ?: return
    val isError = state.error != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isError) ReEventCoralSoft else ReEventMint)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError) ReEventCoral else ReEventGreenDeep,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun roleColor(role: UserRole): Color = when (role) {
    UserRole.ORGANIZER -> ReEventMint
    UserRole.PARTICIPANT -> Color(0xFFE8F0FF)
    UserRole.PARTNER -> Color(0xFFFFF1D2)
}

private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private fun String.isPlausibleEmail(): Boolean = trim().length <= 254 && emailPattern.matches(trim())

private fun roleLabel(role: UserRole): String = when (role) {
    UserRole.ORGANIZER -> "Organiser"
    UserRole.PARTICIPANT -> "Participant"
    UserRole.PARTNER -> "Circular partner"
}

private fun errorText(reason: FailureReason): String = when (reason) {
    FailureReason.CONFIGURATION -> "Supabase is not configured. Complete the connection steps before using a live account."
    FailureReason.VALIDATION -> "Check the entered details. Passwords must have at least 8 characters."
    FailureReason.ACCOUNT_ALREADY_EXISTS -> "This email already has a ReEvent account. Sign in instead or reset its password."
    FailureReason.UNAUTHENTICATED -> "The email or password is incorrect."
    FailureReason.EMAIL_CONFIRMATION_REQUIRED -> "Confirm your email before signing in."
    FailureReason.RATE_LIMITED -> "Too many requests were made. Wait a moment before trying again."
    FailureReason.OFFLINE -> "The connection timed out. Check your internet connection and try again."
    else -> "We could not complete that action. Please try again."
}
