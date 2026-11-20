# BPlayer Brand Identity, Attribution & Cross-App Audit

**Document:** `docs/BRAND_IDENTITY_AUDIT.md`  
**Date:** November 2026  
**Status:** Audit Completed & Remediated  
**Official Repository:** `https://github.com/arybishal/BPlayer`  
**Application ID:** `com.pulse.player`  
**Product Name:** **BPlayer**  

---

## Executive Summary

This audit establishes the definitive brand identity, open-source attribution, and asset integrity for **BPlayer**. BPlayer is an independent, high-fidelity native Android music player engineered for audiophile playback, seamless local library management, and modern Material 3 fluid design.

While BPlayer's codebase originated from a modern native music player architecture (referencing LastWave-Native), BPlayer is **NOT** LastWave, nor is it affiliated with or an official product of LastWave's developers. Prior to this master audit, multiple remnants of upstream branding, extraneous cross-promotions, outdated repository URLs, and an inaccurate country flag attribution were present across various app resources and code files.

This document records the comprehensive audit, itemized classifications across all 5 mandatory tiers, and the exact remediation actions applied.

---

## 1. Classification Framework

Every audited element in the BPlayer repository is categorized into one of five standard tiers:

| Tier | Category | Policy & Treatment |
|---|---|---|
| **Tier 1** | **Required BPlayer Identity** | Canonical product identity. Must consistently reflect BPlayer, its official repository (`arybishal/BPlayer`), its package ID (`com.pulse.player`), and original design tokens. |
| **Tier 2** | **Legitimate Technical Attribution & Licenses** | Open-source licenses (GPL v3), copyright notices, and upstream architectural credits. **Preserved strictly** to comply with legal and ethical open-source standards. |
| **Tier 3** | **Reference-Only Development Material** | Reference codebase copies (e.g. `wave/LastWave-Native/`), architectural comparison notes, and reference migration documentation. Retained in internal developer-only docs or isolated folders. |
| **Tier 4** | **Incorrect Inherited Branding (Removed)** | Upstream app names, logos, package references, or strings mistakenly shown in user-facing surfaces (UI, notifications, share cards, settings). **Completely removed/purged**. |
| **Tier 5** | **Unrelated Cross-Promotion (Removed)** | Promotional app recommendations, desktop download banners, third-party companion app links (Lyricify, Sennheiser, Sleep Tracker) that do not belong to BPlayer. **Completely removed**. |

---

## 2. Complete Repository & Resource Audit Matrix

### 2.1 Identity, Package & Repository URLs

| Item / Resource | Prior State | Classification | Remediation Applied |
|---|---|---|---|
| **Application ID** | `com.pulse.player` | Tier 1 | Preserved for installed-app compatibility and user data continuity. |
| **Code Namespace** | `com.lastwave.app` | Tier 2 | Kept in internal Kotlin package names to preserve Room schema migrations and JNI audio driver bindings (`decent-usb-audio-driver`). |
| **App Name (Default)** | `BPlayer` | Tier 1 | Verified in `app/src/main/res/values/strings.xml` (`app_name = "BPlayer"`). |
| **App Name (Localized)** | `LastWave` in 14 locales | Tier 4 | Replaced with `BPlayer` across `values-ar`, `de`, `es`, `fr`, `hi`, `in`, `it`, `ja`, `ko`, `pt-rBR`, `ru`, `tr`, `vi`, `zh-rCN`. |
| **Official GitHub Repo** | Various (`ClashProjects/LastWave-Android`, etc.) | Tier 4 | Standardized exclusively to `https://github.com/arybishal/BPlayer`. |
| **Update Check API** | Upstream releases URL | Tier 4 | Updated to `https://api.github.com/repos/arybishal/BPlayer/releases/latest` and `/releases`. |
| **Source Code Link** | Outdated or upstream link | Tier 4 | Updated to `https://github.com/arybishal/BPlayer` in `SettingsAboutSection.kt` and `SettingsSearchIndex.kt`. |
| **Issue Tracker Link** | Generic or upstream link | Tier 4 | Updated to `https://github.com/arybishal/BPlayer/issues`. |
| **Discord Presence Links**| Upstream LastWave URLs | Tier 4 | Updated rich presence buttons to "Listen On BPlayer", "Get BPlayer" -> `https://github.com/arybishal/BPlayer`. |

---

### 2.2 Visual Branding, Logo & Launcher Icons

| Asset / Surface | Prior State | Classification | Remediation Applied |
|---|---|---|---|
| **Launcher Logo Mark** | Upstream "W" / Wave logo | Tier 4 | Replaced with original BPlayer geometric soundmark (resonant dual acoustic loops + play triangle). |
| **Light Theme Logo** | Single static asset or wave | Tier 4 | Replaced with theme-aware `ic_bplayer_logo.xml` (Deep Charcoal `#121316` + Vivid Emerald `#10B981` on Pure White `#FFFFFFFF`). |
| **Dark Theme Logo** | High-contrast wave or dark static | Tier 4 | Created `drawable-night/ic_bplayer_logo.xml` (Crisp White `#FFFFFF` + Radiant Emerald `#34D399` on Obsidian `#FF100F0F`). |
| **Monochrome Vector** | Upstream `ic_lastwave_logo_mono` | Tier 4 | Created `ic_bplayer_logo_mono.xml` with `evenOdd` fill rule for Android 13+ themed dynamic icons. |
| **Adaptive Launcher Icons**| Upstream icon layers | Tier 4 | Replaced `ic_launcher_foreground.xml` and `drawable-night/ic_launcher_foreground.xml` with new BPlayer emblem. |
| **Raster Mipmaps** | Upstream icons in mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi | Tier 4 | Replaced all `ic_launcher.png` and `ic_launcher_round.png` raster assets across all 5 densities (48px to 192px). |
| **Desktop Launcher Variant**| `AppIcon.DESKTOP` with `ic_launcher_desktop.png` | Tier 5 | Deleted `AppIcon.DESKTOP`, purged all desktop launcher PNGs, and aliased manifest entry safely to standard launcher. |
| **Splash Screen Brand** | Upstream branding in splash drawable | Tier 4 | Configured `ic_launcher_logo.xml` and `splash_background` in `values` and `values-night` for seamless zero-flicker launch. |

---

### 2.3 Attribution, Country Flags & Legal Compliance

| Surface / Code Location | Prior State | Classification | Remediation Applied |
|---|---|---|---|
| **Team Attribution** | `Crafted by Bplayer Team` with `ic_flag_india.xml` | Tier 4 | Deleted `ic_flag_india.xml`. Replaced attribution with clean text: `Crafted by BPlayer Team`. No country flags. |
| **About Card Layout** | Row with flag drawable + text | Tier 4 | Refactored in `SettingsAboutSection.kt` to clean centered `Text(stringResource(R.string.about_made_by))`. |
| **Open-Source Licenses** | GPL v3 License text & upstream notices | Tier 2 | **Preserved intact**. Legal compliance maintained under GPL v3 with proper copyright attribution to original authors and libraries. |
| **Third-Party Libraries** | JAudioTagger, Media3, ExoPlayer, Coil, Hilt, etc. | Tier 2 | Preserved with full upstream license integrity in Open Source Licenses sheet. |

---

### 2.4 Unrelated Cross-Promotions & Dead Destinations

| Component / Section | Prior State | Classification | Remediation Applied |
|---|---|---|---|
| **"Download Desktop App"** | Button in Settings About card & action cards | Tier 5 | Completely removed from `SettingsScreen.kt`, `SettingsAboutSection.kt`, and `SettingsSearchIndex.kt`. |
| **"More from us" Section** | Section featuring external apps | Tier 5 | Completely removed `MoreFromUsCard` from Settings and About. |
| **Sennheiser Promotion** | Promotional card with `ic_logo_sernheise.xml` | Tier 5 | Deleted `ic_logo_sernheise.xml` and card. (True Bluetooth / USB audio driver features remain fully intact). |
| **Lyricify Promotion** | Promotional card with `ic_logo_lyricify.xml` | Tier 5 | Deleted `ic_logo_lyricify.xml` and card. (BPlayer native LRC synchronizer and lyrics viewer remain fully intact). |
| **Sleep Tracker Promotion**| Promotional card with `logo_sleeptracker.png` | Tier 5 | Deleted `logo_sleeptracker.png` and card. (BPlayer internal sleep timer remains fully intact). |
| **Community Prompt Dialog**| Startup dialog directing to third-party group | Tier 4 | Cleaned `MainShell.kt` to direct support strictly to official GitHub Issues, Releases, and Source repository. |

---

### 2.5 In-App Strings, Notifications & Share Cards

| Component / File | Prior State | Classification | Remediation Applied |
|---|---|---|---|
| **Expanded Notification** | `LASTWAVE  •  NOW PLAYING` in `notification_player_expanded.xml` | Tier 4 | Replaced with `BPLAYER  •  NOW PLAYING`. |
| **Lyrics Share Card** | Hardcoded `"LastWave"` watermarked text | Tier 4 | Replaced with `"BPlayer"` in `ShareCardRenderer.kt`. |
| **Lyrics Share Logo** | `ic_lastwave_logo_mono` | Tier 4 | Replaced with `ic_bplayer_logo_mono`. |
| **Full Player Source** | Fallback track source label `"LastWave"` | Tier 4 | Replaced with fallback `"BPlayer"` in `FullPlayer.kt`. |
| **Track Context Sheets** | Default `sourceLabel = "LastWave"` | Tier 4 | Replaced with `"BPlayer"` in `TrackContextMenuSheet.kt` and `TrackMiniTraySheet.kt`. |
| **Launch Splash Text** | `"LastWave"` displayed in `NavGraph.kt` | Tier 4 | Replaced with `"BPlayer"`. |
| **Jam Session Sheet** | `"LastWave Jam"` and share intent text | Tier 4 | Replaced with `"BPlayer Jam"` and `"Join my BPlayer Jam session"` in `LastWaveJamSheet.kt` and `LastWaveJamViewModel.kt`. |
| **Backup Storage** | Filename `lastwave-backup-...` | Tier 1 / Tier 4 | Updated `BackupRepository.kt` to write `bplayer-backup-...`, while maintaining read compatibility for existing backup files. |
| **Auth Error Messages** | `"Failed to create LastWave account"` | Tier 4 | Updated to `"Failed to create BPlayer account"` in `AuthViewModel.kt`. |

---

## 3. Functionality Preservation Verification

A core directive of this master task was ensuring that brand cleanups did not degrade or remove legitimate playback features:

1. **Bluetooth & Headphone Controls**:  
   Purging the Sennheiser promotional card had zero effect on BPlayer's native Bluetooth AVRCP support, high-res USB DAC output via `decent-usb-audio-driver`, or becoming-noisy automatic pause handling.
2. **Synchronized Lyrics**:  
   Purging Lyricify cross-promotion did not alter BPlayer's internal LRC parsing, live line-by-line scrolling, translation support, or synced playback tracking.
3. **Sleep Timer**:  
   Purging the third-party Sleep Tracker promo did not affect BPlayer's built-in sleep timer with gradual audio fade-out.
4. **Library & Backup Continuity**:  
   Existing users can seamlessly read prior backup files (`lastwave-backup-*.json`), while new backups are written as `bplayer-backup-*.json`. Application ID `com.pulse.player` is preserved, safeguarding all Room database records and playlist definitions.

---

## 4. Verification & Sign-Off

- **Code Quality**: Clean build with zero deprecation or branding regressions.
- **Unit Tests**: Full unit test suite (`testDebugUnitTest`) passing cleanly.
- **Resource Integrity**: Zero missing asset references or dangling string IDs.
- **Release Verification**: Verified on actual Android emulator (`emulator-5554`) across both Light and Dark system UI modes.

Signed off by: **BPlayer Engineering Team**
