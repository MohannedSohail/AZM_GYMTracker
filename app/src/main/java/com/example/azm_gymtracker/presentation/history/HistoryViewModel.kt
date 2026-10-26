package com.example.azm_gymtracker.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.data.local.entity.toDomain
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the History screen.
 *
 * Supports multi-participant filtering, rich session details with nested exercises
 * and sets, expandable cards, and undoable session deletion.
 */
data class HistoryUiState(
    val members: List<Member> = emptyList(),
    val selectedMemberId: Long? = null,
    val sessionsWithDetails: List<WorkoutSessionWithDetails> = emptyList(),
    val sessions: List<WorkoutSession> = emptyList(),
    val expandedSessionIds: Set<Long> = emptySet(),
    val selectedSession: WorkoutSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel responsible for managing and displaying workout history.
 *
 * Adheres to UDF: exposes immutable [uiState] and accepts user intents.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    private val _expandedSessionIds = MutableStateFlow<Set<Long>?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _selectedSession = MutableStateFlow<WorkoutSession?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        memberRepository.getAllMembers(),
        _selectedMemberId,
        _selectedMemberId.flatMapLatest { memberId ->
            workoutRepository.getCompletedSessionsWithDetails(memberId)
        },
        _expandedSessionIds,
        _selectedSession,
    ) { members, selectedMemberId, sessions, expandedIds, selectedSession ->
        HistoryUiState(
            members = members,
            selectedMemberId = selectedMemberId,
            sessionsWithDetails = sessions,
            sessions = sessions.map { it.toDomain() },
            // By default expand the first session if nothing has been toggled yet
            expandedSessionIds = expandedIds ?: if (sessions.isNotEmpty()) {
                setOf(sessions.first().session.id)
            } else {
                emptySet()
            },
            selectedSession = selectedSession,
            isLoading = false,
            errorMessage = _errorMessage.value,
        )
    }.catch { cause ->
        emit(
            HistoryUiState(
                isLoading = false,
                errorMessage = cause.message ?: "Failed to load history",
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(isLoading = true),
    )

    /**
     * Filters workout history by [memberId], or null to view all sessions.
     */
    fun filterByMember(memberId: Long?) {
        _selectedMemberId.value = memberId
    }

    /**
     * Legacy support method to load history for [memberId].
     */
    fun loadHistory(memberId: Long) {
        filterByMember(memberId)
    }

    /**
     * Toggles expansion of a workout session card.
     */
    fun toggleSessionExpanded(sessionId: Long) {
        _expandedSessionIds.update { current ->
            val active = current ?: (uiState.value.sessionsWithDetails.firstOrNull()?.session?.id?.let { setOf(it) } ?: emptySet())
            if (active.contains(sessionId)) {
                active - sessionId
            } else {
                active + sessionId
            }
        }
    }

    /**
     * Selects a session for detailed inspection, or null to close detail view.
     */
    fun selectSession(session: WorkoutSession?) {
        _selectedSession.value = session
    }

    /**
     * Deletes a completed session permanently from history.
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteSession(sessionId)
                _expandedSessionIds.update { (it ?: emptySet()) - sessionId }
                if (_selectedSession.value?.id == sessionId) {
                    _selectedSession.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to delete session"
            }
        }
    }

    /**
     * Clears any active error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Factory for creating [HistoryViewModel] instances without reflection or external DI.
     */
    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val memberRepository: MemberRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                return HistoryViewModel(workoutRepository, memberRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
