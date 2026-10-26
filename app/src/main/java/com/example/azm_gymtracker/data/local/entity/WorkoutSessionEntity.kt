package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a workout session.
 *
 * Supports multi-participant shared sessions on a single device.
 * Participants are linked via [SessionParticipantEntity].
 *
 * Index on `status` enables fast query of the single active session (`IN_PROGRESS`).
 */
@Entity(
    tableName = "workout_sessions",
    indices = [Index(value = ["status"])],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "ended_at")
    val endedAt: Long? = null,

    @ColumnInfo(name = "paused_total_ms", defaultValue = "0")
    val pausedTotalMs: Long = 0L,

    /**
     * Lifecycle status stored as string:
     * "IN_PROGRESS", "COMPLETED", "CANCELLED".
     */
    @ColumnInfo(name = "status")
    val status: String = "IN_PROGRESS",

    @ColumnInfo(name = "notes")
    val notes: String? = null,
)
