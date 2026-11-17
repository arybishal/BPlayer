package com.pulse.player.ui.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.domain.model.backup.BackupManifest
import com.pulse.player.domain.model.backup.RestoreResult
import com.pulse.player.domain.model.backup.RestoreSummary
import com.pulse.player.domain.repository.BackupRepository
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class BackupRestoreUiState(
    val isExporting: Boolean = false,
    val exportSuccessManifest: BackupManifest? = null,
    val exportError: String? = null,
    val isInspecting: Boolean = false,
    val restoreSummary: RestoreSummary? = null,
    val inspectionError: String? = null,
    val isRestoring: Boolean = false,
    val restoreResult: RestoreResult? = null,
    val restoreError: String? = null,
    val lastBackupTimestamp: Long? = null,
    val lastBackupName: String? = null,
    val selectedImportUri: Uri? = null,
    val requiresPasswordPrompt: Boolean = false
)

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupRepository: BackupRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val tag = "BackupRestoreViewModel"

    private val _state = MutableStateFlow(BackupRestoreUiState())
    private var currentSessionPassword: String? = null

    val uiState: StateFlow<BackupRestoreUiState> = combine(
        _state,
        preferencesManager.lastBackupTimestampFlow,
        preferencesManager.lastBackupNameFlow
    ) { baseState, lastTimestamp, lastName ->
        baseState.copy(
            lastBackupTimestamp = lastTimestamp,
            lastBackupName = lastName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BackupRestoreUiState()
    )

    fun exportToUri(uri: Uri, password: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isExporting = true,
                exportError = null,
                exportSuccessManifest = null
            )

            withContext(Dispatchers.IO) {
                try {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                        ?: throw IllegalStateException("Could not open output stream for selected file location")

                    outputStream.use { os ->
                        val passwordChars = if (!password.isNullOrBlank()) password.toCharArray() else null
                        val result = backupRepository.exportBackup(os, passwordChars)

                        result.onSuccess { manifest ->
                            _state.value = _state.value.copy(
                                isExporting = false,
                                exportSuccessManifest = manifest,
                                exportError = null
                            )
                        }.onFailure { error ->
                            AppLogger.e(tag, "Backup export failed", error)
                            _state.value = _state.value.copy(
                                isExporting = false,
                                exportError = error.localizedMessage ?: "Export failed unexpectedly"
                            )
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e(tag, "Backup export I/O error", e)
                    _state.value = _state.value.copy(
                        isExporting = false,
                        exportError = e.localizedMessage ?: "Failed to write backup file"
                    )
                }
            }
        }
    }

    fun onFileSelectedForRestore(uri: Uri) {
        _state.value = _state.value.copy(
            selectedImportUri = uri,
            inspectionError = null,
            restoreSummary = null,
            requiresPasswordPrompt = false
        )
        // Inspect without password first to check if credentials are encrypted
        inspectBackup(uri, password = null)
    }

    fun inspectBackup(uri: Uri, password: String?) {
        currentSessionPassword = password
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isInspecting = true,
                inspectionError = null
            )

            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Could not open backup file for reading")

                    inputStream.use { isStream ->
                        val passwordChars = if (!password.isNullOrBlank()) password.toCharArray() else null
                        val result = backupRepository.inspectBackup(isStream, passwordChars)

                        result.onSuccess { summary ->
                            _state.value = _state.value.copy(
                                isInspecting = false,
                                restoreSummary = summary,
                                inspectionError = null,
                                requiresPasswordPrompt = false
                            )
                        }.onFailure { error ->
                            AppLogger.e(tag, "Backup inspection failed", error)
                            val isPasswordIssue = error is SecurityException ||
                                    error.message?.contains("password", ignoreCase = true) == true
                            _state.value = _state.value.copy(
                                isInspecting = false,
                                inspectionError = error.localizedMessage ?: "Failed to inspect backup file",
                                requiresPasswordPrompt = isPasswordIssue
                            )
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e(tag, "Backup inspection I/O error", e)
                    _state.value = _state.value.copy(
                        isInspecting = false,
                        inspectionError = e.localizedMessage ?: "Could not read backup file"
                    )
                }
            }
        }
    }

    fun confirmRestore(password: String? = null) {
        val uri = _state.value.selectedImportUri ?: return
        val effectivePassword = password ?: currentSessionPassword

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRestoring = true,
                restoreError = null,
                restoreResult = null
            )

            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Could not open backup file for restoration")

                    inputStream.use { isStream ->
                        val passwordChars = if (!effectivePassword.isNullOrBlank()) effectivePassword.toCharArray() else null
                        val result = backupRepository.restoreBackup(isStream, passwordChars)

                        result.onSuccess { res ->
                            _state.value = _state.value.copy(
                                isRestoring = false,
                                restoreResult = res,
                                restoreError = null,
                                restoreSummary = null
                            )
                        }.onFailure { error ->
                            AppLogger.e(tag, "Backup restoration failed", error)
                            _state.value = _state.value.copy(
                                isRestoring = false,
                                restoreError = error.localizedMessage ?: "Restoration failed unexpectedly"
                            )
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.e(tag, "Backup restore I/O error", e)
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        restoreError = e.localizedMessage ?: "Could not access backup archive"
                    )
                }
            }
        }
    }

    fun dismissExportDialog() {
        _state.value = _state.value.copy(
            exportSuccessManifest = null,
            exportError = null
        )
    }

    fun dismissInspectionSheet() {
        currentSessionPassword = null
        _state.value = _state.value.copy(
            selectedImportUri = null,
            restoreSummary = null,
            inspectionError = null,
            requiresPasswordPrompt = false
        )
    }

    fun dismissRestoreResultDialog() {
        currentSessionPassword = null
        _state.value = _state.value.copy(
            restoreResult = null,
            restoreError = null
        )
    }
}
