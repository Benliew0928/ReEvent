package com.reevent.app.core.data

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.feature.matching.CircularRecommendationEngine

/** Local, explainable MVP matching. Higher scores are more appropriate circular routes. */
object ProgrammeMatcher {
    fun rank(resource: ResourceItem, programmes: List<CircularProgramme>): List<CircularProgramme> =
        CircularRecommendationEngine.rankProgrammes(resource, programmes)
}
