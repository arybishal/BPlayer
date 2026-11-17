package com.pulse.player.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pulse.player.data.local.db.dao.ErrorLogDao
import com.pulse.player.data.local.db.entity.ErrorLogEntity

@Database(
    entities = [
        ErrorLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun errorLogDao(): ErrorLogDao

    companion object {
        const val DATABASE_NAME = "pulse_music.db"
    }
}
