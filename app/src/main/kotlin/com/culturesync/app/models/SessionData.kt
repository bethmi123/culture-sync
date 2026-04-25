package com.culturesync.app.models

data class SessionData(
    val id: Int = 0,
    val userId: Int,
    val techniqueId: String,
    val techniqueName: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Float,
    val accuracyScore: Float,       // 0.0 – 100.0 (from DTW, NOT hardcoded)
    val frameCount: Int,
    val synced: Boolean = false     // false until uploaded to MongoDB
)
