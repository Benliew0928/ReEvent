package com.reevent.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.reevent.app.ui.theme.ReEventAmber
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen

enum class ResourceTone(
    val label: String,
    val color: Color,
) {
    Ready("Ready", ReEventGreen),
    Repair("Repair", ReEventAmber),
    Recycle("Recycle", ReEventBlue),
    Hot("High demand", ReEventCoral),
}

data class ResourceCardModel(
    val title: String,
    val owner: String,
    val category: String,
    val price: String,
    val quantity: String,
    val location: String,
    val impact: String,
    val tone: ResourceTone,
    @DrawableRes val imageRes: Int,
    val photoPath: String? = null,
    val id: String? = null,
)

data class ImpactMetric(
    val value: String,
    val label: String,
    val detail: String,
)

data class RecoveryStep(
    val title: String,
    val detail: String,
    val status: String,
    val tone: ResourceTone,
)
