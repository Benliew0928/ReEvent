package com.reevent.app.feature.matching

import com.reevent.app.core.model.ResourceCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PrototypeAssessmentTest {
    @Test
    fun confirmed_fields_generate_an_explainable_prototype_insight() {
        val result = PrototypeAssessment.assess("Signage", "Acrylic", ResourceCondition.GOOD)

        assertEquals("Prototype estimate from confirmed resource details", result?.disclosure)
        assertEquals(CircularAction.REUSE, result?.suggestedAction)
    }

    @Test
    fun missing_material_returns_no_confident_assessment() {
        assertNull(PrototypeAssessment.assess("Signage", "", ResourceCondition.GOOD))
    }
}
