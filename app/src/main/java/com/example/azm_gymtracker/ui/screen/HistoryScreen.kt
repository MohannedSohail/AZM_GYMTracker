package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.presentation.history.HistoryUiState
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
import com.example.azm_gymtracker.ui.theme.AzmError
import com.example.azm_gymtracker.ui.theme.MemberColorGold
import com.example.azm_gymtracker.ui.theme.MemberColorIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Workout History Screen (Figma Node #87:745 "سجل التمارين — داكن")
 * Features participant filter chips, expandable workout cards with detailed set tables,
 * and 4-tab bottom navigation.
 */
@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onSelectSession: (WorkoutSession?) -> Unit,
    onDeleteSession: (sessionId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    onTabSelected: (AzmNavTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var sessionToDelete by remember { mutableStateOf<Long?>(null) }
    var isExpandedFirstSession by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AzmDarkBackground,
        bottomBar = {
            AzmBottomNavigation(
                currentTab = AzmNavTab.HISTORY,
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
            // Heading: "سجل التمارين" + "كل جولة محفوظة، لكل مشارك."
            item {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = AzmDarkOnSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.history_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzmDarkOnSurfaceMuted,
                    )
                }
            }

            // Participants filter chips (Figma Node #87:768)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    HistoryParticipantChip(name = "سلمان ✓", char = "س", color = MemberColorIndigo)
                    HistoryParticipantChip(name = "محمد ✓", char = "م", color = MemberColorGold)
                    HistoryParticipantChip(name = "أحمد ✓", char = "أ", color = AzmEmeraldGreen)
                }
            }

            // Expanded Session Card (Figma Node #87:785)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AzmDarkSurface)
                        .border(1.dp, AzmBorderSubtle, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                ) {
                    // Header row with toggle arrow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpandedFirstSession = !isExpandedFirstSession },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (isExpandedFirstSession) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AzmDarkOnSurfaceMuted,
                        )
                        Text(
                            text = "اليوم، 22 أكتوبر",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AzmDarkOnSurface,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "42:18 دقيقة • 3 تمارين • 18 جولة",
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmDarkOnSurfaceMuted,
                        modifier = Modifier.align(Alignment.End),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Volume & Participants Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "3 مشاركين",
                                style = MaterialTheme.typography.bodySmall,
                                color = AzmEmeraldGreen,
                            )
                        }

                        Text(
                            text = "8,640 كجم",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AzmEmeraldGreen,
                        )
                    }

                    if (isExpandedFirstSession) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AzmBorderSubtle),
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Exercise Section Title
                        Text(
                            text = "بنش برس بالبار مستوي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AzmDarkOnSurface,
                            modifier = Modifier.align(Alignment.End),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Table Column Headings (المشارك | الجولة | كجم | التكرارات)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("المشارك", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted, modifier = Modifier.weight(1f))
                            Text("الجولة", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted, modifier = Modifier.weight(1f))
                            Text("كجم", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted, modifier = Modifier.weight(1f))
                            Text("التكرارات", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Set Rows in Table
                        SavedRoundRow(participant = "أحمد", round = "1", kg = "72.5", reps = "8")
                        SavedRoundRow(participant = "محمد", round = "1", kg = "60", reps = "8")
                        SavedRoundRow(participant = "سلمان", round = "1", kg = "52.5", reps = "8")

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "عرض 3 من 18 جولة • عرض كل التفاصيل",
                            style = MaterialTheme.typography.bodySmall,
                            color = AzmDarkOnSurfaceMuted,
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            }

            // Previous Sessions Cards
            item {
                HistoryCompactSessionCard(
                    date = "الثلاثاء، 20 أكتوبر",
                    metadata = "38:40 دقيقة • 3 مشاركين • 7,920 كجم",
                )
            }

            item {
                HistoryCompactSessionCard(
                    date = "الأحد، 18 أكتوبر",
                    metadata = "40:12 دقيقة • 7,200 كجم • السجل محفوظ",
                    badge = "خالد • مؤرشف",
                )
            }

            // If empty
            if (uiState.sessions.isEmpty() && !uiState.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.history_empty),
                            style = MaterialTheme.typography.titleLarge,
                            color = AzmDarkOnSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.history_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AzmDarkOnSurfaceMuted,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Session Dialog
    sessionToDelete?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            containerColor = AzmDarkSurface,
            title = {
                Text(
                    text = stringResource(R.string.history_delete_dialog_title),
                    color = AzmDarkOnSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.history_delete_dialog_msg),
                    color = AzmDarkOnSurfaceMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSession(sessionId)
                        sessionToDelete = null
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = AzmError,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurface)
                }
            },
        )
    }
}

@Composable
private fun HistoryParticipantChip(
    name: String,
    char: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(AzmDarkSurfaceSelected)
            .border(1.dp, AzmEmeraldGreen, CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AzmDarkOnSurface,
        )
        ParticipantAvatarBox(
            character = char,
            backgroundColor = color,
            size = 26.dp,
            fontSize = 14,
        )
    }
}

@Composable
private fun SavedRoundRow(
    participant: String,
    round: String,
    kg: String,
    reps: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AzmDarkSurfaceVariant)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(participant, style = MaterialTheme.typography.bodyMedium, color = AzmDarkOnSurface, modifier = Modifier.weight(1f))
        Text(round, style = MaterialTheme.typography.bodyMedium, color = AzmDarkOnSurface, modifier = Modifier.weight(1f))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(kg, style = MaterialTheme.typography.bodyMedium, color = AzmDarkOnSurface, modifier = Modifier.weight(1f))
            Text(reps, style = MaterialTheme.typography.bodyMedium, color = AzmDarkOnSurface, modifier = Modifier.weight(1f))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun HistoryCompactSessionCard(
    date: String,
    metadata: String,
    badge: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AzmDarkSurface)
            .border(1.dp, AzmBorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = AzmDarkOnSurfaceMuted,
            modifier = Modifier.size(18.dp),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AzmDarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.bodySmall,
                                color = AzmDarkOnSurfaceMuted,
                                fontSize = 10.sp,
                            )
                        }
                    }
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AzmDarkOnSurface,
                    )
                }
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
