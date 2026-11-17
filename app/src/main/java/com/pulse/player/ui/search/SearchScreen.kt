package com.pulse.player.ui.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.player.domain.model.Track
import com.pulse.player.ui.components.AlbumGridCard
import com.pulse.player.ui.components.ArtistGridCard
import com.pulse.player.ui.components.SongActionsSheet
import com.pulse.player.ui.components.TrackItemRow
import com.pulse.player.ui.recognition.SongRecognitionSheet
import com.pulse.player.ui.theme.LocalExtendedColors

@Composable
fun SearchScreen(
    onTrackClick: (Track, List<Track>) -> Unit = { _, _ -> },
    onPlayNext: (Track) -> Unit = {},
    onAddToQueue: (Track) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val extendedColors = LocalExtendedColors.current
    var selectedTrackForActions by remember { mutableStateOf<Track?>(null) }
    var showRecognitionSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input Bar
        OutlinedTextField(
            value = state.query,
            onValueChange = { viewModel.onQueryChange(it) },
            placeholder = {
                Text(
                    text = "Search tracks, artists, albums...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = extendedColors.onSurfaceMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Icon",
                    tint = extendedColors.onSurfaceMuted
                )
            },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearQuery() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear Search",
                            tint = extendedColors.onSurfaceMuted
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showRecognitionSheet = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Hearing,
                            contentDescription = "Identify Song",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.query.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = extendedColors.onSurfaceSubtle,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Instant search across your music",
                        style = MaterialTheme.typography.bodyLarge,
                        color = extendedColors.onSurfaceMuted
                    )
                }
            }
        } else if (state.matchingTracks.isEmpty() && state.matchingAlbums.isEmpty() && state.matchingArtists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No results for \"${state.query}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = extendedColors.onSurfaceMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Artists Match Row
                if (state.matchingArtists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.matchingArtists, key = { it.id }) { artist ->
                                ArtistGridCard(artist = artist, onArtistClick = {})
                            }
                        }
                    }
                }

                // Albums Match Row
                if (state.matchingAlbums.isNotEmpty()) {
                    item {
                        Text(
                            text = "Albums",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.matchingAlbums, key = { it.id }) { album ->
                                AlbumGridCard(
                                    album = album,
                                    onAlbumClick = {},
                                    modifier = Modifier.size(width = 140.dp, height = 180.dp)
                                )
                            }
                        }
                    }
                }

                // Tracks List Matches
                if (state.matchingTracks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Songs (${state.matchingTracks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(state.matchingTracks, key = { it.id }) { track ->
                        TrackItemRow(
                            track = track,
                            onTrackClick = { onTrackClick(it, state.matchingTracks) },
                            onFavoriteClick = { viewModel.toggleFavorite(it) },
                            onMoreClick = { selectedTrackForActions = it }
                        )
                    }
                }
            }
        }
    }

    selectedTrackForActions?.let { track ->
        SongActionsSheet(
            track = track,
            onDismiss = { selectedTrackForActions = null },
            onPlayNext = { onPlayNext(it) },
            onAddToQueue = { onAddToQueue(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            playlists = state.playlists,
            onAddToPlaylist = { playlist, t -> viewModel.addTrackToPlaylist(playlist.id, t.id) },
            onCreatePlaylistAndAdd = { name, t -> viewModel.createPlaylist(name, t.id) },
            onDeleteRequest = { viewModel.deleteTrack(it) }
        )
    }

    if (showRecognitionSheet) {
        SongRecognitionSheet(
            onDismiss = { showRecognitionSheet = false },
            onPlayTrack = { track ->
                onTrackClick(track, listOf(track))
            },
            onSearchLibrary = { query ->
                viewModel.onQueryChange(query)
            }
        )
    }
}
