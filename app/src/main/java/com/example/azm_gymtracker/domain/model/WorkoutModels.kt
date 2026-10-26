package com.example.azm_gymtracker.domain.model

/**
 * Status lifecycle for a workout session.
 */
enum class SessionStatus {
    /** Session has been started and is still being recorded. */
    IN_PROGRESS,
    /** Session was explicitly finished by the user and is permanently saved. */
    COMPLETED,
    /** Session was discarded by the user without saving. */
    DISCARDED,
}

/**
 * Pure domain model representing a single workout session for one member.
 *
 * @property id         Stable unique identifier.
 * @property memberId   Foreign key to the owning [Member].
 * @property startedAt  Unix epoch milliseconds when the session was started.
 * @property finishedAt Unix epoch milliseconds when the session was finished; null while in progress.
 * @property status     Lifecycle state of the session.
 * @property exercises  Ordered list of exercises recorded in this session.
 */
data class WorkoutSession(
    val id: Long = 0L,
    val memberId: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val status: SessionStatus = SessionStatus.IN_PROGRESS,
    val exercises: List<WorkoutExercise> = emptyList(),
)

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
 * @property setNumber          1-based display number within the exercise.
 * @property reps               Number of repetitions performed (1–999).
 * @property weightKg           Weight lifted in kilograms; null means bodyweight / no external load.
 */
data class WorkoutSet(
    val id: Long = 0L,
    val workoutExerciseId: Long,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float? = null,
)
