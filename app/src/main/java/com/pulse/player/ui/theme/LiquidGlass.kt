package com.pulse.player.ui.theme

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies a Liquid Glass frosted translucency effect with:
 * - API 31+ (Android 12+): Real-time RenderEffect blur with translucent backing and specular border.
 * - API 26-30 (Fallback): High-opacity tonal surface without blur to ensure 60fps performance.
 */
fun Modifier.liquidGlass(
    shape: Shape = GlassPodShape,
    isGlassEnabled: Boolean = true,
    blurRadius: Float = 28f,
    borderWidth: Dp = 1.dp,
    borderColor: Color = GlassBorderWhite,
    backgroundColor: Color = GlassSurfaceDark,
    fallbackBackgroundColor: Color = DarkSurfaceElevated.copy(alpha = 0.94f)
): Modifier {
    if (!isGlassEnabled) {
        return this
            .clip(shape)
            .background(fallbackBackgroundColor, shape)
            .border(borderWidth, DarkOutline, shape)
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this
            .clip(shape)
            .graphicsLayer {
                renderEffect = RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.CLAMP
                ).asComposeRenderEffect()
            }
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
    } else {
        // Graceful legacy fallback
        this
            .clip(shape)
            .background(fallbackBackgroundColor, shape)
            .border(borderWidth, borderColor.copy(alpha = 0.10f), shape)
    }
}
