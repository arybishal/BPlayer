## BPlayer v1.2.0 — Production Release 🎵

Welcome to **BPlayer v1.2.0**! This major release introduces a complete **user-selectable offline audio download format and native conversion pipeline**, expanded audiophile DSP capabilities, precision AutoEQ headphone calibration, and extensive SEO/visual overhauls.

---

### 🌟 What's New in v1.2.0

#### 1. 💾 User-Selectable Offline Music Download Formats
Configure your preferred offline audio format via **Settings → Audio & Playback → Offline audio format** or **Library → Downloads → Options (3-dots) → Offline audio format**:
- **OPUS (Default)**: Modern streaming standard (~2.5 MB / song) with pristine stream fidelity.
- **MP3**: Universal MPEG-1 Audio Layer III encoded in genuine CBR/VBR via our native **Shine ANSI C engine**. Universal compatibility with vintage car stereos, home theaters, and older media players.
- **AAC / M4A**: High-quality Apple / MPEG-4 standard. Features **Direct Stream Optimization**: automatically downloads native itag 140 streams directly when available without redundant lossy transcoding!
- **FLAC**: Lossless compression container hardware-encoded via Android `MediaCodec` (`audio/flac`).
- **Original Source Format**: Saves raw remote bitstreams without container modification.

#### 2. 🎚️ Documented Quality Presets
- **Standard**: 128 kbps MP3/AAC, 96 kbps Opus (ideal for constrained device storage).
- **High (Default)**: 192 kbps MP3/AAC, 128 kbps Opus (balanced audiophile sweet spot).
- **Maximum**: 320 kbps MP3, 256 kbps AAC, 160 kbps Opus, FLAC bit-exact.
- *Transparent Fidelity Guarantee*: UI dialogs clearly notify users that transcoding from lossy streams cannot recover discarded ultrasonic frequencies.

#### 3. ⚙️ Native Audio Transcoder & Headless PCM Pipeline
- **Compiled Multi-ABI C++ Shared Library (`liblastwave_audio.so`)**: Integrates the lightweight Shine MP3 encoder (LGPL-2.0) and 64-bit Soxr resampler across `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.
- **Headless ExoPlayer PCM Decoder**: Decodes any remote audio container into standard 16-bit linear PCM on a background thread.
- **Atomic File Finalization**: Downloads to `.tmp` files, verifies byte headers and playback duration, injects metadata tags via JAudioTagger, and atomically moves completed tracks to `Music/BPlayer/`.
- **Non-Destructive Integrity**: Changing format preferences affects future downloads only; existing offline songs remain completely untouched and playable.

#### 4. 🎧 AutoEQ Headphone Correction & Audiophile DSP
- Over **4,000 lab-measured headphone calibration curves** (Crinacle, Oratory1990, Rtings, InnerFidelity).
- **10-Band Parametric EQ Solver**: Real-time Levenberg-Marquardt biquad fitter with automatic preamp gain calculation to prevent digital clipping.
- **Poweramp-Style Signal Path Inspector**: End-to-end audit verifying Source Stream → 32-bit Float DSP → AudioTrack HAL bit-perfect routing.

#### 5. 📜 Liquid Glass UI & Synchronized Lyrics
- **Zero Blue Gradients**: Natural obsidian surfaces (`#121214`), AMOLED pitch-black (`#000000`), and warm accents (Warm Amber, Sunset Coral, Deep Emerald).
- **Synchronized LRC Lyrics**: Millisecond-accurate rendering for embedded ID3 `USLT`/`SYLT` and sidecar `.lrc` files with tap-to-seek and in-app timing editor.
- **Smart Transport Control**: Single tap restarts track from `0:00`; double-tap (< 350ms) skips to previous song.

#### 6. 🚗 Android Auto & Connected Ecosystem
- Native **`MediaLibraryService`** browse tree integration for vehicle displays.
- **Last.fm Scrobbler**: Authenticated mobile session with offline SQLite disk cache.
- **AES-256-GCM Encrypted Backup**: Single-file `.pulsebackup` export and restore.

---

### 📦 Release Asset Verification

| Property | Value |
|---|---|
| **File Name** | `app-release.apk` |
| **Package Name** | `com.pulse.player` |
| **Version Name** | `1.2.0` |
| **Version Code** | `3` |
| **File Size** | `27,691,695 bytes` (~26.4 MB) |
| **Target SDK** | Android 15 (API 35) |
| **Minimum SDK** | Android 8.0 (API 26) |
| **SHA-256 Checksum** | `C1F262858B2F448BC940794E86782B3EA03AE4835B9D20B4418898A31384D310` |

---

### 📲 Quick Installation
```bash
# Verify integrity
Get-FileHash app-release.apk -Algorithm SHA256

# Install on your device via ADB
adb install -r app-release.apk
```
