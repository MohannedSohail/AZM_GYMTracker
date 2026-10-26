package com.example.azm_gymtracker.data.repository

import com.example.azm_gymtracker.data.local.dao.WorkoutDao
import com.example.azm_gymtracker.data.local.entity.ExerciseRecordTuple
import com.example.azm_gymtracker.data.local.entity.ParticipantVolumeTuple
import com.example.azm_gymtracker.data.local.entity.WeightProgressionPointTuple
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionEntity
import com.example.azm_gymtracker.data.local.entity.WorkoutSessionWithDetails
import com.example.azm_gymtracker.data.local.entity.WorkoutSetEntity
import com.example.azm_gymtracker.domain.model.SessionStatus
import com.example.azm_gymtracker.domain.model.WorkoutSession
import com.example.azm_gymtracker.domain.model.WorkoutSet
import com.example.azm_gymtracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of [WorkoutRepository].
 */
class WorkoutRepositoryImpl(private val dao: WorkoutDao) : WorkoutRepository {

    override fun getActiveSession(): Flow<WorkoutSession?> =
        dao.getActiveSession().map { entity -> entity?.toDomain() }

    override fun getActiveSessionWithDetails(): Flow<WorkoutSessionWithDetails?> =
        dao.getActiveSessionWithDetails()

    override fun getSessionWithDetails(sessionId: Long): Flow<WorkoutSessionWithDetails?> =
        dao.getSessionWithDetails(sessionId)

    override fun getCompletedSessions(memberId: Long): Flow<List<WorkoutSession>> =
        dao.getCompletedSessionsForMember(memberId).map { entities -> entities.map { it.toDomain() } }

    override fun getCompletedSessionsWithDetails(memberId: Long?): Flow<List<WorkoutSessionWithDetails>> =
        if (memberId == null) {
            dao.getAllCompletedSessionsWithDetails()
        } else {
            dao.getCompletedSessionsWithDetailsForMember(memberId)
        }

    override suspend fun startSession(participantIds: List<Long>): Long {
        return dao.startSessionWithParticipants(
            session = WorkoutSessionEntity(status = "IN_PROGRESS"),
            participantIds = participantIds,
        )
    }

    override suspend fun addExerciseToSession(
        sessionId: Long,
        exerciseId: Long,
        exerciseNameSnapshot: String,
    ): Long {
        return dao.addExerciseToSession(sessionId, exerciseId, exerciseNameSnapshot)
    }

    override suspend fun addSet(
        workoutExerciseId: Long,
        memberId: Long,
        reps: Int,
        weightKg: Float?,
    ): Long {
        return dao.addSetAtomic(workoutExerciseId, memberId, reps, weightKg)
    }

    override suspend fun markSetCompleted(setId: Long) {
        dao.markSetCompleted(setId)
    }

    override suspend fun updateSet(set: WorkoutSet) {
        dao.updateSet(set.toEntity())
    }

    override suspend fun deleteSet(setId: Long, workoutExerciseId: Long) {
        dao.deleteSetAndReindex(setId, workoutExerciseId)
    }

    override suspend fun finishSession(sessionId: Long) {
        dao.finishSession(sessionId)
    }

    override suspend fun cancelSession(sessionId: Long) {
        dao.cancelSession(sessionId)
    }

    override suspend fun deleteSession(sessionId: Long) {
        dao.deleteSession(sessionId)
    }

    override suspend fun updateSessionPausedTime(sessionId: Long, pausedTotalMs: Long) {
        dao.updateSessionPausedTime(sessionId, pausedTotalMs)
    }

    override suspend fun getLastSetForMemberInExercise(
        exerciseId: Long,
        memberId: Long,
    ): WorkoutSetEntity? {
        return dao.getLastSetForMemberInExercise(exerciseId, memberId)
    }

    override suspend fun getLastSetInExercise(exerciseId: Long): WorkoutSetEntity? {
        return dao.getLastSetInExercise(exerciseId)
    }

    override fun getParticipantVolumeBreakdown(sessionId: Long): Flow<List<ParticipantVolumeTuple>> =
        dao.getParticipantVolumeBreakdown(sessionId)

    override fun getPersonalRecordsForMember(memberId: Long): Flow<List<ExerciseRecordTuple>> =
        dao.getPersonalRecordsForMember(memberId)

    override fun getTotalVolumeForMember(memberId: Long): Flow<Double?> =
        dao.getTotalVolumeForMember(memberId)

    override fun getWeightProgression(
        memberId: Long,
        exerciseId: Long,
    ): Flow<List<WeightProgressionPointTuple>> =
        dao.getWeightProgression(memberId, exerciseId)

    // ── Mapping ───────────────────────────────────────────────────────────────────

    private fun WorkoutSessionEntity.toDomain() = WorkoutSession(
        id = id,
        startedAt = startedAt,
        endedAt = endedAt,
        pausedTotalMs = pausedTotalMs,
        status = when (status) {
            "COMPLETED" -> SessionStatus.COMPLETED
            "CANCELLED", "DISCARDED" -> SessionStatus.CANCELLED
            else -> SessionStatus.IN_PROGRESS
        },
        notes = notes,
        participants = emptyList(),
        exercises = emptyList(),
    )

    private fun WorkoutSet.toEntity() = WorkoutSetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        memberId = memberId,
        setNumber = setNumber,
        reps = reps,
        weightKg = weightKg,
        isCompleted = isCompleted,
        completedAt = completedAt,
    )
}
