package com.lastwave.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * BPlayer Canonical Glass Design Tokens.
 * Single source of truth for all translucent liquid glass components,
 * satisfying Rule 3 of the BPlayer Design Constitution.
 */
object GlassTokens {
    // ── Alpha & Tonal Fills ──
    const val DarkContainerAlpha = 0.68f
    const val LightContainerAlpha = 0.78f
    const val RestSurfaceAlpha = 0.14f
    const val ActiveSurfaceAlpha = 0.86f
    const val ScrimAlpha = 0.32f

    val DarkGlassColor = Color(0xFF101318).copy(alpha = DarkContainerAlpha)
    val LightGlassColor = Color(0xFFFFFFFF).copy(alpha = LightContainerAlpha)
    val DarkBaseBackground = Color(0xFF0C0D10)
    val PitchBlackBackground = Color(0xFF000000)
    val LightBaseBackground = Color(0xFFF8F9FA)

    // ── Blur Densities ──
    val BaseBlurDp: Dp = 12.dp
    val BrightBlurDp: Dp = 20.dp
    val DarkBlurDp: Dp = 6.dp
    val HeaderBlurDp: Dp = 24.dp
    val AmbientBlurDp: Dp = 36.dp
    val LyricsAmbientBlurDp: Dp = 46.dp

    // ── Corner Radii ──
    val RadiusDialog: Dp = 28.dp
    val RadiusBottomSheet: Dp = 28.dp
    val RadiusCard: Dp = 24.dp
    val RadiusHeader: Dp = 24.dp
    val RadiusGroup: Dp = 22.dp
    val RadiusTrackRow: Dp = 16.dp
    val RadiusTag: Dp = 8.dp

    val ShapeDialog = RoundedCornerShape(RadiusDialog)
    val ShapeBottomSheet = RoundedCornerShape(topStart = RadiusBottomSheet, topEnd = RadiusBottomSheet)
    val ShapeCard = RoundedCornerShape(RadiusCard)
    val ShapeHeader = RoundedCornerShape(bottomStart = RadiusHeader, bottomEnd = RadiusHeader)
    val ShapeGroup = RoundedCornerShape(RadiusGroup)
    val ShapeTrackRow = RoundedCornerShape(RadiusTrackRow)
    val ShapePill = CircleShape

    // ── Specular Borders & Sheen ──
    val BorderWidth: Dp = 1.dp

    fun borderBrush(isDark: Boolean): Brush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.22f else 0.42f),
            Color.White.copy(alpha = if (isDark) 0.06f else 0.12f),
        )
    )

    fun topSheenBrush(isDark: Boolean): Brush = Brush.verticalGradient(
        0f to Color.White.copy(alpha = if (isDark) 0.12f else 0.24f),
        0.45f to Color.Transparent,
    )

    // ── Touch Targets & Sizing ──
    val MinTouchTarget: Dp = 48.dp
    val HeroPlayPauseSize: Dp = 76.dp
    val StandardIconSize: Dp = 24.dp
    val SmallIconSize: Dp = 18.dp
    val NavDockHeight: Dp = 64.dp
    val CompactNavDockHeight: Dp = 60.dp

    // ── Shadows ──
    val FloatingElevation: Dp = 8.dp
    val CardElevation: Dp = 0.dp // Prevent dark double-toning over translucent surfaces
    val AmbientShadowColor = Color.Black.copy(alpha = 0.10f)
    val SpotShadowColor = Color.Black.copy(alpha = 0.14f)
}
