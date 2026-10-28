package com.example.azm_gymtracker.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmNeonGreen
import com.example.azm_gymtracker.ui.theme.AzmPrimaryGradient
import com.example.azm_gymtracker.ui.theme.LocalThemeIsDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Splash Screen for AZM (عزم — سجّل جولتك)
 * Features official folded Ayn geometric logo from Figma, layered breathing neon aura,
 * and authentic Thmanyah typography with smooth spring entrances.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val currentOnSplashFinished by rememberUpdatedState(onSplashFinished)

    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(20f) }
    val badgeAlpha = remember { Animatable(0f) }

    // Pulsing halo animation
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseScale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    val pulseAlpha = infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    LaunchedEffect(Unit) {
        // Step 1: Logo spring entrance & background glow
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(600, easing = LinearEasing))
        }

        // Step 2: Text reveal after 300ms
        delay(300)
        launch {
            textAlpha.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        }
        launch {
            textOffsetY.animateTo(0f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        }

        // Step 3: Badge reveal after 600ms
        delay(300)
        launch {
            badgeAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }

        // Wait and proceed smoothly
        delay(1300)
        currentOnSplashFinished()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            // Atmospheric Radial Glow in the background
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .scale(pulseScale.value)
                    .alpha(pulseAlpha.value)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isDark) AzmNeonGreen else MaterialTheme.colorScheme.primary).copy(alpha = if (isDark) 0.35f else 0.18f),
                                (if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primaryContainer).copy(alpha = if (isDark) 0.15f else 0.08f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val rippleColor = if (isDark) AzmNeonGreen else primaryColor

                // Animated Glowing Emblem with the official Figma vector
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value),
                ) {
                    // Pulsing outer ripple ring
                    Canvas(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale.value)
                            .alpha(pulseAlpha.value),
                    ) {
                        drawCircle(
                            color = rippleColor,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }

                    // Main Emblem Container
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(32.dp),
                                spotColor = if (isDark) AzmNeonGreen.copy(alpha = 0.35f) else Color(0x18000000),
                            )
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = "شعار عزم",
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(24.dp)),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Brand Typography: عزم + AZM GYM TRACKER + Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(textAlpha.value)
                        .offset(y = textOffsetY.value.dp),
                ) {
                    Text(
                        text = "عـــزم",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "AZM GYM TRACKER",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) AzmNeonGreen else MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "هاتف واحد. عزم يجمعكم.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Focus Pill Badge at bottom
                Box(
                    modifier = Modifier
                        .alpha(badgeAlpha.value)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isDark) AzmNeonGreen else MaterialTheme.colorScheme.primary),
                        )
                        Text(
                            text = "سجّل جولاتك بدقة • تركيز كامل",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
