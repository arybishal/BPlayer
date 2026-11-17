package com.pulse.player.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pulse.player.ui.theme.LocalExtendedColors

@Composable
fun SettingsScreen(
    onNavigateToErrorLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

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

        SettingsSectionTitle(title = "Appearance & Audio")
        SettingsRow(
            icon = Icons.Rounded.ColorLens,
            title = "Theme & Colors",
            subtitle = "Dark, Light, AMOLED & warm accent palettes",
            onClick = {}
        )
        SettingsRow(
            icon = Icons.Rounded.MusicNote,
            title = "Playback Engine",
            subtitle = "Smart Previous, crossfade, gapless",
            onClick = {}
        )
        SettingsRow(
            icon = Icons.Rounded.GraphicEq,
            title = "Equalizer & Effects",
            subtitle = "Bass boost, virtualizer, loudness enhancer",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionTitle(title = "System & Diagnostics")
        SettingsRow(
            icon = Icons.Rounded.BatteryAlert,
            title = "Battery & Background Playback",
            subtitle = "Prevent OEM process termination",
            onClick = {}
        )
        SettingsRow(
            icon = Icons.Rounded.BugReport,
            title = "Diagnostic Error Logs",
            subtitle = "View, copy, and export caught exceptions",
            onClick = onNavigateToErrorLogs
        )
        SettingsRow(
            icon = Icons.Rounded.Info,
            title = "About Pulse",
            subtitle = "Version 1.0.0 (Build 1) • Open source licenses",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(32.dp))
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
private fun SettingsRow(
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
        Spacer(modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = extendedColors.onSurfaceMuted
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = extendedColors.onSurfaceSubtle,
            modifier = Modifier.size(16.dp)
        )
    }
}
