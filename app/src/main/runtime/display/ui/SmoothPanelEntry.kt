package com.winlator.cmod.runtime.display.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Lightweight entry transition for runtime panels.
 * Only the panel root animates; lists/cards stay static to avoid per-item animation cost.
 */
@Composable
fun SmoothPanelEntry(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(160, easing = FastOutSlowInEasing)) +
            scaleIn(initialScale = 0.985f, animationSpec = tween(160, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(120, easing = FastOutSlowInEasing)) +
            scaleOut(targetScale = 0.985f, animationSpec = tween(120, easing = FastOutSlowInEasing)),
    ) {
        content()
    }
}
