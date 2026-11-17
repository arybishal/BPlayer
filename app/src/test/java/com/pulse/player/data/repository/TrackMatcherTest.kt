package com.pulse.player.data.repository

import com.pulse.player.data.local.db.entity.TrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TrackMatcherTest {

    private val localTracks = listOf(
        TrackEntity(
            id = "track_101",
            mediaStoreId = 101L,
            title = "Bohemian Rhapsody",
            artist = "Queen",
            artistId = 1L,
            album = "A Night at the Opera",
            albumId = 10L,
            durationMs = 354000L,
            path = "/storage/emulated/0/Music/Queen/bohemian.flac",
            trackNumber = 11,
            year = 1975,
            mimeType = "audio/flac",
            bitRate = 900,
            sampleRate = 44100,
            bitDepth = 16,
            dateAdded = 1000L,
            dateModified = 1000L,
            size = 35000000L
        ),
        TrackEntity(
            id = "track_102",
            mediaStoreId = 102L,
            title = "Hotel California",
            artist = "Eagles",
            artistId = 2L,
            album = "Hotel California",
            albumId = 20L,
            durationMs = 391000L,
            path = "/storage/emulated/0/Music/Eagles/hotel.mp3",
            trackNumber = 1,
            year = 1976,
            mimeType = "audio/mpeg",
            bitRate = 320,
            sampleRate = 44100,
            bitDepth = 16,
            dateAdded = 1000L,
            dateModified = 1000L,
            size = 12000000L
        ),
        TrackEntity(
            id = "track_103",
            mediaStoreId = 103L,
            title = "Comfortably Numb",
            artist = "Pink Floyd",
            artistId = 3L,
            album = "The Wall",
            albumId = 30L,
            durationMs = 382000L,
            path = "/storage/emulated/0/Music/Pink Floyd/numb.mp3",
            trackNumber = 6,
            year = 1979,
            mimeType = "audio/mpeg",
            bitRate = 320,
            sampleRate = 44100,
            bitDepth = 16,
            dateAdded = 1000L,
            dateModified = 1000L,
            size = 14000000L
        )
    )

    private val matcher = TrackMatcher(localTracks)

    @Test
    fun `exact title and artist matches correctly`() {
        val result = matcher.findMatch(
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            durationMs = 354000L
        )
        assertNotNull(result)
        assertEquals("track_101", result?.id)
    }

    @Test
    fun `case-insensitive and punctuation-tolerant match succeeds`() {
        val result = matcher.findMatch(
            title = "  bohemian rhapsody! ",
            artist = "QUEEN.",
            album = "Unknown",
            durationMs = 0L
        )
        assertNotNull(result)
        assertEquals("track_101", result?.id)
    }

    @Test
    fun `fuzzy duration match within 4s window matches when artist is missing`() {
        val result = matcher.findMatch(
            title = "Hotel California",
            artist = "",
            album = "",
            durationMs = 393000L // 2 seconds diff
        )
        assertNotNull(result)
        assertEquals("track_102", result?.id)
    }

    @Test
    fun `match by title and album when artist is missing`() {
        val result = matcher.findMatch(
            title = "Comfortably Numb",
            artist = "Unknown Artist",
            album = "The Wall",
            durationMs = 0L
        )
        assertNotNull(result)
        assertEquals("track_103", result?.id)
    }

    @Test
    fun `completely nonexistent track returns null`() {
        val result = matcher.findMatch(
            title = "Stairway to Heaven",
            artist = "Led Zeppelin",
            album = "Led Zeppelin IV",
            durationMs = 482000L
        )
        assertNull(result)
    }
}
