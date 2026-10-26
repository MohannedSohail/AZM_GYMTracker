package com.example.azm_gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for workout sessions, exercises, and sets.
 *
 * All write operations that touch multiple rows (finish, discard, set delete + re-index)
 * must be called within a [Transaction].
 */
@Dao
interface WorkoutDao {

    // ─── Session ─────────────────────────────────────────────────────────────────

    /**
     * Inserts a new workout session and returns its generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    /**
     * Updates the status and finishedAt timestamp for an existing session.
     * Used to transition IN_PROGRESS → COMPLETED or DISCARDED.
     */
    @Update
    suspend fun updateSession(session: WorkoutSessionEntity): Int

    /**
     * Observes the most recent IN_PROGRESS session for a given [memberId].
     * Returns null if no active session exists.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE member_id = :memberId AND status = 'IN_PROGRESS'
        ORDER BY started_at DESC
        LIMIT 1
        """,
    )
    fun getActiveSession(memberId: Long): Flow<WorkoutSessionEntity?>

    /**
     * Observes all COMPLETED sessions for a given [memberId], newest first.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE member_id = :memberId AND status = 'COMPLETED'
        ORDER BY finished_at DESC
        """,
    )
    fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSessionEntity>>

    // ─── Workout Exercises ────────────────────────────────────────────────────────

    /**
     * Inserts an exercise entry into a session and returns its row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercise(entity: WorkoutExerciseEntity): Long

    /**
     * Observes the ordered list of exercises for a given [sessionId].
     */
    @Query(
        """
        SELECT * FROM workout_exercises
        WHERE session_id = :sessionId
        ORDER BY order_index ASC
        """,
    )
    fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExerciseEntity>>

    // ─── Sets ─────────────────────────────────────────────────────────────────────

    /**
     * Inserts a single workout set and returns its row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    /**
     * Updates an existing set (reps, weight).
     */
    @Update
    suspend fun updateSet(set: WorkoutSetEntity): Int

    /**
     * Deletes a set by [setId].
     * The caller is responsible for re-indexing remaining sets within the same exercise.
     */
    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSetById(setId: Long)

    /**
     * Re-indexes all sets for a given [workoutExerciseId] to restore consecutive 1-based
     * set numbers after a deletion. Must be called inside a [Transaction].
     */
    @Query(
        """
        UPDATE workout_sets
        SET set_number = (
            SELECT COUNT(*) FROM workout_sets ws2
            WHERE ws2.workout_exercise_id = :workoutExerciseId
              AND ws2.id <= workout_sets.id
        )
        WHERE workout_exercise_id = :workoutExerciseId
        """,
    )
    suspend fun reindexSets(workoutExerciseId: Long)

    /**
     * Observes all sets for a given [workoutExerciseId], ordered by set number.
     */
    @Query(
        """
        SELECT * FROM workout_sets
        WHERE workout_exercise_id = :workoutExerciseId
        ORDER BY set_number ASC
        """,
    )
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>>

    /**
     * Returns all sets for a given [sessionId] joined via workout_exercises.
     * Used for history detail and volume calculation.
     */
    @Query(
        """
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE we.session_id = :sessionId
        """,
    )
    suspend fun getAllSetsForSession(sessionId: Long): List<WorkoutSetEntity>

    /**
     * Atomically deletes a set and re-indexes the remaining sets.
     */
    @Transaction
    suspend fun deleteSetAndReindex(setId: Long, workoutExerciseId: Long) {
        deleteSetById(setId)
        reindexSets(workoutExerciseId)
    }
}
