package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for the exercise catalog.
 * Both system-preloaded exercises and member-created custom exercises share this table.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    /** English display name. */
    @ColumnInfo(name = "name_en")
    val nameEn: String,

    /** Arabic display name for RTL support. */
    @ColumnInfo(name = "name_ar")
    val nameAr: String,

    /** Muscle group category stored as enum name string (e.g. "CHEST"). */
    @ColumnInfo(name = "category")
    val category: String,

    /** True for user-created custom exercises; false for preloaded system exercises. */
    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    /**
     * Optional foreign key to the [MemberEntity] that created this custom exercise.
     * No Room foreign key annotation here to avoid cascade complexity — orphan cleanup
     * is handled at the repository layer when a member is deleted.
     */
    @ColumnInfo(name = "creator_member_id")
    val creatorMemberId: Long? = null,
)
