package com.pulse.player.service.sleeptimer

import com.pulse.player.util.logger.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0L,
    val totalSeconds: Long = 0L,
    val isEndOfTrackMode: Boolean = false
) {
    val formattedRemaining: String
        get() {
            if (isEndOfTrackMode) return "End of Track"
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

@Singleton
class SleepTimerManager @Inject constructor() {

    private val tag = "SleepTimerManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null

    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    // Callbacks to player
    private var onPausePlayer: (() -> Unit)? = null
    private var onSetVolume: ((Float) -> Unit)? = null

    fun initialize(onPause: () -> Unit, onVolume: (Float) -> Unit) {
        this.onPausePlayer = onPause
        this.onSetVolume = onVolume
    }

    fun startMinutesTimer(minutes: Int) {
        cancelTimer()
        val totalSecs = (minutes * 60).toLong()
        _state.value = SleepTimerState(
            isActive = true,
            remainingSeconds = totalSecs,
            totalSeconds = totalSecs,
            isEndOfTrackMode = false
        )

        AppLogger.i(tag, "Started sleep timer for $minutes minutes ($totalSecs seconds)")

        timerJob = scope.launch {
            var currentRemaining = totalSecs
            while (isActive && currentRemaining > 0) {
                delay(1000)
                currentRemaining--
                _state.value = _state.value.copy(remainingSeconds = currentRemaining)

                // 10-second smooth volume fade-out prior to pause
                if (currentRemaining in 1..10) {
                    val fadeVolume = (currentRemaining / 10f).coerceIn(0f, 1f)
                    onSetVolume?.invoke(fadeVolume)
                }
            }

            if (isActive && currentRemaining <= 0) {
                triggerSleepPause()
            }
        }
    }

    fun startEndOfTrackTimer() {
        cancelTimer()
        _state.value = SleepTimerState(
            isActive = true,
            remainingSeconds = 0L,
            totalSeconds = 0L,
            isEndOfTrackMode = true
        )
        AppLogger.i(tag, "Started sleep timer in End-of-Track mode")
    }

    fun onTrackEnded() {
        if (_state.value.isActive && _state.value.isEndOfTrackMode) {
            triggerSleepPause()
        }
    }

    private fun triggerSleepPause() {
        AppLogger.i(tag, "Sleep timer expired. Pausing playback and restoring normal volume.")
        onPausePlayer?.invoke()
        onSetVolume?.invoke(1.0f)
        _state.value = SleepTimerState(isActive = false)
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        onSetVolume?.invoke(1.0f)
        _state.value = SleepTimerState(isActive = false)
        AppLogger.i(tag, "Sleep timer cancelled")
    }
}
