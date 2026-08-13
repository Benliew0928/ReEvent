package com.reevent.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventLine
import com.reevent.app.ui.theme.ReEventMintSoft
import com.reevent.app.ui.theme.ReEventSurface
import com.reevent.app.ui.theme.ReEventTextSecondary

@Immutable
private data class BottomDestination(
    val destination: TopLevelDestination,
    val label: String,
    val icon: ImageVector,
)

/** The shell is shared, while each authenticated role owns a distinct information architecture. */
val LocalUserRole = staticCompositionLocalOf { UserRole.ORGANIZER }

@Composable
fun ReEventBottomBar(
    selected: TopLevelDestination,
    onNavigate: (TopLevelDestination) -> Unit,
) {
    val destinations =
        when (LocalUserRole.current) {
            UserRole.ORGANIZER -> {
                listOf(
                    BottomDestination(TopLevelDestination.HOME, "Home", Icons.Outlined.Home),
                    BottomDestination(TopLevelDestination.MARKETPLACE, "Market", Icons.Outlined.Search),
                    BottomDestination(TopLevelDestination.EVENTS, "Events", Icons.Outlined.Add),
                    BottomDestination(TopLevelDestination.PARTNERS, "Partners", Icons.Outlined.Map),
                    BottomDestination(TopLevelDestination.IMPACT, "Impact", Icons.Outlined.BarChart),
                )
            }

            UserRole.PARTICIPANT -> {
                listOf(
                    BottomDestination(TopLevelDestination.RETURNS, "Returns", Icons.Outlined.Refresh),
                    BottomDestination(TopLevelDestination.MARKETPLACE, "Resources", Icons.Outlined.Search),
                    BottomDestination(TopLevelDestination.ACCOUNT, "Account", Icons.Outlined.Person),
                )
            }

            UserRole.PARTNER -> {
                listOf(
                    BottomDestination(TopLevelDestination.WORKBENCH, "Workbench", Icons.Outlined.Settings),
                    BottomDestination(TopLevelDestination.MARKETPLACE, "Resources", Icons.Outlined.ShoppingBag),
                    BottomDestination(TopLevelDestination.PARTNERS, "Network", Icons.Outlined.Map),
                    BottomDestination(TopLevelDestination.ACCOUNT, "Account", Icons.Outlined.Person),
                )
            }
        }

    Surface(
        color = ReEventSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, ReEventLine),
        tonalElevation = 0.dp,
    ) {
        NavigationBar(containerColor = ReEventSurface, tonalElevation = 0.dp) {
            destinations.forEach { item ->
                NavigationBarItem(
                    selected = selected == item.destination,
                    onClick = { onNavigate(item.destination) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    alwaysShowLabel = false,
                    colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = ReEventGreenDeep,
                            selectedTextColor = ReEventGreenDeep,
                            indicatorColor = ReEventMintSoft,
                            unselectedIconColor = ReEventTextSecondary,
                            unselectedTextColor = ReEventTextSecondary,
                        ),
                )
            }
        }
    }
}
