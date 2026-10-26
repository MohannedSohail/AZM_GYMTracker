package com.example.azm_gymtracker.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple
import com.example.azm_gymtracker.domain.model.Member
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProgressPeriod {
    WEEK, MONTH, THREE_MONTHS, ALL
}

/**
 * UI State for the Progress & Analytics Screen (Figma Node #87:859).
 */
data class ProgressUiState(
    val members: List<Member> = emptyList(),
    val activeMember: Member? = null,
    val selectedPeriod: ProgressPeriod = ProgressPeriod.MONTH,
    val setsCount: Int = 0,
    val sessionsCount: Int = 0,
    val maxWeightKg: Float = 0f,
    val totalVolumeKg: Double = 0.0,
    val weightProgressionPoints: List<WeightProgressionPointTuple> = emptyList(),
    val weightDeltaKg: Float = 0f,
    val weeklySessionsCount: List<Int> = List(7) { 0 }, // Sun..Sat session distribution
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel managing progress analytics, timeframe filtering, and progression curves.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(
    private val memberRepository: MemberRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _selectedMemberId = MutableStateFlow<Long?>(null)
    private val _selectedPeriod = MutableStateFlow(ProgressPeriod.MONTH)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProgressUiState> = combine(
        memberRepository.getAllMembers(),
        memberRepository.getActiveMemberId(),
        _selectedMemberId,
        _selectedPeriod,
    ) { members, persistedActiveId, selectedId, period ->
        val effectiveMemberId = selectedId ?: persistedActiveId ?: members.firstOrNull()?.id
        val activeMember = members.find { it.id == effectiveMemberId }
        MemberPeriodSelection(members, activeMember, period)
    }.flatMapLatest { selection ->
        val member = selection.activeMember
        if (member == null) {
            flowOf(
                ProgressUiState(
                    members = selection.members,
                    activeMember = null,
                    selectedPeriod = selection.period,
                    isLoading = false,
                ),
            )
        } else {
            workoutRepository.getCompletedSessionsWithDetails(member.id).map { sessions ->
                val now = System.currentTimeMillis()
                val periodStartMs = when (selection.period) {
                    ProgressPeriod.WEEK -> now - 7L * 24 * 60 * 60 * 1000
                    ProgressPeriod.MONTH -> now - 30L * 24 * 60 * 60 * 1000
                    ProgressPeriod.THREE_MONTHS -> now - 90L * 24 * 60 * 60 * 1000
                    ProgressPeriod.ALL -> 0L
                }

                // Filter sessions within period
                val periodSessions = sessions.filter { it.session.startedAt >= periodStartMs }
                val sessionCount = periodSessions.size

                // Extract all completed sets for this member in the period
                val completedSets = periodSessions.flatMap { sessionWithDetails ->
                    sessionWithDetails.exercises.flatMap { exerciseWithSets ->
                        exerciseWithSets.sets.filter { set ->
                            set.memberId == member.id && set.isCompleted
                        }
                    }
                }

                val setsCount = completedSets.size
                val maxWeight = completedSets.mapNotNull { it.weightKg }.maxOrNull() ?: 0f
                val totalVolume = completedSets.sumOf { set ->
                    (set.reps * (set.weightKg ?: 0f)).toDouble()
                }

                // Compute weight progression from all completed sets with weight, ordered by time
                val weightPoints = completedSets
                    .filter { it.weightKg != null && it.weightKg > 0f }
                    .sortedBy { it.completedAt ?: 0L }
                    .map {
                        WeightProgressionPointTuple(
                            timestamp = it.completedAt ?: 0L,
                            weightKg = it.weightKg ?: 0f,
                            reps = it.reps,
                        )
                    }

                val deltaKg = if (weightPoints.size >= 2) {
                    (weightPoints.last().weightKg ?: 0f) - (weightPoints.first().weightKg ?: 0f)
                } else {
                    0f
                }

                ProgressUiState(
                    members = selection.members,
                    activeMember = member,
                    selectedPeriod = selection.period,
                    setsCount = setsCount,
                    sessionsCount = sessionCount,
                    maxWeightKg = maxWeight,
                    totalVolumeKg = totalVolume,
                    weightProgressionPoints = weightPoints,
                    weightDeltaKg = deltaKg,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }.catch { cause ->
        emit(
            ProgressUiState(
                isLoading = false,
                errorMessage = cause.message ?: "Failed to load progress",
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState(isLoading = true),
    )

    fun selectMember(member: Member) {
        _selectedMemberId.value = member.id
        viewModelScope.launch {
            memberRepository.setActiveMemberId(member.id)
        }
    }

    fun selectPeriod(period: ProgressPeriod) {
        _selectedPeriod.value = period
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private data class MemberPeriodSelection(
        val members: List<Member>,
        val activeMember: Member?,
        val period: ProgressPeriod,
    )

    class Factory(
        private val memberRepository: MemberRepository,
        private val workoutRepository: WorkoutRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                return ProgressViewModel(memberRepository, workoutRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
