# Project Milestones & Task Checklist — Pulse

This tracker serves as the authoritative milestone checklist for the development of **Pulse**. Each task must be checked off (`[x]`) upon completion and verification.

---

## Milestone 0: Foundations & Architecture Skeleton (M0)
- [ ] Initialize Android project structure (Gradle Version Catalog `libs.versions.toml`, Min SDK 26, Target SDK 34/35).
- [ ] Configure Hilt dependency injection, Coroutines, and AndroidX Lifecycle.
- [ ] Implement foundational Design System in `ui/theme`:
  - [ ] Color tokens (Light, Dark, AMOLED Black) with strict **No Blue Gradients** rule.
  - [ ] Typography scale (Plus Jakarta Sans) with dynamic font-scale multiplier ($S$).
  - [ ] Elevation, corner radii (8/12/20/28/full), and spacing grid (4dp).
  - [ ] Liquid Glass modifier with `RenderEffect` (API 31+) and graceful fallback (API 26–30).
- [ ] Configure Material Symbols Rounded iconography.
- [ ] Build navigation scaffold (Home, Library, Search, Settings) with bottom bar.
- [ ] Implement `AppLogger` utility and Room-backed `error_logs` table with an uncaught exception handler.
- [ ] Build basic **Settings > Error Logs** viewer (list, severity badge, copy/share/clear).

---

## Milestone 1: Library Indexing & Browsing (M1)
- [ ] Build Just-In-Time media permission request flow (`READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`).
- [ ] Implement Room database schema (`TRACK`, `ALBUM`, `ARTIST`, `PLAYLIST`, `QUEUE_ITEM`).
- [ ] Build high-performance `MediaStore` audio scanner with incremental diffing and pull-to-refresh.
- [ ] Build Library screen with 6 tabs:
  - [ ] Tracks list (thumbnail, title, artist, duration, format badge).
  - [ ] Albums list/grid toggle.
  - [ ] Artists list/grid toggle.
  - [ ] Genres list.
  - [ ] Folders physical file tree browser.
  - [ ] Playlists tab.
- [ ] Implement sorting engine (Title, Artist, Album, Date Added, Duration, Play Count).
- [ ] Implement fast-scroll alphabet index scrubber with popup bubble.

---

## Milestone 2: Playback Engine Core & Transport (M2)
- [ ] Implement `PlaybackService` extending AndroidX Media3 `MediaSessionService`.
- [ ] Configure `ExoPlayer` instance with audio attributes (usage: `USAGE_MEDIA`, content type: `CONTENT_TYPE_MUSIC`).
- [ ] Build rich media foreground notification with album art and transport controls.
- [ ] Implement Audio Focus manager (ducking on alerts, pause on incoming calls).
- [ ] Implement `BECOMING_NOISY` receiver (auto-pause when headphones unplugged).
- [ ] Implement `MediaController` connection bridge for UI state observation.
- [ ] Build persistent Mini Player:
  - [ ] Track thumbnail, title, artist.
  - [ ] Play/Pause morph icon button, Next button.
  - [ ] Swipe gestures (up to expand, left/right for next/prev).
- [ ] Build Hero Seek Bar with expanding thumb and live elapsed/remaining timestamps.
- [ ] Implement **Smart Previous Button**:
  - [ ] Single tap: instant seek to 0:00.
  - [ ] Double tap (< 350ms): jump to previous track.
  - [ ] Previous button behavior setting (Smart, Always Previous, Always Restart).
- [ ] Implement Shuffle (non-repeating index mapping) and Repeat modes (Off, All, One).
- [ ] Build Queue Manager with Room persistence, drag-to-reorder, and swipe-to-delete.

---

## Milestone 3: Hero Now Playing Screen & Polish (M3)
- [ ] Implement full-screen modal Now Playing container with collapse gesture (swipe down / back).
- [ ] Build `HorizontalPager` with 3 dedicated pages and dot indicator:
  - [ ] Page 1: Hero Player View (giant cover, typography, seek bar, transport pod).
  - [ ] Page 2: Synchronized Lyrics View.
  - [ ] Page 3: High-Res Album Art Viewer.
- [ ] Implement AndroidX Palette dynamic color extraction with WCAG AA contrast enforcement.
- [ ] Implement shared-element bounds transition between Mini Player artwork and Now Playing hero artwork.
- [ ] Add micro-interactions:
  - [ ] Animated play/pause icon morph.
  - [ ] Heart favorite button particle burst.
  - [ ] Subtle album art breathing scale between playing and paused states.

---

## Milestone 4: Search, Playlists & Song Actions (M4)
- [ ] Implement instant search screen with 300ms debouncing and highlighted match text.
- [ ] Build Playlist management: create, rename, delete, add/remove tracks, reorder tracks.
- [ ] Build Favorites collection with one-tap heart toggle.
- [ ] Implement contextual Song Actions bottom sheet:
  - [ ] Play Next / Add to Queue.
  - [ ] Add to Playlist.
  - [ ] Go to Artist / Go to Album deep navigation.
  - [ ] Technical Song Details dialog (bitrate, sample rate, bit depth, container, path).
  - [ ] Share song (audio file via `FileProvider` or text title-artist).
  - [ ] Scoped storage song deletion (`MediaStore.createDeleteRequest`).

---

## Milestone 5: Synchronized & Online Lyrics (M5)
- [ ] Implement ID3 embedded lyrics reader (`USLT` / `SYLT` frames via JAudioTagger).
- [ ] Implement local sidecar `.lrc` file reader and parser with high-precision timestamp sorting.
- [ ] Build synchronized lyrics UI:
  - [ ] Active line glow and vertical center auto-scrolling.
  - [ ] Tap-to-seek to jump audio position to tapped line.
- [ ] Implement LRCLIB API client for fetching synchronized and plain text lyrics online.
- [ ] Implement Room `lyrics_cache` table to persist lyrics offline.
- [ ] Integrate optional Gemini AI fallback for missing lyrics with clear `⚠️ AI-Generated (Unverified)` badge.
- [ ] Build in-app lyrics editor allowing users to fix timestamps or write text.

---

## 6. Milestone 6: Tag Editor & Album Art Discovery (M6)
- [ ] Implement scoped storage write flow using `MediaStore.createWriteRequest` (Android 11+).
- [ ] Integrate JAudioTagger for in-place tag editing (Title, Artist, Album, Year, Genre, Track Number).
- [ ] Build Tag Editor bottom sheet with field validation.
- [ ] Implement online album art search pipeline (MusicBrainz + Cover Art Archive, iTunes API, Deezer API).
- [ ] Build 3-column candidate artwork picker dialog.
- [ ] Add "Pick artwork from device gallery" option using Android Photo Picker.
- [ ] Embed artwork into audio file tags or save to app cache and update Room.

---

## 7. Milestone 7: Equalizer, Audio Effects & Sleep Timer (M7)
- [ ] Implement system-backed Equalizer attached to ExoPlayer audio session ID:
  - [ ] 5–10 band sliders with decibel scale.
  - [ ] Presets dropdown (Rock, Pop, Jazz, Flat, etc.) and custom preset saver.
- [ ] Implement Bass Boost, Virtualizer, and Loudness Enhancer with graceful unsupported checks.
- [ ] Build Sleep Timer:
  - [ ] Presets: 15, 30, 45, 60 minutes, Custom Minute Picker, and "End of current track".
  - [ ] Countdown chip on Now Playing screen.
  - [ ] 10-second smooth volume fade-out prior to pause.

---

## 8. Milestone 8: Settings, Theming & Battery Optimization (M8)
- [ ] Build Appearance settings:
  - [ ] Theme switcher: Light, Dark, System, AMOLED Black.
  - [ ] Font size slider (85% to 140%) scaling typography dynamically.
  - [ ] Background style: Solid, Album-art blurred, Custom image, Dynamic color.
  - [ ] Liquid Glass effect toggle.
  - [ ] Curated accent color picker (Amber, Coral, Emerald).
- [ ] Build Playback settings:
  - [ ] Smart Previous behavior toggle.
  - [ ] Gapless playback toggle and crossfade slider (0–10s).
- [ ] Build Battery & Background Playback settings:
  - [ ] Battery exemption status indicator.
  - [ ] "Allow Unrestricted Battery Use" action triggering system request.
  - [ ] OEM background setup guides (Xiaomi, Samsung, OnePlus, Oppo, Vivo, Huawei).
- [ ] Build Online settings:
  - [ ] Encrypted Gemini API key storage and "Test Key" ping.
  - [ ] Lyrics and artwork source prioritization order.

---

## 9. Milestone 9: Extended Audio Formats & FFmpeg (M9)
- [ ] Evaluate Media3 FFmpeg decoder extension and configure build scripts for audio codecs:
  - [ ] Enable decoders: APE, WavPack, TTA, MPC, ALAC, WMA, AIFF, DSD.
- [ ] Implement real-time DSD to 24-bit/88.2kHz PCM software decimation.
- [ ] Implement format capability check and friendly unsupported error messaging.
- [ ] Add format badge rendering (`FLAC`, `HI-RES`, `DSD`, `LOSSLESS`) across UI.

---

## 10. Milestone 10: Acoustic Song Recognition (M10)
- [ ] Implement `RECORD_AUDIO` permission request with transparent rationale screen.
- [ ] Build 10-second microphone audio capture pipeline into memory buffer.
- [ ] Integrate AudD / ACRCloud API client with user-configured API token.
- [ ] Design animated circular waveform listening UI in accent color (no blue gradients).
- [ ] Build Match Result card with quick library search and web lookup actions.

---

## 11. Milestone 11: Hardening, QA & Release Preparation (M11)
- [ ] Run full Unit Test suite (Smart Previous, LRC parser, Queue, Sleep Timer, Scanner diff).
- [ ] Execute manual QA checklist across target devices (Pixel, Samsung One UI, Xiaomi MIUI).
- [ ] Audit TalkBack accessibility labels and contrast ratios across all themes.
- [ ] Optimize scroll performance and memory footprint with Android Studio Profiler.
- [ ] Configure ProGuard / R8 optimization rules for Media3, JAudioTagger, Retrofit, and Room.
- [ ] Generate Baseline Profiles to optimize cold start time to under 1.5 seconds.
