package com.reevent.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMist
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper

@Immutable
private data class NavigationDestination(
    val destination: TopLevelDestination,
    val label: String,
    val icon: ImageVector,
)

val LocalUserRole = staticCompositionLocalOf { UserRole.ORGANIZER }

@Composable
fun ReEventBottomBar(
    selected: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        NavigationBar(containerColor = HomePaper, tonalElevation = 0.dp) {
            navigationDestinations(LocalUserRole.current).forEach { item ->
                NavigationBarItem(
                    selected = selected == item.destination,
                    onClick = { onNavigate(item.destination) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    alwaysShowLabel = true,
                    modifier = Modifier.testTag("nav_${item.destination.name.lowercase()}"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HomeForest,
                        selectedTextColor = HomeForest,
                        indicatorColor = HomeMist,
                        unselectedIconColor = HomeMuted,
                        unselectedTextColor = HomeMuted,
                    ),
                )
            }
        }
    }
}

@Composable
fun ReEventNavigationRail(
    selected: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = HomePaper,
        border = BorderStroke(1.dp, HomeLine),
        tonalElevation = 0.dp,
    ) {
        NavigationRail(containerColor = HomePaper) {
            navigationDestinations(LocalUserRole.current).forEach { item ->
                NavigationRailItem(
                    selected = selected == item.destination,
                    onClick = { onNavigate(item.destination) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    alwaysShowLabel = true,
                    modifier = Modifier.testTag("nav_${item.destination.name.lowercase()}"),
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = HomeForest,
                        selectedTextColor = HomeForest,
                        indicatorColor = HomeMist,
                        unselectedIconColor = HomeMuted,
                        unselectedTextColor = HomeMuted,
                    ),
                )
            }
        }
    }
}

private fun navigationDestinations(role: UserRole): List<NavigationDestination> = when (role) {
    UserRole.ORGANIZER -> listOf(
        NavigationDestination(TopLevelDestination.HOME, "Home", Icons.Outlined.Home),
        NavigationDestination(TopLevelDestination.MARKETPLACE, "Market", Icons.Outlined.Search),
        NavigationDestination(TopLevelDestination.EVENTS, "Events", Icons.Outlined.CalendarMonth),
        NavigationDestination(TopLevelDestination.PARTNERS, "Partners", Icons.Outlined.Map),
        NavigationDestination(TopLevelDestination.IMPACT, "Impact", Icons.Outlined.BarChart),
    )

    UserRole.PARTICIPANT -> listOf(
        NavigationDestination(TopLevelDestination.HOME, "Home", Icons.Outlined.Home),
        NavigationDestination(TopLevelDestination.MARKETPLACE, "Resources", Icons.Outlined.Search),
        NavigationDestination(TopLevelDestination.EVENTS, "Events", Icons.Outlined.CalendarMonth),
        NavigationDestination(TopLevelDestination.PARTNERS, "Partners", Icons.Outlined.Map),
        NavigationDestination(TopLevelDestination.ACCOUNT, "Account", Icons.Outlined.Person),
    )

    UserRole.PARTNER -> listOf(
        NavigationDestination(TopLevelDestination.HOME, "Home", Icons.Outlined.Home),
        NavigationDestination(TopLevelDestination.MARKETPLACE, "Resources", Icons.Outlined.Search),
        NavigationDestination(TopLevelDestination.EVENTS, "Events", Icons.Outlined.CalendarMonth),
        NavigationDestination(TopLevelDestination.PROGRAMMES, "Programmes", Icons.Outlined.Dashboard),
        NavigationDestination(TopLevelDestination.ACCOUNT, "Account", Icons.Outlined.Person),
    )
}
