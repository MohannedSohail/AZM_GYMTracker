package com.example.azm_gymtracker.domain.model

/**
 * Muscle group categories used to organize the exercise catalog.
 * Values are uppercase stable identifiers stored in the database.
 */
enum class MuscleCategory {
    CHEST,
    BACK,
    LEGS,
    SHOULDERS,
    ARMS,
    CORE,
    CARDIO,
    OTHER,
}

/**
 * Pure domain model representing a catalogued exercise.
 *
 * @property id               Stable unique identifier.
 * @property nameEn           English name (always populated).
 * @property nameAr           Arabic name (always populated for RTL support).
 * @property category         Primary muscle group classification.
 * @property isCustom         True when created by a user rather than preloaded.
 * @property creatorMemberId  Foreign key to the member who created a custom exercise; null for system exercises.
 */
data class Exercise(
    val id: Long = 0L,
    val nameEn: String,
    val nameAr: String,
    val category: MuscleCategory,
    val isCustom: Boolean = false,
    val creatorMemberId: Long? = null,
)
