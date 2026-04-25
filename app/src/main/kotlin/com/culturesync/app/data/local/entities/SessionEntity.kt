package com.culturesync.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TechniqueEntity::class,
            parentColumns = ["id"],
            childColumns = ["techniqueId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("techniqueId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String? = null,
    val userId: Int,
    val techniqueId: Int,
    val startTime: Long,
    val endTime: Long? = null,
    val durationSeconds: Int? = null,
    val finalAccuracy: Float? = null,         // 0.0–1.0 — mean of all frame accuracies
    val bestFrameAccuracy: Float? = null,     // 0.0–1.0 — peak frame accuracy
    val status: String = "IN_PROGRESS",       // IN_PROGRESS | COMPLETED | ABORTED
    val syncStatus: String = "PENDING"        // PENDING | SYNCED | ERROR
)
