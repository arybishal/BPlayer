package com.pulse.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.domain.model.Lyrics
import com.pulse.player.domain.model.LyricsSource
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.repository.LyricsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LyricsUiState(
    val lyrics: Lyrics? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val lyricsRepository: LyricsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LyricsUiState())
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    private var currentTrackId: String? = null

    fun loadLyricsForTrack(track: Track) {
        if (currentTrackId == track.id && _uiState.value.lyrics != null) return
        currentTrackId = track.id

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val localLyrics = lyricsRepository.getLyricsForTrack(track)
            if (localLyrics != null) {
                _uiState.value = LyricsUiState(lyrics = localLyrics, isLoading = false)
            } else {
                _uiState.value = LyricsUiState(lyrics = null, isLoading = false)
            }
        }
    }

    fun searchOnline(track: Track) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val remote = lyricsRepository.fetchOnlineLyrics(track)
            if (remote != null) {
                _uiState.value = LyricsUiState(lyrics = remote, isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No lyrics found online for \"${track.title}\"."
                )
            }
        }
    }

    fun saveCustomLyrics(trackId: String, text: String, isSynced: Boolean) {
        viewModelScope.launch {
            lyricsRepository.saveLyrics(
                trackId = trackId,
                lyricsText = text,
                isSynced = isSynced,
                source = LyricsSource.USER_MANUAL
            )
            // Reload updated lyrics
            val updated = lyricsRepository.observeLyricsForTrack(trackId)
            // Or directly update state
            val parsed = com.pulse.player.util.lrc.LrcParser.parse(trackId, text, LyricsSource.USER_MANUAL)
            _uiState.value = LyricsUiState(lyrics = parsed, isLoading = false)
        }
    }
}
