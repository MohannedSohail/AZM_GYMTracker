package com.example.azm_gymtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [MemberEntity] table.
 */
@Dao
interface MemberDao {

    /** Observes all members ordered by creation time (oldest first). */
    @Query("SELECT * FROM members ORDER BY created_at ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    /** Looks up a single member by [id], or returns null if not found. */
    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): MemberEntity?

    @Query("SELECT COUNT(*) FROM members")
    suspend fun getMemberCount(): Int

    /**
     * Inserts a new member.
     * Duplicate names (case-insensitive) throw SQLiteConstraintException.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMember(member: MemberEntity): Long

    /** Updates an existing member record. Returns number of updated rows. */
    @Update
    suspend fun updateMember(member: MemberEntity): Int

    /**
     * Deletes a member. CASCADE automatically removes their participation records and sets.
     */
    @Delete
    suspend fun deleteMember(member: MemberEntity): Int

    @Query("DELETE FROM members WHERE id = :memberId")
    suspend fun deleteMemberById(memberId: Long): Int

    /**
     * Atomically deletes a member and removes any workout sessions left without participants.
     */
    @Transaction
    suspend fun deleteMemberWithCleanup(memberId: Long): Int {
        val deleted = deleteMemberById(memberId)
        deleteOrphanSessions()
        return deleted
    }

    @Query("DELETE FROM workout_sessions WHERE id NOT IN (SELECT DISTINCT session_id FROM session_participants)")
    suspend fun deleteOrphanSessions()
}
