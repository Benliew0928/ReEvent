package com.reevent.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.User
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.components.EditorialDetailHeader
import com.reevent.app.ui.components.EditorialDetailScaffold
import com.reevent.app.ui.components.EditorialEmptyState
import com.reevent.app.ui.components.ReEventLazyColumn
import com.reevent.app.ui.theme.HomeBodyStyle
import com.reevent.app.ui.theme.HomeCardTitleStyle
import com.reevent.app.ui.theme.HomeForest
import com.reevent.app.ui.theme.HomeInk
import com.reevent.app.ui.theme.HomeLine
import com.reevent.app.ui.theme.HomeMuted
import com.reevent.app.ui.theme.HomePaper

@Composable
fun PartnerPassportListScreen(
    user: User,
    onOpenPassport: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeDashboardViewModel = hiltViewModel(),
) {
    val resourceFlow = remember(user.id) { viewModel.partnerPassportResources(user) }
    val passportFlow = remember(user.id) { viewModel.partnerPassports(user) }
    val resources by resourceFlow.collectAsState(emptyList())
    val passports by passportFlow.collectAsState(emptyList())
    val availableResourceIds = passports.map { it.resourceId }.toSet()
    EditorialDetailScaffold(
        selected = TopLevelDestination.PROGRAMMES,
        onNavigate = onNavigate,
        modifier = modifier,
        showNavigation = false,
    ) { padding ->
        ReEventLazyColumn(paddingValues = padding) {
            item {
                EditorialDetailHeader(
                    eyebrow = "Partner access",
                    title = "Resource passports",
                    subtitle = "Verified resource histories shared through your programme transactions.",
                    onBack = onBack,
                    onProfile = { onNavigate(TopLevelDestination.ACCOUNT) },
                    profileName = user.displayName,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (resources.isEmpty()) {
                item {
                    EditorialEmptyState(
                        title = "No authorised passports",
                        detail = "Resources appear here after a programme transaction grants this partner access.",
                        icon = Icons.Outlined.Badge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            items(resources.distinctBy(ResourceItem::id), key = ResourceItem::id) { resource ->
                val available = resource.id in availableResourceIds
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = available) { onOpenPassport(resource.id) },
                    shape = RoundedCornerShape(17.dp),
                    color = HomePaper,
                    border = BorderStroke(1.dp, HomeLine),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            Icons.Outlined.Badge,
                            contentDescription = "Passport for ${resource.title}",
                            tint = if (available) HomeForest else HomeMuted,
                        )
                        Column(Modifier.weight(1f)) {
                            Text(resource.title, style = HomeCardTitleStyle, color = HomeInk)
                            Text(
                                if (available) "${resource.materialLabel} · ${resource.quantity} ${resource.unit}" else "Passport is not available yet",
                                style = HomeBodyStyle,
                                color = HomeMuted,
                            )
                        }
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = if (available) "Open passport" else null,
                            tint = if (available) HomeForest else HomeLine,
                        )
                    }
                }
            }
        }
    }
}
