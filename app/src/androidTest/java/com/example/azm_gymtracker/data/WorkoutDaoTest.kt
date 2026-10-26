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
import com.example.azm_gymtracker.data.local.entity.WorkoutExerciseEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
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
 * In-memory Room database tests for [WorkoutDao].
 *
 * Verifies: session lifecycle, cascade deletion from members, set re-indexing,
 * and concurrent active sessions per member.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class WorkoutDaoTest {

    private lateinit var db: AZMDatabase
    private lateinit var memberDao: MemberDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var workoutDao: WorkoutDao

    private var memberId1 = 0L
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
        exerciseId = exerciseDao.insertExercise(
            ExerciseEntity(nameEn = "Bench Press", nameAr = "ضغط المقعد", category = "CHEST")
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    // ── Session Lifecycle ────────────────────────────────────────────────────────

    @Test
    fun insertSession_andGetActiveSession() = runTest {
        workoutDao.insertSession(
            WorkoutSessionEntity(memberId = memberId1, status = "IN_PROGRESS")
        )
        val active = workoutDao.getActiveSession(memberId1).first()
        assertNotNull(active)
        assertEquals("IN_PROGRESS", active!!.status)
    }

    @Test
    fun finishSession_noLongerShowsAsActive() = runTest {
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(memberId = memberId1, status = "IN_PROGRESS")
        )
        workoutDao.updateSession(
            WorkoutSessionEntity(
                id = sessionId,
                memberId = memberId1,
                status = "COMPLETED",
                finishedAt = System.currentTimeMillis(),
            )
        )
        val active = workoutDao.getActiveSession(memberId1).first()
        assertNull(active)
    }

    @Test
    fun completedSessions_returnedNewestFirst() = runTest {
        val id1 = workoutDao.insertSession(
            WorkoutSessionEntity(memberId = memberId1, startedAt = 1000L)
        )
        val id2 = workoutDao.insertSession(
            WorkoutSessionEntity(memberId = memberId1, startedAt = 2000L)
        )
        workoutDao.updateSession(
            WorkoutSessionEntity(id = id1, memberId = memberId1, status = "COMPLETED", finishedAt = 1500L)
        )
        workoutDao.updateSession(
            WorkoutSessionEntity(id = id2, memberId = memberId1, status = "COMPLETED", finishedAt = 2500L)
        )
        val history = workoutDao.getCompletedSessions(memberId1).first()
        assertEquals(2, history.size)
        assertEquals(id2, history[0].id) // Newest first
    }

    // ── Cascade Delete ───────────────────────────────────────────────────────────

    @Test
    fun deleteMember_cascadesSessionExercisesAndSets() = runTest {
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(memberId = memberId1, status = "IN_PROGRESS")
        )
        val workoutExerciseId = workoutDao.insertWorkoutExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseNameSnapshot = "Bench Press",
                orderIndex = 0,
            )
        )
        workoutDao.insertSet(
            WorkoutSetEntity(workoutExerciseId = workoutExerciseId, setNumber = 1, reps = 10, weightKg = 80f)
        )

        // Delete the owning member — cascade should wipe everything
        val member = memberDao.getMemberById(memberId1)!!
        memberDao.deleteMember(member)

        val activeSessions = workoutDao.getActiveSession(memberId1).first()
        assertNull(activeSessions)

        val sets = workoutDao.getSetsForExercise(workoutExerciseId).first()
        assertEquals(0, sets.size)
    }

    // ── Set Re-indexing ──────────────────────────────────────────────────────────

    @Test
    fun deleteMiddleSet_reindexesRemainingSets() = runTest {
        val sessionId = workoutDao.insertSession(
            WorkoutSessionEntity(memberId = memberId1, status = "IN_PROGRESS")
        )
        val workoutExerciseId = workoutDao.insertWorkoutExercise(
            WorkoutExerciseEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                exerciseNameSnapshot = "Bench Press",
                orderIndex = 0,
            )
        )
        workoutDao.insertSet(WorkoutSetEntity(workoutExerciseId = workoutExerciseId, setNumber = 1, reps = 10))
        val setId2 = workoutDao.insertSet(
            WorkoutSetEntity(workoutExerciseId = workoutExerciseId, setNumber = 2, reps = 8)
        )
        workoutDao.insertSet(WorkoutSetEntity(workoutExerciseId = workoutExerciseId, setNumber = 3, reps = 6))

        // Delete set #2 and re-index
        workoutDao.deleteSetAndReindex(setId2, workoutExerciseId)

        val remaining = workoutDao.getSetsForExercise(workoutExerciseId).first()
        assertEquals(2, remaining.size)
        assertEquals(1, remaining[0].setNumber)
        assertEquals(2, remaining[1].setNumber) // Was #3, now re-indexed to #2
    }

    // ── Member Isolation ─────────────────────────────────────────────────────────

    @Test
    fun activeSession_isolatedPerMember() = runTest {
        val memberId2 = memberDao.insertMember(MemberEntity(name = "Sara", colorHex = "#4CAF50"))
        workoutDao.insertSession(WorkoutSessionEntity(memberId = memberId1, status = "IN_PROGRESS"))

        val activeForMember2 = workoutDao.getActiveSession(memberId2).first()
        assertNull(activeForMember2) // Sara should not see Ali's session
    }
}
