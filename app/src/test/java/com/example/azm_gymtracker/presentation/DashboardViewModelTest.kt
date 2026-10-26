package com.example.azm_gymtracker.presentation

import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import com.example.azm_gymtracker.data.local.entity.ExerciseRecordTuple
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple
import com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseWithSets
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeMemberRepository: FakeMemberRepository
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeMemberRepository = FakeMemberRepository()
        fakeWorkoutRepository = FakeWorkoutRepository()
        viewModel = DashboardViewModel(fakeMemberRepository, fakeWorkoutRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsMembersAndActiveSession() = runTest(testDispatcher) {
        val member = Member(id = 1, name = "Mohanned", colorHex = "#3FE779")
        fakeMemberRepository.emitMembers(listOf(member))

        val activeDetails = WorkoutSessionWithDetails(
            session = WorkoutSessionEntity(id = 99L, startedAt = 1000L, status = "IN_PROGRESS"),
            participants = listOf(MemberEntity(id = 1L, name = "Mohanned", colorHex = "#3FE779")),
            exercises = listOf(
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExerciseEntity(
                        id = 1,
                        sessionId = 99,
                        exerciseId = 10,
                        exerciseNameSnapshot = "Bench",
                        orderIndex = 0,
                    ),
                    catalogExercise = ExerciseEntity(id = 10, nameAr = "بنش", nameEn = "Bench", category = "CHEST"),
                    sets = emptyList(),
                ),
            ),
        )
        fakeWorkoutRepository.emitActiveSessionWithDetails(activeDetails)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.allMembers.size)
        assertEquals(member, state.activeMember)
        assertNotNull(state.activeSession)
        assertEquals(99L, state.activeSession?.session?.id)
        assertEquals(1, state.activeSession?.exercises?.size)
    }

    @Test
    fun selectActiveMember_persistsActiveMemberId() = runTest(testDispatcher) {
        val m1 = Member(id = 1, name = "Mohanned", colorHex = "#3FE779")
        val m2 = Member(id = 2, name = "Ahmed", colorHex = "#8CFF68")
        fakeMemberRepository.emitMembers(listOf(m1, m2))
        advanceUntilIdle()

        viewModel.selectActiveMember(m2.id)
        advanceUntilIdle()

        assertEquals(2L, fakeMemberRepository.activeIdFlow.value)
    }

    private class FakeMemberRepository : MemberRepository {
        private val membersFlow = MutableStateFlow<List<Member>>(emptyList())
        val activeIdFlow = MutableStateFlow<Long?>(null)

        fun emitMembers(members: List<Member>) {
            membersFlow.value = members
        }

        override fun getAllMembers(): Flow<List<Member>> = membersFlow.asStateFlow()
        override suspend fun getMemberCount(): Int = membersFlow.value.size
        override suspend fun createMember(member: Member): Long = 1L
        override suspend fun updateMember(member: Member) {}
        override suspend fun deleteMember(member: Member) {}
        override fun getActiveMemberId(): Flow<Long?> = activeIdFlow.asStateFlow()
        override suspend fun setActiveMemberId(memberId: Long) {
            activeIdFlow.value = memberId
        }
    }

    private class FakeWorkoutRepository : WorkoutRepository {
        val activeDetailsFlow = MutableStateFlow<WorkoutSessionWithDetails?>(null)
        val completedWithDetailsFlow = MutableStateFlow<List<WorkoutSessionWithDetails>>(emptyList())

        fun emitActiveSessionWithDetails(details: WorkoutSessionWithDetails?) {
            activeDetailsFlow.value = details
        }

        override fun getActiveSession(): Flow<WorkoutSession?> = MutableStateFlow(null)
        override fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?> = activeDetailsFlow.asStateFlow()
        override fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?> = activeDetailsFlow.asStateFlow()
        override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> = MutableStateFlow(emptyList())
        override fun getCompletedSessionsWithDetails(memberId: Long?): Flow<List<WorkoutSessionWithDetails>> = completedWithDetailsFlow.asStateFlow()

        override suspend fun startSession(participantIds: List<Long>): Long = 1L
        override suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long = 1L
        override suspend fun addSet(workoutExerciseId: Long, memberId: Long, reps: Int, weightKg: Float?): Long = 1L
        override suspend fun markSetCompleted(setId: Long) {}
        override suspend fun updateSet(set: WorkoutSet) {}
        override suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {}
        override suspend fun finishSession(sessionId: Long) {}
        override suspend fun cancelSession(sessionId: Long) {}
        override suspend fun deleteSession(sessionId: Long) {}
        override suspend fun updateSessionPausedTime(sessionId: Long, pausedTotalMs: Long) {}
        override suspend fun getLastSetForMemberInExercise(exerciseId: Long, memberId: Long) = null
        override suspend fun getLastSetInExercise(exerciseId: Long) = null
        override fun getParticipantVolumeBreakdown(sessionId: Long) = MutableStateFlow(emptyList<ParticipantVolumeTuple>())
        override fun getPersonalRecordsForMember(memberId: Long) = MutableStateFlow(emptyList<ExerciseRecordTuple>())
        override fun getTotalVolumeForMember(memberId: Long) = MutableStateFlow<Double?>(0.0)
        override fun getWeightProgression(memberId: Long, exerciseId: Long) = MutableStateFlow(emptyList<WeightProgressionPointTuple>())
    }
}
