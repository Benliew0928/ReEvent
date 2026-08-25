package com.reevent.app.feature.matching

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.PartnerDiscoveryRequest
import com.reevent.app.core.model.PartnerMapFilters
import com.reevent.app.core.model.PartnerOriginSource
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerDiscoveryEngineTest {
    @Test
    fun `coordinates reject blank labels and out of range pairs`() {
        assertThrows(IllegalArgumentException::class.java) { GeoLocation("", 3.0, 101.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoLocation("bad latitude", 91.0, 101.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoLocation("bad longitude", 3.0, 181.0) }
    }

    @Test
    fun `distance filter defaults to Any and exact Haversine boundary is included`() {
        assertEquals(null, PartnerMapFilters().maximumDistanceKm)
        val origin = GeoLocation("Origin", 3.0, 101.0)
        val destination = GeoLocation("Destination", 3.1, 101.0)
        val boundary = checkNotNull(PartnerDiscoveryEngine.distanceKm(origin, destination))

        val result = discover(
            programmes = listOf(programme("boundary", location = destination)),
            request = PartnerDiscoveryRequest(
                deviceLocation = origin,
                filters = PartnerMapFilters(maximumDistanceKm = boundary),
            ),
        )

        assertEquals(listOf("boundary"), result.candidates.map { it.programme.id })
    }

    @Test
    fun `device then resource then event determines origin precedence`() {
        val event = GeoLocation("Event", 3.0, 101.0)
        val resource = resource().copy(geoLocation = GeoLocation("Resource", 3.1, 101.1))
        val device = GeoLocation("Device", 3.2, 101.2)

        assertEquals(
            PartnerOriginSource.DEVICE,
            discover(listOf(programme("one")), resource, event, PartnerDiscoveryRequest(deviceLocation = device)).originSource,
        )
        assertEquals(
            PartnerOriginSource.RESOURCE,
            discover(listOf(programme("one")), resource, event, PartnerDiscoveryRequest()).originSource,
        )
        assertEquals(
            PartnerOriginSource.EVENT,
            discover(listOf(programme("one")), resource.copy(geoLocation = null), event, PartnerDiscoveryRequest()).originSource,
        )
    }

    @Test
    fun `wildcard rules accept while unit and capacity rules exclude`() {
        val wildcard = programme("wildcard", materials = emptyList()).copy(
            acceptedCategories = emptyList(),
            acceptedConditions = ResourceCondition.entries.toSet(),
            unit = null,
            remainingCapacity = null,
        )
        val wrongUnit = programme("wrong-unit").copy(unit = "KG")
        val insufficient = programme("capacity").copy(unit = "ITEM", remainingCapacity = 0.5)

        val result = discover(listOf(wildcard, wrongUnit, insufficient))

        assertEquals(listOf("wildcard"), result.candidates.map { it.programme.id })
        assertEquals(1, result.exclusionCounts["UNIT_NOT_ACCEPTED"])
        assertEquals(1, result.exclusionCounts["CAPACITY_UNAVAILABLE"])
    }

    @Test
    fun `ranking remains stable for equal score and distance`() {
        val location = GeoLocation("Same point", 3.0, 101.0)
        val result = discover(
            listOf(
                programme("z-id", name = "Beta", location = location),
                programme("b-id", name = "Alpha", location = location),
                programme("a-id", name = "Alpha", location = location),
            ),
            request = PartnerDiscoveryRequest(deviceLocation = location),
        )

        assertEquals(listOf("a-id", "b-id", "z-id"), result.candidates.map { it.programme.id })
        assertTrue(result.candidates.all { it.reasons.isNotEmpty() })
    }

    private fun discover(
        programmes: List<CircularProgramme>,
        resource: ResourceItem? = resource(),
        event: GeoLocation? = GeoLocation("Event", 3.0, 101.0),
        request: PartnerDiscoveryRequest = PartnerDiscoveryRequest(),
    ) = PartnerDiscoveryEngine.discover(programmes, resource, event, OWNER, request)

    private fun resource() = ResourceItem(
        id = "resource",
        eventId = "event",
        ownerId = OWNER,
        title = "Chair",
        category = "Furniture",
        material = "Wood",
        condition = ResourceCondition.GOOD,
        quantity = 1.0,
        unit = "ITEM",
        status = ResourceStatus.ACTIVE,
        valueCents = 0,
        imageUrls = emptyList(),
        createdAt = 1,
        updatedAt = 1,
    )

    private fun programme(
        id: String,
        name: String = id,
        materials: List<String> = listOf("Wood"),
        location: GeoLocation = GeoLocation("Partner", 3.05, 101.05),
    ) = CircularProgramme(
        id = id,
        partnerId = "partner",
        name = name,
        type = ProgrammeType.REPAIR,
        acceptedMaterials = materials,
        location = location.displayAddress,
        active = true,
        createdAt = 1,
        updatedAt = 1,
        geoLocation = location,
    )

    private companion object {
        const val OWNER = "owner"
    }
}
