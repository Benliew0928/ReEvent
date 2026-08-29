package com.reevent.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.auth.AuthViewModel
import com.reevent.app.core.auth.PasswordRules
import com.reevent.app.core.auth.SignUpFormValidation
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventInk
import com.reevent.app.ui.theme.ReEventTextSecondary

private val EcoGreen = Color(0xFF00875A)
private val LightMintBg = Color(0xFFE8F5E9)

private enum class AuthStep {
    INITIAL_EMAIL,
    SIGN_UP_FORM,
    WELCOME_BACK_FORM,
}

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

    var step by rememberSaveable { mutableStateOf(AuthStep.INITIAL_EMAIL) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var receiveUpdates by rememberSaveable { mutableStateOf(true) }
    var emailSubmitted by rememberSaveable { mutableStateOf(false) }
    var formSubmitted by rememberSaveable { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        when (step) {
            AuthStep.INITIAL_EMAIL -> {
                InitialEmailScreen(
                    email = email,
                    onEmailChange = {
                        email = it
                        emailSubmitted = false
                        viewModel.clearFeedback()
                    },
                    isError = emailSubmitted && !email.isPlausibleEmail(),
                    loading = state.loading,
                    onLogIn = {
                        emailSubmitted = true
                        if (email.isPlausibleEmail()) {
                            viewModel.clearFeedback()
                            step = AuthStep.WELCOME_BACK_FORM
                        }
                    },
                    onSignUp = {
                        emailSubmitted = true
                        if (email.isPlausibleEmail()) {
                            viewModel.clearFeedback()
                            step = AuthStep.SIGN_UP_FORM
                        }
                    },
                    onGoogleSignIn = viewModel::signInWithGoogle,
                    state = state,
                )
            }

            AuthStep.SIGN_UP_FORM -> {
                SignUpFormScreen(
                    email = email.trim(),
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    receiveUpdates = receiveUpdates,
                    onReceiveUpdatesChange = { receiveUpdates = it },
                    loading = state.loading,
                    formSubmitted = formSubmitted,
                    state = state,
                    onBackToEmail = {
                        viewModel.clearFeedback()
                        step = AuthStep.INITIAL_EMAIL
                    },
                    onAlreadyRegistered = {
                        viewModel.clearFeedback()
                        step = AuthStep.WELCOME_BACK_FORM
                    },
                    onResendConfirmation = { viewModel.resendSignUpConfirmation(it) },
                    onSubmitSignUp = {
                        formSubmitted = true
                        val displayName = "${firstName.trim()} ${lastName.trim()}".trim()
                        val validation = SignUpFormValidation.validate(displayName, email.trim(), password, password)
                        if (validation.isValid) {
                            viewModel.signUp(email.trim(), password, displayName)
                        }
                    },
                )
            }

            AuthStep.WELCOME_BACK_FORM -> {
                WelcomeBackFormScreen(
                    email = email.trim(),
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisible = { passwordVisible = !passwordVisible },
                    loading = state.loading,
                    formSubmitted = formSubmitted,
                    state = state,
                    onBackToEmail = {
                        viewModel.clearFeedback()
                        step = AuthStep.INITIAL_EMAIL
                    },
                    onNeedSignUp = {
                        viewModel.clearFeedback()
                        step = AuthStep.SIGN_UP_FORM
                    },
                    onSubmitSignIn = {
                        formSubmitted = true
                        if (password.isNotBlank()) {
                            viewModel.signIn(email.trim(), password)
                        }
                    },
                    onSendMagicLink = {
                        if (email.isPlausibleEmail()) {
                            viewModel.requestPasswordReset(email.trim())
                        }
                    },
                    onForgotPassword = {
                        viewModel.clearFeedback()
                        passwordResetMode = true
                    },
                )
            }
        }
    }
}

/**
 * Screen 1: Welcome to ReEvent (Initial Email Entry)
 * Displays 8-tile eco mosaic grid, email field, "Log in or Sign up" button, and Google sign-in.
 */
@Composable
private fun InitialEmailScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    isError: Boolean,
    loading: Boolean,
    onLogIn: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    state: com.reevent.app.core.auth.AuthUiState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            // 8-Tile Recycling / Circular Asset Mosaic Grid
            RecyclingMosaicHeader()

            Spacer(Modifier.height(28.dp))

            // Title
            Text(
                text = "Welcome to ReEvent",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color(0xFF111827),
            )

            Spacer(Modifier.height(20.dp))

            // Email Address Label & Field
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF374151),
            )
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
                isError = isError,
                supportingText = if (isError) {
                    { Text("Please enter a valid email address.", color = ReEventCoral) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EcoGreen,
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                ),
            )

            Spacer(Modifier.height(20.dp))

            // Primary Log in Button
            Button(
                onClick = onLogIn,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGreen,
                    contentColor = Color.White,
                ),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = "Log in",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Secondary Sign up Button
            OutlinedButton(
                onClick = onSignUp,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, EcoGreen),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EcoGreen,
                ),
            ) {
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(Modifier.height(32.dp))

            // Social Login Section (Google Only)
            Text(
                text = "Or continue with:",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
            )
            Spacer(Modifier.height(12.dp))

            GoogleAccountButton(loading = loading, onClick = onGoogleSignIn)

            AccountMessage(state)
        }
    }
}

/**
 * Screen 2: Sign up for ReEvent
 * Top email pill header + First Name, Last Name, Password + Requirements checklist + Updates checkbox.
 */
@Composable
private fun SignUpFormScreen(
    email: String,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    receiveUpdates: Boolean,
    onReceiveUpdatesChange: (Boolean) -> Unit,
    loading: Boolean,
    formSubmitted: Boolean,
    state: com.reevent.app.core.auth.AuthUiState,
    onBackToEmail: () -> Unit,
    onAlreadyRegistered: () -> Unit,
    onResendConfirmation: (String) -> Unit,
    onSubmitSignUp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        // Top Bar: Back button
        IconButton(
            onClick = onBackToEmail,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF111827),
            )
        }

        Spacer(Modifier.height(24.dp))

        // Title & Subtitle
        Text(
            text = "Sign up for ReEvent",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = (-0.5).sp,
            ),
            color = Color(0xFF111827),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sign up for easy access to your circular event resources and tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B5563),
        )

        Spacer(Modifier.height(24.dp))

        // First Name Input
        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = formSubmitted && firstName.isBlank(),
            supportingText = if (formSubmitted && firstName.isBlank()) {
                { Text("Enter a name with at least 2 characters.", color = ReEventCoral) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoGreen,
                unfocusedBorderColor = Color(0xFFD1D5DB),
            ),
        )

        Spacer(Modifier.height(14.dp))

        // Last Name Input
        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoGreen,
                unfocusedBorderColor = Color(0xFFD1D5DB),
            ),
        )

        Spacer(Modifier.height(14.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisible) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF6B7280),
                    )
                }
            },
            isError = formSubmitted && !PasswordRules.isValid(password),
            supportingText = if (formSubmitted && !PasswordRules.isValid(password)) {
                { Text("Use at least ${PasswordRules.MIN_LENGTH} characters.", color = ReEventCoral) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoGreen,
                unfocusedBorderColor = Color(0xFFD1D5DB),
            ),
        )

        Spacer(Modifier.height(12.dp))

        // Password Requirement Checklist
        PasswordRequirementItem(text = "Must have at least 8 characters", isMet = password.length >= 8)
        PasswordRequirementItem(text = "Must have at least 1 uppercase letter", isMet = password.any { it.isUpperCase() })
        PasswordRequirementItem(text = "Must have at least 1 number", isMet = password.any { it.isDigit() })

        Spacer(Modifier.height(20.dp))

        // Updates Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Checkbox(
                checked = receiveUpdates,
                onCheckedChange = onReceiveUpdatesChange,
                colors = CheckboxDefaults.colors(checkedColor = EcoGreen),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Receive email updates on special offers, inspiration, tips and other updates from ReEvent.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF374151),
            )
        }

        Spacer(Modifier.height(24.dp))

        // Create Account Button
        Button(
            onClick = onSubmitSignUp,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGreen,
                contentColor = Color.White,
            ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Create an account",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }


        Spacer(Modifier.height(20.dp))

        // Footer Terms
        Text(
            text = "By creating an account, you agree to our Terms & Conditions, Privacy Policy and Agreement with ReEvent.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Account feedback message & Email confirmation card
        AccountMessage(state)

        if (state.confirmationRequired) {
            Spacer(Modifier.height(16.dp))
            EmailConfirmationCard(
                email = state.confirmationEmail ?: email,
                loading = loading,
                onResend = { onResendConfirmation(state.confirmationEmail ?: email) },
                onSignIn = onAlreadyRegistered,
            )
        }
    }
}

/**
 * Screen 3: Welcome back! (Log In)
 * Top email pill header + Password field + "Log in" button + One-time login link + Forgot password link.
 */
@Composable
private fun WelcomeBackFormScreen(
    email: String,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    loading: Boolean,
    formSubmitted: Boolean,
    state: com.reevent.app.core.auth.AuthUiState,
    onBackToEmail: () -> Unit,
    onNeedSignUp: () -> Unit,
    onSubmitSignIn: () -> Unit,
    onSendMagicLink: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        // Top Bar: Back button
        IconButton(
            onClick = onBackToEmail,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF111827),
            )
        }

        Spacer(Modifier.height(28.dp))

        // Title
        Text(
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = (-0.5).sp,
            ),
            color = Color(0xFF111827),
        )

        Spacer(Modifier.height(24.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisible) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xFF6B7280),
                    )
                }
            },
            isError = formSubmitted && password.isBlank(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EcoGreen,
                unfocusedBorderColor = Color(0xFFD1D5DB),
            ),
        )

        Spacer(Modifier.height(24.dp))

        // Log in Button
        Button(
            onClick = onSubmitSignIn,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoGreen,
                contentColor = Color.White,
            ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Alternative: One-time login link
        Text(
            text = "Or, we'll email you a one-time login link",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF4B5563),
        )
        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onSendMagicLink,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF111827)),
        ) {
            Text(
                text = "Email me",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        Spacer(Modifier.height(20.dp))

        // Forgot password? Link
        TextButton(
            onClick = onForgotPassword,
            enabled = !loading,
        ) {
            Text(
                text = "Forgot password?",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                ),
                color = Color(0xFF111827),
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onNeedSignUp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Don't have an account? Sign up instead", color = EcoGreen)
        }

        AccountMessage(state)
    }
}

/**
 * Top User Email Header Pill showing active email + "use a different email" back action link.
 */
@Composable
private fun UserEmailHeaderPill(
    email: String,
    onUseDifferentEmail: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = onUseDifferentEmail,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF111827),
            )
        }
        Spacer(Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE6F4EA),
            modifier = Modifier.clickable(onClick = onUseDifferentEmail),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = EcoGreen,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (email.isBlank()) "user@example.com" else email,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = Color(0xFF111827),
                    )
                    Text(
                        text = "use a different email",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = Color(0xFF111827),
                    )
                }
            }
        }
    }
}

/**
 * Password requirement item for Sign Up checklist.
 */
@Composable
private fun PasswordRequirementItem(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Text(
            text = "• ",
            color = if (isMet) EcoGreen else Color(0xFF6B7280),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) EcoGreen else Color(0xFF4B5563),
        )
    }
}

/**
 * Asymmetric 8-Tile Mosaic Header representing recycling & circular event assets.
 */
@Composable
private fun RecyclingMosaicHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Column 1
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MosaicTile(
                icon = Icons.Outlined.Recycling,
                bgColor = Color(0xFFE8F5E9),
                iconColor = EcoGreen,
                modifier = Modifier.weight(1.2f),
                topCorner = 16.dp,
            )
            MosaicTile(
                icon = Icons.Outlined.Apartment,
                bgColor = Color(0xFFF3F4F6),
                iconColor = Color(0xFF4B5563),
                modifier = Modifier.weight(1f),
            )
        }
        // Column 2
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MosaicTile(
                icon = Icons.Outlined.Groups,
                bgColor = Color(0xFFE0F2FE),
                iconColor = Color(0xFF0284C7),
                modifier = Modifier.weight(0.9f),
            )
            MosaicTile(
                icon = Icons.Outlined.BusinessCenter,
                bgColor = Color(0xFFFEF3C7),
                iconColor = Color(0xFFD97706),
                modifier = Modifier.weight(1.3f),
            )
        }
        // Column 3
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MosaicTile(
                icon = Icons.Outlined.CheckCircle,
                bgColor = Color(0xFFF0FDF4),
                iconColor = EcoGreen,
                modifier = Modifier.weight(1.4f),
            )
            MosaicTile(
                icon = Icons.Outlined.Email,
                bgColor = Color(0xFFF5F3FF),
                iconColor = Color(0xFF7C3AED),
                modifier = Modifier.weight(0.8f),
            )
        }
        // Column 4
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MosaicTile(
                icon = Icons.Outlined.Lock,
                bgColor = Color(0xFFFFF7ED),
                iconColor = Color(0xFFEA580C),
                modifier = Modifier.weight(1f),
                topCorner = 16.dp,
            )
            MosaicTile(
                icon = Icons.Outlined.Recycling,
                bgColor = Color(0xFFECFDF5),
                iconColor = EcoGreen,
                modifier = Modifier.weight(1.1f),
            )
        }
    }
}

@Composable
private fun MosaicTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    topCorner: androidx.compose.ui.unit.Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = topCorner, topEnd = topCorner, bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )
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
        headerTitle = "Reset Password",
        subtitle = "Enter your registered email address to receive a password reset link.",
        onBack = onBack,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
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
                AccountTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (submitted) submitted = false
                    },
                    label = "Email Address",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    isError = emailError,
                    supportingText = if (emailError) "Please enter a valid email address." else null,
                )

                Spacer(Modifier.height(8.dp))

                PrimaryAccountButton(
                    text = "Send reset link",
                    loading = state.loading,
                    onClick = {
                        submitted = true
                        if (email.isPlausibleEmail()) viewModel.requestPasswordReset(email)
                    },
                )

                AccountMessage(state)
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

    Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Title & Subtitle
            Text(
                text = if (state.passwordUpdated) "Password updated!" else "Choose a new password",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = Color(0xFF111827),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (state.passwordUpdated) {
                    "Your ReEvent account password has been changed successfully."
                } else {
                    "Please create a strong new password for your account."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563),
            )

            Spacer(Modifier.height(24.dp))

            if (state.passwordUpdated) {
                Button(
                    onClick = viewModel::finishPasswordRecovery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGreen,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "Continue to sign in",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }
            } else {
                // New Password Input
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (submitted) submitted = false
                    },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF6B7280),
                            )
                        }
                    },
                    isError = passwordError,
                    supportingText = if (passwordError) {
                        { Text("Use at least ${PasswordRules.MIN_LENGTH} characters.", color = ReEventCoral) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreen,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                    ),
                )

                Spacer(Modifier.height(14.dp))

                // Confirm Password Input
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = {
                        confirmation = it
                        if (submitted) submitted = false
                    },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (confirmationVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmationVisible = !confirmationVisible }) {
                            Icon(
                                imageVector = if (confirmationVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (confirmationVisible) "Hide password" else "Show password",
                                tint = Color(0xFF6B7280),
                            )
                        }
                    },
                    isError = confirmationError,
                    supportingText = if (confirmationError) {
                        { Text("Passwords do not match.", color = ReEventCoral) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EcoGreen,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                    ),
                )

                Spacer(Modifier.height(14.dp))

                // Requirement Checklist
                PasswordRequirementItem(text = "Must have at least 8 characters", isMet = password.length >= 8)
                PasswordRequirementItem(text = "Must have at least 1 uppercase letter", isMet = password.any { it.isUpperCase() })
                PasswordRequirementItem(text = "Must have at least 1 number", isMet = password.any { it.isDigit() })

                Spacer(Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        submitted = true
                        if (PasswordRules.isValid(password) && PasswordRules.matchesConfirmation(password, confirmation)) {
                            viewModel.updatePassword(password)
                        }
                    },
                    enabled = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoGreen,
                        contentColor = Color.White,
                    ),
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Update password",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }

                AccountMessage(state)
            }
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
