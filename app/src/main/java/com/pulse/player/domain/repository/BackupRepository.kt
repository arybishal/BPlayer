package com.pulse.player.domain.repository

import com.pulse.player.domain.model.backup.BackupManifest
import com.pulse.player.domain.model.backup.RestoreResult
import com.pulse.player.domain.model.backup.RestoreSummary
import java.io.InputStream
import java.io.OutputStream

interface BackupRepository {
    /**
     * Packages app settings, library data (playlists, favorites, lyrics, queue), and
     * optionally encrypted API credentials into a versioned ZIP backup stream.
     */
    suspend fun exportBackup(
        outputStream: OutputStream,
        password: CharArray? = null
    ): Result<BackupManifest>

    /**
     * Inspects a backup file without applying changes, validating its manifest,
     * verifying the encryption password (if applicable), and checking match statistics
     * against the current local media library.
     */
    suspend fun inspectBackup(
        inputStream: InputStream,
        password: CharArray? = null
    ): Result<RestoreSummary>

    /**
     * Transactionally restores app state from a backup stream, reproducing settings,
     * playlists, favorites, custom lyrics, and encrypted credentials.
     */
    suspend fun restoreBackup(
        inputStream: InputStream,
        password: CharArray? = null
    ): Result<RestoreResult>
}
