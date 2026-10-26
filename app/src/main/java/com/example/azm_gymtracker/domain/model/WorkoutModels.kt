package com.example.azm_gymtracker.domain.model

/**
 * Status lifecycle for a workout session.
 */
enum class SessionStatus {
    /** Session has been started and is still being recorded. */
    IN_PROGRESS,
    /** Session was explicitly finished by the user and is permanently saved. */
    COMPLETED,
    /** Session was cancelled / discarded by the user without saving. */
    CANCELLED;

    companion object {
        val DISCARDED = CANCELLED
    }
}

/**
 * Pure domain model representing a workout session (shared or individual).
 *
 * @property id             Stable unique identifier.
 * @property startedAt      Unix epoch milliseconds when the session was started.
 * @property endedAt        Unix epoch milliseconds when the session was finished; null while in progress.
 * @property pausedTotalMs  Cumulative duration the session was paused in milliseconds.
 * @property status         Lifecycle state of the session.
 * @property notes          Optional user notes for the session.
 * @property participants   List of participating [Member]s in this session.
 * @property exercises      Ordered list of exercises recorded in this session.
 */
data class WorkoutSession(
    val id: Long = 0L,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val pausedTotalMs: Long = 0L,
    val status: SessionStatus = SessionStatus.IN_PROGRESS,
    val notes: String? = null,
    val participants: List<Member> = emptyList(),
    val exercises: List<WorkoutExercise> = emptyList(),
) {
    /** Backward compatibility alias for endedAt. */
    val finishedAt: Long? get() = endedAt
}

/**
 * A single exercise entry within a [WorkoutSession].
 *
 * @property id           Stable unique identifier.
 * @property sessionId    Foreign key to the parent [WorkoutSession].
 * @property exerciseId   Foreign key to the catalog [Exercise].
 * @property exerciseName Snapshot of the exercise name at the time of logging (denormalized for history display).
 * @property orderIndex   Zero-based position within the session for stable ordering.
 * @property sets         Ordered list of sets performed for this exercise.
 */
data class WorkoutExercise(
    val id: Long = 0L,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val orderIndex: Int,
    val sets: List<WorkoutSet> = emptyList(),
)

/**
 * A single set within a [WorkoutExercise].
 *
 * @property id                 Stable unique identifier.
 * @property workoutExerciseId  Foreign key to the parent [WorkoutExercise].
 * @property memberId           Foreign key to the [Member] who logged this set.
 * @property setNumber          1-based display number within the exercise.
 * @property reps               Number of repetitions performed (1–999).
 * @property weightKg           Weight lifted in kilograms; null means bodyweight / no external load.
 * @property isCompleted        Whether this set was finished and marked done.
 * @property completedAt        Timestamp when the set was marked completed.
 */
data class WorkoutSet(
    val id: Long = 0L,
    val workoutExerciseId: Long,
    val memberId: Long = 0L,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
)
