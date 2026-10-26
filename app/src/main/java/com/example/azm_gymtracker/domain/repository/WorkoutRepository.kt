package com.example.azm_gymtracker.domain.repository

import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for workout session persistence operations.
 */
interface WorkoutRepository {

    /**
     * Observes the current IN_PROGRESS session for [memberId].
     * Emits null if no active session exists.
     */
    fun getActiveSession(memberId: Long): Flow<WorkoutSession?>

    /**
     * Observes all COMPLETED sessions for [memberId], newest first.
     */
    fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>>

    /**
     * Starts a new workout session for [memberId] and returns the new session ID.
     */
    suspend fun startSession(memberId: Long): Long

    /**
     * Adds an exercise from the catalog to an existing [sessionId].
     * [exerciseId] must reference an existing exercise.
     * Returns the new workout exercise ID.
     */
    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long

    /**
     * Logs a new set for [workoutExerciseId].
     * Validation (reps 1-999, weight 0-999.5 or null) must be performed
     * before calling this method.
     * Returns the new set ID.
     */
    suspend fun addSet(workoutExerciseId: Long, reps: Int, weightKg: Float?): Long

    /**
     * Updates the reps and/or weight for an existing set.
     */
    suspend fun updateSet(set: WorkoutSet)

    /**
     * Deletes a set and atomically re-indexes the remaining sets within the exercise.
     */
    suspend fun deleteSet(setId: Long, workoutExerciseId: Long)

    /**
     * Marks the session as COMPLETED and records [finishedAt] timestamp.
     */
    suspend fun finishSession(sessionId: Long)

    /**
     * Marks the session as DISCARDED. The session will not appear in history.
     */
    suspend fun discardSession(sessionId: Long)

    /**
     * Permanently deletes a COMPLETED workout session from history.
     */
    suspend fun deleteSession(sessionId: Long)
}
