package com.reevent.app.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ResourcePhotoSnapshotRulesTest {
    @Test
    fun `newest metadata row is the only displayed photo while older rows await cleanup`() {
        val result = primaryResourcePhotoPaths(
            listOf(
                SupabaseCoreGateway.ResourcePhotoRow("resource-a", "owner/resources/resource-a/old.jpg", 0),
                SupabaseCoreGateway.ResourcePhotoRow("resource-a", "owner/resources/resource-a/new.jpg", 1),
                SupabaseCoreGateway.ResourcePhotoRow("resource-b", "owner/resources/resource-b/only.webp", 0)
            )
        )

        assertEquals(listOf("owner/resources/resource-a/new.jpg"), result["resource-a"])
        assertEquals(listOf("owner/resources/resource-b/only.webp"), result["resource-b"])
    }

    @Test
    fun `blank malformed metadata is ignored`() {
        assertEquals(
            emptyMap<String, List<String>>(),
            primaryResourcePhotoPaths(listOf(SupabaseCoreGateway.ResourcePhotoRow("resource-a", "", 0)))
        )
    }
}
