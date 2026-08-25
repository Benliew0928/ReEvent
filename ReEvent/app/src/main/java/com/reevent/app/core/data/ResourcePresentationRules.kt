package com.reevent.app.core.data

import com.reevent.app.core.model.UserRole
import java.math.BigDecimal

/**
 * Deterministic, display-only labels for resource information.
 *
 * These helpers must not be used to transform values before validation, persistence, or server
 * commands. A quantity remains a [Double] in the domain model; this class only removes visual
 * noise such as `11.0 items` from read-only UI.
 */
object ResourcePresentationRules {
    fun quantityLabel(quantity: Double, unit: String): String =
        "${quantityNumber(quantity)} ${displayUnit(quantity, unit)}".trim()

    fun quantityNumber(quantity: Double): String = when {
        !quantity.isFinite() -> "—"
        quantity == 0.0 -> "0"
        else -> BigDecimal.valueOf(quantity).stripTrailingZeros().toPlainString()
    }

    fun ownerLabel(viewerId: String, viewerRole: UserRole?, resourceOwnerId: String): String = when {
        viewerId.isNotBlank() && viewerId == resourceOwnerId && viewerRole == UserRole.ORGANIZER -> "Your organisation"
        viewerId.isNotBlank() && viewerId == resourceOwnerId -> "You"
        else -> "Owner identity protected"
    }

    private fun displayUnit(quantity: Double, rawUnit: String): String {
        val unit = rawUnit.trim().lowercase()
        val singular = quantity == 1.0
        return when (unit) {
            "item", "items" -> if (singular) "item" else "items"
            "box", "boxes" -> if (singular) "box" else "boxes"
            "set", "sets" -> if (singular) "set" else "sets"
            "kg" -> "kg"
            "metre", "metres", "meter", "meters" -> if (singular) "metre" else "metres"
            else -> unit
        }
    }
}
