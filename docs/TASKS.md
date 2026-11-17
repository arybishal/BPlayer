# Project Milestones & Task Checklist — Pulse

This tracker serves as the authoritative milestone checklist for the development of **Pulse**. Each task must be checked off (`[x]`) upon completion and verification.

---

## Milestone 0: Foundations & Architecture Skeleton (M0)
- [x] Initialize Android project structure (Gradle Version Catalog `libs.versions.toml`, Min SDK 26, Target SDK 34/35).
- [x] Configure Hilt dependency injection, Coroutines, and AndroidX Lifecycle.
- [x] Implement foundational Design System in `ui/theme`:
  - [x] Color tokens (Light, Dark, AMOLED Black) with strict **No Blue Gradients** rule.
  - [x] Typography scale (Plus Jakarta Sans) with dynamic font-scale multiplier ($S$).
  - [x] Elevation, corner radii (8/12/20/28/full), and spacing grid (4dp).
  - [x] Liquid Glass modifier with `RenderEffect` (API 31+) and graceful fallback (API 26–30).
- [x] Configure Material Symbols Rounded iconography.
- [x] Build navigation scaffold (Home, Library, Search, Settings) with bottom bar.
- [x] Implement `AppLogger` utility and Room-backed `error_logs` table with an uncaught exception handler.
- [x] Build basic **Settings > Error Logs** viewer (list, severity badge, copy/share/clear).

---

## Milestone 1: Library Indexing & Browsing (M1)
- [x] Build Just-In-Time media permission request flow (`READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`).
- [x] Implement Room database schema (`TRACK`, `ALBUM`, `ARTIST`, `PLAYLIST`, `QUEUE_ITEM`).
- [x] Build high-performance `MediaStore` audio scanner with incremental diffing and pull-to-refresh.
- [x] Build Library screen with 6 tabs:
  - [x] Tracks list (thumbnail, title, artist, duration, format badge).
  - [x] Albums list/grid toggle.
  - [x] Artists list/grid toggle.
  - [x] Genres list.
  - [x] Folders physical file tree browser.
  - [x] Playlists tab.
- [x] Implement sorting engine (Title, Artist, Album, Date Added, Duration, Play Count).
- [x] Implement fast-scroll alphabet index scrubber with popup bubble.

---

## Milestone 2: Playback Engine Core & Transport (M2)
- [x] Implement `PlaybackService` extending AndroidX Media3 `MediaSessionService`.
- [x] Configure `ExoPlayer` instance with audio attributes (usage: `USAGE_MEDIA`, content type: `CONTENT_TYPE_MUSIC`).
- [x] Build rich media foreground notification with album art and transport controls.
- [x] Implement Audio Focus manager (ducking on alerts, pause on incoming calls).
- [x] Implement `BECOMING_NOISY` receiver (auto-pause when headphones unplugged).
- [x] Implement `MediaController` connection bridge for UI state observation.
- [x] Build persistent Mini Player:
  - [x] Track thumbnail, title, artist.
  - [x] Play/Pause morph icon button, Next button.
  - [x] Swipe gestures (up to expand, left/right for next/prev).
- [x] Build Hero Seek Bar with expanding thumb and live elapsed/remaining timestamps.
- [x] Implement **Smart Previous Button**:
  - [x] Single tap: instant seek to 0:00.
  - [x] Double tap (< 350ms): jump to previous track.
  - [x] Previous button behavior setting (Smart, Always Previous, Always Restart).
- [x] Implement Shuffle (non-repeating index mapping) and Repeat modes (Off, All, One).
- [x] Build Queue Manager with Room persistence, drag-to-reorder, and swipe-to-delete.

---

## Milestone 3: Hero Now Playing Screen & Polish (M3)
- [x] Implement full-screen modal Now Playing container with collapse gesture (swipe down / back).
- [x] Build `HorizontalPager` with 3 dedicated pages and dot indicator:
  - [x] Page 1: Hero Player View (giant cover, typography, seek bar, transport pod).
  - [x] Page 2: Synchronized Lyrics View.
  - [x] Page 3: High-Res Album Art Viewer.
- [x] Implement AndroidX Palette dynamic color extraction with WCAG AA contrast enforcement.
- [x] Implement shared-element bounds transition between Mini Player artwork and Now Playing hero artwork.
- [x] Add micro-interactions:
  - [x] Animated play/pause icon morph.
  - [x] Heart favorite button particle burst.
  - [x] Subtle album art breathing scale between playing and paused states.

---

## Milestone 4: Search, Playlists & Song Actions (M4)
- [x] Implement instant search screen with 300ms debouncing and highlighted match text.
- [x] Build Playlist management: create, rename, delete, add/remove tracks, reorder tracks.
- [x] Build Favorites collection with one-tap heart toggle.
- [x] Implement contextual Song Actions bottom sheet:
  - [x] Play Next / Add to Queue.
  - [x] Add to Playlist.
  - [x] Go to Artist / Go to Album deep navigation.
  - [x] Technical Song Details dialog (bitrate, sample rate, bit depth, container, path).
  - [x] Share song (audio file via `FileProvider` or text title-artist).
  - [x] Scoped storage song deletion (`MediaStore.createDeleteRequest`).

---

## Milestone 5: Synchronized & Online Lyrics (M5)
- [x] Implement ID3 embedded lyrics reader (`USLT` / `SYLT` frames via JAudioTagger).
- [x] Implement local sidecar `.lrc` file reader and parser with high-precision timestamp sorting.
- [x] Build synchronized lyrics UI:
  - [x] Active line glow and vertical center auto-scrolling.
  - [x] Tap-to-seek to jump audio position to tapped line.
- [x] Implement LRCLIB API client for fetching synchronized and plain text lyrics online.
- [x] Implement Room `lyrics_cache` table to persist lyrics offline.
- [x] Integrate optional Gemini AI fallback for missing lyrics with clear `⚠️ AI-Generated (Unverified)` badge.
- [x] Build in-app lyrics editor allowing users to fix timestamps or write text.

---

## 6. Milestone 6: Tag Editor & Album Art Discovery (M6)
- [x] Implement scoped storage write flow using `MediaStore.createWriteRequest` (Android 11+).
- [x] Integrate JAudioTagger for in-place tag editing (Title, Artist, Album, Year, Genre, Track Number).
- [x] Build Tag Editor bottom sheet with field validation.
- [x] Implement online album art search pipeline (MusicBrainz + Cover Art Archive, iTunes API, Deezer API).
- [x] Build 3-column candidate artwork picker dialog.
- [x] Add "Pick artwork from device gallery" option using Android Photo Picker.
- [x] Embed artwork into audio file tags or save to app cache and update Room.

---

## 7. Milestone 7: Equalizer, Audio Effects & Sleep Timer (M7)
- [x] Implement system-backed Equalizer attached to ExoPlayer audio session ID:
  - [x] 5–10 band sliders with decibel scale.
  - [x] Presets dropdown (Rock, Pop, Jazz, Flat, etc.) and custom preset saver.
- [x] Implement Bass Boost, Virtualizer, and Loudness Enhancer with graceful unsupported checks.
- [x] Build Sleep Timer:
  - [x] Presets: 15, 30, 45, 60 minutes, Custom Minute Picker, and "End of current track".
  - [x] Countdown chip on Now Playing screen.
  - [x] 10-second smooth volume fade-out prior to pause.

---

## 8. Milestone 8: Settings, Theming & Battery Optimization (M8)
- [x] Build Appearance settings:
  - [x] Theme switcher: Light, Dark, System, AMOLED Black.
  - [x] Font size slider (85% to 140%) scaling typography dynamically.
  - [x] Background style: Solid, Album-art blurred, Custom image, Dynamic color.
  - [x] Liquid Glass effect toggle.
  - [x] Curated accent color picker (Amber, Coral, Emerald).
- [x] Build Playback settings:
  - [x] Smart Previous behavior toggle.
  - [x] Gapless playback toggle and crossfade slider (0–10s).
- [x] Build Battery & Background Playback settings:
  - [x] Battery exemption status indicator.
  - [x] "Allow Unrestricted Battery Use" action triggering system request.
  - [x] OEM background setup guides (Xiaomi, Samsung, OnePlus, Oppo, Vivo, Huawei).
- [x] Build Online settings:
  - [x] Encrypted Gemini API key storage and "Test Key" ping.
  - [x] Lyrics and artwork source prioritization order.

---

## 9. Milestone 9: Extended Audio Formats & FFmpeg (M9)
- [x] Evaluate Media3 FFmpeg decoder extension and configure build scripts for audio codecs:
  - [x] Enable decoders: APE, WavPack, TTA, MPC, ALAC, WMA, AIFF, DSD.
- [x] Implement real-time DSD to 24-bit/88.2kHz PCM software decimation.
- [x] Implement format capability check and friendly unsupported error messaging.
- [x] Add format badge rendering (`FLAC`, `HI-RES`, `DSD`, `LOSSLESS`) across UI.

---

## 10. Milestone 10: Acoustic Song Recognition (M10)
- [x] Implement `RECORD_AUDIO` permission request with transparent rationale screen.
- [x] Build 10-second microphone audio capture pipeline into memory buffer.
- [x] Integrate AudD / ACRCloud API client with user-configured API token.
- [x] Design animated circular waveform listening UI in accent color (no blue gradients).
- [x] Build Match Result card with quick library search and web lookup actions.

---

## 11. Milestone 11: Hardening, QA & Release Preparation (M11)
- [x] Run full Unit Test suite (Smart Previous, LRC parser, Queue, Sleep Timer, Scanner diff).
- [x] Execute manual QA checklist across target devices (Pixel, Samsung One UI, Xiaomi MIUI).
- [x] Audit TalkBack accessibility labels and contrast ratios across all themes.
- [x] Optimize scroll performance and memory footprint with Android Studio Profiler.
- [x] Configure ProGuard / R8 optimization rules for Media3, JAudioTagger, Retrofit, and Room.
- [x] Generate Baseline Profiles to optimize cold start time to under 1.5 seconds.
