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
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reevent.app.core.auth.AuthUiState
import com.reevent.app.core.auth.PasswordRules
import com.reevent.app.core.data.FailureReason
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.components.LogoMark
import com.reevent.app.ui.theme.HomeCanvas
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSage
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
internal fun AccountScaffold(
    title: String? = null,
    subtitle: String? = null,
    eyebrow: String? = null,
    headerTitle: String = "Profile",
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(color = HomeCanvas, modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 44.dp, y = 52.dp)
                        .size(172.dp)
                        .clip(CircleShape)
                        .background(HomeSage.copy(alpha = 0.5f)),
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .background(HomePaper, CircleShape)
                                .size(42.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = HomeInk,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                    }
                    Text(
                        text = headerTitle,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium,
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                        ),
                        color = HomeInk,
                    )
                }
                if (!eyebrow.isNullOrBlank() || (!title.isNullOrBlank() && title != headerTitle) || !subtitle.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!eyebrow.isNullOrBlank()) {
                            Text(
                                eyebrow,
                                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 15.sp),
                                color = HomeForest,
                            )
                        }
                        if (!title.isNullOrBlank() && title != headerTitle) {
                            Text(
                                title,
                                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 28.sp),
                                color = HomeInk,
                            )
                        }
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                subtitle,
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 17.sp,
                                    lineHeight = 23.sp,
                                ),
                                color = ReEventTextSecondary,
                            )
                        }
                    }
                }
                content()
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun AccountCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HomePaper),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
internal fun AccountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp)) },
        textStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 17.sp, color = HomeInk),
        leadingIcon = icon,
        trailingIcon = trailingIcon,
        singleLine = true,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        supportingText = supportingText?.let { { Text(it, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp)) } },
        shape = RoundedCornerShape(14.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HomePaper,
                unfocusedContainerColor = HomePaper,
                focusedBorderColor = HomeForest,
                focusedLabelColor = HomeForest,
                cursorColor = HomeForest,
                unfocusedBorderColor = HomeLine,
            ),
    )
}

@Composable
internal fun PrimaryAccountButton(
    text: String,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !loading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = HomeForest, contentColor = Color.White),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Please wait", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp))
        } else {
            Text(text, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp))
        }
    }
}

@Composable
internal fun GoogleAccountButton(
    loading: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !loading,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, HomeLine),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = HomePaper, contentColor = HomeInk),
    ) {
        Text("G", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 20.sp), color = Color(0xFF4285F4))
        Spacer(Modifier.width(10.dp))
        Text("Continue with Google", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp))
    }
}

@Composable
internal fun AuthDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = HomeLine)
        Text("or", color = ReEventTextSecondary, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = HomeLine)
    }
}

@Composable
internal fun EmailConfirmationCard(
    email: String,
    loading: Boolean,
    onResend: () -> Unit,
    onSignIn: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HomeMist),
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = ReEventGreenDeep)
                Text("Check your inbox", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 20.sp), color = HomeInk)
            }
            Text(
                "If this is a new address, confirm $email before signing in. If you already have a ReEvent account, sign in instead. For account security, we cannot disclose which case applies.",
                color = ReEventTextSecondary,
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onSignIn, enabled = !loading) { Text("Go to sign in", color = HomeForest, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp)) }
                TextButton(onClick = onResend, enabled = !loading) { Text("Resend email", color = HomeForest, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp)) }
            }
        }
    }
}

@Composable
internal fun PasswordResetEmailSentCard(
    email: String,
    loading: Boolean,
    onResend: () -> Unit,
    onUseDifferentEmail: () -> Unit,
    onBack: () -> Unit,
    backLabel: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HomeMist),
        border = BorderStroke(1.dp, HomeLine),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = HomeForest,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Check your inbox",
                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 20.sp),
                    color = HomeInk
                )
            }
            Text(
                text = "We sent a password reset link to $email. Please check your inbox and follow the link to reset your password.",
                color = HomeInk,
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
            )
            Text(
                text = "If you don't see the email, check your spam or junk folder.",
                color = ReEventTextSecondary,
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onBack,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeForest,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = backLabel,
                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 17.sp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onUseDifferentEmail, enabled = !loading) {
                    Text("Use another email", color = HomeForest, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp))
                }
                TextButton(onClick = onResend, enabled = !loading) {
                    Text("Resend link", color = HomeForest, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp))
                }
            }
        }
    }
}

@Composable
internal fun PasswordUpdatedCard(onContinue: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventMint),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ReEventGreenDeep)
                Text("Password changed", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 20.sp), color = ReEventInk)
            }
            Text(
                "Your new password is active. Continue to the workspace that belongs to this account.",
                color = ReEventTextSecondary,
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
            )
            PrimaryAccountButton("Continue to ReEvent", loading = false, onClick = onContinue)
        }
    }
}

@Composable
internal fun PasswordRecoveryError(state: AuthUiState) {
    val reason = state.error ?: return
    val message =
        when (reason) {
            FailureReason.UNAUTHENTICATED -> "This recovery link is invalid or has expired. Request a new password-reset email."
            FailureReason.VALIDATION -> "Choose a password with at least ${PasswordRules.MIN_LENGTH} characters and enter it the same way twice."
            else -> errorText(reason)
        }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventCoralSoft),
    ) {
        Text(message, Modifier.padding(16.dp), color = ReEventCoral, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp))
    }
}

@Composable
internal fun RoleOption(
    role: UserRole,
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventSurface),
        border = BorderStroke(1.dp, ReEventLine),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(roleColor(role)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 18.sp), color = ReEventInk)
                Text(description, style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp), color = ReEventTextSecondary)
            }
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ReEventGreen.copy(alpha = 0.55f))
        }
    }
}

@Composable
internal fun CircularFeatureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ReEventGreenDeep),
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(ReEventMint), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = ReEventGreenDeep)
            }
            Text("One resource can have many useful lives.", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 22.sp), color = Color.White)
            Text(
                "Coordinate resources, handovers and impact without crossing workspace boundaries.",
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
                color = Color.White.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
internal fun Avatar(name: String) {
    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(ReEventGreenDeep), contentAlignment = Alignment.Center) {
        Text(
            text =
                name
                    .trim()
                    .split(
                        Regex("\\s+"),
                    ).filter { it.isNotEmpty() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifBlank { "R" },
            style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 22.sp),
            color = Color.White,
        )
    }
}

@Composable
internal fun AccountMessage(state: AuthUiState) {
    val message =
        when {
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
        colors = CardDefaults.cardColors(containerColor = if (isError) ReEventCoralSoft else ReEventMint),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = if (isError) ReEventCoral else ReEventGreenDeep,
            style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        )
    }
}

internal fun roleColor(role: UserRole): Color =
    when (role) {
        UserRole.ORGANIZER -> ReEventMint
        UserRole.PARTICIPANT -> Color(0xFFE8F0FF)
        UserRole.PARTNER -> Color(0xFFFFF1D2)
    }

private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal fun String.isPlausibleEmail(): Boolean = trim().length <= 254 && emailPattern.matches(trim())

internal fun roleLabel(role: UserRole): String =
    when (role) {
        UserRole.ORGANIZER -> "Organiser"
        UserRole.PARTICIPANT -> "Participant"
        UserRole.PARTNER -> "Circular partner"
    }

internal fun errorText(reason: FailureReason): String =
    when (reason) {
        FailureReason.CONFIGURATION -> "Supabase is not configured. Complete the connection steps before using a live account."
        FailureReason.VALIDATION -> "Check the entered details. Passwords must have at least 8 characters."
        FailureReason.ACCOUNT_ALREADY_EXISTS -> "This email already has a ReEvent account. Sign in instead or reset its password."
        FailureReason.UNAUTHENTICATED -> "The email or password is incorrect."
        FailureReason.EMAIL_CONFIRMATION_REQUIRED -> "Confirm your email before signing in."
        FailureReason.RATE_LIMITED -> "Too many requests were made. Wait a moment before trying again."
        FailureReason.OFFLINE -> "The connection timed out. Check your internet connection and try again."
        FailureReason.CONFLICT -> "This account already has a different permanent role. Sign in again to continue with that workspace."
        else -> "We could not complete that action. Please try again."
    }
