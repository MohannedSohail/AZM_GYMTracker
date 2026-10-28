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
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.presentation.summary.SummaryViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SummaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var viewModel: SummaryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkoutRepository = FakeWorkoutRepository()
        viewModel = SummaryViewModel(
            workoutRepository = fakeWorkoutRepository,
            sessionId = 100L,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadSummary_computesAggregatesAndParticipantBreakdown() = runTest(testDispatcher) {
        val sessionEntity = WorkoutSessionEntity(
            id = 100L,
            startedAt = 1000L,
            endedAt = 1000L + (42 * 60 + 18) * 1000L, // 42m 18s
            status = "COMPLETED",
        )
        val details = WorkoutSessionWithDetails(
            session = sessionEntity,
            participants = listOf(
                MemberEntity(id = 1L, name = "Ahmed", colorHex = "#3FE779"),
                MemberEntity(id = 2L, name = "Mohanned", colorHex = "#8CFF68"),
            ),
            exercises = listOf(
                WorkoutExerciseWithSets(
                    workoutExercise = WorkoutExerciseEntity(
                        id = 10,
                        sessionId = 100,
                        exerciseId = 5,
                        exerciseNameSnapshot = "Bench",
                        orderIndex = 0,
                    ),
                    catalogExercise = ExerciseEntity(id = 5, nameAr = "بنش", nameEn = "Bench", category = "CHEST"),
                    sets = listOf(
                        WorkoutSetEntity(id = 1, workoutExerciseId = 10, memberId = 1L, setNumber = 1, reps = 10, weightKg = 100f, isCompleted = true),
                        WorkoutSetEntity(id = 2, workoutExerciseId = 10, memberId = 2L, setNumber = 2, reps = 8, weightKg = 80f, isCompleted = true),
                    ),
                ),
            ),
        )
        val volumeBreakdowns = listOf(
            ParticipantVolumeTuple(memberId = 1L, totalVolume = 1000.0, setsCount = 1),
            ParticipantVolumeTuple(memberId = 2L, totalVolume = 640.0, setsCount = 1),
        )
        fakeWorkoutRepository.emitSessionWithDetails(details)
        fakeWorkoutRepository.emitParticipantVolumeBreakdown(volumeBreakdowns)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.totalSetsCount)
        assertEquals(1, state.totalExercisesCount)
        assertEquals(42L, state.durationMinutes) // 42m
        assertEquals(1640.0, state.totalVolumeKg, 0.01) // 10*100 + 8*80
        assertEquals(2, state.participantBreakdowns.size)

        val p1 = state.participantBreakdowns.find { it.memberId == 1L }
        assertEquals(1000.0, p1?.totalVolume ?: 0.0, 0.01)
        assertEquals(1, p1?.setsCount)

        val p2 = state.participantBreakdowns.find { it.memberId == 2L }
        assertEquals(640.0, p2?.totalVolume ?: 0.0, 0.01)
        assertEquals(1, p2?.setsCount)
    }

    private class FakeWorkoutRepository : WorkoutRepository {
        private val sessionWithDetailsFlow = MutableStateFlow<WorkoutSessionWithDetails?>(null)
        private val breakdownsFlow = MutableStateFlow<List<ParticipantVolumeTuple>>(emptyList())

        fun emitSessionWithDetails(details: WorkoutSessionWithDetails?) {
            sessionWithDetailsFlow.value = details
        }

        fun emitParticipantVolumeBreakdown(list: List<ParticipantVolumeTuple>) {
            breakdownsFlow.value = list
        }

        override fun getActiveSession(): Flow<WorkoutSession?> = MutableStateFlow(null)
        override fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?> = MutableStateFlow(null)
        override fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?> = sessionWithDetailsFlow.asStateFlow()
        override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> = MutableStateFlow(emptyList())
        override fun getCompletedSessionsWithDetails(memberId: Long?): Flow<List<WorkoutSessionWithDetails>> = MutableStateFlow(emptyList())

        override suspend fun startSession(participantIds: List<Long>): Long = 1L
        override suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long = 1L
        override suspend fun removeExerciseFromSession(workoutExerciseId: Long) {}
        override suspend fun updateSessionStartedAt(sessionId: Long, startedAt: Long) {}
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
        override fun getParticipantVolumeBreakdown(sessionId: Long): Flow<List<ParticipantVolumeTuple>> = breakdownsFlow.asStateFlow()
        override fun getPersonalRecordsForMember(memberId: Long) = MutableStateFlow(emptyList<ExerciseRecordTuple>())
        override fun getTotalVolumeForMember(memberId: Long) = MutableStateFlow<Double?>(0.0)
        override fun getWeightProgression(memberId: Long, exerciseId: Long) = MutableStateFlow(emptyList<WeightProgressionPointTuple>())
    }
}
