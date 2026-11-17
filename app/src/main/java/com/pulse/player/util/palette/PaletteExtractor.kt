package com.pulse.player.util.palette

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.pulse.player.ui.theme.AccentAmber
import com.pulse.player.ui.theme.DarkBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PaletteExtractor {

    suspend fun extractAccentFromArtwork(
        context: Context,
        artworkUri: String?
    ): Color = withContext(Dispatchers.IO) {
        if (artworkUri.isNullOrEmpty()) {
            return@withContext AccentAmber
        }

        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(artworkUri)
                .allowHardware(false) // Required for Palette software bitmap access
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap ?: return@withContext AccentAmber

            val palette = Palette.from(bitmap).generate()
            val candidateSwatch = palette.vibrantSwatch
                ?: palette.mutedSwatch
                ?: palette.dominantSwatch

            val candidateColor = candidateSwatch?.rgb?.let { Color(it) } ?: AccentAmber

            // Enforce WCAG AA contrast ratio against dark background (min 4.5:1)
            ensureContrast(candidateColor, DarkBackground)
        } catch (_: Exception) {
            AccentAmber
        }
    }

    private fun ensureContrast(foreground: Color, background: Color, targetContrast: Double = 4.5): Color {
        val fgArgb = foreground.toArgb()
        val bgArgb = background.toArgb()

        var currentContrast = ColorUtils.calculateContrast(fgArgb, bgArgb)
        if (currentContrast >= targetContrast) {
            return foreground
        }

        // Adjust lightness in HSL space until contrast threshold is achieved
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(fgArgb, hsl)

        var attempts = 0
        while (currentContrast < targetContrast && attempts < 15) {
            hsl[2] = (hsl[2] + 0.05f).coerceAtMost(0.92f) // Lighten
            val adjustedArgb = ColorUtils.HSLToColor(hsl)
            currentContrast = ColorUtils.calculateContrast(adjustedArgb, bgArgb)
            attempts++
        }

        return Color(ColorUtils.HSLToColor(hsl))
    }
}
