package com.reevent.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.reevent.app.core.auth.AppEntry
import com.reevent.app.core.auth.SessionViewModel
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.screens.AddResourceLiveScreen
import com.reevent.app.ui.screens.CompleteRoleFlowScreen
import com.reevent.app.ui.screens.ImpactLiveScreen
import com.reevent.app.ui.screens.MatchingLiveScreen
import com.reevent.app.ui.screens.MarketplaceLiveScreen
import com.reevent.app.ui.screens.OnboardingFlowScreen
import com.reevent.app.ui.screens.OrganizerHomeLiveScreen
import com.reevent.app.ui.screens.ParticipantReturnLiveScreen
import com.reevent.app.ui.screens.PartnerMapLiveScreen
import com.reevent.app.ui.screens.PartnerWorkbenchLiveScreen
import com.reevent.app.ui.screens.PassportLiveScreen
import com.reevent.app.ui.screens.ProfileFlowScreen
import com.reevent.app.ui.screens.SignInFlowScreen
import com.reevent.app.ui.theme.ReEventBackground
import kotlinx.serialization.Serializable

@Serializable private data object OrganizerHomeRoute
@Serializable private data object ParticipantReturnRoute
@Serializable private data object PartnerWorkbenchRoute
@Serializable private data object MarketplaceRoute
@Serializable private data class AddResourceRoute(val eventId: String)
@Serializable private data class PassportRoute(val resourceId: String)
@Serializable private data class MatchingRoute(val resourceId: String)
@Serializable private data object PartnerMapRoute
@Serializable private data class ImpactRoute(val eventId: String)
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
    val start = when (role) {
        UserRole.ORGANIZER -> OrganizerHomeRoute
        UserRole.PARTICIPANT -> ParticipantReturnRoute
        UserRole.PARTNER -> PartnerWorkbenchRoute
    }
    NavHost(navController = nav, startDestination = start) {
        when (role) {
            UserRole.ORGANIZER -> organiserGraph(nav, user)
            UserRole.PARTICIPANT -> participantGraph(nav, user)
            UserRole.PARTNER -> partnerGraph(nav, user)
        }
        composable<ProfileRoute> { ProfileFlowScreen(user, onBack = { nav.popBackStack() }) }
    }
}

private fun androidx.navigation.NavGraphBuilder.organiserGraph(nav: NavHostController, user: User) {
    composable<OrganizerHomeRoute> { OrganizerHomeLiveScreen(user, { nav.navigate(AddResourceRoute(it)) }, { nav.navigate(PassportRoute(it)) }, { nav.navigate(ImpactRoute(it)) }, { nav.navigate(ProfileRoute) { launchSingleTop = true } }) }
    composable<AddResourceRoute> { entry -> AddResourceLiveScreen(user, entry.toRoute<AddResourceRoute>().eventId, { nav.navigate(PassportRoute(it)) { popUpTo<OrganizerHomeRoute>() } }, { nav.popBackStack() }) }
    composable<PassportRoute> { entry -> PassportLiveScreen(entry.toRoute<PassportRoute>().resourceId, { nav.navigate(MatchingRoute(it)) }, { nav.popBackStack() }) }
    composable<MatchingRoute> { entry -> MatchingLiveScreen(entry.toRoute<MatchingRoute>().resourceId, { nav.popBackStack() }) }
    composable<ImpactRoute> { entry -> ImpactLiveScreen(entry.toRoute<ImpactRoute>().eventId, { nav.popBackStack() }) }
    composable<MarketplaceRoute> { MarketplaceLiveScreen(user, { nav.navigate(PassportRoute(it)) }, { nav.navigate(ProfileRoute) { launchSingleTop = true } }) }
    composable<PartnerMapRoute> { PartnerMapLiveScreen(onBack = { nav.popBackStack() }) }
}

private fun androidx.navigation.NavGraphBuilder.participantGraph(nav: NavHostController, user: User) {
    composable<ParticipantReturnRoute> { ParticipantReturnLiveScreen(user, { nav.navigate(MarketplaceRoute) }, { nav.navigate(ProfileRoute) { launchSingleTop = true } }) }
    composable<MarketplaceRoute> { MarketplaceLiveScreen(user, { nav.navigate(PassportRoute(it)) }, { nav.navigate(ProfileRoute) { launchSingleTop = true } }) }
    composable<PassportRoute> { entry -> PassportLiveScreen(entry.toRoute<PassportRoute>().resourceId, onMatch = { }, onBack = { nav.popBackStack() }) }
}

private fun androidx.navigation.NavGraphBuilder.partnerGraph(nav: NavHostController, user: User) {
    composable<PartnerWorkbenchRoute> { PartnerWorkbenchLiveScreen(user, { nav.navigate(PartnerMapRoute) }, { nav.navigate(ProfileRoute) { launchSingleTop = true } }) }
    composable<PartnerMapRoute> { PartnerMapLiveScreen(onBack = { nav.popBackStack() }) }
}
