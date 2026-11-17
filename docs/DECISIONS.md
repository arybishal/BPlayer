# Architecture Decision Records (ADRs) — Pulse

This document logs critical architectural, technical, and design tradeoffs made during the engineering of **Pulse**.

---

## ADR 001: MediaSessionService (AndroidX Media3) vs. Activity-Bound Player

### Context
Offline music players frequently suffer from playback termination when users swipe the app away from recent apps or switch to heavy applications (games, camera). In legacy Android architectures, players were either bound directly to an `Activity` lifecycle or used deprecated `MediaBrowserServiceCompat` APIs.

### Decision
Pulse adopts **AndroidX Media3 `MediaSessionService`** as a dedicated foreground service (`mediaPlayback` type) holding the solitary `ExoPlayer` instance. The UI connects as a `MediaController` client.

### Consequences & Tradeoffs
- **Positive**: Playback survives Activity recreation, backgrounding, configuration changes, and system memory pressure. Full out-of-the-box support for Android Auto, lock-screen controls, Bluetooth media buttons, and Wear OS controllers.
- **Negative**: Requires asynchronous IPC communication between the UI and service; slightly more complex setup than an in-Activity player.

---

## ADR 002: Smart Previous Button Implementation (Immediate Restart vs. Debounced Delay)

### Context
The Smart Previous button requirement:
- Single tap: restart the current song from 0:00.
- Double tap (within ~350ms): skip to the previous song in queue.

Traditional double-tap detectors delay firing the single-tap action until the 350ms timeout expires. If applied to an audio player, pressing "Previous" would introduce an intolerable **350ms delay of silence or delayed action** before the song restarts.

### Decision
Pulse implements an **eager single-tap with secondary-tap intercept**:
1. On the first tap, the player **immediately** seeks to `0:00` with zero latency.
2. A 350ms window timer starts.
3. If a second tap arrives within this 350ms window, the player immediately cancels the restart state and executes `seekToPreviousMediaItem()`.

### Consequences & Tradeoffs
- **Positive**: Instant tactile responsiveness. Single-tap restart feels immediate.
- **Negative**: During a double-tap, the current song briefly seeks to 0:00 for ~100–200ms before jumping to the previous track. In practice, this audio blip is barely noticeable and is standard behavior in leading hardware and software audio controllers (e.g. Apple AirPods, Sony WH-1000XM5).

---

## ADR 003: Metadata Tag Editing via Scoped Storage and JAudioTagger

### Context
Android 11+ (API 30+) strictly enforces Scoped Storage. Direct file modification using `java.io.File(path)` fails with `EACCES (Permission denied)` on shared media storage.

### Decision
Pulse uses **`MediaStore.createWriteRequest`** on Android 11+ to obtain scoped write permission for target audio URIs. Upon user confirmation, Pulse opens a `ParcelFileDescriptor` in read-write mode (`"rw"`) and performs byte-level ID3/Vorbis tag mutations using a lightweight Android-compatible build of **JAudioTagger**.

### Consequences & Tradeoffs
- **Positive**: 100% compliant with Google Play Store policies and modern Android storage guidelines. Enables editing metadata directly on internal storage and external SD cards.
- **Negative**: Requires a system dialog prompt when modifying files not originally created by Pulse.

---

## ADR 004: Lyrics Resolution Hierarchy & Gemini AI Guardrails

### Context
Offline users expect synchronized lyrics. While local files (`.lrc`) and open-source databases (LRCLIB) cover most commercial tracks, obscure indie or local songs often lack lyrics. Generative LLMs (Google Gemini) can theoretically provide lyrics, but LLMs may hallucinate verses or refuse full lyrics due to copyright guardrails.

### Decision
1. Strict resolution waterfall:
   - 1. Embedded tags (`USLT`/`SYLT`) $\to$ 2. Sidecar `.lrc` $\to$ 3. Room cache $\to$ 4. LRCLIB API $\to$ 5. Gemini AI Fallback.
2. If Gemini is used:
   - System prompt instructs Gemini to output `"LYRICS_UNAVAILABLE"` if unsure, preventing lyrical hallucinations.
   - The UI explicitly badges Gemini results as `⚠️ AI-Generated (Unverified)`.
   - Results are never presented as official licensed lyrics.

### Consequences & Tradeoffs
- **Positive**: Maximizes lyrics availability while upholding transparency and avoiding misleading the listener.
- **Negative**: Gemini is unable to provide timestamps for synchronization; AI lyrics remain unsynced text.

---

## ADR 005: Liquid Glass Effect with Hardware Capability Fallback

### Context
A modern visual design calls for frosted glass translucent surfaces. `Modifier.blur` and `RenderEffect` were introduced in Android 12 (API 31). Running real-time graphical blur on older devices (API 26–30) or low-tier GPU hardware can severely drop rendering performance from 60 FPS down to 15 FPS during list scrolling.

### Decision
Pulse implements a **conditional rendering strategy**:
- **API 31+**: `RenderEffect.createBlurEffect(28f, 28f)` with a translucent surface (`alpha = 0.65f`) and 1dp white highlight specular border (`alpha = 0.15f`).
- **API 26–30**: Automatic graceful fallback to a high-opacity tonal surface (`surfaceElevated` with `alpha = 0.92f`) without blur shaders.
- **User Setting**: A master toggle in Settings allows disabling the glass effect completely for maximum battery conservation.

### Consequences & Tradeoffs
- **Positive**: Stunning, contemporary UI on modern devices without crashing or degrading older devices.
- **Negative**: Slight visual variance across OS versions.

---

## ADR 006: Acoustic Song Identification Provider (AudD / ACRCloud vs. Gemini)

### Context
The user requested a song identification feature ("What's this song?"). 

### Decision
Pulse utilizes dedicated acoustic fingerprinting services (**AudD** or **ACRCloud**) via user-supplied API keys, and **strictly avoids using Gemini for audio recognition**.

### Rationale
Gemini is a multimodal large language model, not an acoustic fingerprinting engine. It lacks an inverted audio spectrogram index of the global recorded music repertoire. Attempting to pass 10 seconds of raw microphone PCM audio to Gemini to recognize a song is technically ineffective, results in high token cost, and produces frequent hallucinations. AudD and ACRCloud specialize in sub-second spectrogram hash matching against 100M+ commercial audio tracks.

---

## ADR 007: Battery Optimization Exemption Handling

### Context
Aggressive OEM battery managers (Xiaomi MIUI, Samsung One UI, Huawei EMUI) terminate background music playback processes after 10–20 minutes of screen-off time.

### Decision
Pulse implements a two-stage mitigation:
1. Prompts for `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
2. Provides a fallback deep-link to application details settings and an interactive OEM setup guide sheet.

### Rationale
Music players are explicitly recognized under Google Play policy exemptions for background power management. However, if an OEM disables the direct request dialog, our deep-link and guide guarantee that users can easily grant unrestricted background execution manually.

---

## ADR 008: Color Philosophy — Strict Exclusion of Blue Gradients

### Context
Many generic Android audio players rely on cold blue/purple/cyan linear gradients that look outdated and look like template UI kits.

### Decision
Pulse strictly forbids blue/purple/cyan linear gradients across all application surfaces:
1. Base theme uses warm, neutral charcoals (`#121214`), soft light off-whites (`#F9F9FB`), and true pitch-black AMOLED (`#000000`).
2. Accent palettes feature curated organic warm hues: **Warm Amber/Terracotta**, **Sunset Coral**, and **Deep Emerald**.
3. Dynamic artwork theming uses subtle tonal tinting with strict WCAG AA contrast ratio enforcement.
