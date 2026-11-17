# MASTER PROMPT: Build "Pulse" — A Modern, Full-Featured Android Music Player

> Paste this entire file to your coding agent (Claude Code, Cursor, etc.) at the root of an empty Android project repo.
> The app name "Pulse" is a placeholder. Rename it anywhere.

---

## 0. YOUR ROLE

You are a senior Android engineer and product designer. You are building a production-quality offline music player for Android. **The two top priorities are: (1) a beautiful, modern UI and (2) a great, effortless user experience.** Every technical decision must serve those two goals without sacrificing playback reliability.

---

## 1. HOW YOU MUST WORK (MANDATORY WORKFLOW)

Do **not** start writing app code immediately. Follow these phases in order:

### Phase A: Planning documents (write these FIRST, then stop for review)
Create the following files in the repo. Be specific and concrete in each. No filler.

| File | Purpose |
|---|---|
| `README.md` | Project overview, setup, build and run instructions |
| `AGENTS.md` | Rules for you (the agent): coding standards, commands, do/don't list, definition of done |
| `docs/PRD.md` | Product requirements, personas, user stories, feature list with priorities (P0/P1/P2) |
| `docs/DESIGN.md` | Full design system (see section 4 for required contents) |
| `docs/ARCHITECTURE.md` | Layers, modules, data flow, DI, DB schema, playback service design, diagrams in Mermaid |
| `docs/FEATURES.md` | Every feature spec with behavior, edge cases, and acceptance criteria |
| `docs/UX_FLOWS.md` | Screen map, navigation graph, gestures, empty/loading/error states, micro-interactions |
| `docs/AUDIO_FORMATS.md` | Format support matrix, decoder strategy, known limits (see section 6) |
| `docs/INTEGRATIONS.md` | Gemini API, lyrics sources, album art sources, song recognition (see section 7) |
| `docs/PERMISSIONS_AND_BACKGROUND.md` | Runtime permissions, foreground service, battery optimization handling |
| `docs/TESTING.md` | Test strategy, device matrix, manual QA checklist |
| `docs/TASKS.md` | Ordered milestone checklist with checkboxes; update as you work |
| `docs/DECISIONS.md` | Short ADR-style log of important choices and tradeoffs |

After creating these, **stop and summarize** what you decided and list any open questions. Wait for approval before Phase B.

### Phase B: Build in milestones
Work milestone by milestone from `docs/TASKS.md` (section 9). After each milestone: build must compile, app must run, tick the checklist, commit with a clear message, and report what changed.

### Phase C: Polish and hardening
Animations, accessibility, performance profiling, error log review, QA checklist.

**Rules for all phases:**
- If a requirement is impossible or unreliable as stated, say so in `docs/DECISIONS.md` and propose the best realistic alternative. Do not silently fake a feature.
- Never hardcode API keys. Never commit secrets.
- Prefer fewer, well-built features over many half-working ones. But all listed P0 features must work.

---

## 2. TECH STACK (use unless you document a better reason)

- **Language:** Kotlin (latest stable)
- **UI:** Jetpack Compose + Material 3 (heavily customized, see design system), Compose Animation APIs
- **Min SDK 26, Target/Compile SDK latest stable**
- **Playback:** AndroidX **Media3** (ExoPlayer + `MediaSessionService` + `MediaController`)
- **Architecture:** MVVM + Unidirectional Data Flow, Clean-ish layers (ui / domain / data), multi-module only if it stays simple
- **DI:** Hilt
- **Async:** Kotlin Coroutines + Flow
- **Database:** Room (library index cache, playlists, favorites, queue persistence, play counts, error logs)
- **Settings:** Jetpack DataStore
- **Image loading:** Coil
- **Color extraction:** AndroidX Palette (from album art)
- **Networking:** Retrofit/OkHttp + kotlinx.serialization
- **Audio effects:** `android.media.audiofx` (Equalizer, BassBoost, Virtualizer, LoudnessEnhancer)
- **Tag reading/writing:** JAudioTagger-compatible library or similar that works on Android (document the choice)
- **Icons:** Material Symbols Rounded (or Phosphor Icons). One icon family only, consistent stroke weight.
- **Fonts:** One modern geometric sans (e.g. Inter, Plus Jakarta Sans, or Manrope) bundled locally.

---

## 3. FEATURE REQUIREMENTS

Priority: **P0** = must ship, **P1** = should ship, **P2** = nice to have.

### 3.1 Library and browsing
- **P0** Track list of all audio on device (MediaStore scan + manual folder inclusion/exclusion, fast incremental rescan, pull-to-refresh).
- **P0** Sort and browse by: **Tracks, Albums, Artists, Genres, Playlists, Folders**. Sort options: title, artist, album, date added, duration, play count; ascending/descending.
- **P0** **Search**: instant, across tracks, albums, artists, playlists, genres. Recent searches. Debounced. Highlighted matches.
- **P1** Fast-scroll with alphabet index bubble.
- **P1** Grid/list toggle for albums and artists.

### 3.2 Playback core
- **P0** Play, pause, next, previous, seek bar (draggable, with time labels), skip +/-10s optional.
- **P0** **Smart Previous button:**
  - **Single tap** → restart the current song from 0:00.
  - **Double tap (within ~350 ms)** → go to the previous song.
  - Implement with a debounced tap detector. The single-tap action must fire only after the double-tap window expires, OR (preferred for snappiness) restart immediately on first tap and, if a second tap arrives within the window, go to previous. Document the chosen behavior in `FEATURES.md`. Add a setting "Previous button behavior" with options: *Smart (default)*, *Always previous*, *Always restart*.
- **P0** **Shuffle** (random) toggle.
- **P0** **Repeat modes:** off / repeat all / **repeat one**.
- **P0** Queue: view, reorder via drag, remove via swipe, **Add to queue**, **Play next**, clear queue. Queue persists across app restarts.
- **P0** Playback continues in background, from lock screen, notification, headset buttons, Bluetooth, Android Auto-ready MediaSession. Rich media notification with album art and icon-only controls.
- **P0** Audio focus handling, becoming-noisy handling (headphones unplugged → pause), resume position on restart.
- **P1** Gapless playback and optional crossfade (setting).
- **P1** Playback speed control.
- **P1** Resume last session on launch (restores queue, track, position, paused state).

### 3.3 Now Playing screen (the hero screen; spend the most design effort here)
A horizontally paged experience, with clear page indicator:
1. **Player page:** large album art, title, artist, seek bar, main controls (icons only), shuffle, repeat, favorite (heart), queue, more menu.
2. **Lyrics page (swipe):**
   - Read embedded lyrics (USLT/SYLT tags) and sidecar `.lrc` files first.
   - If synced (LRC), highlight the current line and auto-scroll smoothly; tap a line to seek.
   - If not found locally, show a clean empty state with a **"Search lyrics online"** button.
   - Online search order: (a) free lyrics DB (e.g. LRCLIB) first, (b) Gemini API fallback (see section 7). Cache results in Room. Allow user to save/edit the lyrics.
3. **Album art page (swipe):**
   - Full-size art viewer with pinch zoom.
   - If missing or wrong: **"Find album art"** → search online (see section 7), show a grid of candidates, user picks one, app saves it (embed in tag if possible, else store in app cache and map in DB). Also allow picking from gallery.

### 3.4 Song actions (long-press menu AND "more" menu; use a bottom sheet with icons + labels)
- **P0** Add to queue / Play next
- **P0** Add to playlist (create new inline)
- **P0** Add/remove favorite
- **P0** **Share song** (share the audio file via Android share sheet; optionally share text "Title – Artist")
- **P0** **Go to artist** / **Go to album**
- **P0** **Edit song info** (title, artist, album, album artist, genre, year, track number, lyrics, artwork). Write tags to file using the proper scoped-storage write flow (`MediaStore.createWriteRequest` on Android 11+) and update the DB and MediaStore.
- **P0** **Delete** (with confirmation; use `MediaStore.createDeleteRequest` on Android 11+; handle legacy storage on older versions). Support multi-select delete.
- **P1** Song details (format, bitrate, sample rate, bit depth, size, path).
- **P1** Set as ringtone.

### 3.5 Sleep timer
- **P0** Presets (15/30/45/60 min) + custom + "end of current track". Visible countdown chip on Now Playing. Fade-out in the last 10 seconds (setting). Cancel anytime.

### 3.6 Equalizer and audio effects
- **P0** System-backed Equalizer with band sliders, presets, custom preset save, on/off switch.
- **P0** Bass boost, virtualizer, loudness enhancer (where supported; hide gracefully if device doesn't support).
- Attach effects to the ExoPlayer audio session ID; persist settings.

### 3.7 Song identification ("What's this song?")
- **P1** A button (icon: microphone/waveform) that listens via mic for ~10–15 seconds and identifies the song playing in the environment.
- **Important technical reality:** Gemini is **not** an audio-fingerprinting service and should **not** be relied on for recognition. Use a real recognition API (e.g. ACRCloud, AudD, or Shazam-class provider). Let the user paste their own API credentials in Settings. Document the provider choice and free-tier limits in `INTEGRATIONS.md`.
- Show result card: title, artist, album, art, with actions: search in my library, copy, open in browser. Animated "listening" UI (pulsing rings, in the accent color, no blue gradient).
- Needs `RECORD_AUDIO` permission with a proper rationale screen.

### 3.8 Settings (clean, searchable sections, icons for each row)
- **Appearance**
  - Theme: Light / Dark / System / AMOLED black
  - **Font size** (slider with live preview; scales the whole app typography, range 85%–140%)
  - **Background style:** Solid / Album-art blurred / Custom image / Dynamic color from album art
  - **Glass effect toggle** (Liquid Glass / translucent surfaces, see 4.6)
  - Accent color picker (curated palette; **no blue gradients anywhere**)
  - Now Playing layout options
- **Playback:** previous-button behavior, crossfade, gapless, replay gain (P2), audio focus behavior, headset behavior
- **Library:** scan folders, excluded folders, minimum track length, rescan
- **Sleep timer defaults**
- **Equalizer** shortcut
- **Lyrics and Online features:** Gemini API key field (stored encrypted), lyrics source order, album-art source order, "use mobile data" toggle
- **Song recognition:** provider and key
- **Battery and background:**
  - Status row showing whether the app is exempt from battery optimization.
  - Button **"Allow unrestricted battery use"** → launches the correct system flow (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` where policy allows, otherwise deep-link to battery settings). Include OEM-specific guidance (Xiaomi, Oppo, Vivo, Samsung, Huawei, OnePlus) in a help sheet.
- **Logs (P0):** "Error logs" screen listing captured errors/crashes/non-fatal exceptions with timestamp, tag, message, stack trace; filter by level; **Copy**, **Share as .txt**, **Clear**. Wire a global `Logger` used across the app, plus an uncaught-exception handler that saves crashes to the DB before the process dies.
- **About:** version, licenses, build info.

---

## 4. UI/UX DESIGN SYSTEM (write this into `docs/DESIGN.md`)

The UI must look **current, premium, and calm**, comparable to the best modern players. It must **not** look old, cluttered, or like a default Material template.

### 4.1 Design principles
1. **Content first:** album art is the hero. UI chrome stays quiet.
2. **Icon-first controls:** transport and common actions use **icons, not text** (play, pause, next, previous, shuffle, repeat, heart, queue, more, search, sleep, equalizer, share, etc.). Text labels only where an icon alone would be ambiguous (e.g. in menus beside the icon, settings rows).
3. **One-hand friendly:** primary actions in the bottom 40% of the screen. Min touch target 48dp (main play button 72–80dp).
4. **Fewer taps:** common tasks within 1–2 taps. Long-press reveals secondary actions.
5. **Motion with purpose:** smooth, physics-based, never slow. Respect the "reduce animations" system setting.
6. **Consistency:** one icon family, one corner radius scale, one spacing scale.

### 4.2 Color
- Neutral foundation: near-black / soft off-white surfaces with subtle tonal elevation.
- **One accent color** (default: a warm tone such as coral/amber or fresh emerald; agent proposes 3 options in DESIGN.md, user picks). Accent adapts dynamically from the current album art when "Dynamic color" is on, with contrast checks (WCAG AA minimum).
- **STRICT RULE: No blue gradients.** Avoid gradients that default-look "techy blue/purple". Prefer flat tonal surfaces and, where depth is needed, subtle tonal overlays or blurred album art. If a gradient is ever used it must be a soft, same-hue tonal fade (e.g. scrim from art color to transparent), never blue.
- Define full semantic tokens: `background, surface, surfaceElevated, onSurface, onSurfaceMuted, accent, onAccent, outline, danger, success` for light, dark, AMOLED.

### 4.3 Typography
- One font family, 6-level scale (Display, Title, Subtitle, Body, Label, Caption), defined in `sp`, all multiplied by the user font-scale setting. Tight, confident hierarchy; song title bold, artist regular and muted.

### 4.4 Shape, spacing, elevation
- Corner radii: 8 / 12 / 20 / 28 / full. Album art on Now Playing: 28dp radius with soft shadow.
- 4dp base spacing grid.
- Prefer tonal elevation and blur over hard shadows.

### 4.5 Components to design and specify (with states: default, pressed, disabled, loading, error)
Track row, album card, artist circle, playlist card, mini player, full player, seek bar (custom, thick rounded, with draggable thumb that grows on touch), play/pause morph animation, icon buttons, chips, bottom sheet action menu, search bar, empty states (with friendly icon illustration), skeleton loaders, snackbars, dialogs, dropdowns, equalizer sliders, sleep-timer picker, lyrics line, settings row/section.

### 4.6 Liquid glass / transparent effect (toggle in Settings)
- When ON: mini player, bottom navigation, sheets, and top bars use translucent frosted surfaces with background blur, a thin 1dp highlight border (white at ~20% alpha), and subtle inner glow.
- Implement with `Modifier.blur`/`RenderEffect` on Android 12+ (API 31+) and a **graceful fallback** (higher-alpha translucent surface without blur) on older devices.
- When OFF: solid tonal surfaces. Both modes must pass contrast checks.
- Make sure the effect doesn't tank scroll performance; profile it.

### 4.7 Navigation and layout
- Bottom navigation (icons + small labels): **Home, Library, Search, Settings** (agent may propose refinements).
- Library has top tabs/segmented control: Tracks · Albums · Artists · Genres · Playlists · Folders.
- **Persistent mini player** above bottom nav: art, title, artist, play/pause, next; swipe up opens full player (shared-element transition of the album art), swipe left/right on mini player changes track, swipe down on full player dismisses.
- Home: Continue listening, Recently added, Most played, Favorites, Shuffle-all button.
- Now Playing pages: Player / Lyrics / Album art, swipe horizontally, with dot indicator. A compact top row: collapse chevron, "Playing from …", more-menu.

### 4.8 Micro-interactions
- Play/pause icon morphs. Heart has a small burst animation. Album art subtly scales on play/pause. Shared-element transitions between list → detail. Haptic feedback on key actions (setting to disable). Smooth list item add/remove animations. Dragging queue items lifts them with elevation.

### 4.9 Accessibility
- TalkBack content descriptions for every icon button. Dynamic type via the font-size setting. Contrast AA. Reduce-motion support. Large-screen/tablet and landscape layouts (adaptive two-pane on wide screens), foldable-aware if time permits.

### 4.10 Required deliverables in DESIGN.md
Color tokens (light/dark/AMOLED) as a table, type scale table, spacing/radius scale, icon list mapping every action to its icon name, component specs, screen wireframes (ASCII or Mermaid), motion spec (durations/easings), glass-effect spec, and 3 accent-color proposals.

---

## 5. ARCHITECTURE REQUIREMENTS (write into `docs/ARCHITECTURE.md`)

- Layers: `ui` (Compose screens, ViewModels) → `domain` (use cases, models) → `data` (repositories, Room, MediaStore, network, DataStore).
- **Playback lives in a `MediaSessionService`** (foreground, `mediaPlayback` type) holding the ExoPlayer; UI talks to it via `MediaController`. The UI must never own the player.
- Single source of truth for library: Room, populated by a scanner that diffs MediaStore. Expose as Flow.
- Queue + playback state persisted in Room/DataStore for restore.
- Error handling: sealed `Result`/`AppError`; every caught exception goes through `AppLogger` → Room (`error_log` table) → viewable in Settings > Logs.
- Include in the doc: module/package tree, Room schema (tables, keys, indices), sequence diagrams (play a track, restore session, edit tags, lyrics fetch), threading model, DI graph, caching policy (art, lyrics), and performance targets (cold start < 1.5 s on mid-range device; scrolling at 60fps with 10,000+ tracks; library scan incremental).

---

## 6. AUDIO FORMAT SUPPORT (write into `docs/AUDIO_FORMATS.md`)

Target support list:
- **Uncompressed:** WAV, AIFF, PCM, DSD
- **Lossless:** FLAC, ALAC, APE, WavPack (WV), TTA, WMA Lossless
- **Lossy:** MP3, AAC, Ogg Vorbis, Opus, WMA, ATRAC, MPC

**Be realistic and document this honestly.** Media3/ExoPlayer natively handles the common formats (MP3, AAC/M4A, FLAC, Ogg Vorbis, Opus, WAV, ALAC in MP4, and others depending on Android version). Rarer formats (APE, WavPack, TTA, MPC, DSD, ATRAC, WMA variants, AIFF on some devices) require extra decoding:
- Evaluate the **Media3 FFmpeg decoder extension** (must be built from source with the needed decoders enabled; document the build steps, APK size cost, and licensing/LGPL implications) and/or an alternative such as libVLC or a native decoding library.
- Produce a **support matrix**: format × (native / needs FFmpeg / unsupported) × notes (DSD is typically converted to PCM/DoP; ATRAC support depends on available codec libraries).
- Implement a **format capability check**: if a file's format can't be played, show a friendly message (not a crash) and log it.
- Scanner must index these extensions even if MediaStore doesn't classify them as audio (fallback file walk for user-selected folders).
- Show format badges (FLAC, Hi-Res, etc.) on tracks and in song details, with bit depth and sample rate.

---

## 7. ONLINE INTEGRATIONS (write into `docs/INTEGRATIONS.md`)

All online features are **optional**, need user consent for network use, and must degrade gracefully offline.

### 7.1 Gemini API
- User enters their own Gemini API key in Settings (stored with EncryptedSharedPreferences/Keystore-backed DataStore). Provide a "Test key" button.
- **Lyrics fallback:** only after local tags, `.lrc` files, and a free lyrics database fail. Be aware: Gemini may refuse or hallucinate copyrighted lyrics. Treat its output as unverified; label it "AI-generated, may be inaccurate" in the UI; never present it as official. Document this in `DECISIONS.md`.
- **Metadata help:** use Gemini to suggest correct title/artist/album/year/genre for badly tagged files (user reviews and confirms before saving).
- **Album art:** Gemini cannot reliably *download* images. Use proper art sources first: **MusicBrainz + Cover Art Archive**, iTunes Search API, Deezer API. Gemini may be used to disambiguate the album/artist query or to generate a clean search string. Show candidates in a grid; user picks one.
- Handle rate limits, timeouts, empty responses, and quota errors with clear messages and logs.

### 7.2 Song recognition provider
See 3.7. Use a real fingerprinting provider.

### 7.3 Privacy
- A first-use consent dialog explaining what is sent (song title/artist text, or ~10s of microphone audio for recognition). No analytics or tracking. No data leaves the device unless the user triggers an online action.

---

## 8. PERMISSIONS AND BACKGROUND PLAYBACK (write into `docs/PERMISSIONS_AND_BACKGROUND.md`)

- `READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` (≤12); `POST_NOTIFICATIONS` (13+); `RECORD_AUDIO` (only when using recognition); `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`; `WAKE_LOCK`; `INTERNET`; `ACCESS_NETWORK_STATE`; `MODIFY_AUDIO_SETTINGS`. Request each **just-in-time with a friendly rationale screen**, never all at launch.
- Foreground service with media-style notification so the OS does not kill playback. Use Media3's built-in notification provider, customized with icon-only actions.
- Battery: onboarding step and Settings row to request unrestricted battery use (see 3.8). Be aware Google Play restricts `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` to certain app categories (music players generally qualify, but document the Play policy risk and the fallback deep-link approach).
- Handle service restarts (`onTaskRemoved`, process death) and restore the session.

---

## 9. MILESTONES (copy into `docs/TASKS.md` as a checklist and expand)

1. **M0 Foundations:** project setup, Gradle version catalog, Hilt, theme/tokens from DESIGN.md, icon set, navigation shell, AppLogger + Logs screen skeleton.
2. **M1 Library:** permissions flow, MediaStore scanner, Room, Tracks/Albums/Artists/Genres/Folders lists, sorting, fast scroll.
3. **M2 Playback core:** MediaSessionService, notification, mini player, full player, seek, next/prev (smart previous), shuffle, repeat, queue + persistence.
4. **M3 Now Playing polish:** shared-element transitions, dynamic color, 3-page pager, animations.
5. **M4 Playlists and favorites, search, song actions menu** (queue, share, go to artist, delete).
6. **M5 Lyrics:** local + LRC sync, online lookup, caching, edit/save.
7. **M6 Tag editing + album art:** edit song, art search/pick, scoped-storage write flow.
8. **M7 Equalizer + sleep timer + audio effects.**
9. **M8 Settings:** appearance (font size, backgrounds, glass), battery flow, logs complete, gemini key.
10. **M9 Extra formats:** FFmpeg/decoder integration, capability checks, badges.
11. **M10 Song recognition.**
12. **M11 Polish and QA:** accessibility, tablet/landscape, performance profiling (Baseline Profiles), crash review, release build, ProGuard rules, README screenshots.

---

## 10. QUALITY BAR AND DEFINITION OF DONE

- Compiles cleanly, zero warnings you introduced, no `TODO` left in shipped code paths.
- No ANRs; no work on main thread beyond UI. Library of 10k+ tracks stays smooth.
- Every icon button has a content description. Every screen has loading, empty, and error states.
- Unit tests for: tap-detector (smart previous), sorting, LRC parser, queue logic, sleep timer, scanner diffing. UI tests for core flows. Document in `TESTING.md`.
- Every error path logs via `AppLogger`.
- UI review checklist before each milestone is marked done: *Does this look modern? Is it uncluttered? Are controls icons? Is there any blue gradient? (There must not be.) Does it feel fast?*

---

## 11. WHAT TO DO RIGHT NOW

1. Read this entire prompt.
2. Complete **Phase A**: create every document listed in section 1, fully written, concrete, and internally consistent.
3. In your final message of Phase A, give: a summary of key decisions, the 3 proposed accent colors, the format-support matrix summary, any feature you consider risky or infeasible (with alternatives), and a list of questions for me.
4. **Stop and wait for my approval.** Do not write app code yet.
