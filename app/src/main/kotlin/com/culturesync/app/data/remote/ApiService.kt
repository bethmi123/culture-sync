package com.culturesync.app.data.remote

import com.culturesync.app.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- Auth ---
    @POST("api/v1/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("api/v1/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<ApiResponse<AuthResponse>>

    // --- Sessions ---
    @POST("api/v1/sessions")
    suspend fun uploadSession(
        @Header("Authorization") token: String,
        @Body session: SessionUploadRequest
    ): Response<ApiResponse<RemoteSession>>

    @GET("api/v1/sessions")
    suspend fun getUserSessions(
        @Header("Authorization") token: String,
        @Query("userId") userId: String
    ): Response<ApiResponse<List<RemoteSession>>>

    // --- Leaderboard ---
    @GET("api/v1/leaderboard")
    suspend fun getLeaderboard(): Response<ApiResponse<List<LeaderboardEntry>>>

    // --- Techniques ---
    @GET("api/v1/techniques")
    suspend fun getTechniques(): Response<ApiResponse<List<RemoteTechnique>>>

    // --- Sync (§5.3) ---
    @POST("api/v1/sync/push")
    suspend fun syncPush(
        @Header("Authorization") token: String,
        @Body body: SyncPushRequest
    ): Response<ApiResponse<SyncPushResponse>>
}
