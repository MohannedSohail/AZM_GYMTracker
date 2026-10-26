package com.example.azm_gymtracker.domain

import com.example.azm_gymtracker.domain.validator.WeightValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightValidatorTest {

    @Test
    fun `null weight returns Bodyweight`() {
        assertEquals(WeightValidator.Result.Bodyweight, WeightValidator.validate(null))
    }

    @Test
    fun `zero weight is valid`() {
        assertEquals(WeightValidator.Result.Valid, WeightValidator.validate(0.0f))
    }

    @Test
    fun `positive weight within range is valid`() {
        assertEquals(WeightValidator.Result.Valid, WeightValidator.validate(80.0f))
    }

    @Test
    fun `max weight 999_5 is valid`() {
        assertEquals(WeightValidator.Result.Valid, WeightValidator.validate(999.5f))
    }

    @Test
    fun `weight above 999_5 returns TooHigh`() {
        assertEquals(WeightValidator.Result.TooHigh, WeightValidator.validate(1000.0f))
    }

    @Test
    fun `negative weight returns TooLow`() {
        assertEquals(WeightValidator.Result.TooLow, WeightValidator.validate(-1.0f))
    }

    @Test
    fun `isValid true for null bodyweight`() {
        assertTrue(WeightValidator.isValid(null))
    }

    @Test
    fun `isValid true for 60 kg`() {
        assertTrue(WeightValidator.isValid(60.0f))
    }

    @Test
    fun `isValid false for negative weight`() {
        assertFalse(WeightValidator.isValid(-5.0f))
    }

    @Test
    fun `validateString with empty string returns Bodyweight`() {
        assertEquals(WeightValidator.Result.Bodyweight, WeightValidator.validateString(""))
    }

    @Test
    fun `validateString with whitespace returns Bodyweight`() {
        assertEquals(WeightValidator.Result.Bodyweight, WeightValidator.validateString("   "))
    }

    @Test
    fun `validateString with valid decimal`() {
        assertEquals(WeightValidator.Result.Valid, WeightValidator.validateString("72.5"))
    }

    @Test
    fun `validateString with non-numeric returns TooLow`() {
        assertEquals(WeightValidator.Result.TooLow, WeightValidator.validateString("abc"))
    }

    @Test
    fun `validateString with too large value returns TooHigh`() {
        assertEquals(WeightValidator.Result.TooHigh, WeightValidator.validateString("9999.0"))
    }
}
