# LastWave-Native Glass Design Audit & Technical Reference

**Audit Date:** November 20, 2026  
**Audited Source:** `D:\Webapp\BPlayer\wave\LastWave-Native`  
**Purpose:** Official technical baseline for adapting the iOS-style translucent liquid glass design into BPlayer while preserving BPlayer's independent branding and architecture.

---

## 1. Executive Summary & Core Philosophy

LastWave-Native implements an **iOS/visionOS-inspired Liquid Glass Design System** utilizing hardware-accelerated offscreen layer capture (Kyant Backdrop) combined with a comprehensive Android 8–11 translucent fallback pipeline. 

The core philosophy centers on:
1. **Content-First Depth**: The background (album art, blurred palettes, and feed content) is never blocked by opaque flat slabs. Surfaces refract and reveal underlying visual movement.
2. **Sibling Render Hierarchy**: A glass surface is **never nested inside its own capture source**. Instead, content is captured via `layerBackdropCompat`, and floating elements (Dock, MiniPlayer, ExpressiveHeader) sit as siblings directly above it.
3. **Optically Correct Physics**: Surfaces incorporate G2 continuous-curvature squircles, fine 1dp specular highlights, top-edge reflections, and reactive spring physics during touch.

---

## 2. Technical Audit by Domain

### 1. Theme Implementation (`Theme.kt`)
- **Theme Modes**: Supports `ThemeMode.SYSTEM`, `ThemeMode.DARK`, and `ThemeMode.LIGHT`.
- **Theme Color Animation**: Dynamic color palette changes (driven by Monet wallpaper or Now Playing artwork extraction) glide smoothly via `animateColorScheme`:
  ```kotlin
  private const val THEME_TRANSITION_MS = 600
  // ColorScheme animated with tween(600) across all 24 Material3 color roles
  ```
- **Edge-to-Edge System Bars**:
  - `window.statusBarColor = Color.TRANSPARENT`
  - `window.navigationBarColor = Color.TRANSPARENT`
  - `isAppearanceLightStatusBars = !isDark`
  - `isAppearanceLightNavigationBars = !isDark`
  - `window.isStatusBarContrastEnforced = false` (API 29+)
  - `window.isNavigationBarContrastEnforced = false` (API 29+)

### 2. Surface Hierarchy & Layer Model
- **Layer 0 (Background)**: Root application canvas (`MaterialTheme.colorScheme.background`).
- **Layer 1 (Scrolling Content)**: Captured into `LayerBackdrop` using `Modifier.liquidGlassSource(backdrop)`.
- **Layer 2 (Floating Glass Chrome)**:
  - `ExpressiveHeader`: Anchored to top with `headerLiquidGlass`.
  - `MiniPlayer`: Floating capsule at bottom with `liquidGlass(backdrop, luminance)`.
  - `LiquidNavigationBar`: Floating dock with `railShadow` and `railTint`.
- **Layer 3 (Overlays & Dialogs)**: Modal sheets, context menus, and update dialogs floating above both content and chrome with dedicated frosted glass containers.

### 3. Color Values, Alpha Levels & Glass Recipes

#### Glass Recipe Constants (`LiquidGlass.kt` - `GlassRecipe`)
| Parameter | Value | Purpose |
| :--- | :--- | :--- |
| `BLUR_BASE_DP` | `12.dp` | Base blur level for resting glass surfaces |
| `BLUR_BRIGHT_DP`| `20.dp` | Blur level when underlying luminance is high |
| `BLUR_DARK_DP` | `6.dp` | Blur level when underlying luminance is low |
| `BRIGHTNESS` | `0.05f` | Color control brightness boost |
| `CONTRAST` | `1.0f` | Color control contrast unity |
| `SATURATION` | `1.1f` | Vibrant color boost for refracted light |
| `LENS_DIVISOR` | `6.0f` | SDF lens curvature radius divisor |
| `MIN_SCRIM` | `0.18f` | Minimum surface darkening scrim |
| `MAX_SCRIM` | `0.56f` | Maximum surface darkening scrim |
| `PRESS_BLUR_DP` | `2.0f` | Additional blur added during press |

#### Header Glass Recipe (`HeaderGlassRecipe`)
| Parameter | Value | Purpose |
| :--- | :--- | :--- |
| `BLUR_DP` | `24.dp` | Deep frost to preserve headline text legibility |
| `SATURATION` | `1.6f` | Extra vibrancy boost for header wash |
| `BRIGHTNESS_DARK` | `0.0f` | Dark mode brightness adjustment |
| `BRIGHTNESS_LIGHT`| `0.04f`| Light mode brightness adjustment |
| `TINT_DARK` | `0.30f` | Dark mode base tint (`Color.Black.copy(0.30f)`) |
| `TINT_LIGHT` | `0.46f` | Light mode base tint (`Color.White.copy(0.46f)`) |
| `LENS_HEIGHT_DP` | `14.dp` | Optical lens refraction height |
| `LENS_AMOUNT_DP` | `28.dp` | Optical lens refraction displacement |
| `RIM` | `Highlight(width = 1.dp)` | 1dp crisp specular border |

#### Navigation Dock Recipe (`LiquidNavigationBar.kt`)
| Parameter | Value | Purpose |
| :--- | :--- | :--- |
| `railTint (Dark)` | `Color(0xFF111319).copy(alpha = 0.32f)` | Floating dock tint in dark theme |
| `railTint (Light)`| `Color.White.copy(alpha = 0.30f)` | Floating dock tint in light theme |
| `railShadow` | `8.dp` blur, ambient `0.10f`, spot `0.14f` | Soft floating dock drop shadow |
| `lensTint` | `Color.White.copy(alpha = if (isDark) 0.012f else 0.018f)` | Optical selection bubble fill |
| `lensFrost` | `12.dp` | Match mini-player base blur |
| `refraction` | `0.07f` resting, up to `0.45f` on press | Optical displacement |
| `dispersion` | `0.01f` resting, `0.14f` on press | Subtle chromatic aberration during drag |

### 4. Specular Highlights & Illumination (`LiquidBubbleHighlight.kt`)
Local illumination provides authentic glass volume and curvature without heavy shaders:
- **Upper Reflection**: Linear gradient from `Offset.Zero` to `Offset(width * 0.78f, height)`:
  - 0%: `Color.White.copy(alpha = if (isDark) 0.68f else 0.80f)`
  - 24%: `Color.White.copy(alpha = 0.30f)`
  - 52%: `Color.Transparent`
  - 100%: `Color.Transparent`
- **Opposite Rim Reflection**:
  - 0%–62%: `Color.Transparent`
  - 84%: `Color.White.copy(alpha = 0.06f)`
  - 100%: `Color.White.copy(alpha = if (isDark) 0.28f else 0.34f)`
- **Lower Ambient Shade**:
  - 0%–50%: `Color.Transparent`
  - 74%: `Color.Black.copy(alpha = 0.025f)`
  - 100%: `Color.Black.copy(alpha = if (isDark) 0.13f else 0.09f)`
- **Surface Sheen**:
  - 0%: `Color.White.copy(alpha = if (isDark) 0.055f else 0.045f)`
  - 38%: `Color.White.copy(alpha = 0.006f)`
  - 64%: `Color.Transparent`
  - 100%: `Color.White.copy(alpha = 0.012f)`

### 5. Floating Navigation Dock (`LiquidNavigationBar.kt`)
- **Dock Height**: 64dp (standard tabs) / 60dp (compact).
- **Corner Shape**: Squircle (`SquircleShape(radius = 24.dp)`) or `CircleShape`.
- **Tab Selection Transition**:
  - Snappy spring physics: `spring(dampingRatio = 0.95f, stiffness = 520f)`.
  - Trailing shape spring: `spring(dampingRatio = 0.92f, stiffness = 540f)`.
  - Drag gesture inspector: rubber-band scrubbing across navigation items without locking outer gesture scrolls.

### 6. Top Bars & Toolbars (`ExpressiveHeader.kt` & `GlassHeaderLayout.kt`)
- **Header Shape**: `RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)`.
- **Header Glass Wash**: Theme-tinted gradient preventing dark hollows:
  - `primary.copy(alpha = 0.20f)` -> `tertiary.copy(alpha = 0.08f)` -> `secondary.copy(alpha = 0.16f)`.
- **Header Action Buttons**: `FilledTonalIconButton` with container color:
  - `Color.White.copy(alpha = if (isDark) 0.12f else 0.20f)` in glass mode.
- **GlassHeaderLayout Inset Handling**: Automatically offsets list content by `headerHeight`, letting list items smoothly pass underneath the frosted top bar.

### 7. Interactive Controls & Buttons (`PlayerControls.kt`)
- **Transport Buttons** (Play, Pause, Skip, Prev):
  - Touch target $\ge 48\times 48\text{ dp}$ (hero play/pause is 72–80dp).
  - High-alpha translucent surfaces: `Color.White.copy(alpha = 0.14f)` at rest.
  - Active toggle buttons: `MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.86f)` with bottom indicator pip.
  - Press animation: `animateFloatAsState(if (pressed) 0.92f else 1f, ExpressiveMotion.spatialSpring())`.
- **Divided Utility Pill**: Split capsule containing Audio Controls and Video Toggle with a 1dp vertical separator (`foreground.copy(alpha = 0.22f)`).

### 8. Cards & List Items (`LiquidGlassCard.kt` & `ExpressiveGroup.kt`)
- **Corner Radii**:
  - Large Cards: `24.dp`.
  - Grouped List Items: `groupShape(position)` with 22dp outer corners and 4dp inner corners for contiguous lists.
- **Surface Elevation**: `0.dp` default elevation to prevent Android's default tonal elevation from compounding unwanted grey layers over translucent glass.

### 9. Dialogs & Bottom Sheets (`TrackContextMenuSheet.kt`, `AppUpdateDialog.kt`)
- **Modal Sheets**:
  - `shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`.
  - `containerColor = Color.Transparent` (enables background blur visibility).
  - `scrimColor = Color.Black.copy(alpha = 0.32f)`.
  - Content rows wrapped in grouped translucent cards with `surfaceContainerHigh` or `iosTranslucentGlass`.
- **Dialogs**:
  - `shape = RoundedCornerShape(28.dp)`.
  - 1dp specular border (`Color.White.copy(0.22f)` to `Color.White.copy(0.06f)`).
  - Translucent container fill (`0.85f` alpha).

### 10. Player & Mini-Player (`MiniPlayer.kt` & `FullPlayer.kt`)
- **Mini-Player**:
  - Capsule docked at bottom: `RoundedCornerShape(28.dp)`.
  - Dynamic luminance adaptation: samples background luminance and adjusts scrim between `0.18f` and `0.56f`.
  - 1dp subtle specular border and top highlight.
- **Full Player Ambient Backdrop**:
  - Base: `#050505` pitch black.
  - Blurred Artwork: `Modifier.blur(36.dp)` or `Modifier.blur(46.dp)` on API 31+; RenderScript/StackBlur `BlurTransformation(radius = 25)` on API < 31.
  - Vertical 3-stop gradient extracted from album art with alpha `0.88f`, `0.76f`, `0.96f`.

### 11. Fallback Strategy for API 26–30 & Non-Blur Devices
When `Build.VERSION.SDK_INT < Build.VERSION_CODES.S` (API 31) or when `ActivityManager.isLowRamDevice` is true:
- Kyant Backdrop hardware blur is bypassed to prevent render thread crashes or severe frame drops.
- **Deliberate Translucent Tint Fallback**:
  - Surface tint: `Color(0xFF101318).copy(alpha = 0.68f)` in dark mode, `Color(0xFFFFFFFF).copy(alpha = 0.78f)` in light mode.
  - Border: 1dp brush gradient from `Color.White.copy(0.22f)` to `Color.White.copy(0.06f)`.
  - Sheen: Vertical specular gradient `Color.White.copy(0.12f)` fading to transparent at 45% height.
  - Text & Icons: Remain 100% sharp with WCAG AA contrast.

---

## 3. Comparison & Gap Analysis with Current BPlayer

| Feature / Area | LastWave-Native Baseline | BPlayer Current State | Action Required in Phase 2 |
| :--- | :--- | :--- | :--- |
| **Glass Material Foundation** | Centralized `drawInteractiveGlass` + `iosTranslucentGlass` fallback | Partial fallback implemented in previous turn | Consolidate canonical tokens; create centralized `GlassSurface`, `GlassCard`, `GlassButton`, etc. |
| **Bottom Navigation Dock** | Floating Squircle/Capsule dock with liquid lens & bubble highlight | Restored dock by default | Add permanent design locks; verify tab transitions and stable positioning |
| **Top App Bars** | `ExpressiveHeader` + `GlassHeaderLayout` | Header layout used on some screens | Roll out `GlassHeaderLayout` across Library, Albums, Artists, Search, and Settings |
| **Cards & Panels** | Translucent cards with 0dp elevation and grouped shapes | Some screens use opaque M3 `Card` / `Surface` | Replace flat opaque card surfaces with `GlassCard` across all screens |
| **Dialogs & Bottom Sheets** | Transparent sheet container with grouped floating cards | Some dialogs still use default M3 solid container | Standardize all dialogs and sheets to `GlassDialog` and `GlassBottomSheet` |
| **Light & Dark Consistency** | Harmonious translucent treatment in both modes | Dark mode mostly tuned, light mode needs verified contrast | Apply light-mode translucent tokens (`0.78f` white tint, `0.06f` border) |

---

## 4. Verification Checkpoints for Implementation
1. **Compilation**: Zero compilation warnings or missing symbols.
2. **Backdrop Safety**: Sibling render pattern strictly enforced; no self-capture loops.
3. **Contrast Compliance**: Minimum 4.5:1 text-to-background contrast ratio in both light and dark themes.
4. **Performance**: Consistent 60/120 FPS list scrolling without frame spikes on mid-range devices.
