package com.pulse.player.data.remote.audd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuddResponse(
    val status: String,
    val result: AuddResult? = null,
    val error: AuddError? = null
)

@Serializable
data class AuddError(
    @SerialName("error_code") val errorCode: Int? = null,
    @SerialName("error_message") val errorMessage: String? = null
)

@Serializable
data class AuddResult(
    val artist: String? = null,
    val title: String? = null,
    val album: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val label: String? = null,
    val timecode: String? = null,
    @SerialName("song_link") val songLink: String? = null,
    val spotify: AuddSpotify? = null,
    @SerialName("apple_music") val appleMusic: AuddAppleMusic? = null
)

@Serializable
data class AuddSpotify(
    val album: AuddSpotifyAlbum? = null
)

@Serializable
data class AuddSpotifyAlbum(
    val name: String? = null,
    val images: List<AuddSpotifyImage> = emptyList()
)

@Serializable
data class AuddSpotifyImage(
    val url: String? = null,
    val height: Int? = null,
    val width: Int? = null
)

@Serializable
data class AuddAppleMusic(
    val artwork: AuddArtwork? = null
)

@Serializable
data class AuddArtwork(
    val url: String? = null
)
