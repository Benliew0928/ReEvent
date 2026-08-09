package com.reevent.app.core.config

import com.reevent.app.BuildConfig

enum class AppEnvironment(val wireValue: String) {
    LOCAL("local"),
    STAGING("staging"),
    PRODUCTION("production");

    companion object {
        val current: AppEnvironment by lazy { from(BuildConfig.APP_ENVIRONMENT) }

        fun from(value: String): AppEnvironment = entries.firstOrNull { it.wireValue == value }
            ?: error("Unsupported app environment: $value")
    }
}
