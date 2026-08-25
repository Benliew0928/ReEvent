package com.reevent.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.auth.AuthViewModel
import com.reevent.app.core.auth.PasswordRules
import com.reevent.app.core.auth.SignUpFormValidation
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventTextSecondary

@Composable
fun OnboardingFlowScreen(onContinue: () -> Unit) {
    AccountScaffold(
        eyebrow = "REUSE WITH PURPOSE",
        title = "Circular events, ready for real work",
        subtitle = "A calm, secure place to keep materials moving from one event to the next.",
    ) {
        CircularFeatureCard()
        PrimaryAccountButton(text = "Get started", loading = false, onClick = onContinue)
        Text(
            text = "Three workspaces. One trusted circular network.",
            modifier = Modifier.fillMaxWidth(),
            color = ReEventTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
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
            viewModel = viewModel,
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
    val signUpValidation =
        SignUpFormValidation.validate(
            displayName = displayName,
            email = email,
            password = password,
            confirmation = passwordConfirmation,
        )

    AccountScaffold(
        eyebrow = if (registrationMode) "YOUR CIRCULAR ACCOUNT" else "WELCOME BACK",
        title = if (registrationMode) "Create your account" else "Keep the good in motion",
        subtitle =
            if (registrationMode) {
                "Start with one role. Your data and permissions stay securely separated."
            } else {
                "Sign in to continue the circular flow of your events."
            },
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
                    supportingText = if (signUpSubmitted) signUpValidation.nameError else null,
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
                supportingText = if (registrationMode && signUpSubmitted) signUpValidation.emailError else null,
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
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        )
                    }
                },
                isError = registrationMode && signUpSubmitted && signUpValidation.passwordError != null,
                supportingText =
                    if (registrationMode && signUpSubmitted) {
                        signUpValidation.passwordError
                    } else if (registrationMode) {
                        "Use at least 8 characters."
                    } else {
                        null
                    },
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
                                contentDescription = if (confirmationVisible) "Hide password confirmation" else "Show password confirmation",
                            )
                        }
                    },
                    isError = signUpSubmitted && signUpValidation.confirmationError != null,
                    supportingText = if (signUpSubmitted) signUpValidation.confirmationError else null,
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
                },
            )
            if (!registrationMode) {
                TextButton(
                    onClick = {
                        viewModel.clearFeedback()
                        passwordResetMode = true
                    },
                    enabled = !state.loading,
                    modifier = Modifier.align(Alignment.End),
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
                },
            )
        }

        TextButton(
            onClick = {
                registrationMode = !registrationMode
                signUpSubmitted = false
                viewModel.clearFeedback()
            },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (registrationMode) "Already have an account? Sign in" else "New to ReEvent? Create an account")
        }
    }
}

@Composable
internal fun PasswordResetRequestFlow(
    initialEmail: String,
    backLabel: String,
    onBack: () -> Unit,
    viewModel: AuthViewModel,
) {
    var email by rememberSaveable(initialEmail) { mutableStateOf(initialEmail) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val emailError = submitted && !email.isPlausibleEmail()

    AccountScaffold(
        eyebrow = "ACCOUNT RECOVERY",
        title = "Reset your password",
        subtitle = "Enter your email and we will send a secure link if it belongs to a ReEvent account.",
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
                backLabel = backLabel,
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
                    supportingText = if (emailError) "Enter a valid email address." else "We never reveal whether an account exists.",
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
        subtitle =
            if (state.passwordUpdated) {
                "Your ReEvent password has been changed successfully."
            } else {
                "This protected screen was opened from your recovery email. Choose a new password before continuing."
            },
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
                            Icon(
                                if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    isError = passwordError,
                    supportingText = "Use at least ${PasswordRules.MIN_LENGTH} characters.",
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
                            Icon(
                                if (confirmationVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                if (confirmationVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                    isError = confirmationError,
                    supportingText = if (confirmationError) "Passwords do not match." else null,
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
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Cancel recovery and sign in instead") }
        }
    }
}

@Composable
fun CompleteRoleFlowScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val retryRole = state.pendingRoleAssignment
    AccountScaffold(
        eyebrow = "ONE FINAL STEP",
        title = "Choose your workspace",
        subtitle = "This controls what you can see and do. It is permanent in the mobile app, so choose the role that matches your work.",
    ) {
        RoleOption(
            role = UserRole.ORGANIZER,
            icon = { Icon(Icons.Outlined.Apartment, null) },
            title = "Organiser",
            description = "Plan events, list resources, match partners and measure impact.",
            enabled = !state.loading && (retryRole == null || retryRole == UserRole.ORGANIZER),
            onClick = { viewModel.completeRole(UserRole.ORGANIZER) },
        )
        RoleOption(
            role = UserRole.PARTICIPANT,
            icon = { Icon(Icons.Outlined.Groups, null) },
            title = "Participant",
            description = "Return items, browse available resources and follow your exchanges.",
            enabled = !state.loading && (retryRole == null || retryRole == UserRole.PARTICIPANT),
            onClick = { viewModel.completeRole(UserRole.PARTICIPANT) },
        )
        RoleOption(
            role = UserRole.PARTNER,
            icon = { Icon(Icons.Outlined.BusinessCenter, null) },
            title = "Circular partner",
            description = "Manage programmes and complete handovers assigned to your organisation.",
            enabled = !state.loading && (retryRole == null || retryRole == UserRole.PARTNER),
            onClick = { viewModel.completeRole(UserRole.PARTNER) },
        )
        if (retryRole != null) {
            Text(
                "We are confirming your ${roleLabel(retryRole)} selection. Roles are permanent, so retry that same choice before selecting another.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        AccountMessage(state)
    }
}

/**
 * A prepared account has already lost its role and normal workspace privileges. This screen is
 * intentionally outside every role navigation graph: the only safe actions are retrying the
 * protected server finalisation or clearing the local session.
 */
@Composable
fun AccountDeletionPendingFlowScreen(
    user: User,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var currentPassword by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    AccountScaffold(
        eyebrow = "ACCOUNT DELETION PENDING",
        title = "Finish removing your account",
        subtitle = "Your ReEvent workspace is locked and cannot be restored. Retry the protected final step when you have a connection.",
    ) {
        AccountCard {
            Text("Signed in as ${user.email}", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
            Text(
                "No role, wallet, event, resource, Marketplace, or partner action is available while deletion is pending.",
                color = ReEventTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            AccountTextField(
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    submitted = false
                    viewModel.clearFeedback()
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
                isError = submitted && currentPassword.isBlank(),
                supportingText = if (submitted && currentPassword.isBlank()) "Enter your current password to retry." else null,
            )
            if (state.accountDeletionPending) {
                Text(
                    "The server still could not finish private-file or sign-in removal. Nothing was restored; check your connection and retry.",
                    color = ReEventCoral,
                    style = MaterialTheme.typography.bodySmall,
                )
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
                    "This account does not support password re-authentication. Sign out and contact the ReEvent project team to finish deletion.",
                    color = ReEventCoral,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            state.error?.let { error ->
                Text(errorText(error), color = ReEventCoral, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    submitted = true
                    if (currentPassword.isNotBlank()) viewModel.deleteAccount(currentPassword)
                },
                enabled = !state.loading && !state.passwordReauthenticationUnavailable,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ReEventCoral, contentColor = Color.White),
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.loading) "Retrying" else "Retry permanent deletion")
            }
        }
        TextButton(onClick = viewModel::signOut, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out without restoring access")
        }
    }
}
