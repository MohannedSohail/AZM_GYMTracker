package com.example.azm_gymtracker.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.azm_gymtracker.data.local.dao.AppSettingDao
import com.example.azm_gymtracker.data.local.dao.MemberDao
import com.example.azm_gymtracker.data.local.entity.AppSettingEntity
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.domain.model.Member
import com.example.azm_gymtracker.domain.repository.MemberRepository
import com.example.azm_gymtracker.domain.validator.MemberNameValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [MemberRepository].
 */
class MemberRepositoryImpl(
    private val memberDao: MemberDao,
    private val appSettingDao: AppSettingDao,
) : MemberRepository {

    companion object {
        private const val KEY_ACTIVE_MEMBER_ID = "active_member_id"
    }

    override fun getAllMembers(): Flow<List<Member>> =
        memberDao.getAllMembers().map { entities -> entities.map { it.toDomain() } }

    override fun getActiveMemberId(): Flow<Long?> =
        appSettingDao.getSetting(KEY_ACTIVE_MEMBER_ID).map { it?.toLongOrNull() }

    override suspend fun setActiveMemberId(memberId: Long) {
        appSettingDao.setSetting(AppSettingEntity(KEY_ACTIVE_MEMBER_ID, memberId.toString()))
    }

    override suspend fun getMemberCount(): Int =
        memberDao.getMemberCount()

    override suspend fun createMember(member: Member): Long {
        require(MemberNameValidator.isValid(member.name)) {
            "Invalid member name: '${member.name}'"
        }
        return try {
            val id = memberDao.insertMember(member.toEntity())
            // If this is the only member, automatically make them active
            if (memberDao.getMemberCount() == 1) {
                setActiveMemberId(id)
            }
            id
        } catch (e: SQLiteConstraintException) {
            throw IllegalStateException("A member named '${member.name.trim()}' already exists.", e)
        }
    }

    override suspend fun updateMember(member: Member) {
        require(MemberNameValidator.isValid(member.name)) {
            "Invalid member name: '${member.name}'"
        }
        memberDao.updateMember(member.toEntity())
    }

    override suspend fun deleteMember(member: Member) {
        memberDao.deleteMemberWithCleanup(member.id)
        // If the deleted member was active, clear the setting
        val activeId = appSettingDao.getSettingDirect(KEY_ACTIVE_MEMBER_ID)?.toLongOrNull()
        if (activeId == member.id) {
            appSettingDao.removeSetting(KEY_ACTIVE_MEMBER_ID)
        }
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
