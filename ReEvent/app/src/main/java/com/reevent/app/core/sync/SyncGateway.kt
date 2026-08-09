package com.reevent.app.core.sync

import com.reevent.app.core.config.AppEnvironment
import kotlinx.serialization.json.JsonObject

interface SyncGateway {
    val environment: AppEnvironment

    fun isConfigured(): Boolean
    suspend fun authenticatedAccountIdOrNull(): String?
    suspend fun upsert(table: String, payload: JsonObject)
    suspend fun archive(table: String, recordId: String)
}
