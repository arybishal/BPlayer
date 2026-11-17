package com.pulse.player.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pulse.player.ui.theme.AccentChoice
import com.pulse.player.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.pulseDataStore: DataStore<Preferences> by preferencesDataStore(name = "pulse_settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) : RecognitionPreferences {
    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_ACCENT_CHOICE = stringPreferencesKey("accent_choice")
        val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        val KEY_LIQUID_GLASS = booleanPreferencesKey("liquid_glass_enabled")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_AUDD_API_KEY = stringPreferencesKey("audd_api_key")
        val KEY_LASTFM_API_KEY = stringPreferencesKey("lastfm_api_key")
        val KEY_LASTFM_API_SECRET = stringPreferencesKey("lastfm_api_secret")
        val KEY_LASTFM_SESSION_KEY = stringPreferencesKey("lastfm_session_key")
        val KEY_LASTFM_USERNAME = stringPreferencesKey("lastfm_username")
        val KEY_LASTFM_SCROBBLING_ENABLED = booleanPreferencesKey("lastfm_scrobbling_enabled")
        val KEY_LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
        val KEY_LAST_BACKUP_NAME = stringPreferencesKey("last_backup_name")
        val KEY_EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val KEY_EQ_PRESET = stringPreferencesKey("eq_preset")
        val KEY_BASS_BOOST = intPreferencesKey("eq_bass_boost")
        val KEY_VIRTUALIZER = intPreferencesKey("eq_virtualizer")
    }

    val lastBackupTimestampFlow: Flow<Long?> = context.pulseDataStore.data.map { prefs ->
        prefs[KEY_LAST_BACKUP_TIMESTAMP]
    }

    val lastBackupNameFlow: Flow<String?> = context.pulseDataStore.data.map { prefs ->
        prefs[KEY_LAST_BACKUP_NAME]
    }

    val themeModeFlow: Flow<ThemeMode> = context.pulseDataStore.data.map { prefs ->
        val modeStr = prefs[KEY_THEME_MODE] ?: ThemeMode.DARK.name
        try { ThemeMode.valueOf(modeStr) } catch (_: Exception) { ThemeMode.DARK }
    }

    val accentChoiceFlow: Flow<AccentChoice> = context.pulseDataStore.data.map { prefs ->
        val accentStr = prefs[KEY_ACCENT_CHOICE] ?: AccentChoice.WARM_AMBER.name
        try { AccentChoice.valueOf(accentStr) } catch (_: Exception) { AccentChoice.WARM_AMBER }
    }

    val fontScaleFlow: Flow<Float> = context.pulseDataStore.data.map { prefs ->
        prefs[KEY_FONT_SCALE] ?: 1.0f
    }

    val liquidGlassFlow: Flow<Boolean> = context.pulseDataStore.data.map { prefs ->
        prefs[KEY_LIQUID_GLASS] ?: true
    }

    val geminiApiKeyFlow: Flow<String> = context.pulseDataStore.data.map { prefs ->
        prefs[KEY_GEMINI_API_KEY] ?: ""
    }

    override val auddApiKeyFlow: Flow<String> = context.pulseDataStore.data.map { prefs ->
        prefs[KEY_AUDD_API_KEY] ?: ""
    }

    val lastFmApiKeyFlow: Flow<String> = context.pulseDataStore.data.map { it[KEY_LASTFM_API_KEY] ?: "" }
    val lastFmApiSecretFlow: Flow<String> = context.pulseDataStore.data.map { it[KEY_LASTFM_API_SECRET] ?: "" }
    val lastFmSessionKeyFlow: Flow<String> = context.pulseDataStore.data.map { it[KEY_LASTFM_SESSION_KEY] ?: "" }
    val lastFmUsernameFlow: Flow<String> = context.pulseDataStore.data.map { it[KEY_LASTFM_USERNAME] ?: "" }
    val lastFmScrobblingEnabledFlow: Flow<Boolean> = context.pulseDataStore.data.map { it[KEY_LASTFM_SCROBBLING_ENABLED] ?: false }

    suspend fun setLastFmSession(apiKey: String, apiSecret: String, sessionKey: String, username: String) {
        context.pulseDataStore.edit {
            it[KEY_LASTFM_API_KEY] = apiKey.trim()
            it[KEY_LASTFM_API_SECRET] = apiSecret.trim()
            it[KEY_LASTFM_SESSION_KEY] = sessionKey.trim()
            it[KEY_LASTFM_USERNAME] = username.trim()
            it[KEY_LASTFM_SCROBBLING_ENABLED] = sessionKey.isNotBlank()
        }
    }

    suspend fun setLastFmScrobblingEnabled(enabled: Boolean) {
        context.pulseDataStore.edit { it[KEY_LASTFM_SCROBBLING_ENABLED] = enabled }
    }

    suspend fun clearLastFmSession() {
        context.pulseDataStore.edit {
            it.remove(KEY_LASTFM_SESSION_KEY)
            it.remove(KEY_LASTFM_USERNAME)
            it[KEY_LASTFM_SCROBBLING_ENABLED] = false
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.pulseDataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setAccentChoice(accent: AccentChoice) {
        context.pulseDataStore.edit { it[KEY_ACCENT_CHOICE] = accent.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.pulseDataStore.edit { it[KEY_FONT_SCALE] = scale.coerceIn(0.85f, 1.40f) }
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.pulseDataStore.edit { it[KEY_LIQUID_GLASS] = enabled }
    }

    suspend fun setGeminiApiKey(key: String) {
        context.pulseDataStore.edit { it[KEY_GEMINI_API_KEY] = key.trim() }
    }

    suspend fun setAuddApiKey(key: String) {
        context.pulseDataStore.edit { it[KEY_AUDD_API_KEY] = key.trim() }
    }

    suspend fun recordBackup(fileName: String, timestamp: Long = System.currentTimeMillis()) {
        context.pulseDataStore.edit {
            it[KEY_LAST_BACKUP_NAME] = fileName
            it[KEY_LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }
}
