package com.reevent.app.feature.events

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class EventFormValidationResult(
    val nameError: String? = null,
    val venueError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null
) {
    val isValid: Boolean get() = listOf(nameError, venueError, startDateError, endDateError).all { it == null }
}

/** Keeps event editor dates explicit and timezone-safe without relying on locale-specific input. */
object EventFormValidation {
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun dateText(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate().format(dateFormat)

    fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value.trim(), dateFormat)
    } catch (_: DateTimeParseException) {
        null
    }

    fun validate(name: String, venue: String, startText: String, endText: String): EventFormValidationResult {
        val start = parseDate(startText)
        val end = parseDate(endText)
        return EventFormValidationResult(
            nameError = if (name.trim().length < 2) "Enter an event name with at least 2 characters." else null,
            venueError = if (venue.trim().length < 2) "Enter the event location." else null,
            startDateError = if (start == null) "Use YYYY-MM-DD for the start date." else null,
            endDateError = when {
                end == null -> "Use YYYY-MM-DD for the end date."
                start != null && end.isBefore(start) -> "End date cannot be before the start date."
                else -> null
            }
        )
    }

    fun startOfDayMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun endOfDayMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long =
        date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
}
