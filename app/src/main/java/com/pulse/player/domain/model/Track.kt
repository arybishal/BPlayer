package com.pulse.player.domain.model

data class Track(
    val id: String,
    val mediaStoreId: Long = 0L,
    val title: String,
    val artist: String,
    val artistId: Long = 0L,
    val album: String,
    val albumId: Long = 0L,
    val durationMs: Long,
    val path: String,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val mimeType: String = "audio/mpeg",
    val bitRate: Int = 320000,
    val sampleRate: Int = 44100,
    val bitDepth: Int = 16,
    val dateAdded: Long = 0L,
    val dateModified: Long = 0L,
    val size: Long = 0L,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val genre: String = "Unknown",
    val artworkUri: String? = null
) {
    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val formatBadge: String
        get() {
            val ext = path.substringAfterLast('.', "").uppercase()
            return when {
                sampleRate >= 88200 || bitDepth >= 24 -> "HI-RES"
                ext in listOf("FLAC", "ALAC", "WAV", "APE", "WV", "DSD") -> ext
                ext == "M4A" -> "AAC"
                ext.isNotEmpty() -> ext
                else -> "AUDIO"
            }
        }
}
