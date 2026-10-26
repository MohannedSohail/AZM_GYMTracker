package com.example.azm_gymtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Key-value table for lightweight application settings (e.g. active member selection)
 * stored natively within SQLite without external library dependencies.
 */
@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "value")
    val value: String,
)
