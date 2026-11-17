package com.pulse.player.ui.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.audiofx.EqualizerBand
import com.pulse.player.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    audioEffectsController: AudioEffectsController,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by audioEffectsController.state.collectAsState()
    val extendedColors = LocalExtendedColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header with Master Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = if (state.isEnabled) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Equalizer",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (state.isEnabled) "System AudioFX Active" else "Disabled",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isEnabled) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceMuted
                        )
                    }
                }

                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = { audioEffectsController.setEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Horizontal Chip Bar
            Text(
                text = "Presets",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.onSurfaceMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.availablePresets) { presetName ->
                    val isSelected = state.currentPreset.equals(presetName, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .clickable(enabled = state.isEnabled) {
                                audioEffectsController.setPreset(presetName)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = presetName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frequency Bands Sliders
            Text(
                text = "Frequency Bands",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.onSurfaceMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            state.bands.forEach { band ->
                BandSliderRow(
                    band = band,
                    enabled = state.isEnabled,
                    onLevelChange = { newLevel ->
                        audioEffectsController.setBandLevel(band.index, newLevel)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Additional Audio Effects: Bass Boost & Virtualizer
            Text(
                text = "Audio Enhancements",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.onSurfaceMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Bass Boost
            if (state.isBassBoostSupported) {
                EffectSliderRow(
                    icon = Icons.Rounded.Waves,
                    label = "Bass Boost",
                    value = state.bassBoostStrength.toFloat(),
                    max = 1000f,
                    enabled = state.isEnabled,
                    onValueChange = { audioEffectsController.setBassBoost(it.toInt().toShort()) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Virtualizer
            if (state.isVirtualizerSupported) {
                EffectSliderRow(
                    icon = Icons.Rounded.SurroundSound,
                    label = "Virtualizer (3D Audio)",
                    value = state.virtualizerStrength.toFloat(),
                    max = 1000f,
                    enabled = state.isEnabled,
                    onValueChange = { audioEffectsController.setVirtualizer(it.toInt().toShort()) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BandSliderRow(
    band: EqualizerBand,
    enabled: Boolean,
    onLevelChange: (Short) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val freqLabel = if (band.centerFreqHz >= 1000) {
        "%.1fkHz".format(band.centerFreqHz / 1000f)
    } else {
        "${band.centerFreqHz}Hz"
    }
    val dbValue = band.currentLevelMilliDb / 100 // Convert mB to dB

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = freqLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(60.dp)
        )

        Slider(
            value = band.currentLevelMilliDb.toFloat(),
            onValueChange = { onLevelChange(it.toInt().toShort()) },
            valueRange = band.minLevelMilliDb.toFloat()..band.maxLevelMilliDb.toFloat(),
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (dbValue >= 0) "+${dbValue}dB" else "${dbValue}dB",
            style = MaterialTheme.typography.labelSmall,
            color = if (dbValue != 0) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceMuted,
            modifier = Modifier.width(50.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
private fun EffectSliderRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Float,
    max: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val percent = ((value / max) * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled && percent > 0) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceMuted,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "$percent%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..max,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
