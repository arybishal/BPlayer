package com.pulse.player.data.security

import com.pulse.player.domain.model.backup.BackupManifest
import com.pulse.player.domain.model.backup.CredentialsBackup
import com.pulse.player.domain.model.backup.LibraryBackup
import com.pulse.player.domain.model.backup.SettingsBackup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ParsedBackupArchive(
    val manifest: BackupManifest,
    val settings: SettingsBackup,
    val library: LibraryBackup,
    val credentialsBytes: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ParsedBackupArchive
        if (manifest != other.manifest) return false
        if (settings != other.settings) return false
        if (library != other.library) return false
        if (credentialsBytes != null) {
            if (other.credentialsBytes == null) return false
            if (!credentialsBytes.contentEquals(other.credentialsBytes)) return false
        } else if (other.credentialsBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = manifest.hashCode()
        result = 31 * result + settings.hashCode()
        result = 31 * result + library.hashCode()
        result = 31 * result + (credentialsBytes?.contentHashCode() ?: 0)
        return result
    }
}

class BackupArchiveManager(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    }
) {
    companion object {
        const val CURRENT_BACKUP_VERSION = 1
        const val APP_VERSION = "1.0.0"

        const val ENTRY_MANIFEST = "manifest.json"
        const val ENTRY_SETTINGS = "settings.json"
        const val ENTRY_LIBRARY = "library.json"
        const val ENTRY_CREDENTIALS = "credentials.enc"
    }

    fun writeArchive(
        outputStream: OutputStream,
        manifest: BackupManifest,
        settings: SettingsBackup,
        library: LibraryBackup,
        encryptedCredentialsBytes: ByteArray? = null
    ) {
        ZipOutputStream(outputStream).use { zip ->
            // 1. Manifest
            zip.putNextEntry(ZipEntry(ENTRY_MANIFEST))
            zip.write(json.encodeToString(manifest).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2. Settings
            zip.putNextEntry(ZipEntry(ENTRY_SETTINGS))
            zip.write(json.encodeToString(settings).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 3. Library
            zip.putNextEntry(ZipEntry(ENTRY_LIBRARY))
            zip.write(json.encodeToString(library).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 4. Encrypted Credentials (if present)
            if (encryptedCredentialsBytes != null) {
                zip.putNextEntry(ZipEntry(ENTRY_CREDENTIALS))
                zip.write(encryptedCredentialsBytes)
                zip.closeEntry()
            }
        }
    }

    fun readArchive(inputStream: InputStream): ParsedBackupArchive {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(4096)
                var len: Int
                while (zip.read(chunk).also { len = it } != -1) {
                    buffer.write(chunk, 0, len)
                }
                entries[entry.name] = buffer.toByteArray()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val manifestBytes = entries[ENTRY_MANIFEST]
            ?: throw IllegalArgumentException("Invalid backup file: manifest.json is missing")
        val manifest = json.decodeFromString<BackupManifest>(String(manifestBytes, Charsets.UTF_8))

        if (manifest.version > CURRENT_BACKUP_VERSION) {
            throw IllegalArgumentException("Unsupported backup version ${manifest.version}. Maximum supported version is $CURRENT_BACKUP_VERSION. Please update Pulse.")
        }

        val settingsBytes = entries[ENTRY_SETTINGS]
            ?: throw IllegalArgumentException("Invalid backup file: settings.json is missing")
        val settings = json.decodeFromString<SettingsBackup>(String(settingsBytes, Charsets.UTF_8))

        val libraryBytes = entries[ENTRY_LIBRARY]
            ?: throw IllegalArgumentException("Invalid backup file: library.json is missing")
        val library = json.decodeFromString<LibraryBackup>(String(libraryBytes, Charsets.UTF_8))

        val credentialsBytes = entries[ENTRY_CREDENTIALS]

        return ParsedBackupArchive(
            manifest = manifest,
            settings = settings,
            library = library,
            credentialsBytes = credentialsBytes
        )
    }

    fun verifyAndDecryptCredentials(
        archive: ParsedBackupArchive,
        password: CharArray?
    ): CredentialsBackup? {
        if (!archive.manifest.hasEncryptedCredentials || archive.credentialsBytes == null) {
            return null
        }

        if (password == null || password.isEmpty()) {
            throw SecurityException("Backup contains encrypted credentials. Please provide the backup password.")
        }

        val salt = BackupCrypto.hexToBytes(archive.manifest.saltHex)
        val iv = BackupCrypto.hexToBytes(archive.manifest.ivHex)
        val decryptedBytes = BackupCrypto.decrypt(archive.credentialsBytes, password, salt, iv)
        return json.decodeFromString<CredentialsBackup>(String(decryptedBytes, Charsets.UTF_8))
    }
}
