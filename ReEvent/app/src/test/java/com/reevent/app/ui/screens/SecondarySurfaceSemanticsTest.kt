package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.MaterialFamily
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryResult
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
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecondarySurfaceSemanticsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `matching surface exposes resource route facts and callbacks`() {
        var mapOpened = false
        var chosenProgramme: String? = null
        val candidate = PartnerCandidate(programme = programme, distanceKm = 2.4, score = 86, reasons = listOf("Wood is accepted"))

        compose.setContent {
            ReEventTheme {
                MatchingEditorialContent(
                    user = organiser,
                    resource = resource,
                    eventLocation = "Kuala Lumpur",
                    discovery = PartnerDiscoveryResult(candidates = listOf(candidate)),
                    loading = false,
                    error = null,
                    notice = null,
                    onBack = {},
                    onOpenMap = { mapOpened = true },
                    onRetry = {},
                    onCandidate = { chosenProgramme = it.programme.id },
                )
            }
        }

        compose.onNodeWithText("Find the right route").assertIsDisplayed()
        compose.onNodeWithText("Reclaimed oak panels").assertIsDisplayed()
        compose.onNodeWithTag("matching_editorial_grid").performScrollToIndex(2)
        compose.onNodeWithText("View routes on map").performClick()
        assertTrue(mapOpened)
        compose.onNodeWithTag("matching_editorial_grid").performScrollToIndex(4)
        compose.onNodeWithText("Timber Recovery Collective").performClick()
        assertEquals("programme", chosenProgramme)
    }

    @Test
    fun `matching empty and error states remain actionable`() {
        var retried = false
        compose.setContent {
            ReEventTheme {
                MatchingEditorialContent(
                    user = organiser,
                    resource = resource,
                    eventLocation = "Kuala Lumpur",
                    discovery = PartnerDiscoveryResult(),
                    loading = false,
                    error = "Programme routes could not be refreshed.",
                    notice = null,
                    onBack = {},
                    onOpenMap = {},
                    onRetry = { retried = true },
                    onCandidate = {},
                )
            }
        }

        compose.onNodeWithTag("matching_editorial_grid").performScrollToIndex(3)
        compose.onNodeWithText("Programme routes could not be refreshed.").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertTrue(retried)
    }

    @Test
    fun `lifecycle card exposes passport and authorised next step`() {
        var passportOpened = false
        var approved = false
        val request = transaction.copy(status = TransactionStatus.REQUESTED)

        compose.setContent {
            ReEventTheme {
                TransactionCard(
                    user = organiser,
                    transaction = request,
                    resource = resource,
                    syncCommand = null,
                    onApprove = { approved = true },
                    onCancel = {},
                    onComplete = {},
                    onInTransit = {},
                    onPassport = { passportOpened = true },
                )
            }
        }

        compose.onNodeWithText("NEXT STEP").assertIsDisplayed()
        compose.onNodeWithText("Open passport").performClick()
        compose.onNodeWithText("Approve request").performClick()
        assertTrue(passportOpened)
        assertTrue(approved)
    }

    @Test
    fun `impact surface uses authoritative unavailable and metric labels`() {
        compose.setContent {
            ReEventTheme {
                ImpactScreen(
                    onNavigate = {},
                    onProfile = {},
                    metrics = listOf(ImpactMetric("214 kg", "Materials diverted", "Verified completed recoveries")),
                    recoveryRate = null,
                    recoveryLabel = "—",
                    unavailableEstimateReason = "A verified mass estimate is unavailable.",
                    selectedScope = ImpactEventScope("event", "Spring Makers Market"),
                )
            }
        }

        compose.onNodeWithText("Impact, made visible").assertIsDisplayed()
        compose.onNodeWithText("Spring Makers Market").assertIsDisplayed()
        compose.onNodeWithText("A verified mass estimate is unavailable.", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag("impact_editorial_list").performScrollToNode(hasText("Materials diverted"))
        compose.onNodeWithText("Materials diverted").assertIsDisplayed()
    }

    @Test
    fun `personal and notification pages disclose local only values`() {
        compose.setContent {
            ReEventTheme {
                PersonalInfoScreen(
                    userDisplayName = "Alex Rivera",
                    phoneNumber = null,
                    gender = null,
                    onEditName = {},
                    onEditPhone = {},
                    onEditGender = {},
                    onBack = {},
                )
            }
        }

        compose.onNodeWithText("not uploaded", substring = true).assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    private val organiser = User(
        id = "organiser",
        email = "alex@example.com",
        displayName = "Alex Rivera",
        role = UserRole.ORGANIZER,
        createdAt = 0,
        updatedAt = 0,
    )

    private val resource = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = organiser.id,
        title = "Reclaimed oak panels",
        category = "Event signage",
        materialFamily = MaterialFamily.WOOD,
        materialDetail = "Oak",
        condition = ResourceCondition.GOOD,
        quantity = 12.0,
        unit = "panels",
        status = ResourceStatus.ACTIVE,
        valueCents = 0,
        imageUrls = emptyList(),
        createdAt = 0,
        updatedAt = 0,
        syncState = SyncState.SYNCED,
    )

    private val programme = CircularProgramme(
        id = "programme",
        partnerId = "partner",
        name = "Timber Recovery Collective",
        type = ProgrammeType.REPAIR,
        acceptedMaterialFamilies = setOf(MaterialFamily.WOOD),
        location = "Kuala Lumpur",
        active = true,
        createdAt = 0,
        updatedAt = 0,
        syncState = SyncState.SYNCED,
        remainingCapacity = 40.0,
        unit = "panels",
        pickupAvailable = true,
    )

    private val transaction = CircularTransaction(
        id = "transaction",
        eventId = "event",
        resourceId = resource.id,
        senderId = organiser.id,
        receiverId = "participant",
        partnerId = null,
        type = TransactionType.BORROW,
        status = TransactionStatus.APPROVED,
        quantity = 4.0,
        createdAt = 0,
        updatedAt = 0,
        syncState = SyncState.SYNCED,
    )
}
