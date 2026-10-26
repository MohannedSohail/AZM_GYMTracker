package com.example.azm_gymtracker.presentation.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable UI state for the Exercise Catalog screen.
 */
data class ExercisesUiState(
    val exercises: List<Exercise> = emptyList(),
    val filteredExercises: List<Exercise> = emptyList(),
    val selectedCategory: MuscleCategory? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel for browsing, searching, filtering, and creating exercises.
 */
class ExercisesViewModel(
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<MuscleCategory?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExercisesUiState> = combine(
        exerciseRepository.getAllExercises(),
        _selectedCategory,
        _searchQuery,
        _isLoading,
        _errorMessage,
    ) { exercises, category, query, isLoading, error ->
        val filtered = exercises.filter { exercise ->
            val matchesCategory = category == null || exercise.category == category
            val matchesSearch = query.isBlank() ||
                exercise.nameAr.contains(query.trim(), ignoreCase = true) ||
                exercise.nameEn.contains(query.trim(), ignoreCase = true)
            matchesCategory && matchesSearch
        }
        ExercisesUiState(
            exercises = exercises,
            filteredExercises = filtered,
            selectedCategory = category,
            searchQuery = query,
            isLoading = false,
            errorMessage = error,
        )
    }.catch { cause ->
        emit(
            ExercisesUiState(
                isLoading = false,
                errorMessage = cause.message ?: "Failed to load exercises",
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExercisesUiState(isLoading = true),
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: MuscleCategory?) {
        _selectedCategory.value = category
    }

    fun createCustomExercise(
        nameAr: String,
        nameEn: String = "",
        category: MuscleCategory,
        creatorMemberId: Long? = null,
        onSuccess: (Exercise) -> Unit = {},
    ) {
        if (nameAr.isBlank() && nameEn.isBlank()) {
            _errorMessage.value = "Exercise name cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val newExercise = Exercise(
                    nameAr = nameAr.ifBlank { nameEn },
                    nameEn = nameEn.ifBlank { nameAr },
                    category = category,
                    isCustom = true,
                    creatorMemberId = creatorMemberId,
                )
                val id = exerciseRepository.createCustomExercise(newExercise)
                onSuccess(newExercise.copy(id = id))
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to create exercise"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    class Factory(
        private val exerciseRepository: ExerciseRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExercisesViewModel::class.java)) {
                return ExercisesViewModel(exerciseRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
