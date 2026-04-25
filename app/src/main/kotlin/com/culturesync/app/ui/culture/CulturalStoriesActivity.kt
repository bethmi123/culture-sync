package com.culturesync.app.ui.culture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.culturesync.app.R
import com.culturesync.app.databinding.ActivityCulturalStoriesBinding

/**
 * Cultural Stories screen (PRD Screen #16 / FR-CNT-02).
 * Displays story cards covering the history of Beeralu lace,
 * M.B. Priyani's post-tsunami revival, and Portuguese colonial origins.
 * All content available in both English and Sinhala via string resources.
 */
class CulturalStoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCulturalStoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCulturalStoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val stories = listOf(
            StoryItem(
                title = getString(R.string.story_title_1),
                body = getString(R.string.story_body_1),
                imageRes = R.drawable.ic_lace_history
            ),
            StoryItem(
                title = getString(R.string.story_title_2),
                body = getString(R.string.story_body_2),
                imageRes = R.drawable.ic_tsunami_revival
            ),
            StoryItem(
                title = getString(R.string.story_title_3),
                body = getString(R.string.story_body_3),
                imageRes = R.drawable.ic_portuguese_origins
            )
        )

        binding.rvStories.layoutManager = LinearLayoutManager(this)
        binding.rvStories.adapter = StoryAdapter(stories)
    }
}

data class StoryItem(val title: String, val body: String, val imageRes: Int)

class StoryAdapter(private val items: List<StoryItem>) :
    RecyclerView.Adapter<StoryAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivStoryImage)
        val title: TextView  = view.findViewById(R.id.tvStoryTitle)
        val body:  TextView  = view.findViewById(R.id.tvStoryBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.title.text = s.title
        holder.body.text  = s.body
        holder.image.setImageResource(s.imageRes)
    }

    override fun getItemCount() = items.size
}
