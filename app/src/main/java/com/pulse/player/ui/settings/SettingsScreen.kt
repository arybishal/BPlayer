package com.pulse.player.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.sleeptimer.SleepTimerManager
import com.pulse.player.ui.equalizer.EqualizerSheet
import com.pulse.player.ui.sleeptimer.SleepTimerDialog
import com.pulse.player.ui.theme.AccentAmber
import com.pulse.player.ui.theme.AccentChoice
import com.pulse.player.ui.theme.AccentCoral
import com.pulse.player.ui.theme.AccentEmerald
import com.pulse.player.ui.theme.AmoledBackground
import com.pulse.player.ui.theme.DarkBackground
import com.pulse.player.ui.theme.LightBackground
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
    var showAccentDialog by remember { mutableStateOf(false) }
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

        // SECTION 1: Appearance & Design Tokens
        SettingsSectionHeader(title = "Appearance & Styling")

        SettingsCard {
            val themeLabel = when (state.themeMode) {
                ThemeMode.DARK -> "Charcoal Dark"
                ThemeMode.AMOLED -> "Pitch Black (AMOLED)"
                ThemeMode.LIGHT -> "Warm Light"
                ThemeMode.SYSTEM -> "Follow System"
            }

            SettingsActionRow(
                icon = Icons.Rounded.ColorLens,
                title = "Theme Foundation",
                subtitle = themeLabel,
                onClick = { showThemeDialog = true },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when (state.themeMode) {
                                    ThemeMode.AMOLED -> AmoledBackground
                                    ThemeMode.LIGHT -> LightBackground
                                    else -> DarkBackground
                                }
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            )

            SettingsCardDivider()

            val (accentName, accentColor) = when (state.accentChoice) {
                AccentChoice.WARM_AMBER -> Pair("Warm Amber", AccentAmber)
                AccentChoice.SUNSET_CORAL -> Pair("Sunset Coral", AccentCoral)
                AccentChoice.DEEP_EMERALD -> Pair("Deep Emerald", AccentEmerald)
            }

            SettingsActionRow(
                icon = Icons.Rounded.Palette,
                title = "Accent Color",
                subtitle = accentName,
                onClick = { showAccentDialog = true },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    )
                }
            )

            SettingsCardDivider()

            SettingsActionRow(
                icon = Icons.Rounded.FormatSize,
                title = "Typography Scale",
                subtitle = "${(state.fontScale * 100).toInt()}% multiplier",
                onClick = { showFontDialog = true }
            )

            SettingsCardDivider()

            SettingsSwitchRow(
                icon = Icons.Rounded.BlurOn,
                title = "Liquid Glass Effect",
                subtitle = "Translucent frosted surfaces (API 31+)",
                checked = state.isLiquidGlassEnabled,
                onCheckedChange = { viewModel.setLiquidGlassEnabled(it) }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // SECTION 2: Audio Engine & Timers
        SettingsSectionHeader(title = "Audio Engine & Transport")

        SettingsCard {
            SettingsActionRow(
                icon = Icons.Rounded.GraphicEq,
                title = "System Equalizer & Effects",
                subtitle = "Hardware EQ, Bass Boost, 3D Virtualizer",
                onClick = { showEqualizerSheet = true }
            )

            SettingsCardDivider()

            SettingsActionRow(
                icon = Icons.Rounded.Bedtime,
                title = "Sleep Timer",
                subtitle = "Presets & 10s smooth volume fade-out",
                onClick = { showSleepTimerDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // SECTION 3: Background Reliability & Battery
        SettingsSectionHeader(title = "Background Playback Reliability")

        SettingsCard {
            val isOpt = state.isBatteryOptimized
            SettingsActionRow(
                icon = if (isOpt) Icons.Rounded.BatteryAlert else Icons.Rounded.VerifiedUser,
                title = "Battery Optimization",
                subtitle = if (isOpt) "Restricted (OEM may kill playback)" else "Unrestricted (Rock-solid background audio)",
                onClick = { showBatteryDialog = true },
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isOpt) extendedColors.statusDanger.copy(alpha = 0.15f)
                                else extendedColors.statusSuccess.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isOpt) "Restricted" else "Unrestricted",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isOpt) extendedColors.statusDanger else extendedColors.statusSuccess
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // SECTION 4: Cloud & AI Integrations
        SettingsSectionHeader(title = "Cloud & AI Integrations")

        SettingsCard {
            SettingsActionRow(
                icon = Icons.Rounded.Key,
                title = "API Credentials",
                subtitle = "Gemini AI (Lyrics) & AudD (Song Identification)",
                onClick = { showApiKeysDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // SECTION 5: Diagnostics & About
        SettingsSectionHeader(title = "Diagnostics & Info")

        SettingsCard {
            SettingsActionRow(
                icon = Icons.Rounded.BugReport,
                title = "Diagnostic Error Logs",
                subtitle = "Inspect and export runtime SQLite logs",
                onClick = onNavigateToErrorLogs
            )

            SettingsCardDivider()

            SettingsActionRow(
                icon = Icons.Rounded.Info,
                title = "About Pulse",
                subtitle = "Version 1.0.0 • Offline content-first audio player",
                onClick = { showAboutDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // THEME CHOOSER DIALOG
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

    // ACCENT COLOR DIALOG
    if (showAccentDialog) {
        AccentChooserDialog(
            currentChoice = state.accentChoice,
            onDismiss = { showAccentDialog = false },
            onSelectChoice = {
                viewModel.setAccentChoice(it)
                showAccentDialog = false
            }
        )
    }

    // FONT SCALE DIALOG
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

    // BATTERY OPTIMIZATION DIALOG
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

    // API KEYS DIALOG
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

    // ABOUT DIALOG
    if (showAboutDialog) {
        AboutPulseDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // EQUALIZER SHEET
    if (showEqualizerSheet && audioEffectsController != null) {
        EqualizerSheet(
            audioEffectsController = audioEffectsController,
            onDismiss = { showEqualizerSheet = false }
        )
    }

    // SLEEP TIMER DIALOG
    if (showSleepTimerDialog && sleepTimerManager != null) {
        SleepTimerDialog(
            sleepTimerManager = sleepTimerManager,
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}

// -------------------------------------------------------------------------------------------------
// STYLED CONTAINER & ROW COMPONENTS ("CSS" EQUIVALENTS)
// -------------------------------------------------------------------------------------------------

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 0.75.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun SettingsCardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Squircle Container
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.onSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = extendedColors.onSurfaceSubtle,
            modifier = Modifier.size(14.dp)
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
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.onSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

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

// -------------------------------------------------------------------------------------------------
// POLISHED SUB-DIALOGS ("CSS" & LAYOUT)
// -------------------------------------------------------------------------------------------------

@Composable
private fun ThemeChooserDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelectMode: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Theme Foundation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOptionItem(
                    title = "Charcoal Dark (Default)",
                    subtitle = "Refined deep neutral charcoals",
                    dotColor = DarkBackground,
                    isSelected = currentMode == ThemeMode.DARK,
                    onClick = { onSelectMode(ThemeMode.DARK) }
                )
                ThemeOptionItem(
                    title = "Pitch Black (AMOLED)",
                    subtitle = "Pure #000000 backdrop for display battery savings",
                    dotColor = AmoledBackground,
                    isSelected = currentMode == ThemeMode.AMOLED,
                    onClick = { onSelectMode(ThemeMode.AMOLED) }
                )
                ThemeOptionItem(
                    title = "Warm Light",
                    subtitle = "Soft daylight cream tones (strictly no blue gradients)",
                    dotColor = LightBackground,
                    isSelected = currentMode == ThemeMode.LIGHT,
                    onClick = { onSelectMode(ThemeMode.LIGHT) }
                )
                ThemeOptionItem(
                    title = "Follow System",
                    subtitle = "Automatically adapts to your Android OS Dark Mode",
                    dotColor = MaterialTheme.colorScheme.primary,
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
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 1.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(dotColor)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.onSurfaceMuted
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AccentChooserDialog(
    currentChoice: AccentChoice,
    onDismiss: () -> Unit,
    onSelectChoice: (AccentChoice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Accent Color",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccentOptionItem(
                    name = "Warm Amber",
                    description = "Warm, organic, and retro-analog (#F4A261)",
                    color = AccentAmber,
                    isSelected = currentChoice == AccentChoice.WARM_AMBER,
                    onClick = { onSelectChoice(AccentChoice.WARM_AMBER) }
                )
                AccentOptionItem(
                    name = "Sunset Coral",
                    description = "Vibrant, dynamic, and expressive (#FF6B6B)",
                    color = AccentCoral,
                    isSelected = currentChoice == AccentChoice.SUNSET_CORAL,
                    onClick = { onSelectChoice(AccentChoice.SUNSET_CORAL) }
                )
                AccentOptionItem(
                    name = "Deep Emerald",
                    description = "Lush, calm, and balanced (#2EC4B6)",
                    color = AccentEmerald,
                    isSelected = currentChoice == AccentChoice.DEEP_EMERALD,
                    onClick = { onSelectChoice(AccentChoice.DEEP_EMERALD) }
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
private fun AccentOptionItem(
    name: String,
    description: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (isSelected) 1.25.dp else 0.5.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.onSurfaceMuted
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
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
    val extendedColors = LocalExtendedColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Typography Scaling",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Adjust text size proportionally across all library and player screens.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.onSurfaceMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live dynamic preview card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.75.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Bohemian Rhapsody",
                            fontSize = (18f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Queen • A Night at the Opera",
                            fontSize = (13f * scale).sp,
                            color = extendedColors.onSurfaceMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scale Factor",
                        style = MaterialTheme.typography.labelMedium,
                        color = extendedColors.onSurfaceMuted
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${(scale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.85f..1.40f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        Pair("85%", 0.85f),
                        Pair("100%", 1.0f),
                        Pair("115%", 1.15f),
                        Pair("130%", 1.30f)
                    ).forEach { (label, value) ->
                        val isPreset = (scale - value) in -0.02f..0.02f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isPreset) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { scale = value }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isPreset) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(scale) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Apply")
            }
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
        title = {
            Text(
                text = "Background Reliability",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Hero Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isOptimized) extendedColors.statusDanger.copy(alpha = 0.12f)
                            else extendedColors.statusSuccess.copy(alpha = 0.12f)
                        )
                        .border(
                            0.75.dp,
                            if (isOptimized) extendedColors.statusDanger.copy(alpha = 0.4f)
                            else extendedColors.statusSuccess.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOptimized) Icons.Rounded.BatteryAlert else Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = if (isOptimized) extendedColors.statusDanger else extendedColors.statusSuccess,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOptimized) "Battery Restricted" else "Unrestricted Background Audio",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isOptimized) extendedColors.statusDanger else extendedColors.statusSuccess
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isOptimized) "OEM process killer may stop audio after 15–30 min"
                                else "Audio will survive deep sleep & screen locks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Text(
                    text = "OEM Whitelist Instructions:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text("• Xiaomi / HyperOS: Set Battery Saver to 'No restrictions' & enable 'Autostart'", style = MaterialTheme.typography.bodySmall)
                    Text("• Samsung One UI: Battery > Remove Pulse from 'Sleeping apps'", style = MaterialTheme.typography.bodySmall)
                    Text("• OnePlus / Oppo: App Management > Pulse > Disable 'Pause activity if unused'", style = MaterialTheme.typography.bodySmall)
                    Text("• Huawei / EMUI: App Launch > Set Pulse to 'Manage manually'", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (isOptimized) {
                Button(
                    onClick = onRequestExemption,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Allow Unrestricted")
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
    val extendedColors = LocalExtendedColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cloud & AI Credentials",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🔒 Keys are stored securely in local app preferences on your device and are never uploaded to any intermediary servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Gemini AI API Key (Lyrics Generation)") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = auddKey,
                    onValueChange = { auddKey = it },
                    label = { Text("AudD API Token (Song Identification)") },
                    placeholder = { Text("Your token from audd.io") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(geminiKey, auddKey) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AboutPulseDialog(
    onDismiss: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pulse Music Player",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Version 1.0.0 (Release Build)",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.onSurfaceMuted
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pulse is an exquisite, offline, content-first audio player engineered with Jetpack Compose and AndroidX Media3.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Feature Highlights Pills
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text("• Strict Warm Palette (NO generic blue gradients)", style = MaterialTheme.typography.bodySmall)
                    Text("• Hi-Res & DSD Decimation Pipeline (up to 24-bit/88.2kHz)", style = MaterialTheme.typography.bodySmall)
                    Text("• Synchronized Precision LRC Lyrics + In-App Editor", style = MaterialTheme.typography.bodySmall)
                    Text("• Hardware EQ, Bass Boost, 3D Virtualizer & Sleep Fadeout", style = MaterialTheme.typography.bodySmall)
                    Text("• Acoustic Song Recognition Powered by AudD", style = MaterialTheme.typography.bodySmall)
                    Text("• Scoped Storage In-Place Metadata & Tag Editing", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        }
    )
}
