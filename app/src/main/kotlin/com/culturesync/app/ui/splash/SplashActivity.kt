package com.culturesync.app.ui.splash

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.databinding.ActivitySplashBinding
import com.culturesync.app.ui.auth.LoginActivity
import com.culturesync.app.ui.home.HomeActivity
import com.culturesync.app.ui.onboarding.OnboardingActivity
import com.culturesync.app.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen (PRD Screen #1).
 * App logo, animated fade-in, version number. 2-second branded delay.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvVersion.text = getString(com.culturesync.app.R.string.label_version_format, com.culturesync.app.BuildConfig.VERSION_NAME)

        // Animate fade-in of logo + title
        binding.ivLogo.alpha = 0f
        binding.tvAppName.alpha = 0f
        binding.tvTagline.alpha = 0f
        binding.tvVersion.alpha = 0f

        ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 0f, 1f).apply { duration = 800; start() }
        ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f).apply { duration = 800; startDelay = 300; start() }
        ObjectAnimator.ofFloat(binding.tvTagline, View.ALPHA, 0f, 1f).apply { duration = 600; startDelay = 600; start() }
        ObjectAnimator.ofFloat(binding.tvVersion, View.ALPHA, 0f, 1f).apply { duration = 600; startDelay = 800; start() }

        val session = SessionManager(this)
        lifecycleScope.launch {
            // Seed techniques if empty (Production requirement FR-TMP-03)
            val db = com.culturesync.app.data.local.AppDatabase.getInstance(this@SplashActivity)
            val techniqueRepo = com.culturesync.app.data.repository.TechniqueRepository(db)
            if (techniqueRepo.getCount() == 0) {
                techniqueRepo.seedTechniques(listOf(
                    com.culturesync.app.data.local.entities.TechniqueEntity(
                        techniqueKey = "beeralu_basic",
                        nameEn = getString(com.culturesync.app.R.string.tech_beeralu_basic_name),
                        nameSi = getString(com.culturesync.app.R.string.tech_beeralu_basic_name),
                        techniqueType = "lace", difficulty = 1, templateAssetPath = "templates/beeralu_basic.json",
                        videoAssetPath = "demo_beeralu_basic",
                        descriptionEn = getString(com.culturesync.app.R.string.tech_beeralu_basic_desc),
                        descriptionSi = getString(com.culturesync.app.R.string.tech_beeralu_basic_desc)
                    ),
                    com.culturesync.app.data.local.entities.TechniqueEntity(
                        techniqueKey = "beeralu_cross",
                        nameEn = getString(com.culturesync.app.R.string.tech_beeralu_cross_name),
                        nameSi = getString(com.culturesync.app.R.string.tech_beeralu_cross_name),
                        techniqueType = "lace", difficulty = 1, templateAssetPath = "templates/beeralu_cross.json",
                        videoAssetPath = "demo_beeralu_cross",
                        descriptionEn = getString(com.culturesync.app.R.string.tech_beeralu_cross_desc),
                        descriptionSi = getString(com.culturesync.app.R.string.tech_beeralu_cross_desc)
                    ),
                    com.culturesync.app.data.local.entities.TechniqueEntity(
                        techniqueKey = "beeralu_twist",
                        nameEn = getString(com.culturesync.app.R.string.tech_beeralu_twist_name),
                        nameSi = getString(com.culturesync.app.R.string.tech_beeralu_twist_name),
                        techniqueType = "lace", difficulty = 2, templateAssetPath = "templates/beeralu_twist.json",
                        videoAssetPath = "demo_beeralu_twist",
                        descriptionEn = getString(com.culturesync.app.R.string.tech_beeralu_twist_desc),
                        descriptionSi = getString(com.culturesync.app.R.string.tech_beeralu_twist_desc)
                    ),
                    com.culturesync.app.data.local.entities.TechniqueEntity(
                        techniqueKey = "beeralu_diamond",
                        nameEn = getString(com.culturesync.app.R.string.tech_beeralu_diamond_name),
                        nameSi = getString(com.culturesync.app.R.string.tech_beeralu_diamond_name),
                        techniqueType = "lace", difficulty = 3, templateAssetPath = "templates/beeralu_diamond.json",
                        videoAssetPath = "demo_beeralu_diamond",
                        descriptionEn = getString(com.culturesync.app.R.string.tech_beeralu_diamond_desc),
                        descriptionSi = getString(com.culturesync.app.R.string.tech_beeralu_diamond_desc)
                    ),
                    com.culturesync.app.data.local.entities.TechniqueEntity(
                        techniqueKey = "beeralu_flower",
                        nameEn = getString(com.culturesync.app.R.string.tech_beeralu_flower_name),
                        nameSi = getString(com.culturesync.app.R.string.tech_beeralu_flower_name),
                        techniqueType = "lace", difficulty = 4, templateAssetPath = "templates/beeralu_flower.json",
                        videoAssetPath = "demo_beeralu_flower",
                        descriptionEn = getString(com.culturesync.app.R.string.tech_beeralu_flower_desc),
                        descriptionSi = getString(com.culturesync.app.R.string.tech_beeralu_flower_desc)
                    ),
                    com.culturesync.app.data.local.entities.TechniqueEntity(
                        techniqueKey = "beeralu_wave",
                        nameEn = getString(com.culturesync.app.R.string.tech_beeralu_wave_name),
                        nameSi = getString(com.culturesync.app.R.string.tech_beeralu_wave_name),
                        techniqueType = "lace", difficulty = 4, templateAssetPath = "templates/beeralu_wave.json",
                        videoAssetPath = "demo_beeralu_wave",
                        descriptionEn = getString(com.culturesync.app.R.string.tech_beeralu_wave_desc),
                        descriptionSi = getString(com.culturesync.app.R.string.tech_beeralu_wave_desc)
                    )

                ))
            }

            delay(2000L)
            val dest = when {
                session.isLoggedIn()    -> HomeActivity::class.java
                session.isFirstLaunch() -> OnboardingActivity::class.java
                else                    -> LoginActivity::class.java
            }
            startActivity(Intent(this@SplashActivity, dest))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
