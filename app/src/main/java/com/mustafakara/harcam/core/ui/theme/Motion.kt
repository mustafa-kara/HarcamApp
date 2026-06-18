package com.mustafakara.harcam.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Motion tokens — design.md §6. Durations in milliseconds; exit = 70% of enter. */
object Motion {
    const val FAST = 150
    const val BASE = 200
    const val SLOW = 300

    /** EaseOutCubic — entering elements. */
    val EnterEasing: Easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)

    /** EaseInCubic — exiting elements. */
    val ExitEasing: Easing = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)

    /** Press feedback scale — design.md §6. */
    const val PRESS_SCALE = 0.97f
}
