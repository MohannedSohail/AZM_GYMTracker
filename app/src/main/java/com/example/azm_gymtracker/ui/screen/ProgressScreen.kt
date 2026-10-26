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

enum class TimePeriod {
    WEEK, MONTH, THREE_MONTHS, ALL
}

/**
 * Progress & Analytics Screen (Figma Node #87:859 "التقدم")
 * Period switcher, 4 stats cards, weight plot over time, and weekly session bars.
 */
@Composable
fun ProgressScreen(
    members: List<Member>,
    activeMember: Member?,
    onSelectMember: (Member) -> Unit,
    onTabSelected: (AzmNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.MONTH) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AzmDarkBackground,
        bottomBar = {
            AzmBottomNavigation(
                currentTab = AzmNavTab.PROGRESS,
                onTabSelected = onTabSelected,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Heading: "تقدمك، جولة بجولة"
            item {
                Text(
                    text = stringResource(R.string.progress_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }

            // Participants Chips Row (Figma Node #87:881)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    members.forEach { member ->
                        val isSelected = activeMember?.id == member.id
                        val memberColor = parseMemberColor(member.colorHex)

                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) AzmDarkSurfaceSelected else AzmDarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) AzmEmeraldGreen else AzmBorderSubtle,
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
                                color = if (isSelected) AzmDarkOnSurface else AzmDarkOnSurfaceMuted,
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
                        .background(AzmDarkSurfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    PeriodOption(
                        label = stringResource(R.string.period_all),
                        isSelected = selectedPeriod == TimePeriod.ALL,
                        onClick = { selectedPeriod = TimePeriod.ALL },
                        modifier = Modifier.weight(1f),
                    )
                    PeriodOption(
                        label = stringResource(R.string.period_3months),
                        isSelected = selectedPeriod == TimePeriod.THREE_MONTHS,
                        onClick = { selectedPeriod = TimePeriod.THREE_MONTHS },
                        modifier = Modifier.weight(1f),
                    )
                    PeriodOption(
                        label = stringResource(R.string.period_month),
                        isSelected = selectedPeriod == TimePeriod.MONTH,
                        onClick = { selectedPeriod = TimePeriod.MONTH },
                        modifier = Modifier.weight(1f),
                    )
                    PeriodOption(
                        label = stringResource(R.string.period_week),
                        isSelected = selectedPeriod == TimePeriod.WEEK,
                        onClick = { selectedPeriod = TimePeriod.WEEK },
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
                            value = "24",
                            label = stringResource(R.string.progress_stat_sets),
                            modifier = Modifier.weight(1f),
                        )
                        ProgressStatCard(
                            value = "8",
                            label = stringResource(R.string.progress_stat_month_sessions),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ProgressStatCard(
                            value = "72.5",
                            label = stringResource(R.string.progress_stat_max_weight),
                            modifier = Modifier.weight(1f),
                        )
                        ProgressStatCard(
                            value = "12,960",
                            label = stringResource(R.string.progress_stat_total_volume),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Line Graph Card: Bench weight over time (Figma Node #87:930)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AzmDarkSurface)
                        .border(1.dp, AzmBorderSubtle, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AzmDarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "+12.5 كجم",
                                style = MaterialTheme.typography.bodySmall,
                                color = AzmEmeraldGreen,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Text(
                            text = stringResource(R.string.progress_bench_history),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AzmDarkOnSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            Text("75", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                            Text("65", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                            Text("55", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                        }

                        Canvas(modifier = Modifier.weight(1f).height(88.dp)) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle horizontal grid lines
                            drawLine(AzmBorderSubtle, Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1f)
                            drawLine(AzmBorderSubtle, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1f)
                            drawLine(AzmBorderSubtle, Offset(0f, h), Offset(w, h), strokeWidth = 1f)

                            // Trend curve points: 60kg -> 65kg -> 67.5kg -> 72.5kg
                            val path = Path().apply {
                                moveTo(0f, h * 0.75f)
                                lineTo(w * 0.33f, h * 0.50f)
                                lineTo(w * 0.66f, h * 0.38f)
                                lineTo(w, h * 0.12f)
                            }
                            drawPath(
                                path = path,
                                color = AzmEmeraldGreen,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // End point circle
                            drawCircle(AzmNeonGreen, radius = 5.dp.toPx(), center = Offset(w, h * 0.12f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date labels under graph
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("1 أكتوبر", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                        Text("8 أكتوبر", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                        Text("15 أكتوبر", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                        Text("22 أكتوبر", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                    }
                }
            }

            // Weekly Session Bars Card (Figma Node #87:964)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AzmDarkSurface)
                        .border(1.dp, AzmBorderSubtle, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.progress_weekly_sessions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AzmDarkOnSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        WeeklyBar(count = 1, barHeight = 16.dp, label = "1–7")
                        WeeklyBar(count = 2, barHeight = 32.dp, label = "8–14")
                        WeeklyBar(count = 2, barHeight = 32.dp, label = "15–21")
                        WeeklyBar(count = 3, barHeight = 48.dp, label = "22–28", isCurrent = true)
                    }
                }
            }

            // Personal Record List Item (Figma Node #87:987)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AzmDarkSurface)
                        .border(1.dp, AzmBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AzmDarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "72.5 كجم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MemberColorGold,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text = "رقم قياسي • بنش برس بالبار",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzmDarkOnSurface,
                        fontWeight = FontWeight.Medium,
                    )
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (isSelected) AzmDarkSurfaceSelected else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AzmEmeraldGreen else AzmDarkOnSurfaceMuted,
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
            .background(AzmDarkSurface)
            .border(1.dp, AzmBorderSubtle, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AzmDarkOnSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AzmDarkOnSurfaceMuted,
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodySmall,
            color = AzmDarkOnSurface,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isCurrent) AzmEmeraldGreen else AzmEmeraldGreen.copy(alpha = 0.5f)),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AzmDarkOnSurfaceMuted,
            fontSize = 10.sp,
        )
    }
}
