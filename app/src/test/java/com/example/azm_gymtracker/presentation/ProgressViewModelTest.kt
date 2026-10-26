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
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.presentation.progress.ProgressPeriod
import com.example.azm_gymtracker.presentation.progress.ProgressViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeMemberRepository: FakeMemberRepository
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var viewModel: ProgressViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeMemberRepository = FakeMemberRepository()
        fakeWorkoutRepository = FakeWorkoutRepository()
        viewModel = ProgressViewModel(fakeMemberRepository, fakeWorkoutRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_calculatesMemberStatsAccurately() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val member = Member(id = 1L, name = "Mohanned", colorHex = "#3FE779")
        fakeMemberRepository.emitMembers(listOf(member))

        val now = System.currentTimeMillis()
        val session = WorkoutSessionEntity(id = 10L, startedAt = now - 1000L, endedAt = now, status = "COMPLETED")
        val details = WorkoutSessionWithDetails(
            session = session,
            participants = listOf(MemberEntity(id = 1L, name = "Mohanned", colorHex = "#3FE779")),
            exercises = listOf(
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExerciseEntity(
                        id = 1,
                        sessionId = 10,
                        exerciseId = 100,
                        exerciseNameSnapshot = "Bench",
                        orderIndex = 0,
                    ),
                    catalogExercise = ExerciseEntity(id = 100, nameAr = "بنش", nameEn = "Bench", category = "CHEST"),
                    sets = listOf(
                        WorkoutSetEntity(id = 1, workoutExerciseId = 1, memberId = 1L, setNumber = 1, reps = 10, weightKg = 70f, isCompleted = true, completedAt = now - 500L),
                        WorkoutSetEntity(id = 2, workoutExerciseId = 1, memberId = 1L, setNumber = 2, reps = 8, weightKg = 80f, isCompleted = true, completedAt = now - 200L),
                    ),
                ),
            ),
        )
        fakeWorkoutRepository.emitCompletedSessionsWithDetails(listOf(details))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(member, state.activeMember)
        assertEquals(1, state.sessionsCount)
        assertEquals(2, state.setsCount)
        assertEquals(80f, state.maxWeightKg)
        assertEquals(1340.0, state.totalVolumeKg, 0.01) // 10*70 + 8*80 = 700 + 640 = 1340
        assertEquals(2, state.weightProgressionPoints.size)
        assertEquals(10f, state.weightDeltaKg) // 80 - 70
    }

    @Test
    fun periodFilter_excludesOlderSessions() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val member = Member(id = 1L, name = "Mohanned", colorHex = "#3FE779")
        fakeMemberRepository.emitMembers(listOf(member))

        val now = System.currentTimeMillis()
        // Session from 15 days ago
        val oldSession = WorkoutSessionEntity(id = 5L, startedAt = now - 15L * 24 * 60 * 60 * 1000, endedAt = now, status = "COMPLETED")
        val oldDetails = WorkoutSessionWithDetails(
            session = oldSession,
            participants = listOf(MemberEntity(id = 1L, name = "Mohanned", colorHex = "#3FE779")),
            exercises = listOf(
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExerciseEntity(
                        id = 1,
                        sessionId = 5,
                        exerciseId = 100,
                        exerciseNameSnapshot = "Bench",
                        orderIndex = 0,
                    ),
                    catalogExercise = ExerciseEntity(id = 100, nameAr = "بنش", nameEn = "Bench", category = "CHEST"),
                    sets = listOf(
                        WorkoutSetEntity(id = 1, workoutExerciseId = 1, memberId = 1L, setNumber = 1, reps = 10, weightKg = 60f, isCompleted = true),
                    ),
                ),
            ),
        )
        fakeWorkoutRepository.emitCompletedSessionsWithDetails(listOf(oldDetails))
        advanceUntilIdle()

        // In WEEK period, session from 15 days ago should be excluded
        viewModel.selectPeriod(ProgressPeriod.WEEK)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.sessionsCount)
        assertEquals(0, viewModel.uiState.value.setsCount)

        // In MONTH period, session from 15 days ago should be included
        viewModel.selectPeriod(ProgressPeriod.MONTH)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.sessionsCount)
        assertEquals(1, viewModel.uiState.value.setsCount)
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
        private val completedWithDetailsFlow = MutableStateFlow<List<WorkoutSessionWithDetails>>(emptyList())

        fun emitCompletedSessionsWithDetails(list: List<WorkoutSessionWithDetails>) {
            completedWithDetailsFlow.value = list
        }

        override fun getActiveSession(): Flow<WorkoutSession?> = MutableStateFlow(null)
        override fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?> = MutableStateFlow(null)
        override fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?> = MutableStateFlow(null)
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
