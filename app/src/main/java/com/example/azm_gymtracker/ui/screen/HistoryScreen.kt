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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.azm_gymtracker.ui.animation.azmPhysicsItemEntrance
import com.example.azm_gymtracker.ui.animation.azmPhysicsPress
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
    onSelectMemberFilter: (Long?) -> Unit = {},
    onToggleSessionExpanded: (Long) -> Unit = {},
    onSelectSession: (WorkoutSession?) -> Unit = {},
    onDeleteSession: (sessionId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    onTabSelected: (AzmNavTab) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var sessionToDelete by remember { mutableStateOf<Long?>(null) }
    val dateFormat = remember { SimpleDateFormat("EEEE، d MMMM", Locale.forLanguageTag("ar")) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AzmBottomNavigation(
                currentTab = AzmNavTab.HISTORY,
                onTabSelected = onTabSelected,
            )
        },
    ) { innerPadding ->
        val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Heading: "سجل التمارين" + "كل جولة محفوظة، لكل مشارك."
            item {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.history_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Participants filter chips (Dynamic from uiState.members)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                ) {
                    HistoryParticipantChip(
                        name = stringResource(R.string.picker_category_all),
                        char = "★",
                        color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                        isSelected = uiState.selectedMemberId == null,
                        onClick = { onSelectMemberFilter(null) },
                    )

                    uiState.members.forEach { member ->
                        val isSelected = uiState.selectedMemberId == member.id
                        HistoryParticipantChip(
                            name = member.name,
                            char = member.name.take(1),
                            color = parseMemberColor(member.colorHex),
                            isSelected = isSelected,
                            onClick = {
                                onSelectMemberFilter(if (isSelected) null else member.id)
                            },
                        )
                    }
                }
            }

            // Loading State
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Dynamic Session Cards with Physics Entrance
            itemsIndexed(uiState.sessionsWithDetails, key = { _, it -> it.session.id }) { index, item ->
                val session = item.session
                val participants = item.participants
                val exercises = item.exercises
                val isExpanded = uiState.expandedSessionIds.contains(session.id)

                val durationMinutes = if (session.endedAt != null) {
                    maxOf(1, ((session.endedAt - session.startedAt - session.pausedTotalMs) / 60000).toInt())
                } else {
                    0
                }

                val totalSets = exercises.sumOf { it.sets.size }
                val totalVolume = exercises.sumOf { ex ->
                    ex.sets.sumOf { s -> (s.weightKg ?: 0f).toDouble() * s.reps }
                }

                val dateText = remember(session.startedAt) {
                    try {
                        dateFormat.format(Date(session.startedAt))
                    } catch (e: Exception) {
                        ""
                    }
                }

                val volumeFormatted = String.format(Locale.US, "%,d", totalVolume.toInt())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .azmPhysicsItemEntrance(index = index)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                ) {
                    // Header row with date (Start) & toggle arrow / delete (End)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSessionExpanded(session.id) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            androidx.compose.material3.IconButton(
                                onClick = { sessionToDelete = session.id },
                                modifier = Modifier.size(28.dp),
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.action_delete),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$durationMinutes دقيقة • ${exercises.size} تمارين • $totalSets جولة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Volume (Start) & Participants (End) Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "$volumeFormatted kg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "${participants.size} مشاركين",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outline),
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        exercises.forEach { exWithSets ->
                            val exName = exWithSets.catalogExercise.nameAr.ifBlank {
                                exWithSets.workoutExercise.exerciseNameSnapshot
                            }

                            Text(
                                text = exName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.align(Alignment.Start),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Column Headings (المشارك | الجولة | كجم | التكرارات)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("المشارك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text("الجولة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text("كجم", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                Text("التكرارات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            exWithSets.sets.forEach { set ->
                                val pName = participants.firstOrNull { it.id == set.memberId }?.name ?: ""
                                val kgStr = if (set.weightKg != null) {
                                    if (set.weightKg % 1f == 0f) "${set.weightKg.toInt()}" else "${set.weightKg}"
                                } else "-"

                                SavedRoundRow(
                                    participant = pName,
                                    round = "${set.setNumber}",
                                    kg = kgStr,
                                    reps = "${set.reps}",
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Delete Session Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            TextButton(onClick = { sessionToDelete = session.id }) {
                                Text(
                                    text = stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
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
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.history_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.history_delete_dialog_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.history_delete_dialog_msg),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
    val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (isSelected) accent else MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ParticipantAvatarBox(
            character = char,
            backgroundColor = color,
            size = 26.dp,
            fontSize = 14,
        )
        Text(
            text = if (isSelected) "$name ✓" else name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface,
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
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(participant, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text(round, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(kg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(reps, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
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
    val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
    val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Start side / Right in RTL: Calendar icon + Date & Metadata
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(horizontalAlignment = Alignment.Start) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                            )
                        }
                    }
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // End side / Left in RTL: Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}
