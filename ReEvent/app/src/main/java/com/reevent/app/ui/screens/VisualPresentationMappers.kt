package com.reevent.app.ui.screens

import androidx.compose.foundation.layout.size
import com.reevent.app.R
import com.reevent.app.core.data.ResourcePresentationRules
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.PassportHistoryEntry
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.feature.impact.ImpactDashboardState
import com.reevent.app.ui.ImpactMetric
import com.reevent.app.ui.RecoveryStep
import com.reevent.app.ui.ResourceCardModel
import com.reevent.app.ui.ResourceTone
import com.reevent.app.ui.theme.ReEventBlue
import com.reevent.app.ui.theme.ReEventCoral
import com.reevent.app.ui.theme.ReEventGreen
import com.reevent.app.ui.theme.ReEventGreenDeep
import com.reevent.app.ui.theme.ReEventTextSecondary
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.Locale

internal fun com.reevent.app.core.model.ResourceItem.toVisualResource(
    viewer: User,
    eventName: String? = null,
    venue: String? = null,
) = ResourceCardModel(
    title = title,
    owner = ResourcePresentationRules.ownerLabel(viewer.id, viewer.role, ownerId),
    category = category.ifBlank { "Uncategorised" },
    price = valueCents.takeIf { it > 0 }?.let { "RM %.2f".format(Locale.US, it / 100.0) } ?: "Value not set",
    quantity = ResourcePresentationRules.quantityLabel(quantity, unit),
    location = venue?.takeIf(String::isNotBlank) ?: "Location to be confirmed",
    impact = "${material.ifBlank { "Material pending" }} • ${status.visualLabel()}",
    tone = status.toVisualTone(condition),
    imageRes = R.drawable.resource_display_stand,
    photoPath = imageUrls.firstOrNull(),
    id = id,
)

internal fun List<ImpactRecord>.toImpactMetrics(): List<ImpactMetric> {
    if (isEmpty()) return emptyList()
    return listOf(
        ImpactMetric(
            mapNotNull { it.materialDivertedKg }.sum().formatQuantity() + " kg",
            "Materials diverted",
            "Verified recovery records",
        ),
        ImpactMetric(mapNotNull { it.emissionsAvoidedKg }.sum().formatQuantity() + " kg", "Emissions avoided", "Estimated CO₂e avoided"),
        ImpactMetric(
            sumOf { it.recoinsTransferred + it.recoinsRewarded }.toString(),
            "ReCoins moved",
            "Transferred plus earned recognition",
        ),
    )
}

internal fun ImpactDashboardState.toImpactMetrics(): List<ImpactMetric> {
    val completedOutcomes = reusedCount + repairedCount + donatedCount + recycledCount
    if (completedOutcomes == 0 && materialDivertedKg == null && emissionsAvoidedKg == null && recoinsTransferred == null &&
        recoinsRewarded == null
    ) {
        return emptyList()
    }
    return buildList {
        add(ImpactMetric("$completedOutcomes", "Completed outcomes", "Reuse, repair, donation, and recycling"))
        materialDivertedKg?.let {
            add(ImpactMetric(it.formatQuantity() + " kg", "Materials diverted", "Mass-based documented estimate"))
        }
        emissionsAvoidedKg?.let {
            add(ImpactMetric(it.formatQuantity() + " kg", "CO₂e avoided", "MVP demonstration estimate"))
        }
        recoinsTransferred?.let {
            add(ImpactMetric(it.toString(), "ReCoins transferred", "Completed server settlements"))
        }
        recoinsRewarded?.let {
            add(ImpactMetric(it.toString(), "ReCoins rewarded", "Versioned circular recognition"))
        }
    }
}

internal fun com.reevent.app.core.model.ResourceItem.toPassportRecoveryStep() =
    RecoveryStep(
        title = "Resource recorded",
        detail = "${ResourcePresentationRules.quantityLabel(quantity, unit)} recorded as ${status.visualLabel().lowercase()}",
        status = status.visualLabel(),
        tone = status.toVisualTone(condition),
    )

private val passportHistoryJson = Json { ignoreUnknownKeys = true }

internal fun String.toPassportHistorySteps(condition: ResourceCondition): List<RecoveryStep> =
    runCatching {
        passportHistoryJson.decodeFromString(ListSerializer(PassportHistoryEntry.serializer()), this)
    }.getOrDefault(emptyList()).sortedByDescending(PassportHistoryEntry::occurredAt).map { entry ->
        val transition =
            entry.previousCondition?.let { previous ->
                entry.newCondition?.let { next -> "Condition changed from ${previous.name} to ${next.name}" }
            } ?: entry.quantity?.let { value ->
                entry.unit
                    ?.takeIf(String::isNotBlank)
                    ?.let { unit -> "${ResourcePresentationRules.quantityLabel(value, unit)} recorded" }
                    ?: "${ResourcePresentationRules.quantityNumber(value)} recorded"
            }
        RecoveryStep(
            title =
                entry.action
                    .lowercase()
                    .replace('_', ' ')
                    .replaceFirstChar(Char::titlecase),
            detail = listOfNotNull(entry.note, transition).joinToString(" • "),
            status = entry.newStatus.visualLabel(),
            tone = entry.newStatus.toVisualTone(condition),
        )
    }

internal fun com.reevent.app.core.model.ResourceItem.recommendedAction() =
    when {
        status == ResourceStatus.ARCHIVED -> "No action needed — this resource is archived"
        status == ResourceStatus.RECOVERED || status == ResourceStatus.RECOVERY_IN_PROGRESS -> "Recovery route completed"
        condition == ResourceCondition.END_OF_LIFE -> "Send to a verified recycling partner"
        condition == ResourceCondition.NEEDS_REPAIR -> "Request a repair-partner assessment"
        status == ResourceStatus.RECOVERY_IN_PROGRESS -> "Prepare the reserved handover"
        status == ResourceStatus.ACTIVE -> "Match with a reuse partner"
        else -> "Review the resource status"
    }

internal fun ResourceStatus.toVisualTone(condition: ResourceCondition) =
    when {
        this == ResourceStatus.ACTIVE -> ResourceTone.Ready
        this == ResourceStatus.RECOVERED || this == ResourceStatus.RECOVERY_IN_PROGRESS -> ResourceTone.Recycle
        condition == ResourceCondition.NEEDS_REPAIR -> ResourceTone.Repair
        else -> ResourceTone.Hot
    }

internal fun ProgrammeType.toVisualTone() =
    when (this) {
        ProgrammeType.REPAIR -> ResourceTone.Repair
        ProgrammeType.RECYCLE -> ResourceTone.Recycle
        ProgrammeType.BUY_BACK -> ResourceTone.Hot
    }

internal fun com.reevent.app.core.model.ResourceItem.availableMarketplaceTypes(): List<TransactionType> =
    marketplaceListing?.allowedActions.orEmpty()

internal fun ResourceStatus.visualLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

internal fun TransactionStatus.displayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

internal fun TransactionType.displayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

internal fun ProgrammeType.displayLabel() = name.lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

internal fun TransactionStatus.toUiColor() =
    when (this) {
        TransactionStatus.REQUESTED -> ReEventCoral
        TransactionStatus.APPROVED -> ReEventGreen
        TransactionStatus.IN_TRANSIT -> ReEventBlue
        TransactionStatus.ACTIVE -> ReEventGreen
        TransactionStatus.RETURN_IN_PROGRESS -> ReEventCoral
        TransactionStatus.COMPLETED -> ReEventGreenDeep
        TransactionStatus.REJECTED -> ReEventCoral
        TransactionStatus.CANCELLED -> ReEventTextSecondary
    }

internal fun Double.formatQuantity() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(Locale.US, this)

internal fun Long.toMoney() = "RM %.2f".format(Locale.US, this / 100.0)
