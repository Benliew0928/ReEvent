package com.reevent.app.core.auth

import com.reevent.app.core.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class RoleAssignmentStateTest {
    @Test
    fun `each role can be assigned to an unassigned account`() {
        UserRole.entries.forEach { role ->
            assertEquals(RoleAssignmentState.UNASSIGNED, roleAssignmentStateFor(role, null))
        }
    }

    @Test
    fun `each role can safely recover its own lost response`() {
        UserRole.entries.forEach { role ->
            assertEquals(RoleAssignmentState.MATCHES_REQUEST, roleAssignmentStateFor(role, role))
        }
    }

    @Test
    fun `a retry using any different role is rejected`() {
        UserRole.entries.forEach { requestedRole ->
            UserRole.entries
                .filterNot { it == requestedRole }
                .forEach { assignedRole ->
                    assertEquals(
                        RoleAssignmentState.CONFLICTS_WITH_REQUEST,
                        roleAssignmentStateFor(requestedRole, assignedRole)
                    )
                }
        }
    }
}
