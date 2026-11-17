package dev.minimaltv.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks how a row reacts to the focus.
 *
 * These tests protect the rule that a row without the focus always goes back to its
 * normal size: getting that wrong leaves the rows the user already went past looking
 * like the selected one.
 */
class FocusEffectsTest {

    @Test
    fun `a row without the focus goes back to its normal size`() {
        assertEquals(
            FocusEffects.SCALE_UNFOCUSED,
            FocusEffects.scaleFor(focused = false),
            0.0001f,
        )
    }

    @Test
    fun `a row with the focus grows`() {
        assertEquals(
            FocusEffects.SCALE_FOCUSED,
            FocusEffects.scaleFor(focused = true),
            0.0001f,
        )
    }

    @Test
    fun `rows that fill the screen width never grow`() {
        assertEquals(
            FocusEffects.SCALE_UNFOCUSED,
            FocusEffects.scaleFor(focused = true, scale = false),
            0.0001f,
        )
    }

    @Test
    fun `a focused row turns fully opaque and the others dim`() {
        assertEquals(FocusEffects.ALPHA_FOCUSED, FocusEffects.alphaFor(true), 0.0001f)
        assertEquals(FocusEffects.ALPHA_UNFOCUSED, FocusEffects.alphaFor(false), 0.0001f)
    }
}
