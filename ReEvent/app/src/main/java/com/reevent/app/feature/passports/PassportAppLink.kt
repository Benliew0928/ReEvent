package com.reevent.app.feature.passports

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds one validated HTTPS passport link while Android brings the signed-in UI to the foreground. */
object PassportAppLink {
    private val mutablePendingPayload = MutableStateFlow<String?>(null)
    val pendingPayload: StateFlow<String?> = mutablePendingPayload.asStateFlow()

    fun submit(payload: String) {
        mutablePendingPayload.value = payload
    }

    fun consume(payload: String) {
        if (mutablePendingPayload.value == payload) mutablePendingPayload.value = null
    }
}
