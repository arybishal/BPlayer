# Testing Strategy, Device Matrix & QA Checklist — Pulse

## 1. Testing Philosophy & Quality Standard

Pulse is engineered with the premise that an audio player must never crash, stutter, or drop audio frames. Our testing pyramid balances **fast unit tests** for business and playback state logic, **Compose instrumentation tests** for UI interactions, and a comprehensive **manual QA checklist** covering hardware-specific behavior (headset buttons, Bluetooth, audio focus, scoped storage).

---

## 2. Unit Testing Strategy

All domain logic, queue operations, state machines, and parsers must maintain > 90% unit test coverage.

### 2.1 Critical Test Suites
1. **`SmartPreviousTapDetectorTest`**:
   - Verify single tap at $t=0$ immediately triggers track restart.
   - Verify second tap arriving at $t=280\text{ms}$ (< 350ms window) triggers `skipToPrevious()`.
   - Verify second tap arriving at $t=400\text{ms}$ (> 350ms window) triggers second track restart.
   - Verify setting toggle "Always Previous" skips back on single tap.
2. **`LrcParserTest`**:
   - Parse standard LRC timestamps: `[01:23.45]`, `[00:12.345]`, and multi-timestamp lines: `[00:10.00][00:25.00]Chorus line`.
   - Verify sorted chronological order and binary search lookup for current playback position.
   - Validate handling of malformed lines, empty files, and non-UTF8 encodings.
3. **`PlaybackQueueManagerTest`**:
   - Verify "Play Next" inserts track at `currentIndex + 1`.
   - Verify "Add to Queue" appends track at `queue.size`.
   - Verify drag-and-drop reordering retains correct index pointers and active item tracker.
   - Verify queue shuffle preserves currently playing track at position 0 without repeating tracks.
4. **`LibraryScannerDiffTest`**:
   - Test incremental scanner: given 1,000 existing tracks, modifying 2 tracks and adding 1 new file results in exactly 3 database updates, skipping 998 unchanged files.
   - Test directory filtering with excluded folder paths.
5. **`SleepTimerTest`**:
   - Test countdown timer tick flow and 10-second fade-out volume interpolation curve ($1.0 \to 0.0$).
   - Test "End of current track" mode triggering stop exactly upon track completion.

---

## 3. Instrumentation & UI Testing (Compose)

Using `androidx.compose.ui.test.junit4.createAndroidComposeRule`:

1. **`NowPlayingScreenTest`**:
   - Verify clicking Play/Pause toggles icon state and announces correct TalkBack content description.
   - Verify dragging the custom seek bar updates the preview timestamp bubble.
   - Verify horizontal swiping transitions between Player, Lyrics, and Art Viewer pages.
2. **`LibraryNavigationTest`**:
   - Verify tab navigation between Tracks, Albums, Artists, Folders, and Playlists.
   - Verify search input debounces correctly and displays highlighted search matches.
3. **`TagEditorDialogTest`**:
   - Verify field validation (empty title fallback, numeric year constraints).

---

## 4. Target Device & Environment Matrix

Testing must span diverse Android OS versions, architectures, and OEM skins:

| Device Tier | Target Environment | OS Version / API | Primary Validation Focus |
|---|---|---|---|
| **Reference / Modern** | Pixel 7 / 8 / 9 | Android 14 / 15 (API 34/35) | Predictive back animations, Scoped storage, Liquid glass `RenderEffect`. |
| **Mainstream OEM** | Samsung Galaxy S21 / S23 | Android 13 / 14 (One UI 5/6) | Background service survivability, Lock screen media style, Audio focus. |
| **Aggressive Battery OEM** | Xiaomi Redmi Note 12 / POCO | Android 12 / 13 (MIUI 14 / HyperOS) | Aggressive task killing, Battery optimization whitelist deep link. |
| **Baseline / Minimum SDK** | Moto G6 / Generic Emulator | Android 8.0 / 8.1 (API 26/27) | Min SDK backward compatibility, Software blur fallback, Memory usage. |
| **Form Factor / Tablet** | Pixel Tablet / Galaxy Tab S8 | Android 14 (Landscape / Wide) | Two-pane adaptive layout, 600dp+ split view, touch targets. |

---

## 5. Manual QA Checklist

Before any milestone is deemed complete, execute this checklist on a physical device:

### 5.1 Permissions & First Run
- [ ] Fresh install: App launches and displays permission rationale before requesting media access.
- [ ] Denying permission displays clean empty state with button to re-prompt or open settings.
- [ ] Granting permission initiates immediate background scan without freezing UI.

### 5.2 Playback & Background Survival
- [ ] Tap track: Audio plays immediately through phone speaker / connected headphones.
- [ ] Lock screen: Playback continues uninterrupted; media notification shows cover art and playback controls.
- [ ] Notification controls: Play, pause, skip forward, and skip back respond instantly.
- [ ] Disconnecting headphones (`BECOMING_NOISY`): Audio pauses instantly without leaking to external speakers.
- [ ] Incoming phone call: Playback pauses during ring/call and resumes when call is terminated.
- [ ] 30 minutes screen-off: Background playback does not get killed by OEM battery manager.

### 5.3 Smart Previous Button
- [ ] Track playing at 0:15: Tap previous button once $\to$ Track restarts immediately from 0:00.
- [ ] Track playing at 0:15: Double tap previous button within 350ms $\to$ Jumps to previous track.
- [ ] Settings changed to "Always Previous" $\to$ Single tap jumps to previous track.

### 5.4 Lyrics & Album Art
- [ ] Track with embedded LRC: Displays synchronized lines, auto-scrolls, active line glows.
- [ ] Tapping a lyric line seeks audio playback directly to that position.
- [ ] Track with no lyrics: Tap "Search Online" $\to$ Fetches from LRCLIB and renders.
- [ ] Album Art page: Pinch-to-zoom works smoothly up to 3x zoom and bounces back.
- [ ] Tap "Find Album Art" $\to$ Candidate grid loads; picking an image updates cover everywhere.

### 5.5 Metadata Editing & Scoped Storage
- [ ] Edit track title and artist in Tag Editor.
- [ ] Android 11+ displays native system dialog: *"Allow Pulse to modify this file?"*.
- [ ] Tap "Allow" $\to$ Tags save successfully; library updates; external players show new tags.
- [ ] Delete track $\to$ System confirmation prompt appears; file is deleted from disk and library.

### 5.6 Audio Effects & Sleep Timer
- [ ] Equalizer: Adjust 60Hz and 14kHz bands $\to$ Audible change in audio profile.
- [ ] Sleep timer set to 15 minutes $\to$ Countdown chip appears on Now Playing.
- [ ] Sleep timer reaches final 10 seconds $\to$ Volume fades out smoothly to 0 before pausing.

### 5.7 Aesthetics & Strict Styling
- [ ] Verify **zero blue gradients** exist in any theme, background, or button.
- [ ] Toggle **Liquid Glass**: On API 31+, verify frosted background blur on Mini Player; on API 26–30, verify clean opaque fallback.
- [ ] Adjust **Font Size Slider**: Text across entire app scales proportionally from 85% to 140%.
- [ ] Error Logs: Throw test exception $\to$ Log entry appears in **Settings > Error Logs** with full stack trace, copy, and share actions.
