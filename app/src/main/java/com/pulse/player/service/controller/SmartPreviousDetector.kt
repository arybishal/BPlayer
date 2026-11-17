package com.pulse.player.service.controller

enum class PreviousBehavior {
    SMART,           // Single tap restarts current track; double tap (< 350ms) skips to previous
    ALWAYS_PREVIOUS, // Every tap jumps to previous track
    ALWAYS_RESTART   // Every tap restarts current track from 0:00
}

enum class PreviousAction {
    RESTART_TRACK,
    SKIP_TO_PREVIOUS
}

class SmartPreviousDetector(
    var behavior: PreviousBehavior = PreviousBehavior.SMART,
    val doubleTapWindowMs: Long = 350L
) {
    private var lastTapTimestamp: Long = 0L

    fun onPreviousTapped(currentPositionMs: Long = 0L, currentTimeMs: Long = System.currentTimeMillis()): PreviousAction {
        return when (behavior) {
            PreviousBehavior.ALWAYS_PREVIOUS -> PreviousAction.SKIP_TO_PREVIOUS
            PreviousBehavior.ALWAYS_RESTART -> PreviousAction.RESTART_TRACK
            PreviousBehavior.SMART -> {
                val timeSinceLastTap = currentTimeMs - lastTapTimestamp
                if (timeSinceLastTap in 1..doubleTapWindowMs) {
                    // Double tap within 350ms -> skip to previous
                    lastTapTimestamp = 0L
                    PreviousAction.SKIP_TO_PREVIOUS
                } else {
                    // First tap -> restart track immediately
                    lastTapTimestamp = currentTimeMs
                    PreviousAction.RESTART_TRACK
                }
            }
        }
    }

    fun reset() {
        lastTapTimestamp = 0L
    }
}
