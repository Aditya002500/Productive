package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    /** Records explicit DPDP consent (or its withdrawal) with a timestamp. */
    suspend fun setConsentGiven(given: Boolean) {
        context.settingsDataStore.edit {
            it[Keys.CONSENT_GIVEN] = given
            it[Keys.CONSENT_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }
}
