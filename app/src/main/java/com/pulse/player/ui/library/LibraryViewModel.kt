package com.pulse.player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.mediastore.ScanResult
import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.AudioFolder
import com.pulse.player.domain.model.Playlist
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LibraryRepository
import com.pulse.player.domain.repository.TrackSortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val selectedTab: Int = 0,
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<AudioFolder> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val sortOrder: TrackSortOrder = TrackSortOrder.TITLE_ASC,
    val isGridView: Boolean = true,
    val isScanning: Boolean = false,
    val scanMessage: String? = null
)

private data class LibraryContent(
    val tracks: List<Track>,
    val albums: List<Album>,
    val artists: List<Artist>,
    val folders: List<AudioFolder>,
    val playlists: List<Playlist>
)

private data class ViewPreferences(
    val selectedTab: Int,
    val sortOrder: TrackSortOrder,
    val isGridView: Boolean,
    val isScanning: Boolean,
    val scanMessage: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    private val selectedTab = MutableStateFlow(0)
    private val sortOrder = MutableStateFlow(TrackSortOrder.TITLE_ASC)
    private val isGridView = MutableStateFlow(true)
    private val isScanning = MutableStateFlow(false)
    private val scanMessage = MutableStateFlow<String?>(null)

    private val tracksFlow = sortOrder.flatMapLatest { order ->
        repository.getTracks(order)
    }

    private val libraryContentFlow = combine(
        tracksFlow,
        repository.getAlbums(),
        repository.getArtists(),
        repository.getFolders(),
        repository.getPlaylists()
    ) { tracks, albums, artists, folders, playlists ->
        LibraryContent(tracks, albums, artists, folders, playlists)
    }

    private val viewPreferencesFlow = combine(
        selectedTab,
        sortOrder,
        isGridView,
        isScanning,
        scanMessage
    ) { tab, sort, grid, scan, msg ->
        ViewPreferences(tab, sort, grid, scan, msg)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        libraryContentFlow,
        viewPreferencesFlow
    ) { content, prefs ->
        LibraryUiState(
            selectedTab = prefs.selectedTab,
            tracks = content.tracks,
            albums = content.albums,
            artists = content.artists,
            folders = content.folders,
            playlists = content.playlists,
            sortOrder = prefs.sortOrder,
            isGridView = prefs.isGridView,
            isScanning = prefs.isScanning,
            scanMessage = prefs.scanMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState(isScanning = true)
    )

    init {
        triggerLibraryScan()
    }

    fun setSelectedTab(tab: Int) {
        selectedTab.value = tab
    }

    fun setSortOrder(order: TrackSortOrder) {
        sortOrder.value = order
    }

    fun toggleGridView() {
        isGridView.value = !isGridView.value
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, !track.isFavorite)
        }
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            repository.deleteTrack(track.id)
        }
    }

    fun triggerLibraryScan() {
        viewModelScope.launch {
            isScanning.value = true
            scanMessage.value = "Scanning music library..."
            val result = repository.rescanLibrary()
            isScanning.value = false
            scanMessage.value = when (result) {
                is ScanResult.Success -> "${result.totalCount} tracks synchronized."
                is ScanResult.Error -> "Scan error: ${result.message}"
            }
        }
    }
}
