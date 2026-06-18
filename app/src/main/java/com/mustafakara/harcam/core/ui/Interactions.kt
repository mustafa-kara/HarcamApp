package com.mustafakara.harcam.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import com.mustafakara.harcam.core.ui.theme.Motion

/**
 * Whether the system "remove animations" / reduced-motion setting is on — design.md §6.
 * When true, callers skip stagger, count-up, scale pulses and chart entrance animations and
 * present the final frame immediately.
 */
@Composable
fun reducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val scale = android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}

/**
 * Animation progress target that collapses to 1f (final frame) under reduced motion — design.md §6.
 * Charts use this so data is readable immediately when animations are disabled.
 */
@Composable
fun entranceProgress(durationMillis: Int = 400, label: String = "entrance"): Float {
    if (reducedMotion()) return 1f
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis, easing = Motion.EnterEasing),
        label = label,
    )
    return progress
}

/**
 * Press-feedback scale — design.md §6 (0.97 on press, restored on release). Respects reduced
 * motion (no pulse). Pair with the same [interactionSource] passed to the clickable.
 */
@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val target = if (pressed && !reducedMotion()) Motion.PRESS_SCALE else 1f
    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(Motion.FAST),
        label = "pressScale",
    )
    return this.scale(scale)
}
