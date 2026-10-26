package com.example.azm_gymtracker.domain

import com.example.azm_gymtracker.domain.validator.RepValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepValidatorTest {

    @Test
    fun `1 rep is valid`() {
        assertEquals(RepValidator.Result.Valid, RepValidator.validate(1))
    }

    @Test
    fun `999 reps is valid`() {
        assertEquals(RepValidator.Result.Valid, RepValidator.validate(999))
    }

    @Test
    fun `0 reps returns TooLow`() {
        assertEquals(RepValidator.Result.TooLow, RepValidator.validate(0))
    }

    @Test
    fun `negative reps returns TooLow`() {
        assertEquals(RepValidator.Result.TooLow, RepValidator.validate(-5))
    }

    @Test
    fun `1000 reps returns TooHigh`() {
        assertEquals(RepValidator.Result.TooHigh, RepValidator.validate(1000))
    }

    @Test
    fun `isValid true for 10 reps`() {
        assertTrue(RepValidator.isValid(10))
    }

    @Test
    fun `isValid false for 0 reps`() {
        assertFalse(RepValidator.isValid(0))
    }

    @Test
    fun `validateString with valid integer string`() {
        assertEquals(RepValidator.Result.Valid, RepValidator.validateString("12"))
    }

    @Test
    fun `validateString with empty string returns TooLow`() {
        assertEquals(RepValidator.Result.TooLow, RepValidator.validateString(""))
    }

    @Test
    fun `validateString with non-numeric returns TooLow`() {
        assertEquals(RepValidator.Result.TooLow, RepValidator.validateString("abc"))
    }

    @Test
    fun `validateString trims whitespace`() {
        assertEquals(RepValidator.Result.Valid, RepValidator.validateString("  5  "))
    }
}
