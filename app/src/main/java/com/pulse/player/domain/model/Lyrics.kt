package com.pulse.player.domain.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

enum class LyricsSource {
    EMBEDDED,
    SIDECAR_LRC,
    LRCLIB_REMOTE,
    GEMINI_AI,
    USER_MANUAL
}

data class Lyrics(
    val trackId: String,
    val lines: List<LyricLine>,
    val plainText: String,
    val isSynchronized: Boolean,
    val source: LyricsSource
)
