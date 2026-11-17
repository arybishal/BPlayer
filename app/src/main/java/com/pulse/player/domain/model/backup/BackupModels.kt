package com.pulse.player.domain.model.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupManifest(
    val version: Int = 1,
    val appVersion: String = "1.0.0",
    val createdAt: Long = System.currentTimeMillis(),
    val hasEncryptedCredentials: Boolean = false,
    val saltHex: String = "",
    val ivHex: String = "",
    val playlistCount: Int = 0,
    val favoritesCount: Int = 0,
    val lyricsCount: Int = 0,
    val queueCount: Int = 0,
    val deviceInfo: String = ""
)

@Serializable
data class SettingsBackup(
    val themeMode: String = "DARK",
    val accentChoice: String = "WARM_AMBER",
    val fontScale: Float = 1.0f,
    val liquidGlassEnabled: Boolean = true,
    val eqEnabled: Boolean = false,
    val eqPreset: String = "Flat",
    val eqBassBoost: Int = 0,
    val eqVirtualizer: Int = 0
)

@Serializable
data class TrackBackup(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val trackNumber: Int = 0,
    val year: Int = 0
)

@Serializable
data class PlaylistTrackBackup(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val orderIndex: Int
)

@Serializable
data class PlaylistBackup(
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tracks: List<PlaylistTrackBackup> = emptyList()
)

@Serializable
data class LyricsBackup(
    val trackTitle: String,
    val trackArtist: String,
    val plainText: String,
    val syncedLrc: String? = null,
    val isSynchronized: Boolean = false,
    val source: String = "USER_MANUAL"
)

@Serializable
data class QueueItemBackup(
    val position: Int,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long
)

@Serializable
data class LibraryBackup(
    val playlists: List<PlaylistBackup> = emptyList(),
    val favorites: List<TrackBackup> = emptyList(),
    val lyrics: List<LyricsBackup> = emptyList(),
    val queue: List<QueueItemBackup> = emptyList()
)

@Serializable
data class CredentialsBackup(
    val geminiApiKey: String = "",
    val auddApiKey: String = ""
)

data class RestoreSummary(
    val manifest: BackupManifest,
    val settings: SettingsBackup,
    val totalPlaylists: Int,
    val totalFavorites: Int,
    val totalLyrics: Int,
    val matchedFavorites: Int,
    val unmatchedFavorites: Int,
    val matchedPlaylistTracks: Int,
    val unmatchedPlaylistTracks: Int,
    val matchedLyrics: Int,
    val unmatchedLyrics: Int,
    val hasEncryptedCredentials: Boolean
)

data class RestoreResult(
    val isSuccess: Boolean,
    val restoredPlaylistsCount: Int = 0,
    val restoredFavoritesCount: Int = 0,
    val restoredLyricsCount: Int = 0,
    val matchedTracksCount: Int = 0,
    val unresolvedTracks: List<String> = emptyList(),
    val errorMessage: String? = null
)
