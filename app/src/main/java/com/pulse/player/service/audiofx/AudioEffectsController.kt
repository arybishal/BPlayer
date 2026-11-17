@file:Suppress("DEPRECATION")

package com.pulse.player.service.audiofx

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pulse.player.data.local.datastore.pulseDataStore
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMilliDb: Short,
    val maxLevelMilliDb: Short,
    val currentLevelMilliDb: Short
)

data class AudioEffectsState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val currentPreset: String = "Flat",
    val availablePresets: List<String> = emptyList(),
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val isBassBoostSupported: Boolean = true,
    val isVirtualizerSupported: Boolean = true,
    val isLoudnessSupported: Boolean = true
)

@Suppress("DEPRECATION")
@Singleton
class AudioEffectsController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "AudioEffectsController"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var currentSessionId: Int = 0
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _state = MutableStateFlow(AudioEffectsState())
    val state: StateFlow<AudioEffectsState> = _state.asStateFlow()

    private val KEY_EQ_ENABLED = booleanPreferencesKey("eq_enabled")
    private val KEY_EQ_PRESET = stringPreferencesKey("eq_preset")
    private val KEY_BASS_BOOST = intPreferencesKey("eq_bass_boost")
    private val KEY_VIRTUALIZER = intPreferencesKey("eq_virtualizer")

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        currentSessionId = audioSessionId
        releaseEffects()

        try {
            // 1. Equalizer
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }

            // 2. BassBoost
            try {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    if (strengthSupported) {
                        setStrength(_state.value.bassBoostStrength)
                        enabled = _state.value.isEnabled
                    }
                }
            } catch (e: Exception) {
                AppLogger.d(tag, "BassBoost unsupported: ${e.message}")
            }

            // 3. Virtualizer
            try {
                virtualizer = Virtualizer(0, audioSessionId).apply {
                    if (strengthSupported) {
                        setStrength(_state.value.virtualizerStrength)
                        enabled = _state.value.isEnabled
                    }
                }
            } catch (e: Exception) {
                AppLogger.d(tag, "Virtualizer unsupported: ${e.message}")
            }

            // 4. Loudness Enhancer
            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _state.value.isEnabled
                }
            } catch (e: Exception) {
                AppLogger.d(tag, "LoudnessEnhancer unsupported: ${e.message}")
            }

            loadPreferencesAndSync()
            AppLogger.i(tag, "Audio effects attached to audio session $audioSessionId")
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to initialize audio effects", e)
        }
    }

    private fun loadPreferencesAndSync() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands
        val minLevel = eq.bandLevelRange[0]
        val maxLevel = eq.bandLevelRange[1]

        val bandsList = mutableListOf<EqualizerBand>()
        for (i in 0 until numBands) {
            val bandIdx = i.toShort()
            val freq = eq.getCenterFreq(bandIdx) / 1000 // Convert mHz to Hz
            val currentLevel = eq.getBandLevel(bandIdx)
            bandsList.add(
                EqualizerBand(
                    index = bandIdx,
                    centerFreqHz = freq,
                    minLevelMilliDb = minLevel,
                    maxLevelMilliDb = maxLevel,
                    currentLevelMilliDb = currentLevel
                )
            )
        }

        val presetsList = mutableListOf<String>()
        val numPresets = eq.numberOfPresets
        for (i in 0 until numPresets) {
            presetsList.add(eq.getPresetName(i.toShort()))
        }
        if (!presetsList.contains("Custom")) {
            presetsList.add("Custom")
        }

        _state.value = _state.value.copy(
            bands = bandsList,
            availablePresets = presetsList,
            isBassBoostSupported = bassBoost?.strengthSupported ?: false,
            isVirtualizerSupported = virtualizer?.strengthSupported ?: false
        )

        scope.launch {
            try {
                val prefs = context.pulseDataStore.data.first()
                val isEnabled = prefs[KEY_EQ_ENABLED] ?: false
                val preset = prefs[KEY_EQ_PRESET] ?: "Flat"
                val bass = (prefs[KEY_BASS_BOOST] ?: 0).toShort()
                val virt = (prefs[KEY_VIRTUALIZER] ?: 0).toShort()

                setEnabled(isEnabled)
                setBassBoost(bass)
                setVirtualizer(virt)
                if (preset != "Custom") {
                    setPreset(preset)
                }
            } catch (e: Exception) {
                AppLogger.d(tag, "Could not load saved audiofx settings: ${e.message}")
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            loudnessEnhancer?.enabled = enabled
            _state.value = _state.value.copy(isEnabled = enabled)

            scope.launch {
                context.pulseDataStore.edit { it[KEY_EQ_ENABLED] = enabled }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to toggle effects: ${e.message}")
        }
    }

    fun setBandLevel(bandIndex: Short, levelMilliDb: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMilliDb)
            val updatedBands = _state.value.bands.map {
                if (it.index == bandIndex) it.copy(currentLevelMilliDb = levelMilliDb) else it
            }
            _state.value = _state.value.copy(bands = updatedBands, currentPreset = "Custom")

            scope.launch {
                context.pulseDataStore.edit { it[KEY_EQ_PRESET] = "Custom" }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to set band level: ${e.message}")
        }
    }

    fun setPreset(presetName: String) {
        val eq = equalizer ?: return
        try {
            val presetIndex = (0 until eq.numberOfPresets).firstOrNull {
                eq.getPresetName(it.toShort()).equals(presetName, ignoreCase = true)
            }?.toShort()

            if (presetIndex != null) {
                eq.usePreset(presetIndex)
                val updatedBands = _state.value.bands.map {
                    it.copy(currentLevelMilliDb = eq.getBandLevel(it.index))
                }
                _state.value = _state.value.copy(
                    currentPreset = presetName,
                    bands = updatedBands
                )

                scope.launch {
                    context.pulseDataStore.edit { it[KEY_EQ_PRESET] = presetName }
                }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to use preset $presetName: ${e.message}")
        }
    }

    fun setBassBoost(strength: Short) {
        try {
            val clamped = strength.coerceIn(0, 1000)
            bassBoost?.setStrength(clamped)
            _state.value = _state.value.copy(bassBoostStrength = clamped)

            scope.launch {
                context.pulseDataStore.edit { it[KEY_BASS_BOOST] = clamped.toInt() }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to set bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(strength: Short) {
        try {
            val clamped = strength.coerceIn(0, 1000)
            virtualizer?.setStrength(clamped)
            _state.value = _state.value.copy(virtualizerStrength = clamped)

            scope.launch {
                context.pulseDataStore.edit { it[KEY_VIRTUALIZER] = clamped.toInt() }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to set virtualizer: ${e.message}")
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            loudnessEnhancer?.release()
            equalizer = null
            bassBoost = null
            virtualizer = null
            loudnessEnhancer = null
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed releasing audiofx: ${e.message}")
        }
    }
}
