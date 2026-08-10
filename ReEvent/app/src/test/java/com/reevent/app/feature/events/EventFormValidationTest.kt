package com.reevent.app.feature.events

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
