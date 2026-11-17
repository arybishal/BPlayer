package com.pulse.player.ui.settings

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.ui.theme.ThemeMode
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
    val fontScale: Float = 1.0f,
    val isLiquidGlassEnabled: Boolean = true,
    val isBatteryOptimized: Boolean = true,
    val geminiApiKey: String = "",
    val auddApiKey: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _batteryOptimized = MutableStateFlow(checkBatteryStatus())

    private val themeAndFontFlow = combine(
        preferencesManager.themeModeFlow,
        preferencesManager.fontScaleFlow,
        preferencesManager.liquidGlassFlow
    ) { theme, fontScale, glass ->
        Triple(theme, fontScale, glass)
    }

    private val apiAndBatteryFlow = combine(
        preferencesManager.geminiApiKeyFlow,
        preferencesManager.auddApiKeyFlow,
        _batteryOptimized
    ) { geminiKey, auddKey, batteryOpt ->
        Triple(geminiKey, auddKey, batteryOpt)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        themeAndFontFlow,
        apiAndBatteryFlow
    ) { (theme, fontScale, glass), (geminiKey, auddKey, batteryOpt) ->
        SettingsUiState(
            themeMode = theme,
            fontScale = fontScale,
            isLiquidGlassEnabled = glass,
            isBatteryOptimized = batteryOpt,
            geminiApiKey = geminiKey,
            auddApiKey = auddKey
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
