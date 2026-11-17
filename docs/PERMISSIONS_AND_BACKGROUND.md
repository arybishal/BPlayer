# Permissions, Background Playback & Battery Architecture — Pulse

## 1. Android Permission Strategy

Pulse adheres strictly to the principle of **Just-In-Time (JIT) Permission Requests**. Permissions are never requested in a bulk barrage on first app launch; each permission is requested only when the user navigates to a feature that requires it, accompanied by a clean, explanatory rationale dialog.

### 1.1 Complete Permission Matrix

| Permission | Android Version | Purpose | Prompt Timing |
|---|---|---|---|
| `READ_MEDIA_AUDIO` | API 33+ (Android 13+) | Scanning audio files from shared storage | First entry to Library or Home |
| `READ_EXTERNAL_STORAGE`| API 26–32 (Android 8.0–12) | Scanning audio files from shared storage | First entry to Library or Home |
| `POST_NOTIFICATIONS` | API 33+ (Android 13+) | Displaying persistent media playback notification | First time user presses "Play" |
| `FOREGROUND_SERVICE` | API 28+ (Android 9+) | Keeping playback alive when app is backgrounded | Declared in Manifest |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | API 34+ (Android 14+) | Required type declaration for media playback | Declared in Manifest |
| `WAKE_LOCK` | All | Preventing CPU sleep while audio decoding is active | Declared in Manifest |
| `RECORD_AUDIO` | All | Microphone snippet capture for song recognition | When user taps "What's this song?" |
| `INTERNET` | All | Online lyrics, artwork lookup, song recognition | Declared in Manifest |
| `ACCESS_NETWORK_STATE` | All | Verifying Wi-Fi vs. Mobile data connectivity | Declared in Manifest |
| `MODIFY_AUDIO_SETTINGS`| All | Attaching Equalizer & Audio FX to audio session | Declared in Manifest |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | API 23+ | Requesting battery optimization exemption | Settings > Battery Optimization |

---

## 2. Background Playback & MediaSession Foreground Service

Android's Low Memory Killer (LMK) will terminate background processes unless protected by a properly configured Foreground Service.

### 2.1 Service Lifecycle & Android 14 Compliance
`PlaybackService` inherits from AndroidX Media3's `MediaSessionService`:
- Declared with `android:foregroundServiceType="mediaPlayback"` in `AndroidManifest.xml`.
- When playback starts:
  ```kotlin
  override fun onPlaybackResumption(
      mediaSession: MediaSession,
      controller: MediaSession.ControllerInfo
  ): ListenableFuture<MediaItemsWithStartPosition> {
      // Restore persisted queue and position from Room/DataStore
      return restorePersistedQueueAsync()
  }
  ```
- **Foreground Transition**: When state transitions to `STATE_BUFFERING` or `STATE_READY` with `playWhenReady == true`, the service promotes itself using `startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)`.
- **Background Pause**: When the user explicitly pauses playback, the service remains foregrounded with the notification accessible for 10 minutes, after which it safely steps down to avoid unnecessary battery drain while retaining state in Room.

### 2.2 Rich Media Notification Design
The notification is managed via a custom `MediaNotification.Provider`:
- Displays extracted album artwork as a large notification icon.
- Dynamic color background automatically tinted by system MediaStyle notification logic.
- **Controls (Pure Vector Icons)**:
  - Previous Track (Smart Previous action)
  - Play / Pause toggle
  - Next Track
  - Favorite Heart toggle (custom action)
  - Close / Dismiss (when paused)

---

## 3. Battery Optimization & Aggressive OEM Mitigation

Many Android OEMs (Xiaomi, Samsung, Huawei, Oppo, Vivo, OnePlus) enforce proprietary battery-saving software (e.g. MIUI Battery Saver, One UI Sleeping Apps, ColorOS Quick Freeze) that terminates background audio services regardless of standard Android foreground rules.

### 3.1 Unrestricted Battery Access Flow
Pulse provides an explicit setup module under **Settings > Battery & Background Playback**:
1. Checks if the app is already whitelisted via `PowerManager.isIgnoringBatteryOptimizations(packageName)`.
2. If not whitelisted:
   - Displays an onboarding rationale explaining why background audio may stop unexpectedly.
   - Primary Action Button: **"Allow Unrestricted Battery Use"**.
   - Triggers the direct system dialog via `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (`package:com.pulse.player`).

### 3.2 Google Play Policy Risk & Fallback Strategy
Google Play policies restrict `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` to specific high-priority use cases. **Music and audio players that play audio while the screen is locked qualify under policy exceptions.** 

However, to guarantee 100% compliance during Play Store reviews:
- If `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is rejected or blocked on certain devices, Pulse provides a graceful fallback:
  ```kotlin
  val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
  // or Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
  ```
- The app automatically falls back to opening the system App Info / Battery Settings page where the user can manually select **"Unrestricted"**.

### 3.3 OEM-Specific Background Setup Guide
Pulse embeds a contextual help sheet detailing exact OEM-specific steps:

```markdown
### Xiaomi / Redmi / POCO (MIUI / HyperOS)
1. Go to Settings > Apps > Manage Apps > Pulse.
2. Enable "Autostart".
3. Under "Battery Saver", select "No Restrictions".
4. In recent apps, long-press Pulse and tap the Lock icon.

### Samsung (One UI)
1. Go to Settings > Apps > Pulse > Battery.
2. Select "Unrestricted" (do not use "Optimized").
3. Go to Device Care > Battery > Background Usage Limits.
4. Add Pulse to "Never sleeping apps".

### OnePlus / Oppo / Realme (OxygenOS / ColorOS)
1. Go to Settings > Battery > Battery Usage > Pulse.
2. Enable "Allow background activity" and "Allow auto-launch".

### Huawei / Honor (EMUI / Magic UI)
1. Go to Settings > Battery > App Launch > Pulse.
2. Switch from "Manage automatically" to "Manage manually".
3. Enable Auto-launch, Secondary launch, and Run in background.

### Vivo / iQOO (Funtouch OS)
1. Go to Settings > Battery > High background power consumption.
2. Toggle Pulse to ON.
```

---

## 4. Crash Survival & Process Death Restoration

To provide an effortless user experience, Pulse handles process death seamlessly:
1. `onTaskRemoved(rootIntent: Intent)`: If playback is currently active, the foreground service keeps playing. If playback is paused, the notification is cleanly dismissed, and the exact state is flushed to DataStore.
2. **Cold Launch Restoration**:
   - On app launch, `PlaybackConnection` checks DataStore for the last active `track_id`, `queue_list`, `playback_position_ms`, and `shuffle_mode`.
   - The UI immediately populates the Mini Player and Now Playing screen with the restored track metadata in a paused state.
   - Pressing "Play" instantly resumes playback from the exact millisecond where the user left off.
