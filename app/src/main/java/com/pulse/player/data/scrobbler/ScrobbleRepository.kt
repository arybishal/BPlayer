package com.pulse.player.data.scrobbler

import android.os.SystemClock
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.util.logger.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScrobbleRepository @Inject constructor(
    private val client: OkHttpClient,
    private val preferencesManager: PreferencesManager,
    private val rateGuard: LastFmRateGuard,
) {
    private val tag = "ScrobbleRepository"
    private val json = Json { ignoreUnknownKeys = true }
    private val callMutex = Mutex()
    private var lastWriteAtElapsed = 0L
    @Volatile private var lastNowPlayingKey: String? = null

    private val _scrobbleEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrobbleEvents: SharedFlow<Unit> = _scrobbleEvents.asSharedFlow()

    private data class PendingScrobble(
        val artist: String,
        val track: String,
        val album: String?,
        val timestampSec: Long,
    )
    private val offlineQueue = ConcurrentLinkedQueue<PendingScrobble>()

    sealed interface Result {
        data object Success : Result
        data object NoSessionKey : Result
        data class Failed(
            val message: String,
            val retryable: Boolean = false,
        ) : Result
    }

    suspend fun updateNowPlaying(artist: String, track: String, album: String?): Result {
        val enabled = preferencesManager.lastFmScrobblingEnabledFlow.first()
        if (!enabled) return Result.Success

        if (artist.equals("Podcast", ignoreCase = true) || track.equals("Podcast", ignoreCase = true)) {
            return Result.Success
        }
        val key = "${artist.lowercase().trim()}|${track.lowercase().trim()}"
        if (key == lastNowPlayingKey) {
            return Result.Success
        }

        val result = signedCall(
            method = "track.updateNowPlaying",
            extra = buildMap {
                put("artist", artist)
                put("track", track)
                if (!album.isNullOrBlank()) put("album", album)
            },
        )
        if (result is Result.Success) {
            lastNowPlayingKey = key
            AppLogger.d(tag, "Last.fm Now Playing updated: $artist - $track")
        }
        return result
    }

    suspend fun scrobble(artist: String, track: String, album: String?, timestampSec: Long = System.currentTimeMillis() / 1000): Result {
        val enabled = preferencesManager.lastFmScrobblingEnabledFlow.first()
        if (!enabled) return Result.Success

        if (artist.equals("Podcast", ignoreCase = true) || track.equals("Podcast", ignoreCase = true)) {
            return Result.Success
        }
        flushPendingScrobbles()

        val result = signedCall(
            method = "track.scrobble",
            extra = buildMap {
                put("artist", artist)
                put("track", track)
                put("timestamp", timestampSec.toString())
                if (!album.isNullOrBlank()) put("album", album)
            },
        )

        if (result is Result.Success) {
            _scrobbleEvents.tryEmit(Unit)
            AppLogger.i(tag, "Last.fm Scrobble submitted: $artist - $track")
        } else if (result is Result.Failed && result.retryable) {
            offlineQueue.add(PendingScrobble(artist, track, album, timestampSec))
            while (offlineQueue.size > MAX_OFFLINE_QUEUE) offlineQueue.poll()
            AppLogger.w(tag, "Last.fm Scrobble queued for offline retry: $artist - $track")
        }
        return result
    }

    private suspend fun flushPendingScrobbles() {
        if (offlineQueue.isEmpty()) return
        if (rateGuard.cooldownRemainingMs > 0L) return
        while (offlineQueue.isNotEmpty()) {
            val pending = offlineQueue.peek() ?: break
            val res = signedCall(
                method = "track.scrobble",
                extra = buildMap {
                    put("artist", pending.artist)
                    put("track", pending.track)
                    put("timestamp", pending.timestampSec.toString())
                    if (!pending.album.isNullOrBlank()) put("album", pending.album)
                },
            )
            if (res is Result.Success) {
                offlineQueue.poll()
                _scrobbleEvents.tryEmit(Unit)
            } else {
                break
            }
        }
    }

    suspend fun authenticateMobileSession(username: String, password: String, apiKey: String, apiSecret: String): Result = withContext(Dispatchers.IO) {
        try {
            val cleanApiKey = apiKey.ifBlank { DEFAULT_API_KEY }
            val cleanApiSecret = apiSecret.ifBlank { DEFAULT_API_SECRET }

            val params = mapOf(
                "method" to "auth.getMobileSession",
                "username" to username.trim(),
                "password" to password,
                "api_key" to cleanApiKey,
            )
            val sig = LastFmSigner.sign(params, cleanApiSecret)
            val form = FormBody.Builder()
            params.forEach { (k, v) -> form.add(k, v) }
            form.add("api_sig", sig)
            form.add("format", "json")

            val request = Request.Builder()
                .url(API_URL)
                .post(form.build())
                .build()

            val response = client.newCall(request).execute()
            val text = response.body?.string() ?: return@withContext Result.Failed("Empty response from Last.fm")
            val root = json.parseToJsonElement(text).jsonObject
            val session = root["session"]?.jsonObject
            val sessionKey = session?.get("key")?.jsonPrimitive?.content
            val authUser = session?.get("name")?.jsonPrimitive?.content ?: username

            if (!sessionKey.isNullOrBlank()) {
                preferencesManager.setLastFmSession(
                    apiKey = cleanApiKey,
                    apiSecret = cleanApiSecret,
                    sessionKey = sessionKey,
                    username = authUser,
                )
                Result.Success
            } else {
                val errorMsg = root["message"]?.jsonPrimitive?.content ?: "Authentication failed"
                Result.Failed(errorMsg)
            }
        } catch (e: Exception) {
            Result.Failed(e.message ?: "Authentication error")
        }
    }

    private suspend fun signedCall(method: String, extra: Map<String, String>): Result = withContext(Dispatchers.IO) {
        callMutex.withLock {
            val apiKey = preferencesManager.lastFmApiKeyFlow.first().ifBlank { DEFAULT_API_KEY }
            val apiSecret = preferencesManager.lastFmApiSecretFlow.first().ifBlank { DEFAULT_API_SECRET }
            val sessionKey = preferencesManager.lastFmSessionKeyFlow.first()

            if (sessionKey.isBlank()) {
                return@withLock Result.NoSessionKey
            }

            if (!rateGuard.suspendAwaitClearance(WRITE_COOLDOWN_WAIT_MS)) {
                return@withLock Result.Failed(
                    "Rate limit cooldown active — queued for retry",
                    retryable = true,
                )
            }

            val sinceLastWrite = SystemClock.elapsedRealtime() - lastWriteAtElapsed
            if (sinceLastWrite in 1 until WRITE_SPACING_MS) {
                delay(WRITE_SPACING_MS - sinceLastWrite)
            }

            try {
                val signParams = extra + mapOf(
                    "method" to method,
                    "sk" to sessionKey,
                    "api_key" to apiKey,
                )
                val sig = LastFmSigner.sign(signParams, apiSecret)
                val form = FormBody.Builder()
                signParams.forEach { (k, v) -> form.add(k, v) }
                form.add("api_sig", sig)
                form.add("format", "json")

                val request = Request.Builder()
                    .url(API_URL)
                    .post(form.build())
                    .build()

                val response = client.newCall(request).execute()
                lastWriteAtElapsed = SystemClock.elapsedRealtime()

                if (response.code == 429) {
                    rateGuard.onRequestLimited()
                    return@withLock Result.Failed("Last.fm rate limited (429)", retryable = true)
                }

                val text = response.body?.string() ?: return@withLock Result.Failed("Empty response from Last.fm")
                val parsed = json.parseToJsonElement(text).jsonObject
                val errorCode = parsed["error"]?.jsonPrimitive?.content?.toIntOrNull()
                if (errorCode != null) {
                    if (errorCode == 29) {
                        rateGuard.onRequestLimited()
                        return@withLock Result.Failed("Last.fm rate limited (error 29)", retryable = true)
                    }
                    val msg = parsed["message"]?.jsonPrimitive?.content ?: "Error $errorCode"
                    Result.Failed(msg)
                } else {
                    rateGuard.onRequestSucceeded()
                    Result.Success
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: IOException) {
                Result.Failed(e.message ?: "Could not reach Last.fm", retryable = true)
            } catch (e: Exception) {
                Result.Failed(e.message ?: "Could not reach Last.fm")
            }
        }
    }

    companion object {
        const val API_URL = "https://ws.audioscrobbler.com/2.0/"
        const val DEFAULT_API_KEY = "c89b25fc8f7470404be1b86d9a0d8199"
        const val DEFAULT_API_SECRET = "dbb8b7dfbd850a9d949b28a2a46e10f1"
        private const val WRITE_COOLDOWN_WAIT_MS = 25_000L
        private const val WRITE_SPACING_MS = 700L
        private const val MAX_OFFLINE_QUEUE = 200
    }
}
