package com.pulse.player.domain.model

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int,
    val artworkUri: String? = null
)

data class Artist(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val albumCount: Int
)

data class Genre(
    val id: Long,
    val name: String,
    val trackCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AudioFolder(
    val path: String,
    val name: String,
    val trackCount: Int
)
