package com.reevent.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.PartnerCandidate
import com.reevent.app.core.model.PartnerDiscoveryResult
import com.reevent.app.core.model.PartnerMapFilters
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import com.reevent.app.ui.theme.ReEventTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class PartnerMapScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `fake marker and list share candidate detail selection`() {
        var selectedType: ProgrammeType? = null
        compose.setContent {
            var state by remember { mutableStateOf(state(presentation = PartnerMapPresentation.MAP)) }
            ReEventTheme {
                PartnerMapScreen(
                    user = user(),
                    state = state,
                    resource = null,
                    marketplaceResources = emptyList(),
                    onNavigate = {},
                    onProfile = {},
                    onBack = null,
                    onMaterialChange = {},
                    onToggleType = { selectedType = it },
                    onDistanceChange = {},
                    onPickupChange = {},
                    onNearMe = {},
                    onPresentationChange = { state = state.copy(presentation = it) },
                    onSelectCandidate = { state = state.copy(selectedCandidate = it) },
                    onMapLoading = {},
                    onMapLoaded = {},
                    onMapFailed = {},
                    mapContent = { current, onSelect, mapModifier ->
                        Button(
                            onClick = { onSelect(current.result.candidates.single()) },
                            modifier = mapModifier,
                        ) { Text("Fake marker") }
                    },
                    onOpenPassport = {},
                    onCreateHandover = {},
                )
            }
        }

        compose.onNodeWithText("Keep every stop moving").fetchSemanticsNode()
        compose.onNodeWithText("Pending\ncollection").fetchSemanticsNode()
        compose.onNodeWithText("More filters").performClick()
        compose.onNodeWithText("Repair").performClick()
        compose.runOnIdle { assertEquals(ProgrammeType.REPAIR, selectedType) }
        compose.onNodeWithText("Fake marker").performClick()
        compose.onNodeWithText("Accepted rules").fetchSemanticsNode()
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("Programme list").performClick()
        compose.onNodeWithContentDescription("Repair Hub, Repair, 2.5 kilometres").performScrollTo().performClick()
        compose.onNodeWithText("Accepted rules").fetchSemanticsNode()
    }

    @Test
    fun `resource context uses shared confirmation and submits once`() {
        var requests = 0
        val resource = resource()
        compose.setContent {
            var state by remember { mutableStateOf(state(resourceId = resource.id, presentation = PartnerMapPresentation.LIST)) }
            ReEventTheme {
                PartnerMapScreen(
                    user = user(), state = state, resource = resource, marketplaceResources = emptyList(),
                    onNavigate = {}, onProfile = {}, onBack = {}, onMaterialChange = {}, onToggleType = {},
                    onDistanceChange = {}, onPickupChange = {}, onNearMe = {},
                    onPresentationChange = { state = state.copy(presentation = it) },
                    onSelectCandidate = { state = state.copy(selectedCandidate = it) },
                    onMapLoading = {}, onMapLoaded = {}, onMapFailed = {}, onOpenPassport = {},
                    onCreateHandover = { requests += 1 },
                )
            }
        }

        compose.onNodeWithContentDescription("Repair Hub, Repair, 2.5 kilometres").performScrollTo().performClick()
        compose.onNodeWithText("• Accepts Wood").fetchSemanticsNode()
        compose.onNodeWithText("Request recovery").performScrollTo().performClick()
        compose.onNodeWithText("Request recovery?").fetchSemanticsNode()
        compose.onNodeWithText("Send request").performClick()
        compose.runOnIdle { assertEquals(1, requests) }
    }

    @Test
    fun `filters report pickup and material changes and list survives tile failure`() {
        var pickup = false
        compose.setContent {
            ReEventTheme {
                PartnerMapScreen(
                    user = user(),
                    state = state(presentation = PartnerMapPresentation.LIST).copy(
                        mapError = "Map tiles are unavailable. Use the programme list below.",
                        filters = PartnerMapFilters(),
                    ),
                    resource = null,
                    marketplaceResources = emptyList(),
                    onNavigate = {}, onProfile = {}, onBack = null,
                    onMaterialChange = {}, onToggleType = {}, onDistanceChange = {},
                    onPickupChange = { pickup = it }, onNearMe = {}, onPresentationChange = {},
                    onSelectCandidate = {}, onMapLoading = {}, onMapLoaded = {}, onMapFailed = {},
                    onOpenPassport = {}, onCreateHandover = {},
                )
            }
        }

        compose.onNodeWithText("More filters").performClick()
        compose.onNodeWithText("Pickup only").performClick()
        compose.onNodeWithText("Map tiles are unavailable. Use the programme list below.").fetchSemanticsNode()
        compose.onNodeWithContentDescription("Repair Hub, Repair, 2.5 kilometres").fetchSemanticsNode()
        compose.runOnIdle {
            assertTrue(pickup)
        }
    }

    @Test
    fun `participant map uses nearest partner presentation and pickup quick filter`() {
        var pickup = false
        compose.setContent {
            ReEventTheme {
                PartnerMapScreen(
                    user = user(UserRole.PARTICIPANT),
                    state = state(presentation = PartnerMapPresentation.MAP),
                    resource = null,
                    marketplaceResources = emptyList(),
                    onNavigate = {},
                    onProfile = {},
                    onBack = null,
                    onMaterialChange = {},
                    onToggleType = {},
                    onDistanceChange = {},
                    onPickupChange = { pickup = it },
                    onNearMe = {},
                    onPresentationChange = {},
                    onSelectCandidate = {},
                    onMapLoading = {},
                    onMapLoaded = {},
                    onMapFailed = {},
                    onOpenPassport = {},
                    onCreateHandover = {},
                    mapContent = { _, _, mapModifier -> Button(modifier = mapModifier, onClick = {}) { Text("Participant map") } },
                )
            }
        }

        compose.onNodeWithText("Return it, right here").fetchSemanticsNode()
        compose.onNodeWithText("Nearest partner").fetchSemanticsNode()
        compose.onNodeWithText("Pickup available").performClick()
        compose.runOnIdle { assertTrue(pickup) }
    }

    private fun state(
        resourceId: String? = null,
        presentation: PartnerMapPresentation,
    ) = PartnerMapUiState(
        resourceId = resourceId,
        result = PartnerDiscoveryResult(
            candidates = listOf(
                PartnerCandidate(
                    programme = programme(),
                    distanceKm = 2.5,
                    score = 90,
                    reasons = listOf("Accepts Wood", "Pickup available"),
                ),
            ),
        ),
        presentation = presentation,
    )

    private fun programme() = CircularProgramme(
        id = "programme",
        partnerId = "partner",
        name = "Repair Hub",
        type = ProgrammeType.REPAIR,
        acceptedMaterialFamilies = setOf(com.reevent.app.core.model.MaterialFamily.WOOD),
        location = "Kuala Lumpur",
        active = true,
        createdAt = 1,
        updatedAt = 1,
        geoLocation = GeoLocation("Kuala Lumpur", 3.139, 101.6869),
        processingMethod = "Repair",
        terms = "Appointment required",
        pickupAvailable = true,
    )

    private fun resource() = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = "owner",
        title = "Chair",
        category = "Furniture",
        materialFamily = com.reevent.app.core.model.MaterialFamily.WOOD,
        condition = ResourceCondition.GOOD,
        quantity = 1.0,
        unit = "ITEM",
        status = ResourceStatus.ACTIVE,
        valueCents = 0,
        imageUrls = emptyList(),
        createdAt = 1,
        updatedAt = 1,
    )

    private fun user(role: UserRole = UserRole.ORGANIZER) = User(
        id = "owner",
        email = "owner@example.com",
        displayName = "Owner",
        role = role,
        createdAt = 1,
        updatedAt = 1,
    )
}
