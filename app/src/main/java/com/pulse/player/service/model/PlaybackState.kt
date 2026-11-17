package com.pulse.player.service.model

import com.pulse.player.domain.model.Track

enum class RepeatState {
    OFF, ALL, ONE
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatState: RepeatState = RepeatState.OFF,
    val queue: List<Track> = emptyList(),
    val currentQueueIndex: Int = -1
) {
    val progress: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
