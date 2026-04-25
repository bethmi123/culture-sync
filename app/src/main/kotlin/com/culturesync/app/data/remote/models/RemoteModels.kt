package com.culturesync.app.data.remote.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(val name: String, val email: String, val password: String)

data class AuthResponse(
    val token: String,
    val user: RemoteUser
)

data class RemoteUser(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class SessionUploadRequest(
    val userId: Int,
    val techniqueId: Int,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Float,
    val accuracyScore: Float,
    val frameCount: Int
)

data class RemoteSession(
    @SerializedName("_id") val id: String,
    val techniqueId: String?,
    val techniqueName: String?,
    val accuracyScore: Float,
    val durationSeconds: Float,
    val startTime: Long
)

data class LeaderboardEntry(
    val userId: String,
    val userName: String,
    val averageAccuracy: Float,
    val totalSessions: Int,
    val rank: Int
)

data class RemoteTechnique(
    @SerializedName("_id") val id: String,
    val techniqueId: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val durationMinutes: Int,
    val videoUrl: String?,
    val thumbnailUrl: String?
)

data class SyncPushRequest(
    val sessions: List<SessionUploadRequest>
)

data class SyncPushResponse(
    val synced: Int
)
