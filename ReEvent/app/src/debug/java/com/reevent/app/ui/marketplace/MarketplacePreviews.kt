package com.reevent.app.ui.marketplace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.components.LocalUserRole
import com.reevent.app.ui.theme.ReEventTheme

@Preview(name = "Compass · compact organiser", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun CompactOrganiserMarketplacePreview() = MarketplacePreview(UserRole.ORGANIZER)

@Preview(name = "Compass · reference participant", widthDp = 430, heightDp = 1024, showBackground = true)
@Composable
private fun ReferenceParticipantMarketplacePreview() = MarketplacePreview(UserRole.PARTICIPANT)

@Preview(name = "Compass · tablet partner", widthDp = 900, heightDp = 900, showBackground = true)
@Composable
private fun TabletPartnerMarketplacePreview() = MarketplacePreview(UserRole.PARTNER)

@Preview(name = "Compass · large text", widthDp = 430, heightDp = 1024, fontScale = 1.6f, showBackground = true)
@Composable
private fun LargeTextMarketplacePreview() = MarketplacePreview(UserRole.PARTICIPANT)

@Composable
private fun MarketplacePreview(role: UserRole) {
    val user = User("preview-user", "alex@example.com", "Alex Rivera", role, createdAt = 0, updatedAt = 0)
    val resources = previewResources().map { resource ->
        MarketplaceResourceUi(
            resource = resource,
            isOwner = role == UserRole.ORGANIZER && resource.id == "oak",
            programmeFits = if (role == UserRole.PARTNER) listOf("Circular Partner Programme") else emptyList(),
        )
    }
    val state = MarketplaceUiState(
        role = role,
        resources = resources,
        publishableResources = if (role == UserRole.ORGANIZER) listOf(resources.last().resource.copy(marketplaceListing = null)) else emptyList(),
        resultCount = resources.size,
        activityTitle = when (role) {
            UserRole.ORGANIZER -> "Open marketplace activity"
            UserRole.PARTICIPANT -> "My activity"
            UserRole.PARTNER -> "Programme activity"
        },
    )
    ReEventTheme(darkTheme = false) {
        CompositionLocalProvider(LocalUserRole provides role) {
            MaterialCompassMarketplaceScreen(
                user = user,
                state = state,
                onQuery = {},
                onFamily = {},
                onAction = {},
                onCompassPage = {},
                onClearFilters = {},
                onRefresh = {},
                onNavigate = {},
                onListing = {},
                onRequest = { _, _ -> },
                onPassport = {},
                onPublish = {},
                onApprove = {},
                onCancel = {},
                onComplete = {},
                onInTransit = {},
            )
        }
    }
}

private fun previewResources(): List<ResourceItem> = listOf(
    ResourceItem(
        id = "oak", eventId = "event", ownerId = "preview-user", title = "Reclaimed oak panels",
        category = "Building materials", materialFamily = MaterialFamily.WOOD, materialDetail = "European oak",
        condition = ResourceCondition.GOOD, quantity = 24.0, unit = "panels", status = ResourceStatus.ACTIVE,
        valueCents = 12_000, imageUrls = emptyList(), createdAt = 0, updatedAt = 0, syncState = SyncState.SYNCED,
        marketplaceListing = MarketplaceListing(
            id = "oak-listing", allowedActions = listOf(TransactionType.BORROW, TransactionType.DONATE),
            publishedQuantity = 24.0, terms = "Collection by arrangement",
        ),
    ),
    ResourceItem(
        id = "linen", eventId = "event", ownerId = "another-owner", title = "Natural linen tablecloths",
        category = "Event textiles", materialFamily = MaterialFamily.TEXTILES, materialDetail = "Linen",
        condition = ResourceCondition.GOOD, quantity = 40.0, unit = "items", status = ResourceStatus.ACTIVE,
        valueCents = 8_000, imageUrls = emptyList(), createdAt = 0, updatedAt = 0, syncState = SyncState.SYNCED,
        marketplaceListing = MarketplaceListing(
            id = "linen-listing", allowedActions = listOf(TransactionType.RENT, TransactionType.BUY),
            publishedQuantity = 40.0, rentUnitPrice = 250, defaultDurationDays = 3,
        ),
    ),
)
