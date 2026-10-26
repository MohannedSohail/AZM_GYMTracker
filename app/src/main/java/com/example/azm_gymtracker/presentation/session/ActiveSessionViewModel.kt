package com.example.azm_gymtracker.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.model.WorkoutExercise
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.domain.validator.RepValidator
import com.example.azm_gymtracker.domain.validator.WeightValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Immutable UI state for an active workout session. */
data class ActiveSessionUiState(
    val session: WorkoutSession? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = true,
    val isSessionFinished: Boolean = false,
    val isSessionDiscarded: Boolean = false,
    val error: String? = null,
    /** Validation error for the reps input in the current set row. */
    val repsError: String? = null,
    /** Validation error for the weight input in the current set row. */
    val weightError: String? = null,
)

/**
 * ViewModel for the active workout session screen.
 *
 * Handles starting a session, adding exercises, logging sets with validation,
 * finishing, and discarding sessions.
 */
class ActiveSessionViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val memberId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveSessionUiState())
    val uiState: StateFlow<ActiveSessionUiState> = _uiState.asStateFlow()

    init {
        observeActiveSession()
        loadExerciseCatalog()
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            workoutRepository.getActiveSession(memberId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { session ->
                    _uiState.update { it.copy(session = session, isLoading = false) }
                }
        }
    }

    private fun loadExerciseCatalog() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { exercises ->
                    _uiState.update { it.copy(availableExercises = exercises) }
                }
        }
    }

    /** Starts a new workout session for the active member. */
    fun startSession() {
        viewModelScope.launch {
            runCatching { workoutRepository.startSession(memberId) }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to start session") }
                }
        }
    }

    /** Adds an exercise from the catalog to the active session. */
    fun addExercise(exercise: Exercise) {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            runCatching {
                workoutRepository.addExerciseToSession(sessionId, exercise.id, exercise.nameEn)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to add exercise") }
            }
        }
    }

    /** Creates a custom exercise in the catalog scoped to this member. */
    fun createCustomExercise(name: String, category: MuscleCategory) {
        viewModelScope.launch {
            runCatching {
                exerciseRepository.createCustomExercise(
                    Exercise(
                        nameEn = name,
                        nameAr = name,
                        category = category,
                        isCustom = true,
                        creatorMemberId = memberId,
                    )
                )
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to create exercise") }
            }
        }
    }

    /**
     * Validates and logs a new set for [workoutExerciseId].
     * Updates [ActiveSessionUiState.repsError] or [ActiveSessionUiState.weightError] on failure.
     */
    fun addSet(workoutExerciseId: Long, repsRaw: String, weightRaw: String) {
        val repsResult = RepValidator.validateString(repsRaw)
        val weightResult = WeightValidator.validateString(weightRaw)

        val repsError = when (repsResult) {
            RepValidator.Result.Valid -> null
            RepValidator.Result.TooLow -> "Enter at least 1 rep"
            RepValidator.Result.TooHigh -> "Maximum 999 reps"
        }
        val weightError = when (weightResult) {
            WeightValidator.Result.Valid, WeightValidator.Result.Bodyweight -> null
            WeightValidator.Result.TooLow -> "Enter a valid weight"
            WeightValidator.Result.TooHigh -> "Maximum 999.5 kg"
        }

        if (repsError != null || weightError != null) {
            _uiState.update { it.copy(repsError = repsError, weightError = weightError) }
            return
        }

        _uiState.update { it.copy(repsError = null, weightError = null) }
        val reps = repsRaw.trim().toInt()
        val weightKg = weightRaw.trim().toFloatOrNull()

        viewModelScope.launch {
            runCatching {
                workoutRepository.addSet(workoutExerciseId, reps, weightKg)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to add set") }
            }
        }
    }

    /** Updates an existing set's reps and weight. */
    fun updateSet(set: WorkoutSet, repsRaw: String, weightRaw: String) {
        val reps = repsRaw.trim().toIntOrNull() ?: return
        val weightKg = weightRaw.trim().toFloatOrNull()
        viewModelScope.launch {
            runCatching {
                workoutRepository.updateSet(set.copy(reps = reps, weightKg = weightKg))
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to update set") }
            }
        }
    }

    /** Atomically deletes a set and re-indexes remaining sets. */
    fun deleteSet(setId: Long, workoutExerciseId: Long) {
        viewModelScope.launch {
            runCatching {
                workoutRepository.deleteSet(setId, workoutExerciseId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to delete set") }
            }
        }
    }

    /** Marks the session as COMPLETED and navigates away. */
    fun finishSession() {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            runCatching { workoutRepository.finishSession(sessionId) }
                .onSuccess { _uiState.update { it.copy(isSessionFinished = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to finish session") }
                }
        }
    }

    /** Marks the session as DISCARDED without saving to history. */
    fun discardSession() {
        val sessionId = _uiState.value.session?.id ?: return
        viewModelScope.launch {
            runCatching { workoutRepository.discardSession(sessionId) }
                .onSuccess { _uiState.update { it.copy(isSessionDiscarded = true) } }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to discard session") }
                }
        }
    }

    /** Clears input validation errors when the user starts editing. */
    fun clearInputErrors() {
        _uiState.update { it.copy(repsError = null, weightError = null) }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
        private val memberId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ActiveSessionViewModel(workoutRepository, exerciseRepository, memberId) as T
    }
}
