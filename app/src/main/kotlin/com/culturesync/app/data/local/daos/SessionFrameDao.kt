package com.culturesync.app.data.local.daos

import androidx.room.*
import com.culturesync.app.data.local.entities.SessionFrameEntity

@Dao
interface SessionFrameDao {
    @Insert
    suspend fun insertFrame(frame: SessionFrameEntity): Long

    @Insert
    suspend fun insertFrames(frames: List<SessionFrameEntity>)

    @Query("SELECT * FROM session_frames WHERE sessionId = :sessionId ORDER BY frameIndex ASC")
    suspend fun getFramesForSession(sessionId: Int): List<SessionFrameEntity>

    @Query("SELECT AVG(overallAccuracy) FROM session_frames WHERE sessionId = :sessionId")
    suspend fun getAverageAccuracyForSession(sessionId: Int): Float?

    @Query("SELECT MAX(overallAccuracy) FROM session_frames WHERE sessionId = :sessionId")
    suspend fun getBestAccuracyForSession(sessionId: Int): Float?

    @Query("SELECT COUNT(*) FROM session_frames WHERE sessionId = :sessionId")
    suspend fun getFrameCountForSession(sessionId: Int): Int

    @Query("DELETE FROM session_frames WHERE sessionId = :sessionId")
    suspend fun deleteFramesForSession(sessionId: Int)
}
