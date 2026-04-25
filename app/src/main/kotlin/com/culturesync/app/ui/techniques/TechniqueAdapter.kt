package com.culturesync.app.ui.techniques

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.culturesync.app.R
import com.culturesync.app.models.Technique

class TechniqueAdapter(
    private val items: List<Technique>,
    private val onClick: (Technique) -> Unit
) : RecyclerView.Adapter<TechniqueAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView       = view.findViewById(R.id.tvTechniqueName)
        val difficulty: TextView = view.findViewById(R.id.tvDifficulty)
        val desc: TextView       = view.findViewById(R.id.tvTechniqueDesc)
        val duration: TextView   = view.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_technique, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.name.text       = t.name
        holder.desc.text       = t.description
        holder.duration.text   = "${t.durationMinutes} min"
        holder.difficulty.text = t.difficulty
        holder.difficulty.setTextColor(
            when (t.difficulty) {
                "Beginner"     -> Color.parseColor("#00C853")
                "Intermediate" -> Color.parseColor("#FF8F00")
                else           -> Color.parseColor("#D50000")
            }
        )
        holder.itemView.setOnClickListener { onClick(t) }
    }

    override fun getItemCount() = items.size
}
