# Granular Feature Specifications & Acceptance Criteria — BPlayer

This document outlines detailed behavioral requirements, edge case mitigations, and explicit acceptance criteria for every feature in **BPlayer**.

---

## 1. Library & Browsing Engine

### 1.1 Local MediaStore Scanner & Indexer
- **Description**: Scans external and internal shared storage for audio files, parses metadata tags, and indexes them into Room.
- **Behavior**:
  - Triggers automatically on cold launch (if permissions granted) and on pull-to-refresh in the Library tab.
  - Compares file paths, sizes, and `date_modified` against Room records to skip untouched files (incremental diffing).
  - Filters out tracks shorter than the user-configurable threshold (default: 30 seconds) to exclude ringtones and notifications.
- **Edge Cases**:
  - *Corrupted Audio Header*: Skip file, record warning to `error_log`, and do not crash the scan.
  - *Missing Tags*: Fallback to raw filename for `title`, "Unknown Artist" for `artist`, and "Unknown Album" for `album`.
  - *Storage Unmounted / SD Card Ejected*: Safely mark missing tracks as unavailable without deleting user playlist mappings.
- **Acceptance Criteria**:
  - [ ] 10,000 files scanned incrementally in under 2.5 seconds on mid-range hardware.
  - [ ] Pull-to-refresh displays an active sync indicator and dismisses promptly upon completion.
  - [ ] Excluded folders in Settings are strictly ignored during scanner passes.

### 1.2 Categorized Views (Tracks, Albums, Artists, Genres, Folders, Playlists)
- **Behavior**:
  - Top tab strip / segmented control allows swift switching between the 6 library perspectives.
  - Tracks list displays index number, artwork thumbnail, title, artist, duration, and overflow menu icon.
  - Albums and Artists support both 2-column Grid and single-column List views via a top-right toggle.
  - Folders view displays physical directory tree, allowing users to drill into nested directories.
- **Sorting Options**:
  - Tracks: Title, Artist, Album, Date Added, Duration, Play Count (Ascending / Descending).
  - Albums/Artists: Name, Track Count, Release Year.
- **Acceptance Criteria**:
  - [ ] Sorting order persists across app restarts via DataStore.
  - [ ] Fast-scroll bubble displays current alphabet letter when scrolling through > 50 items.

### 1.3 Instant Search
- **Behavior**:
  - Search bar placed at top of main screen with instant query execution debounced by 300ms.
  - Returns unified categorized results: Top Result, Matching Tracks, Matching Albums, Matching Artists.
  - Highlights matched character sequences in bold accent color.
- **Acceptance Criteria**:
  - [ ] Typing generates results within 50ms of debounce expiration.
  - [ ] Clearing search query restores full library state smoothly without jarring animations.

---

## 2. Playback Core & Transport Engine

### 2.1 Smart Previous Button
- **Description**: Intelligent transport behavior designed for intuitive one-handed audio control.
- **Behavior Specification**:
  - **Single Tap**: Instantly restarts the currently playing track from position `0:00`.
  - **Double Tap (< 350ms)**: Jumps to the previous song in the queue (or loops to queue end if repeat-all is enabled).
  - **Implementation Mechanics**:
    - To preserve snappiness and avoid annoying 350ms audio latency on restart, the player **immediately seeks to 0:00 on the first tap**.
    - If a second tap arrives within 350ms, the player immediately commands `seekToPreviousMediaItem()`.
  - **User Configurable Setting** (**Settings > Playback > Previous Button Behavior**):
    - *Smart (Default)*: Single-tap restart, double-tap previous.
    - *Always Previous*: Every tap jumps to the previous track regardless of elapsed time.
    - *Always Restart*: Every tap restarts current track from 0:00.
- **Edge Cases**:
  - *First track in queue*: Double-tap restarts song or loops to last track based on Repeat mode.
  - *Audio track under 2 seconds duration*: Double-tap still reliably jumps to previous track.
- **Acceptance Criteria**:
  - [ ] Double-tap within 350ms skips back 100% of the time.
  - [ ] Single tap restarts playback instantly with zero auditory delay.

### 2.2 Shuffle & Repeat Modes
- **Shuffle**:
  - Toggling ON creates a non-repeating randomized index mapping of the current queue while preserving the currently playing track at position 0.
  - Toggling OFF restores the natural queue order without interrupting the active song.
- **Repeat Modes**:
  - Cycle states: `OFF` -> `ALL` (loop queue) -> `ONE` (loop current track indefinitely).
  - UI state reflected via distinct vector icons (`repeat` vs `repeat_one` with accent highlight).

### 2.3 Queue Management & Persistence
- **Behavior**:
  - Accessible via the Queue button in Now Playing or via a swipe-up sheet.
  - Drag-and-drop handles for reordering tracks in real time.
  - Swipe-to-dismiss removes track from queue with an "Undo" snackbar.
  - "Play Next": Inserts selected track immediately after current track.
  - "Add to Queue": Appends track to the tail of the current queue.
  - "Clear Queue": Clears all items except the currently playing track.
- **Persistence**:
  - Every queue mutation writes to Room `QUEUE_ITEM` table on a background thread.
  - On app crash or process kill, the active queue, active track index, and playback position (down to millisecond) are restored upon restart.
- **Acceptance Criteria**:
  - [ ] Reordering a 200-track queue executes smoothly with zero UI jank.
  - [ ] Force-killing the app during playback and relaunching restores the exact queue and track.

### 2.4 Audio Focus & System Events
- **Audio Becoming Noisy**: Unplugging wired headphones or disconnecting Bluetooth headphones immediately pauses playback.
- **Incoming Calls / Voice Prompts**:
  - Transient focus loss (navigation prompt): Ducks audio volume to 20% and restores smoothly over 400ms.
  - Full focus loss (phone call, ringtone): Pauses playback; auto-resumes when call ends if user was playing prior to call.

---

## 3. Hero Now Playing Screen (3-Page Horizontal Pager)

The Now Playing screen is the aesthetic heart of Pulse, presented as a full-screen modal with a horizontal `HorizontalPager` containing 3 dedicated views.

### 3.1 Page 1: Main Player View
- **Components**:
  - Top Bar: Collapse chevron (`keyboard_arrow_down`), "Playing from [Source]", More options (`more_vert`).
  - Hero Cover Art: 280x280dp rounded square (28dp radius) with soft ambient drop shadow.
  - Title & Artist: High-contrast typography with marquee scrolling for long titles.
  - Hero Seek Bar: Custom rounded track with draggable expanding thumb and time labels.
  - Transport Controls: Large centered 76dp Play/Pause morph button, flanked by Previous/Next (28dp), Shuffle, and Repeat.
  - Auxiliary Row: Sleep timer chip, Equalizer button, and Queue sheet launcher.
- **Dynamic Backdrop**:
  - Ambient gradient mesh or blurred album art tinting based on the user's Background Style setting.
  - Contrast AA enforced against all text tokens.

### 3.2 Page 2: Synchronized Lyrics View
- **Resolution Hierarchy**:
  1. Local embedded tags (ID3v2 `USLT` unsynced or `SYLT` synced).
  2. Local sidecar file in the same directory (`[filename].lrc`).
  3. Cached online lyrics in local Room database.
  4. Remote query to **LRCLIB API** (free, public lyrics database).
  5. Fallback: Google Gemini AI API (if user enabled key, clearly labeled "AI-Generated, Unverified").
- **Behavior**:
  - Smooth auto-scrolling keeping the active lyric line vertically centered with glowing accent color.
  - Past and future lines render in dimmed `onSurfaceMuted` font.
  - **Tap-to-Seek**: Tapping any lyric line instantly seeks audio playback to that line's timestamp.
  - If no lyrics are found: Friendly empty state with animated musical note illustration and a prominent "Search Lyrics Online" button.
  - Manual Edit / Save: User can edit lyrics and save back to local cache or export `.lrc` file.
- **Acceptance Criteria**:
  - [ ] Active line updates synchronously with audio playback within ±100ms.
  - [ ] Tap-to-seek seeks accurately without audio popping or crackling.

### 3.3 Page 3: Album Art & Visualizer View
- **Features**:
  - Full-resolution artwork display filling the screen.
  - Smooth pinch-to-zoom and pan gestures with physics bounce-back.
  - Format and codec badge overlay in the corner (e.g. `FLAC 24-bit / 96.0 kHz • 1824 kbps`).
  - **"Find Album Art"**:
    - Queries MusicBrainz, Cover Art Archive, and Deezer APIs.
    - Displays a 3-column modal grid of high-resolution candidate images.
    - User selects an image -> Prompt: "Embed in audio file or save to app cache?".
    - Applies selected artwork instantly and invalidates Coil cache.
  - **"Pick from Gallery"**: Allows selecting any local photo via system photo picker.

---

## 4. Song Actions & Scoped Storage Operations

### 4.1 Bottom Sheet Actions Menu
Triggered via track row overflow (`more_vert`) or long-press:
- *Play Next* / *Add to Queue*
- *Add to Playlist* (modal with "Create New Playlist" shortcut)
- *Toggle Favorite* (instant Room update with heart animation)
- *Share Song* (exports audio file via `FileProvider` to Android Sharesheet; or text "Title - Artist")
- *Go to Artist* / *Go to Album* (navigates library hierarchy)
- *Edit Song Info* (opens Tag Editor)
- *Delete Song* (triggers scoped deletion prompt)
- *Song Details* (technical metadata inspect modal)

### 4.2 Scoped Storage Metadata Tag Editor
- **Editable Fields**: Title, Artist, Album, Album Artist, Genre, Release Year, Track Number, Disc Number.
- **Android Scoped Storage Write Flow**:
  - Android 11+ (API 30+): Requests user confirmation via `MediaStore.createWriteRequest(contentResolver, listOf(trackUri))`.
  - Android 10 (API 29): Uses Scoped Storage SAF or legacy permissions.
  - On user grant: In-place byte write using JAudioTagger via `ContentResolver.openFileDescriptor(uri, "rw")`.
  - Notifies `MediaScannerConnection` to update system indices, then commits update to Room.
- **Acceptance Criteria**:
  - [ ] Metadata updates persist across external players and file managers.
  - [ ] User denial of system write dialog aborts operation without crash or data corruption.

### 4.3 Scoped Song Deletion
- **Behavior**:
  - Single track: Launches `MediaStore.createDeleteRequest(contentResolver, listOf(trackUri))`.
  - Multi-select: Users can select N tracks and delete them in a single atomic system dialog.
  - If approved: Deletes physical file, purges records from Room, and removes track from active queue if queued.

---

## 5. Audio Effects & Equalizer

### 5.1 System-Backed Equalizer
- **Engine**: Binds to `ExoPlayer.getAudioSessionId()` via `android.media.audiofx.Equalizer`.
- **UI Components**:
  - Master On/Off toggle switch.
  - 5 to 10 frequency band sliders (dependent on device hardware capabilities: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz).
  - Presets dropdown: Normal, Rock, Pop, Jazz, Classical, Hip Hop, Electronic, Acoustic, Vocal Booster, and Custom.
  - "Save Custom Preset" with user-defined preset name.
- **Auxiliary Effects**:
  - **Bass Boost**: `android.media.audiofx.BassBoost` with slider (0% to 100%).
  - **Virtualizer**: `android.media.audiofx.Virtualizer` with slider (0% to 100%).
  - **Loudness Enhancer**: `android.media.audiofx.LoudnessEnhancer` (API 19+) with target gain slider.
- **Acceptance Criteria**:
  - [ ] If device hardware does not support an effect, the slider is cleanly hidden with zero log crashes.
  - [ ] EQ settings persist across app restarts and re-apply automatically to new playback sessions.

---

## 6. Sleep Timer

- **Behavior**:
  - Floating dialog with presets: 15 min, 30 min, 45 min, 60 min, Custom Minute Picker, and "End of Current Track".
  - Active timer displays an elegant pill chip on the Now Playing screen showing remaining time (e.g. `🌙 18:42`).
  - **Fade-Out Transition**: During the final 10 seconds, audio volume scales down linearly from 1.0 to 0.0 before issuing `player.pause()`.
  - Tapping the timer chip allows canceling or extending the timer by 15 minutes.
- **Acceptance Criteria**:
  - [ ] Timer continues decrementing accurately when screen is turned off.
  - [ ] Fade-out occurs smoothly without abrupt audio cut-offs.

---

## 7. Acoustic Song Identification ("What's This Song?")

- **Behavior**:
  - Triggered via waveform/microphone icon in the top app bar.
  - Requests `RECORD_AUDIO` permission with an explanatory rationale bottom sheet.
  - Records a 10–12 second PCM audio snippet from device microphone.
  - Transmits audio fingerprint payload to user's configured recognition provider (**AudD** or **ACRCloud**).
  - **Visual Feedback**: Pulsing geometric waveform rings in the warm accent color (strictly no blue gradients).
  - **Result Card**:
    - Match found: Displays Title, Artist, Album, and Cover Art. Actions: "Search in My Library", "Copy Song Name", "Look up on Web".
    - No match: "No match found. Make sure music is clearly audible."
- **Acceptance Criteria**:
  - [ ] If user has not configured an API key in Settings, a helpful setup dialog is shown with a link to get a free key.
  - [ ] Recorded audio buffer is deleted from memory immediately following API transmission.

---

## 8. Diagnostic Logs & Crash Handler

- **Behavior**:
  - App installs an `UncaughtExceptionHandler` that catches fatal app crashes, serializes the stack trace, and writes a `FATAL` entry to Room before process exit.
  - Global `AppLogger` records info, warning, and non-fatal error events throughout the app.
  - **Settings > Error Logs**:
    - Chronological list of log entries with colored severity badges (DEBUG, INFO, WARN, ERROR, FATAL).
    - Search and filter by severity level.
    - Quick actions: **Copy to Clipboard**, **Share as .txt**, **Clear Logs**.
- **Acceptance Criteria**:
  - [ ] Force-induced runtime exception appears in the Logs screen upon subsequent app launch.
  - [ ] Exported `.txt` contains device model, OS version, app version, timestamp, and formatted trace.
