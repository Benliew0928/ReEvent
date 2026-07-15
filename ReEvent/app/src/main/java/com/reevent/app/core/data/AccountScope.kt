package com.reevent.app.core.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory account boundary used to keep one user's Room cache from another user's screens. */
@Singleton
class AccountScope @Inject constructor() {
    private val mutableAccountId = MutableStateFlow<String?>(null)
    val accountId: StateFlow<String?> = mutableAccountId

    fun activate(id: String) { mutableAccountId.value = id }
    fun clear() { mutableAccountId.value = null }
    fun requireId(): String = checkNotNull(mutableAccountId.value) { "An authenticated account is required" }
}
