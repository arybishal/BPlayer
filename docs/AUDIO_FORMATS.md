# Audio Format Support Matrix & Decoder Architecture — Pulse

## 1. Executive Summary & Philosophy

A premier offline music player must handle diverse audiophile collections without failing or crashing. However, Android's native multimedia framework (`MediaCodec`) has clear boundaries. 

Pulse adopts a **hybrid decoding strategy**:
1. **Tier 1 (Native Media3 / ExoPlayer)**: Zero APK overhead, hardware-accelerated where possible, handles 95%+ of typical consumer audio (MP3, AAC, FLAC, WAV, Ogg Vorbis, Opus).
2. **Tier 2 (Media3 FFmpeg Decoder Extension)**: Software decoding fallback compiled via Android NDK for audiophile and legacy formats (APE, WavPack, ALAC in raw containers, AIFF, WMA).
3. **Graceful Handling & Capability Validation**: When encountering an unsupported format (e.g. proprietary ATRAC or raw multi-channel DSD without DoP DAC), the app gracefully notifies the user with clear format telemetry rather than crashing.

---

## 2. Comprehensive Audio Format Support Matrix

| Category | Format / Container | File Extension | Decoder Pipeline | Status | Technical Details & Limitations |
|---|---|---|---|---|---|
| **Lossy** | MP3 | `.mp3` | Android MediaCodec / ExoPlayer | **Native** | Complete support (ID3v1, ID3v2.3, ID3v2.4). CBR and VBR. |
| **Lossy** | AAC | `.m4a`, `.aac` | Android MediaCodec / ExoPlayer | **Native** | LC, HE-AAC, and HEv2 supported up to 48kHz. |
| **Lossy** | Ogg Vorbis | `.ogg`, `.oga` | Media3 Default Extractor | **Native** | Full Vorbis comment support, gapless playback. |
| **Lossy** | Opus | `.opus`, `.ogg` | Android MediaCodec (API 29+) / Media3 | **Native** | Excellent low-bitrate performance; native in modern Android. |
| **Lossy** | WMA Standard | `.wma` | FFmpeg Extension (API fallback) | **FFmpeg** | MediaCodec support is OEM-dependent; FFmpeg ensures universal support. |
| **Lossy** | Musepack (MPC) | `.mpc` | FFmpeg Extension | **FFmpeg** | Legacy format; decoded via libavcodec `mpc7` / `mpc8`. |
| **Lossy** | ATRAC (OMGAudio) | `.oma`, `.aa3` | Unsupported / Deprecated | **Unsupported** | Sony proprietary DRM/codecs; legal and decoder licensing barriers. |
| **Lossless** | FLAC | `.flac` | Android MediaCodec / Media3 | **Native** | Up to 24-bit / 192kHz hi-res; full seek table & embedded art. |
| **Lossless** | ALAC | `.m4a`, `.alac`| Android MediaCodec / Media3 | **Native** | Supported natively inside MP4 container (`audio/mp4a-latm`). |
| **Lossless** | Monkey's Audio (APE)| `.ape` | FFmpeg Extension | **FFmpeg** | High CPU overhead in "Insane" compression; handled via FFmpeg. |
| **Lossless** | WavPack | `.wv` | FFmpeg Extension | **FFmpeg** | Supports lossless and hybrid compression modes. |
| **Lossless** | True Audio (TTA) | `.tta` | FFmpeg Extension | **FFmpeg** | Decoded via FFmpeg `tta` decoder. |
| **Lossless** | WMA Lossless | `.wma` | FFmpeg Extension | **FFmpeg** | Decoded via software FFmpeg pipeline. |
| **Uncompressed**| WAV (PCM) | `.wav` | Media3 Default Extractor | **Native** | 16/24/32-bit integer PCM and IEEE float. |
| **Uncompressed**| AIFF | `.aif`, `.aiff`| FFmpeg Extension / Media3 | **FFmpeg** | Apple uncompressed format; decoded cleanly via FFmpeg. |
| **Uncompressed**| DSD (Direct Stream Digital)| `.dsf`, `.dff` | Software DSD-to-PCM Transcoder | **Transcode** | Real-time decimation to 24-bit / 88.2kHz or 176.4kHz PCM for Android AudioTrack. |

---

## 3. Media3 FFmpeg Extension Architecture

AndroidX Media3 provides an official **FFmpeg Audio Renderer Extension** (`media3-decoder-ffmpeg`).

### 3.1 Build Steps & Configuration
To include software decoding without bloating the APK with unnecessary video codecs, FFmpeg is built from source targeting only required audio decoders:

```bash
# Set NDK path and target ABI
export NDK_PATH=/path/to/android-ndk-r26
export HOST_PLATFORM=linux-x86_64

# Configure FFmpeg build flags for Pulse (Audio Only)
./configure \
    --prefix=android-build \
    --enable-cross-compile \
    --target-os=android \
    --arch=arm64 \
    --cpu=armv8-a \
    --disable-everything \
    --disable-programs \
    --disable-doc \
    --enable-decoder=ape \
    --enable-decoder=wavpack \
    --enable-decoder=alac \
    --enable-decoder=wmav1 \
    --enable-decoder=wmav2 \
    --enable-decoder=wmalossless \
    --enable-decoder=tta \
    --enable-decoder=mpc7 \
    --enable-decoder=mpc8 \
    --enable-decoder=flac \
    --enable-decoder=pcm_s16le \
    --enable-decoder=pcm_s24le \
    --enable-decoder=pcm_s32le \
    --enable-decoder=dsd_lsbf \
    --enable-decoder=dsd_msbf \
    --enable-shared \
    --disable-static
```

### 3.2 APK Size and Architectural Tradeoffs
- **APK Size Cost**: Compiling FFmpeg for `arm64-v8a` and `armeabi-v7a` adds **~3.2 MB** to the total APK size (using ABI splits in production reduces this impact to ~1.6 MB per device).
- **Licensing & LGPL Compliance**:
  - Pulse compiles FFmpeg under **LGPL 2.1+** (strictly omitting `--enable-gpl` and `--enable-nonfree`).
  - Dynamic `.so` linking is maintained to comply with LGPL relinking requirements.
  - License notice and build instructions are bundled in the app's **About > Open Source Licenses** section.

---

## 4. DSD (Direct Stream Digital) Strategy

Android's standard audio framework (`AudioTrack`) operates strictly on Linear PCM audio. Direct bitstream DSD output requires specialized USB DACs supporting DoP (DSD over PCM) or native DSD over USB.

**Pulse's Implementation**:
1. For internal speakers and standard 3.5mm/Bluetooth headphones: DSD bitstreams (`.dsf` / `.dff`) are converted in real time to high-precision **24-bit / 88.2 kHz PCM** using high-order FIR decimation filters.
2. In the UI, the track is badged as `DSD64 (PCM Converted)` or `DSD128` so audiophiles are completely aware of the signal path.

---

## 5. Format Capability Validation & Safe Fallback

Pulse checks track playability before attempting audio playback to prevent ANRs or silent playback states:

```kotlin
sealed interface FormatCompatibility {
    object SupportedNative : FormatCompatibility
    object SupportedFFmpeg : FormatCompatibility
    data class Unsupported(val reason: String, val suggestedAction: String) : FormatCompatibility
}

fun checkAudioPlayability(mimeType: String, fileExtension: String): FormatCompatibility {
    return when (fileExtension.lowercase()) {
        "mp3", "aac", "m4a", "flac", "wav", "ogg", "opus" -> FormatCompatibility.SupportedNative
        "ape", "wv", "tta", "mpc", "wma", "aiff", "dsf", "dff" -> {
            if (isFFmpegExtensionLoaded()) FormatCompatibility.SupportedFFmpeg
            else FormatCompatibility.Unsupported(
                reason = "Extended decoder required for .$fileExtension files.",
                suggestedAction = "Install Pulse Extended Decoder plugin or enable FFmpeg in settings."
            )
        }
        "oma", "aa3", "atrac" -> FormatCompatibility.Unsupported(
            reason = "Proprietary ATRAC DRM format is not supported.",
            suggestedAction = "Convert audio file to FLAC or MP3 on your computer."
        )
        else -> FormatCompatibility.Unsupported(
            reason = "Unknown audio container format.",
            suggestedAction = "Check file integrity."
        )
    }
}
```

---

## 6. High-Resolution Audio Badging

Pulse inspects extracted file headers and displays visual badges in the Track Row, Now Playing screen, and Song Details sheet:

| Badge Display | Condition | Visual Token |
|---|---|---|
| **HI-RES AUDIO** | Sample Rate ≥ 88.2 kHz OR Bit Depth ≥ 24-bit | Accent Pill (Gold / Amber border) |
| **FLAC** | Container is FLAC (16-bit / 44.1 kHz) | Subtle Surface Pill |
| **DSD** | DSD64, DSD128, DSD256 (`.dsf` / `.dff`) | Accent Filled Pill |
| **LOSSLESS** | ALAC, WAV, APE, WavPack | Outline Pill |
| **STANDARD** | MP3, AAC, OGG (< 320 kbps) | Muted Caption |
