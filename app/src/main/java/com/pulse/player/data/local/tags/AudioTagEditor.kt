package com.pulse.player.data.local.tags

import android.content.Context
import android.net.Uri
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.util.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class EditableSongTags(
    val trackId: String,
    val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: Int,
    val genre: String,
    val trackNumber: Int,
    val existingArtworkUri: String? = null,
    val newArtworkBytes: ByteArray? = null,
    val newArtworkMime: String = "image/jpeg"
)

sealed interface TagUpdateResult {
    data class Success(val updatedArtworkUri: String?) : TagUpdateResult
    data class Error(val message: String) : TagUpdateResult
}

@Singleton
class AudioTagEditor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao
) {
    private val tag = "AudioTagEditor"

    suspend fun updateSongTags(tags: EditableSongTags): TagUpdateResult = withContext(Dispatchers.IO) {
        try {
            val file = File(tags.path)
            if (!file.exists()) {
                return@withContext TagUpdateResult.Error("Audio file not found at ${tags.path}")
            }

            // 1. Read existing audio file and tags
            val audioFile = AudioFileIO.read(file)
            val audioTag = audioFile.tagOrCreateAndSetDefault

            // 2. Set metadata fields
            audioTag.setField(FieldKey.TITLE, tags.title.trim())
            audioTag.setField(FieldKey.ARTIST, tags.artist.trim())
            audioTag.setField(FieldKey.ALBUM, tags.album.trim())
            if (tags.year > 0) {
                audioTag.setField(FieldKey.YEAR, tags.year.toString())
            }
            if (tags.genre.isNotBlank()) {
                audioTag.setField(FieldKey.GENRE, tags.genre.trim())
            }
            if (tags.trackNumber > 0) {
                audioTag.setField(FieldKey.TRACK, tags.trackNumber.toString())
            }

            // 3. Handle artwork update
            var finalArtworkUri = tags.existingArtworkUri
            if (tags.newArtworkBytes != null && tags.newArtworkBytes.isNotEmpty()) {
                try {
                    val artwork = ArtworkFactory.getNew()
                    artwork.binaryData = tags.newArtworkBytes
                    artwork.mimeType = tags.newArtworkMime
                    audioTag.deleteArtworkField()
                    audioTag.setField(artwork)

                    // Also cache on local storage for fast Compose rendering
                    val thumbDir = File(context.filesDir, "album_thumbs")
                    if (!thumbDir.exists()) thumbDir.mkdirs()
                    val thumbFile = File(thumbDir, "${tags.trackId}.jpg")
                    thumbFile.writeBytes(tags.newArtworkBytes)
                    finalArtworkUri = Uri.fromFile(thumbFile).toString()
                } catch (e: Exception) {
                    AppLogger.d(tag, "Artwork embedding error: ${e.message}")
                }
            }

            // 4. Commit changes to audio file
            audioFile.commit()
            AppLogger.i(tag, "Successfully updated tags in audio file: ${file.name}")

            // 5. Update Room single source of truth
            trackDao.updateTrackTags(
                id = tags.trackId,
                title = tags.title.trim(),
                artist = tags.artist.trim(),
                album = tags.album.trim(),
                year = tags.year,
                genre = tags.genre.trim(),
                trackNumber = tags.trackNumber,
                artworkUri = finalArtworkUri
            )

            TagUpdateResult.Success(finalArtworkUri)
        } catch (e: Exception) {
            AppLogger.e(tag, "Failed to edit tags for ${tags.title}", e)
            TagUpdateResult.Error(e.message ?: "Failed to write tags")
        }
    }
}
