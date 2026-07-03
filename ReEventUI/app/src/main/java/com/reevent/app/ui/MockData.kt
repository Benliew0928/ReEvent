package com.reevent.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.reevent.app.R
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventWarm

enum class ReEventRole(val label: String, val description: String) {
    Organizer("Organizer", "Recover, resell and repair event resources"),
    Participant("Participant", "Return, swap and buy lower-impact items"),
    Partner("Partner", "Receive reusable, repairable or recyclable lots")
}

enum class ResourceTone(val label: String, val color: Color) {
    Ready("Ready", ReEventGreen),
    Repair("Repair", ReEventWarm),
    Recycle("Recycle", ReEventBlue),
    Hot("High demand", ReEventCoral)
}

data class ResourceItem(
    val title: String,
    val owner: String,
    val category: String,
    val price: String,
    val quantity: String,
    val location: String,
    val impact: String,
    val tone: ResourceTone,
    @DrawableRes val imageRes: Int
)

data class ImpactMetric(
    val value: String,
    val label: String,
    val detail: String
)

data class PartnerMatch(
    val name: String,
    val type: String,
    val score: String,
    val distance: String,
    val offer: String,
    val detail: String,
    val tone: ResourceTone
)

data class RecoveryStep(
    val title: String,
    val detail: String,
    val status: String,
    val tone: ResourceTone
)

object MockData {
    val resources = listOf(
        ResourceItem(
            title = "Reusable Cup Crates",
            owner = "Green Campus Run",
            category = "Food & beverage",
            price = "RM 480",
            quantity = "320 units",
            location = "Petaling Jaya",
            impact = "38 kg CO2e avoided",
            tone = ResourceTone.Hot,
            imageRes = R.drawable.resource_reusable_cup
        ),
        ResourceItem(
            title = "Fabric Wayfinding Banners",
            owner = "Merdeka Arts Night",
            category = "Event dressing",
            price = "RM 260",
            quantity = "24 panels",
            location = "Kuala Lumpur",
            impact = "12 kg textile saved",
            tone = ResourceTone.Ready,
            imageRes = R.drawable.resource_fabric_banners
        ),
        ResourceItem(
            title = "Acrylic Stage Signage",
            owner = "KL Startup Week",
            category = "Signage",
            price = "RM 190",
            quantity = "18 boards",
            location = "Bangsar South",
            impact = "Partner recycle route",
            tone = ResourceTone.Recycle,
            imageRes = R.drawable.resource_acrylic_sign
        ),
        ResourceItem(
            title = "Display Stand Kit",
            owner = "School Open Day",
            category = "Exhibition",
            price = "RM 360",
            quantity = "10 stands",
            location = "Subang Jaya",
            impact = "Repairable minor dents",
            tone = ResourceTone.Repair,
            imageRes = R.drawable.resource_display_stand
        ),
        ResourceItem(
            title = "Lanyard Badge Holders",
            owner = "GovTech Forum",
            category = "Check-in",
            price = "RM 120",
            quantity = "600 pieces",
            location = "Putrajaya",
            impact = "Direct reuse channel",
            tone = ResourceTone.Ready,
            imageRes = R.drawable.resource_badge_holders
        ),
        ResourceItem(
            title = "Canvas Gift Bags",
            owner = "Creator Market",
            category = "Merchandise",
            price = "RM 310",
            quantity = "140 bags",
            location = "Mont Kiara",
            impact = "Participant resale",
            tone = ResourceTone.Hot,
            imageRes = R.drawable.resource_gift_bags
        )
    )

    val metrics = listOf(
        ImpactMetric("83%", "Recovery rate", "Target 90% by close-out"),
        ImpactMetric("1.8t", "Materials diverted", "Across 14 active lots"),
        ImpactMetric("RM 7.4k", "Value recovered", "Resale, repair and buy-back")
    )

    val matches = listOf(
        PartnerMatch(
            name = "GreenCycle Materials",
            type = "Recycler + remanufacturer",
            score = "96%",
            distance = "4.2 km",
            offer = "RM 1.20/kg buy-back",
            detail = "Best fit for acrylic signage, badge holders and mixed plastic lots.",
            tone = ResourceTone.Recycle
        ),
        PartnerMatch(
            name = "StageKit Renew",
            type = "Repair partner",
            score = "91%",
            distance = "8.7 km",
            offer = "RM 18/unit repair",
            detail = "Repairs stands, frames, lighting brackets and booth hardware.",
            tone = ResourceTone.Repair
        ),
        PartnerMatch(
            name = "Campus Reuse Shelf",
            type = "Community reuse",
            score = "88%",
            distance = "2.1 km",
            offer = "Free collection",
            detail = "Routes clean banners, bags and decor to student events.",
            tone = ResourceTone.Ready
        )
    )

    val recoverySteps = listOf(
        RecoveryStep(
            title = "Inventory tagged",
            detail = "Each item gets condition, quantity, material and ownership data.",
            status = "Done",
            tone = ResourceTone.Ready
        ),
        RecoveryStep(
            title = "AI routing",
            detail = "Reuse, repair, resale and recycling options are ranked by value and impact.",
            status = "Live",
            tone = ResourceTone.Hot
        ),
        RecoveryStep(
            title = "Partner handover",
            detail = "Factories and repair partners receive verified lots with digital passports.",
            status = "Next",
            tone = ResourceTone.Recycle
        )
    )
}
