# BPlayer v1.1.0 — Master Brand Identity, Original Logo & Clean Production Release 🎵

Welcome to **BPlayer v1.1.0**, featuring our completely new, original BPlayer geometric soundmark logo identity, theme-aware adaptive icons, clean team attribution, and full standardization on the official BPlayer repository (`https://github.com/arybishal/BPlayer`).

---

### 🌟 Key Highlights & Brand Overhaul

- **✨ Original BPlayer Soundmark Logo**:
  - Distinctive, minimalist, modern emblem: vertical acoustic stem + dual resonant acoustic loops forming an iconic "B" with embedded forward Play Triangle (▶).
  - Theme-aware selection via Android night-qualified resources (`drawable` vs `drawable-night`, `values` vs `values-night`):
    - **Light Mode**: Deep Obsidian Charcoal (`#121316`) + Vivid Hi-Fi Emerald (`#10B981`) on pure white background (`#FFFFFFFF`).
    - **Dark Mode**: Crisp Luminescent White (`#FFFFFF`) + Radiant Emerald (`#34D399`) on obsidian dark background (`#FF100F0F`).
  - Full vector XML deliverables: `ic_bplayer_logo.xml`, `ic_bplayer_logo_mono.xml` (evenOdd fill rule for Android 13+ dynamic themed icons), and complete raster mipmaps (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).
  - Zero-flicker splash screen branding.

- **🛡️ Clean Team Attribution & Strict Identity Isolation**:
  - Removed incorrect country flag (`ic_flag_india.xml`) completely. No nationality or country flags anywhere in the app.
  - Standardized clean team attribution: **Crafted by BPlayer Team**.
  - Purged misleading "Download Desktop App" option and all unused desktop launcher assets.
  - Purged unrelated "More from us" cross-promotions (Sennheiser, Lyricify, Sleep Tracker cards, upstream Telegram/Discord links) without touching actual audio hardware, lyrics engine, or sleep timer functionality.
  - Standardized app name to **BPlayer** across all 14 localized translation resource catalogs.
  - Cleaned expanded player notifications to `BPLAYER  •  NOW PLAYING`.
  - Updated lyrics share cards with BPlayer watermark branding.

- **🌐 Official Repository Integration**:
  - Source code, issue tracker, in-app updater, and release discovery integrated exclusively with `https://github.com/arybishal/BPlayer`.
  - Update API connected to `https://api.github.com/repos/arybishal/BPlayer/releases/latest`.

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

---

### 📦 Release Artifacts & Checksums

- **APK File**: `BPlayer-v1.1.0-release.apk`
- **Application ID**: `com.pulse.player` (preserved for install compatibility)
- **Version Code**: `2`
- **Version Name**: `1.1.0`
- **File Size**: `27,560,623` bytes (26.28 MB)
- **SHA-256 Checksum**: `EFE887A89088E24BF8613F1391795C53B58F5F72CB7DE884F3443C7A2443BAE7`
- **Target Android**: Android 8.0+ (API 26 to 35)

---

### ⚖️ Licensing
Licensed under the **GNU General Public License v3.0 (GPL-3.0)**. Incorporates open-source components from LastWave-Native and Decent USB Audio.
