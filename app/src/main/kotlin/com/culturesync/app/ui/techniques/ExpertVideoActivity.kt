package com.culturesync.app.ui.techniques

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.culturesync.app.databinding.ActivityExpertVideoBinding

/**
 * Expert video playback screen (PRD Screen #9).
 * Media3 ExoPlayer with custom controls. Loops expert demonstration video.
 * Play/pause/replay. No network — bundled video (FR-CNT-01).
 */
class ExpertVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpertVideoBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpertVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val techniqueName = intent.getStringExtra("technique_name") ?: getString(com.culturesync.app.R.string.title_expert_demo)
        val techniqueKey  = intent.getStringExtra("technique_key") ?: ""

        binding.tvTitle.text = techniqueName
        binding.btnBack.setOnClickListener { finish() }

        // Offline-only: load bundled video from res/raw/demo_<key>.mp4 (FR-CNT-01, NFR-OFF-03)
        val videoRes = resources.getIdentifier(
            "demo_${techniqueKey.replace("-", "_")}", "raw", packageName
        )
        if (videoRes != 0) {
            setupPlayer(Uri.parse("android.resource://$packageName/$videoRes"))
        } else {
            binding.tvNoVideo.visibility = android.view.View.VISIBLE
            binding.playerView.visibility = android.view.View.GONE
        }
    }

    private fun setupPlayer(uri: Uri) {
        player = ExoPlayer.Builder(this).build().also { exo ->
            binding.playerView.player = exo
            exo.setMediaItem(MediaItem.fromUri(uri))
            exo.repeatMode = ExoPlayer.REPEAT_MODE_ALL  // Loop for practice
            exo.prepare()
        }
    }

    override fun onPause()   { super.onPause(); player?.pause() }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}
