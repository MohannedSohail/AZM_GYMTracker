package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity linking a catalog exercise to a workout session.
 *
 * Foreign keys:
 * - [sessionId] → [WorkoutSessionEntity.id] with CASCADE delete.
 * - [exerciseId] → [ExerciseEntity.id] with RESTRICT to prevent deleting an exercise
 *   that has been used in a workout (history preservation).
 */
@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index(value = ["session_id"]), Index(value = ["exercise_id"])],
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,

    /** Snapshot of the exercise name at logging time for history display. */
    @ColumnInfo(name = "exercise_name_snapshot")
    val exerciseNameSnapshot: String,

    /** Zero-based ordering within the session. */
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
)
