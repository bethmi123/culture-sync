package com.culturesync.app.ui.practice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.repository.SessionRepository
import com.culturesync.app.databinding.ActivitySessionResultBinding
import com.culturesync.app.ml.ARConstants
import com.culturesync.app.ui.home.HomeActivity
import com.culturesync.app.utils.toAccuracyLabelRes
import com.culturesync.app.utils.toTimeString
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Session result summary screen (PRD Screen #11).
 * Displays: accuracy percentage, trend line chart (MPAndroidChart), duration, frame count.
 * Premium dark theme with gold accent.
 */
class SessionResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySessionResultBinding
    private lateinit var sessionRepo: SessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionRepo = SessionRepository(AppDatabase.getInstance(this))

        val accuracy  = intent.getFloatExtra("accuracy", 0f)       // 0–100 scale
        val duration  = intent.getIntExtra("duration", 0)           // seconds
        val name      = intent.getStringExtra("technique_name") ?: ""
        val frames    = intent.getIntExtra("frame_count", 0)
        val sessionId = intent.getIntExtra("session_id", -1)

        val accuracyNorm = accuracy / 100f

        binding.tvTechniqueName.text = name
        binding.tvAccuracyScore.text = getString(com.culturesync.app.R.string.percentage_format, accuracy.toInt())
        // accuracy is 0–100 → use context-free toAccuracyLabel(); accuracyNorm (0–1) → localised label
        binding.tvAccuracyLabel.text = getString(accuracyNorm.toAccuracyLabelRes())
        binding.tvDuration.text      = duration.toTimeString(this)
        binding.tvFrameCount.text    = String.format("%,d", frames)
        binding.progressResult.progress = accuracy.toInt()

        setupFeedback(accuracyNorm)
        setupChart(sessionId)

        binding.btnPracticeAgain.setOnClickListener { finish() }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }
    }

    private fun setupFeedback(accuracyNorm: Float) {
        binding.tvFeedback.text = when {
            accuracyNorm >= ARConstants.GREEN_ACCURACY_THRESHOLD ->
                getString(com.culturesync.app.R.string.feedback_excellent)
            accuracyNorm >= ARConstants.YELLOW_ACCURACY_THRESHOLD ->
                getString(com.culturesync.app.R.string.feedback_good)
            accuracyNorm >= 0.50f ->
                getString(com.culturesync.app.R.string.feedback_average)
            else ->
                getString(com.culturesync.app.R.string.feedback_low)
        }
    }

    private fun setupChart(sessionId: Int) {
        if (sessionId == -1) {
            binding.chartAccuracy.visibility = android.view.View.GONE
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val frames = sessionRepo.getFramesForSession(sessionId)
            if (frames.isEmpty()) return@launch

            val entries = frames.mapIndexed { index, frame ->
                Entry(index.toFloat(), frame.overallAccuracy * 100f)
            }

            withContext(Dispatchers.Main) {
                val dataSet = LineDataSet(entries, getString(com.culturesync.app.R.string.title_accuracy_trend)).apply {
                    color = Color.parseColor("#FACC15") // brand_accent
                    setCircleColor(Color.parseColor("#FACC15"))
                    lineWidth = 2f
                    circleRadius = 0f // No circles for smooth line
                    setDrawCircleHole(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    fillColor = Color.parseColor("#FACC15")
                    fillAlpha = 50
                }

                binding.chartAccuracy.apply {
                    data = LineData(dataSet)
                    description.isEnabled = false
                    legend.isEnabled = false
                    xAxis.isEnabled = false
                    axisLeft.apply {
                        textColor = Color.WHITE
                        axisMaximum = 100f
                        axisMinimum = 0f
                        setDrawGridLines(false)
                    }
                    axisRight.isEnabled = false
                    setTouchEnabled(false)
                    invalidate()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }
}
