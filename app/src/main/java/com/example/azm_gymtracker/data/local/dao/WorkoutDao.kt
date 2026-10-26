package com.example.azm_gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.azm_gymtracker.data.local.entity.ExerciseRecordTuple
import com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple
import com.example.azm_gymtracker.data.local.entity.SessionParticipantEntity
import com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for workout sessions, multi-participant shared sessions,
 * exercises, and sets.
 */
@Dao
interface WorkoutDao {

    // ─── Sessions ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: SessionParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<SessionParticipantEntity>)

    /**
     * Atomically creates a session and registers all participating members.
     */
    @Transaction
    suspend fun startSessionWithParticipants(
        session: WorkoutSessionEntity,
        participantIds: List<Long>,
    ): Long {
        val sessionId = insertSession(session)
        val participants = participantIds.mapIndexed { index, memberId ->
            SessionParticipantEntity(
                sessionId = sessionId,
                memberId = memberId,
                joinOrder = index,
            )
        }
        insertParticipants(participants)
        return sessionId
    }

    /**
     * Observes the single active IN_PROGRESS session on the device.
     */
    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' ORDER BY started_at DESC LIMIT 1")
    fun getActiveSession(): Flow<WorkoutSessionEntity?>

    /**
     * Observes the active IN_PROGRESS session with all nested participants, exercises, and sets.
     */
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'IN_PROGRESS' ORDER BY started_at DESC LIMIT 1")
    fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?>

    /**
     * Observes a specific session by ID with all nested relations.
     */
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?>

    /**
     * Observes all completed sessions for a specific member.
     */
    @Query(
        """
        SELECT s.* FROM workout_sessions s
        INNER JOIN session_participants sp ON s.id = sp.session_id
        WHERE sp.member_id = :memberId AND s.status = 'COMPLETED'
        ORDER BY s.started_at DESC
        """,
    )
    fun getCompletedSessionsForMember(memberId: Long): Flow<List<WorkoutSessionEntity>>

    /**
     * Observes all completed sessions with full details across all members, newest first.
     */
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE status = 'COMPLETED' ORDER BY started_at DESC")
    fun getAllCompletedSessionsWithDetails(): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Observes all completed sessions with full details for a specific member, newest first.
     */
    @Transaction
    @Query(
        """
        SELECT DISTINCT s.* FROM workout_sessions s
        INNER JOIN session_participants sp ON s.id = sp.session_id
        WHERE sp.member_id = :memberId AND s.status = 'COMPLETED'
        ORDER BY s.started_at DESC
        """,
    )
    fun getCompletedSessionsWithDetailsForMember(memberId: Long): Flow<List<WorkoutSessionWithDetails>>

    /**
     * Marks a session as COMPLETED with an ended timestamp.
     */
    @Query("UPDATE workout_sessions SET status = 'COMPLETED', ended_at = :endedAt WHERE id = :sessionId")
    suspend fun finishSession(sessionId: Long, endedAt: Long = System.currentTimeMillis())

    /**
     * Marks an in-progress session as CANCELLED.
     */
    @Query("UPDATE workout_sessions SET status = 'CANCELLED', ended_at = :endedAt WHERE id = :sessionId")
    suspend fun cancelSession(sessionId: Long, endedAt: Long = System.currentTimeMillis())

    /**
     * Permanently deletes a session. Foreign key CASCADE handles exercises, sets, and participants.
     */
    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    /**
     * Updates cumulative paused time for an active session.
     */
    @Query("UPDATE workout_sessions SET paused_total_ms = :pausedTotalMs WHERE id = :sessionId")
    suspend fun updateSessionPausedTime(sessionId: Long, pausedTotalMs: Long)

    /**
     * Deletes sessions that have zero remaining participants (e.g. after member deletions).
     */
    @Query("DELETE FROM workout_sessions WHERE id NOT IN (SELECT DISTINCT session_id FROM session_participants)")
    suspend fun cleanupOrphanSessions()

    // ─── Workout Exercises ────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkoutExercise(entity: WorkoutExerciseEntity): Long

    @Query("SELECT * FROM workout_exercises WHERE session_id = :sessionId ORDER BY order_index ASC")
    fun getExercisesForSession(sessionId: Long): Flow<List<WorkoutExerciseEntity>>

    @Query("DELETE FROM workout_exercises WHERE id = :workoutExerciseId")
    suspend fun deleteWorkoutExercise(workoutExerciseId: Long)

    @Query("SELECT COALESCE(MAX(order_index), -1) + 1 FROM workout_exercises WHERE session_id = :sessionId")
    suspend fun nextOrderIndex(sessionId: Long): Int

    /**
     * Atomically adds an exercise with auto-calculated order index.
     */
    @Transaction
    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, snapshotName: String): Long {
        val nextIndex = nextOrderIndex(sessionId)
        return insertWorkoutExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseNameSnapshot = snapshotName,
                orderIndex = nextIndex,
            ),
        )
    }

    // ─── Sets ─────────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateSet(set: WorkoutSetEntity): Int

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSetById(setId: Long)

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

    @Query("SELECT COALESCE(MAX(set_number), 0) + 1 FROM workout_sets WHERE workout_exercise_id = :workoutExerciseId")
    suspend fun nextSetNumber(workoutExerciseId: Long): Int

    /**
     * Atomically adds a set with calculated sequential set number.
     */
    @Transaction
    suspend fun addSetAtomic(
        workoutExerciseId: Long,
        memberId: Long,
        reps: Int,
        weightKg: Float?,
    ): Long {
        val setNumber = nextSetNumber(workoutExerciseId)
        return insertSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                memberId = memberId,
                setNumber = setNumber,
                reps = reps,
                weightKg = weightKg,
                isCompleted = false,
            ),
        )
    }

    /**
     * Atomically deletes a set and re-indexes the remaining sets.
     */
    @Transaction
    suspend fun deleteSetAndReindex(setId: Long, workoutExerciseId: Long) {
        deleteSetById(setId)
        reindexSets(workoutExerciseId)
    }

    @Query("UPDATE workout_sets SET is_completed = 1, completed_at = :completedAt WHERE id = :setId")
    suspend fun markSetCompleted(setId: Long, completedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM workout_sets WHERE workout_exercise_id = :workoutExerciseId ORDER BY set_number ASC")
    fun getSetsForExercise(workoutExerciseId: Long): Flow<List<WorkoutSetEntity>>

    @Query(
        """
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE we.session_id = :sessionId
        """,
    )
    suspend fun getAllSetsForSession(sessionId: Long): List<WorkoutSetEntity>

    /**
     * Returns the last completed set by a specific member in an exercise (for stepper pre-filling).
     */
    @Query(
        """
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE we.exercise_id = :exerciseId AND ws.member_id = :memberId AND ws.is_completed = 1
        ORDER BY ws.id DESC
        LIMIT 1
        """,
    )
    suspend fun getLastSetForMemberInExercise(exerciseId: Long, memberId: Long): WorkoutSetEntity?

    /**
     * Returns the last completed set in an exercise regardless of member (fallback for steppers).
     */
    @Query(
        """
        SELECT ws.* FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE we.exercise_id = :exerciseId AND ws.is_completed = 1
        ORDER BY ws.id DESC
        LIMIT 1
        """,
    )
    suspend fun getLastSetInExercise(exerciseId: Long): WorkoutSetEntity?

    // ─── Analytics & Aggregates ────────────────────────────────────────────────────

    /**
     * Calculates volume breakdown per participant in a given session.
     */
    @Query(
        """
        SELECT ws.member_id,
               SUM(ws.reps * COALESCE(ws.weight_kg, 0.0)) as total_volume,
               COUNT(ws.id) as sets_count
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE we.session_id = :sessionId AND ws.is_completed = 1
        GROUP BY ws.member_id
        """,
    )
    fun getParticipantVolumeBreakdown(sessionId: Long): Flow<List<ParticipantVolumeTuple>>

    /**
     * Finds personal records (highest logged weight) per exercise for a member.
     */
    @Query(
        """
        SELECT we.exercise_id, MAX(ws.weight_kg) as max_weight
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        WHERE ws.member_id = :memberId AND ws.is_completed = 1 AND ws.weight_kg IS NOT NULL
        GROUP BY we.exercise_id
        """,
    )
    fun getPersonalRecordsForMember(memberId: Long): Flow<List<ExerciseRecordTuple>>

    /**
     * Total lifetime volume lifted by a member across all completed sessions.
     */
    @Query(
        """
        SELECT SUM(ws.reps * COALESCE(ws.weight_kg, 0.0))
        FROM workout_sets ws
        WHERE ws.member_id = :memberId AND ws.is_completed = 1
        """,
    )
    fun getTotalVolumeForMember(memberId: Long): Flow<Double?>

    /**
     * Weight progression history for a member on a specific exercise.
     */
    @Query(
        """
        SELECT COALESCE(ws.completed_at, s.started_at) as completed_at,
               ws.weight_kg,
               ws.reps
        FROM workout_sets ws
        INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
        INNER JOIN workout_sessions s ON we.session_id = s.id
        WHERE ws.member_id = :memberId
          AND we.exercise_id = :exerciseId
          AND ws.is_completed = 1
          AND ws.weight_kg IS NOT NULL
        ORDER BY completed_at ASC
        """,
    )
    fun getWeightProgression(memberId: Long, exerciseId: Long): Flow<List<WeightProgressionPointTuple>>
}
