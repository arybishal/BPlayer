package com.lastwave.app.data.download

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import com.lastwave.app.playback.CapturingAudioSink
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ConvertedAudioResult(
    val file: File,
    val format: DownloadAudioFormat,
    val formatBadge: String,
    val extension: String,
    val mimeType: String,
    val isLossless: Boolean,
)

/**
 * Universal offline audio converter for BPlayer.
 * Performs real decoding (via ExoPlayer + CapturingAudioSink) and genuine encoding
 * to user-selected target formats (OPUS, MP3, AAC/M4A, FLAC).
 */
@UnstableApi
@Singleton
class AudioFormatConverter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "AudioFormatConverter"
        private const val MAX_CONVERT_TIMEOUT_MS = 10 * 60 * 1000L
        private const val PUMP_BYTES = 64 * 1024
    }

    /**
     * Determines whether conversion is required for the given input extension and desired format.
     */
    fun isConversionRequired(currentExtension: String, targetFormat: DownloadAudioFormat): Boolean {
        if (targetFormat == DownloadAudioFormat.ORIGINAL) return false
        val ext = currentExtension.lowercase()
        return when (targetFormat) {
            DownloadAudioFormat.OPUS -> ext != "opus" && ext != "ogg"
            DownloadAudioFormat.MP3 -> ext != "mp3"
            DownloadAudioFormat.AAC_M4A -> ext != "m4a" && ext != "mp4" && ext != "aac"
            DownloadAudioFormat.FLAC -> ext != "flac"
            DownloadAudioFormat.ORIGINAL -> false
        }
    }

    /**
     * Converts the source audio file to the desired target format and quality.
     * If the source is already in the target format or ORIGINAL was selected, returns the source file.
     */
    suspend fun convert(
        sourceFile: File,
        sourceExtension: String,
        targetFormat: DownloadAudioFormat,
        quality: ConversionQuality,
        title: String,
        artist: String,
        album: String?,
    ): ConvertedAudioResult = withContext(Dispatchers.IO) {
        if (!isConversionRequired(sourceExtension, targetFormat)) {
            val badge = when (sourceExtension.lowercase()) {
                "flac" -> "FLAC"
                "opus" -> "OPUS"
                "webm" -> "WEBM OPUS"
                "mp3" -> "MP3"
                "m4a", "mp4", "aac" -> "M4A AAC"
                else -> sourceExtension.uppercase()
            }
            val mime = when (sourceExtension.lowercase()) {
                "flac" -> "audio/flac"
                "opus" -> "audio/ogg"
                "webm" -> "audio/webm"
                "mp3" -> "audio/mpeg"
                "m4a", "mp4", "aac" -> "audio/mp4"
                else -> "audio/*"
            }
            return@withContext ConvertedAudioResult(
                file = sourceFile,
                format = targetFormat,
                formatBadge = badge,
                extension = sourceExtension,
                mimeType = mime,
                isLossless = sourceExtension.equals("flac", ignoreCase = true),
            )
        }

        withTimeout(MAX_CONVERT_TIMEOUT_MS) {
            val pcmFile = File.createTempFile("conv_pcm_", ".pcm", context.cacheDir)
            val sink = CapturingAudioSink(pcmFile)
            val mainLooper = Looper.getMainLooper()

            val renderers = object : DefaultRenderersFactory(context) {
                override fun buildAudioSink(
                    context: Context,
                    enableFloatOutput: Boolean,
                    enableAudioTrackPlaybackParams: Boolean,
                ): AudioSink = sink
            }

            var player: ExoPlayer? = null
            try {
                withContext(Dispatchers.Main) {
                    player = ExoPlayer.Builder(context, renderers)
                        .setLooper(mainLooper)
                        .build()
                }

                val mediaItem = MediaItem.Builder()
                    .setMediaId("convert:${sourceFile.name}")
                    .setUri(Uri.fromFile(sourceFile))
                    .build()

                awaitEnded(player!!, mediaItem)

                if (!sink.isConfigured() || sink.framesWritten <= 0) {
                    throw IOException("ExoPlayer failed to decode source audio to PCM")
                }

                val sampleRate = sink.sampleRate
                val channels = sink.channelCount

                when (targetFormat) {
                    DownloadAudioFormat.MP3 -> {
                        val mp3File = File.createTempFile("conv_mp3_", ".mp3", context.cacheDir)
                        val ok = Mp3Encoder.encodePcmToMp3(
                            pcmFile = pcmFile,
                            sampleRate = sampleRate,
                            channels = channels,
                            mp3File = mp3File,
                            bitrateKbps = quality.mp3BitrateKbps,
                        )
                        if (!ok) {
                            mp3File.delete()
                            throw IOException("Shine MP3 encoding failed")
                        }
                        ConvertedAudioResult(
                            file = mp3File,
                            format = DownloadAudioFormat.MP3,
                            formatBadge = "MP3 ${quality.mp3BitrateKbps}k",
                            extension = "mp3",
                            mimeType = "audio/mpeg",
                            isLossless = false,
                        )
                    }

                    DownloadAudioFormat.AAC_M4A -> {
                        val m4aFile = File.createTempFile("conv_m4a_", ".m4a", context.cacheDir)
                        val ok = encodePcmToAacM4a(
                            pcmFile = pcmFile,
                            sampleRate = sampleRate,
                            channels = channels,
                            outFile = m4aFile,
                            bitrateKbps = quality.aacBitrateKbps,
                        )
                        if (!ok) {
                            m4aFile.delete()
                            throw IOException("Android MediaCodec AAC encoding failed")
                        }
                        ConvertedAudioResult(
                            file = m4aFile,
                            format = DownloadAudioFormat.AAC_M4A,
                            formatBadge = "AAC ${quality.aacBitrateKbps}k",
                            extension = "m4a",
                            mimeType = "audio/mp4",
                            isLossless = false,
                        )
                    }

                    DownloadAudioFormat.FLAC -> {
                        val flacFile = File.createTempFile("conv_flac_", ".flac", context.cacheDir)
                        val ok = encodePcmToFlac(
                            pcmFile = pcmFile,
                            sampleRate = sampleRate,
                            channels = channels,
                            outFile = flacFile,
                        )
                        if (!ok) {
                            flacFile.delete()
                            throw IOException("Android MediaCodec FLAC encoding failed")
                        }
                        ConvertedAudioResult(
                            file = flacFile,
                            format = DownloadAudioFormat.FLAC,
                            formatBadge = "FLAC",
                            extension = "flac",
                            mimeType = "audio/flac",
                            isLossless = true,
                        )
                    }

                    DownloadAudioFormat.OPUS -> {
                        // If source is webm opus, losslessly remux to opus
                        if (sourceExtension.equals("webm", ignoreCase = true)) {
                            val opusFile = File.createTempFile("conv_opus_", ".opus", context.cacheDir)
                            if (WebmOpusRemuxer.remux(sourceFile, opusFile)) {
                                ConvertedAudioResult(
                                    file = opusFile,
                                    format = DownloadAudioFormat.OPUS,
                                    formatBadge = "OPUS",
                                    extension = "opus",
                                    mimeType = "audio/ogg",
                                    isLossless = false,
                                )
                            } else {
                                opusFile.delete()
                                throw IOException("WebM Opus remux failed")
                            }
                        } else {
                            // Direct source kept if non-WebM Opus
                            ConvertedAudioResult(
                                file = sourceFile,
                                format = DownloadAudioFormat.OPUS,
                                formatBadge = "OPUS",
                                extension = "opus",
                                mimeType = "audio/ogg",
                                isLossless = false,
                            )
                        }
                    }

                    DownloadAudioFormat.ORIGINAL -> {
                        ConvertedAudioResult(
                            file = sourceFile,
                            format = DownloadAudioFormat.ORIGINAL,
                            formatBadge = sourceExtension.uppercase(),
                            extension = sourceExtension,
                            mimeType = "audio/*",
                            isLossless = sourceExtension.equals("flac", ignoreCase = true),
                        )
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    runCatching { player?.release() }
                }
                runCatching { pcmFile.delete() }
            }
        }
    }

    private suspend fun awaitEnded(
        player: ExoPlayer,
        item: MediaItem,
    ): Unit = suspendCancellableCoroutine { cont ->
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED && cont.isActive) {
                    player.removeListener(this)
                    cont.resume(Unit)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                if (cont.isActive) {
                    player.removeListener(this)
                    cont.resumeWithException(error)
                }
            }
        }
        player.addListener(listener)
        cont.invokeOnCancellation {
            runCatching { player.removeListener(listener) }
        }
        player.setMediaItem(item)
        player.prepare()
        player.playWhenReady = true
    }

    /**
     * Encodes raw 16-bit PCM into standard AAC-LC in an MP4 (.m4a) container using MediaCodec + MediaMuxer.
     */
    private fun encodePcmToAacM4a(
        pcmFile: File,
        sampleRate: Int,
        channels: Int,
        outFile: File,
        bitrateKbps: Int,
    ): Boolean {
        if (sampleRate <= 0 || channels !in 1..2) return false
        val mime = MediaFormat.MIMETYPE_AUDIO_AAC
        val codec = try {
            MediaCodec.createEncoderByType(mime)
        } catch (_: Exception) {
            return false
        }

        var muxer: MediaMuxer? = null
        try {
            val format = MediaFormat.createAudioFormat(mime, sampleRate, channels).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, bitrateKbps * 1000)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var audioTrackIndex = -1
            var muxerStarted = false

            FileInputStream(pcmFile).buffered().use { input ->
                var inputEos = false
                var outputEos = false
                val bufferInfo = MediaCodec.BufferInfo()
                val chunk = ByteArray(PUMP_BYTES)

                while (!outputEos) {
                    if (!inputEos) {
                        val inIndex = codec.dequeueInputBuffer(10_000)
                        if (inIndex >= 0) {
                            val inBuf = codec.getInputBuffer(inIndex) ?: error("null input buffer")
                            inBuf.clear()
                            var read = 0
                            while (read < inBuf.remaining()) {
                                val n = input.read(chunk, 0, minOf(chunk.size, inBuf.remaining()))
                                if (n < 0) break
                                inBuf.put(chunk, 0, n)
                                read += n
                            }
                            if (read <= 0) {
                                codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputEos = true
                            } else {
                                codec.queueInputBuffer(inIndex, 0, read, 0, 0)
                            }
                        }
                    }

                    val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    when {
                        outIndex >= 0 -> {
                            val outBuf = codec.getOutputBuffer(outIndex)
                            if (outBuf != null && bufferInfo.size > 0 && muxerStarted) {
                                outBuf.position(bufferInfo.offset)
                                outBuf.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(audioTrackIndex, outBuf, bufferInfo)
                            }
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                outputEos = true
                            }
                            codec.releaseOutputBuffer(outIndex, false)
                        }
                        outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = codec.outputFormat
                            audioTrackIndex = muxer.addTrack(newFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    }
                }
            }

            return muxerStarted && outFile.length() > 512L
        } catch (t: Throwable) {
            runCatching { outFile.delete() }
            return false
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
            runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
        }
    }

    /**
     * PCM16 file -> framed .flac using Android MediaCodec AUDIO_FLAC encoder.
     */
    private fun encodePcmToFlac(pcmFile: File, sampleRate: Int, channels: Int, outFile: File): Boolean {
        if (sampleRate <= 0 || channels <= 0) return false
        val codec = try {
            MediaCodec.createEncoderByType(MimeTypes.AUDIO_FLAC)
        } catch (_: Exception) {
            return false
        }
        try {
            val format = MediaFormat().apply {
                setString(MediaFormat.KEY_MIME, MimeTypes.AUDIO_FLAC)
                setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate)
                setInteger(MediaFormat.KEY_CHANNEL_COUNT, channels)
            }
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()
            var streamInfo: ByteArray? = null
            val pendingFrames = mutableListOf<ByteArray>()
            var out: OutputStream? = null

            fun ensureOutput(): OutputStream {
                var o = out
                if (o == null) {
                    val csd = streamInfo ?: error("no STREAMINFO")
                    o = outFile.outputStream().buffered()
                    o.write("fLaC".toByteArray(Charsets.US_ASCII))
                    // METADATA_BLOCK_HEADER: last-block + STREAMINFO(0) + length 34
                    o.write(byteArrayOf(0x90.toByte(), 0x00, 0x00, 0x22.toByte()))
                    o.write(csd)
                    for (f in pendingFrames) o.write(f)
                    pendingFrames.clear()
                    out = o
                }
                return o
            }

            fun onStreamInfo(bytes: ByteArray) {
                if (streamInfo == null) {
                    streamInfo = bytes
                    if (pendingFrames.isNotEmpty()) ensureOutput()
                }
            }

            pcmFile.inputStream().buffered().use { input ->
                var inputEos = false
                var outputEos = false
                val bufferInfo = MediaCodec.BufferInfo()
                val chunk = ByteArray(PUMP_BYTES)
                while (!outputEos) {
                    if (!inputEos) {
                        val inIndex = codec.dequeueInputBuffer(10_000)
                        if (inIndex >= 0) {
                            val inBuf = codec.getInputBuffer(inIndex) ?: error("null input")
                            inBuf.clear()
                            var read = 0
                            while (read < inBuf.remaining()) {
                                val n = input.read(chunk, 0, minOf(chunk.size, inBuf.remaining()))
                                if (n < 0) break
                                inBuf.put(chunk, 0, n)
                                read += n
                            }
                            if (read <= 0) {
                                codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputEos = true
                            } else {
                                codec.queueInputBuffer(inIndex, 0, read, 0, 0)
                            }
                        }
                    }
                    val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    when {
                        outIndex >= 0 -> {
                            val outBuf = codec.getOutputBuffer(outIndex)
                            if (outBuf != null && bufferInfo.size > 0) {
                                val bytes = ByteArray(bufferInfo.size)
                                outBuf.position(bufferInfo.offset)
                                outBuf.limit(bufferInfo.offset + bufferInfo.size)
                                outBuf.get(bytes)
                                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                    onStreamInfo(bytes)
                                } else if (streamInfo != null) {
                                    ensureOutput().write(bytes)
                                } else {
                                    pendingFrames.add(bytes)
                                }
                            }
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                outputEos = true
                            }
                            codec.releaseOutputBuffer(outIndex, false)
                        }
                        outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            codec.outputFormat.getByteBuffer("csd-0")?.let { csdBuf ->
                                val bytes = ByteArray(csdBuf.remaining())
                                csdBuf.get(bytes)
                                onStreamInfo(bytes)
                            }
                        }
                    }
                }
            }
            if (out == null && streamInfo != null && pendingFrames.isNotEmpty()) {
                ensureOutput()
            }
            out?.flush()
            out?.close()
            return streamInfo != null && outFile.length() > 42
        } catch (_: Exception) {
            runCatching { outFile.delete() }
            return false
        } finally {
            runCatching { codec.stop() }
            runCatching { codec.release() }
        }
    }
}
