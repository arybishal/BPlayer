package com.pulse.player.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulse.player.ui.common.ExpressiveMotion
import com.pulse.player.ui.common.expressivePressScale
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.LocalIsDarkTheme
import com.pulse.player.ui.theme.LocalLiquidGlass
import com.pulse.player.ui.theme.SquircleShape
import com.pulse.player.ui.theme.liquidGlass

private val DockShape = RoundedCornerShape(32.dp)

/**
 * BPlayer Floating Liquid Glass Capsule Navigation Dock.
 * Features:
 * - Floating glass capsule floating gracefully above the system navigation bar
 * - Subtle 1dp border and tactile spring press feedback
 * - Warm organic accent indicator pill for active tab
 * - Accessible >= 48dp touch targets
 */
@Composable
fun BPlayerBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    val isDark = LocalIsDarkTheme.current
    val haptic = LocalHapticFeedback.current
    val isGlass = LocalLiquidGlass.current

    val containerBg = if (isDark) {
        Color(0xFF16161A).copy(alpha = if (isGlass) 0.78f else 0.94f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = if (isGlass) 0.82f else 0.96f)
    }

    val borderStroke = BorderStroke(
        width = 1.dp,
        color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = DockShape,
            color = containerBg,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            border = borderStroke,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .liquidGlass(
                    shape = DockShape,
                    isGlassEnabled = extendedColors.isGlassEnabled,
                    backgroundColor = containerBg,
                    fallbackBackgroundColor = containerBg
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (screen in Screen.bottomNavItems) {
                    val selected = currentRoute == screen.route
                    val interactionSource = remember { MutableInteractionSource() }

                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            if (isDark) Color(0xFFA0A0AA) else Color(0xFF6B6B76)
                        },
                        animationSpec = ExpressiveMotion.effectsFast(),
                        label = "navTextColor"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.18f else 0.14f)
                        } else {
                            Color.Transparent
                        },
                        animationSpec = ExpressiveMotion.effectsDefault(),
                        label = "navPillBg"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(pillBgColor)
                            .expressivePressScale(interactionSource, targetScale = 0.92f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (!selected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigateToRoute(screen.route)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            screen.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.title,
                                    tint = contentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
