package com.example.azm_gymtracker.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable UI state for DashboardScreen (Figma #87:249).
 */
data class DashboardUiState(
    val activeMember: Member? = null,
    val allMembers: List<Member> = emptyList(),
    val activeSession: WorkoutSessionWithDetails? = null,
    val recentSessions: List<WorkoutSession> = emptyList(),
    val totalVolume: Double = 0.0,
    val completedSessionsCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

/**
 * ViewModel for the main Dashboard screen.
 */
class DashboardViewModel(
    private val memberRepository: MemberRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeMembersAndActiveSession()
    }

    private fun observeMembersAndActiveSession() {
        viewModelScope.launch {
            // Combine all members and active member ID
            combine(
                memberRepository.getAllMembers(),
                memberRepository.getActiveMemberId(),
                workoutRepository.getActiveSessionWithDetails(),
            ) { members, activeId, activeSession ->
                val activeMember = members.find { it.id == activeId } ?: members.firstOrNull()
                Triple(members, activeMember, activeSession)
            }.catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }.collect { (members, activeMember, activeSession) ->
                _uiState.update {
                    it.copy(
                        allMembers = members,
                        activeMember = activeMember,
                        activeSession = activeSession,
                        isLoading = false,
                    )
                }

                // If active member changed or loaded, load their recent sessions & stats
                if (activeMember != null) {
                    loadMemberStats(activeMember.id)
                }
            }
        }
    }

    private fun loadMemberStats(memberId: Long) {
        viewModelScope.launch {
            workoutRepository.getCompletedSessions(memberId)
                .catch { /* ignore */ }
                .collect { sessions ->
                    _uiState.update {
                        it.copy(
                            recentSessions = sessions.take(5),
                            completedSessionsCount = sessions.size,
                        )
                    }
                }
        }
        viewModelScope.launch {
            workoutRepository.getTotalVolumeForMember(memberId)
                .catch { /* ignore */ }
                .collect { volume ->
                    _uiState.update { it.copy(totalVolume = volume ?: 0.0) }
                }
        }
    }

    fun selectActiveMember(memberId: Long) {
        viewModelScope.launch {
            memberRepository.setActiveMemberId(memberId)
        }
    }

    class Factory(
        private val memberRepository: MemberRepository,
        private val workoutRepository: WorkoutRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DashboardViewModel(memberRepository, workoutRepository) as T
    }
}
