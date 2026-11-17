package com.pulse.player.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulse.player.data.local.db.entity.AlbumEntity
import com.pulse.player.data.local.db.entity.ArtistEntity
import com.pulse.player.data.local.db.entity.GenreEntity
import com.pulse.player.data.local.db.entity.PlaylistEntity
import com.pulse.player.data.local.db.entity.PlaylistTrackEntity
import com.pulse.player.data.local.db.entity.QueueItemEntity
import com.pulse.player.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("SELECT * FROM albums ORDER BY title COLLATE NOCASE ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumById(id: Long): AlbumEntity?

    @Query("DELETE FROM albums")
    suspend fun clearAlbums()
}

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Query("SELECT * FROM artists ORDER BY name COLLATE NOCASE ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :id LIMIT 1")
    suspend fun getArtistById(id: Long): ArtistEntity?

    @Query("DELETE FROM artists")
    suspend fun clearArtists()
}

@Dao
interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(genres: List<GenreEntity>)

    @Query("SELECT * FROM genres ORDER BY name COLLATE NOCASE ASC")
    fun getAllGenres(): Flow<List<GenreEntity>>

    @Query("DELETE FROM genres")
    suspend fun clearGenres()
}

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(tracks: List<PlaylistTrackEntity>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    @Query("SELECT t.* FROM tracks t INNER JOIN playlist_tracks pt ON t.id = pt.trackId WHERE pt.playlistId = :playlistId ORDER BY pt.orderIndex ASC")
    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackCountForPlaylist(playlistId: Long): Int

    @Query("UPDATE playlists SET trackCount = :count, updatedAt = :updatedAt WHERE id = :playlistId")
    suspend fun updatePlaylistTrackCount(playlistId: Long, count: Int, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setQueue(items: List<QueueItemEntity>)

    @Query("SELECT * FROM queue_items ORDER BY queuePosition ASC")
    fun getPersistedQueue(): Flow<List<QueueItemEntity>>

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()
}
