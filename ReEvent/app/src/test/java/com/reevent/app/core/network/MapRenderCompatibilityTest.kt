package com.reevent.app.core.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MapRenderCompatibilityTest {
    @Test
    fun `legacy Android emulator uses non-map fallback`() {
        assertFalse(
            MapRenderCompatibility.canRender(
                sdkInt = 26,
                fingerprint = "generic/sdk/generic",
                model = "Android SDK built for x86_64",
                manufacturer = "Google",
            )
        )
    }

    @Test
    fun `modern emulator may render with compatible graphics`() {
        assertTrue(
            MapRenderCompatibility.canRender(
                sdkInt = 35,
                fingerprint = "generic/sdk/generic",
                model = "Android SDK built for x86_64",
                manufacturer = "Google",
            )
        )
    }

    @Test
    fun `physical Android 8 device remains supported`() {
        assertTrue(
            MapRenderCompatibility.canRender(
                sdkInt = 26,
                fingerprint = "vendor/device/release-keys",
                model = "Physical Phone",
                manufacturer = "Vendor",
            )
        )
    }
}
