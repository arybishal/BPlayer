package com.pulse.player.data.repository

import com.pulse.player.data.local.audio.AudioRecorder
import com.pulse.player.data.local.datastore.RecognitionPreferences
import com.pulse.player.data.remote.audd.AuddApi
import com.pulse.player.domain.model.RecognizedSong
import com.pulse.player.domain.repository.LibraryRepository
import com.pulse.player.domain.repository.RecognitionState
import com.pulse.player.domain.repository.SongRecognitionRepository
import com.pulse.player.util.logger.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRecognitionRepositoryImpl @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val auddApi: AuddApi,
    private val preferences: RecognitionPreferences,
    private val libraryRepository: LibraryRepository
) : SongRecognitionRepository {

    private val tag = "SongRecognitionRepo"

    override val amplitudeFlow: StateFlow<Float> = audioRecorder.amplitudeFlow

    override suspend fun recognizeFromMicrophone(durationSeconds: Int): RecognitionState = withContext(Dispatchers.IO) {
        try {
            val wavBytes = audioRecorder.recordSample(durationSeconds)
            if (wavBytes == null || wavBytes.size <= 44) {
                return@withContext RecognitionState.Error("Microphone capture failed or was cancelled.")
            }
            return@withContext recognizeFromWavBytes(wavBytes)
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to capture microphone audio", e)
            return@withContext RecognitionState.Error("Microphone capture error: ${e.message}")
        }
    }

    override suspend fun recognizeFromWavBytes(wavBytes: ByteArray): RecognitionState = withContext(Dispatchers.IO) {
        try {
            val savedKey = preferences.auddApiKeyFlow.first().trim()
            val token = savedKey.ifEmpty { "test" }

            val tokenPart = token.toRequestBody("text/plain".toMediaType())
            val returnPart = "apple_music,spotify".toRequestBody("text/plain".toMediaType())
            val audioRequestBody = wavBytes.toRequestBody("audio/wav".toMediaType())
            val filePart = MultipartBody.Part.createFormData("file", "sample.wav", audioRequestBody)

            AppLogger.i(tag, "Sending audio fingerprint to AudD API (token=${if (token == "test") "demo/test" else "custom"})...")
            val response = auddApi.recognizeSong(tokenPart, filePart, returnPart)

            if (response.status == "error") {
                val errMsg = response.error?.errorMessage ?: "Acoustic fingerprinting failed."
                val needsKey = token == "test" || response.error?.errorCode == 900 || response.error?.errorCode == 901
                AppLogger.w(tag, "AudD error response: $errMsg (code: ${response.error?.errorCode})")
                return@withContext RecognitionState.Error(
                    message = if (needsKey) "AudD API Key required or limit reached. Please add your key in Settings." else errMsg,
                    needsApiKey = needsKey
                )
            }

            val result = response.result
            if (result == null || result.title.isNullOrEmpty()) {
                AppLogger.i(tag, "No matching song found by AudD.")
                return@withContext RecognitionState.NoMatch(
                    "No match found. Please bring your phone closer to the speaker and try again."
                )
            }

            // Extract artwork url
            val artworkUrl = result.spotify?.album?.images?.firstOrNull()?.url
                ?: result.appleMusic?.artwork?.url?.replace("{w}x{h}", "600x600")

            // Check if track exists in user's local library
            val tracks = libraryRepository.getTracks().first()
            val matchedLocalTrack = tracks.firstOrNull { local ->
                val titleMatch = local.title.contains(result.title, ignoreCase = true) ||
                        result.title.contains(local.title, ignoreCase = true)
                val artistMatch = result.artist?.let { local.artist.contains(it, ignoreCase = true) || it.contains(local.artist, ignoreCase = true) } ?: false
                titleMatch && artistMatch
            }

            val recognizedSong = RecognizedSong(
                title = result.title,
                artist = result.artist ?: "Unknown Artist",
                album = result.album ?: "Unknown Album",
                releaseDate = result.releaseDate,
                label = result.label,
                artworkUrl = artworkUrl,
                songLink = result.songLink,
                localTrackMatch = matchedLocalTrack
            )

            AppLogger.i(tag, "Successfully recognized: ${recognizedSong.title} by ${recognizedSong.artist} (Local match: ${matchedLocalTrack != null})")
            return@withContext RecognitionState.Success(recognizedSong)
        } catch (e: Exception) {
            AppLogger.e(tag, "AudD network recognition call failed", e)
            return@withContext RecognitionState.Error(
                "Unable to connect to song recognition service. Please check your internet connection."
            )
        }
    }
}
