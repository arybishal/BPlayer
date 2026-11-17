package com.pulse.player.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pulse.player.service.controller.PlaybackConnection
import com.pulse.player.service.model.RepeatState
import com.pulse.player.ui.components.HeroSeekBar
import com.pulse.player.ui.theme.HeroAlbumArtShape
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.StatusDanger

@Composable
fun NowPlayingScreen(
    playbackConnection: PlaybackConnection,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by playbackConnection.playbackState.collectAsState()
    val track = state.currentTrack ?: return
    val extendedColors = LocalExtendedColors.current

    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        onCollapse() // Swipe down dismisses
                    }
                }
            }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Row: Collapse, Source Label, More
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Collapse Player",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PLAYING FROM",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.onSurfaceSubtle
                )
                Text(
                    text = track.album,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "Options",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3-Page Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> {
                    // Page 1: Hero Player Page
                    PlayerHeroView(
                        track = track,
                        isPlaying = state.isPlaying
                    )
                }
                1 -> {
                    // Page 2: Lyrics View
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Synchronized Lyrics\n(Swipe back for Player)",
                            style = MaterialTheme.typography.titleMedium,
                            color = extendedColors.onSurfaceMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                2 -> {
                    // Page 3: Full Art Viewer
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Full Resolution Album Art\n(Swipe back for Player)",
                            style = MaterialTheme.typography.titleMedium,
                            color = extendedColors.onSurfaceMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hero Seek Bar
        HeroSeekBar(
            currentPositionMs = state.currentPositionMs,
            durationMs = state.durationMs,
            onSeek = { playbackConnection.seekTo(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Transport Controls Pod (Pure Vector Icons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button
            IconButton(
                onClick = { playbackConnection.toggleShuffle() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Toggle Shuffle",
                    tint = if (state.isShuffleEnabled) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Smart Previous Button
            IconButton(
                onClick = { playbackConnection.smartPrevious() },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous Track (Smart Previous)",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Hero Play/Pause Button (76dp)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { playbackConnection.playPause() },
                    modifier = Modifier.size(76.dp)
                ) {
                    AnimatedContent(
                        targetState = state.isPlaying,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "playPauseHero"
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            // Next Button
            IconButton(
                onClick = { playbackConnection.skipToNext() },
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Next Track",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat Button
            IconButton(
                onClick = { playbackConnection.toggleRepeat() },
                modifier = Modifier.size(48.dp)
            ) {
                val icon = when (state.repeatState) {
                    RepeatState.ONE -> Icons.Rounded.RepeatOne
                    else -> Icons.Rounded.Repeat
                }
                val tint = if (state.repeatState != RepeatState.OFF) {
                    MaterialTheme.colorScheme.primary
                } else {
                    extendedColors.onSurfaceMuted
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle Repeat",
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Auxiliary Actions Row: Favorite, Sleep Timer, Queue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) StatusDanger else extendedColors.onSurfaceMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = "Sleep Timer",
                    tint = extendedColors.onSurfaceMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                    contentDescription = "Up Next Queue",
                    tint = extendedColors.onSurfaceMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pager Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else extendedColors.onSurfaceSubtle
                        )
                )
            }
        }
    }
}

@Composable
private fun PlayerHeroView(
    track: com.pulse.player.domain.model.Track,
    isPlaying: Boolean
) {
    val extendedColors = LocalExtendedColors.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Artwork (280x280dp)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(HeroAlbumArtShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!track.artworkUri.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(track.artworkUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album Artwork",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = extendedColors.onSurfaceMuted,
                    modifier = Modifier.size(72.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Song Title
        Text(
            text = track.title,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Artist & Album
        Text(
            text = "${track.artist} • ${track.album}",
            style = MaterialTheme.typography.titleMedium,
            color = extendedColors.onSurfaceMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
    }
}
