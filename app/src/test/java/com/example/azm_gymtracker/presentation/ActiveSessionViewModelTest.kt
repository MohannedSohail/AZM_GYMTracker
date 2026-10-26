package com.example.azm_gymtracker.presentation

import com.example.azm_gymtracker.data.local.entity.ExerciseRecordTuple
import com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple
import com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.presentation.session.ActiveSessionViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeWorkoutRepository: FakeWorkoutRepository
    private lateinit var fakeExerciseRepository: FakeExerciseRepository
    private lateinit var viewModel: ActiveSessionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWorkoutRepository = FakeWorkoutRepository()
        fakeExerciseRepository = FakeExerciseRepository()
        viewModel = ActiveSessionViewModel(
            workoutRepository = fakeWorkoutRepository,
            exerciseRepository = fakeExerciseRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stepperAdjustments_respectExactStepSizes() = runTest(testDispatcher) {
        // Initial defaults are 70.0 kg, 8 reps
        viewModel.adjustWeight(2.5f)
        assertEquals(72.5f, viewModel.uiState.value.weightStepper, 0.001f)

        viewModel.adjustWeight(-2.5f)
        assertEquals(70.0f, viewModel.uiState.value.weightStepper, 0.001f)

        viewModel.adjustReps(1)
        assertEquals(9, viewModel.uiState.value.repsStepper)

        viewModel.adjustReps(-1)
        assertEquals(8, viewModel.uiState.value.repsStepper)
    }

    @Test
    fun adjustWeight_doesNotDropBelowZero() = runTest(testDispatcher) {
        viewModel.adjustWeight(-100f)
        assertEquals(0.0f, viewModel.uiState.value.weightStepper, 0.001f)
    }

    @Test
    fun adjustReps_doesNotDropBelowOne() = runTest(testDispatcher) {
        viewModel.adjustReps(-50)
        assertEquals(1, viewModel.uiState.value.repsStepper)
    }

    @Test
    fun restTimer_adjustAndSkip() = runTest(testDispatcher) {
        viewModel.adjustRestTimer(30)
        assertEquals(120, viewModel.uiState.value.restTimerRemainingSeconds)

        viewModel.skipRestTimer()
        assertFalse(viewModel.uiState.value.isRestTimerVisible)
    }

    @Test
    fun finishSession_callsRepositoryAndSetsFinishedState() = runTest(testDispatcher) {
        val sessionEntity = WorkoutSessionEntity(id = 42L, startedAt = 1000L, status = "IN_PROGRESS")
        val details = WorkoutSessionWithDetails(
            session = sessionEntity,
            participants = emptyList(),
            exercises = emptyList(),
        )
        fakeWorkoutRepository.emitActiveSessionWithDetails(details)
        testScheduler.runCurrent()

        viewModel.finishSession()
        testScheduler.runCurrent()

        assertEquals(listOf(42L), fakeWorkoutRepository.finishedSessionIds)
        assertTrue(viewModel.uiState.value.isSessionFinished)
    }

    @Test
    fun cancelSession_callsRepositoryAndSetsCancelledState() = runTest(testDispatcher) {
        val sessionEntity = WorkoutSessionEntity(id = 42L, startedAt = 1000L, status = "IN_PROGRESS")
        val details = WorkoutSessionWithDetails(
            session = sessionEntity,
            participants = emptyList(),
            exercises = emptyList(),
        )
        fakeWorkoutRepository.emitActiveSessionWithDetails(details)
        testScheduler.runCurrent()

        viewModel.cancelSession()
        testScheduler.runCurrent()

        assertEquals(listOf(42L), fakeWorkoutRepository.cancelledSessionIds)
        assertTrue(viewModel.uiState.value.isSessionCancelled)
    }

    private class FakeWorkoutRepository : WorkoutRepository {
        val detailsFlow = MutableStateFlow<WorkoutSessionWithDetails?>(null)
        val finishedSessionIds = mutableListOf<Long>()
        val cancelledSessionIds = mutableListOf<Long>()
        val addedExercises = mutableListOf<Triple<Long, Long, String>>()

        fun emitActiveSessionWithDetails(details: WorkoutSessionWithDetails?) {
            detailsFlow.value = details
        }

        override fun getActiveSession(): Flow<WorkoutSession?> = MutableStateFlow(null)
        override fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?> = detailsFlow.asStateFlow()
        override fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?> = detailsFlow.asStateFlow()
        override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> = MutableStateFlow(emptyList())
        override fun getCompletedSessionsWithDetails(memberId: Long?): Flow<List<WorkoutSessionWithDetails>> = MutableStateFlow(emptyList())

        override suspend fun startSession(participantIds: List<Long>): Long = 1L

        override suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long {
            addedExercises.add(Triple(sessionId, exerciseId, exerciseNameSnapshot))
            return 101L
        }

        override suspend fun addSet(workoutExerciseId: Long, memberId: Long, reps: Int, weightKg: Float?): Long = 201L
        override suspend fun markSetCompleted(setId: Long) {}
        override suspend fun updateSet(set: WorkoutSet) {}
        override suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {}

        override suspend fun finishSession(sessionId: Long) {
            finishedSessionIds.add(sessionId)
        }

        override suspend fun cancelSession(sessionId: Long) {
            cancelledSessionIds.add(sessionId)
        }

        override suspend fun deleteSession(sessionId: Long) {}
        override suspend fun updateSessionPausedTime(sessionId: Long, pausedTotalMs: Long) {}
        override suspend fun getLastSetForMemberInExercise(exerciseId: Long, memberId: Long) = null
        override suspend fun getLastSetInExercise(exerciseId: Long) = null
        override fun getParticipantVolumeBreakdown(sessionId: Long) = MutableStateFlow(emptyList<ParticipantVolumeTuple>())
        override fun getPersonalRecordsForMember(memberId: Long) = MutableStateFlow(emptyList<ExerciseRecordTuple>())
        override fun getTotalVolumeForMember(memberId: Long) = MutableStateFlow<Double?>(0.0)
        override fun getWeightProgression(memberId: Long, exerciseId: Long) = MutableStateFlow(emptyList<WeightProgressionPointTuple>())
    }

    private class FakeExerciseRepository : ExerciseRepository {
        private val exercisesFlow = MutableStateFlow<List<Exercise>>(emptyList())

        override fun getAllExercises(): Flow<List<Exercise>> = exercisesFlow.asStateFlow()
        override fun getExercisesByCategory(category: MuscleCategory): Flow<List<Exercise>> = exercisesFlow.asStateFlow()
        override fun searchExercises(query: String): Flow<List<Exercise>> = exercisesFlow.asStateFlow()
        override suspend fun createCustomExercise(exercise: Exercise): Long = 1L
    }
}
