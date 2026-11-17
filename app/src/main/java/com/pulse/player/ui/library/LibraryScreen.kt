package com.pulse.player.ui.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.player.domain.repository.TrackSortOrder
import com.pulse.player.ui.components.AlbumGridCard
import com.pulse.player.ui.components.AlphabetScrubber
import com.pulse.player.ui.components.ArtistGridCard
import com.pulse.player.ui.components.FolderItemRow
import com.pulse.player.ui.components.PermissionRationaleCard
import com.pulse.player.ui.components.PlaylistItemRow
import com.pulse.player.ui.components.SongActionsSheet
import com.pulse.player.ui.components.TrackItemRow
import com.pulse.player.ui.theme.LocalExtendedColors
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onTrackClick: (com.pulse.player.domain.model.Track, List<com.pulse.player.domain.model.Track>) -> Unit = { _, _ -> },
    onPlayNext: (com.pulse.player.domain.model.Track) -> Unit = {},
    onAddToQueue: (com.pulse.player.domain.model.Track) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTrackForActions by remember { mutableStateOf<com.pulse.player.domain.model.Track?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    val permissionToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permissionToRequest) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.triggerLibraryScan()
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.triggerLibraryScan()
        }
    }

    val tabs = remember { listOf("Tracks", "Albums", "Artists", "Genres", "Playlists", "Folders") }
    var showSortMenu by remember { mutableStateOf(false) }
    val trackListState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Refresh / Scan
                IconButton(
                    onClick = { viewModel.triggerLibraryScan() },
                    modifier = Modifier.size(48.dp)
                ) {
                    if (state.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Rescan Library",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Grid / List toggle (for Albums/Artists)
                if (state.selectedTab == 1 || state.selectedTab == 2) {
                    IconButton(
                        onClick = { viewModel.toggleGridView() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isGridView) Icons.AutoMirrored.Rounded.ViewList else Icons.Rounded.GridView,
                            contentDescription = "Toggle Grid/List View",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Sort Button (for Tracks)
                if (state.selectedTab == 0) {
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Sort,
                                contentDescription = "Sort Tracks",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Title (A to Z)") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.TITLE_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title (Z to A)") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.TITLE_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Artist") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.ARTIST)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Album") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.ALBUM)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Date Added") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.DATE_ADDED)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duration") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.DURATION)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Most Played") },
                                onClick = {
                                    viewModel.setSortOrder(TrackSortOrder.PLAY_COUNT)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = state.selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setSelectedTab(index) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.onSurfaceMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Permission check
        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                PermissionRationaleCard(
                    onRequestPermission = {
                        permissionLauncher.launch(permissionToRequest)
                    }
                )
            }
            return@Column
        }

        // Tab Content
        when (state.selectedTab) {
            0 -> {
                // Tracks Tab with Alphabet Scrubber
                if (state.tracks.isEmpty() && !state.isScanning) {
                    EmptyLibraryView(message = "No audio tracks found on this device.")
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = trackListState,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxSize().padding(end = 20.dp)
                        ) {
                            items(state.tracks, key = { it.id }) { track ->
                                TrackItemRow(
                                    track = track,
                                    onTrackClick = { onTrackClick(it, state.tracks) },
                                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                                    onMoreClick = { selectedTrackForActions = it }
                                )
                            }
                        }

                        // Fast Scroll Alphabet Scrubber
                        if (state.tracks.size > 20) {
                            AlphabetScrubber(
                                onLetterSelected = { letter ->
                                    val targetIndex = state.tracks.indexOfFirst {
                                        val firstChar = it.title.firstOrNull()?.uppercaseChar() ?: '#'
                                        if (letter == '#') !firstChar.isLetter() else firstChar == letter
                                    }
                                    if (targetIndex >= 0) {
                                        coroutineScope.launch {
                                            trackListState.scrollToItem(targetIndex)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
            1 -> {
                // Albums Tab
                if (state.albums.isEmpty() && !state.isScanning) {
                    EmptyLibraryView(message = "No albums found in your library.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (state.isGridView) 2 else 1),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.albums, key = { it.id }) { album ->
                            AlbumGridCard(album = album, onAlbumClick = {})
                        }
                    }
                }
            }
            2 -> {
                // Artists Tab
                if (state.artists.isEmpty() && !state.isScanning) {
                    EmptyLibraryView(message = "No artists found in your library.")
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (state.isGridView) 2 else 1),
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.artists, key = { it.id }) { artist ->
                            ArtistGridCard(artist = artist, onArtistClick = {})
                        }
                    }
                }
            }
            3 -> {
                // Genres Tab
                EmptyLibraryView(message = "Genres are organized as audio files are scanned.")
            }
            4 -> {
                // Playlists Tab
                Column(modifier = Modifier.fillMaxSize()) {
                    // Create Playlist Button Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreatePlaylistDialog = true }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Create Playlist",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Create Playlist",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Build custom collections",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extendedColors.onSurfaceMuted
                            )
                        }
                    }

                    if (state.playlists.isEmpty()) {
                        EmptyLibraryView(message = "No custom playlists created yet.")
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.playlists, key = { it.id }) { playlist ->
                                PlaylistItemRow(
                                    playlist = playlist,
                                    onPlaylistClick = {},
                                    onDeletePlaylist = { pl -> viewModel.deletePlaylist(pl.id) }
                                )
                            }
                        }
                    }
                }
            }
            5 -> {
                // Folders Tab
                if (state.folders.isEmpty() && !state.isScanning) {
                    EmptyLibraryView(message = "No audio folders detected.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.folders, key = { it.path }) { folder ->
                            FolderItemRow(folder = folder, onFolderClick = {})
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreatePlaylistDialog = false
                newPlaylistName = ""
            },
            title = { Text(text = "New Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylist(newPlaylistName.trim())
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    enabled = newPlaylistName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreatePlaylistDialog = false
                        newPlaylistName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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
            onCreatePlaylistAndAdd = { name, t ->
                viewModel.createPlaylist(name)
            },
            onDeleteRequest = { viewModel.deleteTrack(it) }
        )
    }
}

@Composable
private fun EmptyLibraryView(message: String) {
    val extendedColors = LocalExtendedColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.MusicOff,
                contentDescription = null,
                tint = extendedColors.onSurfaceSubtle,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = extendedColors.onSurfaceMuted
            )
        }
    }
}
