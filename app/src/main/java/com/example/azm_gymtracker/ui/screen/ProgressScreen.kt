package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.ui.component.AzmBottomNavigation
import com.example.azm_gymtracker.ui.component.AzmNavTab
import com.example.azm_gymtracker.ui.component.ParticipantAvatarBox
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceSelected
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceVariant
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmNeonGreen
import com.example.azm_gymtracker.ui.theme.MemberColorGold

import com.example.azm_gymtracker.presentation.progress.ProgressPeriod
import com.example.azm_gymtracker.presentation.progress.ProgressUiState

/**
 * Progress & Analytics Screen (Figma Node #87:859 "التقدم")
 * Period switcher, 4 stats cards, weight plot over time, and weekly session bars.
 */
@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    onSelectMember: (Member) -> Unit,
    onPeriodSelected: (ProgressPeriod) -> Unit,
    onTabSelected: (AzmNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AzmBottomNavigation(
                currentTab = AzmNavTab.PROGRESS,
                onTabSelected = onTabSelected,
            )
        },
    ) { innerPadding ->
        val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
        val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Heading: "تقدمك، جولة بجولة"
            item {
                Text(
                    text = stringResource(R.string.progress_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // Participants Chips Row (Figma Node #87:881)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                ) {
                    uiState.members.forEach { member ->
                        val isSelected = uiState.activeMember?.id == member.id
                        val memberColor = parseMemberColor(member.colorHex)

                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    if (isSelected) accent else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clickable { onSelectMember(member) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = if (isSelected) "${member.name} ✓" else member.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
                            )
                            ParticipantAvatarBox(
                                character = member.name,
                                backgroundColor = memberColor,
                                size = 26.dp,
                                fontSize = 14,
                            )
                        }
                    }
                }
            }

            // Period Selector (Pill bar: أسبوع, شهر, 3 أشهر, الكل)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PeriodOption(
                        label = stringResource(R.string.period_week),
                        isSelected = uiState.selectedPeriod == ProgressPeriod.WEEK,
                        onClick = { onPeriodSelected(ProgressPeriod.WEEK) },
                        modifier = Modifier.weight(1f),
                    )
                    PeriodOption(
                        label = stringResource(R.string.period_month),
                        isSelected = uiState.selectedPeriod == ProgressPeriod.MONTH,
                        onClick = { onPeriodSelected(ProgressPeriod.MONTH) },
                        modifier = Modifier.weight(1f),
                    )
                    PeriodOption(
                        label = stringResource(R.string.period_3months),
                        isSelected = uiState.selectedPeriod == ProgressPeriod.THREE_MONTHS,
                        onClick = { onPeriodSelected(ProgressPeriod.THREE_MONTHS) },
                        modifier = Modifier.weight(1f),
                    )
                    PeriodOption(
                        label = stringResource(R.string.period_all),
                        isSelected = uiState.selectedPeriod == ProgressPeriod.ALL,
                        onClick = { onPeriodSelected(ProgressPeriod.ALL) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 4 Stats Cards Grid (2x2)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ProgressStatCard(
                            value = "${uiState.sessionsCount}",
                            label = stringResource(R.string.progress_stat_month_sessions),
                            modifier = Modifier.weight(1f),
                        )
                        ProgressStatCard(
                            value = "${uiState.setsCount}",
                            label = stringResource(R.string.progress_stat_sets),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val maxWeightStr = if (uiState.maxWeightKg % 1f == 0f) {
                            "${uiState.maxWeightKg.toInt()}"
                        } else {
                            "${uiState.maxWeightKg}"
                        }
                        val volumeFormatted = String.format(java.util.Locale.US, "%,d", uiState.totalVolumeKg.toInt())

                        ProgressStatCard(
                            value = volumeFormatted,
                            label = stringResource(R.string.progress_stat_total_volume),
                            modifier = Modifier.weight(1f),
                        )
                        ProgressStatCard(
                            value = maxWeightStr,
                            label = stringResource(R.string.progress_stat_max_weight),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Line Graph Card: Weight Progression over time (Figma Node #87:930)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.progress_bench_history),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        val deltaSign = if (uiState.weightDeltaKg >= 0) "+" else ""
                        val deltaVal = if (uiState.weightDeltaKg % 1f == 0f) {
                            uiState.weightDeltaKg.toInt().toString()
                        } else {
                            uiState.weightDeltaKg.toString()
                        }
                        val deltaText = if (uiState.weightDeltaKg != 0f) "$deltaSign$deltaVal kg" else "—"

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = deltaText,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (uiState.weightDeltaKg >= 0) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val points = uiState.weightProgressionPoints
                    val minWeight = if (points.isNotEmpty()) points.minOf { it.weightKg } else 0f
                    val maxWeight = if (points.isNotEmpty()) points.maxOf { it.weightKg } else 0f
                    val weightSpan = if (maxWeight > minWeight) maxWeight - minWeight else 1f
                    val gridColor = MaterialTheme.colorScheme.outline

                    // Line Graph Canvas
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Numeric weight scale labels
                        Column(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = if (maxWeight > 0) "${maxWeight.toInt()}" else "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (maxWeight > minWeight) "${((maxWeight + minWeight) / 2).toInt()}" else "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = if (minWeight > 0) "${minWeight.toInt()}" else "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Canvas(modifier = Modifier.weight(1f).height(88.dp)) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle horizontal grid lines
                            drawLine(gridColor, Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                            drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
                            drawLine(gridColor, Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                            if (points.size >= 2) {
                                val path = Path()
                                points.forEachIndexed { index, pt ->
                                    val x = (index.toFloat() / (points.size - 1)) * w
                                    val normalizedY = (pt.weightKg - minWeight) / weightSpan
                                    val y = h - (normalizedY * (h * 0.8f) + h * 0.1f)
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = accent,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                                )

                                val lastPt = points.last()
                                val lastNormalizedY = (lastPt.weightKg - minWeight) / weightSpan
                                val lastY = h - (lastNormalizedY * (h * 0.8f) + h * 0.1f)
                                drawCircle(if (isDark) AzmNeonGreen else accent, radius = 5.dp.toPx(), center = Offset(w, lastY))
                            } else if (points.size == 1) {
                                val y = h / 2f
                                drawCircle(if (isDark) AzmNeonGreen else accent, radius = 5.dp.toPx(), center = Offset(w / 2f, y))
                            }
                        }
                    }
                }
            }

            // Weekly Session Bars Card (Figma Node #87:964)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.progress_weekly_sessions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val maxSessionDay = (uiState.weeklySessionsCount.maxOrNull() ?: 1).coerceAtLeast(1)
                    val daysLabels = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")

                    // Bar Chart Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        uiState.weeklySessionsCount.take(7).forEachIndexed { index, count ->
                            val heightFraction = (count.toFloat() / maxSessionDay).coerceIn(0.15f, 1f)
                            val barHeightDp = (heightFraction * 48).dp
                            val label = daysLabels.getOrElse(index) { "$index" }
                            WeeklyBar(
                                count = count,
                                barHeight = barHeightDp,
                                label = label,
                                isCurrent = index == 6,
                            )
                        }
                    }
                }
            }

            // Personal Record List Item (Figma Node #87:987)
            if (uiState.maxWeightKg > 0f) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${uiState.activeMember?.name ?: ""} • أعلى وزن مسجل",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "${uiState.maxWeightKg} كجم",
                                style = MaterialTheme.typography.bodySmall,
                                color = MemberColorGold,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PeriodOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
    val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeeklyBar(
    count: Int,
    barHeight: androidx.compose.ui.unit.Dp,
    label: String,
    isCurrent: Boolean = false,
) {
    val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
    val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isCurrent) accent else accent.copy(alpha = 0.5f)),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )
    }
}
