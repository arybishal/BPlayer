package com.pulse.player.ui.recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.domain.repository.RecognitionState
import com.pulse.player.domain.repository.SongRecognitionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SongRecognitionUiState(
    val state: RecognitionState = RecognitionState.Idle,
    val hasPermission: Boolean = false,
    val recordingCountdown: Int = 6,
    val amplitude: Float = 0.0f
)

@HiltViewModel
class SongRecognitionViewModel @Inject constructor(
    private val repository: SongRecognitionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SongRecognitionUiState())
    val uiState: StateFlow<SongRecognitionUiState> = _uiState.asStateFlow()

    private var recognitionJob: Job? = null
    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            repository.amplitudeFlow.collect { amp ->
                _uiState.update { it.copy(amplitude = amp) }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted && _uiState.value.state is RecognitionState.Idle) {
            startListening()
        }
    }

    fun startListening() {
        recognitionJob?.cancel()
        countdownJob?.cancel()

        _uiState.update {
            it.copy(
                state = RecognitionState.Recording(secondsRemaining = 6),
                recordingCountdown = 6
            )
        }

        countdownJob = viewModelScope.launch {
            for (i in 6 downTo 1) {
                _uiState.update {
                    it.copy(
                        recordingCountdown = i,
                        state = if (it.state is RecognitionState.Recording) {
                            RecognitionState.Recording(secondsRemaining = i)
                        } else it.state
                    )
                }
                delay(1000)
            }
            if (isActive && _uiState.value.state is RecognitionState.Recording) {
                _uiState.update { it.copy(state = RecognitionState.Analyzing) }
            }
        }

        recognitionJob = viewModelScope.launch {
            val result = repository.recognizeFromMicrophone(durationSeconds = 6)
            countdownJob?.cancel()
            _uiState.update { it.copy(state = result) }
        }
    }

    fun cancelListening() {
        recognitionJob?.cancel()
        countdownJob?.cancel()
        _uiState.update { it.copy(state = RecognitionState.Idle, amplitude = 0.0f) }
    }

    fun reset() {
        cancelListening()
    }
}
