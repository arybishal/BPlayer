package com.pulse.player.domain.repository

import com.pulse.player.domain.model.RecognizedSong
import kotlinx.coroutines.flow.StateFlow

sealed interface RecognitionState {
    data object Idle : RecognitionState
    data class Recording(val secondsRemaining: Int) : RecognitionState
    data object Analyzing : RecognitionState
    data class Success(val song: RecognizedSong) : RecognitionState
    data class NoMatch(val message: String) : RecognitionState
    data class Error(val message: String, val needsApiKey: Boolean = false) : RecognitionState
}

interface SongRecognitionRepository {
    val amplitudeFlow: StateFlow<Float>
    suspend fun recognizeFromMicrophone(durationSeconds: Int = 6): RecognitionState
    suspend fun recognizeFromWavBytes(wavBytes: ByteArray): RecognitionState
}
