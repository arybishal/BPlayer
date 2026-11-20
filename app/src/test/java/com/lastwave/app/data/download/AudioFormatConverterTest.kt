package com.lastwave.app.data.download

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioFormatConverterTest {

    @Test
    fun downloadAudioFormat_fromId_resolvesExpectedFormats() {
        assertThat(DownloadAudioFormat.fromId("opus")).isEqualTo(DownloadAudioFormat.OPUS)
        assertThat(DownloadAudioFormat.fromId("mp3")).isEqualTo(DownloadAudioFormat.MP3)
        assertThat(DownloadAudioFormat.fromId("aac_m4a")).isEqualTo(DownloadAudioFormat.AAC_M4A)
        assertThat(DownloadAudioFormat.fromId("flac")).isEqualTo(DownloadAudioFormat.FLAC)
        assertThat(DownloadAudioFormat.fromId("original")).isEqualTo(DownloadAudioFormat.ORIGINAL)
        // Fallback default
        assertThat(DownloadAudioFormat.fromId(null)).isEqualTo(DownloadAudioFormat.OPUS)
        assertThat(DownloadAudioFormat.fromId("unknown_format")).isEqualTo(DownloadAudioFormat.OPUS)
    }

    @Test
    fun downloadAudioFormat_extensionsAndMimeTypes_areCorrect() {
        assertThat(DownloadAudioFormat.OPUS.extension).isEqualTo("opus")
        assertThat(DownloadAudioFormat.OPUS.mimeType).isEqualTo("audio/ogg")

        assertThat(DownloadAudioFormat.MP3.extension).isEqualTo("mp3")
        assertThat(DownloadAudioFormat.MP3.mimeType).isEqualTo("audio/mpeg")

        assertThat(DownloadAudioFormat.AAC_M4A.extension).isEqualTo("m4a")
        assertThat(DownloadAudioFormat.AAC_M4A.mimeType).isEqualTo("audio/mp4")

        assertThat(DownloadAudioFormat.FLAC.extension).isEqualTo("flac")
        assertThat(DownloadAudioFormat.FLAC.mimeType).isEqualTo("audio/flac")
    }

    @Test
    fun conversionQuality_fromId_resolvesExpectedPresets() {
        assertThat(ConversionQuality.fromId("standard")).isEqualTo(ConversionQuality.STANDARD)
        assertThat(ConversionQuality.fromId("high")).isEqualTo(ConversionQuality.HIGH)
        assertThat(ConversionQuality.fromId("maximum")).isEqualTo(ConversionQuality.MAXIMUM)
        // Fallback default
        assertThat(ConversionQuality.fromId(null)).isEqualTo(ConversionQuality.HIGH)
        assertThat(ConversionQuality.fromId("invalid")).isEqualTo(ConversionQuality.HIGH)
    }

    @Test
    fun conversionQuality_bitrateValues_areDocumentedAndConsistent() {
        assertThat(ConversionQuality.STANDARD.mp3BitrateKbps).isEqualTo(128)
        assertThat(ConversionQuality.STANDARD.aacBitrateKbps).isEqualTo(128)
        assertThat(ConversionQuality.STANDARD.opusBitrateKbps).isEqualTo(96)

        assertThat(ConversionQuality.HIGH.mp3BitrateKbps).isEqualTo(192)
        assertThat(ConversionQuality.HIGH.aacBitrateKbps).isEqualTo(192)
        assertThat(ConversionQuality.HIGH.opusBitrateKbps).isEqualTo(128)

        assertThat(ConversionQuality.MAXIMUM.mp3BitrateKbps).isEqualTo(320)
        assertThat(ConversionQuality.MAXIMUM.aacBitrateKbps).isEqualTo(256)
        assertThat(ConversionQuality.MAXIMUM.opusBitrateKbps).isEqualTo(160)
    }

    @Test
    fun isConversionRequired_returnsFalseWhenAlreadyTargetFormat() {
        val dummyContext = io.mockk.mockk<android.content.Context>(relaxed = true)
        val converter = AudioFormatConverter(dummyContext)

        // Original never requires conversion
        assertThat(converter.isConversionRequired("opus", DownloadAudioFormat.ORIGINAL)).isFalse()
        assertThat(converter.isConversionRequired("mp3", DownloadAudioFormat.ORIGINAL)).isFalse()
        assertThat(converter.isConversionRequired("flac", DownloadAudioFormat.ORIGINAL)).isFalse()
        assertThat(converter.isConversionRequired("m4a", DownloadAudioFormat.ORIGINAL)).isFalse()

        // Same format
        assertThat(converter.isConversionRequired("opus", DownloadAudioFormat.OPUS)).isFalse()
        assertThat(converter.isConversionRequired("mp3", DownloadAudioFormat.MP3)).isFalse()
        assertThat(converter.isConversionRequired("m4a", DownloadAudioFormat.AAC_M4A)).isFalse()
        assertThat(converter.isConversionRequired("mp4", DownloadAudioFormat.AAC_M4A)).isFalse()
        assertThat(converter.isConversionRequired("flac", DownloadAudioFormat.FLAC)).isFalse()

        // Different format
        assertThat(converter.isConversionRequired("opus", DownloadAudioFormat.MP3)).isTrue()
        assertThat(converter.isConversionRequired("webm", DownloadAudioFormat.MP3)).isTrue()
        assertThat(converter.isConversionRequired("opus", DownloadAudioFormat.AAC_M4A)).isTrue()
        assertThat(converter.isConversionRequired("mp3", DownloadAudioFormat.FLAC)).isTrue()
        assertThat(converter.isConversionRequired("flac", DownloadAudioFormat.MP3)).isTrue()
    }
}
