package com.pulse.player.ui.settings.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.player.data.local.db.dao.ErrorLogDao
import com.pulse.player.data.local.db.entity.ErrorLogEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ErrorLogsUiState(
    val logs: List<ErrorLogEntity> = emptyList(),
    val selectedFilter: String = "ALL",
    val isLoading: Boolean = false
)

@HiltViewModel
class ErrorLogsViewModel @Inject constructor(
    private val errorLogDao: ErrorLogDao
) : ViewModel() {

    private val selectedFilter = MutableStateFlow("ALL")

    val uiState: StateFlow<ErrorLogsUiState> = combine(
        errorLogDao.getAllLogs(),
        selectedFilter
    ) { allLogs, filter ->
        val filtered = if (filter == "ALL") {
            allLogs
        } else {
            allLogs.filter { it.level.equals(filter, ignoreCase = true) }
        }
        ErrorLogsUiState(logs = filtered, selectedFilter = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ErrorLogsUiState(isLoading = true)
    )

    fun setFilter(filter: String) {
        selectedFilter.value = filter
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            errorLogDao.clearAllLogs()
        }
    }

    fun getExportText(logs: List<ErrorLogEntity>): String {
        val sb = StringBuilder()
        sb.append("=== PULSE MUSIC PLAYER DIAGNOSTIC ERROR LOGS ===\n")
        sb.append("Export Time: ${java.util.Date()}\n")
        sb.append("Total Entries: ${logs.size}\n\n")

        for (log in logs) {
            sb.append("[${java.util.Date(log.timestamp)}] [${log.level}] [${log.tag}]\n")
            sb.append("Message: ${log.message}\n")
            if (!log.stackTrace.isNullOrBlank()) {
                sb.append("Stack Trace:\n${log.stackTrace}\n")
            }
            sb.append("----------------------------------------------------\n")
        }
        return sb.toString()
    }
}
