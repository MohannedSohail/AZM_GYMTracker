package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.ui.component.AzmBottomNavigation
import com.example.azm_gymtracker.ui.component.AzmEmblem
import com.example.azm_gymtracker.ui.component.AzmNavTab
import com.example.azm_gymtracker.ui.component.AzmPrimaryButton
import com.example.azm_gymtracker.ui.component.AzmTopBar
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmHeroCardGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home / Dashboard Screen (Figma Node #87:249 "الرئيسية — داكن")
 * Features Branded Top Bar, Next Round Hero card, Recent Sessions, and 4-tab Bottom Nav.
 */
@Composable
fun DashboardScreen(
    activeMember: Member?,
    members: List<Member>,
    activeSession: WorkoutSession?,
    recentSessions: List<WorkoutSession>,
    onStartWorkout: () -> Unit,
    onResumeWorkout: (sessionId: Long) -> Unit,
    onTabSelected: (AzmNavTab) -> Unit,
    onSelectRecentSession: (WorkoutSession) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AzmDarkBackground,
        topBar = {
            AzmTopBar(isOffline = true)
        },
        bottomBar = {
            AzmBottomNavigation(
                currentTab = AzmNavTab.HOME,
                onTabSelected = onTabSelected,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Hero Card "جاهز للجولة التالية؟" (Figma Node #87:278)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(278.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(AzmHeroCardGradient)
                        .border(1.dp, AzmBorderSubtle, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                ) {
                    // Geometric plate background watermark
                    AzmEmblem(
                        size = 170.dp,
                        strokeColor = AzmEmeraldGreen.copy(alpha = 0.12f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 10.dp),
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Header text block
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.hero_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = AzmEmeraldGreen,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.hero_title),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = AzmDarkOnSurface,
                                lineHeight = 34.sp,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.hero_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = AzmDarkOnSurfaceMuted,
                            )
                        }

                        // CTA Button
                        if (activeSession != null) {
                            AzmPrimaryButton(
                                text = stringResource(R.string.dashboard_resume_workout),
                                onClick = { onResumeWorkout(activeSession.id) },
                            )
                        } else {
                            AzmPrimaryButton(
                                text = stringResource(R.string.hero_start_button),
                                onClick = onStartWorkout,
                            )
                        }
                    }
                }
            }

            // Recent Sessions Section (Figma Node #87:295)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.recent_sessions_count, recentSessions.size.coerceAtLeast(1)),
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmDarkOnSurfaceMuted,
                    )
                    Text(
                        text = stringResource(R.string.recent_sessions_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AzmDarkOnSurface,
                    )
                }
            }

            // Recent Sessions List
            if (recentSessions.isEmpty()) {
                item {
                    RecentSessionCardMock(
                        dateTitle = "اليوم، 22 أكتوبر",
                        metadata = "42:18 دقيقة • 3 مشاركين • 8,640 كجم",
                        onClick = onStartWorkout,
                    )
                }
                item {
                    RecentSessionCardMock(
                        dateTitle = "الثلاثاء، 20 أكتوبر",
                        metadata = "38:40 دقيقة • 3 مشاركين • 7,920 كجم",
                        onClick = onStartWorkout,
                    )
                }
                item {
                    RecentSessionCardMock(
                        dateTitle = "الأحد، 18 أكتوبر",
                        metadata = "40:12 دقيقة • 3 مشاركين • 7,200 كجم",
                        onClick = onStartWorkout,
                    )
                }
            } else {
                items(recentSessions.take(5), key = { it.id }) { session ->
                    val dateFormat = SimpleDateFormat("EEEE، d MMMM", Locale("ar"))
                    val dateStr = dateFormat.format(Date(session.startedAt))
                    val durationSec = ((session.finishedAt ?: session.startedAt) - session.startedAt) / 1000
                    val minutes = durationSec / 60
                    val seconds = durationSec % 60
                    val durationStr = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    val totalVol = session.exercises.sumOf { ex -> ex.sets.sumOf { (it.weightKg ?: 0f).toDouble() * it.reps } }
                    val volumeStr = String.format(Locale.US, "%,.0f", totalVol)

                    RecentSessionCardMock(
                        dateTitle = dateStr,
                        metadata = "$durationStr دقيقة • $volumeStr كجم",
                        onClick = { onSelectRecentSession(session) },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RecentSessionCardMock(
    dateTitle: String,
    metadata: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AzmDarkSurface)
            .border(1.dp, AzmBorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = AzmDarkOnSurfaceMuted,
            modifier = Modifier.size(20.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dateTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = AzmDarkOnSurfaceMuted,
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AzmDarkBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = AzmEmeraldGreen,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
