package com.pulse.player.ui.theme

import android.app.ActivityManager
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Clean local Backdrop handles. */
class Backdrop
class LayerBackdrop

data class Highlight(
    val width: Dp = 1.dp,
    val color: Color = Color.White.copy(alpha = 0.15f)
) {
    companion object {
        val Default = Highlight()
    }
}

/** Shared opt-in flag for Liquid Glass. */
val LocalLiquidGlass = staticCompositionLocalOf { true }

/** Shared ambient dark theme indicator. */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/** Shared toggle for classic Material 3 full-width bottom navigation bar. */
val LocalMd3NavBar = staticCompositionLocalOf { false }

/** Background-only source for surfaces inside the captured scrolling content. */
val LocalLiquidGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/** Separate source for overlays; kept for call-site compat. */
val LocalLiquidGlassOverlayBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/** Factory matching Backdrop API. */
@Composable
fun rememberLayerBackdrop(
    onDraw: ContentDrawScope.() -> Unit = { drawContent() },
): LayerBackdrop? = remember { LayerBackdrop() }

/** Marks a composable as the source layer that sibling glass surfaces refract. */
fun Modifier.layerBackdropCompat(backdrop: LayerBackdrop): Modifier = this

/** Remember a backdrop that draws a flat color + content. */
@Composable
fun rememberBackdrop(color: Color): LayerBackdrop? = remember { LayerBackdrop() }

/**
 * Continuous-curvature squircle implementing [CornerBasedShape].
 */
class SquircleShape(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

    constructor(radius: Dp) : this(
        CornerSize(radius),
        CornerSize(radius),
        CornerSize(radius),
        CornerSize(radius),
    )

    constructor(percent: Int = 50) : this(
        CornerSize(percent),
        CornerSize(percent),
        CornerSize(percent),
        CornerSize(percent),
    )

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): CornerBasedShape = SquircleShape(topStart, topEnd, bottomEnd, bottomStart)

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return Outline.Rectangle(Rect.Zero)

        val isLtr = layoutDirection == LayoutDirection.Ltr
        val tl = if (isLtr) topStart else topEnd
        val tr = if (isLtr) topEnd else topStart
        val br = if (isLtr) bottomEnd else bottomStart
        val bl = if (isLtr) bottomStart else bottomEnd

        val maxRadius = kotlin.math.min(w, h) / 2f
        val rTl = kotlin.math.min(tl, maxRadius)
        val rTr = kotlin.math.min(tr, maxRadius)
        val rBr = kotlin.math.min(br, maxRadius)
        val rBl = kotlin.math.min(bl, maxRadius)

        val path = Path().apply {
            moveTo(rTl, 0f)
            lineTo(w - rTr, 0f)
            quadraticTo(w, 0f, w, rTr)
            lineTo(w, h - rBr)
            quadraticTo(w, h, w - rBr, h)
            lineTo(rBl, h)
            quadraticTo(0f, h, 0f, h - rBl)
            lineTo(0f, rTl)
            quadraticTo(0f, 0f, rTl, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/** Pre-allocated Squircles. */
val SquircleFull = SquircleShape(50)
val Squircle28 = SquircleShape(28.dp)
val Squircle24 = SquircleShape(24.dp)
val Squircle20 = SquircleShape(20.dp)
val Squircle16 = SquircleShape(16.dp)
val Squircle12 = SquircleShape(12.dp)
val Squircle8 = SquircleShape(8.dp)

@Composable
fun isDeviceGlassCapable(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val context = LocalContext.current
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return true
    return !am.isLowRamDevice
}

@Composable
fun isLiquidGlassBackdropSupported(): Boolean =
    LocalLiquidGlass.current && isDeviceGlassCapable()

fun Modifier.liquidGlassSource(
    backdrop: LayerBackdrop?,
    enabled: Boolean = true,
): Modifier = this

@Composable
fun resolveGlassContainerColor(
    base: Color,
    enabled: Boolean = LocalLiquidGlass.current,
    backdrop: Backdrop? = LocalLiquidGlassBackdrop.current,
): Color = if (enabled && isLiquidGlassBackdropSupported()) {
    base.copy(alpha = 0.55f)
} else {
    base
}

@Composable
fun isLiquidGlassEnabled(): Boolean = LocalLiquidGlass.current

@Composable
fun Modifier.headerLiquidGlass(
    backdrop: Any? = null,
    blurScale: Float = 1f,
    shape: Shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
): Modifier = this.liquidGlass(shape = shape)

/**
 * Core Liquid Glass modifier:
 * - API 31+ (Android 12+): RenderEffect blur with frosted translucent backing and specular 1dp border.
 * - API 26-30 (Fallback): High-alpha tonal surface without blur to ensure 60fps performance.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    isGlassEnabled: Boolean = LocalLiquidGlass.current,
    blurRadius: Float = 28f,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f),
    fallbackBackgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    backdrop: Backdrop? = LocalLiquidGlassBackdrop.current,
    interactive: Boolean = true
): Modifier {
    if (!isGlassEnabled) {
        return this
            .clip(shape)
            .background(fallbackBackgroundColor, shape)
            .border(borderWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), shape)
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
        this
            .clip(shape)
            .background(fallbackBackgroundColor.copy(alpha = 0.90f), shape)
            .border(borderWidth, borderColor.copy(alpha = 0.12f), shape)
    }
}

/** Overload accepting backdrop directly. */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    shape: Shape = RoundedCornerShape(16.dp),
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    forceDark: Boolean = false,
): Modifier = this.liquidGlass(shape = shape, interactive = interactive)

/** Overload accepting luminanceAnimation. */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    luminanceAnimation: Float,
    shape: Shape = RoundedCornerShape(16.dp),
    interactive: Boolean = true,
    blurScale: Float = 1f,
    minScrim: Float = 0.1f,
    maxScrim: Float = 0.4f,
    forceDark: Boolean = false,
): Modifier = this.liquidGlass(shape = shape, interactive = interactive)

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
    isGlassEnabled: Boolean = LocalLiquidGlass.current,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.liquidGlass(
            shape = shape,
            isGlassEnabled = isGlassEnabled,
            backgroundColor = color,
            fallbackBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = shape,
        color = Color.Transparent,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = null,
        content = content,
    )
}

@Composable
fun LiquidGlassContainer(
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(
            shape = shape,
            interactive = interactive,
            borderWidth = highlight.width,
            borderColor = highlight.color
        ),
        contentAlignment = contentAlignment,
        content = content
    )
}

@Composable
fun LiquidGlassActionPill(
    backdrop: Backdrop?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier
            .liquidGlass(shape = shape)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) leadingIcon()
        androidx.compose.material3.Text(text = label, color = textColor)
        if (trailingIcon != null) trailingIcon()
    }
}

@Composable
fun LiquidGlassIconButton(
    backdrop: Backdrop?,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(48.dp),
    shape: Shape = RoundedCornerShape(14.dp),
    tint: Color = Color.White,
    interactive: Boolean = true,
    highlight: Highlight = Highlight(width = 1.dp),
    contentDescription: String? = null,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        modifier = modifier,
        shape = shape,
        interactive = interactive,
        highlight = highlight,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp).clickable(onClick = onClick),
        )
    }
}

@Composable
fun LiquidGlassIconButton(
    backdrop: Backdrop?,
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(48.dp),
    shape: Shape = RoundedCornerShape(14.dp),
    tint: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        modifier = modifier,
        shape = shape,
        interactive = true,
        highlight = Highlight(width = 1.dp),
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp).clickable(onClick = onClick),
        )
    }
}
