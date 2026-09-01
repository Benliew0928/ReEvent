package com.reevent.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EventPublicationDefaultsTest {
    @Test
    fun `hidden publication fields receive existing Supabase compatible defaults`() {
        val event = event(eventType = null, timezoneId = null, recoveryTargetPercent = 80.0)

        val normalized = event.withPublicationDefaults()

        assertEquals("COMMUNITY", normalized.eventType)
        assertEquals("Asia/Kuala_Lumpur", normalized.timezoneId)
        assertEquals(0.0, normalized.recoveryTargetPercent, 0.0)
    }

    @Test
    fun `existing event type and timezone are preserved`() {
        val event = event(eventType = "WORKSHOP", timezoneId = "Asia/Singapore", recoveryTargetPercent = 0.0)

        val normalized = event.withPublicationDefaults()

        assertEquals("WORKSHOP", normalized.eventType)
        assertEquals("Asia/Singapore", normalized.timezoneId)
    }

    private fun event(
        eventType: String?,
        timezoneId: String?,
        recoveryTargetPercent: Double,
    ) = Event(
        id = "event",
        ownerId = "organizer",
        name = "Blood donation",
        description = "Community blood donation event",
        venue = "Kampar Community Hall",
        startsAt = 1_793_001_600_000L,
        endsAt = 1_793_088_000_000L,
        status = "DRAFT",
        createdAt = 1L,
        updatedAt = 1L,
        eventType = eventType,
        timezoneId = timezoneId,
        expectedAttendance = 120,
        recoveryTargetPercent = recoveryTargetPercent,
    )
}
