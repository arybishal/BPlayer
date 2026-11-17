package com.pulse.player.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulse.player.data.local.db.entity.ErrorLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ErrorLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ErrorLogEntity): Long

    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC LIMIT 500")
    fun getAllLogs(): Flow<List<ErrorLogEntity>>

    @Query("SELECT * FROM error_logs WHERE level = :level ORDER BY timestamp DESC LIMIT 500")
    fun getLogsByLevel(level: String): Flow<List<ErrorLogEntity>>

    @Query("DELETE FROM error_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM error_logs WHERE id NOT IN (SELECT id FROM error_logs ORDER BY timestamp DESC LIMIT 500)")
    suspend fun pruneOldLogs()
}
