package com.culturesync.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.culturesync.app.data.remote.SyncManager

/**
 * Background worker for periodic database synchronization (PRD §5.3).
 * Triggered by WorkManager hourly when charging or on Wi-Fi (recommended).
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val syncManager = SyncManager(applicationContext)
        
        return try {
            if (syncManager.isOnline()) {
                syncManager.syncAll()
                Result.success()
            } else {
                // Retry later if offline (handled by WorkManager constraints usually)
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
