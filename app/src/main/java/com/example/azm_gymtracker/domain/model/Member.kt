package com.example.azm_gymtracker.domain.model

/**
 * Pure domain model representing a gym member who tracks their workouts.
 *
 * @property id          Stable unique identifier (Room auto-generated).
 * @property name        Display name (1–50 trimmed characters, validated by [MemberNameValidator]).
 * @property colorHex    Hex color string (e.g. "#FF5722") used to visually distinguish the member.
 * @property createdAt   Unix epoch milliseconds of profile creation for ordering/display.
 */
data class Member(
    val id: Long = 0L,
    val name: String,
    val colorHex: String,
    val createdAt: Long = System.currentTimeMillis(),
)
