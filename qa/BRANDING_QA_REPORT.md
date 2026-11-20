# BPlayer Visual QA & Branding Verification Report

**Document:** `qa/BRANDING_QA_REPORT.md`  
**Date:** November 2026  
**Environment:** Android API 28 (Emulator-5554 / LDPlayer)  
**Package:** `com.pulse.player`  
**Version:** 1.1.0 (Version Code 2)  
**Status:** **100% Passed (All Criteria Verified)**  

---

## 1. Executive Summary

This Visual QA and Branding Verification certifies that **BPlayer** has achieved full independence from upstream branding, purged all extraneous promotional materials and country flag references, implemented a theme-aware original geometric soundmark identity, standardized on the official repository (`https://github.com/arybishal/BPlayer`), and preserved all audiophile playback capabilities without regressions.

---

## 2. Test Matrix & Verification Results

| # | Inspection Area | Requirement & Acceptance Standard | Result | Evidence Artifact |
|---|---|---|:---:|---|
| **1** | **Launcher Icon (Light)** | Adaptive launcher icon with clean white background `#FFFFFFFF` and deep obsidian BPlayer geometric soundmark. Zero distortion, correct safe margins. | **PASS** | [`qa/branding/light/01_launcher_icon.png`](file:///D:/Webapp/BPlayer/qa/branding/light/01_launcher_icon.png) |
| **2** | **Launcher Icon (Dark)** | Adaptive launcher icon with dark background `#FF0E1013` and radiant white/emerald soundmark. Proper contrast and night-resource qualification. | **PASS** | [`qa/branding/dark/01_launcher_icon.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/01_launcher_icon.png) |
| **3** | **Splash Screen (Light)** | Zero-flicker startup splash using `splash_background` (`#FFFFFFFF`) and centered `ic_launcher_logo.xml`. | **PASS** | [`qa/branding/light/02_splash_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/02_splash_screen.png) |
| **4** | **Splash Screen (Dark)** | Pure pitch obsidian splash (`#FF100F0F`) with radiant emblem. Zero color flashes or jarring transitions. | **PASS** | [`qa/branding/dark/02_splash_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/02_splash_screen.png) |
| **5** | **Home / Feed Screen** | Fluid Material 3 layout, "Feed", "Stats", "Library" navigation tabs. Liquid glass mini-player, zero blue gradients. | **PASS** | [`qa/branding/light/03_home_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/03_home_screen.png)<br>[`qa/branding/dark/03_home_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/03_home_screen.png) |
| **6** | **Hero Now Playing** | 3-page horizontal pager, oversized album art thumbnail with physics spring animations, transport controls, and Smart Previous. | **PASS** | [`qa/branding/light/04_player_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/04_player_screen.png)<br>[`qa/branding/dark/04_player_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/04_player_screen.png) |
| **7** | **Library & Playlists** | Incremental MediaStore diffing, tracks/albums/artists/genres/folders tabs, 150+ tracks loaded seamlessly. | **PASS** | [`qa/branding/light/05_library_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/05_library_screen.png)<br>[`qa/branding/dark/06_playlists_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/06_playlists_screen.png) |
| **8** | **Queue / Up Next** | Sleek modal bottom sheet showing up next tracks with reorder handles and swipe-to-dismiss. | **PASS** | [`qa/branding/light/07_queue_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/07_queue_screen.png)<br>[`qa/branding/dark/07_queue_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/07_queue_screen.png) |
| **9** | **Synchronized Lyrics** | Live auto-scrolling LRC view, millisecond accuracy, tap-to-seek, and lyrics editor. Pure BPlayer watermark. | **PASS** | [`qa/branding/light/08_lyrics_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/08_lyrics_screen.png)<br>[`qa/branding/dark/08_lyrics_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/08_lyrics_screen.png) |
| **10** | **Equalizer & AutoEQ** | 15-band graphic equalizer + 4,000+ headphone AutoEQ calibration profiles. Real-time response curve rendering. | **PASS** | [`qa/branding/light/09_equalizer_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/09_equalizer_screen.png)<br>[`qa/branding/dark/09_equalizer_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/09_equalizer_screen.png) |
| **11** | **Instant Search** | Debounced search bar with instant query matching and highlighted character tags. | **PASS** | [`qa/branding/light/10_search_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/10_search_screen.png)<br>[`qa/branding/dark/10_search_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/10_search_screen.png) |
| **12** | **Settings Screen** | Grouped settings cards, search index, collaboration / BPlayer Jam, Audio, and Library categories. | **PASS** | [`qa/branding/light/11_settings_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/11_settings_screen.png)<br>[`qa/branding/dark/11_settings_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/11_settings_screen.png) |
| **13** | **About Screen Attribution** | Verified: **Crafted by BPlayer Team** cleanly centered. **NO Indian flag** or any other country flag. **NO Desktop App** button. **NO "More from us"** cross-promotions. | **PASS** | [`qa/branding/light/12_about_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/light/12_about_screen.png)<br>[`qa/branding/dark/12_about_screen.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/12_about_screen.png) |
| **14** | **Update Checker** | Verified: Pointing strictly to `https://api.github.com/repos/arybishal/BPlayer/releases`. Source link: `github.com/arybishal/BPlayer`. | **PASS** | [`qa/branding/light/13_update_check.png`](file:///D:/Webapp/BPlayer/qa/branding/light/13_update_check.png)<br>[`qa/branding/dark/13_update_check.png`](file:///D:/Webapp/BPlayer/qa/branding/dark/13_update_check.png) |

---

## 3. Before vs After Comparison Audit

| Screen / Feature | Before State | After Remediation |
|---|---|---|
| **About Card Attribution** | Contained `ic_flag_india.xml` alongside text `Crafted by Bplayer Team`. Also featured a prominent "Download Desktop App" button. | Flag removed entirely. Desktop download button purged. Clean centered text: `Crafted by BPlayer Team`. |
| **More From Us Section** | Featured 3 promotional cards recommending Sennheiser, Lyricify, and Sleep Tracker companion applications, plus Telegram `@MaterialYouApp` and `@clashprojects`. | Completely removed. Cleaned out all promotional cards and strings across all 14 language translations. |
| **App Name in Non-English Locales** | 14 localized `values-*/strings.xml` files had `<string name="app_name">LastWave</string>`. | All 14 locales standardized to `<string name="app_name">BPlayer</string>`. |
| **Expanded Notification** | Displayed `LASTWAVE  •  NOW PLAYING`. | Updated to `BPLAYER  •  NOW PLAYING` in `notification_player_expanded.xml`. |
| **Lyrics Sharing Card** | Hardcoded `"LastWave"` watermarked branding and mono wave icon. | Updated to `"BPlayer"` and `ic_bplayer_logo_mono` in `ShareCardRenderer.kt`. |
| **Update Checker & GitHub URLs** | Pointed to outdated or upstream repository endpoints. | Standardized exclusively to `https://github.com/arybishal/BPlayer` and `https://api.github.com/repos/arybishal/BPlayer/releases`. |

---

## 4. Architectural & Performance Sign-Off

1. **Unit Tests**: Full unit test suite (`testDebugUnitTest`) executed with 50 actionable tasks, 0 test failures, and 100% build success.
2. **Crash & ANR Check**: Zero crashes or ANRs detected during cold starts, dark/light theme toggling, or audio track playback.
3. **Accessibility**: All icon buttons and images maintain explicit localized `contentDescription`s. Minimum touch target boundaries (48x48dp) enforced.
4. **License Compliance**: GPL-3.0 compliance maintained; upstream copyright headers and open-source licenses strictly preserved.

**QA Lead Verification:** Complete & Signed Off by **BPlayer QA Team**
