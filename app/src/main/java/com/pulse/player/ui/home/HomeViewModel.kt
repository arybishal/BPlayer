package com.pulse.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LibraryRepository
import com.pulse.player.domain.repository.TrackSortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val trackCount: Int = 0,
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val playlistCount: Int = 0,
    val allTracks: List<Track> = emptyList(),
    val recentTracks: List<Track> = emptyList(),
    val favorites: List<Track> = emptyList(),
    val isScanning: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.getTracks(TrackSortOrder.DATE_ADDED),
                libraryRepository.getAlbums(),
                libraryRepository.getArtists(),
                libraryRepository.getPlaylists(),
                libraryRepository.getFavorites()
            ) { tracks, albums, artists, playlists, favorites ->
                HomeUiState(
                    trackCount = tracks.size,
                    albumCount = albums.size,
                    artistCount = artists.size,
                    playlistCount = playlists.size,
                    allTracks = tracks,
                    recentTracks = tracks.take(12),
                    favorites = favorites.take(12),
                    isScanning = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun rescan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            libraryRepository.rescanLibrary()
            _uiState.update { it.copy(isScanning = false) }
        }
    }
}
