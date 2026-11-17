# BPlayer v1.0.0 — Initial Official Release 🎵

Welcome to the first official release of **BPlayer**, an exquisite, audiophile-grade offline music player for Android built with Jetpack Compose, Material 3, AndroidX Media3 (`MediaLibraryService`), and advanced DSP architecture derived from the open-source LastWave-Native project.

---

### 🌟 What's New in v1.0.0

- **🎧 AutoEQ Headphone Correction Engine**:
  - Over 4,000 lab measurements and calibration curves (Crinacle, Oratory1990, Rtings).
  - Real-time frequency response canvas with target curve compensation (Harman, IEF Neutral, Optimum HiFi).
  - Levenberg-Marquardt biquad filter fitter generating up to 10 parametric filters with auto-preamp.

- **🔬 Poweramp-Style Signal Path Audit**:
  - End-to-end hardware pipeline audit: Source Stream, 32-bit Float DSP Engine, and Android HAL / AudioTrack output.
  - Bit-perfect verdict pill auditing pristine bit-exact playback.

- **📻 Last.fm Scrobbler**:
  - Real-time Now Playing notifications and scrobble tracking at 50% or 240 seconds.
  - Offline retry queue storing scrobbles locally and automatically syncing when back online.
  - Secure MD5 mobile authentication.

- **🔒 Military-Grade Backup & Restore System**:
  - AES-256-GCM authenticated encryption with PBKDF2-HMAC-SHA256 password protection.
  - Backs up app settings, playlists, favorites, custom equalizer presets, and encrypted credentials into a single `.pulsebackup` file.
  - Fuzzy track reconciliation matching songs across device storage by title, artist, album, and duration.

- **🚗 Android Auto & Automotive OS**:
  - Seamless in-car browsing (`ROOT` -> `TRACKS`, `FAVORITES`) via AndroidX `MediaLibraryService`.

- **🔌 Decent USB Audio Driver**:
  - Bit-perfect external USB DAC audio output with 16KB page-aligned native C++ runtime (`liblastwave_audio.so`, `libdecent_usb_audio.so`).

- **🎨 Exquisite Liquid Glass UI (Strictly NO Blue Gradients)**:
  - Eliminated all oval/circular bubbles in favor of sleek Material 3 Bottom Sheets and rounded cards (`14.dp` to `24.dp`).
  - Warm, organic foundation palettes (Charcoal Dark, Pitch Black AMOLED, Warm Light) and curated warm accents (Amber, Coral, Emerald).

- **📜 Synchronized Precision Lyrics & Tag Editor**:
  - Embedded ID3 & sidecar `.lrc` reader with tap-to-seek, built-in LRC editor, and online LRCLIB / Gemini AI fallback.
  - Scoped storage in-place tag editing using JAudioTagger (`MediaStore.createWriteRequest`).

---

### 📦 Installation

Download **`BPlayer-v1.0.0.apk`** below and install it directly on your Android device (Android 8.0 / API 26 or higher).
