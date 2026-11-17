package com.pulse.player.data.remote.lrclib.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LrclibResponse(
    @SerialName("id") val id: Long? = null,
    @SerialName("trackName") val trackName: String? = null,
    @SerialName("artistName") val artistName: String? = null,
    @SerialName("albumName") val albumName: String? = null,
    @SerialName("duration") val duration: Double? = null,
    @SerialName("instrumental") val instrumental: Boolean = false,
    @SerialName("plainLyrics") val plainLyrics: String? = null,
    @SerialName("syncedLyrics") val syncedLyrics: String? = null
)
