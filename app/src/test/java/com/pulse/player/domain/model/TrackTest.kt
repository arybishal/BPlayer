package com.pulse.player.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackTest {

    @Test
    fun `test formatted duration helper`() {
        val trackShort = createTestTrack(durationMs = 45_000L)
        assertEquals("0:45", trackShort.formattedDuration)

        val trackStandard = createTestTrack(durationMs = 215_000L) // 3m 35s
        assertEquals("3:35", trackStandard.formattedDuration)

        val trackLong = createTestTrack(durationMs = 3_665_000L) // 61m 5s
        assertEquals("61:05", trackLong.formattedDuration)
    }

    @Test
    fun `test format badge calculation`() {
        val flacHiRes = createTestTrack(path = "/music/song.flac", sampleRate = 96000, bitDepth = 24)
        assertEquals("HI-RES", flacHiRes.formatBadge)

        val flacStandard = createTestTrack(path = "/music/song.flac", sampleRate = 44100, bitDepth = 16)
        assertEquals("FLAC", flacStandard.formatBadge)

        val mp3 = createTestTrack(path = "/music/song.mp3", sampleRate = 44100, bitDepth = 16)
        assertEquals("MP3", mp3.formatBadge)

        val m4a = createTestTrack(path = "/music/song.m4a", sampleRate = 44100, bitDepth = 16)
        assertEquals("AAC", m4a.formatBadge)
    }

    private fun createTestTrack(
        path: String = "/storage/emulated/0/Music/track.mp3",
        durationMs: Long = 180_000L,
        sampleRate: Int = 44100,
        bitDepth: Int = 16
    ): Track {
        return Track(
            id = "1",
            mediaStoreId = 1L,
            title = "Test Song",
            artist = "Test Artist",
            artistId = 1L,
            album = "Test Album",
            albumId = 1L,
            durationMs = durationMs,
            path = path,
            trackNumber = 1,
            year = 2024,
            mimeType = "audio/mpeg",
            bitRate = 320,
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            dateAdded = 1000L,
            dateModified = 1000L,
            size = 5_000_000L
        )
    }
}
