package com.example.azm_gymtracker.domain

import com.example.azm_gymtracker.domain.validator.MemberNameValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemberNameValidatorTest {

    @Test
    fun `valid name returns Valid`() {
        assertEquals(MemberNameValidator.Result.Valid, MemberNameValidator.validate("Ali"))
    }

    @Test
    fun `name with surrounding spaces is trimmed and valid`() {
        assertEquals(MemberNameValidator.Result.Valid, MemberNameValidator.validate("  Ahmed  "))
    }

    @Test
    fun `empty string returns Blank`() {
        assertEquals(MemberNameValidator.Result.Blank, MemberNameValidator.validate(""))
    }

    @Test
    fun `whitespace only returns Blank`() {
        assertEquals(MemberNameValidator.Result.Blank, MemberNameValidator.validate("   "))
    }

    @Test
    fun `name of exactly 50 chars returns Valid`() {
        val name = "a".repeat(50)
        assertEquals(MemberNameValidator.Result.Valid, MemberNameValidator.validate(name))
    }

    @Test
    fun `name of 51 chars returns TooLong`() {
        val name = "a".repeat(51)
        val result = MemberNameValidator.validate(name)
        assertTrue(result is MemberNameValidator.Result.TooLong)
    }

    @Test
    fun `TooLong result carries correct length and max`() {
        val name = "a".repeat(55)
        val result = MemberNameValidator.validate(name) as MemberNameValidator.Result.TooLong
        assertEquals(55, result.length)
        assertEquals(50, result.max)
    }

    @Test
    fun `isValid returns true for valid name`() {
        assertTrue(MemberNameValidator.isValid("Sara"))
    }

    @Test
    fun `isValid returns false for blank`() {
        assertFalse(MemberNameValidator.isValid(""))
    }

    @Test
    fun `isValid returns false for too long name`() {
        assertFalse(MemberNameValidator.isValid("a".repeat(51)))
    }

    @Test
    fun `Arabic name is valid`() {
        assertTrue(MemberNameValidator.isValid("محمد"))
    }
}
