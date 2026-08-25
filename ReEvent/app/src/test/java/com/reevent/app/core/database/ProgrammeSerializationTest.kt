package com.reevent.app.core.database

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CoinDirection
import com.reevent.app.core.model.GeoLocation
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgrammeSerializationTest {
    @Test
    fun `complete programme survives Room entity serialization`() {
        val programme = CircularProgramme(
            id = "programme",
            partnerId = "partner",
            name = "Repair workshop",
            type = ProgrammeType.REPAIR,
            acceptedMaterials = listOf("Wood", "Fabric"),
            location = "Petaling Jaya, Selangor",
            active = true,
            createdAt = 10,
            updatedAt = 20,
            syncState = SyncState.SYNCED,
            acceptedCategories = listOf("Furniture"),
            acceptedConditions = setOf(ResourceCondition.GOOD, ResourceCondition.NEEDS_REPAIR),
            minimumQuantity = 1.0,
            maximumQuantity = 10.0,
            unit = "ITEM",
            remainingCapacity = 8.0,
            coinDirection = CoinDirection.OWNER_PAYS_PARTNER,
            unitCoinAmount = 15,
            pickupAvailable = true,
            geoLocation = GeoLocation("Petaling Jaya, Selangor", 3.1073, 101.6067),
            processingMethod = "Inspect and repair for reuse",
            terms = "Dry items only",
        )

        val restored = programme.toEntity("account").toDomain()

        assertEquals(programme, restored)
        assertTrue(restored.isActivationReady())
    }
}
