package com.culturesync.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_frames",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SessionFrameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Int,
    val frameIndex: Int,
    val timestampMs: Long,
    val overallAccuracy: Float,                // 0.0–1.0 DTWEngine overall score
    val perJointAccuracyJson: String           // JSON array of 21 Float values
)
