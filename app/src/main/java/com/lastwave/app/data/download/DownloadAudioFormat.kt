package com.lastwave.app.data.download

/**
 * User-selectable offline audio download format.
 */
enum class DownloadAudioFormat(
    val id: String,
    val displayName: String,
    val extension: String,
    val mimeType: String,
    val explanation: String,
) {
    OPUS(
        id = "opus",
        displayName = "OPUS",
        extension = "opus",
        mimeType = "audio/ogg",
        explanation = "High-efficiency modern format (default). Preserves original stream fidelity from streaming sources with minimal storage space.",
    ),
    MP3(
        id = "mp3",
        displayName = "MP3",
        extension = "mp3",
        mimeType = "audio/mpeg",
        explanation = "Universal compatibility with car stereos, legacy media players, and older devices. Encoded in genuine MPEG-1 Layer 3.",
    ),
    AAC_M4A(
        id = "aac_m4a",
        displayName = "AAC / M4A",
        extension = "m4a",
        mimeType = "audio/mp4",
        explanation = "High-quality Apple / MPEG-4 standard. Streamed directly without transcoding when available, or hardware-encoded via AAC.",
    ),
    FLAC(
        id = "flac",
        displayName = "FLAC",
        extension = "flac",
        mimeType = "audio/flac",
        explanation = "Lossless compression. Recommended for Hi-Res / Lossless sources; transcoded from linear PCM when requested.",
    ),
    ORIGINAL(
        id = "original",
        displayName = "Original source format",
        extension = "",
        mimeType = "",
        explanation = "Saves streams in their native container and codec (Opus, AAC, or FLAC) without any re-encoding.",
    );

    companion object {
        fun fromId(id: String?): DownloadAudioFormat =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OPUS
    }
}

/**
 * Encoding quality preset for offline conversion.
 */
enum class ConversionQuality(
    val id: String,
    val displayName: String,
    val mp3BitrateKbps: Int,
    val aacBitrateKbps: Int,
    val opusBitrateKbps: Int,
    val explanation: String,
) {
    STANDARD(
        id = "standard",
        displayName = "Standard",
        mp3BitrateKbps = 128,
        aacBitrateKbps = 128,
        opusBitrateKbps = 96,
        explanation = "128 kbps MP3/AAC, 96 kbps Opus. Compact file sizes, ideal for limited device storage.",
    ),
    HIGH(
        id = "high",
        displayName = "High",
        mp3BitrateKbps = 192,
        aacBitrateKbps = 192,
        opusBitrateKbps = 128,
        explanation = "192 kbps MP3/AAC, 128 kbps Opus. Balanced high fidelity, recommended for everyday listening.",
    ),
    MAXIMUM(
        id = "maximum",
        displayName = "Maximum",
        mp3BitrateKbps = 320,
        aacBitrateKbps = 256,
        opusBitrateKbps = 160,
        explanation = "320 kbps MP3, 256 kbps AAC, 160 kbps Opus, FLAC bit-exact. Note: Transcoding from lossy streams does not restore original lost fidelity.",
    );

    companion object {
        fun fromId(id: String?): ConversionQuality =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: HIGH
    }
}
