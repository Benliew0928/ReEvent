package com.reevent.app.core.model

import java.time.ZoneId

const val DEFAULT_EVENT_TYPE = "COMMUNITY"
const val DEFAULT_EVENT_TIMEZONE_ID = "Asia/Kuala_Lumpur"
const val DEFAULT_EVENT_RECOVERY_TARGET_PERCENT = 0.0

/**
 * Keeps server-required legacy fields out of the organiser form while preserving the existing
 * Supabase event contract. These values do not represent extra organiser choices.
 */
fun Event.withPublicationDefaults(): Event {
    val compatibleType = eventType
        ?.trim()
        ?.uppercase()
        ?.takeIf { candidate -> EventType.entries.any { it.name == candidate } }
        ?: DEFAULT_EVENT_TYPE
    val compatibleTimezone = timezoneId
        ?.trim()
        ?.takeIf { candidate -> runCatching { ZoneId.of(candidate) }.isSuccess }
        ?: DEFAULT_EVENT_TIMEZONE_ID
    return copy(
        eventType = compatibleType,
        timezoneId = compatibleTimezone,
        recoveryTargetPercent = DEFAULT_EVENT_RECOVERY_TARGET_PERCENT,
    )
}
