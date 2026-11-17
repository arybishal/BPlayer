package com.pulse.player.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.domain.model.Album
import com.pulse.player.domain.model.Artist
import com.pulse.player.domain.model.Playlist
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val matchingTracks: List<Track> = emptyList(),
    val matchingAlbums: List<Album> = emptyList(),
    val matchingArtists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: LibraryRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val debouncedQuery = _query
        .debounce(300)
        .distinctUntilChanged()

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        debouncedQuery.flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else repository.searchTracks(q)
        },
        debouncedQuery.flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else repository.getAlbums()
        },
        debouncedQuery.flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else repository.getArtists()
        },
        repository.getPlaylists()
    ) { currentQ, tracks, albums, artists, playlists ->
        val filteredAlbums = if (currentQ.isBlank()) emptyList() else albums.filter {
            it.title.contains(currentQ, ignoreCase = true) || it.artist.contains(currentQ, ignoreCase = true)
        }
        val filteredArtists = if (currentQ.isBlank()) emptyList() else artists.filter {
            it.name.contains(currentQ, ignoreCase = true)
        }

        SearchUiState(
            query = currentQ,
            matchingTracks = tracks,
            matchingAlbums = filteredAlbums,
            matchingArtists = filteredArtists,
            playlists = playlists,
            isSearching = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, !track.isFavorite)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun createPlaylist(name: String, trackId: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createPlaylist(name)
            repository.addTrackToPlaylist(id, trackId)
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            repository.deleteTrack(track.id)
        }
    }
}
