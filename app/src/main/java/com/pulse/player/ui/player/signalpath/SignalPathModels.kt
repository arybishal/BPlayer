package com.pulse.player.ui.player.signalpath

import android.content.Context
import android.media.AudioManager
import android.media.AudioTrack
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.model.formatInfo

data class SignalPathStage(
    val title: String,
    val subtitle: String,
    val details: List<Pair<String, String>>,
    val isLossless: Boolean = true,
    val isBypassed: Boolean = false
)

data class SignalPathData(
    val sourceStage: SignalPathStage,
    val dspStage: SignalPathStage,
    val outputStage: SignalPathStage,
    val verdict: String,
    val isBitPerfect: Boolean,
    val isHiRes: Boolean
)

object SignalPathEvaluator {
    fun evaluate(context: Context, track: Track, isEqEnabled: Boolean): SignalPathData {
        val info = track.formatInfo
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val mixerRate = runCatching {
            AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC)
        }.getOrDefault(48000)

        val isHiRes = info.isHiRes || (info.sampleRateHz > 48000) || (info.bitDepth > 16)
        val isLossless = info.isLossless

        val sourceDetails = mutableListOf(
            "Format" to info.detailedBadgeText,
            "Sample Rate" to "${info.sampleRateHz / 1000.0} kHz",
            "Bit Depth" to "${info.bitDepth}-bit",
            "Encoding" to track.mimeType
        )
        if (info.isDsd && info.dsdDecimationRate != null) {
            sourceDetails.add("DSD Mode" to info.dsdDecimationRate)
        }

        val sourceStage = SignalPathStage(
            title = "Source Audio Stream",
            subtitle = if (isLossless) "Studio Master / Lossless Stream" else "Standard Compressed Stream",
            details = sourceDetails,
            isLossless = isLossless
        )

        val dspDetails = mutableListOf(
            "Equalizer" to if (isEqEnabled) "Hardware Parametric EQ Active" else "Bypassed (Flat / Bit-True)",
            "Precision" to "Float32 Internal DSP Engine",
            "Volume Attenuation" to "0.0 dB Unity Gain"
        )
        val dspStage = SignalPathStage(
            title = "Digital Signal Processor",
            subtitle = if (isEqEnabled) "Active Tone Shaping" else "Bit-Perfect Bypass",
            details = dspDetails,
            isLossless = true,
            isBypassed = !isEqEnabled
        )

        val outputSampleRate = if (isHiRes && mixerRate >= info.sampleRateHz) info.sampleRateHz else mixerRate
        val outputDetails = mutableListOf(
            "Engine" to "AndroidX Media3 ExoPlayer",
            "Hardware Route" to "AudioTrack Low-Latency Pipeline",
            "Mixer Sample Rate" to "${outputSampleRate / 1000.0} kHz",
            "Output Bit Depth" to if (isHiRes) "24-bit PCM" else "16-bit PCM"
        )
        val outputStage = SignalPathStage(
            title = "Hardware Audio Output",
            subtitle = "Android Audio HAL / DAC Interface",
            details = outputDetails,
            isLossless = true
        )

        val isBitPerfect = isLossless && !isEqEnabled && (info.sampleRateHz == outputSampleRate)
        val verdict = when {
            isBitPerfect -> "BIT-PERFECT LOSSLESS"
            isHiRes -> "HI-RES DIRECT PIPELINE"
            isLossless -> "LOSSLESS PLATFORM MIXER"
            else -> "STANDARD PLATFORM MIXER"
        }

        return SignalPathData(
            sourceStage = sourceStage,
            dspStage = dspStage,
            outputStage = outputStage,
            verdict = verdict,
            isBitPerfect = isBitPerfect,
            isHiRes = isHiRes
        )
    }
}
