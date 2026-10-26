package com.example.azm_gymtracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the History screen and History Detail dialog/screen.
 */
data class HistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val selectedSession: WorkoutSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel responsible for managing member workout history.
 *
 * Adheres to UDF: exposes immutable [uiState] and accepts user intents.
 */
class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var sessionObservationJob: Job? = null
    private var currentMemberId: Long? = null

    /**
     * Subscribes to completed workout sessions for [memberId], ordered newest first.
     * Cancels any previous observation.
     */
    fun loadHistory(memberId: Long) {
        if (currentMemberId == memberId && sessionObservationJob?.isActive == true) return
        currentMemberId = memberId

        sessionObservationJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        sessionObservationJob = viewModelScope.launch {
            workoutRepository.getCompletedSessions(memberId)
                .catch { cause ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = cause.message ?: "Failed to load history")
                    }
                }
                .collect { sessions ->
                    _uiState.update { current ->
                        current.copy(
                            sessions = sessions,
                            isLoading = false,
                            // If currently selected session was modified or deleted, update it
                            selectedSession = current.selectedSession?.let { sel ->
                                sessions.find { it.id == sel.id }
                            },
                        )
                    }
                }
        }
    }

    /**
     * Selects a session for detailed inspection, or null to close detail view.
     */
    fun selectSession(session: WorkoutSession?) {
        _uiState.update { it.copy(selectedSession = session) }
    }

    /**
     * Deletes a completed session permanently from history.
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteSession(sessionId)
                _uiState.update { current ->
                    if (current.selectedSession?.id == sessionId) {
                        current.copy(selectedSession = null)
                    } else {
                        current
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete session") }
            }
        }
    }

    /**
     * Clears any active error message.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Factory for creating [HistoryViewModel] instances without reflection or external DI.
     */
    class Factory(
        private val workoutRepository: WorkoutRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(workoutRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
