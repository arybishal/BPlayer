# BPlayer 🎵

**BPlayer** is an exquisite, high-fidelity offline audio player for Android engineered with Jetpack Compose, Material 3, AndroidX Media3 (`MediaLibraryService`), and advanced DSP architecture derived from the open-source LastWave-Native project.

Designed with a relentless focus on audiophile-grade playback fidelity, fluid physics-based micro-interactions, robust background playback, and complete audio format flexibility.

---

## 🌟 Key Features

### 🎧 AutoEQ Headphone Correction Engine
- **4,000+ Headphone Lab Calibration Curves**: Searchable database of frequency response profiles measured from Crinacle, Oratory1990, Rtings, InnerFidelity, and more.
- **Interactive Frequency Response Canvas**: Real-time visual comparison between raw headphone measurements, target curves (Harman 2020, IEF Neutral, Optimum HiFi), and the compensated EQ filter curve.
- **Parametric EQ Solver**: Levenberg-Marquardt biquad filter fitter generating up to 10 parametric peaking and shelving filters with automatic preamp gain calculation to prevent digital clipping.

### 🔬 Poweramp-Style Signal Path Audit
- **End-to-End Pipeline Inspector**: Inspect audio fidelity across three discrete pipeline stages:
  1. **Source Stream**: Container, codec, sample rate, bit depth, channel configuration, and bitrate.
  2. **DSP Engine**: 32-bit floating-point processing, parametric equalizer, AutoEQ biquads, bass boost, and virtualizer status.
  3. **Android HAL / AudioTrack Output**: Output sample rate, buffer size, latency, and hardware routing.
- **Bit-Perfect Verdict**: Instant badge auditing whether the signal is bit-perfect or processed through the DSP pipeline.

### 📻 Last.fm Scrobbler
- **Seamless Scrobbling**: Automatic now-playing notifications and scrobble submissions (triggered at 50% playback duration or 240 seconds).
- **Offline Scrobble Queue**: SQLite-backed offline caching that queues scrobbles during offline playback and automatically flushes them when connectivity is restored.
- **Mobile Session Authentication**: Secure MD5 signature generation without storing plaintext passwords in memory.

### 🔒 Encrypted Backup & Restore System
- **Military-Grade Encryption**: AES-256-GCM authenticated encryption with PBKDF2-HMAC-SHA256 key derivation protecting sensitive API keys and personal playlists.
- **Cross-Device Track Reconciliation**: Multi-pass fuzzy matching engine comparing track title, artist, album, duration, and metadata distance so playlists and favorites are restored reliably across different devices.
- **Storage Access Framework (SAF)**: Native file picker integration for single-file `.pulsebackup` exports and restores.

### 🚗 Android Auto & Automotive OS
- **MediaLibraryService Architecture**: Exposes full local audio browse trees (`ROOT` -> `TRACKS`, `FAVORITES`) to Android Auto head units and Automotive OS.
- **Vehicle Transport Controls**: Standardized media controls, track artwork, and metadata integration for in-car displays.

### 🔌 Decent USB Audio Driver
- **Bit-Perfect USB DAC Output**: Direct USB host audio streaming bypassing Android audio flinger mixer resamplers for external USB DACs.
- **16KB Page Alignment**: Fully compliant with Android 15+ 16KB memory page sizes on native C++ libraries (`liblastwave_audio.so`, `libdecent_usb_audio.so`).

### 🎨 Modern Liquid Glass Design (Strictly NO Blue Gradients)
- **Zero Oval / Bubble Artifacts**: Refined Material 3 Bottom Sheets and sleek rounded cards (`14.dp` to `24.dp`).
- **Warm & Organic Foundation**: Warm neutral charcoals (`#121214`), soft warm daylight creams (`#F9F9FB`), and true pitch-black AMOLED (`#000000`).
- **Curated Accents**: Warm Amber/Terracotta (`#F4A261`), Sunset Coral (`#FF6B6B`), and Deep Emerald (`#2EC4B6`).
- **Dynamic Palette Extraction**: WCAG AA contrast enforcement against album art dominant tones.
- **Typography Scale**: Dynamically scales text size proportionally (85% to 140%).

### 📜 Synchronized Lyrics & In-App LRC Editor
- **Embedded & Sidecar LRC**: Reads ID3 `USLT`/`SYLT` tags and local sidecar `.lrc` files with high-precision timestamp sorting.
- **Online Fetch & AI Fallback**: Automatic lookup via LRCLIB with optional Gemini AI fallback for missing lyrics.
- **Tap-to-Seek & Editor**: Tap any lyric line to instantly jump playback position, or use the built-in editor to fix timing.

### ⚡ Smart Transport & Background Reliability
- **Smart Previous**: Single tap restarts current track; double-tap (< 350ms) skips to previous track.
- **Rock-Solid Background Audio**: Handles audio focus ducking, `ACTION_AUDIO_BECOMING_NOISY` headphone disconnect pauses, and OEM battery restriction guides.

---

## 🛠 Tech Stack

| Layer | Technologies |
|---|---|
| **Architecture** | MVVM + Unidirectional Data Flow (UDF) + Repository Pattern |
| **Language** | Kotlin 2.0 (Coroutines & StateFlow) + Modern C++20 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Audio Playback** | AndroidX Media3 (`ExoPlayer`, `MediaLibraryService`, `MediaController`) |
| **Native DSP** | C++ JNI (`ParametricEq`, `DspProcessor`, `DsdDecimator`, `DecentUsbAudio`) |
| **Dependency Injection** | Hilt (Dagger) |
| **Local Database** | Room (SQLite) with KSP2 |
| **Preferences** | Jetpack DataStore (Preferences) |
| **Tag Editing** | Scoped Storage (`MediaStore.createWriteRequest`) + JAudioTagger |
| **Image Loading** | Coil Compose |
| **Min / Target SDK** | Min SDK 26 (Android 8.0) • Target SDK 35 (Android 15) |

---

## 🚀 Building & Installing

### Clone the Repository
```bash
git clone https://github.com/arybishal/BPlayer.git
cd BPlayer
```

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```
The signed release APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License & Attribution

- Portions of DSP processing, AutoEQ algorithms, and USB Audio driver are derived from **LastWave-Native** under the **GNU General Public License v3.0**.
- Pulse / BPlayer application code, UI architecture, and backup cryptography are licensed under the **GNU GPL v3.0**.
