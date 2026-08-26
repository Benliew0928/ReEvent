package com.reevent.app.ui.home

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ProgrammeType
import com.reevent.app.core.model.ResourceCondition
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.SyncState
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.core.model.UserRole
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeDashboardMappersTest {
    private val zone = ZoneId.of("Asia/Kuala_Lumpur")
    private val now = Instant.parse("2026-08-25T04:00:00Z").toEpochMilli()

    @Test
    fun `empty organizer uses unavailable progress and honest empty state`() {
        val state = HomeDashboardMappers.organizer(
            user(UserRole.ORGANIZER),
            emptyList(),
            null,
            emptyList(),
            emptyList(),
            emptyList(),
            nowMillis = now,
            zoneId = zone,
        )

        assertNull(state.progress)
        assertEquals("—", state.stripMetrics.first().value)
        assertEquals("Create an event", state.emptyState?.actionLabel)
        assertTrue(state.priorities.isEmpty())
    }

    @Test
    fun `participant and partner empty states never invent activity or capacity`() {
        val participant = HomeDashboardMappers.participant(
            user(UserRole.PARTICIPANT),
            ParticipantActivityFilter.ALL,
            emptyList(),
            emptyList(),
            emptyList(),
            nowMillis = now,
            zoneId = zone,
        )
        val partner = HomeDashboardMappers.partner(
            user(UserRole.PARTNER),
            emptyList(),
            null,
            emptyList(),
            emptyList(),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals("Your circular activity starts here", participant.emptyState?.title)
        assertNull(participant.progress)
        assertEquals("No circular programme yet", partner.emptyState?.title)
        assertEquals("—", partner.stripMetrics[1].value)
        assertNull(partner.progress)
    }

    @Test
    fun `event selection falls back deterministically when persisted scope disappeared`() {
        val events = listOf(event("first"), event("second"))

        assertEquals("second", HomeDashboardMappers.selectEvent(events, "second")?.id)
        assertEquals("first", HomeDashboardMappers.selectEvent(events, "missing")?.id)
    }

    @Test
    fun `organizer today subtitles and impact use authoritative fields only`() {
        val selected = event("event")
        val resources = listOf(
            resource("active", ResourceStatus.ACTIVE, createdAt = now - 1_000),
            resource("recovered", ResourceStatus.RECOVERED, createdAt = now - 90_000_000),
        )
        val transactions = listOf(
            transaction("request", TransactionStatus.REQUESTED, resourceId = "active", createdAt = now - 2_000),
            transaction("complete", TransactionStatus.COMPLETED, resourceId = "recovered", completedAt = now - 3_000),
        )
        val impact = listOf(impact(materialKg = 214.0, emissionsKg = null))

        val state = HomeDashboardMappers.organizer(
            user(UserRole.ORGANIZER),
            listOf(selected),
            selected,
            resources,
            transactions,
            impact,
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(0.5f, state.progress)
        assertEquals("+1 today", state.metrics[0].detail)
        assertEquals("+1 today", state.metrics[1].detail)
        assertEquals("+1 today", state.metrics[2].detail)
        assertEquals("214 kg", state.stripMetrics[1].value)
        assertEquals("—", state.stripMetrics[2].value)
    }

    @Test
    fun `participant filters exclude rejected and cancelled while urgent return remains unfiltered`() {
        val activity = listOf(
            transaction("active", TransactionStatus.ACTIVE, type = TransactionType.BORROW, resourceId = "r1"),
            transaction("done", TransactionStatus.COMPLETED, type = TransactionType.REPAIR, resourceId = "r2"),
            transaction("cancelled", TransactionStatus.CANCELLED, resourceId = "r3"),
            transaction("rejected", TransactionStatus.REJECTED, resourceId = "r4"),
        )
        val state = HomeDashboardMappers.participant(
            user(UserRole.PARTICIPANT),
            ParticipantActivityFilter.COMPLETED,
            activity,
            listOf(resource("r1", reuseCount = 3), resource("r2", reuseCount = 2)),
            emptyList(),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(1f, state.progress)
        assertEquals("1", state.metrics[2].value)
        assertEquals("5", state.stripMetrics[2].value)
        assertEquals("Show return passport", state.priorities.first().title)
        assertNull(state.priorities.first().target)
        assertEquals("Passport is not available yet", state.priorities.first().disabledReason)
    }

    @Test
    fun `participant passport target becomes actionable when authorised passport exists`() {
        val transaction = transaction("active", TransactionStatus.ACTIVE, type = TransactionType.RENT, resourceId = "r1")
        val passport = ResourcePassport("p1", "r1", "token", "[]", now, now, SyncState.SYNCED)

        val state = HomeDashboardMappers.participant(
            user(UserRole.PARTICIPANT),
            ParticipantActivityFilter.ALL,
            listOf(transaction),
            listOf(resource("r1")),
            listOf(passport),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(HomeTarget.Passport("r1"), state.priorities.first().target)
    }

    @Test
    fun `partner dashboard scopes transactions by programme and reports remaining capacity`() {
        val first = programme("p1", active = true, remaining = 32.0)
        val second = programme("p2", active = false, remaining = null)
        val activity = listOf(
            transaction("requested", TransactionStatus.REQUESTED, programmeId = "p1", resourceId = "r1"),
            transaction("completed", TransactionStatus.COMPLETED, programmeId = "p1", resourceId = "r2"),
            transaction("other", TransactionStatus.IN_TRANSIT, programmeId = "p2", resourceId = "r3"),
            transaction("cancelled", TransactionStatus.CANCELLED, programmeId = "p1", resourceId = "r4"),
        )

        val state = HomeDashboardMappers.partner(
            user(UserRole.PARTNER),
            listOf(first, second),
            first,
            activity,
            listOf(resource("r1"), resource("r2")),
            nowMillis = now,
            zoneId = zone,
        )

        assertEquals(0.5f, state.progress)
        assertEquals("1", state.metrics[0].value)
        assertEquals("0", state.metrics[2].value)
        assertEquals("32", state.stripMetrics[1].value)
        assertEquals("workflow completed", state.progressLabel)
        assertEquals(listOf("programme-requested"), state.priorities.map(HomePriority::id))
        assertEquals("p1", HomeDashboardMappers.selectProgramme(listOf(first, second), "missing")?.id)
    }

    private fun user(role: UserRole) = User(
        id = "user",
        email = "alex@example.com",
        displayName = "Alex Rivera",
        role = role,
        createdAt = now,
        updatedAt = now,
    )

    private fun event(id: String) = Event(
        id = id,
        ownerId = "user",
        name = "Event $id",
        description = "",
        venue = "",
        startsAt = now,
        endsAt = now + 1_000,
        status = "ACTIVE",
        createdAt = now,
        updatedAt = now,
        syncState = SyncState.SYNCED,
    )

    private fun resource(
        id: String,
        status: ResourceStatus = ResourceStatus.ACTIVE,
        createdAt: Long = now,
        reuseCount: Int = 0,
    ) = ResourceItem(
        id = id,
        eventId = "event",
        ownerId = "user",
        title = "Chair $id",
        category = "Furniture",
        material = "Wood",
        condition = ResourceCondition.GOOD,
        quantity = 1.0,
        unit = "unit",
        status = status,
        valueCents = 0,
        imageUrls = emptyList(),
        createdAt = createdAt,
        updatedAt = createdAt,
        syncState = SyncState.SYNCED,
        reuseCount = reuseCount,
    )

    private fun transaction(
        id: String,
        status: TransactionStatus,
        type: TransactionType = TransactionType.RECYCLE,
        resourceId: String = "resource",
        programmeId: String? = null,
        createdAt: Long = now,
        completedAt: Long? = null,
    ) = CircularTransaction(
        id = id,
        eventId = "event",
        resourceId = resourceId,
        senderId = "sender",
        receiverId = "receiver",
        partnerId = "user",
        type = type,
        status = status,
        quantity = 1.0,
        createdAt = createdAt,
        updatedAt = createdAt,
        syncState = SyncState.SYNCED,
        requesterId = "sender",
        programmeId = programmeId,
        completedAt = completedAt,
    )

    private fun impact(materialKg: Double?, emissionsKg: Double?) = ImpactRecord(
        id = "impact",
        eventId = "event",
        resourceId = "resource",
        transactionId = "transaction",
        transactionType = TransactionType.RECYCLE,
        completedQuantity = 1.0,
        unit = "kg",
        materialDivertedKg = materialKg,
        emissionsAvoidedKg = emissionsKg,
        recoinsTransferred = 0,
        recoinsRewarded = 0,
        calculatedAt = now,
        syncState = SyncState.SYNCED,
    )

    private fun programme(id: String, active: Boolean, remaining: Double?) = CircularProgramme(
        id = id,
        partnerId = "user",
        name = "Programme $id",
        type = ProgrammeType.REPAIR,
        acceptedMaterials = listOf("Wood"),
        location = "Kuala Lumpur",
        active = active,
        createdAt = now,
        updatedAt = now,
        syncState = SyncState.SYNCED,
        unit = "kg",
        remainingCapacity = remaining,
        pickupAvailable = true,
    )
}
