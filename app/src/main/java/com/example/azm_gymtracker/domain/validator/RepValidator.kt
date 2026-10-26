package com.example.azm_gymtracker.domain.validator

/**
 * Validates repetition counts for a workout set.
 *
 * Rules:
 * - Must be a positive integer (≥ 1).
 * - Must not exceed 999.
 */
object RepValidator {

    private const val MIN_REPS = 1
    private const val MAX_REPS = 999

    sealed interface Result {
        data object Valid : Result
        data object TooLow : Result
        data object TooHigh : Result
    }

    /**
     * Validates [reps] and returns a [Result].
     */
    fun validate(reps: Int): Result = when {
        reps < MIN_REPS -> Result.TooLow
        reps > MAX_REPS -> Result.TooHigh
        else -> Result.Valid
    }

    /** Returns true only when [reps] passes all rules. */
    fun isValid(reps: Int): Boolean = validate(reps) == Result.Valid

    /**
     * Parses [raw] input string to an integer and validates it.
     * Returns [Result.TooLow] for non-numeric or empty input.
     */
    fun validateString(raw: String): Result {
        val parsed = raw.trim().toIntOrNull() ?: return Result.TooLow
        return validate(parsed)
    }
}
