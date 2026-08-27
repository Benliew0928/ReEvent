package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.reevent.app.feature.passports.PassportViewerAccess
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.ResourceCardModel
import com.reevent.app.ui.ResourceTone
import com.reevent.app.ui.TopLevelDestination
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PassportScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `verified passport retains callbacks and opens a dismissible QR preview`() {
        var backCount = 0
        var profileCount = 0
        var matchCount = 0
        var destination: TopLevelDestination? = null

        compose.setContent {
            ReEventTheme {
                PassportScreen(
                    onNavigate = { destination = it },
                    onBack = { backCount += 1 },
                    onProfile = { profileCount += 1 },
                    onMatch = { matchCount += 1 },
                    item = resource(),
                    passportId = "REV-2048-81",
                    qrPayload = "https://verify.reevent.example/p/v1/AbCdEfGhIjKlMnOpQrStUv",
                    viewerAccess = access(),
                    recommendedAction = "Match with a reuse partner",
                    recoverySteps = recoverySteps(),
                    showMatchAction = true,
                )
            }
        }

        compose.onNodeWithTag("passport_back").performClick()
        compose.onNodeWithTag("passport_profile").performClick()
        compose.onNodeWithContentDescription("Events").performClick()
        compose.onNodeWithTag("passport_qr_expand").performClick()
        compose.onNodeWithTag("passport_qr_dialog").assertIsDisplayed()
        compose.onNodeWithText("REV-2048-81").assertIsDisplayed()
        compose.onNodeWithTag("passport_qr_dialog_close").performClick()
        compose.onNodeWithTag("passport_qr_dialog").assertDoesNotExist()
        compose.onNodeWithText("Find partner matches").performScrollTo().performClick()

        assertEquals(1, backCount)
        assertEquals(1, profileCount)
        assertEquals(TopLevelDestination.EVENTS, destination)
        assertEquals(1, matchCount)
    }

    @Test
    fun `pending passport hides QR expansion and matching while retaining pending feedback`() {
        compose.setContent {
            ReEventTheme {
                PassportScreen(
                    onNavigate = {},
                    onBack = {},
                    onProfile = {},
                    onMatch = {},
                    item = resource(),
                    passportId = null,
                    qrPayload = null,
                    qrUnavailableMessage = "QR code pending until the server issues this resource passport.",
                    viewerAccess = access(),
                    recommendedAction = "Match with a reuse partner",
                    recoverySteps = recoverySteps(),
                    showMatchAction = true,
                )
            }
        }

        compose.onNodeWithText("VERIFICATION IN PROGRESS").assertIsDisplayed()
        compose.onNodeWithText("Your resource is saved").assertIsDisplayed()
        compose.onNodeWithText("QR code — unavailable").assertIsDisplayed()
        compose.onNodeWithTag("passport_qr_expand").assertDoesNotExist()
        compose.onNodeWithText("Find partner matches").assertDoesNotExist()
    }

    @Test
    fun `no selected resource keeps a safe non-actionable fallback`() {
        compose.setContent {
            ReEventTheme {
                PassportScreen(
                    onNavigate = {},
                    onBack = {},
                    onProfile = {},
                    onMatch = {},
                    item = null,
                )
            }
        }

        compose.onNodeWithText("No resource selected").assertIsDisplayed()
        compose.onNodeWithText("Resource details will appear here.").assertIsDisplayed()
        compose.onNodeWithText("Resource actions").assertDoesNotExist()
        compose.onNodeWithText("Find partner matches").assertDoesNotExist()
    }

    @Test
    fun `passport lifecycle controls show allowed actions and preserve their callbacks`() {
        var recordedAction: ResourceLifecycleAction? = null

        compose.setContent {
            ReEventTheme {
                PassportScreen(
                    onNavigate = {},
                    onBack = {},
                    onProfile = {},
                    onMatch = {},
                    item = resource(),
                    lifecycleActions = listOf(ResourceLifecycleAction.RETURN, ResourceLifecycleAction.MARK_DAMAGED),
                    onLifecycleAction = { recordedAction = it },
                    lifecycleActionLoading = true,
                    lifecycleActionNotice = "QR scan recorded",
                    lifecycleActionError = "Action needs attention",
                )
            }
        }

        compose.onNodeWithText("Resource actions").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Saving action…").assertIsDisplayed()
        compose.onNodeWithText("QR scan recorded").assertIsDisplayed()
        compose.onNodeWithText("Action needs attention").assertIsDisplayed()
        compose.onNodeWithTag("passport_lifecycle_return").performScrollTo().performClick()

        assertEquals(ResourceLifecycleAction.RETURN, recordedAction)
    }

    private fun resource() =
        ResourceCardModel(
            title = "Event signage",
            owner = "Ipoh City Hall",
            category = "Aluminium",
            price = "Value not set",
            quantity = "1 item",
            location = "Ipoh",
            impact = "Ready for reuse",
            tone = ResourceTone.Ready,
            imageRes = com.reevent.app.R.drawable.resource_display_stand,
        )

    private fun access() =
        PassportViewerAccess(
            label = "Organiser and current owner",
            explanation = "You can manage this resource. Partner matching is available while it is active.",
            canFindPartnerMatches = true,
        )

    private fun recoverySteps() =
        listOf(
            RecoveryStep(
                title = "Recorded",
                detail = "Resource recorded as active",
                status = "Active",
                tone = ResourceTone.Ready,
            )
        )
}
