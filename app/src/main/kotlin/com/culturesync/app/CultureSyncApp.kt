package com.culturesync.app

import android.app.Application
import androidx.work.*
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.sync.SyncWorker
import java.util.concurrent.TimeUnit

class CultureSyncApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        
        // 1. Boot up the dynamically configurable Retrofit network router
        val session = com.culturesync.app.utils.SessionManager(this)
        com.culturesync.app.data.remote.RetrofitClient.dynamicBaseUrl = session.getApiUrl()

        // 2. Schedule Periodic Sync Worker (PRD §5.3)
        scheduleSync()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CultureSync_Background_Sync",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            syncRequest
        )
    }
}
