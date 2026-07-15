package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ResourceItem

/** Local, explainable MVP matching. Higher scores are more appropriate circular routes. */
object ProgrammeMatcher {
    fun rank(resource: ResourceItem, programmes: List<CircularProgramme>): List<CircularProgramme> = programmes
        .filter { it.active }
        .filter { it.acceptedMaterials.isEmpty() || it.acceptedMaterials.any { material -> material.equals(resource.material, true) } }
        .sortedWith(compareByDescending<CircularProgramme> { programme ->
            when {
                programme.acceptedMaterials.any { it.equals(resource.material, true) } -> 2
                programme.acceptedMaterials.isEmpty() -> 1
                else -> 0
            }
        }.thenBy { it.name.lowercase() })
}
