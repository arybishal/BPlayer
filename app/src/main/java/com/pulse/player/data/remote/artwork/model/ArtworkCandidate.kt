package com.pulse.player.data.remote.artwork.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class ArtworkCandidate(
    val imageUrl: String,
    val source: String,
    val albumTitle: String,
    val artistName: String
)

@Serializable
data class ItunesSearchResponse(
    @SerialName("resultCount") val resultCount: Int = 0,
    @SerialName("results") val results: List<ItunesAlbumItem> = emptyList()
)

@Serializable
data class ItunesAlbumItem(
    @SerialName("collectionName") val collectionName: String? = null,
    @SerialName("artistName") val artistName: String? = null,
    @SerialName("artworkUrl100") val artworkUrl100: String? = null
) {
    val highResArtworkUrl: String?
        get() = artworkUrl100?.replace("100x100bb", "600x600bb")
}

@Serializable
data class DeezerSearchResponse(
    @SerialName("total") val total: Int = 0,
    @SerialName("data") val data: List<DeezerAlbumItem> = emptyList()
)

@Serializable
data class DeezerAlbumItem(
    @SerialName("title") val title: String? = null,
    @SerialName("artist") val artist: DeezerArtistItem? = null,
    @SerialName("cover_big") val coverBig: String? = null,
    @SerialName("cover_xl") val coverXl: String? = null
)

@Serializable
data class DeezerArtistItem(
    @SerialName("name") val name: String? = null
)
