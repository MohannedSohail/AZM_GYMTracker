package com.example.azm_gymtracker.domain.validator

/**
 * Validates member display names before they are persisted.
 *
 * Rules:
 * - Must not be blank after trimming.
 * - Length must be between 1 and 50 characters (after trimming).
 */
object MemberNameValidator {

    private const val MAX_LENGTH = 50

    sealed interface Result {
        data object Valid : Result
        data object Blank : Result
        data class TooLong(val length: Int, val max: Int) : Result
    }

    /**
     * Validates [name] and returns a [Result] describing the outcome.
     */
    fun validate(name: String): Result {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> Result.Blank
            trimmed.length > MAX_LENGTH -> Result.TooLong(trimmed.length, MAX_LENGTH)
            else -> Result.Valid
        }
    }

    /** Returns true only when [name] passes all rules. */
    fun isValid(name: String): Boolean = validate(name) == Result.Valid
}
