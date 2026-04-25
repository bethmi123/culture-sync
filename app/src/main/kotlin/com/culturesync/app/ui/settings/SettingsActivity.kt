package com.culturesync.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.BuildConfig
import com.culturesync.app.R
import com.culturesync.app.databinding.ActivitySettingsBinding
import com.culturesync.app.utils.Constants
import com.culturesync.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings screen (PRD Screen #15).
 * Language toggle (EN/Sinhala), GPU delegate toggle, overlay colour confirmation,
 * clear session data, app version, admin section.
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }

        // ── Language Toggle (NFR-L10N-01) ──────────────────────────
        val currentLang = session.getLanguage()
        binding.switchLanguage.isChecked = currentLang == "si"
        binding.tvLanguageValue.text = if (currentLang == "si") getString(R.string.label_sinhala) else getString(R.string.label_english)

        binding.switchLanguage.setOnCheckedChangeListener { _, isSinhala ->
            val lang = if (isSinhala) "si" else "en"
            session.setLanguage(lang)
            binding.tvLanguageValue.text = if (isSinhala) getString(R.string.label_sinhala) else getString(R.string.label_english)
            // Apply immediately without restart (NFR-L10N-01)
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(lang)
            )
        }

        // ── GPU Delegate Toggle (FR-HT-03) ─────────────────────────
        binding.switchGpuDelegate.isChecked = session.isGpuDelegateEnabled()
        binding.switchGpuDelegate.setOnCheckedChangeListener { _, enabled ->
            session.setGpuDelegate(enabled)
            Toast.makeText(this, getString(R.string.msg_gpu_toggle), Toast.LENGTH_SHORT).show()
        }

        // ── API URL ────────────────────────────────────────────────
        com.culturesync.app.data.remote.RetrofitClient.dynamicBaseUrl = session.getApiUrl()
        binding.etApiUrl.setText(session.getApiUrl())

        binding.btnSaveApiUrl.setOnClickListener {
            val newUrl = binding.etApiUrl.text.toString().trim()
            if (newUrl.isNotEmpty()) {
                session.saveApiUrl(newUrl)
                com.culturesync.app.data.remote.RetrofitClient.dynamicBaseUrl = newUrl
                Toast.makeText(this, getString(R.string.msg_api_updated), Toast.LENGTH_LONG).show()
            }
        }

        // ── Clear Session Data ─────────────────────────────────────
        binding.btnClearData.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_clear_title))
                .setMessage(getString(R.string.dialog_clear_msg))
                .setPositiveButton(getString(R.string.btn_clear)) { _, _ ->
                    this@SettingsActivity.lifecycleScope.launch(Dispatchers.IO) {
                        val db = com.culturesync.app.data.local.AppDatabase.getInstance(this@SettingsActivity)
                        db.query("DELETE FROM session_frames", null)
                        db.query("DELETE FROM sessions", null)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SettingsActivity, getString(R.string.msg_data_cleared), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show()
        }

        // ── App Version ────────────────────────────────────────────
        binding.tvAppVersion.text = getString(R.string.label_version_format, BuildConfig.VERSION_NAME)

        // ── Admin Section ──────────────────────────────────────────
        binding.cardAdmin.visibility = if (session.isAdmin()) android.view.View.VISIBLE
                                       else android.view.View.GONE

        binding.btnAdmin.setOnClickListener {
            startActivity(android.content.Intent(this, com.culturesync.app.ui.admin.AdminUploadActivity::class.java))
        }
    }
}
