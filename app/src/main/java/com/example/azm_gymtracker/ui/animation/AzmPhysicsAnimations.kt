package com.example.azm_gymtracker.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Physics-based spring specs for AZM (عزم)
 */
object AzmPhysicsSpecs {
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val Subtle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}

/**
 * Tactile physics-based spring press effect:
 * Compresses slightly on press, springs back with bouncy physics on release.
 */
fun Modifier.azmPhysicsPress(
    pressedScale: Float = 0.95f,
    onClick: (() -> Unit)? = null,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnim.animateTo(
                targetValue = pressedScale,
                animationSpec = AzmPhysicsSpecs.Snappy,
            )
        } else {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = AzmPhysicsSpecs.Bouncy,
            )
        }
    }

    this
        .scale(scaleAnim.value)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
            } else {
                Modifier
            },
        )
}

/**
 * Physics-based staggered entrance animation for list items:
 * Staggers in with translationY, bouncy scaling, and smooth alpha fade.
 */
fun Modifier.azmPhysicsItemEntrance(
    index: Int = 0,
    delayPerIndexMs: Long = 35L,
    maxStaggerIndex: Int = 8,
): Modifier = composed {
    val effectiveIndex = index.coerceAtMost(maxStaggerIndex)
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (effectiveIndex > 0) {
            delay(effectiveIndex * delayPerIndexMs)
        }
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    val progress = animProgress.value
    this
        .graphicsLayer {
            translationY = (1f - progress) * 60f
            scaleX = 0.88f + (0.12f * progress)
            scaleY = 0.88f + (0.12f * progress)
            alpha = progress.coerceIn(0f, 1f)
        }
}
