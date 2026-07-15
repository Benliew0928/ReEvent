package com.reevent.app.core.navigation

import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class RoleRoutingTest {
    @Test fun every_role_has_only_its_intended_workspace_start() {
        assertEquals(WorkspaceStart.ORGANISER, UserRole.ORGANIZER.workspaceStart())
        assertEquals(WorkspaceStart.PARTICIPANT, UserRole.PARTICIPANT.workspaceStart())
        assertEquals(WorkspaceStart.PARTNER, UserRole.PARTNER.workspaceStart())
    }
}
