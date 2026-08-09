package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgrammeMatcherTest {
    @Test fun exact_material_match_precedes_generic_programme_and_inactive_is_excluded() {
        val now = 1L
        val resource = ResourceItem("r", "e", "o", "Signs", "", "Acrylic", ResourceCondition.GOOD, 1.0, "ITEM", ResourceStatus.ACTIVE, 0, emptyList(), now, now)
        val generic = CircularProgramme("g", "p", "Generic", ProgrammeType.REPAIR, emptyList(), "", true, now, now)
        val exact = CircularProgramme("e", "p", "Acrylic reuse", ProgrammeType.REPAIR, listOf("acrylic"), "", true, now, now)
        val inactive = CircularProgramme("i", "p", "Closed", ProgrammeType.REPAIR, listOf("Acrylic"), "", false, now, now)
        assertEquals(listOf("e", "g"), ProgrammeMatcher.rank(resource, listOf(generic, inactive, exact)).map { it.id })
    }
}
