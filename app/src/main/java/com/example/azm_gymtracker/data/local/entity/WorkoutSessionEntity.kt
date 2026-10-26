package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a workout session owned by a single member.
 *
 * Foreign key: [memberId] → [MemberEntity.id] with CASCADE delete so that
 * deleting a member automatically removes all their workout sessions (and
 * by cascade, all exercises and sets within those sessions).
 *
 * Index on `(member_id, status)` enables efficient lookup of a member's
 * active in-progress session without a full table scan.
 */
@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["member_id", "status"])],
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "member_id")
    val memberId: Long,

    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "finished_at")
    val finishedAt: Long? = null,

    /**
     * Lifecycle status stored as a string enum name.
     * Valid values: "IN_PROGRESS", "COMPLETED", "DISCARDED".
     */
    @ColumnInfo(name = "status")
    val status: String = "IN_PROGRESS",
)
