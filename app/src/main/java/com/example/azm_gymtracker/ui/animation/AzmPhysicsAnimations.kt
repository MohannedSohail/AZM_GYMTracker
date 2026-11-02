package com.example.azm_gymtracker.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
 * Ultra-smooth, non-bouncy animation specs for AZM (عزم)
 */
object AzmPhysicsSpecs {
    val SmoothRelease = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val SnappyPress = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )
    val Subtle = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    // Backward compatibility aliases
    val Bouncy = SmoothRelease
    val Snappy = SnappyPress
}

/**
 * Tactile smooth press effect:
 * Compresses slightly on press, returns cleanly with zero bouncing on release.
 */
fun Modifier.azmPhysicsPress(
    pressedScale: Float = 0.96f,
    onClick: (() -> Unit)? = null,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleAnim = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnim.animateTo(
                targetValue = pressedScale,
                animationSpec = AzmPhysicsSpecs.SnappyPress,
            )
        } else {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = AzmPhysicsSpecs.SmoothRelease,
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
 * Silky staggered entrance animation for list items:
 * Glides in gently with subtle translationY, scale, and smooth alpha fade without bouncing.
 */
fun Modifier.azmPhysicsItemEntrance(
    index: Int = 0,
    delayPerIndexMs: Long = 25L,
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
            animationSpec = tween(
                durationMillis = 280,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    val progress = animProgress.value
    this
        .graphicsLayer {
            translationY = (1f - progress) * 20f
            scaleX = 0.96f + (0.04f * progress)
            scaleY = 0.96f + (0.04f * progress)
            alpha = progress.coerceIn(0f, 1f)
        }
}
