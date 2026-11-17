package com.pulse.player.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pulse.player.domain.model.Track

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["path"], unique = true),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["title"]),
        Index(value = ["dateAdded"]),
        Index(value = ["isFavorite"])
    ]
)
data class TrackEntity(
    @PrimaryKey
    val id: String,
    val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val artistId: Long,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val path: String,
    val trackNumber: Int,
    val year: Int,
    val mimeType: String,
    val bitRate: Int,
    val sampleRate: Int,
    val bitDepth: Int,
    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val genre: String = "Unknown",
    val artworkUri: String? = null
) {
    fun toDomain(): Track = Track(
        id = id,
        mediaStoreId = mediaStoreId,
        title = title,
        artist = artist,
        artistId = artistId,
        album = album,
        albumId = albumId,
        durationMs = durationMs,
        path = path,
        trackNumber = trackNumber,
        year = year,
        mimeType = mimeType,
        bitRate = bitRate,
        sampleRate = sampleRate,
        bitDepth = bitDepth,
        dateAdded = dateAdded,
        dateModified = dateModified,
        size = size,
        playCount = playCount,
        isFavorite = isFavorite,
        genre = genre,
        artworkUri = artworkUri
    )

    companion object {
        fun fromDomain(track: Track): TrackEntity = TrackEntity(
            id = track.id,
            mediaStoreId = track.mediaStoreId,
            title = track.title,
            artist = track.artist,
            artistId = track.artistId,
            album = track.album,
            albumId = track.albumId,
            durationMs = track.durationMs,
            path = track.path,
            trackNumber = track.trackNumber,
            year = track.year,
            mimeType = track.mimeType,
            bitRate = track.bitRate,
            sampleRate = track.sampleRate,
            bitDepth = track.bitDepth,
            dateAdded = track.dateAdded,
            dateModified = track.dateModified,
            size = track.size,
            playCount = track.playCount,
            isFavorite = track.isFavorite,
            genre = track.genre,
            artworkUri = track.artworkUri
        )
    }
}
