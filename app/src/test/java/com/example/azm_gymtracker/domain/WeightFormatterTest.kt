package com.example.azm_gymtracker.domain

import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.validator.WeightFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightFormatterTest {

    // ─── formatWeight ───────────────────────────────────────────────────────────

    @Test
    fun `null weight formats as empty string`() {
        assertEquals("", WeightFormatter.formatWeight(null))
    }

    @Test
    fun `zero weight formats as 0 kg`() {
        assertEquals("0 kg", WeightFormatter.formatWeight(0.0f))
    }

    @Test
    fun `whole number weight drops decimal`() {
        assertEquals("80 kg", WeightFormatter.formatWeight(80.0f))
    }

    @Test
    fun `decimal weight keeps one decimal place`() {
        assertEquals("72.5 kg", WeightFormatter.formatWeight(72.5f))
    }

    // ─── formatSet ──────────────────────────────────────────────────────────────

    private fun set(reps: Int, weightKg: Float?) = WorkoutSet(
        id = 0L,
        workoutExerciseId = 1L,
        setNumber = 1,
        reps = reps,
        weightKg = weightKg,
    )

    @Test
    fun `bodyweight set displays reps only`() {
        assertEquals("10 reps", WeightFormatter.formatSet(set(10, null)))
    }

    @Test
    fun `weighted set displays reps x weight`() {
        assertEquals("5 × 80 kg", WeightFormatter.formatSet(set(5, 80.0f)))
    }

    @Test
    fun `decimal weight set formatted correctly`() {
        assertEquals("8 × 72.5 kg", WeightFormatter.formatSet(set(8, 72.5f)))
    }

    // ─── formatSetAr ────────────────────────────────────────────────────────────

    @Test
    fun `Arabic bodyweight displays reps with Arabic label`() {
        assertEquals("10 تكرار", WeightFormatter.formatSetAr(set(10, null)))
    }

    @Test
    fun `Arabic weighted set uses Arabic kg label`() {
        assertEquals("5 × 80 كغ", WeightFormatter.formatSetAr(set(5, 80.0f)))
    }

    @Test
    fun `Arabic decimal weight formatted correctly`() {
        assertEquals("8 × 72.5 كغ", WeightFormatter.formatSetAr(set(8, 72.5f)))
    }

    // ─── totalVolumeKg ──────────────────────────────────────────────────────────

    @Test
    fun `empty list returns 0 volume`() {
        assertEquals(0.0f, WeightFormatter.totalVolumeKg(emptyList()), 0.001f)
    }

    @Test
    fun `bodyweight sets contribute 0 to volume`() {
        val sets = listOf(set(10, null), set(8, null))
        assertEquals(0.0f, WeightFormatter.totalVolumeKg(sets), 0.001f)
    }

    @Test
    fun `mixed sets calculate correct total volume`() {
        // 3 × 80 = 240, 5 × 50 = 250 → total 490
        val sets = listOf(set(3, 80.0f), set(5, 50.0f))
        assertEquals(490.0f, WeightFormatter.totalVolumeKg(sets), 0.001f)
    }
}
