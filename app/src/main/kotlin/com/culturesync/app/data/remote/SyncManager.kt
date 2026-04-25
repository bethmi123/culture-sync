package com.culturesync.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.remote.RetrofitClient
import com.culturesync.app.data.remote.models.SessionUploadRequest
import com.culturesync.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SyncManager — detects network availability via ConnectivityManager
 * and pushes queued PENDING deltas to MongoDB Atlas (§4.3).
 *
 * Room is always the source of truth. Sync is one-way (Android → Atlas) for MVP.
 * All writes go to Room first; this manager pushes PENDING records when online.
 */
class SyncManager(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val sessionManager = SessionManager(context)

    /** Returns true if the device has active network connectivity. */
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Push all PENDING completed sessions to MongoDB Atlas.
     * Must be called on a background thread (Dispatchers.IO).
     * Sync is idempotent — duplicate push updates, not creates (FR-AUTH-03).
     */
    suspend fun syncPendingSessions() = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext

        val token = sessionManager.getAuthToken()
        if (token.isEmpty()) return@withContext

        val unsyncedSessions = db.sessionDao().getUnsyncedSessions()
        if (unsyncedSessions.isEmpty()) return@withContext

        val payload = com.culturesync.app.data.remote.models.SyncPushRequest(
            sessions = unsyncedSessions.map { session ->
                com.culturesync.app.data.remote.models.SessionUploadRequest(
                    userId = session.userId,
                    techniqueId = session.techniqueId,
                    startTime = session.startTime,
                    endTime = session.endTime ?: 0L,
                    durationSeconds = (session.durationSeconds ?: 0).toFloat(),
                    accuracyScore = (session.finalAccuracy ?: 0f) * 100f,
                    frameCount = db.sessionDao().getFrameCount(session.id)
                )
            }
        )

        try {
            val response = RetrofitClient.api.syncPush(
                token = "Bearer $token",
                body = payload
            )
            if (response.isSuccessful) {
                unsyncedSessions.forEach {
                    db.sessionDao().markSynced(it.id, "")
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("SyncManager", "Batch session sync failed", e)
        }
    }

    /**
     * Push all PENDING users to MongoDB Atlas.
     */
    suspend fun syncPendingUsers() = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext
        // User sync is handled during login/register via the auth API
        // This is a placeholder for future bulk sync if needed
    }

    /** Convenience: sync everything. */
    suspend fun syncAll() {
        syncPendingSessions()
        syncPendingUsers()
    }
}
