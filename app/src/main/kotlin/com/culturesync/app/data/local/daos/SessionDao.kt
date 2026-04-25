package com.culturesync.app.data.local.daos

import androidx.room.*
import com.culturesync.app.data.local.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getSessionsByUser(userId: Int): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(userId: Int, limit: Int = 10): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): SessionEntity?

    @Query("SELECT * FROM sessions WHERE syncStatus = 'PENDING' AND status = 'COMPLETED'")
    suspend fun getUnsyncedSessions(): List<SessionEntity>

    @Query("UPDATE sessions SET syncStatus = 'SYNCED', remoteId = :remoteId WHERE id = :sessionId")
    suspend fun markSynced(sessionId: Int, remoteId: String)

    @Query("SELECT AVG(finalAccuracy) FROM sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getAverageAccuracy(userId: Int): Float?

    @Query("SELECT MAX(finalAccuracy) FROM sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getBestAccuracy(userId: Int): Float?

    @Query("SELECT COUNT(*) FROM sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getTotalSessions(userId: Int): Int

    @Query("SELECT SUM(durationSeconds) FROM sessions WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getTotalPracticeSeconds(userId: Int): Int?

    @Query("SELECT * FROM sessions WHERE userId = :userId AND techniqueId = :techniqueId AND status = 'COMPLETED' ORDER BY startTime DESC LIMIT 1")
    suspend fun getLastSessionForTechnique(userId: Int, techniqueId: Int): SessionEntity?

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("UPDATE sessions SET status = 'COMPLETED', endTime = :endTime, durationSeconds = :duration, finalAccuracy = :accuracy, bestFrameAccuracy = :bestFrame WHERE id = :sessionId")
    suspend fun closeSession(sessionId: Int, endTime: Long, duration: Int, accuracy: Float, bestFrame: Float)

    @Query("SELECT COUNT(*) FROM session_frames WHERE sessionId = :sessionId")
    suspend fun getFrameCount(sessionId: Int): Int
}
