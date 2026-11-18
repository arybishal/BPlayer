# LICENSING.md — Open Source Licensing & Compliance Notice

## 1. Overview & Project Licensing

**BPlayer** is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.

This software incorporates and adapts source code and architecture from **LastWave-Native** (`https://github.com/arybishal/LastWave-Native`), which is also distributed under the GNU General Public License v3.0.

---

## 2. Upstream Attribution

- **Project**: LastWave-Native
- **Original Authors & Contributors**: Clash-Projects & Ary Bishal
- **Repository**: `https://github.com/arybishal/LastWave-Native`
- **Upstream License**: GNU General Public License v3.0 (GPL-3.0)

In accordance with GPL-3.0 Section 4 and Section 5:
1. **Notice Preservation**: All existing copyright notices, license banners, and author attributions are preserved across all source files.
2. **Derivative Works**: All modifications, integrations, and new components added in BPlayer are licensed under the terms of GPL-3.0.
3. **Source Code Availability**: Complete corresponding machine-readable source code for BPlayer is published openly in the BPlayer repository (`https://github.com/arybishal/BPlayer`).

---

## 3. Third-Party Libraries & Codecs

| Component | Upstream Project | License | Usage in BPlayer |
| :--- | :--- | :--- | :--- |
| **Decent USB Audio Driver** | Decent Audio / LastWave | GPL-3.0 / LGPL | Bit-perfect native USB DAC driver with 16KB memory page alignment. |
| **AndroidX Media3 & ExoPlayer** | Google LLC | Apache 2.0 | Core audio pipeline, buffering, and MediaLibraryService session host. |
| **Media3 FFmpeg Software Decoder** | Jellyfin / FFmpeg | GPL-3.0 / LGPL-2.1 | High-precision audio decoding for ALAC, FLAC, Vorbis, Opus, and DSD. |
| **Google Oboe** | Google LLC | Apache 2.0 | Low-latency C++ audio output via AAudio / OpenSL ES. |
| **SoX Resampler (soxr)** | Rob Sykes | LGPL-2.1 | Fast, high-quality 64-bit float audio resampling and dithering. |
| **InnerTubeX & NewPipeExtractor** | Team NewPipe / InnerTubeX | GPL-3.0 | YouTube Music streaming stream and metadata resolution. |
| **Kyant Backdrop & Shapes** | Kyant0 | Apache 2.0 | Liquid Glass frosted translucency and material surfaces. |
| **FletchMcKee Liquid** | FletchMcKee | Apache 2.0 | Liquid navigation optics for Android 13+. |
| **Google Sans Flex Font** | Google LLC | Google Fonts / SIL OFL | Core typography throughout BPlayer. |

---

## 4. GPL-3.0 Compliance Statement

BPlayer is free and open-source software. You can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

There are no proprietary binary blobs, undisclosed tracking mechanisms, or closed-source forks in this distribution.
