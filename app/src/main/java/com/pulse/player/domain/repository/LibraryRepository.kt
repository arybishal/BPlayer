package com.pulse.player.domain.repository

import com.pulse.player.data.local.mediastore.ScanResult
import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.AudioFolder
import com.pulse.player.domain.model.Playlist
import com.pulse.player.domain.model.Track
import kotlinx.coroutines.flow.Flow

enum class TrackSortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST,
    ALBUM,
    DATE_ADDED,
    DURATION,
    PLAY_COUNT
}

interface LibraryRepository {
    fun getTracks(sortOrder: TrackSortOrder = TrackSortOrder.TITLE_ASC): Flow<List<Track>>
    fun getFavorites(): Flow<List<Track>>
    fun getAlbums(): Flow<List<Album>>
    fun getArtists(): Flow<List<Artist>>
    fun getFolders(): Flow<List<AudioFolder>>
    fun getPlaylists(): Flow<List<Playlist>>
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)
    suspend fun deleteTrack(trackId: String)
    fun getTracksInFolder(folderPath: String): Flow<List<Track>>
    fun getTracksForAlbum(albumId: Long): Flow<List<Track>>
    fun getTracksForArtist(artistId: Long): Flow<List<Track>>
    fun searchTracks(query: String): Flow<List<Track>>
    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean)
    suspend fun rescanLibrary(): ScanResult
}
