package com.pulse.player.data.local.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.pulse.player.data.local.db.dao.AlbumDao
import com.pulse.player.data.local.db.dao.ArtistDao
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.data.local.db.entity.AlbumEntity
import com.pulse.player.data.local.db.entity.ArtistEntity
import com.pulse.player.data.local.db.entity.TrackEntity
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao,
    private val artistDao: ArtistDao
) {
    private val tag = "MediaStoreScanner"

    suspend fun scanLibrary(minDurationMs: Long = 30_000L): ScanResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var addedCount = 0
        var updatedCount = 0

        try {
            val contentResolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(minDurationMs.toString())
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            val existingHeaders = trackDao.getAllTrackSyncHeaders().associateBy { it.path }
            val scannedPaths = mutableListOf<String>()
            val tracksToUpsert = mutableListOf<TrackEntity>()

            val albumMap = mutableMapOf<Long, AlbumEntity>()
            val artistMap = mutableMapOf<Long, ArtistEntity>()

            contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val trackNumberCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val mediaStoreId = cursor.getLong(idCol)
                    val path = cursor.getString(dataCol) ?: continue
                    val dateModified = cursor.getLong(dateModifiedCol)
                    val size = cursor.getLong(sizeCol)

                    scannedPaths.add(path)

                    val existing = existingHeaders[path]
                    val isNew = existing == null
                    val isModified = existing != null && (existing.dateModified != dateModified || existing.size != size)

                    val artistName = cursor.getString(artistCol) ?: "<unknown>"
                    val artistId = cursor.getLong(artistIdCol)
                    val albumName = cursor.getString(albumCol) ?: "<unknown>"
                    val albumId = cursor.getLong(albumIdCol)
                    val year = cursor.getInt(yearCol)

                    // Aggregate albums
                    val currentAlbum = albumMap[albumId]
                    val artworkUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    ).toString()

                    albumMap[albumId] = if (currentAlbum == null) {
                        AlbumEntity(
                            id = albumId,
                            title = albumName,
                            artist = artistName,
                            trackCount = 1,
                            year = year,
                            artworkUri = artworkUri
                        )
                    } else {
                        currentAlbum.copy(trackCount = currentAlbum.trackCount + 1)
                    }

                    // Aggregate artists
                    val currentArtist = artistMap[artistId]
                    artistMap[artistId] = if (currentArtist == null) {
                        ArtistEntity(
                            id = artistId,
                            name = artistName,
                            trackCount = 1,
                            albumCount = 1
                        )
                    } else {
                        currentArtist.copy(trackCount = currentArtist.trackCount + 1)
                    }

                    if (isNew || isModified) {
                        val title = cursor.getString(titleCol) ?: path.substringAfterLast('/').substringBeforeLast('.')
                        val duration = cursor.getLong(durationCol)
                        val trackNumber = cursor.getInt(trackNumberCol)
                        val mimeType = cursor.getString(mimeTypeCol) ?: "audio/*"
                        val dateAdded = cursor.getLong(dateAddedCol)

                        val trackEntity = TrackEntity(
                            id = mediaStoreId.toString(),
                            mediaStoreId = mediaStoreId,
                            title = title,
                            artist = artistName,
                            artistId = artistId,
                            album = albumName,
                            albumId = albumId,
                            durationMs = duration,
                            path = path,
                            trackNumber = trackNumber,
                            year = year,
                            mimeType = mimeType,
                            bitRate = 0,
                            sampleRate = 44100,
                            bitDepth = 16,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            size = size,
                            artworkUri = artworkUri
                        )
                        tracksToUpsert.add(trackEntity)

                        if (isNew) addedCount++ else updatedCount++
                    }
                }
            }

            // Commit upserts
            if (tracksToUpsert.isNotEmpty()) {
                trackDao.insertTracks(tracksToUpsert)
            }

            // Clean up removed/deleted audio files
            if (scannedPaths.isNotEmpty()) {
                trackDao.deleteOrphanedTracks(scannedPaths)
            }

            // Commit aggregated albums and artists
            if (albumMap.isNotEmpty()) {
                albumDao.insertAlbums(albumMap.values.toList())
            }
            if (artistMap.isNotEmpty()) {
                artistDao.insertArtists(artistMap.values.toList())
            }

            val elapsedMs = System.currentTimeMillis() - startTime
            AppLogger.i(tag, "Scan finished in ${elapsedMs}ms: $addedCount added, $updatedCount updated, ${scannedPaths.size} total active tracks.")

            ScanResult.Success(
                totalCount = scannedPaths.size,
                addedCount = addedCount,
                updatedCount = updatedCount,
                durationMs = elapsedMs
            )
        } catch (e: Exception) {
            AppLogger.e(tag, "Error during media scan", e)
            ScanResult.Error(e.message ?: "Unknown scan error")
        }
    }
}

sealed interface ScanResult {
    data class Success(
        val totalCount: Int,
        val addedCount: Int,
        val updatedCount: Int,
        val durationMs: Long
    ) : ScanResult

    data class Error(val message: String) : ScanResult
}
