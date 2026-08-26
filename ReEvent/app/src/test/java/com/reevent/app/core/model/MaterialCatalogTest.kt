package com.reevent.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MaterialCatalogTest {
    @Test
    fun `catalogue exposes the locked eleven families in display order`() {
        assertEquals(
            listOf(
                "Wood", "Textiles", "Metal", "Plastic", "Paper & Card", "Glass", "Ceramic",
                "Electrical & Electronics", "Organic", "Rubber", "Mixed / Other",
            ),
            MaterialFamily.entries.map(MaterialFamily::displayLabel),
        )
    }

    @Test
    fun `legacy aliases resolve to family while preserving useful subtype detail`() {
        assertEquals(MaterialDescriptor(MaterialFamily.PLASTIC, "Acrylic"), MaterialCatalog.resolveLegacy(" Acrylic "))
        assertEquals(MaterialDescriptor(MaterialFamily.TEXTILES, null), MaterialCatalog.resolveLegacy("fabric"))
        assertEquals(MaterialDescriptor(MaterialFamily.PAPER_CARD, "Cardboard"), MaterialCatalog.resolveLegacy("Cardboard"))
    }

    @Test
    fun `unknown legacy values become Mixed Other and preserve the original text`() {
        assertEquals(
            MaterialDescriptor(MaterialFamily.MIXED_OTHER, "Foam-board composite"),
            MaterialCatalog.resolveLegacy(" Foam-board composite "),
        )
    }

    @Test
    fun `detail normalization and Mixed Other validation are strict`() {
        assertFalse(MaterialCatalog.validate(MaterialFamily.MIXED_OTHER, "  ").isValid)
        assertTrue(MaterialCatalog.validate(MaterialFamily.MIXED_OTHER, "  Composite prop  ").isValid)
        assertEquals("Composite prop", MaterialCatalog.validate(MaterialFamily.MIXED_OTHER, "  Composite prop  ").normalizedDetail)
        assertFalse(MaterialCatalog.validate(MaterialFamily.WOOD, "x".repeat(121)).isValid)
        assertNull(MaterialCatalog.validate(MaterialFamily.WOOD, " ").normalizedDetail)
    }
}
