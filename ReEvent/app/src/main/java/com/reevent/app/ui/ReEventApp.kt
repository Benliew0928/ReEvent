package com.reevent.app.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.reevent.app.core.auth.AppEntry
import com.reevent.app.core.auth.SessionViewModel
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.feature.passports.PassportAppLink
import com.reevent.app.ui.components.LocalResourcePhotoLoader
import com.reevent.app.ui.components.LocalUserRole
import com.reevent.app.ui.screens.AccountDeletionPendingFlowScreen
import com.reevent.app.ui.screens.AddResourceLiveScreen
import com.reevent.app.ui.screens.CompleteRoleFlowScreen
import com.reevent.app.ui.screens.EventDetailLiveScreen
import com.reevent.app.ui.screens.EventEditorLiveScreen
import com.reevent.app.ui.screens.EventListLiveScreen
import com.reevent.app.ui.screens.FeatureViewModel
import com.reevent.app.ui.screens.MarketplaceVisualScreen
import com.reevent.app.ui.screens.MatchingLiveScreen
import com.reevent.app.ui.screens.OnboardingFlowScreen
import com.reevent.app.ui.screens.OrganizerHomeVisualScreen
import com.reevent.app.ui.screens.OrganizerImpactVisualScreen
import com.reevent.app.ui.screens.ParticipantReturnVisualScreen
import com.reevent.app.ui.screens.PartnerMapVisualScreen
import com.reevent.app.ui.screens.PartnerWorkbenchVisualScreen
import com.reevent.app.ui.screens.PassportVisualScreen
import com.reevent.app.ui.screens.PasswordRecoveryFlowScreen
import com.reevent.app.ui.screens.ProfileFlowScreen
import com.reevent.app.ui.screens.QrScannerLiveScreen
import com.reevent.app.ui.screens.ResourceEditorLiveScreen
import com.reevent.app.ui.screens.SignInFlowScreen
import com.reevent.app.ui.theme.ReEventBackground
import kotlinx.serialization.Serializable

@Serializable private data object OrganizerHomeRoute

@Serializable private data object ParticipantReturnRoute

@Serializable private data object PartnerWorkbenchRoute

@Serializable private data object MarketplaceRoute

@Serializable private data class OrganizerAddRoute(
    val eventId: String,
)

@Serializable private data object EventListRoute

@Serializable private data class EventEditorRoute(
    val eventId: String? = null,
)

@Serializable private data class EventDetailRoute(
    val eventId: String,
)

@Serializable private data class ResourceEditorRoute(
    val eventId: String,
    val resourceId: String,
)

@Serializable private data class PassportRoute(
    val resourceId: String,
)

@Serializable private data class QrScannerRoute(
    val initialPayload: String? = null,
)

@Serializable private data class MatchingRoute(
    val resourceId: String,
)

@Serializable private data class PartnerMapRoute(val resourceId: String? = null)

@Serializable private data object OrganizerImpactRoute

@Serializable private data object ProfileRoute

@Composable
fun ReEventApp() {
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val session by sessionViewModel.state.collectAsState()
    when (session.entry) {
        AppEntry.LOADING -> {
            LoadingScreen()
        }

        AppEntry.ONBOARDING -> {
            OnboardingFlowScreen(sessionViewModel::completeOnboarding)
        }

        AppEntry.SIGN_IN -> {
            SignInFlowScreen()
        }

        AppEntry.PASSWORD_RESET -> {
            PasswordRecoveryFlowScreen()
        }

        AppEntry.DELETION_PENDING -> {
            AccountDeletionPendingFlowScreen(requireNotNull(session.user))
        }

        AppEntry.COMPLETE_ROLE -> {
            CompleteRoleFlowScreen()
        }

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
private fun LoadingScreen() =
    Surface(color = ReEventBackground, modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }

@Composable
private fun RoleNavigationRoot(
    user: User,
    role: UserRole,
) {
    val nav = rememberNavController()
    val navigationTapGuard = remember { NavigationTapGuard() }
    val featureViewModel: FeatureViewModel = hiltViewModel()
    val pendingPassportPayload by PassportAppLink.pendingPayload.collectAsState()
    val start =
        when (role) {
            UserRole.ORGANIZER -> OrganizerHomeRoute
            UserRole.PARTICIPANT -> ParticipantReturnRoute
            UserRole.PARTNER -> PartnerWorkbenchRoute
        }
    CompositionLocalProvider(
        LocalUserRole provides role,
        LocalResourcePhotoLoader provides featureViewModel::resourcePhoto,
    ) {
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
                    onNavigate =
                        when (role) {
                            UserRole.ORGANIZER -> nav::openOrganiserTopLevelDestination
                            UserRole.PARTICIPANT -> nav::openParticipantTopLevelDestination
                            UserRole.PARTNER -> nav::openPartnerTopLevelDestination
                        },
                )
            }
        }
    }
    LaunchedEffect(pendingPassportPayload) {
        val payload = pendingPassportPayload ?: return@LaunchedEffect
        nav.openDetail(QrScannerRoute(payload))
        PassportAppLink.consume(payload)
    }
}

private fun androidx.navigation.NavGraphBuilder.organiserGraph(
    nav: NavHostController,
    user: User,
    navigationTapGuard: NavigationTapGuard,
) {
    composable<OrganizerHomeRoute> {
        OrganizerHomeVisualScreen(
            user = user,
            onAddResource = { nav.openDetail(OrganizerAddRoute(it)) },
            onPassport = { nav.openDetail(PassportRoute(it)) },
            onImpact = { nav.openTopLevel(OrganizerImpactRoute) },
            onMarketplace = { nav.openTopLevel(MarketplaceRoute) },
            onPartnerMap = { nav.openTopLevel(PartnerMapRoute()) },
            onManageEvents = { nav.openDetail(EventListRoute) },
            onProfile = { navigationTapGuard.runIfAllowed(nav::openProfile) },
        )
    }
    composable<OrganizerAddRoute> { entry ->
        val eventId = entry.toRoute<OrganizerAddRoute>().eventId
        AddResourceLiveScreen(
            user = user,
            eventId = eventId,
            onSaved = { nav.openDetail(EventDetailRoute(eventId)) },
            onBack = {
                navigationTapGuard.blockBriefly()
                nav.popBackStack()
            },
            onNavigate = nav::openOrganiserTopLevelDestination,
        )
    }
    composable<EventListRoute> {
        EventListLiveScreen(
            user = user,
            onCreate = { nav.openDetail(EventEditorRoute()) },
            onOpen = { nav.openDetail(EventDetailRoute(it)) },
            onBack = nav::popBackStack,
            onNavigate = nav::openOrganiserTopLevelDestination,
        )
    }
    composable<EventEditorRoute> { entry ->
        val eventId = entry.toRoute<EventEditorRoute>().eventId
        EventEditorLiveScreen(
            user = user,
            eventId = eventId,
            onSaved = { nav.openDetail(EventDetailRoute(it)) },
            onBack = nav::popBackStack,
            onNavigate = nav::openOrganiserTopLevelDestination,
        )
    }
    composable<EventDetailRoute> { entry ->
        val eventId = entry.toRoute<EventDetailRoute>().eventId
        EventDetailLiveScreen(
            eventId = eventId,
            onEditEvent = { nav.openDetail(EventEditorRoute(eventId)) },
            onAddResource = { nav.openDetail(OrganizerAddRoute(eventId)) },
            onScanResourceQr = { nav.openDetail(QrScannerRoute()) },
            onEditResource = { nav.openDetail(ResourceEditorRoute(eventId, it)) },
            onOpenPassport = { nav.openDetail(PassportRoute(it)) },
            onArchiveEvent = { nav.openTopLevel(EventListRoute) },
            onBack = nav::popBackStack,
            onNavigate = nav::openOrganiserTopLevelDestination,
        )
    }
    composable<ResourceEditorRoute> { entry ->
        val route = entry.toRoute<ResourceEditorRoute>()
        ResourceEditorLiveScreen(
            user,
            route.eventId,
            route.resourceId,
            nav::popBackStack,
            nav::popBackStack,
            nav::openOrganiserTopLevelDestination,
        )
    }
    composable<PassportRoute> { entry ->
        PassportVisualScreen(
            user = user,
            resourceId = entry.toRoute<PassportRoute>().resourceId,
            onMatch = { nav.openDetail(MatchingRoute(it)) },
            onBack = nav::popBackStack,
            onNavigate = nav::openOrganiserTopLevelDestination,
        )
    }
    composable<QrScannerRoute> { entry ->
        QrScannerLiveScreen(
            user,
            { nav.openDetail(PassportRoute(it)) },
            nav::popBackStack,
            entry.toRoute<QrScannerRoute>().initialPayload,
        )
    }
    composable<MatchingRoute> { entry ->
        val resourceId = entry.toRoute<MatchingRoute>().resourceId
        MatchingLiveScreen(
            user = user,
            resourceId = resourceId,
            onBack = { nav.popBackStack() },
            onOpenMap = { nav.openDetail(PartnerMapRoute(resourceId)) },
        )
    }
    composable<OrganizerImpactRoute> { OrganizerImpactVisualScreen(user, nav::openOrganiserTopLevelDestination) }
    composable<MarketplaceRoute> {
        MarketplaceVisualScreen(
            user,
            { nav.openDetail(PassportRoute(it)) },
            nav::openOrganiserTopLevelDestination,
        )
    }
    composable<PartnerMapRoute> { entry ->
        val route = entry.toRoute<PartnerMapRoute>()
        PartnerMapVisualScreen(
            user = user,
            resourceId = route.resourceId,
            onNavigate = nav::openOrganiserTopLevelDestination,
            onOpenPassport = { nav.openDetail(PassportRoute(it)) },
            onBack = if (route.resourceId == null) null else {{ nav.popBackStack() }},
        )
    }
}

private fun androidx.navigation.NavGraphBuilder.participantGraph(
    nav: NavHostController,
    user: User,
) {
    composable<ParticipantReturnRoute> {
        ParticipantReturnVisualScreen(user, { nav.openDetail(QrScannerRoute()) }, nav::openParticipantTopLevelDestination)
    }
    composable<MarketplaceRoute> {
        MarketplaceVisualScreen(user, { nav.openDetail(PassportRoute(it)) }, nav::openParticipantTopLevelDestination)
    }
    composable<PassportRoute> { entry ->
        PassportVisualScreen(user, entry.toRoute<PassportRoute>().resourceId, onMatch = {
        }, onBack = nav::popBackStack, onNavigate = nav::openParticipantTopLevelDestination)
    }
    composable<PartnerMapRoute> { entry ->
        val route = entry.toRoute<PartnerMapRoute>()
        PartnerMapVisualScreen(
            user = user,
            resourceId = route.resourceId,
            onNavigate = nav::openParticipantTopLevelDestination,
            onOpenPassport = { nav.openDetail(PassportRoute(it)) },
            onBack = if (route.resourceId == null) null else {{ nav.popBackStack() }},
        )
    }
    composable<QrScannerRoute> { entry ->
        QrScannerLiveScreen(
            user,
            { nav.openDetail(PassportRoute(it)) },
            nav::popBackStack,
            entry.toRoute<QrScannerRoute>().initialPayload,
        )
    }
}

private fun androidx.navigation.NavGraphBuilder.partnerGraph(
    nav: NavHostController,
    user: User,
) {
    composable<PartnerWorkbenchRoute> {
        PartnerWorkbenchVisualScreen(
            user = user,
            onNavigate = nav::openPartnerTopLevelDestination,
            onOpenPassport = { nav.openDetail(PassportRoute(it)) },
        )
    }
    composable<MarketplaceRoute> {
        MarketplaceVisualScreen(
            user,
            { nav.openDetail(PassportRoute(it)) },
            nav::openPartnerTopLevelDestination,
        )
    }
    composable<PassportRoute> { entry ->
        PassportVisualScreen(user, entry.toRoute<PassportRoute>().resourceId, onMatch = {
        }, onBack = nav::popBackStack, onNavigate = nav::openPartnerTopLevelDestination)
    }
    composable<QrScannerRoute> { entry ->
        QrScannerLiveScreen(
            user,
            { nav.openDetail(PassportRoute(it)) },
            nav::popBackStack,
            entry.toRoute<QrScannerRoute>().initialPayload,
        )
    }
}

/** Keeps rapid Account taps from adding duplicate profile destinations to the back stack. */
private fun NavHostController.openProfile() {
    openTopLevel(ProfileRoute)
}

private fun NavHostController.openOrganiserTopLevelDestination(destination: TopLevelDestination) {
    when (destination) {
        TopLevelDestination.HOME -> openTopLevel(OrganizerHomeRoute)
        TopLevelDestination.MARKETPLACE -> openTopLevel(MarketplaceRoute)
        TopLevelDestination.EVENTS -> openTopLevel(EventListRoute)
        TopLevelDestination.PARTNERS -> openTopLevel(PartnerMapRoute())
        TopLevelDestination.IMPACT -> openTopLevel(OrganizerImpactRoute)
        TopLevelDestination.ACCOUNT -> openProfile()
        else -> Unit
    }
}

private fun NavHostController.openParticipantTopLevelDestination(destination: TopLevelDestination) {
    when (destination) {
        TopLevelDestination.RETURNS -> openTopLevel(ParticipantReturnRoute)
        TopLevelDestination.MARKETPLACE -> openTopLevel(MarketplaceRoute)
        TopLevelDestination.PARTNERS -> openTopLevel(PartnerMapRoute())
        TopLevelDestination.ACCOUNT -> openProfile()
        else -> Unit
    }
}

private fun NavHostController.openPartnerTopLevelDestination(destination: TopLevelDestination) {
    when (destination) {
        TopLevelDestination.WORKBENCH -> openTopLevel(PartnerWorkbenchRoute)
        TopLevelDestination.MARKETPLACE -> openTopLevel(MarketplaceRoute)
        TopLevelDestination.ACCOUNT -> openProfile()
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

private inline fun <reified T : Any> NavHostController.openDetail(
    route: T,
    crossinline builder: androidx.navigation.NavOptionsBuilder.() -> Unit = {},
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
