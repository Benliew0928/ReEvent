package com.reevent.app.ui.home

import com.reevent.app.core.model.CircularProgramme
import com.reevent.app.core.model.CircularTransaction
import com.reevent.app.core.model.Event
import com.reevent.app.core.model.ImpactRecord
import com.reevent.app.core.model.ResourceItem
import com.reevent.app.core.model.ResourcePassport
import com.reevent.app.core.model.ResourceStatus
import com.reevent.app.core.model.TransactionStatus
import com.reevent.app.core.model.TransactionType
import com.reevent.app.core.model.User
import com.reevent.app.ui.TopLevelDestination
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

enum class HomeRole { ORGANIZER, PARTICIPANT, PARTNER }

enum class ParticipantActivityFilter(val label: String) {
    ALL("All activity"),
    ACTIVE("Active handovers"),
    READY_TO_RETURN("Ready to return"),
    COMPLETED("Completed"),
}

enum class HomeIcon {
    EVENT,
    TAG,
    LEAF,
    RECYCLE,
    HANDOVER,
    RETURN,
    CHECK,
    REQUEST,
    TRUCK,
    PASSPORT,
    QR,
    RESOURCE,
    PARTNERS,
    ACCOUNT,
    PROGRAMME,
    IMPACT,
    CAPACITY,
}

data class HomeScopeOption(val id: String, val label: String)

data class HomeMetric(
    val value: String,
    val label: String,
    val detail: String? = null,
    val icon: HomeIcon,
)

sealed interface HomeTarget {
    data class Destination(val destination: TopLevelDestination) : HomeTarget
    data class MatchResource(val resourceId: String) : HomeTarget
    data class FocusMarketplaceTransaction(val transactionId: String) : HomeTarget
    data class FocusProgrammeTransaction(val transactionId: String) : HomeTarget
    data class Passport(val resourceId: String) : HomeTarget
    data object ScanQr : HomeTarget
    data object CreateEvent : HomeTarget
    data object CreateProgramme : HomeTarget
    data object PartnerPassports : HomeTarget
}

data class HomePriority(
    val id: String,
    val badge: String,
    val title: String,
    val detail: String,
    val icon: HomeIcon,
    val target: HomeTarget?,
    val disabledReason: String? = null,
)

data class HomeQuickLink(
    val title: String,
    val detail: String,
    val icon: HomeIcon,
    val target: HomeTarget,
)

data class HomeEmptyState(
    val title: String,
    val detail: String,
    val actionLabel: String,
    val target: HomeTarget,
)

data class HomeDashboardUiState(
    val role: HomeRole,
    val displayName: String,
    val greeting: String,
    val greetingSubtitle: String,
    val scopeLabel: String,
    val scopes: List<HomeScopeOption> = emptyList(),
    val selectedScopeId: String? = null,
    val heroEyebrow: String? = null,
    val heroTitle: String,
    val heroBody: String,
    val progress: Float? = null,
    val progressLabel: String,
    val metrics: List<HomeMetric> = emptyList(),
    val priorityTitle: String,
    val priorities: List<HomePriority> = emptyList(),
    val stripTitle: String,
    val stripMetrics: List<HomeMetric> = emptyList(),
    val quickLinks: List<HomeQuickLink> = emptyList(),
    val emptyState: HomeEmptyState? = null,
    val isRefreshing: Boolean = false,
    val refreshError: String? = null,
)

data class HomeRefreshState(
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

object HomeDashboardMappers {
    fun selectEvent(events: List<Event>, persistedId: String?): Event? =
        events.firstOrNull { it.id == persistedId } ?: events.firstOrNull()

    fun selectProgramme(programmes: List<CircularProgramme>, persistedId: String?): CircularProgramme? =
        programmes.firstOrNull { it.id == persistedId }
            ?: programmes.firstOrNull { it.active }
            ?: programmes.firstOrNull()

    fun organizer(
        user: User,
        events: List<Event>,
        selectedEvent: Event?,
        resources: List<ResourceItem>,
        transactions: List<CircularTransaction>,
        impact: List<ImpactRecord>,
        refresh: HomeRefreshState = HomeRefreshState(),
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): HomeDashboardUiState {
        val tracked = resources.filterNot { it.archived || it.status == ResourceStatus.ARCHIVED }
        val recovered = tracked.count { it.status == ResourceStatus.RECOVERED }
        val matching = tracked.count { it.status == ResourceStatus.RECOVERY_IN_PROGRESS }
        val openTransactionResourceIds = transactions
            .filterNot { it.status in terminalStatuses }
            .map(CircularTransaction::resourceId)
            .toSet()
        val unmatched = tracked.filter { it.status == ResourceStatus.ACTIVE && it.id !in openTransactionResourceIds }
        val actionableTransactions = transactions.filter { it.status in organizerActionableStatuses }
        val priorities = buildList {
            unmatched.take(2).forEach { resource ->
                add(
                    HomePriority(
                        id = "match-${resource.id}",
                        badge = "MATCH",
                        title = "Match ${resource.title}",
                        detail = "Find the best recovery partner for ${formatQuantity(resource.quantity)} ${resource.unit}",
                        icon = HomeIcon.RESOURCE,
                        target = HomeTarget.MatchResource(resource.id),
                    ),
                )
            }
            actionableTransactions.take(maxOf(0, 3 - size)).forEach { transaction ->
                val resource = resources.firstOrNull { it.id == transaction.resourceId }
                add(
                    HomePriority(
                        id = "transaction-${transaction.id}",
                        badge = transaction.status.name.replace('_', ' '),
                        title = organizerTaskTitle(transaction.status),
                        detail = resource?.title ?: "Open the focused lifecycle request",
                        icon = transactionIcon(transaction),
                        target = HomeTarget.FocusMarketplaceTransaction(transaction.id),
                    ),
                )
            }
        }
        val progress = tracked.size.takeIf { it > 0 }?.let { recovered.toFloat() / it }
        val material = impact.mapNotNull(ImpactRecord::materialDivertedKg).takeIf(List<Double>::isNotEmpty)?.sum()
        val emissions = impact.mapNotNull(ImpactRecord::emissionsAvoidedKg).takeIf(List<Double>::isNotEmpty)?.sum()
        return HomeDashboardUiState(
            role = HomeRole.ORGANIZER,
            displayName = user.displayName,
            greeting = greeting(user.displayName, nowMillis, zoneId),
            greetingSubtitle = "Let’s close this event the right way.",
            scopeLabel = selectedEvent?.name ?: "Choose an event",
            scopes = events.map { HomeScopeOption(it.id, it.name) },
            selectedScopeId = selectedEvent?.id,
            heroEyebrow = "EVENT CLOSE-OUT",
            heroTitle = "Close the loop",
            heroBody = "Track returns, confirm handovers and recover more resources.",
            progress = progress,
            progressLabel = "recovery progress",
            metrics = listOf(
                HomeMetric(tracked.size.toString(), "Tagged", todayDetail(tracked.count { isToday(it.createdAt, nowMillis, zoneId) }), HomeIcon.TAG),
                HomeMetric(matching.toString(), "In recovery", todayDetail(transactions.count { it.status == TransactionStatus.REQUESTED && isToday(it.createdAt, nowMillis, zoneId) }), HomeIcon.LEAF),
                HomeMetric(recovered.toString(), "Recovered", todayDetail(transactions.count { it.completedAt?.let { time -> isToday(time, nowMillis, zoneId) } == true }), HomeIcon.RECYCLE),
            ),
            priorityTitle = "Priority inbox",
            priorities = priorities,
            stripTitle = "Your impact so far",
            stripMetrics = listOf(
                HomeMetric(percent(progress), "Recovery rate", icon = HomeIcon.LEAF),
                HomeMetric(material?.let { "${formatQuantity(it)} kg" } ?: "—", "Materials diverted", icon = HomeIcon.RESOURCE),
                HomeMetric(emissions?.let { formatEmissions(it) } ?: "—", "CO₂e avoided", icon = HomeIcon.IMPACT),
            ),
            quickLinks = listOf(
                HomeQuickLink("Events", "View and manage your events", HomeIcon.EVENT, HomeTarget.Destination(TopLevelDestination.EVENTS)),
                HomeQuickLink("Partners", "Find and manage partners", HomeIcon.PARTNERS, HomeTarget.Destination(TopLevelDestination.PARTNERS)),
                HomeQuickLink("Market", "List or browse resources", HomeIcon.RESOURCE, HomeTarget.Destination(TopLevelDestination.MARKETPLACE)),
            ),
            emptyState = if (selectedEvent == null) HomeEmptyState(
                "No event workspace yet",
                "Create an event to start tagging resources and tracking verified recovery.",
                "Create an event",
                HomeTarget.CreateEvent,
            ) else null,
            isRefreshing = refresh.isRefreshing,
            refreshError = refresh.error,
        )
    }

    fun participant(
        user: User,
        filter: ParticipantActivityFilter,
        transactions: List<CircularTransaction>,
        resources: List<ResourceItem>,
        passports: List<ResourcePassport>,
        refresh: HomeRefreshState = HomeRefreshState(),
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): HomeDashboardUiState {
        val eligible = transactions.filterNot { it.status in cancelledStatuses }
        val visible = eligible.filterFor(filter)
        val active = visible.count { it.status in activeStatuses }
        val ready = visible.count { it.type in returnableTypes && it.status == TransactionStatus.ACTIVE }
        val completed = visible.count { it.status == TransactionStatus.COMPLETED }
        val progress = visible.size.takeIf { it > 0 }?.let { completed.toFloat() / it }
        val urgent = eligible.firstOrNull { it.type in returnableTypes && it.status in setOf(TransactionStatus.ACTIVE, TransactionStatus.RETURN_IN_PROGRESS) }
        val urgentResource = urgent?.let { transaction -> resources.firstOrNull { it.id == transaction.resourceId } }
        val urgentPassport = urgent?.let { transaction -> passports.firstOrNull { it.resourceId == transaction.resourceId } }
        val keptInUse = resources.distinctBy(ResourceItem::id).sumOf(ResourceItem::reuseCount)
        val priorities = buildList {
            if (urgent != null) {
                add(
                    HomePriority(
                        id = "passport-${urgent.resourceId}",
                        badge = "RETURN",
                        title = "Show return passport",
                        detail = urgentResource?.let { "Present ${it.title}’s passport to complete the return." }
                            ?: "Present your resource passport to complete the return.",
                        icon = HomeIcon.PASSPORT,
                        target = urgentPassport?.let { HomeTarget.Passport(urgent.resourceId) },
                        disabledReason = if (urgentPassport == null) "Passport is not available yet" else null,
                    ),
                )
            }
            add(
                HomePriority(
                    id = "scan-qr",
                    badge = "SCAN",
                    title = "Scan resource QR",
                    detail = "Scan a QR to start a handover or register a return.",
                    icon = HomeIcon.QR,
                    target = HomeTarget.ScanQr,
                ),
            )
        }
        return HomeDashboardUiState(
            role = HomeRole.PARTICIPANT,
            displayName = user.displayName,
            greeting = greeting(user.displayName, nowMillis, zoneId),
            greetingSubtitle = "Thanks for keeping the loop going.",
            scopeLabel = filter.label,
            scopes = ParticipantActivityFilter.entries.map { HomeScopeOption(it.name, it.label) },
            selectedScopeId = filter.name,
            heroTitle = "Keep the\nloop moving.",
            heroBody = "Every handover counts.\nThank you for your part.",
            progress = progress,
            progressLabel = "your activity progress",
            metrics = listOf(
                HomeMetric(active.toString(), "Active handovers", icon = HomeIcon.HANDOVER),
                HomeMetric(ready.toString(), "Ready to return", icon = HomeIcon.RETURN),
                HomeMetric(completed.toString(), "Completed", icon = HomeIcon.CHECK),
            ),
            priorityTitle = "Your next steps",
            priorities = priorities,
            stripTitle = "Your circular activity",
            stripMetrics = listOf(
                HomeMetric(eligible.count { it.status in activeStatuses }.toString(), "Handovers", icon = HomeIcon.HANDOVER),
                HomeMetric(eligible.count { it.type in returnableTypes && it.status in setOf(TransactionStatus.RETURN_IN_PROGRESS, TransactionStatus.COMPLETED) }.toString(), "Returns", icon = HomeIcon.RETURN),
                HomeMetric(keptInUse.toString(), "Items kept in use", icon = HomeIcon.LEAF),
            ),
            quickLinks = listOf(
                HomeQuickLink("Browse resources", "Explore available circular resources", HomeIcon.RESOURCE, HomeTarget.Destination(TopLevelDestination.MARKETPLACE)),
                HomeQuickLink("Find partners", "Discover repair and recovery partners", HomeIcon.PARTNERS, HomeTarget.Destination(TopLevelDestination.PARTNERS)),
                HomeQuickLink("My account", "Manage your profile and access", HomeIcon.ACCOUNT, HomeTarget.Destination(TopLevelDestination.ACCOUNT)),
            ),
            emptyState = if (eligible.isEmpty()) HomeEmptyState(
                "Your circular activity starts here",
                "Browse resources or scan a resource QR to begin a handover.",
                "Browse resources",
                HomeTarget.Destination(TopLevelDestination.MARKETPLACE),
            ) else null,
            isRefreshing = refresh.isRefreshing,
            refreshError = refresh.error,
        )
    }

    fun partner(
        user: User,
        programmes: List<CircularProgramme>,
        selectedProgramme: CircularProgramme?,
        transactions: List<CircularTransaction>,
        resources: List<ResourceItem>,
        refresh: HomeRefreshState = HomeRefreshState(),
        nowMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): HomeDashboardUiState {
        val programmeTransactions = selectedProgramme?.let { programme ->
            transactions.filter { it.programmeId == programme.id }
        }.orEmpty()
        val eligible = programmeTransactions.filterNot { it.status in cancelledStatuses }
        val completed = eligible.count { it.status == TransactionStatus.COMPLETED }
        val progress = eligible.size.takeIf { it > 0 }?.let { completed.toFloat() / it }
        val actionable = programmeTransactions.filterNot { it.status in terminalStatuses }
        val priorities = actionable.take(3).map { transaction ->
            val resource = resources.firstOrNull { it.id == transaction.resourceId }
            HomePriority(
                id = "programme-${transaction.id}",
                badge = partnerBadge(transaction.status),
                title = partnerTaskTitle(transaction),
                detail = resource?.let { "${it.material.ifBlank { it.category }} · ${formatQuantity(transaction.quantity)} ${it.unit}" }
                    ?: "Open the focused programme task",
                icon = transactionIcon(transaction),
                target = HomeTarget.FocusProgrammeTransaction(transaction.id),
            )
        }
        return HomeDashboardUiState(
            role = HomeRole.PARTNER,
            displayName = user.displayName,
            greeting = greeting(user.displayName, nowMillis, zoneId),
            greetingSubtitle = "Here’s your partner overview.",
            scopeLabel = selectedProgramme?.name ?: "Choose a programme",
            scopes = programmes.map { HomeScopeOption(it.id, it.name) },
            selectedScopeId = selectedProgramme?.id,
            heroEyebrow = "PROGRAMME OVERVIEW",
            heroTitle = "Materials in\nmotion.",
            heroBody = "Move more materials.\nClose the loop together.",
            progress = progress,
            progressLabel = "workflow completed",
            metrics = listOf(
                HomeMetric(programmeTransactions.count { it.status == TransactionStatus.REQUESTED }.toString(), "Requested", "awaiting review", HomeIcon.REQUEST),
                HomeMetric(programmeTransactions.count { it.status == TransactionStatus.APPROVED }.toString(), "Approved", "ready for pickup", HomeIcon.CHECK),
                HomeMetric(programmeTransactions.count { it.status == TransactionStatus.IN_TRANSIT }.toString(), "In transit", "on its way", HomeIcon.TRUCK),
            ),
            priorityTitle = "Priority inbox",
            priorities = priorities,
            stripTitle = "Programme health",
            stripMetrics = listOf(
                HomeMetric(if (selectedProgramme == null) "—" else if (selectedProgramme.active) "Active" else "Inactive", selectedProgramme?.type?.name?.lowercase()?.replaceFirstChar(Char::titlecase) ?: "Programme", icon = HomeIcon.LEAF),
                HomeMetric(selectedProgramme?.remainingCapacity?.let(::formatQuantity) ?: "—", "Capacity remaining${selectedProgramme?.unit?.let { " · $it" }.orEmpty()}", icon = HomeIcon.CAPACITY),
                HomeMetric(selectedProgramme?.let { if (it.pickupAvailable) "Available" else "Unavailable" } ?: "—", "Pickup", icon = HomeIcon.TRUCK),
            ),
            quickLinks = listOf(
                HomeQuickLink("Programmes", "View and manage active programmes", HomeIcon.PROGRAMME, HomeTarget.Destination(TopLevelDestination.PROGRAMMES)),
                HomeQuickLink("Resources", "Browse circular resources", HomeIcon.RESOURCE, HomeTarget.Destination(TopLevelDestination.MARKETPLACE)),
                HomeQuickLink("Resource passports", "View authorised programme resources", HomeIcon.PASSPORT, HomeTarget.PartnerPassports),
            ),
            emptyState = if (selectedProgramme == null) HomeEmptyState(
                "No circular programme yet",
                "Create a programme so organisers can discover your services.",
                "Create a programme",
                HomeTarget.CreateProgramme,
            ) else null,
            isRefreshing = refresh.isRefreshing,
            refreshError = refresh.error,
        )
    }

    private val terminalStatuses = setOf(TransactionStatus.COMPLETED, TransactionStatus.CANCELLED, TransactionStatus.REJECTED)
    private val cancelledStatuses = setOf(TransactionStatus.CANCELLED, TransactionStatus.REJECTED)
    private val activeStatuses = setOf(TransactionStatus.APPROVED, TransactionStatus.IN_TRANSIT, TransactionStatus.ACTIVE, TransactionStatus.RETURN_IN_PROGRESS)
    private val returnableTypes = setOf(TransactionType.BORROW, TransactionType.RENT, TransactionType.REPAIR)
    private val organizerActionableStatuses = setOf(TransactionStatus.REQUESTED, TransactionStatus.APPROVED, TransactionStatus.IN_TRANSIT, TransactionStatus.ACTIVE, TransactionStatus.RETURN_IN_PROGRESS)

    private fun List<CircularTransaction>.filterFor(filter: ParticipantActivityFilter): List<CircularTransaction> = when (filter) {
        ParticipantActivityFilter.ALL -> this
        ParticipantActivityFilter.ACTIVE -> filter { it.status in activeStatuses }
        ParticipantActivityFilter.READY_TO_RETURN -> filter { it.type in returnableTypes && it.status == TransactionStatus.ACTIVE }
        ParticipantActivityFilter.COMPLETED -> filter { it.status == TransactionStatus.COMPLETED }
    }

    private fun organizerTaskTitle(status: TransactionStatus): String = when (status) {
        TransactionStatus.REQUESTED -> "Review recovery request"
        TransactionStatus.APPROVED -> "Confirm handover plan"
        TransactionStatus.IN_TRANSIT -> "Track resource handover"
        TransactionStatus.ACTIVE -> "Review active handover"
        TransactionStatus.RETURN_IN_PROGRESS -> "Confirm resource return"
        else -> "Review lifecycle item"
    }

    private fun partnerBadge(status: TransactionStatus): String = when (status) {
        TransactionStatus.REQUESTED -> "REVIEW"
        TransactionStatus.APPROVED -> "APPROVAL"
        TransactionStatus.IN_TRANSIT -> "IN TRANSIT"
        TransactionStatus.ACTIVE -> "ACTIVE"
        TransactionStatus.RETURN_IN_PROGRESS -> "RETURN"
        else -> status.name
    }

    private fun partnerTaskTitle(transaction: CircularTransaction): String = when (transaction.status) {
        TransactionStatus.REQUESTED -> "Review ${transaction.type.name.lowercase().replace('_', ' ')} request"
        TransactionStatus.APPROVED -> "Prepare approved collection"
        TransactionStatus.IN_TRANSIT -> "Track incoming material"
        TransactionStatus.ACTIVE -> "Complete active programme work"
        TransactionStatus.RETURN_IN_PROGRESS -> "Confirm programme return"
        else -> "Review programme task"
    }

    private fun transactionIcon(transaction: CircularTransaction): HomeIcon = when {
        transaction.type == TransactionType.REPAIR -> HomeIcon.LEAF
        transaction.type == TransactionType.RECYCLE -> HomeIcon.RECYCLE
        transaction.status == TransactionStatus.IN_TRANSIT -> HomeIcon.TRUCK
        else -> HomeIcon.HANDOVER
    }

    private fun greeting(displayName: String, nowMillis: Long, zoneId: ZoneId): String {
        val hour = Instant.ofEpochMilli(nowMillis).atZone(zoneId).hour
        val salutation = when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
        val firstName = displayName.trim().substringBefore(' ').ifBlank { "there" }
        return "$salutation, $firstName"
    }

    private fun isToday(value: Long, nowMillis: Long, zoneId: ZoneId): Boolean =
        Instant.ofEpochMilli(value).atZone(zoneId).toLocalDate() == Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

    private fun todayDetail(count: Int): String? = count.takeIf { it > 0 }?.let { "+$it today" }

    private fun percent(value: Float?): String = value?.let { "${(it.coerceIn(0f, 1f) * 100).roundToInt()}%" } ?: "—"

    private fun formatQuantity(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else "%.1f".format(value)

    private fun formatEmissions(kilograms: Double): String =
        if (kilograms >= 1000) "${formatQuantity(kilograms / 1000)} t" else "${formatQuantity(kilograms)} kg"
}
