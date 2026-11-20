# BPLAYER DESIGN CONSTITUTION (DESIGN_RULES.md)

**Status:** PERMANENT & MANDATORY  
**Authority:** Product Owner & Core Architecture  
**Applies to:** All Engineers, Coding Agents, Automated Tools, and Contributors  
**Scope:** Whole Repository (`D:\Webapp\BPlayer`)

---

## Preamble

BPlayer is an independent, premium music player product for Android. The approved visual direction for BPlayer is the **iOS-style translucent liquid glass interface adapted from LastWave-Native**.

This document is the **authoritative and binding design constitution** for BPlayer. It defines non-negotiable rules protecting the visual identity, interaction fidelity, and aesthetic excellence of the app against regressions, unauthorized overhauls, or accidental reverts to generic Material 3 design.

---

## CRITICAL DIRECTIVE: LASTWAVE VISUAL FIDELITY RULE

**LastWave-Native is the authoritative visual reference for BPlayer.** Future developers and coding agents must not independently redesign, simplify, flatten, recolor, replace or remove the established visual design.

**The existing BPlayer feature set must be preserved while the reference design is reproduced.**

Any proposed visual change must:
1. **Be compared against LastWave**: Validate layout, typography, corner radii, borders, and transparency side-by-side.
2. **Be documented**: Explicitly record differences in `qa/SIDE_BY_SIDE_DESIGN_COMPARISON.md`.
3. **Be checked for impact on existing features**: Ensure zero features, settings, or audio capabilities are dropped.
4. **Receive explicit user approval** before implementation if it changes the established design direction.

**Absolute prohibitions:**
- ❌ **DO NOT** silently replace translucent surfaces with opaque surfaces.
- ❌ **DO NOT** introduce a new or alternative design system.
- ❌ **DO NOT** introduce new colors, typography, component styles, or animations that conflict with the reference.
- ❌ **DO NOT** apply `Modifier.blur()` to cards (blurs foreground content rather than the backdrop).
- ❌ **DO NOT** remove or alter BPlayer branding (`BPlayer` name, original logos, package `com.pulse.player`).

---

## The 12 Fundamental Design Rules

### Rule 1: Translucency is Mandatory
BPlayer's approved visual identity is fundamentally built upon the iOS-style translucent glass design adapted from LastWave-Native.
- Background visibility through glass surfaces is an essential aesthetic requirement.
- Never replace the approved glass design with ordinary opaque backgrounds as a general design decision.
- Content (album artwork, artist imagery, gradients, list items) must remain visibly refracted or observable beneath floating chrome, headers, mini-players, docks, cards, sheets, and dialogs.

### Rule 2: No Unauthorized Design-System Replacement
No developer, coding agent, automated refactoring tool, or future implementation task may replace the approved design system with:
1. Default Material 3 flat visual styling.
2. Flat opaque cards or solid containers.
3. Solid opaque navigation surfaces or rectangular bars.
4. Generic default Material buttons.
5. Unapproved solid background colors.
6. An unrelated design language or template.

Any change that materially alters the established visual identity requires explicit, written approval from the product owner.

### Rule 3: Centralized Design Tokens
All visual tokens must have a single canonical definition in `com.lastwave.app.ui.theme.GlassTokens` and `LiquidGlass.kt`:
- **Light glass colors & tints**: `Color.White.copy(alpha = 0.78f)` container fallback, `Color.Black.copy(alpha = 0.04f)` scrim.
- **Dark glass colors & tints**: `Color(0xFF101318).copy(alpha = 0.68f)` container fallback, `Color.Black.copy(alpha = 0.32f)` scrim.
- **Surface alpha values**: Rest state `0.14f`, Scrim ranges `0.18f` to `0.56f`.
- **Backdrop blur levels**: Base `12.dp`, Bright `20.dp`, Dark `6.dp`, Header `24.dp`, Ambient `36.dp`–`46.dp`.
- **Border colors & opacity**: 1dp linear gradient stroke from `Color.White.copy(alpha = 0.22f)` to `Color.White.copy(alpha = 0.06f)`.
- **Shadow & elevation**: `8.dp` floating rail shadow (ambient `0.10f`, spot `0.14f`); `0.dp` elevation on cards to avoid dual-tone grey muddying.
- **Corner radii**: `28.dp` (dialogs, sheets, mini-player), `24.dp` (cards, headers, squircle dock), `22.dp` (grouped settings/menus), `16.dp` (track rows), `CircleShape` (buttons, pills).
- **Control sizes**: Hero play/pause $\ge 72\text{ dp}$, standard touch targets $\ge 48\times 48\text{ dp}$.
- **Animation duration & easing**: Theme transition `tween(600)`, tab springs `spring(dampingRatio = 0.95f, stiffness = 520f)`.

Do not create competing or fragmented definitions for the same design property.

### Rule 4: Reusable Glass Components
All screens must reuse the canonical shared glass components rather than independently recreating ad-hoc styling:
- `GlassSurface`: Base translucent container with 1dp specular border and optional backdrop refraction.
- `GlassCard`: Standardized card replacing opaque Material 3 `Card`.
- `GlassButton`: Primary, secondary, and tonal interactive buttons.
- `GlassIconButton`: 48dp circular icon button with touch feedback.
- `GlassNavigationBar`: Floating squircle/capsule dock with liquid selection bubble.
- `GlassTopBar`: Extended expressive header with status bar padding and glass refraction.
- `GlassDialog`: Modal alert and confirmation dialogs with frosted glass containers.
- `GlassBottomSheet`: Modal bottom sheets with transparent outer container and grouped glass cards.
- `GlassPanel`: Standalone panel for queue, lyrics, equalizer, and player details.

### Rule 5: No Fake Backdrop Blur
Never describe a component as blurred glass if only its foreground content is blurred:
- Applying `Modifier.blur()` to a card's own contents blurs the text and icons—it does **not** blur what is behind the card.
- Use genuine backdrop refraction via hardware-accelerated sibling capture (`drawBackdrop` / Kyant Backdrop) on supported devices.
- On unsupported devices, provide an authentic, high-quality translucent tint fallback (`iosTranslucentGlass`) with 1dp specular gradient border and top sheen. Text and icons must always remain 100% razor sharp.

### Rule 6: Theme Consistency
Light and dark modes must preserve the same translucent visual identity:
- **Light Theme**: Light translucent surfaces (`Color.White.copy(alpha = 0.78f)`), subtle depth, fine specular borders, dark high-contrast typography.
- **Dark Theme**: Dark translucent surfaces (`Color(0xFF101318).copy(alpha = 0.68f)`), subtle depth, fine specular borders, light high-contrast typography.
- Never revert to flat black cards in dark mode or flat white cards in light mode simply because of theme switching or device limitations.

### Rule 7: No Accidental Opaque Regressions
Before merging or delivering any UI change, inspect whether new or modified components introduce:
- Solid card backgrounds (`CardDefaults.cardColors()` with default container colors).
- Opaque navigation bars (e.g. standard rectangular Material 3 navigation bars).
- Default Material surfaces that occlude underlying content.
- Unintended background replacements.
- Incorrect alpha values or missing borders.
Exceptions must be deliberate, documented in `docs/DECISIONS.md`, and consistent with the reference design.

### Rule 8: Reference Fidelity
LastWave-Native (`D:\Webapp\BPlayer\wave\LastWave-Native`) remains the authoritative visual and interaction reference for the approved translucent design.
- Extract actual source implementations, measurements, and tokens rather than guessing.
- BPlayer must strictly retain its own identity, package name (`com.pulse.player`), official repository (`https://github.com/arybishal/BPlayer`), and original features.

### Rule 9: Performance and Accessibility
The glass design must never compromise:
1. Audio playback stability (background playback, audio focus ducking).
2. 60/120 FPS scrolling smoothness.
3. Touch responsiveness and 48x48 dp minimum touch targets.
4. Battery efficiency.
5. WCAG AA text contrast ratio ($\ge 4.5:1$).
6. Low-end device compatibility via graceful fallback.
Do not apply expensive live blur passes to continuously animating or full-screen areas without hardware acceleration.

### Rule 10: Screenshot Approval
Every substantial UI change must include screenshots in both light and dark modes:
- Store verification captures in `qa/glass-design/light/` and `qa/glass-design/dark/`.
- Visually inspect that elements underneath are visible through glass chrome.
- A task cannot be considered complete if it introduces an unapproved visual regression.

### Rule 11: No Silent Design Changes
If a user prompt, future task, or external dependency conflicts with these rules:
- **STOP and report the conflict.**
- Do not independently decide to alter or abandon the glass design system.
- Clarify requirements with the user while highlighting the design rules.

### Rule 12: Design Preservation is Part of Definition of Done
A feature or milestone is not complete merely because the code compiles or unit tests pass.
It is only complete when:
1. It strictly adheres to BPlayer's established translucent glass design system.
2. It passes light and dark theme visual inspection.
3. It complies with all 12 rules of this Constitution.
