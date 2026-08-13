package com.reevent.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Runtime screens provide an authenticated loader for private Supabase resource photos.
 * Preview fixtures deliberately use the default null loader and retain their drawable image.
 */
val LocalResourcePhotoLoader = staticCompositionLocalOf<suspend (String) -> ByteArray?> { { null } }

@Composable
fun ResourcePhotoImage(
    photoPath: String?,
    @DrawableRes fallbackImageRes: Int,
    contentDescription: String,
    modifier: Modifier,
) {
    val photoLoader = LocalResourcePhotoLoader.current
    val bitmap by produceState<Bitmap?>(initialValue = null, photoPath) {
        value =
            photoPath?.let { path ->
                photoLoader(path)?.let { bytes ->
                    withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                }
            }
    }
    if (bitmap == null) {
        Image(
            painter = painterResource(fallbackImageRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
