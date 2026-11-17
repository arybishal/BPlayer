package com.pulse.player.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.liquidGlass

@Composable
fun PulseBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .liquidGlass(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                isGlassEnabled = extendedColors.isGlassEnabled,
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                fallbackBackgroundColor = MaterialTheme.colorScheme.surface
            )
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (screen in Screen.bottomNavItems) {
                val selected = currentRoute == screen.route
                val contentColor = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    extendedColors.onSurfaceMuted
                }

                Column(
                    modifier = Modifier
                        .size(width = 68.dp, height = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!selected) {
                                onNavigateToRoute(screen.route)
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    screen.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = screen.title,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = screen.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )
                }
            }
        }
    }
}
