package com.reevent.app.ui.screens

import com.reevent.app.core.model.PartnerMapFilters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PartnerMapStateTest {
    @Test
    fun `permission reducer distinguishes granted and denied states`() {
        assertEquals(PartnerLocationPermission.GRANTED, reducePartnerLocationPermission(true))
        assertEquals(PartnerLocationPermission.DENIED, reducePartnerLocationPermission(false))
        assertEquals(PartnerLocationPermission.PERMANENTLY_DENIED, reducePartnerLocationPermission(false, permanent = true))
    }

    @Test
    fun `map state distinguishes browse and resource context while filters default to Any`() {
        assertFalse(PartnerMapUiState().isResourceContext)
        assertTrue(PartnerMapUiState(resourceId = "resource").isResourceContext)
        assertEquals(null, PartnerMapFilters().maximumDistanceKm)
    }
}
