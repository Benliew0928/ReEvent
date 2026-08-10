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
        val passwordRecoveryPending = booleanPreferencesKey("password_recovery_pending")
        fun resourceDraft(userId: String, eventId: String) = stringPreferencesKey("resource_draft_${userId}_${eventId}")
    }

    val onboardingComplete: Flow<Boolean> = context.appPreferencesDataStore.data.map { it[Keys.onboardingComplete] ?: false }
    val cachedUserId: Flow<String?> = context.appPreferencesDataStore.data.map { it[Keys.cachedUserId] }
    val cachedRole: Flow<UserRole?> = context.appPreferencesDataStore.data.map { it[Keys.cachedRole]?.let(UserRole::valueOf) }
    val lastOpenedEventId: Flow<String?> = context.appPreferencesDataStore.data.map { it[Keys.lastOpenedEventId] }
    val passwordRecoveryPending: Flow<Boolean> = context.appPreferencesDataStore.data.map { it[Keys.passwordRecoveryPending] ?: false }

    suspend fun setOnboardingComplete(value: Boolean) = context.appPreferencesDataStore.edit { it[Keys.onboardingComplete] = value }
    suspend fun setLastOpenedEvent(eventId: String) = context.appPreferencesDataStore.edit { it[Keys.lastOpenedEventId] = eventId }
    suspend fun setThemeMode(mode: String) = context.appPreferencesDataStore.edit { it[Keys.themeMode] = mode }
    suspend fun setDistanceUnit(unit: String) = context.appPreferencesDataStore.edit { it[Keys.distanceUnit] = unit }
    suspend fun setPasswordRecoveryPending(value: Boolean) = context.appPreferencesDataStore.edit { it[Keys.passwordRecoveryPending] = value }
    fun resourceDraft(userId: String, eventId: String): Flow<String?> = context.appPreferencesDataStore.data.map { it[Keys.resourceDraft(userId, eventId)] }
    suspend fun saveResourceDraft(userId: String, eventId: String, draft: String) = context.appPreferencesDataStore.edit { it[Keys.resourceDraft(userId, eventId)] = draft }
    suspend fun clearResourceDraft(userId: String, eventId: String) = context.appPreferencesDataStore.edit { it.remove(Keys.resourceDraft(userId, eventId)) }

    suspend fun cacheAccount(userId: String, role: UserRole?) = context.appPreferencesDataStore.edit {
        it[Keys.cachedUserId] = userId
        if (role == null) it.remove(Keys.cachedRole) else it[Keys.cachedRole] = role.name
    }

    suspend fun clearAccount(userId: String? = null) = context.appPreferencesDataStore.edit {
        if (userId == null || it[Keys.cachedUserId] == userId) {
            it.remove(Keys.cachedUserId)
            it.remove(Keys.cachedRole)
            it.remove(Keys.lastOpenedEventId)
            it.remove(Keys.passwordRecoveryPending)
        }
        val draftPrefix = userId?.let { id -> "resource_draft_${id}_" } ?: "resource_draft_"
        it.asMap().keys.filter { key -> key.name.startsWith(draftPrefix) }.forEach { key -> it.remove(key) }
    }
}
