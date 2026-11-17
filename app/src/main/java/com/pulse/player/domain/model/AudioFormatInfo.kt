package com.pulse.player.domain.model

data class AudioFormatInfo(
    val extension: String,
    val mimeType: String,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val bitRateBps: Int,
    val isHiRes: Boolean,
    val isLossless: Boolean,
    val isDsd: Boolean,
    val badgeText: String,
    val detailedBadgeText: String,
    val dsdDecimationRate: String? = null
)

object AudioFormatInspector {

    fun inspect(path: String, mimeType: String, sampleRate: Int, bitDepth: Int, bitRate: Int): AudioFormatInfo {
        val ext = path.substringAfterLast('.', "").uppercase()
        val isDsd = ext in listOf("DSF", "DFF") || mimeType.contains("dsd", ignoreCase = true)
        val isLossless = isDsd || ext in listOf("FLAC", "ALAC", "WAV", "APE", "WV", "AIFF", "AIF", "TTA")
        val isHiRes = sampleRate >= 88200 || bitDepth >= 24 || isDsd

        val primaryBadge = when {
            isDsd -> "DSD"
            isHiRes -> "HI-RES"
            isLossless -> ext.ifEmpty { "LOSSLESS" }
            ext == "M4A" -> "AAC"
            ext.isNotEmpty() -> ext
            else -> "AUDIO"
        }

        val dsdDecimation = if (isDsd) "Decimated to 24-bit / 88.2kHz PCM" else null

        val rateKhz = when {
            isDsd -> "88.2kHz (DSD64 Decimated)"
            sampleRate > 0 -> "%.1fkHz".format(sampleRate / 1000f)
            else -> ""
        }
        val depthStr = when {
            isDsd -> "24-bit"
            bitDepth > 0 -> "${bitDepth}-bit"
            else -> ""
        }
        val detail = listOfNotNull(
            primaryBadge,
            depthStr.ifEmpty { null },
            rateKhz.ifEmpty { null }
        ).joinToString(" • ")

        return AudioFormatInfo(
            extension = ext,
            mimeType = mimeType,
            sampleRateHz = if (isDsd && sampleRate == 0) 88200 else sampleRate,
            bitDepth = if (isDsd && bitDepth == 0) 24 else bitDepth,
            bitRateBps = bitRate,
            isHiRes = isHiRes,
            isLossless = isLossless,
            isDsd = isDsd,
            badgeText = primaryBadge,
            detailedBadgeText = detail,
            dsdDecimationRate = dsdDecimation
        )
    }

    fun isFormatSupported(path: String): Boolean {
        val ext = path.substringAfterLast('.', "").lowercase()
        val supportedExtensions = setOf(
            "mp3", "m4a", "aac", "flac", "wav", "ogg", "oga", "opus",
            "ape", "wv", "aiff", "aif", "dsf", "dff", "wma", "mpc", "tta"
        )
        return ext in supportedExtensions
    }
}

val Track.formatInfo: AudioFormatInfo
    get() = AudioFormatInspector.inspect(path, mimeType, sampleRate, bitDepth, bitRate)

