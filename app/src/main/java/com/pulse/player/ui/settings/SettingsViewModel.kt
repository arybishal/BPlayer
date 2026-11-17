package com.pulse.player.ui.settings

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.ui.theme.AccentChoice
import com.pulse.player.ui.theme.ThemeMode
import com.pulse.player.data.scrobbler.ScrobbleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentChoice: AccentChoice = AccentChoice.WARM_AMBER,
    val fontScale: Float = 1.0f,
    val isLiquidGlassEnabled: Boolean = true,
    val isBatteryOptimized: Boolean = true,
    val geminiApiKey: String = "",
    val auddApiKey: String = "",
    val lastFmUsername: String = ""
)

private data class ThemeAndStyleData(
    val theme: ThemeMode,
    val accent: AccentChoice,
    val fontScale: Float,
    val glass: Boolean
)

private data class ApiAndBatteryData(
    val geminiKey: String,
    val auddKey: String,
    val lastFmUser: String,
    val batteryOpt: Boolean
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val preferencesManager: PreferencesManager,
    val scrobbleRepository: ScrobbleRepository
) : ViewModel() {

    private val _batteryOptimized = MutableStateFlow(checkBatteryStatus())

    private val themeAndFontFlow = combine(
        preferencesManager.themeModeFlow,
        preferencesManager.accentChoiceFlow,
        preferencesManager.fontScaleFlow,
        preferencesManager.liquidGlassFlow
    ) { theme, accent, fontScale, glass ->
        ThemeAndStyleData(theme, accent, fontScale, glass)
    }

    private val apiAndBatteryFlow = combine(
        preferencesManager.geminiApiKeyFlow,
        preferencesManager.auddApiKeyFlow,
        preferencesManager.lastFmUsernameFlow,
        _batteryOptimized
    ) { geminiKey, auddKey, lastFmUser, batteryOpt ->
        ApiAndBatteryData(geminiKey, auddKey, lastFmUser, batteryOpt)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        themeAndFontFlow,
        apiAndBatteryFlow
    ) { styleData, apiData ->
        SettingsUiState(
            themeMode = styleData.theme,
            accentChoice = styleData.accent,
            fontScale = styleData.fontScale,
            isLiquidGlassEnabled = styleData.glass,
            isBatteryOptimized = apiData.batteryOpt,
            geminiApiKey = apiData.geminiKey,
            auddApiKey = apiData.auddKey,
            lastFmUsername = apiData.lastFmUser
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun refreshBatteryStatus() {
        _batteryOptimized.value = checkBatteryStatus()
    }

    private fun checkBatteryStatus(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.let { !it.isIgnoringBatteryOptimizations(context.packageName) } ?: true
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setAccentChoice(choice: AccentChoice) {
        viewModelScope.launch { preferencesManager.setAccentChoice(choice) }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch { preferencesManager.setFontScale(scale) }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setLiquidGlassEnabled(enabled) }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch { preferencesManager.setGeminiApiKey(key) }
    }

    fun setAuddApiKey(key: String) {
        viewModelScope.launch { preferencesManager.setAuddApiKey(key) }
    }
}
