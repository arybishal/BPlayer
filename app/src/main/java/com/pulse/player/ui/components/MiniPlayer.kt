package com.pulse.player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.player.service.model.PlaybackState
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.liquidGlass

@Composable
fun MiniPlayer(
    playbackState: PlaybackState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playbackState.currentTrack ?: return
    val extendedColors = LocalExtendedColors.current
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(18.dp),
                    isGlassEnabled = extendedColors.isGlassEnabled,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    fallbackBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { onExpand() }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragAccumulator > 80f) {
                                onPrevious() // Swiped right -> Smart Previous
                            } else if (dragAccumulator < -80f) {
                                onNext()     // Swiped left -> Skip Next
                            }
                            dragAccumulator = 0f
                        },
                        onDragCancel = { dragAccumulator = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragAccumulator += dragAmount
                        }
                    )
                }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork Thumbnail
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!track.artworkUri.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(track.artworkUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(46.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = extendedColors.onSurfaceMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.onSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    // Play/Pause Morph Button
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Next Button
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Track",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Tiny Progress Line at Bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth(playbackState.progress)
                        .height(2.5.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
