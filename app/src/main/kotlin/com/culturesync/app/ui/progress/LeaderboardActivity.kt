package com.culturesync.app.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.culturesync.app.R
import com.culturesync.app.data.remote.RetrofitClient
import com.culturesync.app.data.remote.models.LeaderboardEntry
import com.culturesync.app.databinding.ActivityLeaderboardBinding
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)

        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val res = RetrofitClient.api.getLeaderboard()
                if (res.isSuccessful && res.body()?.success == true) {
                    val list = res.body()?.data ?: emptyList()
                    binding.rvLeaderboard.adapter = LeaderboardAdapter(list)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}

class LeaderboardAdapter(private val items: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val rank: TextView     = view.findViewById(R.id.tvRank)
        val name: TextView     = view.findViewById(R.id.tvUserName)
        val sessions: TextView = view.findViewById(R.id.tvSessionCount)
        val accuracy: TextView = view.findViewById(R.id.tvAccuracy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = items[position]
        holder.rank.text = entry.rank.toString()
        holder.name.text = entry.userName
        val context = holder.itemView.context
        holder.sessions.text = context.getString(R.string.label_sessions_completed, entry.totalSessions)
        holder.accuracy.text = context.getString(R.string.label_avg_accuracy_short, entry.averageAccuracy.toInt())
        
        val rankColor = when (entry.rank) {
            1 -> Color.parseColor("#FFD700") // Gold
            2 -> Color.parseColor("#C0C0C0") // Silver
            3 -> Color.parseColor("#CD7F32") // Bronze
            else -> Color.parseColor("#1E88E5")
        }
        holder.rank.background.setTint(rankColor)
        
        holder.accuracy.setTextColor(
            when {
                entry.averageAccuracy >= 85 -> Color.parseColor("#00C853")
                entry.averageAccuracy >= 70 -> Color.parseColor("#FF8F00")
                else -> Color.parseColor("#FF5722")
            }
        )
    }

    override fun getItemCount() = items.size
}
