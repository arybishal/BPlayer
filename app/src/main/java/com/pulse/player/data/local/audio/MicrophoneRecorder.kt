package com.pulse.player.data.local.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class MicrophoneRecorder @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioRecorder {
    private val tag = "MicrophoneRecorder"

    private val _amplitudeFlow = MutableStateFlow(0.0f)
    override val amplitudeFlow: StateFlow<Float> = _amplitudeFlow.asStateFlow()

    private val _isRecordingFlow = MutableStateFlow(false)
    override val isRecordingFlow: StateFlow<Boolean> = _isRecordingFlow.asStateFlow()

    companion object {
        const val SAMPLE_RATE = 44100
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    @SuppressLint("MissingPermission")
    override suspend fun recordSample(durationSeconds: Int): ByteArray? = withContext(Dispatchers.IO) {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minBufferSize <= 0) {
            AppLogger.e(tag, "AudioRecord minBufferSize invalid: $minBufferSize")
            return@withContext null
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)
        var audioRecord: AudioRecord? = null

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                AppLogger.e(tag, "AudioRecord failed to initialize.")
                audioRecord.release()
                return@withContext null
            }

            audioRecord.startRecording()
            _isRecordingFlow.value = true
            AppLogger.i(tag, "Started recording microphone audio for $durationSeconds seconds.")

            val outputStream = ByteArrayOutputStream()
            val buffer = ShortArray(bufferSize / 2)
            val startTime = System.currentTimeMillis()
            val maxDurationMs = durationSeconds * 1000L

            while (currentCoroutineContext().isActive && (System.currentTimeMillis() - startTime) < maxDurationMs) {
                val readCount = audioRecord.read(buffer, 0, buffer.size)
                if (readCount > 0) {
                    var sumOfSquares = 0.0
                    for (i in 0 until readCount) {
                        val sample = buffer[i]
                        sumOfSquares += (sample * sample).toDouble()
                        // Write 16-bit PCM little-endian
                        outputStream.write(sample.toInt() and 0xFF)
                        outputStream.write((sample.toInt() shr 8) and 0xFF)
                    }

                    // Compute normalized RMS amplitude (0.0 to 1.0)
                    val rms = sqrt(sumOfSquares / readCount)
                    val normalized = (rms / 12000.0).toFloat().coerceIn(0.0f, 1.0f)
                    _amplitudeFlow.value = normalized
                }
            }

            val pcmData = outputStream.toByteArray()
            AppLogger.i(tag, "Microphone recording completed: ${pcmData.size} PCM bytes captured.")
            return@withContext buildWav(pcmData, SAMPLE_RATE, 1, 16)
        } catch (e: CancellationException) {
            AppLogger.i(tag, "Microphone recording was cancelled.")
            return@withContext null
        } catch (e: Exception) {
            AppLogger.e(tag, "Error during audio recording", e)
            return@withContext null
        } finally {
            _isRecordingFlow.value = false
            _amplitudeFlow.value = 0.0f
            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (_: Exception) {}
        }
    }

    private fun buildWav(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteArray(44)

        // RIFF chunk
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0

        // data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val wav = ByteArray(44 + totalAudioLen)
        System.arraycopy(header, 0, wav, 0, 44)
        System.arraycopy(pcmData, 0, wav, 44, totalAudioLen)
        return wav
    }
}
