package com.culturesync.app.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.R
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.remote.SyncManager
import com.culturesync.app.data.repository.SessionRepository
import com.culturesync.app.databinding.ActivityProfileBinding
import com.culturesync.app.ui.auth.LoginActivity
import com.culturesync.app.utils.SessionManager
import com.culturesync.app.utils.toTimeString
import kotlinx.coroutines.launch

/**
 * Profile screen (PRD Screen #14).
 * Display name, email, user type, join date. Sync status indicator.
 */
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager
    private lateinit var repo: SessionRepository
    private lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        repo    = SessionRepository(AppDatabase.getInstance(this))
        syncManager = SyncManager(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnLogout.setOnClickListener { confirmLogout() }

        binding.tvName.text  = session.getUserName()
        binding.tvEmail.text = session.getUserEmail()
        
        val roleKey = when (session.getUserType().lowercase()) {
            "student"   -> R.string.role_student
            "tourist"   -> R.string.role_tourist
            "artisan"   -> R.string.role_artisan
            "shopowner" -> R.string.role_shopowner
            else -> R.string.role_student
        }
        binding.tvRole.text = getString(roleKey)

        loadStats()
    }

    private fun loadStats() {
        val userId = session.getUserId()
        lifecycleScope.launch {
            binding.tvTotalSessions.text = "${repo.getTotalSessions(userId)}"
            binding.tvAvgAccuracy.text   = "${(repo.getAverageAccuracy(userId) * 100).toInt()}%"
            binding.tvBestAccuracy.text  = "${(repo.getBestAccuracy(userId) * 100).toInt()}%"
            binding.tvTotalTime.text     = repo.getTotalPracticeSeconds(userId).toTimeString()
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_logout_title))
            .setMessage(getString(R.string.dialog_logout_msg))
            .setPositiveButton(getString(R.string.dialog_logout_title)) { _, _ ->
                session.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }
}
