package com.lastwave.app.data.download

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class LiveAudioFormatConversionTest {

    private lateinit var context: Context
    private val tempFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        tempFiles.forEach { file ->
            runCatching { if (file.exists()) file.delete() }
        }
        tempFiles.clear()
    }

    private fun generateSinePcm(
        sampleRate: Int = 44100,
        channels: Int = 2,
        durationSeconds: Double = 3.0,
        frequencyHz: Double = 440.0,
    ): File {
        val totalFrames = (sampleRate * durationSeconds).toInt()
        val file = File.createTempFile("test_sine_", ".pcm", context.cacheDir)
        tempFiles.add(file)

        val byteBuffer = ByteBuffer.allocate(totalFrames * channels * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until totalFrames) {
            val angle = 2.0 * Math.PI * i * frequencyHz / sampleRate
            val sample = (sin(angle) * 32000.0).toInt().coerceIn(-32768, 32767).toShort()
            for (ch in 0 until channels) {
                byteBuffer.putShort(sample)
            }
        }

        FileOutputStream(file).use { out ->
            out.write(byteBuffer.array())
        }
        return file
    }

    @Test
    fun testMp3Encoding_standardBitrate_producesValidMp3() {
        val pcm = generateSinePcm(sampleRate = 44100, channels = 2, durationSeconds = 3.0)
        val mp3 = File.createTempFile("test_out_", ".mp3", context.cacheDir)
        tempFiles.add(mp3)

        val success = Mp3Encoder.encodePcmToMp3(
            pcmFile = pcm,
            sampleRate = 44100,
            channels = 2,
            mp3File = mp3,
            bitrateKbps = 192,
        )

        assertTrue("Mp3Encoder returned false", success)
        assertTrue("MP3 file does not exist", mp3.exists())
        assertTrue("MP3 file is too small: ${mp3.length()} bytes", mp3.length() > 20_000L)

        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(mp3.absolutePath)
            val mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()

            assertNotNull("MIME type was null", mime)
            assertTrue("Expected audio/mpeg, got $mime", mime!!.contains("mpeg"))
            assertNotNull("Duration was null", dur)
            assertTrue("Duration $dur out of bounds (expected ~3000ms)", dur in 2500L..3500L)
        } finally {
            mmr.release()
        }
    }

    @Test
    fun testMp3Encoding_320kbps_isLargerThan128kbps() {
        val pcm = generateSinePcm(sampleRate = 44100, channels = 2, durationSeconds = 2.0)
        val mp3_128 = File.createTempFile("test_128_", ".mp3", context.cacheDir)
        val mp3_320 = File.createTempFile("test_320_", ".mp3", context.cacheDir)
        tempFiles.add(mp3_128)
        tempFiles.add(mp3_320)

        val ok128 = Mp3Encoder.encodePcmToMp3(pcm, 44100, 2, mp3_128, 128)
        val ok320 = Mp3Encoder.encodePcmToMp3(pcm, 44100, 2, mp3_320, 320)

        assertTrue(ok128)
        assertTrue(ok320)
        assertTrue("320k size (${mp3_320.length()}) not greater than 128k size (${mp3_128.length()})", mp3_320.length() > mp3_128.length())
    }

    @Test
    fun testMp3Encoding_hiRes96kHz_resamplesAndEncodesValidMp3() {
        val pcm = generateSinePcm(sampleRate = 96000, channels = 2, durationSeconds = 2.0)
        val mp3 = File.createTempFile("test_96k_", ".mp3", context.cacheDir)
        tempFiles.add(mp3)

        val ok = Mp3Encoder.encodePcmToMp3(
            pcmFile = pcm,
            sampleRate = 96000,
            channels = 2,
            mp3File = mp3,
            bitrateKbps = 256,
        )

        assertTrue("Hi-Res MP3 encode returned false", ok)
        assertTrue(mp3.exists())
        assertTrue(mp3.length() > 10_000L)

        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(mp3.absolutePath)
            val mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()

            assertNotNull(mime)
            assertTrue("Expected audio/mpeg, got $mime", mime!!.contains("mpeg"))
            assertNotNull(dur)
            assertTrue("Duration $dur out of bounds (expected ~2000ms)", dur in 1800L..2500L)
        } finally {
            mmr.release()
        }
    }

    @Test
    fun testCorruptOrMissingPcm_handledSafelyWithoutCrash() {
        val nonExistent = File(context.cacheDir, "does_not_exist.pcm")
        val mp3 = File.createTempFile("test_fail_", ".mp3", context.cacheDir)
        tempFiles.add(mp3)

        val result = Mp3Encoder.encodePcmToMp3(
            pcmFile = nonExistent,
            sampleRate = 44100,
            channels = 2,
            mp3File = mp3,
            bitrateKbps = 192,
        )
        assertFalse(result)
    }
}
