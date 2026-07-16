package com.reevent.app.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.reevent.app.core.auth.AppEntry
import com.reevent.app.core.auth.SessionViewModel
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.screens.CompleteRoleFlowScreen
import com.reevent.app.ui.screens.MatchingLiveScreen
import com.reevent.app.ui.screens.OnboardingFlowScreen
import com.reevent.app.ui.screens.MarketplaceVisualScreen
import com.reevent.app.ui.screens.OrganizerHomeVisualScreen
import com.reevent.app.ui.screens.OrganizerAddResourceVisualScreen
import com.reevent.app.ui.screens.OrganizerImpactVisualScreen
import com.reevent.app.ui.screens.ParticipantReturnVisualScreen
import com.reevent.app.ui.screens.PartnerMapVisualScreen
import com.reevent.app.ui.screens.PartnerWorkbenchVisualScreen
import com.reevent.app.ui.screens.PassportVisualScreen
import com.reevent.app.ui.screens.ProfileFlowScreen
import com.reevent.app.ui.screens.SignInFlowScreen
import com.reevent.app.ui.theme.ReEventBackground
import com.reevent.app.ui.components.LocalReEventRole
import kotlinx.serialization.Serializable

@Serializable private data object OrganizerHomeRoute
@Serializable private data object ParticipantReturnRoute
@Serializable private data object PartnerWorkbenchRoute
@Serializable private data object MarketplaceRoute
@Serializable private data object OrganizerAddRoute
@Serializable private data class PassportRoute(val resourceId: String)
@Serializable private data class MatchingRoute(val resourceId: String)
@Serializable private data object PartnerMapRoute
@Serializable private data object OrganizerImpactRoute
@Serializable private data object ProfileRoute

@Composable
fun ReEventApp() {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val session by sessionViewModel.state.collectAsState()
    when (session.entry) {
        AppEntry.LOADING -> LoadingScreen()
        AppEntry.ONBOARDING -> OnboardingFlowScreen(sessionViewModel::completeOnboarding)
        AppEntry.SIGN_IN -> SignInFlowScreen()
        AppEntry.COMPLETE_ROLE -> CompleteRoleFlowScreen()
        AppEntry.ORGANIZER, AppEntry.PARTICIPANT, AppEntry.PARTNER -> {
            val user = requireNotNull(session.user)
            // A new user must always receive a fresh graph and back stack. Without this key,
            // switching between two organiser accounts could retain the old Add resource page.
            key(user.id, user.role) {
                RoleNavigationRoot(user, requireNotNull(user.role))
            }
        }
    }
}

@Composable
private fun LoadingScreen() = Surface(color = ReEventBackground, modifier = Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun RoleNavigationRoot(user: User, role: UserRole) {
    val nav = rememberNavController()
    val navigationTapGuard = remember { NavigationTapGuard() }
    val start = when (role) {
        UserRole.ORGANIZER -> OrganizerHomeRoute
        UserRole.PARTICIPANT -> ParticipantReturnRoute
        UserRole.PARTNER -> PartnerWorkbenchRoute
    }
    CompositionLocalProvider(LocalReEventRole provides role.toVisualRole()) {
        NavHost(navController = nav, startDestination = start) {
            when (role) {
                UserRole.ORGANIZER -> organiserGraph(nav, user, navigationTapGuard)
                UserRole.PARTICIPANT -> participantGraph(nav, user)
                UserRole.PARTNER -> partnerGraph(nav, user)
            }
            composable<ProfileRoute> {
                ProfileFlowScreen(
                    user = user,
                    onBack = { nav.popBackStack() },
                    onNavigate = when (role) {
                        UserRole.ORGANIZER -> nav::openOrganiserVisualDestination
                        UserRole.PARTICIPANT -> nav::openParticipantVisualDestination
                        UserRole.PARTNER -> nav::openPartnerVisualDestination
                    }
                )
            }
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.organiserGraph(
    nav: NavHostController,
    user: User,
    navigationTapGuard: NavigationTapGuard
) {
    composable<OrganizerHomeRoute> {
        OrganizerHomeVisualScreen(
            user = user,
            onAddResource = { nav.openTopLevel(OrganizerAddRoute) },
            onPassport = { nav.openDetail(PassportRoute(it)) },
            onImpact = { nav.openTopLevel(OrganizerImpactRoute) },
            onMarketplace = { nav.openTopLevel(MarketplaceRoute) },
            onPartnerMap = { nav.openTopLevel(PartnerMapRoute) },
            onProfile = { navigationTapGuard.runIfAllowed(nav::openProfile) }
        )
    }
    composable<OrganizerAddRoute> {
        OrganizerAddResourceVisualScreen(
            user = user,
            onSaved = { nav.openTopLevel(OrganizerHomeRoute) },
            onBack = {
                navigationTapGuard.blockBriefly()
                nav.openTopLevel(OrganizerHomeRoute)
            },
            onNavigate = nav::openOrganiserVisualDestination
        )
    }
    composable<PassportRoute> { entry ->
        PassportVisualScreen(
            resourceId = entry.toRoute<PassportRoute>().resourceId,
            onMatch = { nav.openDetail(MatchingRoute(it)) },
            onNavigate = nav::openOrganiserVisualDestination
        )
    }
    composable<MatchingRoute> { entry -> MatchingLiveScreen(entry.toRoute<MatchingRoute>().resourceId, { nav.popBackStack() }) }
    composable<OrganizerImpactRoute> { OrganizerImpactVisualScreen(user, nav::openOrganiserVisualDestination) }
    composable<MarketplaceRoute> { MarketplaceVisualScreen(user, { nav.openDetail(PassportRoute(it)) }, nav::openOrganiserVisualDestination) }
    composable<PartnerMapRoute> { PartnerMapVisualScreen(nav::openOrganiserVisualDestination) }
}

private fun androidx.navigation.NavGraphBuilder.participantGraph(nav: NavHostController, user: User) {
    composable<ParticipantReturnRoute> { ParticipantReturnVisualScreen(user, nav::openParticipantVisualDestination) }
    composable<MarketplaceRoute> { MarketplaceVisualScreen(user, { nav.openDetail(PassportRoute(it)) }, nav::openParticipantVisualDestination) }
    composable<PassportRoute> { entry ->
        PassportVisualScreen(entry.toRoute<PassportRoute>().resourceId, onMatch = { }, onNavigate = nav::openParticipantVisualDestination)
    }
}

private fun androidx.navigation.NavGraphBuilder.partnerGraph(nav: NavHostController, user: User) {
    composable<PartnerWorkbenchRoute> { PartnerWorkbenchVisualScreen(user, nav::openPartnerVisualDestination) }
    composable<MarketplaceRoute> { MarketplaceVisualScreen(user, { nav.openDetail(PassportRoute(it)) }, nav::openPartnerVisualDestination) }
    composable<PassportRoute> { entry ->
        PassportVisualScreen(entry.toRoute<PassportRoute>().resourceId, onMatch = { }, onNavigate = nav::openPartnerVisualDestination)
    }
    composable<PartnerMapRoute> { PartnerMapVisualScreen(nav::openPartnerVisualDestination) }
}

/** Keeps rapid Account taps from adding duplicate profile destinations to the back stack. */
private fun NavHostController.openProfile() {
    openTopLevel(ProfileRoute)
}

/** The legacy visual bottom bars are adapted to typed routes without letting tab taps grow the stack. */
@Suppress("DEPRECATION")
private fun NavHostController.openOrganiserVisualDestination(screen: ReEventScreen) {
    when (screen) {
        ReEventScreen.Home -> openTopLevel(OrganizerHomeRoute)
        ReEventScreen.Marketplace -> openTopLevel(MarketplaceRoute)
        ReEventScreen.PartnerMap -> openTopLevel(PartnerMapRoute)
        ReEventScreen.Impact -> openTopLevel(OrganizerImpactRoute)
        ReEventScreen.AddResource -> openTopLevel(OrganizerAddRoute)
        ReEventScreen.Profile -> openProfile()
        else -> Unit
    }
}

@Suppress("DEPRECATION")
private fun NavHostController.openParticipantVisualDestination(screen: ReEventScreen) {
    when (screen) {
        ReEventScreen.Home, ReEventScreen.ParticipantReturn -> openTopLevel(ParticipantReturnRoute)
        ReEventScreen.Marketplace -> openTopLevel(MarketplaceRoute)
        ReEventScreen.Profile -> openProfile()
        else -> Unit
    }
}

@Suppress("DEPRECATION")
private fun NavHostController.openPartnerVisualDestination(screen: ReEventScreen) {
    when (screen) {
        ReEventScreen.Home, ReEventScreen.PartnerWorkbench -> openTopLevel(PartnerWorkbenchRoute)
        ReEventScreen.Marketplace -> openTopLevel(MarketplaceRoute)
        ReEventScreen.PartnerMap -> openTopLevel(PartnerMapRoute)
        ReEventScreen.Profile -> openProfile()
        else -> Unit
    }
}

private inline fun <reified T : Any> NavHostController.openTopLevel(route: T) {
    if (currentDestination?.hasRoute<T>() == true) return
    navigate(route) {
        launchSingleTop = true
        // Role tabs always land on their own root. Saving/restoring this stack used to resurrect
        // the passport detail when Home or Add was tapped after a successful resource save.
        popUpTo(graph.findStartDestination().id) { saveState = false }
    }
}

private fun UserRole.toVisualRole() = when (this) {
    UserRole.ORGANIZER -> ReEventRole.Organizer
    UserRole.PARTICIPANT -> ReEventRole.Participant
    UserRole.PARTNER -> ReEventRole.Partner
}

private inline fun <reified T : Any> NavHostController.openDetail(
    route: T,
    crossinline builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {}
) {
    navigate(route) {
        launchSingleTop = true
        builder()
    }
}

/** Prevents a tap intended for a departing screen from activating a control beneath it. */
private class NavigationTapGuard {
    private var blockedUntil = 0L

    fun blockBriefly() {
        blockedUntil = SystemClock.elapsedRealtime() + TAP_GUARD_MILLIS
    }

    fun runIfAllowed(action: () -> Unit) {
        if (SystemClock.elapsedRealtime() >= blockedUntil) action()
    }

    private companion object {
        const val TAP_GUARD_MILLIS = 500L
    }
}
