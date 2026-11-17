package com.pulse.player.data.repository

import com.pulse.player.data.local.db.dao.AlbumDao
import com.pulse.player.data.local.db.dao.ArtistDao
import com.pulse.player.data.local.db.dao.PlaylistDao
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.data.local.mediastore.MediaStoreScanner
import com.pulse.player.data.local.mediastore.ScanResult
import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.AudioFolder
import com.pulse.player.domain.model.Playlist
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LibraryRepository
import com.pulse.player.domain.repository.TrackSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao,
    private val playlistDao: PlaylistDao,
    private val scanner: MediaStoreScanner
) : LibraryRepository {

    override fun getTracks(sortOrder: TrackSortOrder): Flow<List<Track>> {
        val entityFlow = when (sortOrder) {
            TrackSortOrder.TITLE_ASC -> trackDao.getAllTracksSortedByTitleAsc()
            TrackSortOrder.TITLE_DESC -> trackDao.getAllTracksSortedByTitleDesc()
            TrackSortOrder.ARTIST -> trackDao.getAllTracksSortedByArtist()
            TrackSortOrder.ALBUM -> trackDao.getAllTracksSortedByAlbum()
            TrackSortOrder.DATE_ADDED -> trackDao.getAllTracksSortedByDateAdded()
            TrackSortOrder.DURATION -> trackDao.getAllTracksSortedByDuration()
            TrackSortOrder.PLAY_COUNT -> trackDao.getAllTracksSortedByPlayCount()
        }
        return entityFlow.map { list -> list.map { it.toDomain() } }
    }

    override fun getFavorites(): Flow<List<Track>> {
        return trackDao.getFavoriteTracks().map { list -> list.map { it.toDomain() } }
    }

    override fun getAlbums(): Flow<List<Album>> {
        return albumDao.getAllAlbums().map { list -> list.map { it.toDomain() } }
    }

    override fun getArtists(): Flow<List<Artist>> {
        return artistDao.getAllArtists().map { list -> list.map { it.toDomain() } }
    }

    override fun getFolders(): Flow<List<AudioFolder>> {
        return trackDao.getAllTracksSortedByTitleAsc().map { tracks ->
            tracks.groupBy { File(it.path).parent ?: "/storage" }
                .map { (folderPath, tracksInFolder) ->
                    AudioFolder(
                        path = folderPath,
                        name = File(folderPath).name.ifEmpty { "Root" },
                        trackCount = tracksInFolder.size
                    )
                }
                .sortedBy { it.name.lowercase() }
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { list -> list.map { it.toDomain() } }
    }

    override fun getTracksInFolder(folderPath: String): Flow<List<Track>> {
        return trackDao.getTracksInFolder(folderPath).map { list -> list.map { it.toDomain() } }
    }

    override fun getTracksForAlbum(albumId: Long): Flow<List<Track>> {
        return trackDao.getTracksByAlbum(albumId).map { list -> list.map { it.toDomain() } }
    }

    override fun getTracksForArtist(artistId: Long): Flow<List<Track>> {
        return trackDao.getTracksByArtist(artistId).map { list -> list.map { it.toDomain() } }
    }

    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun toggleFavorite(trackId: String, isFavorite: Boolean) {
        trackDao.updateFavorite(trackId, isFavorite)
    }

    override suspend fun rescanLibrary(): ScanResult {
        return scanner.scanLibrary()
    }
}
