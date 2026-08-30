package com.reevent.app.core.network

import android.os.Build

/** Prevents native renderer crashes on legacy emulator graphics stacks. */
object MapRenderCompatibility {
    fun canRender(
        sdkInt: Int = Build.VERSION.SDK_INT,
        fingerprint: String = Build.FINGERPRINT,
        model: String = Build.MODEL,
        manufacturer: String = Build.MANUFACTURER,
    ): Boolean = !(sdkInt <= Build.VERSION_CODES.O_MR1 && isEmulator(fingerprint, model, manufacturer))

    internal fun isEmulator(fingerprint: String, model: String, manufacturer: String): Boolean =
        fingerprint.startsWith("generic", ignoreCase = true) ||
            fingerprint.contains("emulator", ignoreCase = true) ||
            model.contains("Emulator", ignoreCase = true) ||
            model.contains("Android SDK built for", ignoreCase = true) ||
            manufacturer.contains("Genymotion", ignoreCase = true)
}
