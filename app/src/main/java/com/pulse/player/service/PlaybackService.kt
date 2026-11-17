package com.pulse.player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.pulse.player.data.local.db.dao.QueueDao
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.service.audiofx.AudioEffectsController
import com.pulse.player.service.sleeptimer.SleepTimerManager
import com.pulse.player.ui.MainActivity
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private val tag = "PlaybackService"
    private var mediaSession: MediaSession? = null
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

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        audioEffectsController.attachToSession(exoPlayer.audioSessionId)
        sleepTimerManager.initialize(
            onPause = { exoPlayer.pause() },
            onVolume = { vol -> exoPlayer.volume = vol }
        )
        AppLogger.i(tag, "PlaybackService created and ExoPlayer initialized.")
    }

    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // true = handle audio focus automatically
            .setHandleAudioBecomingNoisy(true)        // auto-pause when headphones disconnect
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
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    sleepTimerManager.onTrackEnded()
                }
            }
        })
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

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
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
