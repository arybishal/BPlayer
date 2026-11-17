package com.pulse.player.service.controller

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SmartPreviousDetectorTest {

    private lateinit var detector: SmartPreviousDetector

    @Before
    fun setUp() {
        detector = SmartPreviousDetector(behavior = PreviousBehavior.SMART, doubleTapWindowMs = 350L)
    }

    @Test
    fun `single tap immediately restarts track`() {
        val action = detector.onPreviousTapped(currentPositionMs = 15_000L, currentTimeMs = 1000L)
        assertEquals(PreviousAction.RESTART_TRACK, action)
    }

    @Test
    fun `double tap within 350ms skips to previous track`() {
        // Tap 1 at 1000ms
        val action1 = detector.onPreviousTapped(currentPositionMs = 15_000L, currentTimeMs = 1000L)
        assertEquals(PreviousAction.RESTART_TRACK, action1)

        // Tap 2 at 1250ms (delta 250ms < 350ms)
        val action2 = detector.onPreviousTapped(currentPositionMs = 0L, currentTimeMs = 1250L)
        assertEquals(PreviousAction.SKIP_TO_PREVIOUS, action2)
    }

    @Test
    fun `second tap after 350ms window restarts track again`() {
        // Tap 1 at 1000ms
        val action1 = detector.onPreviousTapped(currentPositionMs = 15_000L, currentTimeMs = 1000L)
        assertEquals(PreviousAction.RESTART_TRACK, action1)

        // Tap 2 at 1450ms (delta 450ms > 350ms)
        val action2 = detector.onPreviousTapped(currentPositionMs = 450L, currentTimeMs = 1450L)
        assertEquals(PreviousAction.RESTART_TRACK, action2)
    }

    @Test
    fun `always previous mode immediately skips to previous on single tap`() {
        detector.behavior = PreviousBehavior.ALWAYS_PREVIOUS
        val action = detector.onPreviousTapped(currentPositionMs = 20_000L, currentTimeMs = 1000L)
        assertEquals(PreviousAction.SKIP_TO_PREVIOUS, action)
    }

    @Test
    fun `always restart mode always restarts track even with rapid taps`() {
        detector.behavior = PreviousBehavior.ALWAYS_RESTART
        val action1 = detector.onPreviousTapped(currentPositionMs = 20_000L, currentTimeMs = 1000L)
        assertEquals(PreviousAction.RESTART_TRACK, action1)

        val action2 = detector.onPreviousTapped(currentPositionMs = 0L, currentTimeMs = 1100L)
        assertEquals(PreviousAction.RESTART_TRACK, action2)
    }
}
