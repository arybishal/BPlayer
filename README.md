# Pulse Music Player

A modern, high-fidelity offline audio player for Android engineered with Jetpack Compose, Material 3, and AndroidX Media3 (ExoPlayer). Built with a relentless focus on clean visual aesthetics, fluid physics-based micro-interactions, robust background playback, and complete audio format flexibility.

---

## Highlights

- **Hero Now Playing Experience**: 3-page horizontal swipe interface:
  - **Player Page**: Dynamic tonal theming extracted from album art, oversized cover view with subtle scale physics, custom rounded seekbar with expanding touch thumb, and icon-first minimalist transport controls.
  - **Synchronized Lyrics Page**: Local ID3 tag (USLT/SYLT) and sidecar `.lrc` file support, smooth auto-scrolling with tap-to-seek, and automatic online lookup via LRCLIB with optional AI fallback.
  - **Album Art Page**: High-resolution zoomable artwork viewer with pinch-to-zoom and integrated metadata image finder (MusicBrainz, Cover Art Archive, Deezer, iTunes API).
- **Audio Architecture & Engine**:
  - Decoupled `MediaSessionService` foreground architecture running on AndroidX Media3.
  - Comprehensive format support: Native lossy/lossless (MP3, AAC, FLAC, OGG, Opus, WAV) plus extended decoding via Media3 FFmpeg decoder extension (APE, WavPack, ALAC, AIFF).
  - 10-band system equalizer, bass boost, audio virtualizer, and loudness enhancer attached to the active audio session.
  - Gapless playback, crossfade transitions, and audio-focus handling (graceful ducking and becoming-noisy pause).
- **Smart Transport & Queue Controls**:
  - Smart Previous button: Single-tap restarts the current track; double-tap within 350ms skips to previous track.
  - Persistent queue with drag-to-reorder, swipe-to-dismiss, "Play Next", and "Add to Queue".
- **Modern Jetpack Compose UI**:
  - Custom Liquid Glass / frosted translucency mode (`RenderEffect.createBlurEffect` on Android 12+ / API 31+, with clean tonal fallbacks for API 26–30).
  - Strictly no generic blue gradients. Rich warm tonal foundations (Charcoal, Deep Plum, Pitch Black AMOLED) paired with customizable warm accent colors.
  - System-wide font scale slider (85%–140%) adjusting typography dynamically across all components.
- **Library & Scoped Storage Tag Editor**:
  - High-performance MediaStore indexer backed by Room with reactive Flow streams.
  - Scoped storage compliant metadata editor (`MediaStore.createWriteRequest` on API 30+) writing directly to ID3/Vorbis tags via JAudioTagger.
  - Multi-select track management and scoped deletion (`MediaStore.createDeleteRequest`).
- **Sleep Timer & Utility Suite**:
  - Sleep timer with 15/30/45/60 min presets, custom duration, "end of current track", and gradual 10-second fade-out.
  - Acoustic song fingerprinting ("What's this song?") via external API (AudD / ACRCloud).
  - Built-in SQLite-backed crash and error logger with exportable `.txt` diagnostics and log filtering.

---

## Tech Stack & Architecture

| Layer / Concern | Technology |
|---|---|
| **Language** | Kotlin 1.9+ / 2.0+ (100% Coroutines & Flow) |
| **UI Framework** | Jetpack Compose + Material 3 (custom tokens and surfaces) |
| **Audio Engine** | AndroidX Media3 1.3+ (`ExoPlayer`, `MediaSessionService`, `MediaController`) |
| **Dependency Injection** | Hilt (Dagger) |
| **Database & Cache** | Room (SQLite) with KSP |
| **Key-Value Storage** | Jetpack DataStore (Preferences & Proto) |
| **Image Loading** | Coil Compose |
| **Palette Extraction** | AndroidX Palette API |
| **Networking** | Retrofit 2 + OkHttp 4 + `kotlinx.serialization` |
| **Audio Metadata Editing** | JAudioTagger (Android-compatible fork) |
| **Min SDK / Target SDK** | Min SDK 26 (Android 8.0) / Target SDK 34/35 (Android 14/15) |

---

## Directory & Documentation Structure

All architectural specifications, designs, and task trackers are cataloged under [`docs/`](file:///D:/Webapp/BPlayer/docs):

- [`AGENTS.md`](file:///D:/Webapp/BPlayer/AGENTS.md) — Coding conventions, architectural rules, agent guardrails.
- [`docs/PRD.md`](file:///D:/Webapp/BPlayer/docs/PRD.md) — Product requirements, personas, feature tiers (P0/P1/P2).
- [`docs/DESIGN.md`](file:///D:/Webapp/BPlayer/docs/DESIGN.md) — Color tokens, typography, component specs, glass physics, layout rules.
- [`docs/ARCHITECTURE.md`](file:///D:/Webapp/BPlayer/docs/ARCHITECTURE.md) — Clean architecture, Room schema, MediaSession data flow, Mermaid diagrams.
- [`docs/FEATURES.md`](file:///D:/Webapp/BPlayer/docs/FEATURES.md) — Granular functional specs, edge cases, acceptance criteria.
- [`docs/UX_FLOWS.md`](file:///D:/Webapp/BPlayer/docs/UX_FLOWS.md) — Navigation graphs, gestures, state transitions, micro-interactions.
- [`docs/AUDIO_FORMATS.md`](file:///D:/Webapp/BPlayer/docs/AUDIO_FORMATS.md) — Codec support matrix, FFmpeg build plan, format capability validation.
- [`docs/INTEGRATIONS.md`](file:///D:/Webapp/BPlayer/docs/INTEGRATIONS.md) — LRCLIB, Gemini API, MusicBrainz/CAA, ACRCloud/AudD audio fingerprinting.
- [`docs/PERMISSIONS_AND_BACKGROUND.md`](file:///D:/Webapp/BPlayer/docs/PERMISSIONS_AND_BACKGROUND.md) — Scoped storage, foreground service, battery optimization guidance.
- [`docs/TESTING.md`](file:///D:/Webapp/BPlayer/docs/TESTING.md) — Test matrix, unit test plan, UI tests, manual QA checklist.
- [`docs/TASKS.md`](file:///D:/Webapp/BPlayer/docs/TASKS.md) — Ordered milestones from M0 to M11 with progress checkboxes.
- [`docs/DECISIONS.md`](file:///D:/Webapp/BPlayer/docs/DECISIONS.md) — Architectural Decision Records (ADRs).

---

## Build & Setup Instructions

### Prerequisites
1. **Android Studio** Hedgehog (2023.1.1) or newer / Ladybug.
2. **JDK 17** (configured in Android Studio under Gradle settings).
3. **Android SDK 34+** with Build Tools 34.0.0+.
4. **Android NDK** (version 26.x or newer if compiling the optional native Media3 FFmpeg decoder extension).

### Building the Project
Clone the repository and build via Gradle wrapper:

```bash
# Clean and assemble debug APK
./gradlew clean assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install on connected device or emulator
./gradlew installDebug
```

### Running on Device
1. Connect an Android device (Android 8.0 / API 26 or higher) with USB Debugging enabled.
2. Grant Media permissions on first launch when prompted (`READ_MEDIA_AUDIO` on Android 13+, `READ_EXTERNAL_STORAGE` on Android 12 and below).
3. Optionally load test audio tracks (MP3, FLAC, WAV, M4A) into `Music/` or `Download/` directories.

---

## License

Internal proprietary project. All rights reserved.
