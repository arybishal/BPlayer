package com.pulse.player.data.local.audio

import kotlinx.coroutines.flow.StateFlow

interface AudioRecorder {
    val amplitudeFlow: StateFlow<Float>
    val isRecordingFlow: StateFlow<Boolean>
    suspend fun recordSample(durationSeconds: Int = 6): ByteArray?
}
