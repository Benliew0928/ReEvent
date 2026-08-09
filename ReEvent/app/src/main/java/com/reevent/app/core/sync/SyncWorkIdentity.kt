package com.reevent.app.core.sync

import androidx.work.Data
import androidx.work.workDataOf
import com.reevent.app.core.config.AppEnvironment

data class SyncWorkIdentity(
    val environment: AppEnvironment,
    val accountId: String
) {
    init { require(accountId.isNotBlank()) { "Sync work requires an accountId" } }

    val uniqueWorkName: String
        get() = "$WORK_NAME_PREFIX:${environment.wireValue}:$accountId"

    fun toInputData(): Data = workDataOf(
        INPUT_ENVIRONMENT to environment.wireValue,
        INPUT_ACCOUNT_ID to accountId
    )

    companion object {
        const val LEGACY_WORK_NAME = "reevent-core-sync"
        private const val WORK_NAME_PREFIX = "reevent-core-sync-v2"
        private const val INPUT_ENVIRONMENT = "sync_environment"
        private const val INPUT_ACCOUNT_ID = "sync_account_id"

        fun from(inputData: Data): SyncWorkIdentity? {
            val environment = inputData.getString(INPUT_ENVIRONMENT)?.let { value ->
                runCatching { AppEnvironment.from(value) }.getOrNull()
            } ?: return null
            val accountId = inputData.getString(INPUT_ACCOUNT_ID)?.takeIf(String::isNotBlank) ?: return null
            return SyncWorkIdentity(environment, accountId)
        }
    }
}
