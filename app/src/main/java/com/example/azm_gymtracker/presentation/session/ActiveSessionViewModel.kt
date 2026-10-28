package com.example.azm_gymtracker.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseWithSets
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.domain.validator.RepValidator
import com.example.azm_gymtracker.domain.validator.WeightValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Immutable UI state for the active workout session screen (Figma #87:436).
 */
data class ActiveSessionUiState(
    val sessionWithDetails: WorkoutSessionWithDetails? = null,
    val participants: List<MemberEntity> = emptyList(),
    val activeTurnMemberId: Long? = null,
    val currentExercise: WorkoutExerciseWithSets? = null,
    val availableExercises: List<com.example.azm_gymtracker.domain.model.Exercise> = emptyList(),
    val isLoading: Boolean = true,

    // One-handed steppers (Figma: ±2.5 kg, ±1 rep)
    val weightStepper: Float = 70.0f,
    val repsStepper: Int = 8,
    val previousSetHint: String? = null,

    // Continuous timer
    val elapsedSeconds: Long = 0L,
    val isPaused: Boolean = false,
    val isWorkoutStarted: Boolean = false,

    // Rest timer overlay (Figma #87:1879)
    val isRestTimerVisible: Boolean = false,
    val restTimerRemainingSeconds: Int = 90,

    // Navigation and status
    val isSessionFinished: Boolean = false,
    val finishedSessionId: Long? = null,
    val isSessionCancelled: Boolean = false,
    val error: String? = null,
    val repsError: String? = null,
    val weightError: String? = null,
)

/**
 * ViewModel for the active workout session screen.
 *
 * Implements:
 * - Multi-participant shared session turn-taking
 * - Thumb-zone stepper adjustments (±2.5 kg, ±1 rep)
 * - Auto-rotation of turns and stepper pre-filling from participant history
 * - Elapsed timer calculation surviving process death
 * - Interactive rest countdown timer overlay
 * - Double-tap debouncing
 */
class ActiveSessionViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var restTimerJob: Job? = null
    private var isActionDebouncing = false

    init {
        observeActiveSession()
        observeAvailableExercises()
    }

    private fun observeAvailableExercises() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises()
                .catch { /* ignore */ }
                .collect { list ->
                    _uiState.update { it.copy(availableExercises = list) }
                }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            workoutRepository.getActiveSessionWithDetails()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { sessionDetails ->
                    if (sessionDetails == null) {
                        timerJob?.cancel()
                        _uiState.update {
                            it.copy(
                                sessionWithDetails = null,
                                participants = emptyList(),
                                isLoading = false,
                            )
                        }
                    } else {
                        val participants = sessionDetails.participants
                        val currentTurn = _uiState.value.activeTurnMemberId
                            ?: participants.firstOrNull()?.id

                        val currentEx = _uiState.value.currentExercise?.let { current ->
                            sessionDetails.exercises.find { it.workoutExercise.id == current.workoutExercise.id }
                        }

                        val hasCompletedAnySet = sessionDetails.exercises.any { it.sets.any { set -> set.isCompleted } }
                        val isStarted = _uiState.value.isWorkoutStarted || hasCompletedAnySet

                        _uiState.update {
                            it.copy(
                                sessionWithDetails = sessionDetails,
                                participants = participants,
                                activeTurnMemberId = currentTurn,
                                currentExercise = currentEx,
                                isWorkoutStarted = isStarted,
                                isPaused = sessionDetails.session.status == "PAUSED",
                                isLoading = false,
                            )
                        }

                        // Pre-fill steppers for the active turn member
                        updateSteppersForActiveTurn()

                        if (isStarted && (timerJob == null || timerJob?.isActive != true)) {
                            startElapsedTicker()
                        }
                    }
                }
        }
    }

    private fun startElapsedTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.sessionWithDetails != null && !_uiState.value.isSessionFinished && !_uiState.value.isSessionCancelled) {
                val session = _uiState.value.sessionWithDetails?.session
                if (session != null && !_uiState.value.isPaused && _uiState.value.isWorkoutStarted) {
                    val now = System.currentTimeMillis()
                    val activeDurationMs = (now - session.startedAt - session.pausedTotalMs).coerceAtLeast(0L)
                    _uiState.update { it.copy(elapsedSeconds = activeDurationMs / 1000L) }
                }
                delay(1000L)
            }
        }
    }

    /**
     * Pre-fills steppers from the active participant's last set in the current exercise.
     */
    private fun updateSteppersForActiveTurn() {
        val exercise = _uiState.value.currentExercise ?: return
        val memberId = _uiState.value.activeTurnMemberId ?: return

        viewModelScope.launch {
            val lastSetForMember = workoutRepository.getLastSetForMemberInExercise(
                exerciseId = exercise.workoutExercise.exerciseId,
                memberId = memberId,
            ) ?: workoutRepository.getLastSetInExercise(exercise.workoutExercise.exerciseId)

            if (lastSetForMember != null) {
                val weight = lastSetForMember.weightKg ?: 0f
                val reps = lastSetForMember.reps
                _uiState.update {
                    it.copy(
                        weightStepper = weight,
                        repsStepper = reps,
                        previousSetHint = if (weight > 0f) "$weight × $reps" else "$reps تكرار",
                    )
                }
            } else {
                _uiState.update { it.copy(previousSetHint = null) }
            }
        }
    }

    /** Starts a new shared session with selected participant IDs. */
    fun startSession(participantIds: List<Long>) {
        if (participantIds.isEmpty()) return
        viewModelScope.launch {
            runCatching { workoutRepository.startSession(participantIds) }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to start session") }
                }
        }
    }

    /** Manually switches the active turn to a specific participant. */
    fun selectTurn(memberId: Long) {
        _uiState.update { it.copy(activeTurnMemberId = memberId) }
        updateSteppersForActiveTurn()
    }

    /** Switches active exercise card view, or null to return to today's exercises list. */
    fun selectExercise(exercise: WorkoutExerciseWithSets?) {
        _uiState.update { it.copy(currentExercise = exercise) }
        if (exercise != null) {
            updateSteppersForActiveTurn()
        }
    }

    /** Removes an exercise from the active session. */
    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.removeExerciseFromSession(workoutExerciseId)
                if (_uiState.value.currentExercise?.workoutExercise?.id == workoutExerciseId) {
                    _uiState.update { it.copy(currentExercise = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to remove exercise") }
            }
        }
    }

    /** Adjusts weight stepper by delta (e.g. +2.5 or -2.5 kg). */
    fun adjustWeight(delta: Float) {
        _uiState.update {
            val newWeight = (it.weightStepper + delta).coerceIn(0f, 999.5f)
            it.copy(weightStepper = newWeight)
        }
    }

    /** Adjusts reps stepper by delta (e.g. +1 or -1 rep). */
    fun adjustReps(delta: Int) {
        _uiState.update {
            val newReps = (it.repsStepper + delta).coerceIn(1, 999)
            it.copy(repsStepper = newReps)
        }
    }

    /**
     * Primary action: "✓ تم"
     * Logs the set, marks it complete, triggers the rest timer overlay,
     * and rotates the turn to the next participant.
     */
    fun onDoneSet() {
        if (isActionDebouncing) return
        val currentEx = _uiState.value.currentExercise ?: return
        val currentMemberId = _uiState.value.activeTurnMemberId ?: return

        isActionDebouncing = true
        viewModelScope.launch {
            try {
                val weight = if (_uiState.value.weightStepper > 0f) _uiState.value.weightStepper else null
                val reps = _uiState.value.repsStepper

                val setId = workoutRepository.addSet(
                    workoutExerciseId = currentEx.workoutExercise.id,
                    memberId = currentMemberId,
                    reps = reps,
                    weightKg = weight,
                )
                workoutRepository.markSetCompleted(setId)

                // Advance turn to next participant
                val participants = _uiState.value.participants
                if (participants.isNotEmpty()) {
                    val currentIndex = participants.indexOfFirst { it.id == currentMemberId }
                    val nextIndex = (currentIndex + 1) % participants.size
                    val nextMemberId = participants[nextIndex].id
                    _uiState.update { it.copy(activeTurnMemberId = nextMemberId) }
                    updateSteppersForActiveTurn()

                    // Start rest timer only when solo or when a full cycle across all participants completes
                    if (participants.size <= 1 || nextIndex == 0) {
                        startRestTimer(90)
                    }
                } else {
                    startRestTimer(90)
                }
            } finally {
                delay(300L)
                isActionDebouncing = false
            }
        }
    }

    private fun startRestTimer(initialSeconds: Int = 90) {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isRestTimerVisible = true, restTimerRemainingSeconds = initialSeconds) }
        restTimerJob = viewModelScope.launch {
            while (isActive && _uiState.value.restTimerRemainingSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(restTimerRemainingSeconds = it.restTimerRemainingSeconds - 1) }
            }
            _uiState.update { it.copy(isRestTimerVisible = false) }
        }
    }

    fun skipRestTimer() {
        restTimerJob?.cancel()
        _uiState.update { it.copy(isRestTimerVisible = false) }
    }

    fun adjustRestTimer(secondsDelta: Int) {
        _uiState.update {
            val newTime = (it.restTimerRemainingSeconds + secondsDelta).coerceAtLeast(0)
            it.copy(restTimerRemainingSeconds = newTime)
        }
    }

    /** Starts the actual workout timer and begins the first exercise. */
    fun startWorkout() {
        val sessionWithDetails = _uiState.value.sessionWithDetails ?: return
        val firstExercise = sessionWithDetails.exercises.firstOrNull()
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            workoutRepository.updateSessionStartedAt(sessionWithDetails.session.id, now)
            _uiState.update {
                it.copy(
                    isWorkoutStarted = true,
                    elapsedSeconds = 0L,
                    currentExercise = it.currentExercise ?: firstExercise,
                )
            }
            updateSteppersForActiveTurn()
            startElapsedTicker()
        }
    }

    /** Adds multiple exercises from domain model to the active session. */
    fun addExercises(exercises: List<com.example.azm_gymtracker.domain.model.Exercise>) {
        val session = _uiState.value.sessionWithDetails?.session ?: return
        viewModelScope.launch {
            try {
                for (ex in exercises) {
                    val name = ex.nameAr.ifBlank { ex.nameEn }
                    workoutRepository.addExerciseToSession(session.id, ex.id, name)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add exercises") }
            }
        }
    }

    /** Adds an exercise from domain model to the active session. */
    fun addExercise(exercise: com.example.azm_gymtracker.domain.model.Exercise) {
        addExercise(exercise.id, exercise.nameAr.ifBlank { exercise.nameEn })
    }

    /** Creates and adds a custom exercise in one flow. */
    fun createCustomExercise(name: String, category: MuscleCategory) {
        viewModelScope.launch {
            try {
                val newExercise = com.example.azm_gymtracker.domain.model.Exercise(
                    nameAr = name,
                    nameEn = name,
                    category = category,
                    isCustom = true,
                )
                val id = exerciseRepository.createCustomExercise(newExercise)
                addExercise(id, name)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to create custom exercise") }
            }
        }
    }

    /** Adds a set with raw input strings. */
    fun addSet(workoutExerciseId: Long, repsRaw: String, weightRaw: String) {
        val reps = repsRaw.toIntOrNull() ?: 8
        val weight = weightRaw.toFloatOrNull()
        val currentMemberId = _uiState.value.activeTurnMemberId ?: return
        viewModelScope.launch {
            try {
                workoutRepository.addSet(workoutExerciseId, currentMemberId, reps, weight)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add set") }
            }
        }
    }

    /** Updates an existing set. */
    fun updateSet(set: com.example.azm_gymtracker.domain.model.WorkoutSet, repsStr: String, weightStr: String) {
        val reps = repsStr.toIntOrNull() ?: set.reps
        val weight = weightStr.toFloatOrNull() ?: set.weightKg
        viewModelScope.launch {
            try {
                workoutRepository.updateSet(set.copy(reps = reps, weightKg = weight))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to update set") }
            }
        }
    }

    /** Discard alias for cancelSession. */
    fun discardSession() {
        cancelSession()
    }

    /** Adds an exercise to the active session. */
    fun addExercise(exerciseId: Long, snapshotName: String) {
        val sessionId = _uiState.value.sessionWithDetails?.session?.id ?: return
        viewModelScope.launch {
            runCatching {
                workoutRepository.addExerciseToSession(sessionId, exerciseId, snapshotName)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to add exercise") }
            }
        }
    }

    /** Deletes a set and re-indexes remaining sets. */
    fun deleteSet(setId: Long, workoutExerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                workoutRepository.deleteSet(setId, workoutExerciseId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to delete set") }
            }
        }
    }

    /** Finishes the active workout session. */
    fun finishSession() {
        val sessionId = _uiState.value.sessionWithDetails?.session?.id ?: return
        viewModelScope.launch {
            runCatching { workoutRepository.finishSession(sessionId) }
                .onSuccess {
                    timerJob?.cancel()
                    restTimerJob?.cancel()
                    _uiState.update { it.copy(isSessionFinished = true, finishedSessionId = sessionId) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to finish session") }
                }
        }
    }

    /** Pauses the session timer. */
    fun pauseSession() {
        _uiState.update { it.copy(isPaused = true) }
    }

    /** Resumes the session timer. */
    fun resumeSession() {
        _uiState.update { it.copy(isPaused = false) }
    }

    /** Discards / cancels the active workout session. */
    fun cancelSession() {
        val sessionId = _uiState.value.sessionWithDetails?.session?.id ?: return
        viewModelScope.launch {
            runCatching { workoutRepository.cancelSession(sessionId) }
                .onSuccess {
                    timerJob?.cancel()
                    restTimerJob?.cancel()
                    _uiState.update { it.copy(isSessionCancelled = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to cancel session") }
                }
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ActiveSessionViewModel(workoutRepository, exerciseRepository) as T
    }
}
