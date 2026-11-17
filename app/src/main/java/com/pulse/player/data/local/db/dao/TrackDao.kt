package com.pulse.player.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pulse.player.data.local.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun getAllTracksSortedByTitleAsc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE DESC")
    fun getAllTracksSortedByTitleDesc(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY artist COLLATE NOCASE ASC, title COLLATE NOCASE ASC")
    fun getAllTracksSortedByArtist(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY album COLLATE NOCASE ASC, trackNumber ASC")
    fun getAllTracksSortedByAlbum(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getAllTracksSortedByDateAdded(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY durationMs DESC")
    fun getAllTracksSortedByDuration(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks ORDER BY playCount DESC")
    fun getAllTracksSortedByPlayCount(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getTrackById(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    fun observeTrackById(id: String): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE albumId = :albumId ORDER BY trackNumber ASC, title ASC")
    fun getTracksByAlbum(albumId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artistId = :artistId ORDER BY year DESC, title ASC")
    fun getTracksByArtist(artistId: Long): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE path LIKE :folderPrefix || '%' ORDER BY title ASC")
    fun getTracksInFolder(folderPrefix: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchTracks(query: String): Flow<List<TrackEntity>>

    @Query("UPDATE tracks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: String)

    @Query("UPDATE tracks SET title = :title, artist = :artist, album = :album, year = :year, genre = :genre, trackNumber = :trackNumber, artworkUri = :artworkUri WHERE id = :id")
    suspend fun updateTrackTags(
        id: String,
        title: String,
        artist: String,
        album: String,
        year: Int,
        genre: String,
        trackNumber: Int,
        artworkUri: String?
    )

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: String)

    @Query("DELETE FROM tracks WHERE id IN (:ids)")
    suspend fun deleteTracksByIds(ids: List<String>)

    @Query("SELECT id, path, dateModified, size FROM tracks")
    suspend fun getAllTrackSyncHeaders(): List<TrackSyncHeader>

    @Query("DELETE FROM tracks WHERE path NOT IN (:activePaths)")
    suspend fun deleteOrphanedTracks(activePaths: List<String>)

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksSync(): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1")
    suspend fun getFavoriteTracksSync(): List<TrackEntity>
}

data class TrackSyncHeader(
    val id: String,
    val path: String,
    val dateModified: Long,
    val size: Long
)
