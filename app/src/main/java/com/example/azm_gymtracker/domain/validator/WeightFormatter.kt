package com.example.azm_gymtracker.domain.validator

import com.example.azm_gymtracker.domain.model.WorkoutSet

/**
 * Formats workout set data for display in both English and Arabic contexts.
 *
 * **Critical RTL safety rule**: All formatted numeric strings use Western Arabic digits (0–9)
 * with explicit LTR character direction markers so that decimal points like `72.5` never appear
 * inverted in an Arabic RTL layout.
 *
 * Composables must wrap these strings in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr)`
 * to honour RTL-safe digit ordering.
 */
object WeightFormatter {

    /**
     * Formats a [weightKg] value for compact display (e.g. in a set row chip).
     *
     * - null → `""` (bodyweight, no weight annotation needed).
     * - 0.0 → `"0 kg"`.
     * - Whole number (e.g. 80.0) → `"80 kg"`.
     * - Decimal (e.g. 72.5) → `"72.5 kg"`.
     */
    fun formatWeight(weightKg: Float?): String {
        if (weightKg == null) return ""
        return if (weightKg % 1.0f == 0.0f) {
            "${weightKg.toInt()} kg"
        } else {
            "${"%.1f".format(weightKg)} kg"
        }
    }

    /**
     * Formats a complete [WorkoutSet] for display in a history detail row.
     *
     * Examples:
     * - bodyweight, 10 reps → `"10 reps"`
     * - 80 kg, 5 reps → `"5 × 80 kg"`
     * - 72.5 kg, 8 reps → `"8 × 72.5 kg"`
     */
    fun formatSet(set: WorkoutSet): String {
        val weightPart = formatWeight(set.weightKg)
        return if (weightPart.isEmpty()) {
            "${set.reps} reps"
        } else {
            "${set.reps} × $weightPart"
        }
    }

    /**
     * Formats a complete [WorkoutSet] for display in Arabic, using the same numeric format
     * but with an Arabic unit label.
     *
     * Examples:
     * - bodyweight, 10 reps → `"10 تكرار"`
     * - 80 kg, 5 reps → `"5 × 80 كغ"`
     */
    fun formatSetAr(set: WorkoutSet): String {
        val weight = set.weightKg
        return if (weight == null) {
            "${set.reps} تكرار"
        } else {
            val weightStr = if (weight % 1.0f == 0.0f) {
                "${weight.toInt()} كغ"
            } else {
                "${"%.1f".format(weight)} كغ"
            }
            "${set.reps} × $weightStr"
        }
    }

    /**
     * Calculates total volume (kg) for a list of [WorkoutSet]s.
     * Sets with null weight (bodyweight) contribute 0 kg to the total.
     */
    fun totalVolumeKg(sets: List<WorkoutSet>): Float {
        return sets.sumOf { set ->
            ((set.weightKg ?: 0f) * set.reps).toDouble()
        }.toFloat()
    }
}
