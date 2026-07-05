package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "captureflow_settings")

/**
 * Persisted user settings (theme, notifications, AI/privacy toggles) backed by
 * Preferences DataStore. Exposed as cold Flows so the UI recomposes on change.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ON_DEVICE_AI = booleanPreferencesKey("on_device_ai")
        val CLOUD_AI = booleanPreferencesKey("cloud_ai")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val CONSENT_GIVEN = booleanPreferencesKey("dpdp_consent_given")
        val CONSENT_TIMESTAMP = stringPreferencesKey("dpdp_consent_timestamp")
        val AI_SUMMARIES_USED = intPreferencesKey("ai_summaries_used")
        val AI_SUMMARIES_RESET_DATE = stringPreferencesKey("ai_summaries_reset_date")
        val FOCUS_SESSIONS_USED = intPreferencesKey("focus_sessions_used")
        val FOCUS_SESSIONS_RESET_DATE = stringPreferencesKey("focus_sessions_reset_date")
        val CLOUD_SYNC = booleanPreferencesKey("cloud_sync_enabled")
    }

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            "LIGHT" -> ThemeMode.LIGHT
            "DARK" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val onDeviceAiEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.ON_DEVICE_AI] ?: true }
    val cloudAiEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.CLOUD_AI] ?: true }
    val analyticsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.ANALYTICS_ENABLED] ?: true }

    /** Cross-device sync of your data via your signed-in account. Defaults on, matching [cloudAiEnabled]; can be turned off in Settings. */
    val cloudSyncEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.CLOUD_SYNC] ?: true }

    /**
     * DPDP Act 2023/Rules 2025 consent: must be explicit, unbundled, and not a
     * condition of using unrelated parts of the app. Defaults to false (not given)
     * until the user actively accepts the onboarding consent step.
     */
    val consentGiven: Flow<Boolean> = context.settingsDataStore.data.map { it[Keys.CONSENT_GIVEN] ?: false }
    val consentTimestamp: Flow<String?> = context.settingsDataStore.data.map { it[Keys.CONSENT_TIMESTAMP] }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setOnDeviceAiEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.ON_DEVICE_AI] = enabled }
    }

    suspend fun setCloudAiEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.CLOUD_AI] = enabled }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.ANALYTICS_ENABLED] = enabled }
    }

    suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.CLOUD_SYNC] = enabled }
    }

    /** Records explicit DPDP consent (or its withdrawal) with a timestamp. */
    suspend fun setConsentGiven(given: Boolean) {
        context.settingsDataStore.edit {
            it[Keys.CONSENT_GIVEN] = given
            it[Keys.CONSENT_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }

    /** Number of AI summaries used today; resets to 0 whenever the stored reset date isn't today. */
    val aiSummariesUsedToday: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        val storedDate = prefs[Keys.AI_SUMMARIES_RESET_DATE]
        if (storedDate != LocalDate.now().toString()) 0 else (prefs[Keys.AI_SUMMARIES_USED] ?: 0)
    }

    /** Records one AI summary usage, auto-resetting the counter if the day has rolled over. */
    suspend fun recordAiSummaryUsed() {
        context.settingsDataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val current = if (prefs[Keys.AI_SUMMARIES_RESET_DATE] == today) (prefs[Keys.AI_SUMMARIES_USED] ?: 0) else 0
            prefs[Keys.AI_SUMMARIES_RESET_DATE] = today
            prefs[Keys.AI_SUMMARIES_USED] = current + 1
        }
    }

    /** Number of focus sessions completed today; resets to 0 whenever the stored reset date isn't today. */
    val focusSessionsToday: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        val storedDate = prefs[Keys.FOCUS_SESSIONS_RESET_DATE]
        if (storedDate != LocalDate.now().toString()) 0 else (prefs[Keys.FOCUS_SESSIONS_USED] ?: 0)
    }

    /** Records one completed focus session, auto-resetting the counter if the day has rolled over. */
    suspend fun recordFocusSessionCompleted() {
        context.settingsDataStore.edit { prefs ->
            val today = LocalDate.now().toString()
            val current = if (prefs[Keys.FOCUS_SESSIONS_RESET_DATE] == today) (prefs[Keys.FOCUS_SESSIONS_USED] ?: 0) else 0
            prefs[Keys.FOCUS_SESSIONS_RESET_DATE] = today
            prefs[Keys.FOCUS_SESSIONS_USED] = current + 1
        }
    }
}
