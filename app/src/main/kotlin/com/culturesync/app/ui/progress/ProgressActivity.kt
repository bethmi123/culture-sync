package com.culturesync.app.ui.progress

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.R
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.repository.SessionRepository
import com.culturesync.app.databinding.ActivityProgressBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.culturesync.app.utils.SessionManager
import com.culturesync.app.utils.toTimeString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Progress dashboard (PRD Screen #12 / FR-PRG-01).
 * Stats cards + MPAndroidChart line chart (last 10 sessions).
 * All data from Room — fully offline (FR-PRG-03).
 */
class ProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgressBinding
    private lateinit var repo: SessionRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        repo    = SessionRepository(AppDatabase.getInstance(this))

        binding.btnBack.setOnClickListener { finish() }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        loadProgress()
    }

    private fun loadProgress() {
        val userId = session.getUserId()
        lifecycleScope.launch {
            val sessions  = repo.getSessionsByUser(userId).first()
            val avg       = repo.getAverageAccuracy(userId)
            val best      = repo.getBestAccuracy(userId)
            val total     = repo.getTotalSessions(userId)
            val totalTime = repo.getTotalPracticeSeconds(userId)

            binding.tvTotalSessions.text = total.toString()
            binding.tvAvgAccuracy.text   = getString(R.string.percentage_format, (avg * 100).toInt())
            binding.tvBestAccuracy.text  = getString(R.string.percentage_format, (best * 100).toInt())
            binding.tvTotalTime.text     = getString(R.string.minutes_format, totalTime / 60, getString(R.string.label_minutes))

            // Build accuracy-over-time line chart from real session data (FR-PRG-01)
            if (sessions.isNotEmpty()) {
                val entries = sessions
                    .filter { it.status == "COMPLETED" }
                    .sortedBy { it.startTime }
                    .takeLast(10)  // last 10 sessions only
                    .mapIndexed { i, s -> Entry(i.toFloat(), (s.finalAccuracy ?: 0f) * 100f) }

                val dataSet = LineDataSet(entries, getString(R.string.title_accuracy_over_time)).apply {
                    color = Color.parseColor("#1E88E5")
                    valueTextColor = Color.BLACK
                    lineWidth = 2.5f
                    circleRadius = 4f
                    setCircleColor(Color.parseColor("#1E88E5"))
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                binding.lineChart.apply {
                    data = LineData(dataSet)
                    description.isEnabled = false
                    legend.isEnabled = true
                    axisRight.isEnabled = false
                    xAxis.granularity = 1f
                    axisLeft.axisMinimum = 0f
                    axisLeft.axisMaximum = 100f
                    animateX(600)
                    invalidate()
                }
            } else {
                binding.tvNoData.visibility = android.view.View.VISIBLE
                binding.lineChart.visibility = android.view.View.GONE
            }
        }
    }
}
