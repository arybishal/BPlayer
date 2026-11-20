package com.lastwave.app.data.download

import android.util.Log
import java.io.File

/**
 * Native MP3 encoder wrapping the embedded Shine MP3 engine and soxr resampler in liblastwave_audio.
 * Encodes 16-bit linear PCM audio to standard MPEG-1 Layer 3 (MP3) files with genuine MP3 bitstreams.
 */
object Mp3Encoder {
    private const val TAG = "Mp3Encoder"

    init {
        try {
            System.loadLibrary("lastwave_audio")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to load lastwave_audio native library for MP3 encoding", t)
        }
    }

    @JvmStatic
    private external fun nativeEncodePcmFile(
        pcmPath: String,
        mp3Path: String,
        inSampleRate: Int,
        inChannels: Int,
        bitrateKbps: Int,
    ): Boolean

    /**
     * Encodes a raw 16-bit PCM file to a standard MP3 file.
     *
     * @param pcmFile Input file containing interleaved 16-bit linear PCM samples.
     * @param sampleRate Input sample rate in Hz (e.g. 44100, 48000, 96000).
     * @param channels Channel count (1 = mono, 2 = stereo).
     * @param mp3File Target MP3 output file.
     * @param bitrateKbps Target bitrate in kbps (e.g. 128, 192, 256, 320).
     * @return true if encoding succeeded and produced a valid MP3 file.
     */
    fun encodePcmToMp3(
        pcmFile: File,
        sampleRate: Int,
        channels: Int,
        mp3File: File,
        bitrateKbps: Int = 192,
    ): Boolean {
        if (!pcmFile.exists() || pcmFile.length() == 0L) {
            Log.e(TAG, "Cannot encode MP3: input PCM file is empty or missing")
            return false
        }
        if (channels !in 1..2) {
            Log.e(TAG, "Cannot encode MP3: channel count $channels is unsupported (mono or stereo only)")
            return false
        }
        if (sampleRate <= 0) {
            Log.e(TAG, "Cannot encode MP3: invalid sample rate $sampleRate")
            return false
        }

        val clampedBitrate = when {
            bitrateKbps >= 320 -> 320
            bitrateKbps >= 256 -> 256
            bitrateKbps >= 224 -> 224
            bitrateKbps >= 192 -> 192
            bitrateKbps >= 160 -> 160
            bitrateKbps >= 128 -> 128
            bitrateKbps >= 96 -> 96
            else -> 64
        }

        return try {
            val success = nativeEncodePcmFile(
                pcmPath = pcmFile.absolutePath,
                mp3Path = mp3File.absolutePath,
                inSampleRate = sampleRate,
                inChannels = channels,
                bitrateKbps = clampedBitrate,
            )
            success && mp3File.exists() && mp3File.length() > 128L
        } catch (t: Throwable) {
            Log.e(TAG, "Native MP3 encoding threw an exception", t)
            false
        }
    }
}
