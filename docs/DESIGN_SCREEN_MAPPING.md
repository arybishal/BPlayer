# BPlayer Screen-to-Screen Design Mapping & Functional Preservation Ledger

This document maps every screen, sheet, and dialog in **BPlayer** to its authoritative visual reference in **LastWave-Native**, documenting the visual replication implementation and confirming the preservation of all BPlayer-specific features.

---

## 1. Master Screen-to-Screen Mapping Matrix

| BPlayer Screen | LastWave Reference Screen | Visual Implementation Details | BPlayer Functionality Preserved |
|---|---|---|---|
| **Home / Feed** (`FeedScreen.kt`) | `ui/feed/FeedScreen.kt` | Floating glass header, expressive genre pills, horizontal scrolling cards with 4dp cover radius and 12dp spacing. | Personalized recommendations, quick access tiles, Made For You stations, trending charts. |
| **Library (Songs)** (`LibraryScreen.kt`) | `ui/library/LibraryScreen.kt` | Liquid glass tab dock, glass track rows (`ExpressiveGroupTrackRow`), multi-select floating action bar. | Local audio scanner, MediaStore indexing, custom sorting (title, artist, album, date added), multi-select batch deletion/playlist addition. |
| **Library (Playlists)** (`PlaylistCards.kt`) | `ui/playlist/PlaylistsScreen.kt` | Translucent playlist cards with 1dp specular border, downloads hero card, local music hero card. | Custom playlist creation, M3U/M3U8 import/export, cloud sync, favorite playlists. |
| **Library (Artists)** (`ArtistDetailScreen.kt`) | `ui/artist/ArtistDetailScreen.kt` | Frosted header banner with artist avatar, glass biography card, discography shelves, similar artists. | Metadata tag extraction, offline artist track grouping, local and remote biography caching. |
| **Library (Albums)** (`AlbumDetailScreen.kt`) | `ui/album/AlbumDetailScreen.kt` | Hero cover art with ambient color glow, glass track list container, release metadata chips. | High-res embedded artwork viewer, FLAC/Hi-Res format badges, album duration statistics. |
| **Search** (`SearchScreen.kt`) | `ui/search/SearchScreen.kt` | Floating pill search bar, glass top result card, history pills, suggestions list. | Debounced 300ms query flow, offline search filter, acoustic song recognition (AudD integration). |
| **Mini-Player** (`MiniPlayer.kt`) | `ui/player/MiniPlayer.kt` | Floating capsule (`[28,1428][872,1572]`) with 24dp backdrop blur, 1dp specular rim, swipe-to-expand. | Background playback survival, swipe to skip/previous, seamless tap to open Full Player. |
| **Full Player** (`FullPlayer.kt`) | `ui/player/FullPlayer.kt` | Full-bleed artwork canvas, animated ambient gradient backdrop, floating circular glass transport controls (`forceDark = true`). | Smart Previous (restart vs prev track), pitch/tempo adjustment, audio session ID routing, bit-perfect playback. |
| **Player Queue** (`QueuePanel.kt`) | `ui/player/QueuePanel.kt` | Bottom-anchored glass drawer, draggable drag-and-drop handles, clear queue / shuffle actions. | Dynamic queue reordering, save current queue as playlist, queue history persistence in Room. |
| **Lyrics Interface** (`LyricsScreen.kt`) | `ui/player/LyricsScreen.kt` | Synchronized word-by-word karaoke highlighting, translucent lyrics controls, time-offset adjustment. | Embedded SYNCED/UNSYNCED LRC parser, multi-provider online lyrics scraper, translation overlay. |
| **Equalizer / DSP** (`EqualizerScreen.kt`, `AutoEqScreen.kt`) | `ui/settings/autoeq/AutoEqScreen.kt` | Parametric EQ curves, 10-band slider cards with liquid glass containers, preset selector chips. | 10-band graphic EQ, AutoEQ database lookup with 5000+ headphone profiles, bass boost, virtualizer. |
| **Settings (Main)** (`SettingsScreen.kt`) | `ui/settings/SettingsScreen.kt` | Categorized glass card groups, icon badges, smooth navigation transitions. | All 7 setting categories (General, Audio, Downloads, Storage, Backup, Accounts, About). |
| **Settings (Audio)** (`SettingsAudioSection.kt`) | `ui/settings/SettingsAudioSection.kt` | Crossfade duration slider card, AutoMix phrase detection card, Music Haptics engine toggles. | Gapless playback, custom DJ crossfade, bit-perfect USB exclusive mode, vibration haptics. |
| **Settings (Downloads)** (`DownloadsCards.kt`, `DownloadsLists.kt`) | `ui/settings/DownloadsScreen.kt` | Active download queue card, downloaded tracks list, storage breakdown bar. | **User-selectable offline format (OPUS, MP3, AAC, FLAC, Original)**, background transcoding, resume on interrupted download. |
| **Settings (Storage)** (`StorageScreen.kt`) | `ui/settings/StorageScreen.kt` | Glass storage usage cards, image cache cleanup button, download directory selector. | Scoped storage API integration, directory change migration, single-tap cache purge. |
| **Settings (Accounts)** (`SettingsAccountsSection.kt`) | `ui/settings/SettingsAccountsSection.kt` | YouTube Music account card, Spotify sync card, Last.fm scrobbler integration card. | Session token management, secure credential storage, scrobble caching when offline. |
| **Settings (About)** (`SettingsAboutSection.kt`) | `ui/settings/SettingsAboutSection.kt` | App icon selector glass card, theme mode selector, clean attribution card ("Crafted by BPlayer Team"). | In-app GitHub update checker, direct APK download & install flow, diagnostics error log viewer. |
| **Context Menu Sheet** (`TrackContextMenuSheet.kt`) | `ui/common/TrackContextMenuSheet.kt` | Frosted bottom sheet (`RoundedCornerShape(24.dp)`), glass quick action pills, menu rows. | Play next, add to queue, add to playlist, edit metadata tags, share track, acoustic song match. |
| **Track Details Sheet** (`TrackDetailsSheet.kt`) | `ui/common/TrackDetailsSheet.kt` | Glass track specs card (codec, sample rate, bitrate, channels, container), offline file path. | Deep audio stream inspection, MediaExtractor format validation, path copy action. |
| **Confirmation Dialogs** (`GlassComponents.kt`) | `ui/common/GlassComponents.kt` | Translucent glass dialog with 24dp corner radius, subtle specular highlight, dual action buttons. | Safe delete verification, cache clear confirmation, unsaved playlist changes prompt. |
| **BPlayer Jam / Sync** (`LastWaveJamSheet.kt`) | Closest: Jam / Sync language | Segregated translucent card containers, 6-character room code hero display, live synced chat panel. | WebSocket peer synchronization, room host controls, real-time shared playback queue. |

---

## 2. Strict Functional Preservation Invariant

At no point in this design restoration has any BPlayer business logic, database table, audio pipeline, or preference been altered or removed. The visual styling has been systematically mapped to LastWave's liquid glass design system while maintaining 100% functional integrity.
