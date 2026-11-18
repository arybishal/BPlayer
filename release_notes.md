# BPlayer v1.1.0 — Complete LastWave-Native Adaptation & Release 🎵

Welcome to **BPlayer v1.1.0**, delivering the complete adaptation of the **LastWave-Native Liquid Glass design system** and modern audio architecture into BPlayer with dedicated branding and application ID preservation (`com.pulse.player`).

---

### 🌟 What's New in v1.1.0

- **✨ Complete LastWave-Native Liquid Glass Design System**:
  - Full Liquid Glass optical translucency with 1dp boundary borders and backdrop blur.
  - Google Sans Flex responsive variable typography.
  - 19 curated preset color palettes with dynamic Monet wallpaper adaptation.
  - Pure Black AMOLED mode (`#000000`) and Obsidian Dark theme (`#0F1115`).
  - Strict Warm Palette compliance: zero blue gradients or generic tech templates.

- **📻 Dynamic Home Feed & Discovery**:
  - "Made For You" infinite radio station generated dynamically from listening history.
  - Quick access vibes (Podcasts, Relax, Workout, Energize).
  - Liked Songs and Discover Mix stations.

- **🎧 Audiophile Audio Engine & DSP**:
  - High-precision Oboe C++ native audio engine with low-latency AAudio and OpenSL ES.
  - Decent USB Audio Driver: bit-perfect external USB DAC output with 16KB memory page alignment.
  - 15-band precision graphic equalizer with real-time response curves and presets.
  - AutoEQ Headphone Correction with 4,000+ lab-calibrated frequency curves.
  - Poweramp-style Hardware Signal Path Audit (Source Stream -> Float32 DSP -> HAL Output).

- **📜 Synchronized Precision LRC Lyrics & Editor**:
  - Full-bleed synced lyrics with millisecond tracking, active line glow, and built-in editor.

- **🔒 Privacy & Storage Architecture**:
  - AES-256-GCM encrypted backup & restore with fuzzy track reconciliation (`.bplayerbackup` / `.pulsebackup`).
  - Scoped storage in-place ID3/FLAC/Vorbis audio tag editing.
  - GitHub In-App Updater directly integrated with `https://api.github.com/repos/arybishal/BPlayer/releases`.

---

### 📦 Release Artifacts & Checksums

- **APK File**: `BPlayer-v1.1.0-release.apk`
- **Application ID**: `com.pulse.player`
- **Version Code**: `2`
- **Version Name**: `1.1.0`
- **SHA-256 Checksum**: `F13CFC6C0DBC0D3DD855E4238B4A704EDFA25B01FBFEC500728BD25735E8104F`
- **Target Android**: Android 8.0+ (API 26 to 35)

---

### ⚖️ Licensing
Licensed under the **GNU General Public License v3.0 (GPL-3.0)**. Incorporates open-source components from LastWave-Native and Decent USB Audio.
