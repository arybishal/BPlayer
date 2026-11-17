package com.pulse.player.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.player.domain.model.LyricsSource
import com.pulse.player.domain.model.Track
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.util.lrc.LrcParser

@Composable
fun LyricsPage(
    track: Track,
    currentPositionMs: Long = 0L,
    onSeekTo: (Long) -> Unit = {},
    activeAccent: Color = MaterialTheme.colorScheme.primary,
    viewModel: LyricsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = LocalExtendedColors.current
    val listState = rememberLazyListState()
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(track.id) {
        viewModel.loadLyricsForTrack(track)
    }

    val activeIndex = remember(currentPositionMs, state.lyrics?.lines) {
        val lines = state.lyrics?.lines
        if (lines.isNullOrEmpty() || state.lyrics?.isSynchronized != true) {
            -1
        } else {
            LrcParser.findActiveLineIndex(lines, currentPositionMs)
        }
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            // Keep active line vertically centered by offsetting scroll target
            val targetIndex = (activeIndex - 3).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = activeAccent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading lyrics...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.onSurfaceMuted
                    )
                }
            }

            state.lyrics != null && state.lyrics!!.lines.isNotEmpty() -> {
                val lyrics = state.lyrics!!
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar with source badge and edit action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val badgeText = when (lyrics.source) {
                            LyricsSource.EMBEDDED -> "Embedded Tag"
                            LyricsSource.SIDECAR_LRC -> "Sidecar .lrc"
                            LyricsSource.LRCLIB_REMOTE -> "LRCLIB Synced"
                            LyricsSource.GEMINI_AI -> "⚠️ AI-Generated (Unverified)"
                            LyricsSource.USER_MANUAL -> "Custom Edit"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = extendedColors.onSurfaceMuted
                            )
                        }

                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EditNote,
                                contentDescription = "Edit Lyrics",
                                tint = extendedColors.onSurfaceMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Scrolling Lyrics Stream
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(lyrics.lines) { index, line ->
                            val isActive = index == activeIndex

                            val textColor by animateColorAsState(
                                targetValue = when {
                                    !lyrics.isSynchronized -> MaterialTheme.colorScheme.onSurface
                                    isActive -> activeAccent
                                    else -> extendedColors.onSurfaceSubtle
                                },
                                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                                label = "lyricsColor"
                            )

                            val scale by animateFloatAsState(
                                targetValue = if (isActive) 1.05f else 1.0f,
                                animationSpec = tween(durationMillis = 250),
                                label = "lyricsScale"
                            )

                            Text(
                                text = line.text.ifEmpty { "• • •" },
                                style = if (isActive) {
                                    MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                } else {
                                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                                },
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = lyrics.isSynchronized) {
                                        if (line.timestampMs >= 0L) {
                                            onSeekTo(line.timestampMs)
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            else -> {
                // Empty State
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Synchronized Lyrics",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.errorMessage
                            ?: "No local lyrics or .lrc file found for \"${track.title}\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extendedColors.onSurfaceMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.searchOnline(track) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = activeAccent),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Search Online")
                        }

                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Add / Edit")
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        LyricsEditorDialog(
            track = track,
            initialText = state.lyrics?.plainText ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { text, isSynced ->
                viewModel.saveCustomLyrics(track.id, text, isSynced)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun LyricsEditorDialog(
    track: Track,
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    var lyricsText by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Lyrics — ${track.title}")
        },
        text = {
            Column {
                Text(
                    text = "Paste or edit timestamped LRC (e.g. [00:12.34] lyric) or plain text lyrics.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = lyricsText,
                    onValueChange = { lyricsText = it },
                    placeholder = { Text("[00:10.00] First line of lyrics...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hasTimeTags = lyricsText.contains(Regex("\\[\\d{1,2}:\\d{2}"))
                    onSave(lyricsText.trim(), hasTimeTags)
                },
                enabled = lyricsText.isNotBlank()
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
