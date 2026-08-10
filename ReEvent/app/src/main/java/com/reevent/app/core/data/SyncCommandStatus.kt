package com.reevent.app.core.data

import com.reevent.app.core.model.SyncState

/** A visible, account-scoped view of work that has not yet reached the server. */
data class SyncCommandStatus(
    val id: String,
    val queuePosition: Int,
    val title: String,
    val detail: String,
    val syncState: SyncState,
    val attempts: Int,
    val lastError: String? = null,
    /** Present only for a queued command that applies to an already-created transaction. */
    val transactionId: String? = null,
    val lifecycleCommandType: String? = null
)
