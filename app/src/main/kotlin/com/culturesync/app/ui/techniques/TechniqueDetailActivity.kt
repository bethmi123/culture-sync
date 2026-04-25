package com.culturesync.app.ui.techniques

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.culturesync.app.databinding.ActivityTechniqueDetailBinding
import com.culturesync.app.ui.practice.PracticeActivity

class TechniqueDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTechniqueDetailBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTechniqueDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id         = intent.getIntExtra("technique_id", 1)
        val name       = intent.getStringExtra("technique_name") ?: ""
        val desc       = intent.getStringExtra("technique_desc") ?: ""
        val difficulty = intent.getStringExtra("difficulty") ?: ""
        val duration   = intent.getIntExtra("duration", 0)
        val videoUrl   = intent.getStringExtra("video_url")
        val techniqueKey = intent.getStringExtra("technique_key") ?: "beeralu_basic"

        binding.tvName.text       = name
        binding.tvDesc.text       = desc
        binding.tvDifficulty.text = getString(com.culturesync.app.R.string.label_difficulty_format, difficulty)
        binding.tvDuration.text   = getString(com.culturesync.app.R.string.label_duration_format, duration)
        binding.btnBack.setOnClickListener { finish() }

        binding.btnStartPractice.setOnClickListener {
            startActivity(
                Intent(this, PracticeActivity::class.java).apply {
                    putExtra("technique_id",   id)
                    putExtra("technique_name", name)
                    putExtra("technique_key",  techniqueKey)
                }
            )
        }

        // 1. Play Dynamic Cloud Streaming Video if provided by Node Backend
        if (!videoUrl.isNullOrEmpty()) {
            setupPlayer(Uri.parse(videoUrl))
        } else {
            // 2. Offline Fallback to raw assets
            val videoRes = resources.getIdentifier(
                "demo_${techniqueKey.replace("-", "_")}", "raw", packageName
            )
            if (videoRes != 0) {
                setupPlayer(Uri.parse("android.resource://$packageName/$videoRes"))
            } else {
                binding.playerView.visibility = android.view.View.GONE
                binding.tvNoVideo.visibility  = android.view.View.VISIBLE
            }
        }
    }

    private fun setupPlayer(uri: Uri) {
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.prepare()
        }
    }

    override fun onPause()   { player?.pause() }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}
