package com.reevent.app.core.auth

import android.util.Log
import com.reevent.app.core.config.AppEnvironment
import com.reevent.app.core.data.AccountScope
import com.reevent.app.core.data.preferences.AppPreferences
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.sync.AccountSyncScheduler
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class AccountSessionCleaner @Inject constructor(
    private val dao: CoreDao,
    private val preferences: AppPreferences,
    private val accountScope: AccountScope,
    private val syncScheduler: AccountSyncScheduler,
    private val environment: AppEnvironment
) {
    suspend fun clear(accountId: String?) {
        accountScope.clear()
        if (accountId == null) {
            preferences.clearAccount()
            return
        }

        try {
            syncScheduler.cancelSync(accountId)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Log.w(TAG, "Unable to confirm cancellation for account-scoped sync", error)
        }

        dao.deleteUser(accountId)
        dao.clearAccountEvents(accountId)
        dao.clearDiscoverableEvents(accountId)
        dao.clearAccountResources(accountId)
        dao.clearAccountPassports(accountId)
        dao.clearAccountProgrammes(accountId)
        dao.clearAccountTransactions(accountId)
        dao.clearAccountImpact(accountId)
        dao.clearAccountOutbox(environment.wireValue, accountId)
        dao.clearAccountLifecycleCommands(environment.wireValue, accountId)
        preferences.clearAccount(accountId)
    }

    private companion object {
        const val TAG = "ReEventAccountCleanup"
    }
}
