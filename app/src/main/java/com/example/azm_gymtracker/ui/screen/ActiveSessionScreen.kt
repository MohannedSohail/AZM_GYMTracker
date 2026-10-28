package com.example.azm_gymtracker.ui.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.presentation.session.ActiveSessionUiState
import com.example.azm_gymtracker.ui.animation.azmPhysicsItemEntrance
import com.example.azm_gymtracker.ui.animation.azmPhysicsPress
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
import com.example.azm_gymtracker.ui.theme.LocalThemeIsDark
import com.example.azm_gymtracker.ui.theme.LocalThemeToggle
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Active Workout Screen (Figma Node #87:436 "التمرين النشط — داكن")
 * Features timer controls, multi-participant turn rotation carousel, set queue,
 * and one-handed thumb-zone steppers (±2.5 kg, ±1 rep).
 */
@Composable
fun ActiveSessionScreen(
    uiState: ActiveSessionUiState,
    onAddExercise: (Exercise) -> Unit,
    onCreateCustomExercise: (name: String, category: MuscleCategory) -> Unit,
    @Suppress("UNUSED_PARAMETER") onAddSet: (workoutExerciseId: Long) -> Unit,
    @Suppress("UNUSED_PARAMETER") onUpdateSet: (WorkoutSet, String, String) -> Unit,
    onDeleteSet: (setId: Long, workoutExerciseId: Long) -> Unit,
    onFinishSession: () -> Unit,
    onDiscardSession: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onSelectExercise: (com.example.azm_gymtracker.data.local.entity.WorkoutExerciseWithSets?) -> Unit = {},
    onRemoveExercise: (Long) -> Unit = {},
    onDoneSet: () -> Unit = {},
    onAdjustWeight: (Float) -> Unit = {},
    onAdjustReps: (Int) -> Unit = {},
    onSelectTurn: (Long) -> Unit = {},
    onSkipRestTimer: () -> Unit = {},
    onAdjustRestTimer: (Int) -> Unit = {},
    onPauseSession: () -> Unit = {},
    onResumeSession: () -> Unit = {},
    onAddExercises: (List<Exercise>) -> Unit = {},
    onStartWorkout: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPauseDialog by remember { mutableStateOf(value = false) }
    var showFinishDialog by remember { mutableStateOf(value = false) }
    var showExitConfirmDialog by remember { mutableStateOf(value = false) }
    var showExercisePicker by remember { mutableStateOf(value = false) }
    var hasAutoOpenedPicker by rememberSaveable { mutableStateOf(false) }

    val exercisesList = uiState.sessionWithDetails?.exercises ?: emptyList()

    // Automatically prompt exercise picker when entering a new session without exercises yet
    LaunchedEffect(exercisesList.isEmpty(), uiState.isLoading) {
        if (!uiState.isLoading && exercisesList.isEmpty() && !hasAutoOpenedPicker) {
            showExercisePicker = true
            hasAutoOpenedPicker = true
        }
    }

    val isDark = LocalThemeIsDark.current
    val toggleTheme = LocalThemeToggle.current

    // Intercept hardware and gesture back presses: return to routine list or confirm exit
    BackHandler(enabled = true) {
        if (uiState.currentExercise != null) {
            onSelectExercise(null)
        } else {
            showExitConfirmDialog = true
        }
    }

    val formattedTimer = remember(uiState.elapsedSeconds) {
        val minutes = uiState.elapsedSeconds / 60
        val seconds = uiState.elapsedSeconds % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // Continuous Timer & Session Controls (Figma Node #87:451)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Back arrow button (Start side / Right in RTL, 44dp circle)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { showExitConfirmDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }

                // Elapsed time in center (starts only when workout starts)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = if (uiState.isWorkoutStarted) formattedTimer else "00:00",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isWorkoutStarted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = if (uiState.isWorkoutStarted) stringResource(R.string.session_timer_label) else "بانتظار البدء",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.isWorkoutStarted) (if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Actions: Theme toggle + Pause button (End side / Left in RTL)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Theme toggle button
                    var buttonCenter by remember { mutableStateOf<Offset?>(null) }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "الوضع الفاتح" else "الوضع الداكن",
                            tint = if (isDark) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // Pause button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { showPauseDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = stringResource(R.string.session_pause_action),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp),
        ) {
            // Participants Carousel Chips (Shows team, turn indicator, and sets per exercise)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
            ) {
                uiState.participants.forEach { member ->
                    val isTurn = member.id == uiState.activeTurnMemberId
                    val currentExSetsCount = uiState.currentExercise?.sets?.count { it.memberId == member.id && it.isCompleted } ?: 0
                    val setsBadge = if (uiState.currentExercise != null && currentExSetsCount > 0) " (${currentExSetsCount}✓)" else ""
                    ParticipantTurnChip(
                        name = "${member.name}$setsBadge" + if (isTurn) " • دوره الآن" else "",
                        character = member.name.take(1),
                        color = parseMemberColor(member.colorHex),
                        isActive = isTurn,
                        onClick = { onSelectTurn(member.id) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentExercise = uiState.currentExercise

            if (currentExercise == null) {
                // ══════════════════════════════════════════════════════════════════════
                // VIEW 1: قائمة تمارين اليوم (Today's Exercises Routine)
                // ══════════════════════════════════════════════════════════════════════

                // Header with exercise count & "+ إضافة تمرين لليوم" button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "تمارين اليوم",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (exercisesList.isEmpty()) "لم يتم اختيار تمارين بعد" else "${exercisesList.size} تمارين في جدول اليوم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    AzmSecondaryButton(
                        text = "＋ إضافة تمرين",
                        onClick = { showExercisePicker = true },
                        modifier = Modifier.width(135.dp),
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (exercisesList.isEmpty()) {
                    // Empty State: Invite to pick exercises for today
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text("🏋️", fontSize = 52.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "اختر تمارين اليوم لتبدأوا",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "حدد التمارين التي ستتمرنونها معاً اليوم، وادخلوا على كل تمرين لتسجيل الأوزان والعدات بالتناوب.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            AzmPrimaryButton(
                                text = "＋ اختيار تمارين اليوم",
                                onClick = { showExercisePicker = true },
                            )
                        }
                    }
                } else {
                    // List of Today's Exercises
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(exercisesList, key = { _, it -> it.workoutExercise.id }) { index, ex ->
                            val setsCount = ex.sets.size
                            val completedSets = ex.sets.count { it.isCompleted }
                            val participantIdsWithSets = ex.sets.mapTo(HashSet()) { it.memberId }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .azmPhysicsItemEntrance(index = index)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                    .azmPhysicsPress(pressedScale = 0.97f, onClick = {
                                        if (!uiState.isWorkoutStarted) {
                                            onStartWorkout()
                                        }
                                        onSelectExercise(ex)
                                    })
                                    .padding(16.dp),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                            Text(
                                                text = ex.workoutExercise.exerciseNameSnapshot,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (setsCount > 0) "$completedSets جولات مسجلة" else "اضغط للبدء وتسجيل الجولات",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (setsCount > 0) (if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }

                                        // Delete exercise from session button
                                        IconButton(
                                            onClick = { onRemoveExercise(ex.workoutExercise.id) },
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف التمرين",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Participant avatars who played in this exercise
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            uiState.participants.filter { it.id in participantIdsWithSets }.forEach { m ->
                                                ParticipantAvatarBox(
                                                    character = m.name.take(1),
                                                    backgroundColor = parseMemberColor(m.colorHex),
                                                    size = 28.dp,
                                                    fontSize = 14,
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(
                                                text = "تسجيل الجولات",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (isDark) AzmNeonGreen else MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = null,
                                                tint = if (isDark) AzmNeonGreen else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!uiState.isWorkoutStarted && exercisesList.isNotEmpty()) {
                    AzmPrimaryButton(
                        text = "🚀 بدء التمرين الأول وتشغيل التايمر",
                        onClick = { onStartWorkout() },
                    )
                } else {
                    // Bottom Session Finalize Button
                    AzmPrimaryButton(
                        text = "إنهاء التمرين وحفظ النتائج ✓",
                        onClick = { showFinishDialog = true },
                    )
                }
            } else {
                // ══════════════════════════════════════════════════════════════════════
                // VIEW 2: تفاصيل التمرين النشط وتسجيل الجولات (Individual Exercise Sets)
                // ══════════════════════════════════════════════════════════════════════

                // Back navigation to Today's Exercises List
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectExercise(null) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "العودة لقائمة تمارين اليوم",
                        tint = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "العودة لقائمة تمارين اليوم",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Current Exercise Header Card (Figma Node #87:482 & #87:1197)
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
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentExercise.catalogExercise.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentExercise.workoutExercise.exerciseNameSnapshot,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🏋️", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Set Queue for this specific exercise
                val currentExerciseSets = currentExercise.sets
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
                        val weightStr = if ((setItem.weightKg ?: 0f) > 0f) "${setItem.weightKg} كغ" else "وزن الجسم"
                        val title = "$memberName • الجولة ${setItem.setNumber} ${if (setItem.isCompleted) "✓" else ""}"
                        val stats = "$weightStr × ${setItem.reps}"

                        SetQueueRow(
                            title = title,
                            stats = stats,
                            isCompleted = setItem.isCompleted,
                            isActive = false,
                            onDelete = { onDeleteSet(setItem.id, currentExercise.workoutExercise.id) },
                        )
                    }

                    // Currently active pending set
                    item {
                        val weightDisplay = if (uiState.weightStepper > 0f) "${uiState.weightStepper} كغ" else "وزن الجسم"
                        SetQueueRow(
                            title = currentTurnTitle,
                            stats = "$weightDisplay × ${uiState.repsStepper}",
                            isCompleted = false,
                            isActive = true,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Thumb-zone Logging Controls (Figma Node #87:516 & #87:1210)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Hint row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = currentTurnTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (uiState.previousSetHint != null) {
                                stringResource(R.string.last_round_hint, uiState.previousSetHint)
                            } else {
                                ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Weight Stepper (±2.5 kg)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = stringResource(R.string.stepper_weight_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "±2.5 كغ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text(text = stringResource(R.string.stepper_reps_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "±1 تكرار", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    StepperControl(
                        displayValue = uiState.repsStepper.toString(),
                        onDecrease = { onAdjustReps(-1) },
                        onIncrease = { onAdjustReps(1) },
                    )

                    // Primary Action Button: "✓ تسجيل الجولة"
                    AzmPrimaryButton(
                        text = "✓ تسجيل الجولة لـ ${activeTurnMember?.name ?: ""}",
                        onClick = {
                            val currentMemberName = activeTurnMember?.name ?: ""
                            val participants = uiState.participants
                            val currentIndex = participants.indexOfFirst { it.id == activeTurnMember?.id }
                            val nextMemberName = if (participants.isNotEmpty() && currentIndex >= 0) {
                                val nextIndex = (currentIndex + 1) % participants.size
                                participants[nextIndex].name
                            } else ""

                            onDoneSet()
                            scope.launch {
                                val message = if (participants.size > 1 && nextMemberName.isNotBlank()) {
                                    "تم تسجيل جولة $currentMemberName ✓ • دور $nextMemberName الآن!"
                                } else {
                                    "تم تسجيل الجولة بنجاح ✓"
                                }
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                    )

                    // Navigation buttons
                    val allExercises = uiState.sessionWithDetails?.exercises ?: emptyList()
                    val currentExIndex = allExercises.indexOfFirst { it.workoutExercise.id == currentExercise.workoutExercise.id }
                    val nextEx = if (currentExIndex in 0 until allExercises.lastIndex) allExercises[currentExIndex + 1] else null

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AzmSecondaryButton(
                            text = "قائمة التمارين",
                            onClick = { onSelectExercise(null) },
                            modifier = Modifier.weight(1f),
                        )
                        if (nextEx != null) {
                            AzmSecondaryButton(
                                text = "التمرين التالي ❯",
                                onClick = { onSelectExercise(nextEx) },
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            AzmSecondaryButton(
                                text = "إنهاء التمرين ✓",
                                onClick = { showFinishDialog = true },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
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

    // Pause Dialog (Figma Node #87:1900 & #87:2033)
    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(
                    text = stringResource(R.string.session_pause_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_pause_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }

    // Exit & Discard Confirmation Dialog (Back-press protection)
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(
                    text = stringResource(R.string.session_discard_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_discard_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    TextButton(onClick = { showExitConfirmDialog = false }) {
                        Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
        )
    }

    // Finish Workout Dialog (Figma Node #87:1893 & #87:2033)
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(32.dp),
            title = {
                Text(
                    text = stringResource(R.string.session_finish_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.session_finish_dialog_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }

    // Exercise Picker Sheet with Multi-Select
    if (showExercisePicker) {
        ExercisePickerSheet(
            exercises = uiState.availableExercises,
            onSelectExercises = { selectedList ->
                onAddExercises(selectedList)
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
    val isDark = LocalThemeIsDark.current
    val bg = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (isActive) (if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.outline
    val textColor = if (isActive) (if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurface

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
            color = textColor,
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
    val isDark = LocalThemeIsDark.current
    val bg = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant

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
        // Start side / Right in RTL: Title + Stats
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = stats,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // End side / Left in RTL: Status indicator / Delete icon
        if (isCompleted && onDelete != null) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDelete),
            )
        } else if (isActive) {
            Icon(
                imageVector = Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
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
    val isDark = LocalThemeIsDark.current
    val accentColor = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, accentColor, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Increase button (＋) (Start side / Right in RTL)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .azmPhysicsPress(pressedScale = 0.88f, onClick = onIncrease),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "＋",
                style = MaterialTheme.typography.headlineMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold,
            )
        }

        // Center Value (24sp Bold)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Decrease button (−) (End side / Left in RTL)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .azmPhysicsPress(pressedScale = 0.88f, onClick = onDecrease),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Rest Timer Dialog (Figma Node #87:1880 & #87:2033)
 */
@Composable
fun RestTimerDialog(
    remainingSeconds: Int,
    onAdjustSeconds: (Int) -> Unit,
    onSkip: () -> Unit,
) {
    val isDark = LocalThemeIsDark.current
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onSkip,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(32.dp),
        title = {
            Text(
                text = stringResource(R.string.rest_timer_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(4.dp, if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
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
