package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Cross-reference join table representing participation of a member in a workout session.
 *
 * Supports multi-participant shared sessions on a single device.
 *
 * Foreign keys:
 * - [sessionId] → [WorkoutSessionEntity.id] with CASCADE delete.
 * - [memberId] → [MemberEntity.id] with CASCADE delete.
 */
@Entity(
    tableName = "session_participants",
    primaryKeys = ["session_id", "member_id"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["member_id"]),
    ],
)
data class SessionParticipantEntity(
    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "member_id")
    val memberId: Long,

    @ColumnInfo(name = "join_order", defaultValue = "0")
    val joinOrder: Int = 0,
)
