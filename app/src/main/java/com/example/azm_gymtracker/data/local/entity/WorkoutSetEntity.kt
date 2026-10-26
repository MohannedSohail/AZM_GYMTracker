package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for an individual set within a workout exercise.
 *
 * Foreign key: [workoutExerciseId] → [WorkoutExerciseEntity.id] with CASCADE delete
 * so that removing a workout exercise removes all its sets automatically.
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["workout_exercise_id"])],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "workout_exercise_id")
    val workoutExerciseId: Long,

    /** 1-based display number within the exercise (re-indexed on delete). */
    @ColumnInfo(name = "set_number")
    val setNumber: Int,

    /** Number of repetitions performed (1–999). */
    @ColumnInfo(name = "reps")
    val reps: Int,

    /** Weight in kilograms; null means bodyweight / no external load. */
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float? = null,
)
