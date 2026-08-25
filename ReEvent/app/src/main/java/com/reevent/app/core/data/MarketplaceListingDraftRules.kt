package com.reevent.app.core.data

import com.reevent.app.core.model.MarketplaceListingDraft
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionType

/** Pure client-side checks. The publish RPC repeats every business-critical condition. */
data class MarketplaceListingDraftValidation(
    val resourceError: String? = null,
    val actionError: String? = null,
    val quantityError: String? = null,
    val buyPriceError: String? = null,
    val rentPriceError: String? = null,
    val durationError: String? = null,
    val termsError: String? = null
) {
    val isValid: Boolean
        get() = listOf(
            resourceError,
            actionError,
            quantityError,
            buyPriceError,
            rentPriceError,
            durationError,
            termsError
        ).all { it == null }
}

object MarketplaceListingDraftRules {
    const val MAX_TERMS_LENGTH = 2_000
    const val MIN_DURATION_DAYS = 1
    const val MAX_DURATION_DAYS = 365

    val publishableActions: Set<TransactionType> = setOf(
        TransactionType.BORROW,
        TransactionType.RENT,
        TransactionType.BUY,
        TransactionType.DONATE,
        TransactionType.EXCHANGE
    )

    fun validate(resource: ResourceItem, draft: MarketplaceListingDraft): MarketplaceListingDraftValidation {
        val borrowsOrRents = draft.allowedActions.any { it in setOf(TransactionType.BORROW, TransactionType.RENT) }
        val allowsBuy = TransactionType.BUY in draft.allowedActions
        val allowsRent = TransactionType.RENT in draft.allowedActions
        val needsWholeQuantity = !resource.unit.equals("KG", ignoreCase = true)
        return MarketplaceListingDraftValidation(
            resourceError = when {
                resource.archived || resource.status != ResourceStatus.ACTIVE -> "Only an active resource can be published."
                resource.syncState != SyncState.SYNCED -> "Wait for this resource to finish syncing before publishing it."
                resource.marketplaceListing != null -> "This resource already has an open marketplace listing."
                else -> null
            },
            actionError = when {
                draft.allowedActions.isEmpty() -> "Select at least one available action."
                !publishableActions.containsAll(draft.allowedActions) -> "Only Borrow, Rent, Buy, Donate, and Exchange can be published."
                else -> null
            },
            quantityError = when {
                !draft.publishedQuantity.isFinite() || draft.publishedQuantity <= 0 -> "Enter a quantity above 0."
                draft.publishedQuantity > resource.quantity -> "Published quantity cannot exceed the available resource quantity."
                needsWholeQuantity && draft.publishedQuantity % 1.0 != 0.0 -> "${resource.unit} resources must be published as whole quantities."
                else -> null
            },
            buyPriceError = when {
                allowsBuy && (draft.buyUnitPrice == null || draft.buyUnitPrice <= 0) -> "Enter a Buy price in ReCoins."
                !allowsBuy && draft.buyUnitPrice != null -> "A Buy price is only allowed when Buy is selected."
                else -> null
            },
            rentPriceError = when {
                allowsRent && (draft.rentUnitPrice == null || draft.rentUnitPrice <= 0) -> "Enter a Rent price in ReCoins."
                !allowsRent && draft.rentUnitPrice != null -> "A Rent price is only allowed when Rent is selected."
                else -> null
            },
            durationError = when {
                borrowsOrRents && draft.defaultDurationDays == null -> "Enter a default Borrow/Rent duration."
                borrowsOrRents && draft.defaultDurationDays !in MIN_DURATION_DAYS..MAX_DURATION_DAYS -> "Duration must be between $MIN_DURATION_DAYS and $MAX_DURATION_DAYS days."
                !borrowsOrRents && draft.defaultDurationDays != null -> "A duration is only used for Borrow or Rent."
                else -> null
            },
            termsError = if (draft.terms.length > MAX_TERMS_LENGTH) {
                "Terms must be $MAX_TERMS_LENGTH characters or fewer."
            } else {
                null
            }
        )
    }
}
