package com.pulse.player.data.security

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private const val ALGORITHM_KEY_DERIVATION = "PBKDF2WithHmacSHA256"
    private const val ALGORITHM_CIPHER = "AES/GCM/NoPadding"
    private const val KEY_LENGTH_BITS = 256
    private const val ITERATION_COUNT = 65536
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12

    data class EncryptionResult(
        val ciphertext: ByteArray,
        val salt: ByteArray,
        val iv: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as EncryptionResult
            if (!ciphertext.contentEquals(other.ciphertext)) return false
            if (!salt.contentEquals(other.salt)) return false
            if (!iv.contentEquals(other.iv)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = ciphertext.contentHashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }

    fun generateRandomSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun generateRandomIv(): ByteArray {
        val iv = ByteArray(IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)
        return iv
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(ALGORITHM_KEY_DERIVATION)
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    fun encrypt(
        plaintext: ByteArray,
        password: CharArray,
        salt: ByteArray = generateRandomSalt(),
        iv: ByteArray = generateRandomIv()
    ): EncryptionResult {
        try {
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance(ALGORITHM_CIPHER)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
            val ciphertext = cipher.doFinal(plaintext)
            return EncryptionResult(
                ciphertext = ciphertext,
                salt = salt,
                iv = iv
            )
        } catch (e: Exception) {
            throw SecurityException("Encryption failed", e)
        }
    }

    fun decrypt(
        ciphertext: ByteArray,
        password: CharArray,
        salt: ByteArray,
        iv: ByteArray
    ): ByteArray {
        try {
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance(ALGORITHM_CIPHER)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            return cipher.doFinal(ciphertext)
        } catch (e: GeneralSecurityException) {
            throw SecurityException("Invalid backup password or corrupted encrypted payload", e)
        } catch (e: Exception) {
            throw SecurityException("Decryption error", e)
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        if (len % 2 != 0) throw IllegalArgumentException("Invalid hex string length")
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) +
                    Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
