package com.example.azm_gymtracker.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.graphicsLayer
import com.example.azm_gymtracker.ui.theme.LocalThemeIsDark
import com.example.azm_gymtracker.ui.theme.LocalThemeToggle
import com.example.azm_gymtracker.ui.animation.azmPhysicsPress
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceSelected
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceVariant
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmNeonGreen
import com.example.azm_gymtracker.ui.theme.AzmPrimaryGradient

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource

/**
 * Geometric folded Ayn emblem of AZM (عزم — رمز الجولة الهندسي)
 * Official brand mark from Figma.
 */
@Composable
fun AzmEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeColor: Color = AzmNeonGreen,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.25f))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(size * 0.25f)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "شعار عزم",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.85f)
                .clip(RoundedCornerShape(size * 0.2f)),
        )
    }
}

/**
 * Branded Top Bar (Figma Node #87:264)
 * Contains the AZM Wordmark + Emblem on one side, and "بدون إنترنت" status pill badge on the other.
 * Includes statusBarsPadding to stay safely below notch and system icons.
 */
@Composable
fun AzmTopBar(
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val toggleTheme = LocalThemeToggle.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // AZM Wordmark + Geometric Emblem (Start side / Right in RTL)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AzmEmblem(size = 42.dp)
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "عزم",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "AZM GYM TRACKER",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                )
            }
        }

        // Actions Row: Theme Toggle Button with beautiful physics animation
        val iconRotation by animateFloatAsState(
            targetValue = if (isDark) 360f else 0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            label = "themeIconRotation",
        )
        var buttonCenter by remember { mutableStateOf<Offset?>(null) }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .onGloballyPositioned { coordinates ->
                    val pos = coordinates.positionInRoot()
                    buttonCenter = Offset(
                        pos.x + coordinates.size.width / 2f,
                        pos.y + coordinates.size.height / 2f,
                    )
                }
                .azmPhysicsPress(pressedScale = 0.88f) { toggleTheme(buttonCenter) },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = isDark,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.3f) + slideInVertically { it / 2 })
                        .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.3f) + slideOutVertically { -it / 2 })
                },
                label = "themeIconContent",
            ) { dark ->
                Icon(
                    imageVector = if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (dark) "الوضع الفاتح" else "الوضع الداكن",
                    tint = if (dark) Color(0xFFFFD54F) else AzmEmeraldGreen,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = iconRotation },
                )
            }
        }
    }
}

/**
 * 4-Destination Bottom Navigation Bar (Figma Node #87:327)
 * Tabs: الرئيسية (Home), التمارين (Exercises), السجل (History), التقدم (Progress)
 * Selected destination: container #153E27, rounded 16dp, text & icon in #3FE779
 * Includes navigationBarsPadding to stay safely above Android navigation and gesture bar.
 */
enum class AzmNavTab {
    HOME, EXERCISES, HISTORY, PROGRESS
}

@Composable
fun AzmBottomNavigation(
    currentTab: AzmNavTab,
    onTabSelected: (AzmNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Tab 4: التقدم (Progress)
            AzmNavItem(
                title = stringResource(R.string.nav_progress),
                icon = Icons.AutoMirrored.Filled.ShowChart,
                isSelected = currentTab == AzmNavTab.PROGRESS,
                onClick = { onTabSelected(AzmNavTab.PROGRESS) },
                modifier = Modifier.weight(1f),
            )

            // Tab 3: السجل (History)
            AzmNavItem(
                title = stringResource(R.string.nav_history),
                icon = Icons.Default.DateRange,
                isSelected = currentTab == AzmNavTab.HISTORY,
                onClick = { onTabSelected(AzmNavTab.HISTORY) },
                modifier = Modifier.weight(1f),
            )

            // Tab 2: التمارين (Exercises)
            AzmNavItem(
                title = stringResource(R.string.nav_exercises),
                icon = Icons.Default.FitnessCenter,
                isSelected = currentTab == AzmNavTab.EXERCISES,
                onClick = { onTabSelected(AzmNavTab.EXERCISES) },
                modifier = Modifier.weight(1f),
            )

            // Tab 1: الرئيسية (Home)
            AzmNavItem(
                title = stringResource(R.string.nav_dashboard),
                icon = Icons.Default.Home,
                isSelected = currentTab == AzmNavTab.HOME,
                onClick = { onTabSelected(AzmNavTab.HOME) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AzmNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundModifier = if (isSelected) {
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    } else {
        Modifier.clip(RoundedCornerShape(16.dp))
    }

    Column(
        modifier = modifier
            .height(54.dp)
            .then(backgroundModifier)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(22.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
        )
    }
}

/**
 * Participant Avatar Box (Figma Node #87:391)
 * 48x48dp with 16dp rounded corners, background colored with member token, letter in bold 24sp #06110D.
 */
@Composable
fun ParticipantAvatarBox(
    character: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: Int = 24,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = character.take(1),
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize.sp),
            fontWeight = FontWeight.Bold,
            color = AzmDarkBackground,
        )
    }
}

/**
 * Primary CTA Button (Figma: Height 56dp, Pill 999dp, AzmPrimaryGradient, text 16sp Bold #06110D)
 */
@Composable
fun AzmPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val opacity = if (enabled) 1f else 0.4f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer { alpha = opacity }
            .clip(CircleShape)
            .background(AzmPrimaryGradient)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AzmDarkBackground,
        )
    }
}

/**
 * Secondary Button (Figma: Height 56dp, Pill 999dp, surfaceVariant, border outline, text onSurface)
 */
@Composable
fun AzmSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
