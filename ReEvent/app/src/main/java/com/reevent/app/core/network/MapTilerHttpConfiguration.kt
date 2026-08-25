package com.reevent.app.core.network

import android.content.Context
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.module.http.HttpRequestUtil

object MapTilerHttpConfiguration {
    @Volatile
    private var configured = false

    fun ensureInitialized(context: Context) {
        if (configured) return
        synchronized(this) {
            if (configured) return
            MapLibre.getInstance(context.applicationContext)
            HttpRequestUtil.setOkHttpClient(
                OkHttpClient.Builder()
                    .addNetworkInterceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .header("User-Agent", MAPTILER_USER_AGENT)
                                .build(),
                        )
                    }
                    .build(),
            )
            configured = true
        }
    }

    private const val MAPTILER_USER_AGENT = "ReEvent/1.0 Android"
}
