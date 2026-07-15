package com.reevent.app.core.navigation

import com.reevent.app.core.model.UserRole

enum class WorkspaceStart { ORGANISER, PARTICIPANT, PARTNER }

fun UserRole.workspaceStart(): WorkspaceStart = when (this) {
    UserRole.ORGANIZER -> WorkspaceStart.ORGANISER
    UserRole.PARTICIPANT -> WorkspaceStart.PARTICIPANT
    UserRole.PARTNER -> WorkspaceStart.PARTNER
}
