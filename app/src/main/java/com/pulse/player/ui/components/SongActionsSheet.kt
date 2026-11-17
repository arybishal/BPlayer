package com.pulse.player.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.OutlinedTextField
import com.pulse.player.domain.model.Playlist
import com.pulse.player.domain.model.Track
import com.pulse.player.domain.model.formatInfo
import com.pulse.player.ui.tageditor.TagEditorSheet
import com.pulse.player.ui.theme.LocalExtendedColors
import com.pulse.player.ui.theme.StatusDanger
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongActionsSheet(
    track: Track,
    onDismiss: () -> Unit,
    onPlayNext: (Track) -> Unit,
    onAddToQueue: (Track) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    playlists: List<Playlist> = emptyList(),
    onAddToPlaylist: (Playlist, Track) -> Unit = { _, _ -> },
    onCreatePlaylistAndAdd: (String, Track) -> Unit = { _, _ -> },
    onDeleteRequest: (Track) -> Unit,
    onGoToArtist: (Long) -> Unit = {},
    onGoToAlbum: (Long) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header: Track Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track.artworkUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(track.artworkUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(54.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = extendedColors.onSurfaceMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${track.artist} • ${track.album}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.onSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        val info = track.formatInfo
                        if (info.badgeText != "AUDIO") {
                            Spacer(modifier = Modifier.width(6.dp))
                            AudioFormatBadge(
                                badgeText = info.badgeText,
                                isHiRes = info.isHiRes,
                                isDsd = info.isDsd,
                                isCompact = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Actions 4-Column Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionButton(
                    icon = Icons.Rounded.QueuePlayNext,
                    label = "Play Next",
                    onClick = {
                        onPlayNext(track)
                        onDismiss()
                    }
                )

                QuickActionButton(
                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                    label = "Add Queue",
                    onClick = {
                        onAddToQueue(track)
                        onDismiss()
                    }
                )

                QuickActionButton(
                    icon = if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    label = "Favorite",
                    iconTint = if (track.isFavorite) StatusDanger else MaterialTheme.colorScheme.primary,
                    onClick = {
                        onToggleFavorite(track)
                    }
                )

                QuickActionButton(
                    icon = Icons.Rounded.Share,
                    label = "Share",
                    onClick = {
                        shareTrack(context, track)
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action List
            ActionListItem(
                icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                label = "Add to playlist",
                onClick = {
                    showPlaylistDialog = true
                }
            )

            ActionListItem(
                icon = Icons.Rounded.Person,
                label = "Go to artist",
                onClick = {
                    onGoToArtist(track.artistId)
                    onDismiss()
                }
            )

            ActionListItem(
                icon = Icons.Rounded.Album,
                label = "Go to album",
                onClick = {
                    onGoToAlbum(track.albumId)
                    onDismiss()
                }
            )

            ActionListItem(
                icon = Icons.Rounded.Info,
                label = "Song technical details",
                onClick = {
                    showDetailsDialog = true
                }
            )

            ActionListItem(
                icon = Icons.Rounded.EditNote,
                label = "Edit song tags & artwork",
                onClick = {
                    showTagEditor = true
                }
            )

            ActionListItem(
                icon = Icons.Rounded.DeleteOutline,
                label = "Delete song",
                labelColor = extendedColors.statusDanger,
                iconTint = extendedColors.statusDanger,
                onClick = {
                    showDeleteConfirmDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDetailsDialog) {
        TechnicalDetailsDialog(
            track = track,
            onDismiss = { showDetailsDialog = false }
        )
    }

    if (showPlaylistDialog) {
        AddToPlaylistDialog(
            track = track,
            playlists = playlists,
            onDismiss = { showPlaylistDialog = false },
            onSelectPlaylist = { pl ->
                onAddToPlaylist(pl, track)
                showPlaylistDialog = false
                onDismiss()
            },
            onCreateNewPlaylist = { name ->
                onCreatePlaylistAndAdd(name, track)
                showPlaylistDialog = false
                onDismiss()
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = "Delete Track?") },
            text = { Text(text = "Are you sure you want to remove \"${track.title}\" from your library and device?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteRequest(track)
                        onDismiss()
                    }
                ) {
                    Text(text = "Delete", color = extendedColors.statusDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "Cancel")
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showTagEditor) {
        TagEditorSheet(
            track = track,
            onDismiss = {
                showTagEditor = false
                onDismiss()
            },
            onSaveSuccess = {
                showTagEditor = false
                onDismiss()
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = extendedColors.onSurfaceMuted
        )
    }
}

@Composable
private fun ActionListItem(
    icon: ImageVector,
    label: String,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    labelColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor
        )
    }
}

@Composable
private fun TechnicalDetailsDialog(
    track: Track,
    onDismiss: () -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    val file = File(track.path)
    val sizeMb = "%.2f MB".format(track.size.toFloat() / (1024 * 1024))
    val info = track.formatInfo

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Track Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Format & Quality", info.detailedBadgeText)
                if (info.isDsd && info.dsdDecimationRate != null) {
                    DetailRow("DSD Processing", info.dsdDecimationRate)
                }
                DetailRow("MIME Type", track.mimeType)
                DetailRow("Duration", track.formattedDuration)
                DetailRow("File Size", sizeMb)
                DetailRow("Sample Rate", "${info.sampleRateHz} Hz")
                DetailRow("Bit Depth", "${info.bitDepth}-bit")
                DetailRow("Path", track.path)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close")
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    val extendedColors = LocalExtendedColors.current
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = extendedColors.onSurfaceMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun shareTrack(context: Context, track: Track) {
    try {
        val file = File(track.path)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = track.mimeType.ifEmpty { "audio/*" }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${track.title} - ${track.artist}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Track"))
    } catch (_: Exception) {
        // Fallback to text share
        val textIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${track.title} by ${track.artist}")
        }
        context.startActivity(Intent.createChooser(textIntent, "Share Track Info"))
    }
}

@Composable
private fun AddToPlaylistDialog(
    track: Track,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: (String) -> Unit
) {
    val extendedColors = LocalExtendedColors.current
    var isCreatingNew by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (isCreatingNew) "New Playlist" else "Add to Playlist")
        },
        text = {
            if (isCreatingNew) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Create New Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isCreatingNew = true }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "New Playlist",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Create new playlist",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (playlists.isEmpty()) {
                        Text(
                            text = "No playlists found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extendedColors.onSurfaceMuted,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectPlaylist(playlist) }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                                    contentDescription = null,
                                    tint = extendedColors.onSurfaceMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${playlist.trackCount} tracks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = extendedColors.onSurfaceMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCreatingNew) {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreateNewPlaylist(newPlaylistName.trim())
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text("Create & Add")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isCreatingNew) {
                        isCreatingNew = false
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
