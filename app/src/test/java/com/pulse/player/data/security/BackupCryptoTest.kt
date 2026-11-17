package com.pulse.player.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {

    @Test
    fun `encrypt and decrypt with valid password succeeds`() {
        val originalText = "AIzaSySecretApiKey123456789"
        val password = "StrongPassword@2026".toCharArray()

        val encryptionResult = BackupCrypto.encrypt(
            plaintext = originalText.toByteArray(Charsets.UTF_8),
            password = password
        )

        assertNotNull(encryptionResult.ciphertext)
        assertEquals(16, encryptionResult.salt.size)
        assertEquals(12, encryptionResult.iv.size)
        assertFalse(originalText.toByteArray(Charsets.UTF_8).contentEquals(encryptionResult.ciphertext))

        val decryptedBytes = BackupCrypto.decrypt(
            ciphertext = encryptionResult.ciphertext,
            password = password,
            salt = encryptionResult.salt,
            iv = encryptionResult.iv
        )

        val decryptedText = String(decryptedBytes, Charsets.UTF_8)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun `decrypt with incorrect password fails with SecurityException`() {
        val originalText = "SensitiveData"
        val password = "CorrectPassword123".toCharArray()
        val wrongPassword = "WrongPassword456".toCharArray()

        val encryptionResult = BackupCrypto.encrypt(
            plaintext = originalText.toByteArray(Charsets.UTF_8),
            password = password
        )

        assertThrows(SecurityException::class.java) {
            BackupCrypto.decrypt(
                ciphertext = encryptionResult.ciphertext,
                password = wrongPassword,
                salt = encryptionResult.salt,
                iv = encryptionResult.iv
            )
        }
    }

    @Test
    fun `decrypt with corrupted ciphertext fails authentication`() {
        val originalText = "AuthenticatedPayload"
        val password = "SecureMasterPassword".toCharArray()

        val encryptionResult = BackupCrypto.encrypt(
            plaintext = originalText.toByteArray(Charsets.UTF_8),
            password = password
        )

        val tamperedCiphertext = encryptionResult.ciphertext.copyOf()
        // Flip bits in ciphertext
        tamperedCiphertext[0] = (tamperedCiphertext[0].toInt() xor 0xFF).toByte()

        assertThrows(SecurityException::class.java) {
            BackupCrypto.decrypt(
                ciphertext = tamperedCiphertext,
                password = password,
                salt = encryptionResult.salt,
                iv = encryptionResult.iv
            )
        }
    }

    @Test
    fun `hex conversion roundtrip maintains byte fidelity`() {
        val originalBytes = byteArrayOf(0x00, 0x01, 0x0F, 0x10, 0x7F, -0x80, -0x01)
        val hex = BackupCrypto.bytesToHex(originalBytes)
        val roundtripBytes = BackupCrypto.hexToBytes(hex)

        assertTrue(originalBytes.contentEquals(roundtripBytes))
    }
}
