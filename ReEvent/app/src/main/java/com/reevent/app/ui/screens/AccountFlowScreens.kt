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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.components.LogoMark
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
    var password by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var registrationMode by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val submitLabel = if (registrationMode) "Create account" else "Sign in"

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
                    onValueChange = { displayName = it },
                    label = "Your name",
                    icon = { Icon(Icons.Outlined.Person, null) },
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            }
            AccountTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email address",
                icon = { Icon(Icons.Outlined.Email, null) },
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            AccountTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                icon = { Icon(Icons.Outlined.Lock, null) },
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
                supportingText = if (registrationMode) "Use at least 8 characters." else null
            )
            PrimaryAccountButton(
                text = submitLabel,
                loading = state.loading,
                onClick = {
                    if (registrationMode) viewModel.signUp(email, password, displayName)
                    else viewModel.signIn(email, password)
                }
            )
            if (!registrationMode) {
                TextButton(
                    onClick = { viewModel.requestPasswordReset(email) },
                    enabled = !state.loading && email.isNotBlank(),
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
                }
            )
        }

        TextButton(
            onClick = { registrationMode = !registrationMode },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (registrationMode) "Already have an account? Sign in" else "New to ReEvent? Create an account")
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
fun ProfileFlowScreen(user: User, onBack: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    AccountScaffold(
        eyebrow = "YOUR ACCOUNT",
        title = "Account & workspace",
        subtitle = "Your account is connected to one protected workspace.",
        onBack = onBack
    ) {
        AccountCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Avatar(user.displayName)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(user.displayName, style = MaterialTheme.typography.titleLarge, color = ReEventInk)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = ReEventTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            HorizontalDivider(color = ReEventLine)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(ReEventMint),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Lock, contentDescription = null, tint = ReEventGreenDeep) }
                Column {
                    Text("Fixed workspace", style = MaterialTheme.typography.labelLarge, color = ReEventTextSecondary)
                    Text("${roleLabel(requireNotNull(user.role))} workspace", style = MaterialTheme.typography.titleMedium, color = ReEventInk)
                }
            }
        }
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

@Composable
private fun AccountScaffold(
    eyebrow: String,
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(color = ReEventBackground, modifier = Modifier.fillMaxSize()) {
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
