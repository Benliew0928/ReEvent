package com.reevent.app.feature.impact

import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import java.math.RoundingMode

/**
 * Creates an idempotent estimate only when the completed action has a documented factor and a
 * mass-based resource quantity. The transaction ID is deliberately also the impact-record ID.
 */
object ImpactRecordFactory {
    fun create(
        transaction: CircularTransaction,
        resource: ResourceItem,
        policy: ImpactEstimatePolicy,
        now: Long
    ): ImpactRecord? {
        if (transaction.status != TransactionStatus.COMPLETED ||
            transaction.eventId != resource.eventId ||
            transaction.resourceId != resource.id ||
            transaction.quantity !in 1..resource.quantity ||
            resource.valueCents < 0 ||
            resource.unit.normalizedMassUnit() == null
        ) return null

        val factor = policy.factorFor(resource.material, transaction.type) ?: return null
        if (factor.kgPerUnit <= 0.0 || !factor.kgPerUnit.isFinite() ||
            factor.co2eKgPerMaterialKg < 0.0 || !factor.co2eKgPerMaterialKg.isFinite()
        ) return null

        val materialKg = transaction.quantity * factor.kgPerUnit
        if (!materialKg.isFinite() || materialKg < 0.0) return null

        val valueRecoveredCents = runCatching {
            resource.valueCents.toBigDecimal()
                .multiply(transaction.quantity.toBigDecimal())
                .divide(resource.quantity.toBigDecimal(), 0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull() ?: return null

        return ImpactRecord(
            id = transaction.id,
            eventId = transaction.eventId,
            resourceId = resource.id,
            transactionId = transaction.id,
            materialDivertedKg = materialKg,
            emissionsAvoidedKg = materialKg * factor.co2eKgPerMaterialKg,
            valueRecoveredCents = valueRecoveredCents,
            calculatedAt = now,
            updatedAt = now
        )
    }

    private fun String.normalizedMassUnit(): String? = trim().lowercase().takeIf {
        it == "kg" || it == "kilogram" || it == "kilograms"
    }
}

data class ImpactEstimateFactor(
    val kgPerUnit: Double,
    val co2eKgPerMaterialKg: Double,
    val source: String,
    val versionOrAccessDate: String
)

data class ImpactEstimateKey(
    val material: String,
    val transactionType: TransactionType
)

data class ImpactEstimatePolicy(
    val factors: Map<ImpactEstimateKey, ImpactEstimateFactor>
) {
    fun factorFor(material: String, transactionType: TransactionType): ImpactEstimateFactor? =
        factors[ImpactEstimateKey(material.trim().lowercase(), transactionType)]
}

/**
 * A deliberately narrow demonstration policy. It is not a lifecycle assessment and must not be
 * extended without adding the source, scope, and conversion calculation to the impact document.
 */
object ImpactEstimatePolicies {
    private val uk2025AveragePlasticRecycle = ImpactEstimateFactor(
        kgPerUnit = 1.0,
        co2eKgPerMaterialKg = 1.59710826,
        source = "UK Government 2025 GHG conversion factors, plastics average primary minus closed-loop source",
        versionOrAccessDate = "2025-06-10"
    )

    val documentedDemo = ImpactEstimatePolicy(
        mapOf(
            ImpactEstimateKey("plastic", TransactionType.RECYCLE) to uk2025AveragePlasticRecycle,
            // Acrylic is treated as a broad plastic category for this disclosed MVP demonstration.
            ImpactEstimateKey("acrylic", TransactionType.RECYCLE) to uk2025AveragePlasticRecycle
        )
    )
}
