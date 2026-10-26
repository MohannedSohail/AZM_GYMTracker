package com.example.azm_gymtracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.azm_gymtracker.data.local.dao.MemberDao
import com.example.azm_gymtracker.data.local.db.AZMDatabase
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * In-memory Room database tests for [MemberDao].
 *
 * Verifies: CRUD operations, unique name constraint, and member isolation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class MemberDaoTest {

    private lateinit var db: AZMDatabase
    private lateinit var dao: MemberDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AZMDatabase::class.java)
            .allowMainThreadQueries() // Permitted only in tests.
            .build()
        dao = db.memberDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetAllMembers() = runTest {
        val member = MemberEntity(name = "Ali", colorHex = "#FF5722")
        dao.insertMember(member)

        val members = dao.getAllMembers().first()
        assertEquals(1, members.size)
        assertEquals("Ali", members[0].name)
    }

    @Test
    fun getMemberById_returnsCorrectMember() = runTest {
        val id = dao.insertMember(MemberEntity(name = "Sara", colorHex = "#4CAF50"))
        val fetched = dao.getMemberById(id)
        assertNotNull(fetched)
        assertEquals("Sara", fetched!!.name)
    }

    @Test
    fun getMemberById_returnsNullForMissingId() = runTest {
        val fetched = dao.getMemberById(999L)
        assertNull(fetched)
    }

    @Test
    fun updateMember_changesName() = runTest {
        val id = dao.insertMember(MemberEntity(name = "Old Name", colorHex = "#9C27B0"))
        val existing = dao.getMemberById(id)!!
        dao.updateMember(existing.copy(name = "New Name"))

        val updated = dao.getMemberById(id)
        assertEquals("New Name", updated?.name)
    }

    @Test
    fun deleteMember_removesFromList() = runTest {
        val id = dao.insertMember(MemberEntity(name = "To Delete", colorHex = "#607D8B"))
        val member = dao.getMemberById(id)!!
        dao.deleteMember(member)

        val members = dao.getAllMembers().first()
        assertEquals(0, members.size)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insertDuplicateName_throwsConstraintException() = runTest {
        dao.insertMember(MemberEntity(name = "Duplicate", colorHex = "#FF5722"))
        dao.insertMember(MemberEntity(name = "Duplicate", colorHex = "#2196F3"))
    }

    @Test
    fun multipleMembers_areReturnedInCreationOrder() = runTest {
        dao.insertMember(MemberEntity(name = "Ahmed", colorHex = "#FF5722", createdAt = 1000L))
        dao.insertMember(MemberEntity(name = "Fatima", colorHex = "#4CAF50", createdAt = 2000L))
        dao.insertMember(MemberEntity(name = "Omar", colorHex = "#2196F3", createdAt = 3000L))

        val members = dao.getAllMembers().first()
        assertEquals(listOf("Ahmed", "Fatima", "Omar"), members.map { it.name })
    }
}
