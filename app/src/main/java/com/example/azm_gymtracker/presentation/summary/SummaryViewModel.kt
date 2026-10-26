package com.example.azm_gymtracker.presentation.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable UI state for WorkoutSummaryScreen (Figma #87:550).
 */
data class WorkoutSummaryUiState(
    val sessionWithDetails: WorkoutSessionWithDetails? = null,
    val participantBreakdowns: List<ParticipantVolumeTuple> = emptyList(),
    val totalVolumeKg: Double = 0.0,
    val totalSetsCount: Int = 0,
    val totalExercisesCount: Int = 0,
    val durationMinutes: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/**
 * ViewModel for Workout Summary screen after finishing a session.
 */
class SummaryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val sessionId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSummaryUiState())
    val uiState: StateFlow<WorkoutSummaryUiState> = _uiState.asStateFlow()

    init {
        loadSessionSummary()
    }

    private fun loadSessionSummary() {
        viewModelScope.launch {
            workoutRepository.getSessionWithDetails(sessionId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { details ->
                    if (details != null) {
                        val session = details.session
                        val durationMs = if (session.endedAt != null) {
                            (session.endedAt - session.startedAt - session.pausedTotalMs).coerceAtLeast(0L)
                        } else {
                            0L
                        }
                        val durationMinutes = durationMs / (1000 * 60)
                        val totalExercises = details.exercises.size

                        _uiState.update {
                            it.copy(
                                sessionWithDetails = details,
                                totalExercisesCount = totalExercises,
                                durationMinutes = durationMinutes,
                                isLoading = false,
                            )
                        }
                    }
                }
        }

        viewModelScope.launch {
            workoutRepository.getParticipantVolumeBreakdown(sessionId)
                .catch { /* ignore */ }
                .collect { breakdowns ->
                    val totalVol = breakdowns.sumOf { it.totalVolume }
                    val totalSets = breakdowns.sumOf { it.setsCount }
                    _uiState.update {
                        it.copy(
                            participantBreakdowns = breakdowns,
                            totalVolumeKg = totalVol,
                            totalSetsCount = totalSets,
                        )
                    }
                }
        }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val sessionId: Long,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SummaryViewModel(workoutRepository, sessionId) as T
    }
}
