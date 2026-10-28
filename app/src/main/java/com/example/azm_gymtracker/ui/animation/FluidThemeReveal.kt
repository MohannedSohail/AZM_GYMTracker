package com.example.azm_gymtracker.ui.animation

import android.graphics.Bitmap
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Supported Theme Reveal Animation Styles for comparison.
 */
enum class ThemeRevealStyle {
    /**
     * 2. Pure Circular Mask Expansion (قناع الانتشار الدائري المباشر)
     * Starts precisely at the tap coordinate and expands radially as a crisp, perfect circle
     * inspired by modern Android (Material You) and iOS control centers.
     */
    CIRCULAR_MASK,

    /**
     * 1. Liquid Swipe / Fluid Drop (تمدد السائل وتموجات القطرة)
     * Expands with organic fluid inertia, drifting toward the opposite corner with liquid ripples.
     */
    FLUID_DROP
}

/**
 * High-performance Theme Reveal Overlay Canvas.
 * Supports both [ThemeRevealStyle.CIRCULAR_MASK] and [ThemeRevealStyle.FLUID_DROP].
 */
@Composable
fun FluidThemeRevealCanvas(
    bitmap: Bitmap,
    progress: Float,
    origin: Offset?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    style: ThemeRevealStyle = ThemeRevealStyle.CIRCULAR_MASK,
) {
    val layoutDirection = LocalLayoutDirection.current
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Absorb taps during the reveal animation to prevent unintended double triggers
                detectTapGestures { }
            }
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Determine actual origin:
        // Use supplied button coordinate or default to the top corner based on layout direction
        val actualOrigin = origin ?: if (layoutDirection == LayoutDirection.Rtl) {
            Offset(w * 0.15f, h * 0.05f)
        } else {
            Offset(w * 0.85f, h * 0.05f)
        }

        // Calculate maximum radius to fully cover all corners of the screen
        val r1 = hypot(actualOrigin.x, actualOrigin.y)
        val r2 = hypot(w - actualOrigin.x, actualOrigin.y)
        val r3 = hypot(actualOrigin.x, h - actualOrigin.y)
        val r4 = hypot(w - actualOrigin.x, h - actualOrigin.y)
        val maxRadius = maxOf(r1, r2, r3, r4) * 1.05f

        val currentRadius = maxRadius * progress

        when (style) {
            ThemeRevealStyle.CIRCULAR_MASK -> {
                // Pure Geometric Circular Mask centered exactly at the click origin
                val circularPath = Path().apply {
                    addOval(
                        Rect(
                            center = actualOrigin,
                            radius = currentRadius
                        )
                    )
                }

                // Draw old snapshot outside the circle, revealing new theme inside
                clipPath(path = circularPath, clipOp = ClipOp.Difference) {
                    drawImage(
                        image = imageBitmap,
                        dstSize = IntSize(w.roundToInt(), h.roundToInt())
                    )
                }

                // Luminous, ultra-smooth modern ring along the circular boundary
                if (progress in 0.01f..0.98f) {
                    val ringAlpha = ((1f - progress) * 1.25f).coerceIn(0f, 1f)
                    val primaryColor = if (isDark) Color(0xFF3FE779) else Color(0xFF0C8F48)
                    val glowColor = if (isDark) Color(0xFF8CFF68) else Color(0xFF42E879)

                    // Outer soft luminous halo
                    drawCircle(
                        color = glowColor.copy(alpha = 0.28f * ringAlpha),
                        radius = currentRadius + 2.dp.toPx(),
                        center = actualOrigin,
                        style = Stroke(width = 5.dp.toPx())
                    )

                    // Inner crisp accent ring
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.65f * ringAlpha),
                        radius = currentRadius,
                        center = actualOrigin,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            ThemeRevealStyle.FLUID_DROP -> {
                // Direction vector from origin to opposite corner
                val targetX = if (actualOrigin.x < w / 2f) w else 0f
                val targetY = if (actualOrigin.y < h / 2f) h else 0f
                val dx = targetX - actualOrigin.x
                val dy = targetY - actualOrigin.y
                val distToOpposite = hypot(dx, dy)
                val dirX = if (distToOpposite > 0f) dx / distToOpposite else 0f
                val dirY = if (distToOpposite > 0f) dy / distToOpposite else 1f

                // Fluid liquid dynamics: Center drifts along flow direction
                val fluidFactor = sin(progress * Math.PI.toFloat())
                val fluidDrift = currentRadius * 0.10f * fluidFactor
                val fluidCenter = Offset(
                    x = actualOrigin.x + dirX * fluidDrift,
                    y = actualOrigin.y + dirY * fluidDrift,
                )

                val fluidPath = Path().apply {
                    val radiusX = currentRadius * (1f + 0.04f * fluidFactor)
                    val radiusY = currentRadius * (1f + 0.07f * fluidFactor)
                    addOval(
                        Rect(
                            center = fluidCenter,
                            radius = maxOf(radiusX, radiusY)
                        )
                    )
                }

                clipPath(path = fluidPath, clipOp = ClipOp.Difference) {
                    drawImage(
                        image = imageBitmap,
                        dstSize = IntSize(w.roundToInt(), h.roundToInt())
                    )
                }

                // Glowing liquid crest & trailing ripple along wave boundary
                if (progress in 0.01f..0.98f) {
                    val crestAlpha = ((1f - progress) * 1.30f).coerceIn(0f, 1f)
                    val primaryGlow = if (isDark) Color(0xFF3FE779) else Color(0xFF0C8F48)
                    val secondaryGlow = if (isDark) Color(0xFF8CFF68) else Color(0xFF42E879)

                    // Primary glowing crest ring
                    drawCircle(
                        color = primaryGlow.copy(alpha = 0.70f * crestAlpha),
                        radius = currentRadius,
                        center = fluidCenter,
                        style = Stroke(width = 5.dp.toPx())
                    )

                    // Outer soft dispersion glow
                    drawCircle(
                        color = secondaryGlow.copy(alpha = 0.35f * crestAlpha),
                        radius = currentRadius + 3.dp.toPx(),
                        center = fluidCenter,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Trailing fluid drop ripple
                    if (currentRadius > 28.dp.toPx()) {
                        val trailingRadius = (currentRadius - 16.dp.toPx()).coerceAtLeast(0f)
                        drawCircle(
                            color = primaryGlow.copy(alpha = 0.22f * crestAlpha),
                            radius = trailingRadius,
                            center = fluidCenter,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
