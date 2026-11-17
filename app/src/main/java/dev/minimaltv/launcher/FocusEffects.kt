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
     * The size a row has with the given focus state.
     *
     * A row that is not focused always goes back to its normal size, and a row that
     * fills the screen width never grows: growing it would push the text past the
     * screen edges and clip it. The home screen list uses a narrow column, so there the
     * growth works as intended.
     */
    fun scaleFor(focused: Boolean, scale: Boolean = true): Float =
        if (focused && scale) SCALE_FOCUSED else SCALE_UNFOCUSED

    /** The opacity a row has with the given focus state. */
    fun alphaFor(focused: Boolean): Float =
        if (focused) ALPHA_FOCUSED else ALPHA_UNFOCUSED

    /** Animates a row into the state its focus says it should be in. */
    fun apply(view: View, focused: Boolean, scale: Boolean = true) {
        view.animate()
            .scaleX(scaleFor(focused, scale))
            .scaleY(scaleFor(focused, scale))
            .alpha(alphaFor(focused))
            .setDuration(DURATION_MS)
            .start()
    }

    /**
     * Puts a row in the state its focus flag says it should be in, without animating.
     *
     * Rebinding a row does not fire the focus listener, so a row that keeps the focus
     * while it is rebound would stay in the unfocused look: it would show the
     * selection box without growing.
     */
    fun sync(view: View, scale: Boolean = true) {
        val focused = view.hasFocus()
        view.animate().cancel()
        view.scaleX = scaleFor(focused, scale)
        view.scaleY = scaleFor(focused, scale)
        view.alpha = alphaFor(focused)
    }

    /** Resets a recycled row to its unfocused look without animating. */
    fun reset(view: View, scale: Boolean = true) {
        view.animate().cancel()
        view.scaleX = scaleFor(false, scale)
        view.scaleY = scaleFor(false, scale)
        view.alpha = alphaFor(false)
    }
}
