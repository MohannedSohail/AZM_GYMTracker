package com.example.azm_gymtracker.domain.repository

import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for exercise catalog operations.
 */
interface ExerciseRepository {

    /** Observes all exercises in the catalog ordered by English name. */
    fun getAllExercises(): Flow<List<Exercise>>

    /** Observes exercises filtered by [category]. */
    fun getExercisesByCategory(category: MuscleCategory): Flow<List<Exercise>>

    /**
     * Observes exercises matching [query] in either English or Arabic name.
     * Case-insensitive search.
     */
    fun searchExercises(query: String): Flow<List<Exercise>>

    /**
     * Creates a new custom exercise.
     * [creatorMemberId] must reference an existing member.
     */
    suspend fun createCustomExercise(exercise: Exercise): Long
}
