package com.pulse.player.service.sleeptimer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerManagerTest {

    @Test
    fun `initial state is inactive`() {
        val manager = SleepTimerManager()
        assertFalse(manager.state.value.isActive)
        assertEquals(0L, manager.state.value.remainingSeconds)
    }

    @Test
    fun `startMinutesTimer sets active state and calculates remaining seconds`() {
        val manager = SleepTimerManager()
        manager.startMinutesTimer(15)

        assertTrue(manager.state.value.isActive)
        assertEquals(900L, manager.state.value.remainingSeconds)
        assertEquals(900L, manager.state.value.totalSeconds)
        assertFalse(manager.state.value.isEndOfTrackMode)
        assertEquals("15:00", manager.state.value.formattedRemaining)
    }

    @Test
    fun `startEndOfTrackTimer sets correct mode`() {
        val manager = SleepTimerManager()
        manager.startEndOfTrackTimer()

        assertTrue(manager.state.value.isActive)
        assertTrue(manager.state.value.isEndOfTrackMode)
        assertEquals("End of Track", manager.state.value.formattedRemaining)
    }

    @Test
    fun `cancelTimer resets state and restores volume`() {
        var restoredVolume = 0f
        val manager = SleepTimerManager()
        manager.initialize(
            onPause = {},
            onVolume = { restoredVolume = it }
        )

        manager.startMinutesTimer(30)
        assertTrue(manager.state.value.isActive)

        manager.cancelTimer()
        assertFalse(manager.state.value.isActive)
        assertEquals(1.0f, restoredVolume, 0.01f)
    }

    @Test
    fun `onTrackEnded triggers pause in end-of-track mode`() {
        var pauseCalled = false
        var restoredVolume = 0f

        val manager = SleepTimerManager()
        manager.initialize(
            onPause = { pauseCalled = true },
            onVolume = { restoredVolume = it }
        )

        manager.startEndOfTrackTimer()
        manager.onTrackEnded()

        assertTrue(pauseCalled)
        assertEquals(1.0f, restoredVolume, 0.01f)
        assertFalse(manager.state.value.isActive)
    }
}
