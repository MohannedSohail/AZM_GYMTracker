package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.model.WorkoutExercise
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.presentation.session.ActiveSessionUiState
import com.example.azm_gymtracker.ui.component.AzmPrimaryButton
import com.example.azm_gymtracker.ui.component.AzmSecondaryButton
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
import com.example.azm_gymtracker.ui.theme.AzmNeonGreen
import com.example.azm_gymtracker.ui.theme.MemberColorGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Active Workout Screen (Figma Node #87:436 "التمرين النشط — داكن")
 * Features timer controls, multi-participant turn rotation carousel, set queue,
 * and one-handed thumb-zone steppers (±2.5 kg, ±1 rep).
 */
import androidx.activity.compose.BackHandler

@Composable
fun ActiveSessionScreen(
    uiState: ActiveSessionUiState,
    onAddExercise: (Exercise) -> Unit,
    onCreateCustomExercise: (name: String, category: MuscleCategory) -> Unit,
    onAddSet: (workoutExerciseId: Long) -> Unit,
    onUpdateSet: (WorkoutSet, String, String) -> Unit,
    onDeleteSet: (setId: Long, workoutExerciseId: Long) -> Unit,
    onFinishSession: () -> Unit,
    onDiscardSession: () -> Unit,
    onNavigateBack: () -> Unit,
    onDoneSet: () -> Unit = {},
    onAdjustWeight: (Float) -> Unit = {},
    onAdjustReps: (Int) -> Unit = {},
    onSelectTurn: (Long) -> Unit = {},
    onSkipRestTimer: () -> Unit = {},
    onAdjustRestTimer: (Int) -> Unit = {},
    onPauseSession: () -> Unit = {},
    onResumeSession: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPauseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }

    // Intercept hardware and gesture back presses to prevent accidental session loss
    BackHandler(enabled = true) {
        showExitConfirmDialog = true
    }

    val formattedTimer = remember(uiState.elapsedSeconds) {
        val minutes = uiState.elapsedSeconds / 60
        val seconds = uiState.elapsedSeconds % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AzmDarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Continuous Timer & Session Controls (Figma Node #87:451)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Pause button (48dp circle)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AzmDarkSurfaceVariant)
                        .clickable { showPauseDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = stringResource(R.string.session_pause_action),
                        tint = AzmDarkOnSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Elapsed time in center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = formattedTimer,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = AzmDarkOnSurface,
                        )
                    }
                    Text(
                        text = stringResource(R.string.session_timer_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmDarkOnSurfaceMuted,
                    )
                }

                // Back arrow button (48dp circle)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AzmDarkSurfaceVariant)
                        .clickable { showExitConfirmDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.action_back),
                        tint = AzmDarkOnSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 6.dp),
        ) {
            // Participants Carousel Chips (Figma Node #87:465)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                uiState.participants.forEach { member ->
                    val isTurn = member.id == uiState.activeTurnMemberId
                    ParticipantTurnChip(
                        name = member.name + if (isTurn) " ✓" else "",
                        character = member.name.take(1),
                        color = parseMemberColor(member.colorHex),
                        isActive = isTurn,
                        onClick = { onSelectTurn(member.id) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current Exercise Header Card (Figma Node #87:482)
            val currentExercise = uiState.currentExercise ?: uiState.sessionWithDetails?.exercises?.firstOrNull()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AzmDarkSurface)
                    .border(1.dp, AzmBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 104.dp, height = 72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AzmDarkSurfaceSelected),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🏋️", fontSize = 32.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "الصدر • بار",
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmEmeraldGreen,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentExercise?.workoutExercise?.exerciseNameSnapshot
                            ?: currentExercise?.catalogExercise?.nameAr
                            ?: "بنش برس بالبار مستوي",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AzmDarkOnSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Set Queue (Figma Node #87:491)
            val currentExerciseSets = currentExercise?.sets ?: emptyList()
            val activeTurnMember = uiState.participants.find { it.id == uiState.activeTurnMemberId }
            val activeMemberTurnSetsCount = currentExerciseSets.count { it.memberId == activeTurnMember?.id }
            val currentTurnTitle = "${activeTurnMember?.name ?: "متمرن"} • الجولة ${activeMemberTurnSetsCount + 1}"

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(currentExerciseSets, key = { it.id }) { setItem ->
                    val member = uiState.participants.find { it.id == setItem.memberId }
                    val memberName = member?.name ?: "متمرن"
                    val weightStr = if (setItem.weightKg != null) "${setItem.weightKg}" else "وزن الجسم"
                    val title = "$memberName • الجولة ${setItem.setNumber} ${if (setItem.isCompleted) "✓" else ""}"
                    val stats = "$weightStr × ${setItem.reps}"

                    SetQueueRow(
                        title = title,
                        stats = stats,
                        isCompleted = setItem.isCompleted,
                        isActive = false,
                        onDelete = { onDeleteSet(setItem.id, currentExercise?.workoutExercise?.id ?: 0L) },
                    )
                }

                // Currently active pending set
                item {
                    val weightDisplay = if (uiState.weightStepper > 0f) "${uiState.weightStepper}" else "وزن الجسم"
                    SetQueueRow(
                        title = currentTurnTitle,
                        stats = "$weightDisplay × ${uiState.repsStepper}",
                        isCompleted = false,
                        isActive = true,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Thumb-zone Logging Controls (Figma Node #87:516)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Hint row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (uiState.previousSetHint != null) {
                            stringResource(R.string.last_round_hint, uiState.previousSetHint)
                        } else {
                            ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AzmDarkOnSurfaceMuted,
                    )
                    Text(
                        text = currentTurnTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = AzmEmeraldGreen,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Weight Stepper (±2.5 kg)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "±2.5", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                    Text(text = stringResource(R.string.stepper_weight_label), style = MaterialTheme.typography.bodyMedium, color = AzmDarkOnSurface)
                }

                StepperControl(
                    displayValue = String.format(Locale.US, "%.1f", uiState.weightStepper),
                    onDecrease = { onAdjustWeight(-2.5f) },
                    onIncrease = { onAdjustWeight(2.5f) },
                )

                // Reps Stepper (±1 rep)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "±1", style = MaterialTheme.typography.bodySmall, color = AzmDarkOnSurfaceMuted)
                    Text(text = stringResource(R.string.stepper_reps_label), style = MaterialTheme.typography.bodyMedium, color = AzmDarkOnSurface)
                }

                StepperControl(
                    displayValue = "${uiState.repsStepper}",
                    onDecrease = { onAdjustReps(-1) },
                    onIncrease = { onAdjustReps(1) },
                )

                // Primary Action Button: "✓ تم"
                AzmPrimaryButton(
                    text = stringResource(R.string.action_done),
                    onClick = {
                        onDoneSet()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "تم حفظ الجولة بنجاح",
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )

                // Secondary Action Buttons Row: "＋ إضافة تمرين" & "＋ إضافة جولة"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AzmSecondaryButton(
                        text = stringResource(R.string.session_add_round),
                        onClick = {
                            currentExercise?.let { onAddSet(it.workoutExercise.id) }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    AzmSecondaryButton(
                        text = stringResource(R.string.session_add_exercise),
                        onClick = { showExercisePicker = true },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Text button: "إنهاء التمرين"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showFinishDialog = true }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.session_finish_button),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzmDarkOnSurfaceMuted,
                    )
                }
            }
        }
    }

    // Rest Timer Overlay (Figma Node #87:1880)
    if (uiState.isRestTimerVisible) {
        RestTimerDialog(
            remainingSeconds = uiState.restTimerRemainingSeconds,
            onAdjustSeconds = { onAdjustRestTimer(it) },
            onSkip = { onSkipRestTimer() },
        )
    }

    // Pause Dialog (Figma Node #87:1900)
    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            containerColor = AzmDarkSurface,
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(
                    text = stringResource(R.string.session_pause_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AzmDarkOnSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_pause_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmDarkOnSurfaceMuted,
                )
            },
            confirmButton = {
                AzmPrimaryButton(
                    text = if (uiState.isPaused) "استئناف التمرين" else stringResource(R.string.session_pause_action),
                    onClick = {
                        if (uiState.isPaused) onResumeSession() else onPauseSession()
                        showPauseDialog = false
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            },
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurfaceMuted)
                }
            },
        )
    }

    // Exit & Discard Confirmation Dialog (Back-press protection)
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            containerColor = AzmDarkSurface,
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(
                    text = stringResource(R.string.session_discard_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AzmDarkOnSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_discard_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmDarkOnSurfaceMuted,
                )
            },
            confirmButton = {
                AzmPrimaryButton(
                    text = stringResource(R.string.session_exit_keep_running),
                    onClick = {
                        showExitConfirmDialog = false
                        onNavigateBack()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showExitConfirmDialog = false
                            onDiscardSession()
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.session_exit_discard),
                            color = AzmError,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurfaceMuted)
                    }
                }
            },
        )
    }

    // Finish Workout Dialog (Figma Node #87:1893)
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = AzmDarkSurface,
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(
                    text = stringResource(R.string.session_finish_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = AzmDarkOnSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_finish_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AzmDarkOnSurfaceMuted,
                )
            },
            confirmButton = {
                AzmPrimaryButton(
                    text = stringResource(R.string.session_finish_and_save),
                    onClick = {
                        showFinishDialog = false
                        onFinishSession()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurfaceMuted)
                }
            },
        )
    }

    // Exercise Picker Sheet
    if (showExercisePicker) {
        ExercisePickerSheet(
            exercises = uiState.availableExercises,
            onSelectExercise = { ex ->
                onAddExercise(ex)
                showExercisePicker = false
            },
            onCreateCustomExercise = { name, cat ->
                onCreateCustomExercise(name, cat)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false },
        )
    }
}

@Composable
private fun ParticipantTurnChip(
    name: String,
    character: String,
    color: Color,
    isActive: Boolean,
    onClick: () -> Unit = {},
) {
    val bg = if (isActive) AzmDarkSurfaceSelected else AzmDarkSurface
    val border = if (isActive) AzmEmeraldGreen else AzmBorderSubtle

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = AzmDarkOnSurface,
        )
        ParticipantAvatarBox(
            character = character,
            backgroundColor = color,
            size = 26.dp,
            fontSize = 14,
        )
    }
}

@Composable
private fun SetQueueRow(
    title: String,
    stats: String,
    isCompleted: Boolean,
    isActive: Boolean,
    onDelete: (() -> Unit)? = null,
) {
    val bg = if (isActive) AzmDarkSurfaceSelected else AzmDarkSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isCompleted) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onDelete != null) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = AzmDarkOnSurfaceMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable(onClick = onDelete),
                    )
                }
            }
        } else if (isActive) {
            Icon(
                imageVector = Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = AzmEmeraldGreen,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = AzmDarkOnSurfaceMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = stats,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) AzmDarkOnSurface else AzmDarkOnSurfaceMuted,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = AzmDarkOnSurface,
            )
        }
    }
}

@Composable
private fun StepperControl(
    displayValue: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AzmDarkSurface)
            .border(1.dp, AzmEmeraldGreen, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Decrease button (−)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AzmDarkSurfaceVariant)
                .clickable(onClick = onDecrease),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.headlineMedium,
                color = AzmDarkOnSurface,
                fontWeight = FontWeight.Bold,
            )
        }

        // Center Value (24sp Bold)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AzmDarkOnSurface,
            )
        }

        // Increase button (＋)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AzmDarkSurfaceSelected)
                .clickable(onClick = onIncrease),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "＋",
                style = MaterialTheme.typography.headlineMedium,
                color = AzmEmeraldGreen,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Rest Timer Dialog (Figma Node #87:1880)
 */
@Composable
fun RestTimerDialog(
    remainingSeconds: Int,
    onAdjustSeconds: (Int) -> Unit,
    onSkip: () -> Unit,
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onSkip,
        containerColor = AzmDarkSurface,
        shape = RoundedCornerShape(32.dp),
        title = {
            Text(
                text = stringResource(R.string.rest_timer_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AzmDarkOnSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Circular Countdown Ring (172dp)
                Box(
                    modifier = Modifier
                        .size(172.dp)
                        .clip(CircleShape)
                        .background(AzmDarkSurfaceSelected)
                        .border(4.dp, AzmEmeraldGreen, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                            fontWeight = FontWeight.Black,
                            color = AzmDarkOnSurface,
                        )
                    }
                }

                // Adjustments (+15s / -15s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AzmSecondaryButton(
                        text = stringResource(R.string.action_minus_15s),
                        onClick = { onAdjustSeconds(-15) },
                        modifier = Modifier.weight(1f),
                    )
                    AzmSecondaryButton(
                        text = stringResource(R.string.action_plus_15s),
                        onClick = { onAdjustSeconds(15) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            AzmPrimaryButton(
                text = stringResource(R.string.action_skip),
                onClick = onSkip,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        },
    )
}
