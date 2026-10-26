package com.example.azm_gymtracker.presentation

import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.model.SessionStatus
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.presentation.history.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var fakeMemberRepository: FakeMemberRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkoutRepository = FakeWorkoutRepository()
        fakeMemberRepository = FakeMemberRepository()
        viewModel = HistoryViewModel(fakeWorkoutRepository, fakeMemberRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsSessionsAndMembers() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val member = Member(id = 1, name = "Ahmed", colorHex = "#3FE779")
        fakeMemberRepository.emitMembers(listOf(member))

        val s1 = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 10, startedAt = 1000, endedAt = 2000, status = "COMPLETED"),
            participants = emptyList(),
            exercises = emptyList(),
        )
        fakeWorkoutRepository.emitCompletedSessionsWithDetails(listOf(s1))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.members.size)
        assertEquals(1, state.sessionsWithDetails.size)
        assertEquals(10L, state.sessionsWithDetails.first().session.id)
        assertTrue(state.expandedSessionIds.contains(10L))
    }

    @Test
    fun toggleSessionExpanded_togglesSessionId() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val s1 = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 10, startedAt = 1000, endedAt = 2000, status = "COMPLETED"),
            participants = emptyList(),
            exercises = emptyList(),
        )
        fakeWorkoutRepository.emitCompletedSessionsWithDetails(listOf(s1))
        advanceUntilIdle()

        // Initially expanded
        assertTrue(viewModel.uiState.value.expandedSessionIds.contains(10L))

        viewModel.toggleSessionExpanded(10L)
        advanceUntilIdle()
        assertTrue(!viewModel.uiState.value.expandedSessionIds.contains(10L))

        viewModel.toggleSessionExpanded(10L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.expandedSessionIds.contains(10L))
    }

    @Test
    fun deleteSession_callsRepositoryAndRemovesFromExpanded() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val s1 = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 10, startedAt = 1000, endedAt = 2000, status = "COMPLETED"),
            participants = emptyList(),
            exercises = emptyList(),
        )
        fakeWorkoutRepository.emitCompletedSessionsWithDetails(listOf(s1))
        advanceUntilIdle()

        viewModel.deleteSession(10L)
        advanceUntilIdle()

        assertEquals(listOf(10L), fakeWorkoutRepository.deletedSessionIds)
        assertTrue(!viewModel.uiState.value.expandedSessionIds.contains(10L))
    }

    private class FakeWorkoutRepository : WorkoutRepository {
        private val completedWithDetailsFlow = MutableStateFlow<List<WorkoutSessionWithDetails>>(emptyList())
        val deletedSessionIds = mutableListOf<Long>()

        fun emitCompletedSessionsWithDetails(sessions: List<WorkoutSessionWithDetails>) {
            completedWithDetailsFlow.value = sessions
        }

        override fun getActiveSession(): Flow<WorkoutSession?> = MutableStateFlow(null)
        override fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?> = MutableStateFlow(null)
        override fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?> = MutableStateFlow(null)
        override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> = MutableStateFlow(emptyList())

        override fun getCompletedSessionsWithDetails(memberId: Long?): Flow<List<WorkoutSessionWithDetails>> =
            completedWithDetailsFlow.asStateFlow()

        override suspend fun startSession(participantIds: List<Long>): Long = 1L
        override suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long = 1L
        override suspend fun addSet(workoutExerciseId: Long, memberId: Long, reps: Int, weightKg: Float?): Long = 1L
        override suspend fun markSetCompleted(setId: Long) {}
        override suspend fun updateSet(set: WorkoutSet) {}
        override suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {}
        override suspend fun finishSession(sessionId: Long) {}
        override suspend fun cancelSession(sessionId: Long) {}
        override suspend fun deleteSession(sessionId: Long) {
            deletedSessionIds.add(sessionId)
            completedWithDetailsFlow.value = completedWithDetailsFlow.value.filter { it.session.id != sessionId }
        }
        override suspend fun updateSessionPausedTime(sessionId: Long, pausedTotalMs: Long) {}
        override suspend fun getLastSetForMemberInExercise(exerciseId: Long, memberId: Long) = null
        override suspend fun getLastSetInExercise(exerciseId: Long) = null
        override fun getParticipantVolumeBreakdown(sessionId: Long) = MutableStateFlow(emptyList<com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple>())
        override fun getPersonalRecordsForMember(memberId: Long) = MutableStateFlow(emptyList<com.example.azm_gymtracker.data.local.entity.ExerciseRecordTuple>())
        override fun getTotalVolumeForMember(memberId: Long) = MutableStateFlow<Double?>(0.0)
        override fun getWeightProgression(memberId: Long, exerciseId: Long) = MutableStateFlow(emptyList<com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple>())
    }

    private class FakeMemberRepository : MemberRepository {
        private val _flow = MutableStateFlow<List<Member>>(emptyList())
        val activeMemberIdFlow = MutableStateFlow<Long?>(null)

        fun emitMembers(list: List<Member>) {
            _flow.value = list
        }

        override fun getAllMembers(): Flow<List<Member>> = _flow.asStateFlow()
        override suspend fun getMemberCount(): Int = _flow.value.size
        override suspend fun createMember(member: Member): Long = 1L
        override suspend fun updateMember(member: Member) {}
        override suspend fun deleteMember(member: Member) {}
        override fun getActiveMemberId(): Flow<Long?> = activeMemberIdFlow.asStateFlow()
        override suspend fun setActiveMemberId(memberId: Long) {
            activeMemberIdFlow.value = memberId
        }
    }
}
