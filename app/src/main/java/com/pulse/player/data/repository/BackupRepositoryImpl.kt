package com.pulse.player.data.repository

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.room.withTransaction
import com.pulse.player.data.local.datastore.PreferencesManager
import com.pulse.player.data.local.datastore.pulseDataStore
import com.pulse.player.data.local.db.PulseDatabase
import com.pulse.player.data.local.db.dao.LyricsDao
import com.pulse.player.data.local.db.dao.PlaylistDao
import com.pulse.player.data.local.db.dao.QueueDao
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.data.local.db.entity.LyricsEntity
import com.pulse.player.data.local.db.entity.PlaylistEntity
import com.pulse.player.data.local.db.entity.PlaylistTrackEntity
import com.pulse.player.data.local.db.entity.QueueItemEntity
import com.pulse.player.data.local.db.entity.TrackEntity
import com.pulse.player.data.security.BackupArchiveManager
import com.pulse.player.data.security.BackupCrypto
import com.pulse.player.domain.model.backup.BackupManifest
import com.pulse.player.domain.model.backup.CredentialsBackup
import com.pulse.player.domain.model.backup.LibraryBackup
import com.pulse.player.domain.model.backup.LyricsBackup
import com.pulse.player.domain.model.backup.PlaylistBackup
import com.pulse.player.domain.model.backup.PlaylistTrackBackup
import com.pulse.player.domain.model.backup.QueueItemBackup
import com.pulse.player.domain.model.backup.RestoreResult
import com.pulse.player.domain.model.backup.RestoreSummary
import com.pulse.player.domain.model.backup.SettingsBackup
import com.pulse.player.domain.model.backup.TrackBackup
import com.pulse.player.domain.repository.BackupRepository
import com.pulse.player.ui.theme.AccentChoice
import com.pulse.player.ui.theme.ThemeMode
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: PulseDatabase,
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val lyricsDao: LyricsDao,
    private val queueDao: QueueDao,
    private val preferencesManager: PreferencesManager
) : BackupRepository {

    private val tag = "BackupRepository"

    companion object {
        const val CURRENT_BACKUP_VERSION = 1
        const val APP_VERSION = "1.0.0"

        private const val ENTRY_MANIFEST = "manifest.json"
        private const val ENTRY_SETTINGS = "settings.json"
        private const val ENTRY_LIBRARY = "library.json"
        private const val ENTRY_CREDENTIALS = "credentials.enc"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun exportBackup(
        outputStream: OutputStream,
        password: CharArray?
    ): Result<BackupManifest> = withContext(Dispatchers.IO) {
        try {
            AppLogger.i(tag, "Initiating full app backup export")

            // 1. Gather Preferences
            val themeMode = preferencesManager.themeModeFlow.first().name
            val accentChoice = preferencesManager.accentChoiceFlow.first().name
            val fontScale = preferencesManager.fontScaleFlow.first()
            val liquidGlass = preferencesManager.liquidGlassFlow.first()
            val geminiKey = preferencesManager.geminiApiKeyFlow.first()
            val auddKey = preferencesManager.auddApiKeyFlow.first()

            val prefs = context.pulseDataStore.data.first()
            val eqEnabled = prefs[PreferencesManager.KEY_EQ_ENABLED] ?: false
            val eqPreset = prefs[PreferencesManager.KEY_EQ_PRESET] ?: "Flat"
            val eqBass = prefs[PreferencesManager.KEY_BASS_BOOST] ?: 0
            val eqVirt = prefs[PreferencesManager.KEY_VIRTUALIZER] ?: 0

            val settingsBackup = SettingsBackup(
                themeMode = themeMode,
                accentChoice = accentChoice,
                fontScale = fontScale,
                liquidGlassEnabled = liquidGlass,
                eqEnabled = eqEnabled,
                eqPreset = eqPreset,
                eqBassBoost = eqBass,
                eqVirtualizer = eqVirt
            )

            // 2. Gather Playlists & tracks
            val allPlaylists = playlistDao.getAllPlaylistsSync()
            val allTracksMap = trackDao.getAllTracksSync().associateBy { it.id }

            val playlistsBackup = allPlaylists.map { playlist ->
                val playlistTracks = playlistDao.getPlaylistTracksSync(playlist.id)
                val trackBackups = playlistTracks.mapNotNull { pt ->
                    val track = allTracksMap[pt.trackId]
                    if (track != null) {
                        PlaylistTrackBackup(
                            title = track.title,
                            artist = track.artist,
                            album = track.album,
                            durationMs = track.durationMs,
                            orderIndex = pt.orderIndex
                        )
                    } else null
                }
                PlaylistBackup(
                    name = playlist.name,
                    createdAt = playlist.createdAt,
                    updatedAt = playlist.updatedAt,
                    tracks = trackBackups
                )
            }

            // 3. Gather Favorites
            val favorites = trackDao.getFavoriteTracksSync().map { track ->
                TrackBackup(
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    durationMs = track.durationMs,
                    trackNumber = track.trackNumber,
                    year = track.year
                )
            }

            // 4. Gather Lyrics
            val lyricsList = lyricsDao.getAllLyrics().mapNotNull { lyricEntity ->
                val track = allTracksMap[lyricEntity.trackId]
                if (track != null) {
                    LyricsBackup(
                        trackTitle = track.title,
                        trackArtist = track.artist,
                        plainText = lyricEntity.plainText,
                        syncedLrc = lyricEntity.syncedLrc,
                        isSynchronized = lyricEntity.isSynchronized,
                        source = lyricEntity.source
                    )
                } else null
            }

            // 5. Gather Queue
            val queueItems = queueDao.getPersistedQueueSync().mapNotNull { item ->
                val track = allTracksMap[item.trackId]
                if (track != null) {
                    QueueItemBackup(
                        position = item.queuePosition,
                        title = track.title,
                        artist = track.artist,
                        album = track.album,
                        durationMs = track.durationMs
                    )
                } else null
            }

            val libraryBackup = LibraryBackup(
                playlists = playlistsBackup,
                favorites = favorites,
                lyrics = lyricsList,
                queue = queueItems
            )

            // 6. Handle Encrypted Credentials
            val hasCredentials = geminiKey.isNotBlank() || auddKey.isNotBlank()
            var saltHex = ""
            var ivHex = ""
            var encryptedCredentialsBytes: ByteArray? = null

            if (hasCredentials && password != null && password.isNotEmpty()) {
                val creds = CredentialsBackup(
                    geminiApiKey = geminiKey,
                    auddApiKey = auddKey
                )
                val credsJsonBytes = json.encodeToString(creds).toByteArray(Charsets.UTF_8)
                val encryptionResult = BackupCrypto.encrypt(credsJsonBytes, password)
                encryptedCredentialsBytes = encryptionResult.ciphertext
                saltHex = BackupCrypto.bytesToHex(encryptionResult.salt)
                ivHex = BackupCrypto.bytesToHex(encryptionResult.iv)
            }

            // 7. Build Manifest
            val manifest = BackupManifest(
                version = CURRENT_BACKUP_VERSION,
                appVersion = APP_VERSION,
                createdAt = System.currentTimeMillis(),
                hasEncryptedCredentials = encryptedCredentialsBytes != null,
                saltHex = saltHex,
                ivHex = ivHex,
                playlistCount = playlistsBackup.size,
                favoritesCount = favorites.size,
                lyricsCount = lyricsList.size,
                queueCount = queueItems.size,
                deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
            )

            // 8. Package ZIP
            val archiveManager = BackupArchiveManager(json)
            archiveManager.writeArchive(
                outputStream = outputStream,
                manifest = manifest,
                settings = settingsBackup,
                library = libraryBackup,
                encryptedCredentialsBytes = encryptedCredentialsBytes
            )

            preferencesManager.recordBackup(
                fileName = "pulse_backup_${manifest.createdAt}.pulsebackup",
                timestamp = manifest.createdAt
            )

            AppLogger.i(tag, "Backup export completed successfully. Playlists: ${manifest.playlistCount}, Favorites: ${manifest.favoritesCount}")
            Result.success(manifest)
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to export backup", e)
            Result.failure(e)
        }
    }

    override suspend fun inspectBackup(
        inputStream: InputStream,
        password: CharArray?
    ): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        try {
            val archiveManager = BackupArchiveManager(json)
            val archive = archiveManager.readArchive(inputStream)

            // Validate password if credentials are encrypted
            archiveManager.verifyAndDecryptCredentials(archive, password)

            // Match library items against current device media
            val localTracks = trackDao.getAllTracksSync()
            val matcher = TrackMatcher(localTracks)

            var matchedFavorites = 0
            var unmatchedFavorites = 0
            archive.library.favorites.forEach { fav ->
                if (matcher.findMatch(fav.title, fav.artist, fav.album, fav.durationMs) != null) {
                    matchedFavorites++
                } else {
                    unmatchedFavorites++
                }
            }

            var totalPlaylistTracks = 0
            var matchedPlaylistTracks = 0
            var unmatchedPlaylistTracks = 0
            archive.library.playlists.forEach { pl ->
                pl.tracks.forEach { pt ->
                    totalPlaylistTracks++
                    if (matcher.findMatch(pt.title, pt.artist, pt.album, pt.durationMs) != null) {
                        matchedPlaylistTracks++
                    } else {
                        unmatchedPlaylistTracks++
                    }
                }
            }

            var matchedLyrics = 0
            var unmatchedLyrics = 0
            archive.library.lyrics.forEach { lyric ->
                if (matcher.findMatch(lyric.trackTitle, lyric.trackArtist, "", 0L) != null) {
                    matchedLyrics++
                } else {
                    unmatchedLyrics++
                }
            }

            val summary = RestoreSummary(
                manifest = archive.manifest,
                settings = archive.settings,
                totalPlaylists = archive.library.playlists.size,
                totalFavorites = archive.library.favorites.size,
                totalLyrics = archive.library.lyrics.size,
                matchedFavorites = matchedFavorites,
                unmatchedFavorites = unmatchedFavorites,
                matchedPlaylistTracks = matchedPlaylistTracks,
                unmatchedPlaylistTracks = unmatchedPlaylistTracks,
                matchedLyrics = matchedLyrics,
                unmatchedLyrics = unmatchedLyrics,
                hasEncryptedCredentials = archive.manifest.hasEncryptedCredentials
            )

            Result.success(summary)
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to inspect backup: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun restoreBackup(
        inputStream: InputStream,
        password: CharArray?
    ): Result<RestoreResult> = withContext(Dispatchers.IO) {
        try {
            AppLogger.i(tag, "Beginning transactional backup restore")
            val archiveManager = BackupArchiveManager(json)
            val archive = archiveManager.readArchive(inputStream)

            // 1. Decrypt credentials if present
            val decryptedCredentials = archiveManager.verifyAndDecryptCredentials(archive, password)

            val localTracks = trackDao.getAllTracksSync()
            val matcher = TrackMatcher(localTracks)

            val unresolvedTracks = mutableListOf<String>()
            var restoredPlaylists = 0
            var restoredFavorites = 0
            var restoredLyrics = 0
            var matchedTracksTotal = 0

            // 2. Perform Transactional Database Updates
            database.withTransaction {
                // Clear existing playlists
                playlistDao.clearPlaylistTracks()
                playlistDao.clearPlaylists()

                // Restore Playlists
                archive.library.playlists.forEach { playlistBackup ->
                    val playlistEntity = PlaylistEntity(
                        name = playlistBackup.name,
                        createdAt = playlistBackup.createdAt,
                        updatedAt = playlistBackup.updatedAt
                    )
                    val newPlaylistId = playlistDao.insertPlaylist(playlistEntity)
                    val playlistTrackEntities = mutableListOf<PlaylistTrackEntity>()

                    playlistBackup.tracks.forEach { pt ->
                        val matched = matcher.findMatch(pt.title, pt.artist, pt.album, pt.durationMs)
                        if (matched != null) {
                            playlistTrackEntities.add(
                                PlaylistTrackEntity(
                                    playlistId = newPlaylistId,
                                    trackId = matched.id,
                                    orderIndex = pt.orderIndex
                                )
                            )
                            matchedTracksTotal++
                        } else {
                            unresolvedTracks.add("${pt.title} - ${pt.artist} (Playlist: ${playlistBackup.name})")
                        }
                    }

                    if (playlistTrackEntities.isNotEmpty()) {
                        playlistDao.insertPlaylistTracks(playlistTrackEntities)
                        playlistDao.updatePlaylistTrackCount(newPlaylistId, playlistTrackEntities.size)
                    }
                    restoredPlaylists++
                }

                // Restore Favorites
                archive.library.favorites.forEach { fav ->
                    val matched = matcher.findMatch(fav.title, fav.artist, fav.album, fav.durationMs)
                    if (matched != null) {
                        trackDao.updateFavorite(matched.id, true)
                        restoredFavorites++
                        matchedTracksTotal++
                    } else {
                        unresolvedTracks.add("${fav.title} - ${fav.artist} (Favorite)")
                    }
                }

                // Restore Lyrics
                val lyricsEntities = mutableListOf<LyricsEntity>()
                archive.library.lyrics.forEach { lyric ->
                    val matched = matcher.findMatch(lyric.trackTitle, lyric.trackArtist, "", 0L)
                    if (matched != null) {
                        lyricsEntities.add(
                            LyricsEntity(
                                trackId = matched.id,
                                plainText = lyric.plainText,
                                syncedLrc = lyric.syncedLrc,
                                isSynchronized = lyric.isSynchronized,
                                source = lyric.source
                            )
                        )
                        restoredLyrics++
                    } else {
                        unresolvedTracks.add("${lyric.trackTitle} - ${lyric.trackArtist} (Lyrics)")
                    }
                }
                if (lyricsEntities.isNotEmpty()) {
                    lyricsDao.insertLyricsList(lyricsEntities)
                }

                // Restore Queue (if queue items exist)
                if (archive.library.queue.isNotEmpty()) {
                    val queueEntities = archive.library.queue.mapNotNull { item ->
                        val matched = matcher.findMatch(item.title, item.artist, item.album, item.durationMs)
                        if (matched != null) {
                            QueueItemEntity(
                                queuePosition = item.position,
                                trackId = matched.id
                            )
                        } else null
                    }
                    if (queueEntities.isNotEmpty()) {
                        queueDao.clearQueue()
                        queueDao.setQueue(queueEntities)
                    }
                }
            }

            // 3. Restore App Settings into DataStore
            try {
                val theme = ThemeMode.valueOf(archive.settings.themeMode)
                preferencesManager.setThemeMode(theme)
            } catch (_: Exception) {}

            try {
                val accent = AccentChoice.valueOf(archive.settings.accentChoice)
                preferencesManager.setAccentChoice(accent)
            } catch (_: Exception) {}

            preferencesManager.setFontScale(archive.settings.fontScale)
            preferencesManager.setLiquidGlassEnabled(archive.settings.liquidGlassEnabled)

            context.pulseDataStore.edit { prefs ->
                prefs[PreferencesManager.KEY_EQ_ENABLED] = archive.settings.eqEnabled
                prefs[PreferencesManager.KEY_EQ_PRESET] = archive.settings.eqPreset
                prefs[PreferencesManager.KEY_BASS_BOOST] = archive.settings.eqBassBoost
                prefs[PreferencesManager.KEY_VIRTUALIZER] = archive.settings.eqVirtualizer
            }

            // 4. Restore API Credentials
            if (decryptedCredentials != null) {
                if (decryptedCredentials.geminiApiKey.isNotBlank()) {
                    preferencesManager.setGeminiApiKey(decryptedCredentials.geminiApiKey)
                }
                if (decryptedCredentials.auddApiKey.isNotBlank()) {
                    preferencesManager.setAuddApiKey(decryptedCredentials.auddApiKey)
                }
            }

            AppLogger.i(tag, "Transactional restore complete. Restored playlists: $restoredPlaylists, favorites: $restoredFavorites, unresolved: ${unresolvedTracks.size}")

            Result.success(
                RestoreResult(
                    isSuccess = true,
                    restoredPlaylistsCount = restoredPlaylists,
                    restoredFavoritesCount = restoredFavorites,
                    restoredLyricsCount = restoredLyrics,
                    matchedTracksCount = matchedTracksTotal,
                    unresolvedTracks = unresolvedTracks
                )
            )
        } catch (e: Exception) {
            AppLogger.e(tag, "Transactional restore failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

/**
 * Intelligent resilient track matching engine that resolves tracks across devices
 * and re-installations where MediaStore URIs or IDs have shifted.
 */
class TrackMatcher(localTracks: List<TrackEntity>) {
    private val exactMap = mutableMapOf<String, TrackEntity>()
    private val titleMap = mutableMapOf<String, MutableList<TrackEntity>>()

    init {
        localTracks.forEach { track ->
            val normTitle = normalize(track.title)
            val normArtist = normalize(track.artist)
            val key = "$normTitle|$normArtist"
            exactMap[key] = track

            titleMap.getOrPut(normTitle) { mutableListOf() }.add(track)
        }
    }

    fun findMatch(
        title: String,
        artist: String,
        album: String,
        durationMs: Long
    ): TrackEntity? {
        val normTitle = normalize(title)
        val normArtist = normalize(artist)

        // Tier 1: Exact match on normalized title + artist
        if (normTitle.isNotBlank() && normArtist.isNotBlank()) {
            val exact = exactMap["$normTitle|$normArtist"]
            if (exact != null) return exact
        }

        // Tier 2: Match by Title and Duration tolerance (within 4000ms)
        val candidates = titleMap[normTitle] ?: return null
        if (durationMs > 0) {
            val durationMatch = candidates.find { cand ->
                abs(cand.durationMs - durationMs) <= 4000L
            }
            if (durationMatch != null) return durationMatch
        }

        // Tier 3: Match by Title and Album
        val normAlbum = normalize(album)
        if (normAlbum.isNotBlank()) {
            val albumMatch = candidates.find { cand ->
                normalize(cand.album) == normAlbum
            }
            if (albumMatch != null) return albumMatch
        }

        // Tier 4: If single candidate exists with that title, match it
        if (candidates.size == 1) {
            return candidates.first()
        }

        return null
    }

    private fun normalize(str: String): String {
        return str.lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
