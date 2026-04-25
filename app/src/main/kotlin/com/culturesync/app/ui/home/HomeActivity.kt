package com.culturesync.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.remote.SyncManager
import com.culturesync.app.data.repository.SessionRepository
import com.culturesync.app.databinding.ActivityHomeBinding
import com.culturesync.app.ui.culture.CulturalStoriesActivity
import com.culturesync.app.ui.progress.ProgressActivity
import com.culturesync.app.ui.profile.ProfileActivity
import com.culturesync.app.ui.settings.SettingsActivity
import com.culturesync.app.ui.techniques.TechniqueListActivity
import com.culturesync.app.utils.SessionManager
import com.culturesync.app.utils.toTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager
    private lateinit var repo: SessionRepository
    private lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        repo    = SessionRepository(AppDatabase.getInstance(this))
        syncManager = SyncManager(this)

        val firstName = session.getUserName().split(" ").first()
        binding.tvWelcome.text = getString(com.culturesync.app.R.string.label_welcome_user, firstName)

        binding.cardLearn.setOnClickListener {
            startActivity(Intent(this, TechniqueListActivity::class.java))
        }
        binding.cardProgress.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
        binding.cardStories.setOnClickListener {
            startActivity(Intent(this, CulturalStoriesActivity::class.java))
        }
        binding.ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.ivSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.culturesync.app.R.id.nav_home -> true
                com.culturesync.app.R.id.nav_practice -> {
                    startActivity(Intent(this, TechniqueListActivity::class.java))
                    false
                }
                com.culturesync.app.R.id.nav_progress -> {
                    startActivity(Intent(this, ProgressActivity::class.java))
                    false
                }
                com.culturesync.app.R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }

        loadStats()
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }

    private fun loadStats() {
        val userId = session.getUserId()

        lifecycleScope.launch {
            // Background sync when online (§4.3)
            if (syncManager.isOnline() && session.getAuthToken().isNotEmpty()) {
                launch(Dispatchers.IO) {
                    try { syncManager.syncPendingSessions() } catch (e: Exception) { e.printStackTrace() }
                }
            }

            val totalSessions  = repo.getTotalSessions(userId)
            val avgAccuracy    = repo.getAverageAccuracy(userId)
            val bestAccuracy   = repo.getBestAccuracy(userId)
            val totalSeconds   = repo.getTotalPracticeSeconds(userId)

            binding.tvTotalSessions.text = totalSessions.toString()
            binding.tvAvgAccuracy.text   = if (avgAccuracy > 0) getString(com.culturesync.app.R.string.percentage_format, (avgAccuracy * 100).toInt()) else getString(com.culturesync.app.R.string.default_accuracy)
            binding.tvBestAccuracy.text  = if (bestAccuracy > 0) getString(com.culturesync.app.R.string.percentage_format, (bestAccuracy * 100).toInt()) else getString(com.culturesync.app.R.string.default_accuracy)
            binding.tvTotalTime.text     = if (totalSeconds > 0) totalSeconds.toTimeString() else getString(com.culturesync.app.R.string.default_accuracy)
        }
    }
}
