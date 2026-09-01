package com.reevent.app.feature.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class EventFormValidationTest {
    @Test
    fun `accepts a named event with a location and same-day dates`() {
        val result = EventFormValidation.validate("Campus reuse fair", "Main hall", "2026-08-10", "2026-08-10")

        assertTrue(result.isValid)
    }

    @Test
    fun `explains missing location and malformed dates`() {
        val result = EventFormValidation.validate("A", "", "10/08/2026", "")

        assertFalse(result.isValid)
        assertEquals("Enter an event name with at least 2 characters.", result.nameError)
        assertEquals("Enter the event location.", result.venueError)
        assertEquals("Use YYYY-MM-DD for the start date.", result.startDateError)
        assertEquals("Use YYYY-MM-DD for the end date.", result.endDateError)
    }

    @Test
    fun `rejects an end date before the start date`() {
        val result = EventFormValidation.validate("Campus reuse fair", "Main hall", "2026-08-11", "2026-08-10")

        assertFalse(result.isValid)
        assertNull(result.startDateError)
        assertEquals("End date cannot be before the start date.", result.endDateError)
    }

    @Test
    fun `publication only requires fields the organiser can enter`() {
        val result = EventFormValidation.validateForPublication(
            name = "Blood donation",
            description = "",
            venue = "",
            startText = "2026-08-30",
            endText = "2026-08-30",
            expectedAttendance = "0",
            hasLocation = false,
        )

        assertFalse(result.isValid)
        assertEquals("Enter a public venue.", result.venueError)
        assertEquals("Select an exact event location.", result.locationError)
        assertEquals("Expected attendance must be greater than 0.", result.expectedAttendanceError)
    }

    @Test
    fun `publication is ready without event type timezone or recovery input`() {
        val result = EventFormValidation.validateForPublication(
            name = "Blood donation",
            description = "Community blood donation event",
            venue = "Kampar Community Hall",
            startText = "2026-08-30",
            endText = "2026-08-30",
            expectedAttendance = "120",
            hasLocation = true,
        )

        assertTrue(result.isValid)
    }

    @Test
    fun `date conversion uses the selected event timezone`() {
        val date = EventFormValidation.parseDate("2026-08-30")!!

        val kualaLumpur = EventFormValidation.startOfDayMillis(date, ZoneId.of("Asia/Kuala_Lumpur"))
        val utc = EventFormValidation.startOfDayMillis(date, ZoneId.of("UTC"))

        assertEquals(8 * 60 * 60 * 1000L, utc - kualaLumpur)
    }

    @Test
    fun `date display can round-trip in the stored event timezone`() {
        val millis = EventFormValidation.startOfDayMillis(
            EventFormValidation.parseDate("2026-08-30")!!,
            ZoneId.of("Asia/Kuala_Lumpur"),
        )

        assertEquals("2026-08-30", EventFormValidation.dateText(millis, ZoneId.of("Asia/Kuala_Lumpur")))
    }
}
