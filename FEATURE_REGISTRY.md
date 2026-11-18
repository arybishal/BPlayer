# FEATURE_REGISTRY.md — BPlayer & LastWave Feature Parity & Single-Source-of-Truth Registry

This document establishes the definitive feature registry for **BPlayer**. In accordance with Section 5 of the BPlayer Adaptation Directives, **every feature has exactly one canonical home, one data source, and one logical implementation** to guarantee zero duplication, zero competing settings, and seamless UX continuity.

---

## 1. Feature Registry & Single Source of Truth

| Feature ID | Canonical Feature Name | Canonical Navigation Destination | Canonical Settings Location | Domain / Engine Implementation | Data Source | Original BPlayer Status | LastWave Reference Status | Final Integration Decision |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `FEAT-001` | **High-Fidelity Audio Engine** | Mini Player & Full Now Playing Screen | `Settings > Audio & Playback` | Media3 / ExoPlayer + Oboe C++ Engine + Decent USB Audio Driver | Local files, Cache, Streaming URIs | ExoPlayer with basic pipeline | Oboe + Float32 DSP + Decent USB DAC | **Adopt LastWave Native Engine** with 16KB page-aligned USB audio driver and ExoPlayer core. |
| `FEAT-002` | **10-Band Graphic Equalizer** | Now Playing Menu > Equalizer | `Settings > Audio & Playback > Equalizer` | Graphic EQ with Preamp, 10 Biquad filters, Bass Boost & Virtualizer | Android AudioEffect + Custom DSP | Basic 5-band EQ sheet | 10-band responsive EQ sheet with presets | **Adopt LastWave 10-Band EQ**; replace old basic 5-band sheet. Single canonical EQ entry. |
| `FEAT-003` | **AutoEQ Headphone Correction** | Now Playing Menu > AutoEQ | `Settings > Audio & Playback > AutoEQ` | 4,000+ Headphone lab calibration curves with Levenberg-Marquardt biquad solver | Local AutoEQ SQLite DB / Raw assets | Standalone AutoEQ dialog | Full-screen AutoEQ browser + frequency canvas | **Adopt LastWave AutoEQ Screen & Canvas**; eliminate duplicate dialog. Single canonical AutoEQ entry. |
| `FEAT-004` | **Hardware Signal Path Audit** | Now Playing Menu > Signal Path | N/A (Inspected from active playback) | Poweramp-style telemetry auditing Source Stream, Float32 DSP Engine, and Android HAL / AudioTrack | Active AudioTrack & MediaSession | Standalone Signal Path Sheet | Interactive Signal Path Modal with bit-depth / sample-rate metrics | **Adopt LastWave Signal Path Modal**. |
| `FEAT-005` | **Synchronized LRC Lyrics & Editor** | Now Playing > Lyrics Tab / Screen | `Settings > Audio & Playback > Lyrics` | Precision timestamped LRC parser, smooth auto-scroll, text highlights, translation & in-app tag editor | Local `.lrc` files, ID3 SYLT tags, LRCLIB / NetEase APIs | Basic LRC parser and editor | Full-bleed synchronized lyrics with edit modal | **Adopt LastWave Lyrics & Editor**; unified lyrics engine. |
| `FEAT-006` | **Dynamic Home Feed & Discovery** | Bottom Navigation > Home | N/A | Dynamic discovery feed: Made for You, Quick Access, Infinite Radio, Moods & Top Mixes | Room DB listening history + InnerTube recommendations | Static recent songs row | Dynamic modular cards & infinite stations | **Adopt LastWave Home Feed**. |
| `FEAT-007` | **Local Music Library** | Bottom Navigation > Library | `Settings > Library & Storage` | Fast Room DB indexing, diffing against MediaStore, folder filtering, duration threshold | MediaStore + Room Database | Room DB (`tracks`, `playlists`, `favorites`) | Room DB with tabs (Songs, Albums, Artists, Playlists, Folders) | **Adopt LastWave Library Navigation & Room DB schema**, preserving user track metadata and playlists. |
| `FEAT-008` | **Scoped Storage Tag Editor** | Library Track Menu > Edit Tags | N/A (Contextual action) | In-place metadata editing via `MediaStore.createWriteRequest` and JAudioTagger | Scoped Storage File Descriptors | Custom Scoped Storage Tag Editor | Integrated track metadata sheet | **Merge BPlayer Scoped Storage JAudioTagger Engine** into track options menu. |
| `FEAT-009` | **Universal Search** | Top Header / Bottom Nav > Search | N/A | Instant debounced (300ms) multi-index search across tracks, artists, albums, and online catalog | Room DB + InnerTubeX | Basic local search | Full search with chips, history, and suggestions | **Adopt LastWave Universal Search**. |
| `FEAT-010` | **Acoustic Music Recognition** | Top Header > Identify Music icon | N/A | 16 kHz mono microphone capture, spectral peak signature generation, audio fingerprinting | Microphone input + AudD / ACRCloud API | AudD standalone recognition sheet | Integrated header icon + waveform recognition sheet | **Adopt LastWave Identify Music Sheet & UI** backed by BPlayer's verified recognition API tokens. |
| `FEAT-011` | **Encrypted Backup & Restore** | N/A | `Settings > Library & Storage > Backup & Restore` | AES-256-GCM encryption with PBKDF2 key derivation, fuzzy track reconciliation | SAF file export/import (`.bplayerbackup` / `.pulsebackup`) | Full AES-256 backup engine | None (LastWave lacked encrypted backup) | **Integrate BPlayer Encrypted Backup Engine** into `Settings > Library & Storage`. Single canonical backup destination. |
| `FEAT-012` | **In-App GitHub Updates** | N/A | `Settings > About > App Updates` | Semantic versioning comparison, SHA-256 APK signature verification, package installer intent | GitHub Releases API (`arybishal/BPlayer`) | GitHub update checker sheet & dialog | GitHub update checker for LastWave | **Adapt Updater to BPlayer Repository (`arybishal/BPlayer`)** under `Settings > About > App Updates`. |
| `FEAT-013` | **Last.fm Scrobbler** | N/A | `Settings > Last.fm Scrobbler` | Real-time Now Playing updates, 50%/240s scrobble triggers, MD5 authentication, offline queue | Last.fm Web Services API 2.0 | Room-based offline scrobbler | Full Last.fm integration screen | **Adopt LastWave Last.fm Module** with offline resilience. |
| `FEAT-014` | **Spotify Sync** | N/A | `Settings > Spotify Sync` | OAuth2 PKCE token exchange, playlist importing, saved library syncing | Spotify Web API | None | Complete Spotify sync module | **Adopt LastWave Spotify Sync**. |
| `FEAT-015` | **YouTube Music Streaming & Downloads** | Search / Home / Downloads | `Settings > YouTube Music` | InnerTubeX API integration, audio stream extraction, chunked background downloading | InnerTubeX + NewPipeExtractor | None | Complete streaming & download manager | **Adopt LastWave YouTube Music & Download Engine**. |
| `FEAT-016` | **Jam Sessions (Local P2P)** | Now Playing > Jam | `Settings > Jam Sessions` | Network Service Discovery (NSD), local WebSocket sync, shared playback queue | Local Wi-Fi network | None | Full Jam session manager | **Adopt LastWave Jam Module**. |
| `FEAT-017` | **Diagnostics & Error Logging** | N/A | `Settings > About > Diagnostics` | SQLite error log persistence (`error_logs` table), uncaught exception interceptor, shareable log bundle | Room SQLite DB | Custom AppLogger & error logs table | Diagnostics export action | **Wire BPlayer SQLite AppLogger** into `Settings > About > Diagnostics` for complete diagnostic export. |
| `FEAT-018` | **Appearance & Theme System** | N/A | `Settings > Appearance` | Liquid Glass translucency (`Modifier.blur` + subtle white borders), Google Sans Flex typography, 19 preset palettes, Obsidian Dark, AMOLED Pure Black, Monet dynamic | DataStore preferences | Custom warm amber / terracotta palette | Comprehensive 19-palette engine with Liquid Glass | **Adopt LastWave Complete Theme System**. Discard old `#E07A5F` / `#F4A261` tokens completely. |
| `FEAT-019` | **Sleep Timer & Smooth Fadeout** | Now Playing Menu > Sleep Timer | N/A (Accessed from player) | Coroutine-driven countdown timer with exponential volume attenuation over 10 seconds | Runtime playback controller | Sleep timer sheet | Sleep timer dialog with custom durations | **Adopt LastWave Sleep Timer Modal** with smooth volume fadeout. |

---

## 2. Duplicate Prevention Rules

1. **Equalizer**: Exactly one EQ entry point located in `Settings > Audio & Playback > Equalizer` and via the Now Playing options menu. No duplicate EQ dialogs or competing presets.
2. **AutoEQ**: Exactly one AutoEQ browser located in `Settings > Audio & Playback > AutoEQ` and via Now Playing. Old popups removed.
3. **Settings Navigation**: Settings is organized strictly following LastWave's hierarchy:
   - `Appearance` (Theme, Palettes, AMOLED, Liquid Glass, Typography)
   - `Audio & Playback` (Audio engine, Equalizer, AutoEQ, Decent USB DAC, ReplayGain, Crossfade, Gapless, Lyrics)
   - `Library & Storage` (Folders, Rescan, Cache, **Backup & Restore**)
   - `Spotify Sync`
   - `Last.fm Scrobbler`
   - `YouTube Music`
   - `Jam Sessions`
   - `Modules`
   - `About BPlayer` (Version, Credits, **App Updates**, **Diagnostics**)
4. **No Parallel Playback Engines**: All audio playback flows exclusively through the unified player service and audio engine.
