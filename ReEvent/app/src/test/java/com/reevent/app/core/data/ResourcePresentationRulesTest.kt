package com.reevent.app.core.data

import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class ResourcePresentationRulesTest {
    @Test
    fun `count units use whole-looking values and correct singular wording`() {
        assertEquals("1 item", ResourcePresentationRules.quantityLabel(1.0, "ITEM"))
        assertEquals("11 items", ResourcePresentationRules.quantityLabel(11.0, "items"))
        assertEquals("1 box", ResourcePresentationRules.quantityLabel(1.0, "boxes"))
        assertEquals("11 boxes", ResourcePresentationRules.quantityLabel(11.0, "BOX"))
    }

    @Test
    fun `kg removes padded zeroes while retaining meaningful decimal places`() {
        assertEquals("2 kg", ResourcePresentationRules.quantityLabel(2.0, "kg"))
        assertEquals("2.5 kg", ResourcePresentationRules.quantityLabel(2.5, "KG"))
        assertEquals("0.125 kg", ResourcePresentationRules.quantityLabel(0.125, "kg"))
        assertEquals("0.1259 kg", ResourcePresentationRules.quantityLabel(0.1259, "kg"))
    }

    @Test
    fun `owner labels are viewer relative and never reveal another account identifier`() {
        assertEquals(
            "Your organisation",
            ResourcePresentationRules.ownerLabel("owner-id", UserRole.ORGANIZER, "owner-id")
        )
        assertEquals("You", ResourcePresentationRules.ownerLabel("owner-id", UserRole.PARTICIPANT, "owner-id"))
        assertEquals(
            "Owner identity protected",
            ResourcePresentationRules.ownerLabel("viewer-id", UserRole.PARTNER, "owner-id")
        )
    }
}
