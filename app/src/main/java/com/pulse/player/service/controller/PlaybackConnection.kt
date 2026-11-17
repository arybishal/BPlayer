package com.pulse.player.service.controller

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.pulse.player.domain.model.Track
import com.pulse.player.service.PlaybackService
import com.pulse.player.service.model.PlaybackState
import com.pulse.player.service.model.RepeatState
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class PlaybackConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "PlaybackConnection"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val smartPreviousDetector = SmartPreviousDetector()

    // In-memory queue cache mapped to domain Track
    private val currentQueue = mutableListOf<Track>()

    init {
        connectToService()
        startPositionTrackingLoop()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                try {
                    mediaController = controllerFuture?.get()
                    mediaController?.let { controller ->
                        controller.addListener(PlayerListener())
                        updatePlaybackState(controller)
                        AppLogger.i(tag, "MediaController connected to PlaybackService.")
                    }
                } catch (e: Exception) {
                    AppLogger.e(tag, "Failed to connect MediaController", e)
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun startPositionTrackingLoop() {
        scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        val currentPos = controller.currentPosition.coerceAtLeast(0L)
                        val duration = controller.duration.coerceAtLeast(0L)
                        _playbackState.value = _playbackState.value.copy(
                            currentPositionMs = currentPos,
                            durationMs = if (duration > 0) duration else _playbackState.value.durationMs
                        )
                    }
                }
                delay(250)
            }
        }
    }

    fun playTrack(track: Track, newQueue: List<Track> = listOf(track)) {
        val controller = mediaController ?: return

        currentQueue.clear()
        currentQueue.addAll(newQueue)

        val targetIndex = currentQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        val mediaItems = currentQueue.map { t ->
            MediaItem.Builder()
                .setMediaId(t.id)
                .setUri(Uri.parse(t.path))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .setAlbumTitle(t.album)
                        .setArtworkUri(t.artworkUri?.let(Uri::parse))
                        .build()
                )
                .build()
        }

        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()
        controller.play()

        updatePlaybackState(controller)
    }

    fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE) {
                controller.prepare()
            }
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs.coerceAtLeast(0L))
        _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun smartPrevious() {
        val controller = mediaController ?: return
        val currentPosition = controller.currentPosition
        val action = smartPreviousDetector.onPreviousTapped(currentPosition)

        when (action) {
            PreviousAction.RESTART_TRACK -> {
                controller.seekTo(0L)
                AppLogger.d(tag, "Smart Previous: Restarted track from 0:00.")
            }
            PreviousAction.SKIP_TO_PREVIOUS -> {
                if (controller.hasPreviousMediaItem()) {
                    controller.seekToPreviousMediaItem()
                    AppLogger.d(tag, "Smart Previous: Skipped to previous track.")
                } else {
                    controller.seekTo(0L)
                }
            }
        }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffle = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newShuffle
        _playbackState.value = _playbackState.value.copy(isShuffleEnabled = newShuffle)
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode

        val stateEnum = when (nextMode) {
            Player.REPEAT_MODE_ALL -> RepeatState.ALL
            Player.REPEAT_MODE_ONE -> RepeatState.ONE
            else -> RepeatState.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatState = stateEnum)
    }

    fun setPreviousBehavior(behavior: PreviousBehavior) {
        smartPreviousDetector.behavior = behavior
    }

    fun addToQueue(track: Track) {
        currentQueue.add(track)
        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(Uri.parse(track.path))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.artworkUri?.let(Uri::parse))
                    .build()
            )
            .build()
        mediaController?.addMediaItem(mediaItem)
        _playbackState.value = _playbackState.value.copy(queue = currentQueue.toList())
    }

    fun playNext(track: Track) {
        val controller = mediaController ?: return
        val nextIndex = (controller.currentMediaItemIndex + 1).coerceAtMost(currentQueue.size)
        currentQueue.add(nextIndex, track)

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(Uri.parse(track.path))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.artworkUri?.let(Uri::parse))
                    .build()
            )
            .build()
        controller.addMediaItem(nextIndex, mediaItem)
        _playbackState.value = _playbackState.value.copy(queue = currentQueue.toList())
    }

    private fun updatePlaybackState(controller: Player) {
        val currentIndex = controller.currentMediaItemIndex
        val currentTrack = currentQueue.getOrNull(currentIndex)
        val duration = controller.duration.coerceAtLeast(0L)
        val position = controller.currentPosition.coerceAtLeast(0L)

        val repeatEnum = when (controller.repeatMode) {
            Player.REPEAT_MODE_ALL -> RepeatState.ALL
            Player.REPEAT_MODE_ONE -> RepeatState.ONE
            else -> RepeatState.OFF
        }

        _playbackState.value = PlaybackState(
            currentTrack = currentTrack,
            isPlaying = controller.isPlaying,
            currentPositionMs = position,
            durationMs = if (duration > 0) duration else (currentTrack?.durationMs ?: 0L),
            isShuffleEnabled = controller.shuffleModeEnabled,
            repeatState = repeatEnum,
            queue = currentQueue.toList(),
            currentQueueIndex = currentIndex
        )
    }

    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            mediaController?.let(::updatePlaybackState)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaController?.let(::updatePlaybackState)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            mediaController?.let(::updatePlaybackState)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playbackState.value = _playbackState.value.copy(isShuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            val repeatEnum = when (repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatState.ALL
                Player.REPEAT_MODE_ONE -> RepeatState.ONE
                else -> RepeatState.OFF
            }
            _playbackState.value = _playbackState.value.copy(repeatState = repeatEnum)
        }
    }
}
