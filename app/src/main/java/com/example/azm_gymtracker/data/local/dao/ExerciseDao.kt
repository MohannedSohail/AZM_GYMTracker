package com.example.azm_gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [ExerciseEntity] table.
 */
@Dao
interface ExerciseDao {

    /** Observes all exercises ordered by English name. */
    @Query("SELECT * FROM exercises ORDER BY name_en ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    /** Observes exercises filtered by [category] string (e.g. "CHEST"). */
    @Query("SELECT * FROM exercises WHERE category = :category ORDER BY name_en ASC")
    fun getExercisesByCategory(category: String): Flow<List<ExerciseEntity>>

    /**
     * Observes exercises whose English or Arabic name contains [query] (case-insensitive).
     * Used for the search bar in the exercise picker.
     */
    @Query(
        """
        SELECT * FROM exercises 
        WHERE name_en LIKE '%' || :query || '%' OR name_ar LIKE '%' || :query || '%'
        ORDER BY name_en ASC
        """,
    )
    fun searchExercises(query: String): Flow<List<ExerciseEntity>>

    /**
     * Inserts a single exercise.
     * [OnConflictStrategy.IGNORE] is used to safely skip duplicate preloaded exercises
     * on subsequent database opens without throwing.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    /** Bulk-inserts multiple exercises (used during pre-population). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    /**
     * Returns the count of exercises in the catalog.
     * Used during DB initialization to decide whether pre-population is needed.
     */
    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getExerciseCount(): Int
}
