package com.example.azm_gymtracker.presentation

import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import com.example.azm_gymtracker.presentation.exercises.ExercisesViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeExerciseRepository
    private lateinit var viewModel: ExercisesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeExerciseRepository()
        viewModel = ExercisesViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsExercisesAndFiltersByCategory() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val ex1 = Exercise(id = 1, nameAr = "بنش برس مستوي", nameEn = "Bench Press", category = MuscleCategory.CHEST)
        val ex2 = Exercise(id = 2, nameAr = "سكوات", nameEn = "Squat", category = MuscleCategory.LEGS)
        fakeRepository.emitExercises(listOf(ex1, ex2))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.exercises.size)
        assertEquals(2, state.filteredExercises.size)

        viewModel.onCategorySelected(MuscleCategory.CHEST)
        advanceUntilIdle()

        val filteredState = viewModel.uiState.value
        assertEquals(1, filteredState.filteredExercises.size)
        assertEquals(1L, filteredState.filteredExercises.first().id)
    }

    @Test
    fun searchExercises_filtersByArabicOrEnglishName() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val ex1 = Exercise(id = 1, nameAr = "بنش برس مستوي", nameEn = "Bench Press", category = MuscleCategory.CHEST)
        val ex2 = Exercise(id = 2, nameAr = "سكوات بالبار", nameEn = "Barbell Squat", category = MuscleCategory.LEGS)
        fakeRepository.emitExercises(listOf(ex1, ex2))
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Squat")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredExercises.size)
        assertEquals("سكوات بالبار", viewModel.uiState.value.filteredExercises.first().nameAr)

        viewModel.onSearchQueryChanged("بنش")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredExercises.size)
        assertEquals("بنش برس مستوي", viewModel.uiState.value.filteredExercises.first().nameAr)
    }

    @Test
    fun createCustomExercise_withValidData_callsRepository() = runTest(testDispatcher) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        var created: Exercise? = null
        viewModel.createCustomExercise(
            nameAr = "تمرين مخصص",
            nameEn = "Custom Exercise",
            category = MuscleCategory.BACK,
            onSuccess = { created = it },
        )
        advanceUntilIdle()

        assertNotNull(created)
        assertEquals(1, fakeRepository.createdExercises.size)
        assertEquals("تمرين مخصص", fakeRepository.createdExercises.first().nameAr)
        assertEquals(MuscleCategory.BACK, fakeRepository.createdExercises.first().category)
    }

    private class FakeExerciseRepository : ExerciseRepository {
        private val flow = MutableStateFlow<List<Exercise>>(emptyList())
        val createdExercises = mutableListOf<Exercise>()

        fun emitExercises(list: List<Exercise>) {
            flow.value = list
        }

        override fun getAllExercises(): Flow<List<Exercise>> = flow.asStateFlow()
        override fun getExercisesByCategory(category: MuscleCategory): Flow<List<Exercise>> = flow.asStateFlow()
        override fun searchExercises(query: String): Flow<List<Exercise>> = flow.asStateFlow()

        override suspend fun createCustomExercise(exercise: Exercise): Long {
            createdExercises.add(exercise)
            val updated = flow.value + exercise.copy(id = (createdExercises.size).toLong())
            flow.value = updated
            return updated.last().id
        }
    }
}
