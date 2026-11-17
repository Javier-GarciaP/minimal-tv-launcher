package dev.minimaltv.launcher

import android.view.View

/**
 * Shared focus behaviour for every list in the launcher.
 *
 * The focused row grows and becomes fully opaque; the others stay dimmed. The animation
 * is done with [android.view.ViewPropertyAnimator] so it runs on the render thread and
 * costs nothing extra in memory.
 */
object FocusEffects {

    const val SCALE_FOCUSED = 1.18f
    const val SCALE_UNFOCUSED = 1.0f
    const val ALPHA_FOCUSED = 1.0f
    const val ALPHA_UNFOCUSED = 0.5f
    const val DURATION_MS = 140L

    /**
     * Applies the focus effect to a row.
     *
     * [scale] has to stay off for rows that fill the screen width: growing them would
     * push the text past the screen edges and clip it. The home screen list uses a
     * narrow column, so there the growth works as intended.
     */
    fun apply(view: View, focused: Boolean, scale: Boolean = true) {
        val factor = if (scale) SCALE_FOCUSED else SCALE_UNFOCUSED
        val alpha = if (focused) ALPHA_FOCUSED else ALPHA_UNFOCUSED
        view.animate()
            .scaleX(factor)
            .scaleY(factor)
            .alpha(alpha)
            .setDuration(DURATION_MS)
            .start()
    }

    /** Resets a recycled row to its unfocused look without animating. */
    fun reset(view: View, scale: Boolean = true) {
        view.animate().cancel()
        val factor = if (scale) SCALE_UNFOCUSED else SCALE_UNFOCUSED
        view.scaleX = factor
        view.scaleY = factor
        view.alpha = ALPHA_UNFOCUSED
    }
}
