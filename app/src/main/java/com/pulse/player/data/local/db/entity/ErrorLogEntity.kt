package com.pulse.player.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "error_logs",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class ErrorLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: String, // DEBUG, INFO, WARN, ERROR, FATAL
    val tag: String,
    val message: String,
    val stackTrace: String? = null
)
