package com.lastwave.app.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lastwave.app.ui.theme.Backdrop
import com.lastwave.app.ui.theme.GlassTokens
import com.lastwave.app.ui.theme.LiquidGlassPreset
import com.lastwave.app.ui.theme.LocalIsDarkTheme
import com.lastwave.app.ui.theme.LocalLiquidGlass
import com.lastwave.app.ui.theme.LocalLiquidGlassBackdrop
import com.lastwave.app.ui.theme.iosTranslucentGlass
import com.lastwave.app.ui.theme.liquidGlassChrome
import com.lastwave.app.ui.theme.liquidGlassContainerColor

/**
 * GlassSurface: Canonical translucent surface with genuine backdrop refraction
 * on supported hardware, and elegant frosted translucent fallback on API 26-30.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.ShapeCard,
    enabled: Boolean = LocalLiquidGlass.current,
    preset: LiquidGlassPreset = LiquidGlassPreset.Card,
    backdrop: Backdrop? = LocalLiquidGlassBackdrop.current,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    val resolvedBase = if (color.isSpecified) color else MaterialTheme.colorScheme.surfaceContainer
    val containerColor = liquidGlassContainerColor(
        color = resolvedBase,
        enabled = enabled,
        backdrop = backdrop,
    )
    val resolvedContentColor = if (contentColor.isSpecified) contentColor else contentColorFor(resolvedBase)

    val clickModifier = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier
            .liquidGlassChrome(
                shape = shape,
                enabled = enabled,
                preset = preset,
                backdrop = backdrop,
            )
            .then(clickModifier),
        shape = shape,
        color = containerColor,
        contentColor = resolvedContentColor,
        tonalElevation = GlassTokens.CardElevation,
        shadowElevation = GlassTokens.CardElevation,
    ) {
        Box(contentAlignment = Alignment.Center, content = content)
    }
}

/**
 * GlassCard: Standard translucent card replacing opaque Material 3 Card across the app.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.ShapeCard,
    enabled: Boolean = LocalLiquidGlass.current,
    tintColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val resolvedContentColor = if (contentColor.isSpecified) {
        contentColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val baseColor = if (tintColor.isSpecified) tintColor else MaterialTheme.colorScheme.surfaceContainer
    val containerColor = liquidGlassContainerColor(
        color = baseColor,
        enabled = enabled,
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            role = Role.Button,
            onClick = onClick,
        )
    } else Modifier

    Card(
        modifier = modifier
            .liquidGlassChrome(
                shape = shape,
                enabled = enabled,
                preset = LiquidGlassPreset.Card,
            )
            .then(clickModifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = resolvedContentColor,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = GlassTokens.CardElevation),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/**
 * GlassButton: Interactive pill button using liquid glass styling and tactile press scaling.
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.ShapePill,
    enabled: Boolean = true,
    isPrimary: Boolean = false,
    contentPadding: Dp = 14.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = "glassButtonScale",
    )

    val isDark = LocalIsDarkTheme.current
    val baseColor = if (isPrimary) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = GlassTokens.ActiveSurfaceAlpha)
    } else {
        if (isDark) Color(0xFF101318).copy(alpha = 0.68f) else Color.White.copy(alpha = 0.78f)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = baseColor,
        contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        interactionSource = interactionSource,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .liquidGlassChrome(
                shape = shape,
                enabled = LocalLiquidGlass.current,
                preset = LiquidGlassPreset.FloatingControls,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * GlassIconButton: Circular translucent icon button satisfying the 48x48dp touch target rule.
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = GlassTokens.MinTouchTarget,
    iconSize: Dp = GlassTokens.StandardIconSize,
    tint: Color = Color.Unspecified,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        label = "glassIconPress",
    )
    val isDark = LocalIsDarkTheme.current
    val resolvedTint = if (tint.isSpecified) tint else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = GlassTokens.ShapePill,
        color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f),
        contentColor = resolvedTint,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .liquidGlassChrome(
                shape = GlassTokens.ShapePill,
                enabled = LocalLiquidGlass.current,
                preset = LiquidGlassPreset.PlayerControls,
            ),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = resolvedTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * GlassDialog: Modal dialog styled with translucent frosted glass and 1dp specular border.
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = GlassTokens.ShapeDialog,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        val isDark = LocalIsDarkTheme.current
        Surface(
            shape = shape,
            color = if (isDark) Color(0xFF101318).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlassChrome(
                    shape = shape,
                    enabled = LocalLiquidGlass.current,
                    preset = LiquidGlassPreset.ModalSheet,
                ),
        ) {
            Column(modifier = Modifier.padding(24.dp), content = content)
        }
    }
}

/**
 * GlassBottomSheet: Modal bottom sheet with transparent outer container and frosted content area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = GlassTokens.ShapeBottomSheet,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        containerColor = Color.Transparent,
        scrimColor = Color.Black.copy(alpha = GlassTokens.ScrimAlpha),
        dragHandle = {
            Surface(
                shape = GlassTokens.ShapePill,
                color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.40f),
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp),
            ) {}
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .iosTranslucentGlass(shape = shape, isDark = isDark),
            content = content,
        )
    }
}

/**
 * GlassPanel: Standalone translucent panel for queue, lyrics, equalizer, or details.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.ShapeCard,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        shape = shape,
        color = if (isDark) Color(0xFF101318).copy(alpha = 0.72f) else Color.White.copy(alpha = 0.82f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier.liquidGlassChrome(
            shape = shape,
            enabled = LocalLiquidGlass.current,
            preset = LiquidGlassPreset.Card,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
