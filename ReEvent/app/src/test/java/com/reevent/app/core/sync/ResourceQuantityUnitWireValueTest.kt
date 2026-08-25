package com.reevent.app.core.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class ResourceQuantityUnitWireValueTest {
    @Test
    fun `form unit labels map to server quantity units`() {
        mapOf(
            "items" to "ITEM",
            "sets" to "SET",
            "kg" to "KG",
            "boxes" to "BOX",
            "metres" to "METRE",
        ).forEach { (label, wireValue) ->
            assertEquals(wireValue, resourceQuantityUnitWireValue(label))
        }
    }

    @Test
    fun `canonical and singular values remain stable`() {
        mapOf(
            "ITEM" to "ITEM",
            "set" to "SET",
            "BOX" to "BOX",
            "kilograms" to "KG",
            "meter" to "METRE",
        ).forEach { (value, wireValue) ->
            assertEquals(wireValue, resourceQuantityUnitWireValue(value))
        }
    }
}
