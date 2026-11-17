package com.pulse.player.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.pulse.player.data.local.db.dao.QueueDao
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.data.local.db.entity.TrackEntity
import com.pulse.player.data.scrobbler.ScrobbleRepository
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.sleeptimer.SleepTimerManager
import com.pulse.player.ui.MainActivity
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {

    private val tag = "PlaybackService"
    private var mediaSession: MediaLibrarySession? = null
    private lateinit var exoPlayer: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var trackDao: TrackDao

    @Inject
    lateinit var queueDao: QueueDao

    @Inject
    lateinit var audioEffectsController: AudioEffectsController

    @Inject
    lateinit var sleepTimerManager: SleepTimerManager

    @Inject
    lateinit var scrobbleRepository: ScrobbleRepository

    @Volatile
    private var hasScrobbledCurrentTrack = false

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        audioEffectsController.attachToSession(exoPlayer.audioSessionId)
        sleepTimerManager.initialize(
            onPause = { exoPlayer.pause() },
            onVolume = { vol -> exoPlayer.volume = vol }
        )
        startScrobbleTrackingLoop()
        AppLogger.i(tag, "PlaybackService created and ExoPlayer initialized.")
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val renderersFactory = DefaultRenderersFactory(this).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableAudioTrackPlaybackParams(true)
        }

        exoPlayer = ExoPlayer.Builder(this, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> AppLogger.d(tag, "ExoPlayer ready. isPlaying=${exoPlayer.isPlaying}")
                    Player.STATE_ENDED -> AppLogger.d(tag, "ExoPlayer track ended.")
                    Player.STATE_BUFFERING -> AppLogger.d(tag, "ExoPlayer buffering...")
                    Player.STATE_IDLE -> AppLogger.d(tag, "ExoPlayer idle.")
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                AppLogger.e(tag, "ExoPlayer playback error: ${error.errorCodeName}", error)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                hasScrobbledCurrentTrack = false
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    sleepTimerManager.onTrackEnded()
                }

                val title = mediaItem?.mediaMetadata?.title?.toString()
                val artist = mediaItem?.mediaMetadata?.artist?.toString()
                val album = mediaItem?.mediaMetadata?.albumTitle?.toString()
                if (!title.isNullOrBlank() && !artist.isNullOrBlank()) {
                    serviceScope.launch {
                        scrobbleRepository.updateNowPlaying(artist, title, album)
                    }
                }
            }
        })
    }

    private fun startScrobbleTrackingLoop() {
        serviceScope.launch {
            while (isActive) {
                delay(1000)
                if (exoPlayer.isPlaying && !hasScrobbledCurrentTrack) {
                    val dur = exoPlayer.duration
                    val pos = exoPlayer.currentPosition
                    val currentItem = exoPlayer.currentMediaItem
                    val artist = currentItem?.mediaMetadata?.artist?.toString()
                    val title = currentItem?.mediaMetadata?.title?.toString()
                    val album = currentItem?.mediaMetadata?.albumTitle?.toString()

                    if (!artist.isNullOrBlank() && !title.isNullOrBlank() && dur >= 30_000L) {
                        val threshold = minOf(dur / 2, 240_000L)
                        if (pos >= threshold) {
                            hasScrobbledCurrentTrack = true
                            scrobbleRepository.scrobble(artist, title, album)
                        }
                    }
                }
            }
        }
    }

    private fun initializeMediaSession() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        mediaSession = MediaLibrarySession.Builder(this, exoPlayer, LibrarySessionCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon().build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .setAvailablePlayerCommands(playerCommands)
                .build()
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId("ROOT")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("BPlayer Library")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val items: List<MediaItem> = when (parentId) {
                        "ROOT" -> listOf(
                            createFolderItem("TRACKS", "All Tracks", MediaMetadata.FOLDER_TYPE_MIXED),
                            createFolderItem("FAVORITES", "Favorites", MediaMetadata.FOLDER_TYPE_MIXED)
                        )
                        "TRACKS" -> {
                            trackDao.getAllTracksSync().map { it.toMediaItem() }
                        }
                        "FAVORITES" -> {
                            trackDao.getFavoriteTracksSync().map { it.toMediaItem() }
                        }
                        else -> emptyList()
                    }
                    future.set(LibraryResult.ofItemList(items, params))
                } catch (e: Exception) {
                    future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_UNKNOWN))
                }
            }
            return future
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val future = SettableFuture.create<LibraryResult<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val track = trackDao.getTrackById(mediaId)
                    if (track != null) {
                        future.set(LibraryResult.ofItem(track.toMediaItem(), null))
                    } else {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                } catch (e: Exception) {
                    future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_UNKNOWN))
                }
            }
            return future
        }
    }

    private fun createFolderItem(id: String, title: String, folderType: Int): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setFolderType(folderType)
                    .build()
            )
            .build()
    }

    private fun TrackEntity.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(path)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(artworkUri?.let(Uri::parse))
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AppLogger.i(tag, "Destroying PlaybackService")
        serviceScope.cancel()
        audioEffectsController.releaseEffects()
        sleepTimerManager.cancelTimer()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
