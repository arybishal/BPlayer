package com.pulse.player.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pulse.player.data.local.db.dao.AlbumDao
import com.pulse.player.data.local.db.dao.ArtistDao
import com.pulse.player.data.local.db.dao.ErrorLogDao
import com.pulse.player.data.local.db.dao.GenreDao
import com.pulse.player.data.local.db.dao.PlaylistDao
import com.pulse.player.data.local.db.dao.QueueDao
import com.pulse.player.data.local.db.dao.TrackDao
import com.pulse.player.data.local.db.entity.AlbumEntity
import com.pulse.player.data.local.db.entity.ArtistEntity
import com.pulse.player.data.local.db.entity.ErrorLogEntity
import com.pulse.player.data.local.db.entity.GenreEntity
import com.pulse.player.data.local.db.entity.PlaylistEntity
import com.pulse.player.data.local.db.entity.PlaylistTrackEntity
import com.pulse.player.data.local.db.entity.QueueItemEntity
import com.pulse.player.data.local.db.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        GenreEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        QueueItemEntity::class,
        ErrorLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun genreDao(): GenreDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun queueDao(): QueueDao
    abstract fun errorLogDao(): ErrorLogDao

    companion object {
        const val DATABASE_NAME = "pulse_music.db"
    }
}
