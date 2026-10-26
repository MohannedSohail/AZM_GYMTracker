package com.example.azm_gymtracker.presentation

import com.example.azm_gymtracker.domain.model.SessionStatus
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import com.example.azm_gymtracker.presentation.history.HistoryViewModel
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeWorkoutRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWorkoutRepository()
        viewModel = HistoryViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadHistory_emitsCompletedSessionsForMember() = runTest(testDispatcher) {
        val s1 = WorkoutSession(id = 10, memberId = 1, startedAt = 1000, finishedAt = 2000, status = SessionStatus.COMPLETED)
        val s2 = WorkoutSession(id = 11, memberId = 1, startedAt = 3000, finishedAt = 4000, status = SessionStatus.COMPLETED)
        fakeRepository.emitCompletedSessions(1, listOf(s2, s1))

        viewModel.loadHistory(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.sessions.size)
        assertEquals(11, state.sessions.first().id)
        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun selectSession_updatesSelectedSessionInState() = runTest(testDispatcher) {
        val s1 = WorkoutSession(id = 10, memberId = 1, startedAt = 1000, finishedAt = 2000, status = SessionStatus.COMPLETED)
        viewModel.selectSession(s1)

        assertEquals(s1, viewModel.uiState.value.selectedSession)

        viewModel.selectSession(null)
        assertNull(viewModel.uiState.value.selectedSession)
    }

    @Test
    fun deleteSession_callsRepositoryAndClearsSelectedIfMatched() = runTest(testDispatcher) {
        val s1 = WorkoutSession(id = 10, memberId = 1, startedAt = 1000, finishedAt = 2000, status = SessionStatus.COMPLETED)
        fakeRepository.emitCompletedSessions(1, listOf(s1))
        viewModel.loadHistory(1)
        advanceUntilIdle()

        viewModel.selectSession(s1)
        assertEquals(s1, viewModel.uiState.value.selectedSession)

        viewModel.deleteSession(10)
        advanceUntilIdle()

        assertEquals(listOf(10L), fakeRepository.deletedSessionIds)
        assertNull(viewModel.uiState.value.selectedSession)
    }

    private class FakeWorkoutRepository : WorkoutRepository {
        private val completedFlow = MutableStateFlow<List<WorkoutSession>>(emptyList())
        val deletedSessionIds = mutableListOf<Long>()

        fun emitCompletedSessions(memberId: Long, sessions: List<WorkoutSession>) {
            completedFlow.value = sessions
        }

        override fun getActiveSession(memberId: Long): Flow<WorkoutSession?> =
            MutableStateFlow(null)

        override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> =
            completedFlow.asStateFlow()

        override suspend fun startSession(memberId: Long): Long = 1L
        override suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long = 1L
        override suspend fun addSet(workoutExerciseId: Long, reps: Int, weightKg: Float?): Long = 1L
        override suspend fun updateSet(set: WorkoutSet) {}
        override suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {}
        override suspend fun finishSession(sessionId: Long) {}
        override suspend fun discardSession(sessionId: Long) {}
        override suspend fun deleteSession(sessionId: Long) {
            deletedSessionIds.add(sessionId)
            completedFlow.value = completedFlow.value.filter { it.id != sessionId }
        }
    }
}
