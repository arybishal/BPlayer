package com.pulse.player.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pulse.player.domain.model.Lyrics
import com.pulse.player.domain.model.LyricsSource
import com.pulse.player.util.lrc.LrcParser

@Entity(
    tableName = "lyrics_cache",
    indices = [Index(value = ["trackId"], unique = true)]
)
data class LyricsEntity(
    @PrimaryKey
    val trackId: String,
    val plainText: String,
    val syncedLrc: String?,
    val isSynchronized: Boolean,
    val source: String,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Lyrics {
        val lyricsSource = try {
            LyricsSource.valueOf(source)
        } catch (_: Exception) {
            LyricsSource.USER_MANUAL
        }

        return if (isSynchronized && !syncedLrc.isNullOrBlank()) {
            LrcParser.parse(trackId, syncedLrc, lyricsSource)
        } else {
            LrcParser.parse(trackId, plainText, lyricsSource)
        }
    }
}
