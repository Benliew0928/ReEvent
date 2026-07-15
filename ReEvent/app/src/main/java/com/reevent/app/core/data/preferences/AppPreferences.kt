package com.reevent.app.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.reevent.app.core.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore by preferencesDataStore(name = "reevent_preferences")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
        val lastOpenedEventId = stringPreferencesKey("last_opened_event_id")
        val themeMode = stringPreferencesKey("theme_mode")
        val distanceUnit = stringPreferencesKey("distance_unit")
        val cachedUserId = stringPreferencesKey("cached_user_id")
        val cachedRole = stringPreferencesKey("cached_role")
    }

    val onboardingComplete: Flow<Boolean> = context.appPreferencesDataStore.data.map { it[Keys.onboardingComplete] ?: false }
    val cachedUserId: Flow<String?> = context.appPreferencesDataStore.data.map { it[Keys.cachedUserId] }
    val cachedRole: Flow<UserRole?> = context.appPreferencesDataStore.data.map { it[Keys.cachedRole]?.let(UserRole::valueOf) }

    suspend fun setOnboardingComplete(value: Boolean) = context.appPreferencesDataStore.edit { it[Keys.onboardingComplete] = value }
    suspend fun setLastOpenedEvent(eventId: String) = context.appPreferencesDataStore.edit { it[Keys.lastOpenedEventId] = eventId }
    suspend fun setThemeMode(mode: String) = context.appPreferencesDataStore.edit { it[Keys.themeMode] = mode }
    suspend fun setDistanceUnit(unit: String) = context.appPreferencesDataStore.edit { it[Keys.distanceUnit] = unit }

    suspend fun cacheAccount(userId: String, role: UserRole?) = context.appPreferencesDataStore.edit {
        it[Keys.cachedUserId] = userId
        if (role == null) it.remove(Keys.cachedRole) else it[Keys.cachedRole] = role.name
    }

    suspend fun clearAccount() = context.appPreferencesDataStore.edit {
        it.remove(Keys.cachedUserId)
        it.remove(Keys.cachedRole)
    }
}
