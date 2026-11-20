# Changelog — BPlayer

All notable changes to **BPlayer** are documented in this file.

## [1.2.0] — 2026-11-20

### Added
- **User-Selectable Offline Download Formats**:
  - Added canonical setting in **Settings → Audio & Playback → Offline audio format** and **Downloads → Options (3-dots) → Offline audio format**.
  - 5 formats supported: `OPUS` (default), `MP3`, `AAC / M4A`, `FLAC`, and `Original source format`.
  - 3 quality presets: `Standard` (128 kbps), `High` (192 kbps), and `Maximum` (320 kbps MP3 / bit-exact FLAC).
  - Clear user disclosure explaining that transcoding from lossy streams cannot recover discarded fidelity.
- **Genuine Native Transcoding Pipeline**:
  - Integrated ANSI C Shine MP3 encoder (LGPL-2.0) into `liblastwave_audio.so` across all 4 Android ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).
  - Integrated Soxr high-res resampler handling arbitrary sample rates up to 96+ kHz.
  - Headless ExoPlayer 16-bit linear PCM decoding with `CapturingAudioSink`.
  - Android `MediaCodec` + `MediaMuxer` hardware encoding for AAC (`audio/mp4a-latm`).
  - Android `MediaCodec` encoding for FLAC (`audio/flac`).
  - Direct stream optimization: Automatically fetches native itag 140 AAC stream when AAC/M4A format is requested, avoiding lossy re-encoding.
- **Atomic Download Finalization**:
  - Staged downloads in `.tmp` files, verified byte headers and length before metadata tagging via `AudioTagWriter` (JAudioTagger).
  - Atomic rename into `Music/BPlayer/` ensuring zero incomplete or corrupted downloads appear in the user's library.
- **Testing & Verification**:
  - Added unit test suite `AudioFormatConverterTest` (100% passing).
  - Added live on-device instrumented test suite `LiveAudioFormatConversionTest` on `emulator-5554` (100% passing: standard MP3, 320k vs 128k sizing, 96 kHz downsampling, corrupt input handling).
  - QA screenshots captured for format dialog, quality presets, and dynamic options menu.

### Changed
- Standardized download folder preference default to `BPlayer`.
- Updated documentation: `docs/AUDIO_FORMATS.md`, `docs/DECISIONS.md` (ADR 013), `docs/FEATURES.md`, and `README.md`.
