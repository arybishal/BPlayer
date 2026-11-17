package com.pulse.player.domain.repository

import com.pulse.player.domain.model.Lyrics
import com.pulse.player.domain.model.LyricsSource
import com.pulse.player.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {
    fun observeLyricsForTrack(trackId: String): Flow<Lyrics?>
    suspend fun getLyricsForTrack(track: Track): Lyrics?
    suspend fun fetchOnlineLyrics(track: Track): Lyrics?
    suspend fun saveLyrics(
        trackId: String,
        lyricsText: String,
        isSynced: Boolean,
        source: LyricsSource
    )
    suspend fun deleteLyrics(trackId: String)
}
