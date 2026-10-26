package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a gym member.
 *
 * Constraints:
 * - [name] has a unique index to prevent duplicate member names at the database level.
 */
@Entity(
    tableName = "members",
    indices = [Index(value = ["name"], unique = true)],
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE)
    val name: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

fun MemberEntity.toDomain() = com.example.azm_gymtracker.domain.model.Member(
    id = id,
    name = name,
    colorHex = colorHex,
    createdAt = createdAt,
)
