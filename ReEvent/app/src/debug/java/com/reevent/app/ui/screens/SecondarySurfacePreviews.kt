package com.reevent.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.PartnerOriginSource
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.components.LocalUserRole
import com.reevent.app.ui.theme.ReEventTheme

@Preview(name = "Matches · compact", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun CompactMatchingPreview() = MatchingPreview()

@Preview(name = "Matches · reference", widthDp = 430, heightDp = 1024, showBackground = true)
@Composable
private fun ReferenceMatchingPreview() = MatchingPreview()

@Preview(name = "Matches · two pane", widthDp = 920, heightDp = 900, showBackground = true)
@Composable
private fun TabletMatchingPreview() = MatchingPreview()

@Preview(name = "Lifecycle task", widthDp = 430, heightDp = 700, showBackground = true)
@Composable
private fun LifecycleTaskPreview() {
    ReEventTheme(darkTheme = false) {
        TransactionCard(
            user = previewUser,
            transaction = previewTransaction,
            resource = previewResource,
            syncCommand = null,
            onApprove = {},
            onCancel = {},
            onComplete = {},
            onInTransit = {},
            onPassport = {},
            modifier = Modifier,
        )
    }
}

@Preview(name = "Impact · reference", widthDp = 430, heightDp = 1024, showBackground = true)
@Composable
private fun ImpactPreview() {
    ReEventTheme(darkTheme = false) {
        CompositionLocalProvider(LocalUserRole provides UserRole.ORGANIZER) {
            ImpactScreen(
                onNavigate = {},
                onProfile = {},
                metrics = listOf(
                    ImpactMetric("214 kg", "Materials diverted", "Verified completed recoveries"),
                    ImpactMetric("1.3 t", "CO2e avoided", "Documented impact factors"),
                    ImpactMetric("53", "Recovered lots", "Completed circular routes"),
                ),
                recoveryRate = .68f,
                recoveryLabel = "68%",
                chartValues = listOf(.82f, .55f, .38f, .67f),
                selectedScope = ImpactEventScope("event", "Spring Makers Market"),
                scopes = listOf(ImpactEventScope("event", "Spring Makers Market")),
            )
        }
    }
}

@Composable
private fun MatchingPreview() {
    ReEventTheme(darkTheme = false) {
        MatchingEditorialContent(
            user = previewUser,
            resource = previewResource,
            eventLocation = "Kuala Lumpur Convention Centre",
            discovery = PartnerDiscoveryResult(
                origin = GeoLocation("Kuala Lumpur Convention Centre", 3.1538, 101.7123),
                originSource = PartnerOriginSource.RESOURCE,
                candidates = listOf(PartnerCandidate(previewProgramme, 2.4, 86, listOf("Wood is accepted", "Quantity is within programme capacity", "Pickup is available"))),
            ),
            loading = false,
            error = null,
            notice = null,
            onBack = {},
            onOpenMap = {},
            onRetry = {},
            onCandidate = {},
        )
    }
}

private val previewUser = User(
    id = "organiser",
    email = "alex@example.com",
    displayName = "Alex Rivera",
    role = UserRole.ORGANIZER,
    createdAt = 0,
    updatedAt = 0,
)

private val previewResource = ResourceItem(
    id = "resource",
    eventId = "event",
    ownerId = "organiser",
    title = "Reclaimed oak display panels",
    category = "Event signage",
    materialFamily = MaterialFamily.WOOD,
    materialDetail = "European oak",
    condition = ResourceCondition.GOOD,
    quantity = 12.0,
    unit = "panels",
    status = ResourceStatus.ACTIVE,
    valueCents = 12_000,
    imageUrls = emptyList(),
    createdAt = 0,
    updatedAt = 0,
    syncState = SyncState.SYNCED,
    geoLocation = GeoLocation("Kuala Lumpur Convention Centre", 3.1538, 101.7123),
)

private val previewProgramme = CircularProgramme(
    id = "programme",
    partnerId = "partner",
    name = "Timber Recovery Collective",
    type = ProgrammeType.REPAIR,
    acceptedMaterialFamilies = setOf(MaterialFamily.WOOD),
    location = "Bukit Bintang, Kuala Lumpur",
    active = true,
    createdAt = 0,
    updatedAt = 0,
    remainingCapacity = 80.0,
    unit = "panels",
    coinDirection = CoinDirection.FREE,
    pickupAvailable = true,
)

private val previewTransaction = CircularTransaction(
    id = "transaction",
    eventId = "event",
    resourceId = "resource",
    senderId = "organiser",
    receiverId = "participant",
    partnerId = null,
    type = TransactionType.BORROW,
    status = TransactionStatus.APPROVED,
    quantity = 4.0,
    createdAt = 0,
    updatedAt = 0,
    syncState = SyncState.SYNCED,
)

