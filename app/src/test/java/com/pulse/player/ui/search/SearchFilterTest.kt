package com.pulse.player.ui.search

import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchFilterTest {

    private val sampleTracks = listOf(
        Track(id = "1", title = "Bohemian Rhapsody", artist = "Queen", album = "A Night at the Opera", durationMs = 354000L, path = "/music/bohemian.mp3"),
        Track(id = "2", title = "Under Pressure", artist = "Queen & David Bowie", album = "Hot Space", durationMs = 248000L, path = "/music/under_pressure.mp3"),
        Track(id = "3", title = "Space Oddity", artist = "David Bowie", album = "David Bowie", durationMs = 315000L, path = "/music/space_oddity.mp3"),
        Track(id = "4", title = "Starman", artist = "David Bowie", album = "The Rise and Fall of Ziggy Stardust", durationMs = 254000L, path = "/music/starman.mp3")
    )

    private val sampleAlbums = listOf(
        Album(id = 1L, title = "A Night at the Opera", artist = "Queen", trackCount = 12, year = 1975),
        Album(id = 2L, title = "Hot Space", artist = "Queen", trackCount = 11, year = 1982),
        Album(id = 3L, title = "Space Oddity", artist = "David Bowie", trackCount = 10, year = 1969)
    )

    private val sampleArtists = listOf(
        Artist(id = 1L, name = "Queen", trackCount = 2, albumCount = 2),
        Artist(id = 2L, name = "David Bowie", trackCount = 3, albumCount = 2)
    )

    @Test
    fun `search filters tracks by title case-insensitively`() {
        val query = "bohemian"
        val results = sampleTracks.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("Bohemian Rhapsody", results.first().title)
    }

    @Test
    fun `search filters tracks by artist across multiple songs`() {
        val query = "bowie"
        val results = sampleTracks.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
        assertEquals(3, results.size)
    }

    @Test
    fun `search filters albums by title and artist`() {
        val query = "space"
        val results = sampleAlbums.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true)
        }
        assertEquals(2, results.size)
        assertTrue(results.any { it.title == "Hot Space" })
        assertTrue(results.any { it.title == "Space Oddity" })
    }

    @Test
    fun `search filters artists accurately`() {
        val query = "queen"
        val results = sampleArtists.filter {
            it.name.contains(query, ignoreCase = true)
        }
        assertEquals(1, results.size)
        assertEquals("Queen", results.first().name)
    }
}
