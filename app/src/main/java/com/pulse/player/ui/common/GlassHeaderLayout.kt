package com.pulse.player.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulse.player.ui.theme.LayerBackdrop
import com.pulse.player.ui.theme.isLiquidGlassBackdropSupported
import com.pulse.player.ui.theme.liquidGlassSource
import com.pulse.player.ui.theme.rememberLayerBackdrop

/**
 * Puts [header] (an [ExpressiveHeader]) over [content] so content scrolls
 * under the glass.
 *
 * Content is the backdrop source and the header a sibling, never inside it.
 */
@Composable
fun GlassHeaderLayout(
    modifier: Modifier = Modifier,
    header: @Composable (modifier: Modifier, backdrop: LayerBackdrop?) -> Unit,
    content: @Composable (topInset: Dp) -> Unit,
) {
    val background = MaterialTheme.colorScheme.background
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val wash = headerGlassWash()
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawRect(wash, size = Size(size.width, headerHeightPx.toFloat()))
        drawContent()
    }
    val glass = isLiquidGlassBackdropSupported() && backdrop != null
    val headerHeight = with(LocalDensity.current) { headerHeightPx.toDp() }
    Box(modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .liquidGlassSource(if (glass) backdrop else null)
                .padding(top = if (glass) 0.dp else headerHeight),
        ) {
            content(if (glass) headerHeight else 0.dp)
        }
        header(
            Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { if (it.height != headerHeightPx) headerHeightPx = it.height },
            if (glass) backdrop else null,
        )
    }
}
