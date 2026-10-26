package com.example.azm_gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [MemberEntity] table.
 *
 * All queries return [Flow] for reactive, lifecycle-aware UI updates.
 */
@Dao
interface MemberDao {

    /** Observes all members ordered by creation time (oldest first). */
    @Query("SELECT * FROM members ORDER BY created_at ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    /** Looks up a single member by [id], or returns null if not found. */
    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): MemberEntity?

    /**
     * Inserts a new member.
     * [OnConflictStrategy.ABORT] ensures that attempting to insert a member with
     * a duplicate name throws [android.database.sqlite.SQLiteConstraintException].
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMember(member: MemberEntity): Long

    /** Updates an existing member record. Returns number of updated rows. */
    @Update
    suspend fun updateMember(member: MemberEntity): Int

    /**
     * Deletes a member and (by CASCADE) all their workout sessions, exercises, and sets.
     * Returns number of deleted rows.
     */
    @Delete
    suspend fun deleteMember(member: MemberEntity): Int
}
