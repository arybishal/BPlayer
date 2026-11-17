package com.pulse.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createPulseTypography(scaleMultiplier: Float = 1.0f): Typography {
    val m = scaleMultiplier.coerceIn(0.85f, 1.40f)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * m).sp,
            lineHeight = (38 * m).sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * m).sp,
            lineHeight = (34 * m).sp,
            letterSpacing = (-0.5).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * m).sp,
            lineHeight = (26 * m).sp,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * m).sp,
            lineHeight = (24 * m).sp,
            letterSpacing = 0.sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * m).sp,
            lineHeight = (22 * m).sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * m).sp,
            lineHeight = (22 * m).sp,
            letterSpacing = 0.1.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * m).sp,
            lineHeight = (20 * m).sp,
            letterSpacing = 0.1.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13 * m).sp,
            lineHeight = (18 * m).sp,
            letterSpacing = 0.3.sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (12 * m).sp,
            lineHeight = (16 * m).sp,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * m).sp,
            lineHeight = (14 * m).sp,
            letterSpacing = 0.5.sp
        )
    )
}
