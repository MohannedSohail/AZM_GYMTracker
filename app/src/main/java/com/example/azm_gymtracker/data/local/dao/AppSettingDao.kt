package com.example.azm_gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azm_gymtracker.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for lightweight local application settings.
 */
@Dao
interface AppSettingDao {

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingDirect(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun removeSetting(key: String)
}
