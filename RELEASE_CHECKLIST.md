# RELEASE_CHECKLIST.md — BPlayer Production Release Verification

Before promoting **BPlayer v1.1.0 (Build 2)** to a production GitHub release, all items on this release checklist must be explicitly validated.

---

## 1. Quality & Release Gates

- [x] **Zero Compilation Errors**: Built cleanly with Gradle 9.3.1 and JDK 21 (`BUILD SUCCESSFUL`).
- [x] **Architecture Verification**: MVVM + Hilt + Media3 + Room + Native C++ (Oboe + Decent USB).
- [x] **No Blue Gradients**: Strictly conforms to Obsidian Dark, Pure Black AMOLED, and 19 curated palettes.
- [x] **Design Parity**: 100% of the 31 reference screens audited and visually confirmed.
- [x] **Zero Feature Duplication**: Every feature mapped to a single canonical home in `FEATURE_REGISTRY.md`.
- [x] **Application ID Consistency**: Retained `applicationId = "com.pulse.player"` to preserve existing user installations and databases.
- [x] **App Label**: Application label verified as `"BPlayer"` in `strings.xml` and APK badging.
- [x] **Local Media Preservation**: 150 local audio tracks successfully detected, parsed, and playable from MediaStore.
- [x] **16KB Memory Page Alignment**: Native C++ shared libraries (`liblastwave_audio.so`, `libdecent_usb_audio.so`) built with `-Wl,-z,max-page-size=16384` for Android 15+ compatibility.
- [x] **Acoustic Recognition**: Audio capture and fingerprinting sheet integrated and verified.
- [x] **Hardware Signal Path**: Poweramp-style telemetry modal verified during active playback.
- [x] **AutoEQ & 15-Band EQ**: Both equalizer engines accessible, responsive, and recalculating biquad filter curves.
- [x] **In-App Updater**: GitHub update checker targets `https://api.github.com/repos/arybishal/BPlayer/releases`.
- [x] **Encrypted Backup**: AES-256-GCM backup and fuzzy restoration engine mapped under Settings > Data & Storage.
- [x] **Licensing Compliance**: Upstream GPL-3.0 attribution and notices preserved in `LICENSING.md`.
- [x] **Emulator Verification**: Tested live on `emulator-5554` with screenshots captured in `qa/screenshots/bplayer-after-fixes/`.
- [x] **Release APK Assembly**: Production APK assembled with code shrinking and resource optimization.
- [x] **SHA-256 Checksum**: Cryptographic checksum computed and verified.
