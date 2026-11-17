package com.pulse.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.player.ui.theme.LocalExtendedColors

@Composable
fun AudioFormatBadge(
    badgeText: String,
    modifier: Modifier = Modifier,
    isHiRes: Boolean = false,
    isDsd: Boolean = false,
    isCompact: Boolean = false
) {
    val extendedColors = LocalExtendedColors.current

    val (badgeBg, badgeBorder, textColor) = when {
        isDsd -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.primary
        )
        isHiRes -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.primary
        )
        badgeText in listOf("FLAC", "ALAC", "WAV", "LOSSLESS", "APE", "WV") -> Triple(
            extendedColors.statusSuccess.copy(alpha = 0.12f),
            extendedColors.statusSuccess.copy(alpha = 0.3f),
            extendedColors.statusSuccess
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            Color.Transparent,
            extendedColors.onSurfaceMuted
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (isCompact) 4.dp else 6.dp))
            .background(badgeBg)
            .border(
                width = 0.75.dp,
                color = badgeBorder,
                shape = RoundedCornerShape(if (isCompact) 4.dp else 6.dp)
            )
            .padding(
                horizontal = if (isCompact) 5.dp else 7.dp,
                vertical = if (isCompact) 1.dp else 2.5.dp
            )
    ) {
        Text(
            text = badgeText,
            style = if (isCompact) {
                MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            },
            color = textColor
        )
    }
}
