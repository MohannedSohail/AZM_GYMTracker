package com.example.azm_gymtracker.domain.validator

/**
 * Validates the optional weight value for a workout set.
 *
 * Rules:
 * - null means bodyweight / no external load — always valid.
 * - When provided, weight must be ≥ 0.0 and ≤ 999.5 kg.
 * - Precision is limited to one decimal place (rounded at persistence, not validated here).
 */
object WeightValidator {

    private const val MIN_WEIGHT = 0.0f
    private const val MAX_WEIGHT = 999.5f

    sealed interface Result {
        data object Valid : Result
        /** Weight is null which means bodyweight — valid. */
        data object Bodyweight : Result
        data object TooLow : Result
        data object TooHigh : Result
    }

    /**
     * Validates an optional [weightKg] float.
     */
    fun validate(weightKg: Float?): Result = when {
        weightKg == null -> Result.Bodyweight
        weightKg < MIN_WEIGHT -> Result.TooLow
        weightKg > MAX_WEIGHT -> Result.TooHigh
        else -> Result.Valid
    }

    /** Returns true for null (bodyweight) or any in-range weight. */
    fun isValid(weightKg: Float?): Boolean {
        val result = validate(weightKg)
        return result == Result.Valid || result == Result.Bodyweight
    }

    /**
     * Parses [raw] user input text.
     * - Empty or blank → null (bodyweight).
     * - Non-numeric → [Result.TooLow] (treated as invalid entry).
     */
    fun validateString(raw: String): Result {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return Result.Bodyweight
        val parsed = trimmed.toFloatOrNull() ?: return Result.TooLow
        return validate(parsed)
    }
}
