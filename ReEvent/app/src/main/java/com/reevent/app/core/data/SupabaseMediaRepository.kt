package com.reevent.app.core.data

import android.content.Context
import android.net.Uri
import com.reevent.app.core.network.SupabaseAuthGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.storage.storage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseMediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountScope: AccountScope,
    private val gateway: SupabaseAuthGateway
) : MediaRepository {
    override suspend fun uploadResourcePhoto(resourceId: String, uri: Uri): AppResult<String> = try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return AppResult.Failure(FailureReason.VALIDATION)
        if (bytes.size > MAX_UPLOAD_BYTES) return AppResult.Failure(FailureReason.VALIDATION)
        val accountId = accountScope.requireId()
        val path = "$accountId/resources/$resourceId/${UUID.randomUUID()}.jpg"
        gateway.withConfiguredClient { it.storage.from("resource-photos").upload(path, bytes) }
        AppResult.Success(path)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.SERVER, error)
    }

    private companion object { const val MAX_UPLOAD_BYTES = 8 * 1024 * 1024 }
}
