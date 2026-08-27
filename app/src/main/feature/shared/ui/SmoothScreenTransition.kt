package com.winlator.cmod.shared.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Lightweight screen-level motion.
 * One animation per screen, never per list/grid item.
 */
@Composable
fun SmoothScreenTransition(
    visible: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(180)) + scaleIn(
            initialScale = 0.985f,
            animationSpec = tween(180),
        ),
        exit = fadeOut(tween(120)),
        content = { content() },
    )
}
