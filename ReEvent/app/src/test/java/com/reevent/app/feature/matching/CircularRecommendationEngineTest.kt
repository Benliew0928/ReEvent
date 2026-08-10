package com.reevent.app.feature.matching

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CircularRecommendationEngineTest {
    @Test
    fun good_available_acrylic_prefers_exact_reuse_programme_before_generic_reuse() {
        val result = CircularRecommendationEngine.recommend(
            resource(condition = ResourceCondition.GOOD, material = "Acrylic"),
            listOf(reuse("generic", emptyList()), reuse("exact", listOf("Acrylic")))
        )

        assertEquals(CircularAction.REUSE, result.primary?.action)
        assertEquals(listOf("exact", "generic"), result.primary?.compatibleProgrammeIds)
    }

    @Test
    fun needs_repair_prefers_repair_and_offers_recycle_as_an_alternative() {
        val result = CircularRecommendationEngine.recommend(
            resource(condition = ResourceCondition.NEEDS_REPAIR, material = "Fabric"),
            listOf(repair("repair", listOf("Fabric")), recycle("recycle", listOf("Fabric")))
        )

        assertEquals(CircularAction.REPAIR, result.primary?.action)
        assertEquals(CircularAction.RECYCLE, result.alternatives.first().action)
    }

    @Test
    fun unavailable_resource_returns_an_explanation_without_a_new_route() {
        val result = CircularRecommendationEngine.recommend(
            resource(status = ResourceStatus.RECOVERY_IN_PROGRESS),
            listOf(reuse("exact", listOf("Acrylic")))
        )

        assertNull(result.primary)
        assertEquals("This resource is not available for a new recovery route.", result.ineligibilityReason)
    }

    @Test
    fun zero_quantity_resource_returns_a_clear_ineligibility_reason() {
        val result = CircularRecommendationEngine.recommend(
            resource(quantity = 0.0),
            listOf(reuse("generic", emptyList()))
        )

        assertNull(result.primary)
        assertEquals("This resource has no available quantity for a new recovery route.", result.ineligibilityReason)
    }

    @Test
    fun exact_event_location_breaks_a_same_material_tie_deterministically() {
        val result = CircularRecommendationEngine.recommend(
            resource(material = "Acrylic"),
            listOf(
                reuse("other-location", listOf("Acrylic"), location = "Johor"),
                reuse("event-location", listOf("Acrylic"), location = "Kuala Lumpur")
            ),
            eventLocation = "Kuala Lumpur"
        )

        assertEquals(listOf("event-location", "other-location"), result.primary?.compatibleProgrammeIds)
        assertEquals(true, result.primary?.explanation?.contains("matches the event location"))
    }

    @Test
    fun missing_material_excludes_material_specific_programmes_but_keeps_generic_programmes() {
        val result = CircularRecommendationEngine.recommend(
            resource(material = ""),
            listOf(reuse("generic", emptyList()), reuse("acrylic", listOf("Acrylic")))
        )

        assertEquals(listOf("generic"), result.primary?.compatibleProgrammeIds)
    }

    @Test
    fun recycle_only_resource_without_recycling_programme_returns_no_match_reason() {
        val result = CircularRecommendationEngine.recommend(
            resource(condition = ResourceCondition.END_OF_LIFE),
            listOf(repair("repair", listOf("Acrylic")))
        )

        assertNull(result.primary)
        assertEquals("No active partner programme supports this recovery route.", result.ineligibilityReason)
    }

    private fun resource(
        condition: ResourceCondition = ResourceCondition.GOOD,
        status: ResourceStatus = ResourceStatus.ACTIVE,
        material: String = "Acrylic",
        quantity: Double = 1.0
    ) = ResourceItem(
        id = "resource-id",
        eventId = "event-id",
        ownerId = "owner-id",
        title = "Test resource",
        category = "Signage",
        material = material,
        condition = condition,
        quantity = quantity,
        unit = "ITEM",
        status = status,
        valueCents = 1_000,
        imageUrls = emptyList(),
        createdAt = NOW,
        updatedAt = NOW
    )

    private fun reuse(id: String, materials: List<String>, location: String = "Test location") = programme(id, ProgrammeType.REPAIR, materials, location)
    private fun repair(id: String, materials: List<String>) = programme(id, ProgrammeType.REPAIR, materials)
    private fun recycle(id: String, materials: List<String>) = programme(id, ProgrammeType.RECYCLE, materials)

    private fun programme(id: String, type: ProgrammeType, materials: List<String>, location: String = "Test location") = CircularProgramme(
        id = id,
        partnerId = "partner-id",
        name = id,
        type = type,
        acceptedMaterials = materials,
        location = location,
        active = true,
        createdAt = NOW,
        updatedAt = NOW
    )

    private companion object {
        const val NOW = 1L
    }
}
