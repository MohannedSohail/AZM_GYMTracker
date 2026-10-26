package com.example.azm_gymtracker.domain.repository

import com.example.azm_gymtracker.domain.model.Member
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for member persistence operations.
 * All implementations must guarantee member isolation — each member's data
 * is strictly scoped by their [Member.id].
 */
interface MemberRepository {

    /** Observes all members ordered by creation date (oldest first). */
    fun getAllMembers(): Flow<List<Member>>

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
     * Permanently deletes a member and all their associated data (sessions, sets).
     * This operation is irreversible.
     */
    suspend fun deleteMember(member: Member)
}
