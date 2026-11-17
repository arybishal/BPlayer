package com.pulse.player.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioFormatInspectorTest {

    @Test
    fun `standard CD quality FLAC is identified as lossless but not hires`() {
        val info = AudioFormatInspector.inspect(
            path = "/storage/emulated/0/Music/song.flac",
            mimeType = "audio/flac",
            sampleRate = 44100,
            bitDepth = 16,
            bitRate = 900000
        )

        assertEquals("FLAC", info.extension)
        assertEquals("FLAC", info.badgeText)
        assertTrue(info.isLossless)
        assertFalse(info.isHiRes)
        assertFalse(info.isDsd)
        assertTrue(info.detailedBadgeText.contains("16-bit"))
        assertTrue(info.detailedBadgeText.contains("44.1kHz"))
    }

    @Test
    fun `24-bit 96kHz FLAC is identified as hires and lossless`() {
        val info = AudioFormatInspector.inspect(
            path = "/storage/emulated/0/Music/master.flac",
            mimeType = "audio/flac",
            sampleRate = 96000,
            bitDepth = 24,
            bitRate = 2500000
        )

        assertEquals("HI-RES", info.badgeText)
        assertTrue(info.isLossless)
        assertTrue(info.isHiRes)
        assertFalse(info.isDsd)
        assertTrue(info.detailedBadgeText.contains("24-bit"))
        assertTrue(info.detailedBadgeText.contains("96.0kHz"))
    }

    @Test
    fun `DSD DSF audio is identified as DSD, Hi-Res, and decimated`() {
        val info = AudioFormatInspector.inspect(
            path = "/storage/emulated/0/Music/album/track.dsf",
            mimeType = "audio/dsd",
            sampleRate = 0,
            bitDepth = 0,
            bitRate = 5644800
        )

        assertEquals("DSD", info.badgeText)
        assertTrue(info.isDsd)
        assertTrue(info.isHiRes)
        assertTrue(info.isLossless)
        assertNotNull(info.dsdDecimationRate)
        assertTrue(info.dsdDecimationRate!!.contains("24-bit / 88.2kHz"))
        assertEquals(88200, info.sampleRateHz)
        assertEquals(24, info.bitDepth)
    }

    @Test
    fun `standard MP3 is identified as lossy audio`() {
        val info = AudioFormatInspector.inspect(
            path = "/storage/emulated/0/Music/pop.mp3",
            mimeType = "audio/mpeg",
            sampleRate = 44100,
            bitDepth = 16,
            bitRate = 320000
        )

        assertEquals("MP3", info.badgeText)
        assertFalse(info.isLossless)
        assertFalse(info.isHiRes)
        assertFalse(info.isDsd)
    }

    @Test
    fun `M4A container with AAC stream produces AAC badge`() {
        val info = AudioFormatInspector.inspect(
            path = "/storage/emulated/0/Music/apple.m4a",
            mimeType = "audio/mp4",
            sampleRate = 44100,
            bitDepth = 16,
            bitRate = 256000
        )

        assertEquals("AAC", info.badgeText)
        assertFalse(info.isLossless)
        assertFalse(info.isHiRes)
    }

    @Test
    fun `format support check recognizes modern and legacy audiophile formats`() {
        assertTrue(AudioFormatInspector.isFormatSupported("track.mp3"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.flac"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.wav"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.dsf"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.dff"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.ape"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.wv"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.tta"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.mpc"))
        assertTrue(AudioFormatInspector.isFormatSupported("track.opus"))

        assertFalse(AudioFormatInspector.isFormatSupported("document.pdf"))
        assertFalse(AudioFormatInspector.isFormatSupported("installer.exe"))
        assertFalse(AudioFormatInspector.isFormatSupported("photo.jpg"))
    }
}
