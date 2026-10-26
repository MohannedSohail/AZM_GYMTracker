package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Composite model representing a workout exercise alongside its catalog definition
 * and all associated sets.
 */
data class WorkoutExerciseWithSets(
    @Embedded
    val workoutExercise: WorkoutExerciseEntity,

    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id",
    )
    val catalogExercise: ExerciseEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "workout_exercise_id",
    )
    val sets: List<WorkoutSetEntity>,
)

/**
 * Composite model representing a full workout session including:
 * - Its core session entity
 * - All participating members (via [SessionParticipantEntity] junction)
 * - All logged exercises with their sets
 */
data class WorkoutSessionWithDetails(
    @Embedded
    val session: WorkoutSessionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SessionParticipantEntity::class,
            parentColumn = "session_id",
            entityColumn = "member_id",
        ),
    )
    val participants: List<MemberEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "session_id",
        entity = WorkoutExerciseEntity::class,
    )
    val exercises: List<WorkoutExerciseWithSets>,
)

/**
 * Query tuple for participant volume breakdown in a session.
 */
data class ParticipantVolumeTuple(
    @ColumnInfo(name = "member_id")
    val memberId: Long,

    @ColumnInfo(name = "total_volume")
    val totalVolume: Double,

    @ColumnInfo(name = "sets_count")
    val setsCount: Int,
)

/**
 * Query tuple for personal records (max weight per exercise).
 */
data class ExerciseRecordTuple(
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    @ColumnInfo(name = "max_weight")
    val maxWeight: Float,
)

/**
 * Query tuple for weight progression over time.
 */
data class WeightProgressionPointTuple(
    @ColumnInfo(name = "completed_at")
    val timestamp: Long,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Float,

    @ColumnInfo(name = "reps")
    val reps: Int,
)

fun WorkoutSessionEntity.toDomain() = com.example.azm_gymtracker.domain.model.WorkoutSession(
    id = id,
    startedAt = startedAt,
    endedAt = endedAt,
    pausedTotalMs = pausedTotalMs,
    notes = notes,
    status = runCatching { com.example.azm_gymtracker.domain.model.SessionStatus.valueOf(status) }
        .getOrDefault(com.example.azm_gymtracker.domain.model.SessionStatus.IN_PROGRESS),
)

fun WorkoutSessionWithDetails.toDomain() = com.example.azm_gymtracker.domain.model.WorkoutSession(
    id = session.id,
    startedAt = session.startedAt,
    endedAt = session.endedAt,
    pausedTotalMs = session.pausedTotalMs,
    notes = session.notes,
    status = runCatching { com.example.azm_gymtracker.domain.model.SessionStatus.valueOf(session.status) }
        .getOrDefault(com.example.azm_gymtracker.domain.model.SessionStatus.IN_PROGRESS),
    participants = participants.map { it.toDomain() },
    exercises = exercises.map { exWithSets ->
        com.example.azm_gymtracker.domain.model.WorkoutExercise(
            id = exWithSets.workoutExercise.id,
            sessionId = exWithSets.workoutExercise.sessionId,
            exerciseId = exWithSets.workoutExercise.exerciseId,
            exerciseName = exWithSets.workoutExercise.exerciseNameSnapshot,
            orderIndex = exWithSets.workoutExercise.orderIndex,
            sets = exWithSets.sets.map { setEntity ->
                com.example.azm_gymtracker.domain.model.WorkoutSet(
                    id = setEntity.id,
                    workoutExerciseId = setEntity.workoutExerciseId,
                    memberId = setEntity.memberId,
                    setNumber = setEntity.setNumber,
                    reps = setEntity.reps,
                    weightKg = setEntity.weightKg,
                    isCompleted = setEntity.isCompleted,
                    completedAt = setEntity.completedAt,
                )
            },
        )
    },
)
