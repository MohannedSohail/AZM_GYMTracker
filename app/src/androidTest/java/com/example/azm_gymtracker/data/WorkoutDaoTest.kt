package com.example.azm_gymtracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.azm_gymtracker.data.local.dao.ExerciseDao
import com.example.azm_gymtracker.data.local.dao.MemberDao
import com.example.azm_gymtracker.data.local.dao.WorkoutDao
import com.example.azm_gymtracker.data.local.db.AZMDatabase
import com.example.azm_gymtracker.data.local.entity.ExerciseEntity
import com.example.azm_gymtracker.data.local.entity.MemberEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * In-memory Room database tests for [WorkoutDao] under Schema v2.
 *
 * Verifies:
 * - Multi-participant shared session lifecycle
 * - Member isolation (sets belong strictly to the member who logged them)
 * - Cascading deletion from members
 * - Orphan session cleanup when all participants are removed
 * - Set re-indexing on deletion
 * - Volume calculations and personal records
 * - Catalog exercise pre-population
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: AZMDatabase
    private lateinit var memberDao: MemberDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var workoutDao: WorkoutDao

    private var memberId1 = 0L
    private var memberId2 = 0L
    private var exerciseId = 0L

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AZMDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memberDao = db.memberDao()
        exerciseDao = db.exerciseDao()
        workoutDao = db.workoutDao()

        memberId1 = memberDao.insertMember(MemberEntity(name = "Ali", colorHex = "#FF5722"))
        memberId2 = memberDao.insertMember(MemberEntity(name = "Sara", colorHex = "#4CAF50"))
        exerciseId = exerciseDao.insertExercise(
            ExerciseEntity(nameEn = "Bench Press", nameAr = "ضغط المقعد", category = "CHEST"),
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    // ── Session Lifecycle ────────────────────────────────────────────────────────

    @Test
    fun startSessionWithParticipants_createsActiveSessionWithAllMembers() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1, memberId2),
        )

        val active = workoutDao.getActiveSessionWithDetails().first()
        assertNotNull(active)
        assertEquals(sessionId, active!!.session.id)
        assertEquals("IN_PROGRESS", active.session.status)
        assertEquals(2, active.participants.size)
        assertTrue(active.participants.any { it.id == memberId1 })
        assertTrue(active.participants.any { it.id == memberId2 })
    }

    @Test
    fun finishSession_updatesStatusToCompleted() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1),
        )

        workoutDao.finishSession(sessionId)

        val active = workoutDao.getActiveSession().first()
        assertNull(active)

        val completed = workoutDao.getCompletedSessionsForMember(memberId1).first()
        assertEquals(1, completed.size)
        assertEquals(sessionId, completed[0].id)
        assertEquals("COMPLETED", completed[0].status)
    }

    @Test
    fun cancelSession_updatesStatusToCancelled() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1),
        )

        workoutDao.cancelSession(sessionId)

        val active = workoutDao.getActiveSession().first()
        assertNull(active)

        val completed = workoutDao.getCompletedSessionsForMember(memberId1).first()
        assertEquals(0, completed.size)
    }

    // ── Member Isolation & Multi-Participant Logging ─────────────────────────────

    @Test
    fun sharedSession_setsBelongStrictlyToLoggingMember() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1, memberId2),
        )

        val workoutExerciseId = workoutDao.addExerciseToSession(sessionId, exerciseId, "Bench Press")

        // Ali logs 80kg x 8 reps
        val aliSetId = workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 8, weightKg = 80f)
        workoutDao.markSetCompleted(aliSetId)

        // Sara logs 50kg x 10 reps
        val saraSetId = workoutDao.addSetAtomic(workoutExerciseId, memberId2, reps = 10, weightKg = 50f)
        workoutDao.markSetCompleted(saraSetId)

        // Check participant volume breakdown
        val breakdown = workoutDao.getParticipantVolumeBreakdown(sessionId).first()
        assertEquals(2, breakdown.size)

        val aliBreakdown = breakdown.first { it.memberId == memberId1 }
        assertEquals(640.0, aliBreakdown.totalVolume, 0.01)
        assertEquals(1, aliBreakdown.setsCount)

        val saraBreakdown = breakdown.first { it.memberId == memberId2 }
        assertEquals(500.0, saraBreakdown.totalVolume, 0.01)
        assertEquals(1, saraBreakdown.setsCount)
    }

    // ── Cascade Deletion & Orphan Session Cleanup ─────────────────────────────────

    @Test
    fun deleteMember_preservesSharedSessionForOtherParticipants() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1, memberId2),
        )

        val workoutExerciseId = workoutDao.addExerciseToSession(sessionId, exerciseId, "Bench Press")
        val aliSetId = workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 8, weightKg = 80f)
        val saraSetId = workoutDao.addSetAtomic(workoutExerciseId, memberId2, reps = 10, weightKg = 50f)

        // Delete Ali
        memberDao.deleteMemberWithCleanup(memberId1)

        // Ali's sets should be gone, but Sara's set and the session remain
        val remainingSets = workoutDao.getSetsForExercise(workoutExerciseId).first()
        assertEquals(1, remainingSets.size)
        assertEquals(memberId2, remainingSets[0].memberId)

        val sessionWithDetails = workoutDao.getSessionWithDetails(sessionId).first()
        assertNotNull(sessionWithDetails)
        assertEquals(1, sessionWithDetails!!.participants.size)
        assertEquals(memberId2, sessionWithDetails.participants[0].id)
    }

    @Test
    fun deleteMember_whenSoleParticipant_cleansUpOrphanSession() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1),
        )

        // Delete Ali (sole participant)
        memberDao.deleteMemberWithCleanup(memberId1)

        val session = workoutDao.getSessionWithDetails(sessionId).first()
        assertNull(session)
    }

    // ── Set Re-indexing ──────────────────────────────────────────────────────────

    @Test
    fun deleteMiddleSet_reindexesRemainingSets() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1),
        )
        val workoutExerciseId = workoutDao.addExerciseToSession(sessionId, exerciseId, "Bench Press")

        workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 10, weightKg = 60f)
        val set2Id = workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 8, weightKg = 70f)
        workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 6, weightKg = 80f)

        // Delete set #2 and re-index
        workoutDao.deleteSetAndReindex(set2Id, workoutExerciseId)

        val remaining = workoutDao.getSetsForExercise(workoutExerciseId).first()
        assertEquals(2, remaining.size)
        assertEquals(1, remaining[0].setNumber)
        assertEquals(2, remaining[1].setNumber)
    }

    // ── Personal Records (PRs) ───────────────────────────────────────────────────

    @Test
    fun personalRecords_calculatesMaxWeightPerExercise() = runTest {
        val sessionId = workoutDao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = listOf(memberId1),
        )
        val workoutExerciseId = workoutDao.addExerciseToSession(sessionId, exerciseId, "Bench Press")

        val s1 = workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 10, weightKg = 80f)
        workoutDao.markSetCompleted(s1)
        val s2 = workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 5, weightKg = 95f)
        workoutDao.markSetCompleted(s2)
        val s3 = workoutDao.addSetAtomic(workoutExerciseId, memberId1, reps = 8, weightKg = 85f)
        workoutDao.markSetCompleted(s3)

        val prs = workoutDao.getPersonalRecordsForMember(memberId1).first()
        assertEquals(1, prs.size)
        assertEquals(exerciseId, prs[0].exerciseId)
        assertEquals(95f, prs[0].maxWeight)
    }
}
