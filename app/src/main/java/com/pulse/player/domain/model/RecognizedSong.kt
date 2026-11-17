package com.pulse.player.domain.model

data class RecognizedSong(
    val title: String,
    val artist: String,
    val album: String,
    val releaseDate: String? = null,
    val label: String? = null,
    val artworkUrl: String? = null,
    val songLink: String? = null,
    val localTrackMatch: Track? = null
)
