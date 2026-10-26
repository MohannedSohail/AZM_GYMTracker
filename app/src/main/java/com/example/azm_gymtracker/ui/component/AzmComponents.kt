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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
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

/**
 * Geometric round emblem of AZM (عزم — رمز الجولة الهندسي)
 * Continuous angled ribbon loop inspired by the Arabic letter Ayn 'ع'.
 */
@Composable
fun AzmEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    strokeColor: Color = AzmEmeraldGreen,
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = this.size.minDimension * 0.12f
        val radius = (this.size.minDimension - strokeWidth) / 2f
        val centerOffset = center

        // Outer atmospheric orbit ring
        drawCircle(
            color = strokeColor.copy(alpha = 0.25f),
            radius = radius,
            center = centerOffset,
            style = Stroke(width = strokeWidth * 0.6f)
        )

        // Geometric dynamic arc track
        drawArc(
            color = strokeColor,
            startAngle = -45f,
            sweepAngle = 230f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Inner core loop point
        drawCircle(
            color = AzmNeonGreen,
            radius = strokeWidth * 0.75f,
            center = centerOffset
        )
    }
}

/**
 * Branded Top Bar (Figma Node #87:264)
 * Contains the AZM Wordmark + Emblem on one side, and "بدون إنترنت" status pill badge on the other.
 */
@Composable
fun AzmTopBar(
    modifier: Modifier = Modifier,
    isOffline: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status Badge: "بدون إنترنت" (Pill 999dp, #10271C background, #3FE779 text)
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(AzmDarkSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isOffline) stringResource(R.string.status_offline) else "",
                style = MaterialTheme.typography.bodySmall,
                color = AzmEmeraldGreen,
                fontWeight = FontWeight.Medium,
            )
        }

        // AZM Wordmark + Geometric Emblem
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "عزم",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
                Text(
                    text = "AZM",
                    style = MaterialTheme.typography.bodySmall,
                    color = AzmDarkOnSurfaceMuted,
                    letterSpacing = 1.sp,
                )
            }
            AzmEmblem(size = 40.dp)
        }
    }
}

/**
 * 4-Destination Bottom Navigation Bar (Figma Node #87:327)
 * Tabs: الرئيسية (Home), التمارين (Exercises), السجل (History), التقدم (Progress)
 * Selected destination: container #153E27, rounded 16dp, text & icon in #3FE779
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(AzmDarkSurface)
            .border(width = 1.dp, color = AzmBorderSubtle, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tab 4: التقدم (Progress)
        AzmNavItem(
            title = stringResource(R.string.nav_progress),
            icon = Icons.Default.ShowChart,
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
            .background(AzmDarkSurfaceSelected)
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
            tint = if (isSelected) AzmEmeraldGreen else AzmDarkOnSurfaceMuted,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AzmEmeraldGreen else AzmDarkOnSurfaceMuted,
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
            .clip(CircleShape)
            .background(if (enabled) AzmPrimaryGradient else AzmPrimaryGradient)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AzmDarkBackground.copy(alpha = opacity),
        )
    }
}

/**
 * Secondary Button (Figma: Height 56dp, Pill 999dp, #10271C, border #284237, text 16sp Bold #F4FFF5)
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
            .background(AzmDarkSurfaceVariant)
            .border(width = 1.dp, color = AzmBorderSubtle, shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AzmDarkOnSurface,
        )
    }
}
