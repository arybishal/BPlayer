package com.pulse.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

enum class AccentChoice {
    WARM_AMBER, SUNSET_CORAL, DEEP_EMERALD
}

@Immutable
data class ExtendedColors(
    val surfaceHighlight: Color,
    val onSurfaceMuted: Color,
    val onSurfaceSubtle: Color,
    val outlineVariant: Color,
    val statusDanger: Color,
    val statusSuccess: Color,
    val statusWarning: Color = StatusWarning,
    val accent: Color = AccentAmber,
    val surfaceElevated: Color = DarkSurfaceElevated,
    val surfaceCard: Color = DarkSurface,
    val isGlassEnabled: Boolean = true
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceHighlight = DarkSurfaceHighlight,
        onSurfaceMuted = DarkOnSurfaceMuted,
        onSurfaceSubtle = DarkOnSurfaceSubtle,
        outlineVariant = DarkOutlineVariant,
        statusDanger = StatusDanger,
        statusSuccess = StatusSuccess,
        statusWarning = StatusWarning,
        accent = AccentAmber,
        surfaceElevated = DarkSurfaceElevated,
        surfaceCard = DarkSurface,
        isGlassEnabled = true
    )
}

val LocalFontScale = staticCompositionLocalOf { 1.0f }

@Composable
fun PulseTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentChoice: AccentChoice = AccentChoice.WARM_AMBER,
    fontScale: Float = 1.0f,
    isGlassEnabled: Boolean = true,
    dynamicAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val isAmoled = themeMode == ThemeMode.AMOLED

    val primaryAccent = dynamicAccentColor ?: when (accentChoice) {
        AccentChoice.WARM_AMBER -> AccentAmber
        AccentChoice.SUNSET_CORAL -> AccentCoral
        AccentChoice.DEEP_EMERALD -> AccentEmerald
    }

    val colorScheme: ColorScheme = when {
        isAmoled -> darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color.Black,
            primaryContainer = primaryAccent.copy(alpha = 0.20f),
            onPrimaryContainer = primaryAccent,
            background = AmoledBackground,
            onBackground = AmoledOnSurface,
            surface = AmoledSurface,
            onSurface = AmoledOnSurface,
            surfaceVariant = AmoledSurfaceElevated,
            onSurfaceVariant = AmoledOnSurfaceMuted,
            outline = AmoledOutline
        )
        isDark -> darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color(0xFF1E1005),
            primaryContainer = primaryAccent.copy(alpha = 0.20f),
            onPrimaryContainer = primaryAccent,
            background = DarkBackground,
            onBackground = DarkOnSurface,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurfaceElevated,
            onSurfaceVariant = DarkOnSurfaceMuted,
            outline = DarkOutline
        )
        else -> lightColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = primaryAccent.copy(alpha = 0.15f),
            onPrimaryContainer = primaryAccent,
            background = LightBackground,
            onBackground = LightOnSurface,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurfaceElevated,
            onSurfaceVariant = LightOnSurfaceMuted,
            outline = LightOutline
        )
    }

    val extendedColors = ExtendedColors(
        surfaceHighlight = when {
            isAmoled -> AmoledSurfaceHighlight
            isDark -> DarkSurfaceHighlight
            else -> LightSurfaceHighlight
        },
        onSurfaceMuted = when {
            isAmoled -> AmoledOnSurfaceMuted
            isDark -> DarkOnSurfaceMuted
            else -> LightOnSurfaceMuted
        },
        onSurfaceSubtle = when {
            isAmoled -> AmoledOnSurfaceSubtle
            isDark -> DarkOnSurfaceSubtle
            else -> LightOnSurfaceSubtle
        },
        outlineVariant = if (isDark || isAmoled) DarkOutlineVariant else LightOutline,
        statusDanger = StatusDanger,
        statusSuccess = StatusSuccess,
        statusWarning = StatusWarning,
        accent = primaryAccent,
        surfaceElevated = when {
            isAmoled -> AmoledSurfaceElevated
            isDark -> DarkSurfaceElevated
            else -> LightSurfaceElevated
        },
        surfaceCard = when {
            isAmoled -> AmoledSurface
            isDark -> DarkSurface
            else -> LightSurface
        },
        isGlassEnabled = isGlassEnabled
    )

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalFontScale provides fontScale,
        LocalIsDarkTheme provides isDark,
        LocalLiquidGlass provides isGlassEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = createPulseTypography(fontScale),
            shapes = PulseShapes,
            content = content
        )
    }
}

/** BPlayer Theme wrapper matching BPlayer brand identity. */
@Composable
fun BPlayerTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentChoice: AccentChoice = AccentChoice.WARM_AMBER,
    fontScale: Float = 1.0f,
    isGlassEnabled: Boolean = true,
    dynamicAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    PulseTheme(
        themeMode = themeMode,
        accentChoice = accentChoice,
        fontScale = fontScale,
        isGlassEnabled = isGlassEnabled,
        dynamicAccentColor = dynamicAccentColor,
        content = content
    )
}
