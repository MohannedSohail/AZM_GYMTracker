package com.example.azm_gymtracker.domain.repository

import com.example.azm_gymtracker.domain.model.Member
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for member persistence operations.
 */
interface MemberRepository {

    /** Observes all members ordered by creation date (oldest first). */
    fun getAllMembers(): Flow<List<Member>>

    /** Observes the ID of the currently active member profile. */
    fun getActiveMemberId(): Flow<Long?>

    /** Sets the currently active member profile. */
    suspend fun setActiveMemberId(memberId: Long)

    /** Returns the total count of created members. */
    suspend fun getMemberCount(): Int

    /**
     * Creates a new member and returns the generated ID.
     * Throws [IllegalArgumentException] if [member] fails name validation.
     * Throws [IllegalStateException] if a member with the same name already exists.
     */
    suspend fun createMember(member: Member): Long

    /**
     * Updates an existing member's name or color.
     * Throws [IllegalArgumentException] if [member] fails name validation.
     */
    suspend fun updateMember(member: Member)

    /**
     * Permanently deletes a member and cleans up orphan sessions.
     */
    suspend fun deleteMember(member: Member)
}
