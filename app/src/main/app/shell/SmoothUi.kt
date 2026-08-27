package com.winlator.cmod.app.shell

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Lightweight screen entrance motion.
 *
 * Intentionally uses one short transform animation and no per-item animation,
 * so large Lazy lists/grids do not create hundreds of concurrent animations.
 */
@Composable
internal fun rememberSmoothScreenEnterModifier(): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            1f,
            animationSpec = tween(durationMillis = 140),
        )
    }
    return Modifier.graphicsLayer {
        val p = progress.value
        alpha = 0.97f + (0.03f * p)
        val scale = 0.99f + (0.01f * p)
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Adds the lightweight entrance transform to an existing modifier chain.
 */
@Composable
internal fun Modifier.smoothScreenEnter(): Modifier =
    then(rememberSmoothScreenEnterModifier())


/**
 * Lightweight press feedback. The animation is transform-only, so it does not
 * trigger layout and remains cheap even when used on frequently tapped controls.
 */
@Composable
internal fun Modifier.smoothPress(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "smoothPressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
