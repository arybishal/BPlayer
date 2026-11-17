package com.pulse.player.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyScaleTest {

    @Test
    fun `test font scale multiplier coerces within safe bounds`() {
        val normal = createPulseTypography(1.0f)
        assertEquals(32f, normal.displayLarge.fontSize.value, 0.01f)

        // Lower bound clamp (0.85)
        val clampedMin = createPulseTypography(0.50f)
        assertEquals(32f * 0.85f, clampedMin.displayLarge.fontSize.value, 0.01f)

        // Upper bound clamp (1.40)
        val clampedMax = createPulseTypography(2.00f)
        assertEquals(32f * 1.40f, clampedMax.displayLarge.fontSize.value, 0.01f)
    }

    @Test
    fun `test typography styles maintain proportional hierarchy`() {
        val typography = createPulseTypography(1.0f)
        assertTrue(typography.displayLarge.fontSize.value > typography.titleLarge.fontSize.value)
        assertTrue(typography.titleLarge.fontSize.value > typography.bodyLarge.fontSize.value)
        assertTrue(typography.bodyLarge.fontSize.value > typography.labelSmall.fontSize.value)
    }
}
