package com.pulse.player.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pulse.player.ui.theme.LocalExtendedColors
import kotlin.math.roundToInt

@Composable
fun HeroSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = LocalExtendedColors.current
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val actualProgress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val displayProgress = if (isDragging) dragProgress else actualProgress

    val thumbDiameter by animateDpAsState(
        targetValue = if (isDragging) 22.dp else 14.dp,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "thumbExpansion"
    )

    val elapsedSeconds = if (isDragging) {
        ((dragProgress * durationMs) / 1000).toLong()
    } else {
        (currentPositionMs / 1000).coerceAtLeast(0)
    }
    val remainingSeconds = ((durationMs - (elapsedSeconds * 1000)) / 1000).coerceAtLeast(0)

    val elapsedFormatted = "%d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)
    val remainingFormatted = "-%d:%02d".format(remainingSeconds / 60, remainingSeconds % 60)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        val targetMs = (newProgress * durationMs).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val targetMs = (dragProgress * durationMs).toLong()
                            onSeek(targetMs)
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            dragProgress = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Inactive Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(extendedColors.surfaceHighlight)
            )

            // Active Played Track
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayProgress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Draggable Thumb
            val thumbOffsetPx = (displayProgress * trackWidthPx).roundToInt()
            Box(
                modifier = Modifier
                    .offset { IntOffset(x = thumbOffsetPx - (thumbDiameter.toPx() / 2).roundToInt(), y = 0) }
                    .size(thumbDiameter)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground)
            )
        }

        // Timestamps Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = elapsedFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.onSurfaceMuted
            )
            Text(
                text = remainingFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.onSurfaceMuted
            )
        }
    }
}
