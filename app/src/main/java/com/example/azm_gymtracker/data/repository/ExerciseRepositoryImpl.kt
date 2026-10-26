package com.example.azm_gymtracker.data.repository

import com.example.azm_gymtracker.data.local.dao.ExerciseDao
import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [ExerciseRepository].
 */
class ExerciseRepositoryImpl(private val dao: ExerciseDao) : ExerciseRepository {

    override fun getAllExercises(): Flow<List<Exercise>> =
        dao.getAllExercises().map { entities -> entities.map { it.toDomain() } }

    override fun getExercisesByCategory(category: MuscleCategory): Flow<List<Exercise>> =
        dao.getExercisesByCategory(category.name).map { entities -> entities.map { it.toDomain() } }

    override fun searchExercises(query: String): Flow<List<Exercise>> =
        dao.searchExercises(query.trim()).map { entities -> entities.map { it.toDomain() } }

    override suspend fun createCustomExercise(exercise: Exercise): Long {
        require(exercise.isCustom) { "Exercise must be marked as custom." }
        require(exercise.nameEn.isNotBlank()) { "English name must not be blank." }
        require(exercise.nameAr.isNotBlank()) { "Arabic name must not be blank." }
        return dao.insertExercise(exercise.toEntity())
    }

    // ── Mapping ───────────────────────────────────────────────────────────────────

    private fun ExerciseEntity.toDomain() = Exercise(
        id = id,
        nameEn = nameEn,
        nameAr = nameAr,
        category = runCatching { MuscleCategory.valueOf(category) }.getOrDefault(MuscleCategory.OTHER),
        isCustom = isCustom,
        creatorMemberId = creatorMemberId,
    )

    private fun Exercise.toEntity() = ExerciseEntity(
        id = id,
        nameEn = nameEn.trim(),
        nameAr = nameAr.trim(),
        category = category.name,
        isCustom = isCustom,
        creatorMemberId = creatorMemberId,
    )
}
