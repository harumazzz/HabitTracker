package com.haruma.habit.tracker.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val context: Context,
) {
    private object Keys {
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DEFAULT_REMINDER_MINUTES = intPreferencesKey("default_reminder_minutes")
        val LANGUAGE = stringPreferencesKey("language")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val BACKUP_ACCOUNT_EMAIL = stringPreferencesKey("backup_account_email")
    }

    val hasSeenOnboarding: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.HAS_SEEN_ONBOARDING] ?: false }

    val themeMode: Flow<String> =
        context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }

    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    val defaultReminderMinutes: Flow<Int> =
        context.dataStore.data.map { it[Keys.DEFAULT_REMINDER_MINUTES] ?: -1 }

    val language: Flow<String> =
        context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }

    val lastBackupTime: Flow<Long> =
        context.dataStore.data.map { it[Keys.LAST_BACKUP_TIME] ?: -1L }

    val backupAccountEmail: Flow<String> =
        context.dataStore.data.map { it[Keys.BACKUP_ACCOUNT_EMAIL] ?: "" }

    suspend fun completeOnboarding(
        language: String,
        reminderMinutes: Int,
        notificationsEnabled: Boolean,
    ) = context.dataStore.edit {
        it[Keys.LANGUAGE] = language
        it[Keys.DEFAULT_REMINDER_MINUTES] = reminderMinutes
        it[Keys.NOTIFICATIONS_ENABLED] = notificationsEnabled
        it[Keys.HAS_SEEN_ONBOARDING] = true
    }

    suspend fun markOnboardingComplete() =
        context.dataStore.edit { it[Keys.HAS_SEEN_ONBOARDING] = true }

    suspend fun setThemeMode(mode: String) =
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }

    suspend fun setDefaultReminderMinutes(minutes: Int) =
        context.dataStore.edit { it[Keys.DEFAULT_REMINDER_MINUTES] = minutes }

    suspend fun setLanguage(language: String) =
        context.dataStore.edit { it[Keys.LANGUAGE] = language }

    suspend fun setLastBackupInfo(timeMillis: Long, email: String) =
        context.dataStore.edit {
            it[Keys.LAST_BACKUP_TIME] = timeMillis
            it[Keys.BACKUP_ACCOUNT_EMAIL] = email
        }
}
