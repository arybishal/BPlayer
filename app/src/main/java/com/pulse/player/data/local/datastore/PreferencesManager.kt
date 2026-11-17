package com.pulse.player.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
}
