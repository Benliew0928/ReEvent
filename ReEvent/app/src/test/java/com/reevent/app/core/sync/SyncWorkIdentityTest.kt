package com.reevent.app.core.sync

import androidx.work.Data
import androidx.work.workDataOf
import com.reevent.app.core.config.AppEnvironment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncWorkIdentityTest {
    @Test
    fun uniqueWorkNameSeparatesAccountsAndEnvironments() {
        val localA = SyncWorkIdentity(AppEnvironment.LOCAL, "account-a")
        val localB = SyncWorkIdentity(AppEnvironment.LOCAL, "account-b")
        val stagingA = SyncWorkIdentity(AppEnvironment.STAGING, "account-a")

        assertNotEquals(localA.uniqueWorkName, localB.uniqueWorkName)
        assertNotEquals(localA.uniqueWorkName, stagingA.uniqueWorkName)
        assertEquals("reevent-core-sync-v2:local:account-a", localA.uniqueWorkName)
    }

    @Test
    fun inputDataRoundTripPreservesImmutableIdentity() {
        val identity = SyncWorkIdentity(AppEnvironment.PRODUCTION, "account-a")

        assertEquals(identity, SyncWorkIdentity.from(identity.toInputData()))
    }

    @Test
    fun missingOrUnsupportedWorkerInputIsRejected() {
        assertNull(SyncWorkIdentity.from(Data.EMPTY))
        assertNull(
            SyncWorkIdentity.from(
                workDataOf("sync_environment" to "unknown", "sync_account_id" to "account-a")
            )
        )
        assertNull(
            SyncWorkIdentity.from(
                workDataOf("sync_environment" to "local", "sync_account_id" to "")
            )
        )
    }
}
