package com.pulse.player.data.repository

import com.pulse.player.data.local.db.dao.LyricsDao
import com.pulse.player.data.local.db.entity.LyricsEntity
import com.pulse.player.data.local.lyrics.LocalLyricsReader
import com.pulse.player.data.remote.lrclib.LrclibApi
import com.pulse.player.domain.model.Lyrics
import com.pulse.player.domain.model.LyricsSource
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LyricsRepository
import com.pulse.player.util.logger.AppLogger
import com.pulse.player.util.lrc.LrcParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lyricsDao: LyricsDao,
    private val localLyricsReader: LocalLyricsReader,
    private val lrclibApi: LrclibApi
) : LyricsRepository {

    private val tag = "LyricsRepository"

    override fun observeLyricsForTrack(trackId: String): Flow<Lyrics?> {
        return lyricsDao.observeLyricsForTrack(trackId).map { it?.toDomain() }
    }

    override suspend fun getLyricsForTrack(track: Track): Lyrics? = withContext(Dispatchers.IO) {
        // 1. Check local Room database cache
        val cached = lyricsDao.getLyricsForTrack(track.id)
        if (cached != null) {
            return@withContext cached.toDomain()
        }

        // 2. Check local sidecar .lrc file next to audio file
        val sidecarLrc = localLyricsReader.readSidecarLrc(track.path)
        if (!sidecarLrc.isNullOrBlank()) {
            val lyrics = LrcParser.parse(track.id, sidecarLrc, LyricsSource.SIDECAR_LRC)
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.id,
                    plainText = lyrics.plainText,
                    syncedLrc = sidecarLrc,
                    isSynchronized = lyrics.isSynchronized,
                    source = LyricsSource.SIDECAR_LRC.name
                )
            )
            return@withContext lyrics
        }

        // 3. Check embedded ID3 / Vorbis comment lyrics in audio file
        val embeddedLyrics = localLyricsReader.readEmbeddedLyrics(track.path)
        if (!embeddedLyrics.isNullOrBlank()) {
            val lyrics = LrcParser.parse(track.id, embeddedLyrics, LyricsSource.EMBEDDED)
            lyricsDao.insertLyrics(
                LyricsEntity(
                    trackId = track.id,
                    plainText = lyrics.plainText,
                    syncedLrc = if (lyrics.isSynchronized) embeddedLyrics else null,
                    isSynchronized = lyrics.isSynchronized,
                    source = LyricsSource.EMBEDDED.name
                )
            )
            return@withContext lyrics
        }

        // 4. If none local, return null (allow UI / user to trigger online fetch)
        null
    }

    override suspend fun fetchOnlineLyrics(track: Track): Lyrics? = withContext(Dispatchers.IO) {
        try {
            val durationSec = (track.durationMs / 1000).toInt()
            val getResponse = lrclibApi.getLyrics(
                artistName = track.artist,
                trackName = track.title,
                albumName = track.album.ifEmpty { null },
                durationSeconds = if (durationSec > 0) durationSec else null
            )

            val body = if (getResponse.isSuccessful && getResponse.body() != null) {
                getResponse.body()
            } else {
                // Fallback to query search
                val searchResponse = lrclibApi.searchLyrics("${track.title} ${track.artist}")
                if (searchResponse.isSuccessful) {
                    searchResponse.body()?.firstOrNull()
                } else {
                    null
                }
            }

            if (body != null) {
                val hasSynced = !body.syncedLyrics.isNullOrBlank()
                val rawLyrics = if (hasSynced) body.syncedLyrics!! else (body.plainLyrics ?: "")
                if (rawLyrics.isNotBlank()) {
                    val lyrics = LrcParser.parse(track.id, rawLyrics, LyricsSource.LRCLIB_REMOTE)
                    lyricsDao.insertLyrics(
                        LyricsEntity(
                            trackId = track.id,
                            plainText = lyrics.plainText,
                            syncedLrc = if (lyrics.isSynchronized) rawLyrics else null,
                            isSynchronized = lyrics.isSynchronized,
                            source = LyricsSource.LRCLIB_REMOTE.name
                        )
                    )
                    return@withContext lyrics
                }
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to fetch online lyrics from LRCLIB for ${track.title}: ${e.message}")
        }
        null
    }

    override suspend fun saveLyrics(
        trackId: String,
        lyricsText: String,
        isSynced: Boolean,
        source: LyricsSource
    ) = withContext(Dispatchers.IO) {
        val parsed = LrcParser.parse(trackId, lyricsText, source)
        lyricsDao.insertLyrics(
            LyricsEntity(
                trackId = trackId,
                plainText = parsed.plainText,
                syncedLrc = if (parsed.isSynchronized) lyricsText else null,
                isSynchronized = parsed.isSynchronized,
                source = source.name
            )
        )
    }

    override suspend fun deleteLyrics(trackId: String) = withContext(Dispatchers.IO) {
        lyricsDao.deleteLyricsForTrack(trackId)
    }
}
