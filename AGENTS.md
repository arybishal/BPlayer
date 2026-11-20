# AGENTS.md — Engineering Rules and Guardrails for Pulse

This document outlines mandatory directives, coding standards, architectural guardrails, and quality gates for any AI coding agent or engineer contributing to the **Pulse** codebase.

---

## 1. Core Principles

1. **Top Priority 1 — LASTWAVE VISUAL FIDELITY RULE & Permanent Design Lock**:
   - **LastWave-Native is the authoritative visual reference for BPlayer.** Future developers and coding agents must not independently redesign, simplify, flatten, recolor, replace or remove the established visual design.
   - **The existing BPlayer feature set must be preserved while the reference design is reproduced.**
   - Any proposed visual change must:
     1. Be compared against LastWave side-by-side (`qa/SIDE_BY_SIDE_DESIGN_COMPARISON.md`).
     2. Be documented.
     3. Be checked for impact on existing features.
     4. Receive explicit user approval before implementation if it changes the established design direction.
   - Do not silently replace translucent surfaces with opaque surfaces.
   - Do not introduce a new design system.
   - Do not introduce new colors, typography, component styles or animations that conflict with the reference.
   - All UI work is strictly governed by [DESIGN_RULES.md](file:///D:/Webapp/BPlayer/DESIGN_RULES.md).
2. **Top Priority 2 — Frictionless UX**: Every interaction must feel snappy, intuitive, and responsive. One-hand friendly design (interactive zones in the bottom 40%).
3. **Playback Reliability is Non-Negotiable**: Background audio playback must survive screen locks, aggressive OEM process management, audio focus changes, and configuration changes. The UI must never directly control ExoPlayer; it talks exclusively to `MediaController`.

---

## 2. Hard Architectural Rules

### Architecture & Layers
- **Unidirectional Data Flow (UDF)**: ViewModels expose immutable `StateFlow<UiState>` and process user `UiEvent`s via dedicated functions.
- **Strict Layer Separation**:
  - `ui/`: Jetpack Compose screens, ViewModels, navigation, design tokens. No direct database or player calls.
  - `domain/`: Use cases, domain models, business validation. Pure Kotlin, no Android framework dependencies where possible.
  - `data/`: Repositories, Room DAOs, DataStore preferences, MediaStore indexer, network sources, Media3 player wrapper.
- **Dependency Injection**: Use **Hilt**. All singletons and scoped dependencies must be bound in standard Hilt modules (`AppModule`, `DatabaseModule`, `MediaModule`, `NetworkModule`).
- **Single Source of Truth**: Room is the single source of truth for the music library, playlists, favorites, play statistics, and queue state. MediaStore is an external source to be diffed against Room.

### Playback Architecture
- **Media3 Service Boundary**: ExoPlayer is hosted exclusively within `PlaybackService` (`androidx.media3.session.MediaSessionService`).
- **No Direct Audio Control in UI**: Activities and Composables must interact with playback solely through an injected `MediaControllerManager` or a shared `PlaybackConnection` that exposes reactive playback state flows.
- **Audio Focus & Becoming Noisy**: The audio engine must handle audio focus ducking/pausing and instantly pause playback when headphones are disconnected (`AudioManager.ACTION_AUDIO_BECOMING_NOISY`).

---

## 3. Visual & UI Guidelines

- **STRICT RULE: NO BLUE GRADIENTS**:
  - Never use blue, purple, or cyan gradient backdrops that look like generic tech templates.
  - The color foundation must use neutral deep charcoals, pitch blacks (AMOLED), or warm muted tones.
  - Accent colors must use curated warm tones (Amber, Coral, Warm Gold) or Emerald. If album art palette extraction is enabled, extract vibrant warm/neutral dominant tones with WCAG AA contrast enforcement.
- **Icon-First Transport Controls**:
  - Play, pause, skip, shuffle, repeat, favorite, and queue actions must be pure vector icons (Material Symbols Rounded), never text buttons.
  - Text labels are only permitted in dropdowns, bottom sheet menus alongside icons, and settings rows.
- **Accessibility**:
  - Every `IconButton`, clickable icon, and image must include an explicit `contentDescription` localized or defined via string resources. Never leave `contentDescription = null` on interactive elements.
- **Glass / Frosted Translucency**:
  - Implement the "Liquid Glass" effect using `Modifier.blur` / `RenderEffect` on API 31+ with a 1dp subtle border (`Color.White.copy(alpha = 0.15f)`).
  - Provide an automatic graceful fallback for API 26–30 (high-alpha tonal surface without blur) to prevent rendering artifacts or dropped frames.

---

## 4. Coding Standards

### Kotlin & Coroutines
- Use Kotlin idiomatic patterns: sealed interfaces for UI states and events, data classes for immutable models, and extension functions for domain mappings.
- **Never use `GlobalScope`**. All asynchronous work must be scoped to `viewModelScope`, `serviceScope`, or injected `CoroutineScope` with proper `CoroutineDispatcher` (e.g., `Dispatchers.IO` for file/DB I/O).
- Keep composables lightweight: hoist state, pass lambdas for events, and use `@Stable` / `@Immutable` annotations on state classes to maximize Compose compiler skipping.

### Storage & Tag Writing
- Never attempt direct file writes to user audio files using `java.io.File` on Android 10+ (API 29+).
- All metadata tag editing (title, artist, album, embedded artwork) must utilize scoped storage APIs:
  - On API 30+, request write access via `MediaStore.createWriteRequest(contentResolver, uris)`.
  - Use `ContentResolver.openFileDescriptor(uri, "rw")` with JAudioTagger to edit metadata in-place.
- Song deletion must use `MediaStore.createDeleteRequest(contentResolver, uris)`.

### Error Logging & Diagnostics
- Never swallow exceptions silently with empty `catch` blocks.
- Route all unexpected errors through `AppLogger.e(tag, message, throwable)`.
- `AppLogger` must write fatal and non-fatal exceptions to the local Room `error_logs` table so users can review, copy, and share diagnostic logs from **Settings > Error Logs**.
- Register an `UncaughtExceptionHandler` in the `Application` class to intercept uncaught thread panics and persist them to SQLite before the process terminates.

---

## 5. Do's and Don'ts

### DO:
- ✅ **DO** verify all touch targets meet or exceed 48x48 dp (hero play/pause button is 72–80 dp).
- ✅ **DO** ensure the Smart Previous button behaves reliably (restart song on single tap; previous song on double-tap within ~350ms).
- ✅ **DO** support multi-select for track deletion and playlist additions.
- ✅ **DO** test all database queries with indexes on frequently queried columns (`artist_id`, `album_id`, `title`, `duration`, `date_added`).
- ✅ **DO** debounce search queries by 300ms using Coroutine Flows (`debounce(300)`).
- ✅ **DO** support the system "Reduce Motion" setting by disabling or dampening non-essential transitions.

### DON'T:
- ❌ **DO NOT** hardcode API keys, secrets, or bearer tokens anywhere in the codebase.
- ❌ **DO NOT** block the Main / UI thread under any circumstances. Library scanning and tag parsing must occur on background threads.
- ❌ **DO NOT** introduce mock or dummy features that appear interactive but do nothing. If an external API or codec requires user setup or native build steps, display a clean configuration UI and document it.
- ❌ **DO NOT** introduce random third-party dependencies when standard AndroidX or Kotlin libraries suffice.
- ❌ **DO NOT** use blue gradients or generic purple gradients.

---

## 6. Definition of Done (DoD)

A milestone or feature is considered **Done** only when:
1. **Compilation**: Code compiles cleanly with zero unresolved warnings or deprecations introduced.
2. **Architecture**: Adheres strictly to MVVM + UDF + Hilt + Media3. No UI leakage into the playback engine.
3. **Design Preservation**: Strictly follows [DESIGN_RULES.md](file:///D:/Webapp/BPlayer/DESIGN_RULES.md). Translucency, 1dp specular borders, and glass components are preserved with zero opaque surface regressions in both light and dark themes.
4. **Error Handling**: All edge cases (corrupted files, missing tags, network timeouts, denied permissions) are handled gracefully with friendly UI states and recorded in `AppLogger`.
5. **Performance**: List scrolling maintains 60/120 FPS with 10,000+ tracks; app cold start completes in < 1.5 seconds.
6. **Testing**: Unit tests are added for core business logic (Smart Previous tap detector, LRC synchronizer, queue manager, search filter).
7. **Documentation**: Milestone checkboxes in `docs/TASKS.md` are updated, and significant architectural deviations are logged in `docs/DECISIONS.md`.
