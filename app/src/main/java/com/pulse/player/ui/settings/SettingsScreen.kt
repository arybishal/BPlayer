package com.pulse.player.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.sleeptimer.SleepTimerManager
import com.pulse.player.ui.equalizer.EqualizerSheet
import com.pulse.player.ui.sleeptimer.SleepTimerDialog
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    onNavigateToErrorLogs: () -> Unit,
    audioEffectsController: AudioEffectsController? = null,
    sleepTimerManager: SleepTimerManager? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = LocalExtendedColors.current
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }
    var showApiKeysDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshBatteryStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Appearance & Typography
        SettingsSectionTitle(title = "Appearance & Styling")

        val themeLabel = when (state.themeMode) {
            ThemeMode.DARK -> "Charcoal Dark (Default)"
            ThemeMode.AMOLED -> "Pitch Black (AMOLED)"
            ThemeMode.LIGHT -> "Warm Light"
            ThemeMode.SYSTEM -> "System Follower"
        }

        SettingsActionRow(
            icon = Icons.Rounded.ColorLens,
            title = "Theme Foundation",
            subtitle = themeLabel,
            onClick = { showThemeDialog = true }
        )

        SettingsActionRow(
            icon = Icons.Rounded.FormatSize,
            title = "Typography Scale",
            subtitle = "${(state.fontScale * 100).toInt()}% multiplier",
            onClick = { showFontDialog = true }
        )

        SettingsSwitchRow(
            icon = Icons.Rounded.BlurOn,
            title = "Liquid Glass Effect",
            subtitle = "Translucent frosted surfaces (RenderEffect on Android 12+)",
            checked = state.isLiquidGlassEnabled,
            onCheckedChange = { viewModel.setLiquidGlassEnabled(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Audio Engine & Timers
        SettingsSectionTitle(title = "Audio Engine & Transport")

        SettingsActionRow(
            icon = Icons.Rounded.GraphicEq,
            title = "System Equalizer & Effects",
            subtitle = "Hardware-accelerated EQ, Bass Boost, 3D Virtualizer",
            onClick = { showEqualizerSheet = true }
        )

        SettingsActionRow(
            icon = Icons.Rounded.Bedtime,
            title = "Sleep Timer",
            subtitle = "Smooth volume fade-out prior to pause",
            onClick = { showSleepTimerDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Background Reliability & Battery
        SettingsSectionTitle(title = "Background Playback Reliability")

        SettingsActionRow(
            icon = if (state.isBatteryOptimized) Icons.Rounded.BatteryAlert else Icons.Rounded.VerifiedUser,
            title = "Battery Optimization",
            subtitle = if (state.isBatteryOptimized) {
                "⚠️ Battery Optimized (OEM may kill playback)"
            } else {
                "✅ Unrestricted (Rock-solid background playback)"
            },
            onClick = { showBatteryDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Cloud & Integrations
        SettingsSectionTitle(title = "Cloud & AI Integrations")

        SettingsActionRow(
            icon = Icons.Rounded.Key,
            title = "API Credentials",
            subtitle = "Gemini AI (lyrics generation) & AudD (song identification)",
            onClick = { showApiKeysDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section 5: Diagnostics & About
        SettingsSectionTitle(title = "Diagnostics & Info")

        SettingsActionRow(
            icon = Icons.Rounded.BugReport,
            title = "Diagnostic Error Logs",
            subtitle = "Inspect and export runtime diagnostic SQLite logs",
            onClick = onNavigateToErrorLogs
        )

        SettingsActionRow(
            icon = Icons.Rounded.Info,
            title = "About Pulse",
            subtitle = "Version 1.0.0 • Offline content-first audio player",
            onClick = { showAboutDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Theme Chooser Dialog
    if (showThemeDialog) {
        ThemeChooserDialog(
            currentMode = state.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelectMode = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            }
        )
    }

    // Font Scale Dialog
    if (showFontDialog) {
        FontScaleDialog(
            currentScale = state.fontScale,
            onDismiss = { showFontDialog = false },
            onSave = {
                viewModel.setFontScale(it)
                showFontDialog = false
            }
        )
    }

    // Battery Optimization Dialog
    if (showBatteryDialog) {
        BatteryOptimizationDialog(
            isOptimized = state.isBatteryOptimized,
            onDismiss = { showBatteryDialog = false },
            onRequestExemption = {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(fallbackIntent)
                }
                showBatteryDialog = false
            }
        )
    }

    // API Keys Dialog
    if (showApiKeysDialog) {
        ApiKeysDialog(
            initialGeminiKey = state.geminiApiKey,
            initialAuddKey = state.auddApiKey,
            onDismiss = { showApiKeysDialog = false },
            onSave = { gemini, audd ->
                viewModel.setGeminiApiKey(gemini)
                viewModel.setAuddApiKey(audd)
                showApiKeysDialog = false
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Pulse Music Player") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pulse is an exquisite, content-first offline music player built with Jetpack Compose and AndroidX Media3.")
                    Text("• Strict no-blue-gradient warm palette\n• High-precision LRC synchronized lyrics\n• Equalizer & 10s fade-out sleep timer\n• Material 3 Design Tokens")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Equalizer Sheet
    if (showEqualizerSheet && audioEffectsController != null) {
        EqualizerSheet(
            audioEffectsController = audioEffectsController,
            onDismiss = { showEqualizerSheet = false }
        )
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog && sleepTimerManager != null) {
        SleepTimerDialog(
            sleepTimerManager = sleepTimerManager,
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = extendedColors.onSurfaceMuted)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = extendedColors.onSurfaceSubtle,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = extendedColors.onSurfaceMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun ThemeChooserDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelectMode: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ThemeOptionItem(
                    title = "Charcoal Dark (Default)",
                    subtitle = "Refined neutral deep charcoals",
                    isSelected = currentMode == ThemeMode.DARK,
                    onClick = { onSelectMode(ThemeMode.DARK) }
                )
                ThemeOptionItem(
                    title = "Pitch Black (AMOLED)",
                    subtitle = "Pure #000000 background for battery savings",
                    isSelected = currentMode == ThemeMode.AMOLED,
                    onClick = { onSelectMode(ThemeMode.AMOLED) }
                )
                ThemeOptionItem(
                    title = "Warm Light",
                    subtitle = "Soft daylight cream tones (no blue gradients)",
                    isSelected = currentMode == ThemeMode.LIGHT,
                    onClick = { onSelectMode(ThemeMode.LIGHT) }
                )
                ThemeOptionItem(
                    title = "System Follower",
                    subtitle = "Automatically adapts to OS Dark Mode setting",
                    isSelected = currentMode == ThemeMode.SYSTEM,
                    onClick = { onSelectMode(ThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = extendedColors.onSurfaceMuted)
        }
    }
}

@Composable
private fun FontScaleDialog(
    currentScale: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(currentScale) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Typography Scaling") },
        text = {
            Column {
                Text(
                    text = "Adjust text size proportionally across all screens.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Scale: ${(scale * 100).toInt()}%")
                }
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.85f..1.40f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Sample Preview: Bohemian Rhapsody — Queen",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(scale) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun BatteryOptimizationDialog(
    isOptimized: Boolean,
    onDismiss: () -> Unit,
    onRequestExemption: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Background Playback Reliability") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isOptimized) {
                    Text(
                        text = "⚠️ Your device currently applies battery restrictions. Aggressive OEM background managers (such as Xiaomi MIUI/HyperOS, Samsung One UI, Oppo, Huawei) may kill audio playback after 15–30 minutes of screen-off time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.statusDanger
                    )
                } else {
                    Text(
                        text = "✅ Pulse has unrestricted background execution permissions. Background playback will survive deep sleep and screen-off states.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.statusSuccess
                    )
                }

                Text(
                    text = "OEM Guidelines:\n• Xiaomi: Set Background Battery to 'No restrictions' & enable Autostart.\n• Samsung: Remove Pulse from 'Sleeping apps'.\n• OnePlus/Oppo: Turn off 'Optimize battery use'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.onSurfaceMuted
                )
            }
        },
        confirmButton = {
            if (isOptimized) {
                Button(onClick = onRequestExemption) {
                    Text("Whitelist App")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ApiKeysDialog(
    initialGeminiKey: String,
    initialAuddKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var geminiKey by remember { mutableStateOf(initialGeminiKey) }
    var auddKey by remember { mutableStateOf(initialAuddKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cloud & AI Credentials") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Keys are securely stored in local app preferences and never shared.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Gemini AI API Key (Lyrics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = auddKey,
                    onValueChange = { auddKey = it },
                    label = { Text("AudD API Token (Song ID)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(geminiKey, auddKey) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
