package com.pulse.player.data.security

import com.pulse.player.domain.model.backup.BackupManifest
import com.pulse.player.domain.model.backup.CredentialsBackup
import com.pulse.player.domain.model.backup.LibraryBackup
import com.pulse.player.domain.model.backup.LyricsBackup
import com.pulse.player.domain.model.backup.PlaylistBackup
import com.pulse.player.domain.model.backup.PlaylistTrackBackup
import com.pulse.player.domain.model.backup.SettingsBackup
import com.pulse.player.domain.model.backup.TrackBackup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupArchiveManagerTest {

    private val archiveManager = BackupArchiveManager()
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    @Test
    fun `write and read unencrypted archive roundtrip succeeds`() {
        val manifest = BackupManifest(
            version = 1,
            appVersion = "1.0.0",
            playlistCount = 1,
            favoritesCount = 1
        )
        val settings = SettingsBackup(
            themeMode = "AMOLED",
            accentChoice = "CORAL",
            fontScale = 1.1f,
            liquidGlassEnabled = true,
            eqEnabled = true,
            eqPreset = "Rock"
        )
        val library = LibraryBackup(
            playlists = listOf(
                PlaylistBackup(
                    name = "Road Trip",
                    tracks = listOf(
                        PlaylistTrackBackup(
                            title = "Born to Run",
                            artist = "Bruce Springsteen",
                            album = "Born to Run",
                            durationMs = 270000L,
                            orderIndex = 0
                        )
                    )
                )
            ),
            favorites = listOf(
                TrackBackup(
                    title = "Thunder Road",
                    artist = "Bruce Springsteen",
                    album = "Born to Run",
                    durationMs = 290000L
                )
            ),
            lyrics = listOf(
                LyricsBackup(
                    trackTitle = "Thunder Road",
                    trackArtist = "Bruce Springsteen",
                    plainText = "The screen door slams...",
                    syncedLrc = "[00:10.00]The screen door slams..."
                )
            )
        )

        val out = ByteArrayOutputStream()
        archiveManager.writeArchive(out, manifest, settings, library, null)

        val parsed = archiveManager.readArchive(ByteArrayInputStream(out.toByteArray()))

        assertEquals(1, parsed.manifest.version)
        assertEquals("AMOLED", parsed.settings.themeMode)
        assertEquals(1, parsed.library.playlists.size)
        assertEquals("Road Trip", parsed.library.playlists[0].name)
        assertEquals(1, parsed.library.favorites.size)
        assertEquals("Thunder Road", parsed.library.favorites[0].title)
        assertNull(parsed.credentialsBytes)
    }

    @Test
    fun `write and read encrypted credentials succeeds with valid password`() {
        val password = "PulseSecurePassword@2026".toCharArray()
        val originalCredentials = CredentialsBackup(
            geminiApiKey = "AIzaSyGeminiApiKeyTest",
            auddApiKey = "auddApiKeyToken123"
        )
        val credsBytes = json.encodeToString(originalCredentials).toByteArray(Charsets.UTF_8)
        val encryptionResult = BackupCrypto.encrypt(credsBytes, password)

        val manifest = BackupManifest(
            version = 1,
            hasEncryptedCredentials = true,
            saltHex = BackupCrypto.bytesToHex(encryptionResult.salt),
            ivHex = BackupCrypto.bytesToHex(encryptionResult.iv)
        )
        val settings = SettingsBackup()
        val library = LibraryBackup()

        val out = ByteArrayOutputStream()
        archiveManager.writeArchive(out, manifest, settings, library, encryptionResult.ciphertext)

        val parsed = archiveManager.readArchive(ByteArrayInputStream(out.toByteArray()))
        assertTrue(parsed.manifest.hasEncryptedCredentials)
        assertNotNull(parsed.credentialsBytes)

        val decryptedCredentials = archiveManager.verifyAndDecryptCredentials(parsed, password)
        assertNotNull(decryptedCredentials)
        assertEquals("AIzaSyGeminiApiKeyTest", decryptedCredentials?.geminiApiKey)
        assertEquals("auddApiKeyToken123", decryptedCredentials?.auddApiKey)
    }

    @Test
    fun `decrypting credentials with wrong password throws SecurityException`() {
        val password = "CorrectPassword123".toCharArray()
        val wrongPassword = "WrongPassword456".toCharArray()
        val originalCredentials = CredentialsBackup(geminiApiKey = "SecretKey")
        val credsBytes = json.encodeToString(originalCredentials).toByteArray(Charsets.UTF_8)
        val encryptionResult = BackupCrypto.encrypt(credsBytes, password)

        val manifest = BackupManifest(
            version = 1,
            hasEncryptedCredentials = true,
            saltHex = BackupCrypto.bytesToHex(encryptionResult.salt),
            ivHex = BackupCrypto.bytesToHex(encryptionResult.iv)
        )
        val out = ByteArrayOutputStream()
        archiveManager.writeArchive(out, manifest, SettingsBackup(), LibraryBackup(), encryptionResult.ciphertext)

        val parsed = archiveManager.readArchive(ByteArrayInputStream(out.toByteArray()))

        assertThrows(SecurityException::class.java) {
            archiveManager.verifyAndDecryptCredentials(parsed, wrongPassword)
        }
    }

    @Test
    fun `decrypting credentials with null or empty password throws SecurityException`() {
        val password = "CorrectPassword123".toCharArray()
        val credsBytes = json.encodeToString(CredentialsBackup(geminiApiKey = "Key")).toByteArray(Charsets.UTF_8)
        val encryptionResult = BackupCrypto.encrypt(credsBytes, password)

        val manifest = BackupManifest(
            version = 1,
            hasEncryptedCredentials = true,
            saltHex = BackupCrypto.bytesToHex(encryptionResult.salt),
            ivHex = BackupCrypto.bytesToHex(encryptionResult.iv)
        )
        val out = ByteArrayOutputStream()
        archiveManager.writeArchive(out, manifest, SettingsBackup(), LibraryBackup(), encryptionResult.ciphertext)

        val parsed = archiveManager.readArchive(ByteArrayInputStream(out.toByteArray()))

        assertThrows(SecurityException::class.java) {
            archiveManager.verifyAndDecryptCredentials(parsed, null)
        }
    }

    @Test
    fun `archive without manifest throws IllegalArgumentException`() {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("settings.json"))
            zip.write("{}".toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        assertThrows(IllegalArgumentException::class.java) {
            archiveManager.readArchive(ByteArrayInputStream(out.toByteArray()))
        }
    }

    @Test
    fun `archive with unsupported future version throws IllegalArgumentException`() {
        val manifest = BackupManifest(version = 999) // Future version
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(json.encodeToString(manifest).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("settings.json"))
            zip.write(json.encodeToString(SettingsBackup()).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("library.json"))
            zip.write(json.encodeToString(LibraryBackup()).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        val ex = assertThrows(IllegalArgumentException::class.java) {
            archiveManager.readArchive(ByteArrayInputStream(out.toByteArray()))
        }
        assertTrue(ex.message?.contains("Unsupported backup version 999") == true)
    }
}
