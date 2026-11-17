package com.pulse.player.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.Genre
import com.pulse.player.domain.model.Playlist

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"])
    ]
)
data class AlbumEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val year: Int,
    val artworkUri: String? = null
) {
    fun toDomain(): Album = Album(
        id = id,
        title = title,
        artist = artist,
        trackCount = trackCount,
        year = year,
        artworkUri = artworkUri
    )
}

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class ArtistEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val trackCount: Int,
    val albumCount: Int
) {
    fun toDomain(): Artist = Artist(
        id = id,
        name = name,
        trackCount = trackCount,
        albumCount = albumCount
    )
}

@Entity(
    tableName = "genres",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class GenreEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val trackCount: Int
) {
    fun toDomain(): Genre = Genre(
        id = id,
        name = name,
        trackCount = trackCount
    )
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Playlist = Playlist(
        id = id,
        name = name,
        trackCount = trackCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["trackId"])
    ]
)
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: String,
    val orderIndex: Int
)

@Entity(
    tableName = "queue_items",
    indices = [
        Index(value = ["queuePosition"], unique = true)
    ]
)
data class QueueItemEntity(
    @PrimaryKey
    val queuePosition: Int,
    val trackId: String,
    val queuedTimestamp: Long = System.currentTimeMillis()
)
