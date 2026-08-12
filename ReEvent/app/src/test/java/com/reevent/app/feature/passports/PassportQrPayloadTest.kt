package com.reevent.app.feature.passports

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PassportQrPayloadTest {
    private val baseUrl = "https://verify.reevent.example"
    private val token = "AbCdEfGhIjKlMnOpQrStUv"

    @Test
    fun `creates and accepts only the configured versioned https payload`() {
        val payload = PassportQrPayload.canonicalPayload(baseUrl, token)

        assertEquals("https://verify.reevent.example/p/v1/$token", payload)
        assertEquals(PassportQrPayload.Validation.Canonical(token), PassportQrPayload.validate(checkNotNull(payload), baseUrl))
    }

    @Test
    fun `rejects a different host extra path query and invalid token`() {
        assertTrue(PassportQrPayload.validate("https://other.example/p/v1/$token", baseUrl) is PassportQrPayload.Validation.Invalid)
        assertTrue(PassportQrPayload.validate("https://verify.reevent.example/p/v1/$token/extra", baseUrl) is PassportQrPayload.Validation.Invalid)
        assertTrue(PassportQrPayload.validate("https://verify.reevent.example/p/v1/$token?resource_id=private", baseUrl) is PassportQrPayload.Validation.Invalid)
        assertNull(PassportQrPayload.canonicalPayload(baseUrl, "resource-id"))
    }

    @Test
    fun `recognises a well formed legacy code only for migration compatibility`() {
        val legacy = "reevent://passport/20000000-0000-0000-0000-000000000001"

        assertEquals(PassportQrPayload.Validation.Legacy, PassportQrPayload.validate(legacy, baseUrl))
        assertTrue(PassportQrPayload.validate("reevent://passport/not-a-uuid", baseUrl) is PassportQrPayload.Validation.Invalid)
    }

    @Test
    fun `raw server token is canonicalised before either screen may render it`() {
        assertEquals(
            PassportQrPayload.RenderResult.Ready("https://verify.reevent.example/p/v1/$token"),
            PassportQrPayload.renderablePayload(token, baseUrl)
        )
        assertTrue(PassportQrPayload.renderablePayload(token, "") is PassportQrPayload.RenderResult.Unavailable)
        assertTrue(
            PassportQrPayload.renderablePayload(
                "reevent://passport/20000000-0000-0000-0000-000000000001",
                baseUrl
            ) is PassportQrPayload.RenderResult.Unavailable
        )
    }

    @Test
    fun `canonical stored URL remains renderable only for the configured verifier`() {
        val payload = "https://verify.reevent.example/p/v1/$token"

        assertEquals(
            PassportQrPayload.RenderResult.Ready(payload),
            PassportQrPayload.renderablePayload(payload, baseUrl)
        )
        assertTrue(
            PassportQrPayload.renderablePayload(payload, "https://different.example")
                is PassportQrPayload.RenderResult.Unavailable
        )
    }
}
