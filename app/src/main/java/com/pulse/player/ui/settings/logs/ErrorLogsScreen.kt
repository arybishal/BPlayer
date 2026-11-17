package com.pulse.player.ui.settings.logs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.player.data.local.db.entity.ErrorLogEntity
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.StatusDanger
import com.pulse.player.ui.theme.StatusSuccess
import com.pulse.player.ui.theme.StatusWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ErrorLogsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ErrorLogsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    var showClearDialog by remember { mutableStateOf(false) }

    val filterOptions = remember { listOf("ALL", "FATAL", "ERROR", "WARN", "INFO") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Error Logs",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            // Copy Action
            IconButton(
                onClick = {
                    val exportText = viewModel.getExportText(state.logs)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Pulse Error Logs", exportText))
                    Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy logs to clipboard",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Share Action
            IconButton(
                onClick = {
                    val exportText = viewModel.getExportText(state.logs)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Pulse Error Logs")
                        putExtra(Intent.EXTRA_TEXT, exportText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Pulse Logs"))
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share logs as text",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Clear Action
            IconButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Clear all logs",
                    tint = extendedColors.statusDanger
                )
            }
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { filter ->
                val selected = state.selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setFilter(filter) },
                    label = { Text(text = filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Log Items List
        if (state.logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No diagnostic logs captured.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = extendedColors.onSurfaceMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.logs, key = { it.id }) { log ->
                    ErrorLogItem(log = log)
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = "Clear Diagnostic Logs?") },
            text = { Text(text = "This will permanently remove all stored diagnostic error entries.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearDialog = false
                    }
                ) {
                    Text(text = "Clear", color = extendedColors.statusDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ErrorLogItem(log: ErrorLogEntity) {
    val extendedColors = LocalExtendedColors.current
    var expanded by remember { mutableStateOf(false) }

    val badgeColor = when (log.level.uppercase()) {
        "FATAL" -> Color(0xFFD90429)
        "ERROR" -> StatusDanger
        "WARN" -> StatusWarning
        "INFO" -> StatusSuccess
        else -> extendedColors.onSurfaceMuted
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { dateFormatter.format(Date(log.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.level,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = log.tag,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.onSurfaceMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = log.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!log.stackTrace.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (expanded) "Tap to collapse stack trace" else "Tap to expand stack trace...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = log.stackTrace,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = extendedColors.onSurfaceSubtle
                    )
                }
            }
        }
    }
}
