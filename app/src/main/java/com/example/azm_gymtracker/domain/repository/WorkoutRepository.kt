package com.example.azm_gymtracker.domain.repository

import com.example.azm_gymtracker.data.local.entity.ExerciseRecordTuple
import com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple
import com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for workout session persistence operations.
 */
interface WorkoutRepository {

    /**
     * Observes the device's single IN_PROGRESS session (lightweight entity).
     */
    fun getActiveSession(): Flow<WorkoutSession?>

    /**
     * Observes the active IN_PROGRESS session with all nested participants, exercises, and sets.
     */
    fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?>

    /**
     * Observes a specific session by ID with full details.
     */
    fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?>

    /**
     * Observes all COMPLETED sessions for a specific [memberId], newest first.
     */
    fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>>

    /**
     * Observes completed sessions with full details.
     * If [memberId] is null, observes all completed sessions across all members.
     */
    fun getCompletedSessionsWithDetails(memberId: Long?): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Starts a new workout session for a group of participating members.
     */
    suspend fun startSession(participantIds: List<Long>): Long

    /**
     * Adds an exercise from the catalog to an existing [sessionId].
     * Returns the new workout exercise ID.
     */
    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, exerciseNameSnapshot: String): Long

    /**
     * Removes an exercise and its sets from the active session.
     */
    suspend fun removeExerciseFromSession(workoutExerciseId: Long)

    /**
     * Updates session start timestamp when user explicitly starts the workout.
     */
    suspend fun updateSessionStartedAt(sessionId: Long, startedAt: Long = System.currentTimeMillis())

    /**
     * Logs a new set for a specific participant within [workoutExerciseId].
     */
    suspend fun addSet(workoutExerciseId: Long, memberId: Long, reps: Int, weightKg: Float?): Long

    /**
     * Marks a set as completed.
     */
    suspend fun markSetCompleted(setId: Long)

    /**
     * Updates the reps and/or weight for an existing set.
     */
    suspend fun updateSet(set: WorkoutSet)

    /**
     * Deletes a set and atomically re-indexes remaining sets within the exercise.
     */
    suspend fun deleteSet(setId: Long, workoutExerciseId: Long)

    /**
     * Marks the session as COMPLETED and records ended timestamp.
     */
    suspend fun finishSession(sessionId: Long)

    /**
     * Marks the session as CANCELLED.
     */
    suspend fun cancelSession(sessionId: Long)

    /**
     * Permanently deletes a workout session.
     */
    suspend fun deleteSession(sessionId: Long)

    /**
     * Updates paused duration for the session.
     */
    suspend fun updateSessionPausedTime(sessionId: Long, pausedTotalMs: Long)

    /**
     * Finds the last set by [memberId] in [exerciseId] for stepper initialization.
     */
    suspend fun getLastSetForMemberInExercise(exerciseId: Long, memberId: Long): WorkoutSetEntity?

    /**
     * Finds the last set in [exerciseId] regardless of member as a fallback.
     */
    suspend fun getLastSetInExercise(exerciseId: Long): WorkoutSetEntity?

    /**
     * Volume lifted per participant in a session.
     */
    fun getParticipantVolumeBreakdown(sessionId: Long): Flow<List<ParticipantVolumeTuple>>

    /**
     * Personal records for a member across all exercises.
     */
    fun getPersonalRecordsForMember(memberId: Long): Flow<List<ExerciseRecordTuple>>

    /**
     * Total lifetime volume lifted by a member.
     */
    fun getTotalVolumeForMember(memberId: Long): Flow<Double?>

    /**
     * Weight progression history for a member on an exercise.
     */
    fun getWeightProgression(memberId: Long, exerciseId: Long): Flow<List<WeightProgressionPointTuple>>
}
