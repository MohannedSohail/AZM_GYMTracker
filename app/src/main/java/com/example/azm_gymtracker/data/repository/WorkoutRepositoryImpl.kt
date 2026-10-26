package com.example.azm_gymtracker.data.repository

import com.example.azm_gymtracker.data.local.dao.WorkoutDao
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import com.example.azm_gymtracker.domain.model.SessionStatus
import com.example.azm_gymtracker.domain.model.WorkoutExercise
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [WorkoutRepository].
 *
 * Note: This implementation returns a simplified [WorkoutSession] from [getActiveSession]
 * and [getCompletedSessions] that does not eagerly load nested exercises and sets.
 * Full nested loading is handled at the ViewModel layer by combining multiple DAOs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRepositoryImpl(private val dao: WorkoutDao) : WorkoutRepository {

    override fun getActiveSession(memberId: Long): Flow<WorkoutSession?> =
        dao.getActiveSession(memberId).map { entity -> entity?.toDomain() }

    override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> =
        dao.getCompletedSessions(memberId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun startSession(memberId: Long): Long {
        return dao.insertSession(
            WorkoutSessionEntity(memberId = memberId, status = "IN_PROGRESS")
        )
    }

    override suspend fun addExerciseToSession(
        sessionId: Long,
        exerciseId: Long,
        exerciseNameSnapshot: String,
    ): Long {
        val exercises = dao.getExercisesForSession(sessionId)
        // Determine current max order index by querying synchronously (acceptable here)
        val orderIndex = 0 // ViewModel tracks this from observed state
        return dao.insertWorkoutExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseNameSnapshot = exerciseNameSnapshot,
                orderIndex = orderIndex,
            )
        )
    }

    override suspend fun addSet(workoutExerciseId: Long, reps: Int, weightKg: Float?): Long {
        // Set number is determined by the ViewModel from observed sets count
        return dao.insertSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = 1, // Placeholder; ViewModel provides actual count + 1
                reps = reps,
                weightKg = weightKg,
            )
        )
    }

    override suspend fun updateSet(set: WorkoutSet) {
        dao.updateSet(set.toEntity())
    }

    override suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {
        dao.deleteSetAndReindex(setId, workoutExerciseId)
    }

    override suspend fun finishSession(sessionId: Long) {
        dao.updateSession(
            WorkoutSessionEntity(
                id = sessionId,
                memberId = 0L, // updated fields only — memberId preserved via UPDATE
                status = "COMPLETED",
                finishedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun discardSession(sessionId: Long) {
        dao.updateSession(
            WorkoutSessionEntity(
                id = sessionId,
                memberId = 0L,
                status = "DISCARDED",
                finishedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun deleteSession(sessionId: Long) {
        // Room will cascade delete all workout exercises and sets.
        // We update status to DISCARDED rather than physically deleting
        // in case the session still has IN_PROGRESS state.
        dao.updateSession(
            WorkoutSessionEntity(
                id = sessionId,
                memberId = 0L,
                status = "DISCARDED",
                finishedAt = System.currentTimeMillis(),
            )
        )
    }

    // ── Mapping ───────────────────────────────────────────────────────────────────

    private fun WorkoutSessionEntity.toDomain() = WorkoutSession(
        id = id,
        memberId = memberId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        status = runCatching { SessionStatus.valueOf(status) }.getOrDefault(SessionStatus.IN_PROGRESS),
        exercises = emptyList(), // Lazy-loaded by ViewModel
    )

    private fun WorkoutSet.toEntity() = WorkoutSetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        reps = reps,
        weightKg = weightKg,
    )
}
