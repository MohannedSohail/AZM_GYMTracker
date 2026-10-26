package com.example.azm_gymtracker.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.azm_gymtracker.data.local.dao.MemberDao
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.validator.MemberNameValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [MemberRepository].
 */
class MemberRepositoryImpl(private val dao: MemberDao) : MemberRepository {

    override fun getAllMembers(): Flow<List<Member>> =
        dao.getAllMembers().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createMember(member: Member): Long {
        require(MemberNameValidator.isValid(member.name)) {
            "Invalid member name: '${member.name}'"
        }
        return try {
            dao.insertMember(member.toEntity())
        } catch (e: SQLiteConstraintException) {
            throw IllegalStateException("A member named '${member.name.trim()}' already exists.", e)
        }
    }

    override suspend fun updateMember(member: Member) {
        require(MemberNameValidator.isValid(member.name)) {
            "Invalid member name: '${member.name}'"
        }
        dao.updateMember(member.toEntity())
    }

    override suspend fun deleteMember(member: Member) {
        dao.deleteMember(member.toEntity())
    }

    // ── Mapping ───────────────────────────────────────────────────────────────────

    private fun MemberEntity.toDomain() = Member(
        id = id,
        name = name,
        colorHex = colorHex,
        createdAt = createdAt,
    )

    private fun Member.toEntity() = MemberEntity(
        id = id,
        name = name.trim(),
        colorHex = colorHex,
        createdAt = createdAt,
    )
}
