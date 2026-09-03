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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.auth.AuthViewModel
import com.reevent.app.core.model.User
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.ReEventScaffold
import com.reevent.app.ui.components.SyncQueueCard
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeGreetingStyle
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomePaper
import com.reevent.app.ui.theme.HomeSupportingTextStyle
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventTextSecondary

enum class ProfileSubScreen {
    PERSONAL_INFO,
    EDIT_NAME,
    EDIT_PHONE,
    EDIT_GENDER,
    ACCOUNT_INFO,
    ACCOUNT_SECURITY,
    DELETE_ACCOUNT,
    PUSH_NOTIFICATIONS,
    EMAIL_NOTIFICATIONS,
    HELP,
    ABOUT,
}

@Composable
fun ProfileFlowScreen(
    user: User,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    syncViewModel: FeatureViewModel = hiltViewModel(),
) {
    var passwordResetMode by rememberSaveable { mutableStateOf(false) }
    var activeSubScreen by rememberSaveable { mutableStateOf<ProfileSubScreen?>(null) }

    var userDisplayName by rememberSaveable { mutableStateOf(user.displayName) }
    var userPhone by rememberSaveable { mutableStateOf<String?>(null) }
    var userGender by rememberSaveable { mutableStateOf<String?>(null) }

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

    when (activeSubScreen) {
        ProfileSubScreen.PERSONAL_INFO -> {
            PersonalInfoScreen(
                userDisplayName = userDisplayName,
                phoneNumber = userPhone,
                gender = userGender,
                onEditName = { activeSubScreen = ProfileSubScreen.EDIT_NAME },
                onEditPhone = { activeSubScreen = ProfileSubScreen.EDIT_PHONE },
                onEditGender = { activeSubScreen = ProfileSubScreen.EDIT_GENDER },
                onBack = { activeSubScreen = null },
            )
            return
        }
        ProfileSubScreen.EDIT_NAME -> {
            EditNameScreen(
                currentName = userDisplayName,
                onSave = {
                    userDisplayName = it
                    activeSubScreen = ProfileSubScreen.PERSONAL_INFO
                },
                onBack = { activeSubScreen = ProfileSubScreen.PERSONAL_INFO },
            )
            return
        }
        ProfileSubScreen.EDIT_PHONE -> {
            EditPhoneScreen(
                currentPhone = userPhone,
                onSave = {
                    userPhone = it
                    activeSubScreen = ProfileSubScreen.PERSONAL_INFO
                },
                onBack = { activeSubScreen = ProfileSubScreen.PERSONAL_INFO },
            )
            return
        }
        ProfileSubScreen.EDIT_GENDER -> {
            EditGenderScreen(
                currentGender = userGender,
                onSave = {
                    userGender = it
                    activeSubScreen = ProfileSubScreen.PERSONAL_INFO
                },
                onBack = { activeSubScreen = ProfileSubScreen.PERSONAL_INFO },
            )
            return
        }
        ProfileSubScreen.ACCOUNT_INFO -> {
            AccountInfoScreen(user = user, onBack = { activeSubScreen = null })
            return
        }
        ProfileSubScreen.ACCOUNT_SECURITY -> {
            AccountSecurityScreen(
                user = user,
                onResetPassword = {
                    viewModel.clearFeedback()
                    passwordResetMode = true
                },
                onDeleteAccount = {
                    viewModel.clearFeedback()
                    activeSubScreen = ProfileSubScreen.DELETE_ACCOUNT
                },
                onBack = { activeSubScreen = null },
            )
            return
        }
        ProfileSubScreen.DELETE_ACCOUNT -> {
            DeleteAccountScreen(
                email = user.email,
                state = authState,
                onBack = { activeSubScreen = ProfileSubScreen.ACCOUNT_SECURITY },
                onSubmit = viewModel::deleteAccount,
            )
            return
        }
        ProfileSubScreen.PUSH_NOTIFICATIONS -> {
            PushNotificationScreen(onBack = { activeSubScreen = null })
            return
        }
        ProfileSubScreen.EMAIL_NOTIFICATIONS -> {
            EmailNotificationScreen(onBack = { activeSubScreen = null })
            return
        }
        ProfileSubScreen.HELP -> {
            HelpScreen(onBack = { activeSubScreen = null })
            return
        }
        ProfileSubScreen.ABOUT -> {
            AboutScreen(onBack = { activeSubScreen = null })
            return
        }
        null -> {}
    }

    ReEventScaffold(selected = TopLevelDestination.ACCOUNT, onNavigate = onNavigate, modifier = modifier) { padding ->
        AccountScaffold(
            headerTitle = "Profile",
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
                    val initials = userDisplayName.trim().split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(HomeMist),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials.ifBlank { "U" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = HomeForest,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = userDisplayName,
                            style = HomeCardTitleStyle.copy(fontSize = 26.sp, lineHeight = 28.sp),
                            color = HomeInk,
                        )
                        Text(
                            text = user.email,
                            style = HomeSupportingTextStyle,
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
                    onClick = { activeSubScreen = ProfileSubScreen.PERSONAL_INFO },
                )
                HorizontalDivider(color = HomeLine)
                ProfileNavigationRow(
                    title = "Account Information",
                    onClick = { activeSubScreen = ProfileSubScreen.ACCOUNT_INFO },
                )
                HorizontalDivider(color = HomeLine)
                ProfileNavigationRow(
                    title = "Account Security",
                    onClick = { activeSubScreen = ProfileSubScreen.ACCOUNT_SECURITY },
                )
            }

            // Section 2: Preferences
            ProfileSectionLabel("Preferences")
            AccountCard {
                ProfileNavigationRow(
                    title = "Push Notification",
                    onClick = { activeSubScreen = ProfileSubScreen.PUSH_NOTIFICATIONS },
                )
                HorizontalDivider(color = HomeLine)
                ProfileNavigationRow(
                    title = "Email Notification",
                    onClick = { activeSubScreen = ProfileSubScreen.EMAIL_NOTIFICATIONS },
                )
            }

            // Section 3: Support
            ProfileSectionLabel("Support")
            AccountCard {
                ProfileNavigationRow(
                    title = "Help",
                    onClick = { activeSubScreen = ProfileSubScreen.HELP },
                )
                HorizontalDivider(color = HomeLine)
                ProfileNavigationRow(
                    title = "About",
                    onClick = { activeSubScreen = ProfileSubScreen.ABOUT },
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
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ReEventCoral),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = HomePaper, contentColor = ReEventCoral),
            ) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionLabel(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = HomeGreetingStyle.copy(fontSize = 25.sp, lineHeight = 28.sp),
        color = HomeInk,
        modifier = modifier.padding(top = 6.dp, bottom = 2.dp),
    )
}

@Composable
private fun ProfileNavigationRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = HomeBodyStyle,
            color = HomeInk,
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = HomeForest,
            modifier = Modifier.size(20.dp),
        )
    }
}
