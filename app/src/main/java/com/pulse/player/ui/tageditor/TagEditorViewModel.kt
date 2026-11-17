package com.pulse.player.ui.tageditor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.tags.AudioTagEditor
import com.pulse.player.data.local.tags.EditableSongTags
import com.pulse.player.data.local.tags.TagUpdateResult
import com.pulse.player.data.remote.artwork.ArtworkSearchService
import com.pulse.player.data.remote.artwork.model.ArtworkCandidate
import com.pulse.player.domain.model.Track
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

data class TagEditorUiState(
    val trackId: String = "",
    val path: String = "",
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: String = "",
    val genre: String = "",
    val trackNumber: String = "",
    val artworkUri: String? = null,
    val newArtworkBytes: ByteArray? = null,
    val isSaving: Boolean = false,
    val isSearchingArtwork: Boolean = false,
    val artworkCandidates: List<ArtworkCandidate> = emptyList(),
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TagEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tagEditor: AudioTagEditor,
    private val artworkSearchService: ArtworkSearchService,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagEditorUiState())
    val uiState: StateFlow<TagEditorUiState> = _uiState.asStateFlow()

    fun initialize(track: Track) {
        _uiState.value = TagEditorUiState(
            trackId = track.id,
            path = track.path,
            title = track.title,
            artist = track.artist,
            album = track.album,
            year = if (track.year > 0) track.year.toString() else "",
            genre = track.genre,
            trackNumber = if (track.trackNumber > 0) track.trackNumber.toString() else "",
            artworkUri = track.artworkUri
        )
    }

    fun onTitleChange(value: String) { _uiState.value = _uiState.value.copy(title = value) }
    fun onArtistChange(value: String) { _uiState.value = _uiState.value.copy(artist = value) }
    fun onAlbumChange(value: String) { _uiState.value = _uiState.value.copy(album = value) }
    fun onYearChange(value: String) { _uiState.value = _uiState.value.copy(year = value) }
    fun onGenreChange(value: String) { _uiState.value = _uiState.value.copy(genre = value) }
    fun onTrackNumberChange(value: String) { _uiState.value = _uiState.value.copy(trackNumber = value) }

    fun searchOnlineArtwork() {
        val query = "${_uiState.value.album} ${_uiState.value.artist}".trim().ifEmpty {
            _uiState.value.title
        }
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingArtwork = true, errorMessage = null)
            val results = artworkSearchService.searchArtwork(query)
            _uiState.value = _uiState.value.copy(
                isSearchingArtwork = false,
                artworkCandidates = results,
                errorMessage = if (results.isEmpty()) "No online artwork found for \"$query\"" else null
            )
        }
    }

    fun selectCandidateArtwork(candidate: ArtworkCandidate) {
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(candidate.imageUrl).build()
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful) response.body?.bytes() else null
                }
                if (bytes != null) {
                    _uiState.value = _uiState.value.copy(
                        newArtworkBytes = bytes,
                        artworkUri = candidate.imageUrl,
                        artworkCandidates = emptyList()
                    )
                }
            } catch (e: Exception) {
                AppLogger.d("TagEditorViewModel", "Failed to download artwork: ${e.message}")
            }
        }
    }

    fun setGalleryArtwork(uri: Uri) {
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                if (bytes != null) {
                    _uiState.value = _uiState.value.copy(
                        newArtworkBytes = bytes,
                        artworkUri = uri.toString()
                    )
                }
            } catch (e: Exception) {
                AppLogger.d("TagEditorViewModel", "Failed to read gallery image: ${e.message}")
            }
        }
    }

    fun removeArtwork() {
        _uiState.value = _uiState.value.copy(
            artworkUri = null,
            newArtworkBytes = ByteArray(0)
        )
    }

    fun saveTags(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Track title cannot be blank")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)
            val result = tagEditor.updateSongTags(
                EditableSongTags(
                    trackId = state.trackId,
                    path = state.path,
                    title = state.title,
                    artist = state.artist,
                    album = state.album,
                    year = state.year.toIntOrNull() ?: 0,
                    genre = state.genre,
                    trackNumber = state.trackNumber.toIntOrNull() ?: 0,
                    existingArtworkUri = state.artworkUri,
                    newArtworkBytes = state.newArtworkBytes
                )
            )

            when (result) {
                is TagUpdateResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                    onSuccess()
                }
                is TagUpdateResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }
}
