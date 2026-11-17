# Product Requirements Document (PRD) — Pulse Music Player

## 1. Executive Summary & Vision

**Pulse** is a flagship-tier, offline-first audio player designed for modern Android devices. While streaming services dominate modern music consumption, millions of users curate expansive local libraries consisting of high-resolution lossless audio (FLAC, ALAC, DSD), personal mixtapes, live bootlegs, and ripped CDs. 

Most existing offline Android music players suffer from dated early-Material aesthetics, intrusive ad networks, sluggish library scanning, or fragile background playback services killed by aggressive OEM battery optimization.

**Pulse's mission:**
Deliver an audio player that rivals the visual elegance of premier modern applications, provides instantaneous library navigation, guarantees uninterrupted background playback across all Android OEM skins, and treats album art and typography as visual centerpieces.

---

## 2. Target Personas

### Persona A: Marcus "The Audiophile & Archiver"
- **Profile**: 29, software engineer, owns 80GB+ of FLAC, ALAC, and DSD audio files organized on a MicroSD card or internal storage.
- **Pain Points**: Player apps that fail to decode non-standard formats (APE, Opus, DSD), flatten directory structures, or corrupt metadata tags when editing.
- **Goals**: Accurate bit-depth and sample-rate indicators, gapless playback, responsive folder navigation, 10-band equalizer, and robust ID3 tag editing.

### Persona B: Elena "The Daily Commuter & Fitness Runner"
- **Profile**: 22, student and daily runner, listens while cycling, running, or studying offline in low-connectivity areas.
- **Pain Points**: Tiny touch targets on transport controls, music abruptly stopping when switching apps, or having to stare at the screen to restart a song.
- **Goals**: Intuitive one-hand controls, reliable Smart Previous button (single tap to replay, double tap to skip back), sleep timer for bedtime, and rapid song search.

### Persona C: Kenji "The Visual & Lyric Enthusiast"
- **Profile**: 25, digital designer, values typography, cover art aesthetics, and synchronized lyrics.
- **Pain Points**: Ugly, cluttered interfaces with generic blue gradients, missing album art, and static text lyrics that don't follow the song.
- **Goals**: Full-screen synchronized lyrics (LRC) that auto-scroll with tap-to-seek, easy album art retrieval, customizable accent palettes, and a frosted glass visual aesthetic.

---

## 3. User Stories

### Library & Organization
- *As a music collector*, I want the app to automatically scan my device's audio files in seconds without freezing the UI, so that my library is instantly accessible.
- *As a user with organized folders*, I want to browse my physical folder structure and exclude system audio (ringtones, voice notes, podcasts), so my music queue stays clean.
- *As a listener*, I want to filter and sort tracks by title, artist, album, date added, duration, and play count with an alphabet scrubber for 5,000+ songs.

### Playback & Transport
- *As a commuter*, I want a "Smart Previous" button that restarts the song if it's already playing, or jumps to the previous track if I double-tap, so I don't have to fiddle with controls while walking.
- *As a night listener*, I want a sleep timer with gradual audio fade-out so the music doesn't wake me when it cuts off abruptly.
- *As an active listener*, I want to rearrange my queue on the fly via drag-and-drop, add songs to "Play Next", and have my exact playback queue restored when I reopen the app.

### Now Playing & Visual Experience
- *As an aesthetic-driven user*, I want the player background to smoothly reflect the colors of the current album art without any harsh blue gradient artifacts.
- *As a karaoke lover*, I want to swipe horizontally to view live synchronized lyrics that highlight the active line and let me tap any line to seek directly to that moment.
- *As an art lover*, I want to swipe to view high-resolution album artwork, pinch-to-zoom into details, or find missing art online with one tap.

### Metadata & Power Features
- *As a curator*, I want to edit misspelled song titles, artist names, and genres directly from the app using Android's native scoped storage prompt.
- *As a sound tweaker*, I want a custom equalizer with bass boost and virtualizer to tune my headphones' frequency response.
- *As a curious listener*, I want to tap a button to identify an ambient song playing in a café and immediately see if it's in my library.

---

## 4. Prioritized Feature Matrix

### P0 (Must Ship — Core Product Viability)

| Module | Feature | Description & Acceptance Criteria |
|---|---|---|
| **Library** | MediaStore Indexer | Reactive Room-backed scanner for tracks, albums, artists, genres, and folders with incremental diffing. |
| **Library** | Multi-View Browsing | Dedicated tabs: Tracks, Albums, Artists, Genres, Playlists, Folders. |
| **Library** | Sorting Engine | Ascending/descending sort by Title, Artist, Album, Date Added, Duration, Play Count. |
| **Library** | Real-Time Search | Debounced instant search highlighting title/artist matches across all library entities. |
| **Playback** | Media3 Service | Foreground `MediaSessionService` with notification controls, lock-screen artwork, and headset support. |
| **Playback** | Smart Previous Button | Single tap: restart track from 0:00. Double tap (< 350ms): skip to previous song. User configurable setting. |
| **Playback** | Queue Management | Persistent queue across restarts; drag-to-reorder; swipe-to-delete; "Add to Queue" and "Play Next". |
| **Playback** | Audio Focus Handler | Automatic ducking during notifications, pause on phone calls, instant pause on headphone disconnect (`BECOMING_NOISY`). |
| **Now Playing** | 3-Page Pager | Horizontal swipe: (1) Main Player, (2) Synced/Static Lyrics, (3) High-Res Artwork Viewer. |
| **Now Playing** | Dynamic Palette | Ambient surface coloring dynamically extracted from album art with strict WCAG AA contrast check. |
| **Actions** | Scoped Tag Editor | In-place ID3/Vorbis tag editing for Title, Artist, Album, Year, Genre using `MediaStore.createWriteRequest`. |
| **Actions** | Scoped Delete | Single and multi-select track deletion using `MediaStore.createDeleteRequest`. |
| **Audio FX** | System Equalizer | 5-to-10 band equalizer sliders, system presets, custom preset saving, Bass Boost, and Virtualizer. |
| **Utility** | Sleep Timer | 15/30/45/60 min presets, custom picker, "End of track", 10s audio fade-out, Now Playing countdown chip. |
| **System** | Battery Optimization Flow | Status indicator and deep link to `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` with OEM guide sheets. |
| **Diagnostics**| Crash & Error Logger | Room-backed `error_logs` table, global uncaught exception interceptor, UI viewer with Copy/Share/Clear. |

### P1 (Should Ship — High Value Enhancements)

| Module | Feature | Description & Acceptance Criteria |
|---|---|---|
| **Library** | Fast-Scroll Scrubber | Alphabet index bubble for rapid navigation through large track/artist lists. |
| **Library** | Grid/List Toggle | Responsive layout toggle for Albums and Artists views. |
| **Playback** | Gapless & Crossfade | True gapless transitions for FLAC/MP3 and optional 1–10s crossfade between queue items. |
| **Playback** | Playback Speed | 0.5x to 2.0x pitch-preserved speed adjustment slider. |
| **Playback** | Session Resumption | Automatically restore last played track, queue, and playback position on cold launch. |
| **Lyrics** | Online Lyrics Lookup | Auto-fetch from LRCLIB API with local Room caching; Gemini API fallback for unindexed songs. |
| **Artwork** | Online Cover Art Search | Query MusicBrainz, Cover Art Archive, and Deezer; display candidate grid for user selection. |
| **Song Actions**| File & Text Share | Share raw audio file via Android Sharesheet; share text snippet ("Title - Artist"). |
| **Song Actions**| Detailed Audio Specs | Dialog showing container format, bitrate, sample rate, bit depth, channel count, and file path. |
| **Song Actions**| Set as Ringtone | Write system ringtone setting using `RingtoneManager` (requires `WRITE_SETTINGS`). |
| **Utility** | Song Recognition | 10–15 second microphone sample sent to external fingerprinting API (AudD / ACRCloud). |

### P2 (Nice to Have — Future Roadmap)

| Module | Feature | Description & Acceptance Criteria |
|---|---|---|
| **Audio** | ReplayGain Normalization | Track and album gain normalization parsing ReplayGain 2.0 tags. |
| **Audio** | Native DSD / APE Decoding | Bundled custom FFmpeg native decoders for rare DSD (DSF/DFF) and Monkey's Audio (APE). |
| **UI** | Foldable / Tablet Layout | Adaptive two-pane layout with permanent queue on wide screens (> 600dp width). |
| **Integrations**| Android Auto Integration | Custom MediaLibraryService browsing tree for vehicle head units. |

---

## 5. Non-Functional Requirements

1. **Performance**:
   - Cold app start to interactive library in under 1.5 seconds on mid-tier hardware (e.g., Snapdragon 695 / Tensor G2).
   - Smooth 60/120 FPS scrolling over 10,000+ tracks using Compose `LazyColumn` with optimized keying and memory-efficient Coil image thumbnails.
2. **Battery & Resource Efficiency**:
   - Background audio playback CPU overhead < 3% on average.
   - Zero wake-locks held when playback is paused or stopped.
3. **Storage & Scoped Permissions**:
   - Zero crashes when editing files stored on external SD cards or internal shared storage under Android 11+ scoped storage rules.
   - Graceful permission rationale dialogs explaining why media access is required before triggering system permission popups.
4. **Security & Privacy**:
   - User API keys (Gemini, AudD, ACRCloud) stored in EncryptedSharedPreferences or encrypted Room cache.
   - Zero third-party telemetry, ads, or tracking SDKs.
