package com.lastwave.app.data.download

import com.lastwave.app.data.music.InnerTubeMusicApi
import com.lastwave.app.data.music.YouTubeAudioStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout
import java.io.IOException

internal suspend fun InnerTubeMusicApi.resolveDownloadCandidate(
    videoId: String,
    rejectedStreams: Set<String>,
    preferM4a: Boolean = false,
    remainingBudgetMs: () -> Long,
): YouTubeAudioStream {
    var lastError: Exception? = null
    fun wasRejected(stream: YouTubeAudioStream) = stream.downloadRenditionKey() in rejectedStreams

    suspend fun attempt(resolve: suspend () -> YouTubeAudioStream): YouTubeAudioStream? = try {
        val timeoutMs = minOf(YT_STREAM_RESOLVE_TIMEOUT_MS, remainingBudgetMs())
        if (timeoutMs <= 0L) throw IOException("YouTube stream resolution timed out")
        withTimeout(timeoutMs) { resolve() }.takeUnless { it.isHlsPlaylist() }
    } catch (error: Exception) {
        currentCoroutineContext().ensureActive()
        lastError = when (error) {
            is TimeoutCancellationException -> IOException("YouTube stream resolution timed out", error)
            is CancellationException -> IOException("YouTube stream resolution interrupted; retrying may help", error)
            else -> error
        }
        null
    }

    if (preferM4a) {
        val m4aDirect = attempt { streamExtractor.resolveAudioStream(videoId, preferM4a = true) }
        val m4aCandidates = listOfNotNull(m4aDirect, attempt { resolveAudioStream(videoId) })
        val m4aChosen = m4aCandidates.firstOrNull { !wasRejected(it) } ?: m4aCandidates.firstOrNull()
        if (m4aChosen != null) return m4aChosen
    }

    val playback = attempt {
        peekCachedStream(videoId)?.takeUnless { it.isHlsPlaylist() || wasRejected(it) } ?: resolveAudioStream(videoId)
    }
    val opus = playback?.takeIf { it.isOpusStream() && !wasRejected(it) }
        ?: attempt { resolveDownloadStream(videoId) }
    val candidates = listOfNotNull(opus?.takeIf { it.isOpusStream() }, playback, opus)
    // Prefer an alternative after a failed rendition, but allow its renewed URL if it is all we have.
    return candidates.firstOrNull { !wasRejected(it) } ?: candidates.firstOrNull()
        ?: throw (lastError ?: IOException("YouTube returned no downloadable audio file"))
}

internal fun YouTubeAudioStream.downloadRenditionKey(): String =
    "$videoId|$clientProfile|${itag?.toString() ?: "$mimeType|$codec"}"
