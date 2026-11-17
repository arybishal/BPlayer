package com.pulse.player.data.local.lyrics

import com.pulse.player.util.logger.AppLogger
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLyricsReader @Inject constructor() {

    private val tag = "LocalLyricsReader"

    fun readSidecarLrc(audioPath: String): String? {
        return try {
            val audioFile = File(audioPath)
            if (!audioFile.exists()) return null

            val parent = audioFile.parentFile ?: return null
            val baseName = audioFile.nameWithoutExtension

            val possibleLrcFiles = listOf(
                File(parent, "$baseName.lrc"),
                File(parent, "$baseName.LRC")
            )

            val targetFile = possibleLrcFiles.firstOrNull { it.exists() && it.isFile && it.length() > 0 }
            targetFile?.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            AppLogger.d(tag, "Failed to read sidecar LRC for $audioPath: ${e.message}")
            null
        }
    }

    fun readEmbeddedLyrics(audioPath: String): String? {
        return try {
            val file = File(audioPath)
            if (!file.exists() || !file.canRead()) return null

            val audioFile = AudioFileIO.read(file)
            val audioTag = audioFile.tag ?: return null

            val lyrics = audioTag.getFirst(FieldKey.LYRICS)
            if (!lyrics.isNullOrBlank()) {
                lyrics
            } else {
                null
            }
        } catch (e: Exception) {
            AppLogger.d(tag, "Could not extract embedded lyrics from $audioPath: ${e.message}")
            null
        }
    }
}
