package com.reevent.app.core.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.reevent.app.core.database.CoreDao
import com.reevent.app.core.network.SupabaseAuthGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class ResourcePhotoMutationResponse(
    @SerialName("storage_path") val storagePath: String,
    @SerialName("cleanup_paths") val cleanupPaths: List<String> = emptyList()
)

private data class ResourcePhotoDescriptor(
    val mimeType: String,
    val width: Int,
    val height: Int,
    val byteSize: Int
)

@Singleton
class SupabaseMediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountScope: AccountScope,
    private val gateway: SupabaseAuthGateway,
    private val dao: CoreDao
) : MediaRepository {
    override suspend fun uploadResourcePhoto(resourceId: String, uri: Uri): AppResult<String> = try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return AppResult.Failure(FailureReason.VALIDATION)
        if (bytes.size > MAX_UPLOAD_BYTES) return AppResult.Failure(FailureReason.VALIDATION)
        val descriptor = describe(bytes) ?: return AppResult.Failure(FailureReason.VALIDATION)
        val accountId = accountScope.requireId()
        // A deterministic path means an interrupted replacement can be retried without creating
        // untracked random objects. Metadata history still tracks older pre-migration paths until
        // their Storage deletion succeeds.
        val path = "$accountId/resources/$resourceId/primary"
        gateway.withConfiguredClient { client ->
            client.storage.from(RESOURCE_PHOTO_BUCKET).upload(path, bytes) {
                upsert = true
                contentType = ContentType.parse(descriptor.mimeType)
            }
        }

        val mutation = try {
            gateway.withConfiguredClient { client ->
                client.postgrest.rpc(
                    "replace_resource_photo",
                    buildJsonObject {
                        put("p_resource_id", resourceId)
                        put("p_storage_path", path)
                        put("p_mime_type", descriptor.mimeType)
                        put("p_width", descriptor.width)
                        put("p_height", descriptor.height)
                        put("p_byte_size", descriptor.byteSize)
                    }
                ).decodeSingle<ResourcePhotoMutationResponse>()
            }
        } catch (error: Throwable) {
            // Never delete this deterministic path here: it may already be the currently tracked
            // photo whose bytes were just replaced. The same path is safe to overwrite/retry and
            // account deletion always removes the whole private prefix.
            throw error
        }

        for (oldPath in mutation.cleanupPaths.filterNot { it == mutation.storagePath }) {
            gateway.withConfiguredClient { it.storage.from(RESOURCE_PHOTO_BUCKET).delete(oldPath) }
            gateway.withConfiguredClient { client ->
                client.postgrest.rpc(
                    "complete_resource_photo_cleanup",
                    buildJsonObject {
                        put("p_resource_id", resourceId)
                        put("p_storage_path", oldPath)
                    }
                )
            }
        }

        // The RPC acknowledgement is authoritative. Update the existing account-scoped Room row
        // immediately; the next full snapshot independently confirms the same metadata.
        dao.setResourceImageUrls(accountId, resourceId, mediaJson.encodeToString(listOf(mutation.storagePath)))
        AppResult.Success(path)
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.SERVER, error)
    }

    override suspend fun downloadResourcePhoto(path: String): AppResult<ByteArray> = try {
        AppResult.Success(gateway.withConfiguredClient { it.storage.from("resource-photos").downloadAuthenticated(path) })
    } catch (error: Throwable) {
        AppResult.Failure(FailureReason.SERVER, error)
    }

    private fun describe(bytes: ByteArray): ResourcePhotoDescriptor? {
        if (bytes.isEmpty()) return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        val mimeType = when (options.outMimeType?.lowercase()) {
            "image/jpeg", "image/jpg" -> "image/jpeg"
            "image/png" -> "image/png"
            "image/webp" -> "image/webp"
            else -> return null
        }
        if (options.outWidth !in 1..MAX_IMAGE_DIMENSION || options.outHeight !in 1..MAX_IMAGE_DIMENSION) return null
        return ResourcePhotoDescriptor(mimeType, options.outWidth, options.outHeight, bytes.size)
    }

    private companion object {
        const val RESOURCE_PHOTO_BUCKET = "resource-photos"
        const val MAX_UPLOAD_BYTES = 8 * 1024 * 1024
        const val MAX_IMAGE_DIMENSION = 8192
        val mediaJson = Json
    }
}
