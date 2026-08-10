package com.reevent.app.core.network

import com.reevent.app.core.model.MarketplaceListing
import com.reevent.app.core.model.MarketplaceListingDraft
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Invokes the server command that re-checks ownership and listing terms under a row lock. */
@Singleton
class SupabaseMarketplaceListingGateway @Inject constructor(
    private val authGateway: SupabaseAuthGateway
) {
    fun isConfigured(): Boolean = authGateway.isConfigured()

    suspend fun publish(resourceId: String, draft: MarketplaceListingDraft): MarketplaceListing =
        authGateway.withConfiguredClient { client ->
            val response = client.postgrest.rpc(
                "publish_marketplace_listing",
                buildJsonObject {
                    put("p_resource_id", resourceId)
                    put(
                        "p_allowed_actions",
                        JsonArray(draft.allowedActions.sortedBy { it.name }.map { JsonPrimitive(it.name) })
                    )
                    put("p_published_quantity", draft.publishedQuantity)
                    put("p_unit_coin_price_buy", draft.buyUnitPrice?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_unit_coin_price_rent", draft.rentUnitPrice?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_default_duration_days", draft.defaultDurationDays?.let(::JsonPrimitive) ?: JsonNull)
                    put("p_terms", draft.terms.trim())
                }
            ).decodeSingle<PublishedListingResponse>()
            MarketplaceListing(
                id = response.id,
                allowedActions = draft.allowedActions.sortedBy { it.name },
                publishedQuantity = draft.publishedQuantity,
                buyUnitPrice = draft.buyUnitPrice,
                rentUnitPrice = draft.rentUnitPrice,
                defaultDurationDays = draft.defaultDurationDays,
                terms = draft.terms.trim()
            )
        }

    @Serializable
    private data class PublishedListingResponse(val id: String)
}
