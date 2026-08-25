package com.reevent.app.core.location

import android.content.Context
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.reevent.app.core.model.GeoLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class DeviceLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    @RequiresPermission(anyOf = [android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION])
    suspend fun currentApproximateLocation(): GeoLocation? = suspendCancellableCoroutine { continuation ->
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> null
        }
        if (provider == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val cancellation = CancellationSignal()
        continuation.invokeOnCancellation { cancellation.cancel() }
        runCatching {
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                cancellation,
                ContextCompat.getMainExecutor(context),
            ) { location ->
                if (continuation.isActive) {
                    continuation.resume(GeoLocation("Current approximate location", location.latitude, location.longitude))
                }
            }
        }.onFailure {
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
