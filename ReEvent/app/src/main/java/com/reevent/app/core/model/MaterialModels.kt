package com.reevent.app.core.model

import java.util.Locale

enum class MaterialFamily(val displayLabel: String) {
    WOOD("Wood"),
    TEXTILES("Textiles"),
    METAL("Metal"),
    PLASTIC("Plastic"),
    PAPER_CARD("Paper & Card"),
    GLASS("Glass"),
    CERAMIC("Ceramic"),
    ELECTRICAL_ELECTRONICS("Electrical & Electronics"),
    ORGANIC("Organic"),
    RUBBER("Rubber"),
    MIXED_OTHER("Mixed / Other"),
}

data class MaterialDescriptor(
    val family: MaterialFamily,
    val detail: String? = null,
) {
    val displayLabel: String
        get() = detail?.takeIf(String::isNotBlank) ?: family.displayLabel
}

data class MaterialValidation(
    val normalizedDetail: String?,
    val detailError: String? = null,
) {
    val isValid: Boolean get() = detailError == null
}

object MaterialCatalog {
    const val MAX_DETAIL_LENGTH = 120

    private val aliases =
        mapOf(
            MaterialFamily.WOOD to setOf("wood", "wooden", "timber", "plywood", "bamboo", "cork", "mdf"),
            MaterialFamily.TEXTILES to setOf("textile", "textiles", "fabric", "canvas", "cotton", "wool", "linen", "polyester"),
            MaterialFamily.METAL to setOf("metal", "steel", "aluminium", "aluminum", "iron", "copper", "brass"),
            MaterialFamily.PLASTIC to setOf("plastic", "plastics", "acrylic", "pet", "pvc", "polypropylene", "pp", "polystyrene"),
            MaterialFamily.PAPER_CARD to setOf("paper", "card", "cardboard", "paper and card", "paper & card"),
            MaterialFamily.GLASS to setOf("glass"),
            MaterialFamily.CERAMIC to setOf("ceramic", "ceramics", "porcelain", "stoneware"),
            MaterialFamily.ELECTRICAL_ELECTRONICS to
                setOf("electrical", "electronics", "electronic", "e waste", "ewaste", "cable", "cables", "lighting"),
            MaterialFamily.ORGANIC to setOf("organic", "food", "compostable", "plant", "plants", "foliage"),
            MaterialFamily.RUBBER to setOf("rubber", "latex", "silicone"),
            MaterialFamily.MIXED_OTHER to setOf("mixed", "composite", "other", "mixed material", "mixed materials"),
        )

    private val genericAliases =
        mapOf(
            MaterialFamily.WOOD to setOf("wood", "wooden", "timber"),
            MaterialFamily.TEXTILES to setOf("textile", "textiles", "fabric"),
            MaterialFamily.METAL to setOf("metal"),
            MaterialFamily.PLASTIC to setOf("plastic", "plastics"),
            MaterialFamily.PAPER_CARD to setOf("paper", "card", "paper and card", "paper & card"),
            MaterialFamily.GLASS to setOf("glass"),
            MaterialFamily.CERAMIC to setOf("ceramic", "ceramics"),
            MaterialFamily.ELECTRICAL_ELECTRONICS to setOf("electrical", "electronics", "electrical and electronics"),
            MaterialFamily.ORGANIC to setOf("organic"),
            MaterialFamily.RUBBER to setOf("rubber"),
            MaterialFamily.MIXED_OTHER to setOf("mixed", "other", "mixed material", "mixed materials"),
        )

    fun validate(family: MaterialFamily, detail: String?): MaterialValidation {
        val normalized = detail?.trim()?.takeIf(String::isNotBlank)
        val error =
            when {
                normalized != null && normalized.length > MAX_DETAIL_LENGTH ->
                    "Material detail must be $MAX_DETAIL_LENGTH characters or fewer."

                family == MaterialFamily.MIXED_OTHER && normalized == null ->
                    "Describe the material when Mixed / Other is selected."

                else -> null
            }
        return MaterialValidation(normalized, error)
    }

    fun descriptor(family: MaterialFamily, detail: String?): MaterialDescriptor? {
        val validation = validate(family, detail)
        return if (validation.isValid) MaterialDescriptor(family, validation.normalizedDetail) else null
    }

    fun resolveLegacy(value: String): MaterialDescriptor {
        val original = value.trim().take(MAX_DETAIL_LENGTH)
        val normalized = normalize(original)
        val family =
            aliases.entries.firstOrNull { (_, values) -> normalized in values }?.key
                ?: MaterialFamily.MIXED_OTHER
        val detail =
            when {
                family == MaterialFamily.MIXED_OTHER -> original.ifBlank { "Unspecified material" }
                normalized in genericAliases.getValue(family) -> null
                else -> original.ifBlank { null }
            }
        return MaterialDescriptor(family, detail)
    }

    fun normalize(value: String): String =
        value
            .trim()
            .lowercase(Locale.ROOT)
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")
}
