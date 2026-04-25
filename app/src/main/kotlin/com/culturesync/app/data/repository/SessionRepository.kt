package com.culturesync.app.data.repository

import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.SessionEntity
import com.culturesync.app.data.local.entities.SessionFrameEntity
import com.culturesync.app.ml.ARConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Single source of truth for session data (§4.3).
 * Writes go to Room first (offline-first). Sync handled by SyncManager.
 */
class SessionRepository(private val db: AppDatabase) {

    fun getSessionsByUser(userId: Int): Flow<List<SessionEntity>> =
        db.sessionDao().getSessionsByUser(userId)

    suspend fun createSession(session: SessionEntity): Long =
        db.sessionDao().insertSession(session)

    suspend fun closeSession(sessionId: Int, endTime: Long, duration: Int, accuracy: Float, bestFrame: Float) =
        db.sessionDao().closeSession(sessionId, endTime, duration, accuracy, bestFrame)

    suspend fun getAverageAccuracy(userId: Int): Float =
        db.sessionDao().getAverageAccuracy(userId) ?: 0f

    suspend fun getBestAccuracy(userId: Int): Float =
        db.sessionDao().getBestAccuracy(userId) ?: 0f

    suspend fun getTotalSessions(userId: Int): Int =
        db.sessionDao().getTotalSessions(userId)

    suspend fun getTotalPracticeSeconds(userId: Int): Int =
        db.sessionDao().getTotalPracticeSeconds(userId) ?: 0

    suspend fun getRecentSessions(userId: Int, limit: Int = 10): List<SessionEntity> =
        db.sessionDao().getRecentSessions(userId, limit)

    suspend fun deleteSession(session: SessionEntity) =
        db.sessionDao().deleteSession(session)

    /**
     * Save a batch of session frames to Room on Dispatchers.IO.
     * Called every SESSION_FRAME_BATCH_SIZE frames during practice (FR-SES-02).
     */
    suspend fun saveFrameBatch(frames: List<SessionFrameEntity>) = withContext(Dispatchers.IO) {
        db.sessionFrameDao().insertFrames(frames)
    }

    suspend fun getAverageFrameAccuracy(sessionId: Int): Float =
        db.sessionFrameDao().getAverageAccuracyForSession(sessionId) ?: 0f

    suspend fun getBestFrameAccuracy(sessionId: Int): Float =
        db.sessionFrameDao().getBestAccuracyForSession(sessionId) ?: 0f

    suspend fun getFramesForSession(sessionId: Int): List<SessionFrameEntity> =
        db.sessionFrameDao().getFramesForSession(sessionId)
}
