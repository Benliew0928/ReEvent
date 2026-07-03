package com.reevent.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.reevent.app.ui.screens.AddResourceScreen
import com.reevent.app.ui.screens.AiMatchScreen
import com.reevent.app.ui.screens.HomeScreen
import com.reevent.app.ui.screens.ImpactScreen
import com.reevent.app.ui.screens.MarketplaceScreen
import com.reevent.app.ui.screens.OnboardingScreen
import com.reevent.app.ui.screens.ParticipantReturnScreen
import com.reevent.app.ui.screens.PartnerMapScreen
import com.reevent.app.ui.screens.PartnerWorkbenchScreen
import com.reevent.app.ui.screens.PassportScreen
import com.reevent.app.ui.screens.ProfileScreen
import com.reevent.app.ui.screens.SignInScreen

enum class ReEventScreen {
    Onboarding,
    SignIn,
    Home,
    Marketplace,
    AddResource,
    Passport,
    AiMatch,
    PartnerMap,
    Impact,
    Profile,
    ParticipantReturn,
    PartnerWorkbench
}

@Composable
fun ReEventApp() {
    var route by rememberSaveable { mutableStateOf(ReEventScreen.Onboarding.name) }
    val currentScreen = ReEventScreen.valueOf(route)
    val navigate: (ReEventScreen) -> Unit = { route = it.name }

    BackHandler(enabled = currentScreen != ReEventScreen.Onboarding && currentScreen != ReEventScreen.Home) {
        navigate(
            when (currentScreen) {
                ReEventScreen.SignIn -> ReEventScreen.Onboarding
                ReEventScreen.Passport,
                ReEventScreen.AiMatch -> ReEventScreen.Marketplace
                ReEventScreen.PartnerWorkbench -> ReEventScreen.PartnerMap
                ReEventScreen.ParticipantReturn -> ReEventScreen.Profile
                else -> ReEventScreen.Home
            }
        )
    }

    when (currentScreen) {
        ReEventScreen.Onboarding -> OnboardingScreen(onNavigate = navigate)
        ReEventScreen.SignIn -> SignInScreen(onNavigate = navigate)
        ReEventScreen.Home -> HomeScreen(onNavigate = navigate)
        ReEventScreen.Marketplace -> MarketplaceScreen(onNavigate = navigate)
        ReEventScreen.AddResource -> AddResourceScreen(onNavigate = navigate)
        ReEventScreen.Passport -> PassportScreen(onNavigate = navigate)
        ReEventScreen.AiMatch -> AiMatchScreen(onNavigate = navigate)
        ReEventScreen.PartnerMap -> PartnerMapScreen(onNavigate = navigate)
        ReEventScreen.Impact -> ImpactScreen(onNavigate = navigate)
        ReEventScreen.Profile -> ProfileScreen(onNavigate = navigate)
        ReEventScreen.ParticipantReturn -> ParticipantReturnScreen(onNavigate = navigate)
        ReEventScreen.PartnerWorkbench -> PartnerWorkbenchScreen(onNavigate = navigate)
    }
}
