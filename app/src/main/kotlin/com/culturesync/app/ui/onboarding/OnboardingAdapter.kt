package com.culturesync.app.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.culturesync.app.R

class OnboardingAdapter(private val pages: List<OnboardingPage>) :
    RecyclerView.Adapter<OnboardingAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivOnboardIcon)
        val title: TextView = view.findViewById(R.id.tvOnboardTitle)
        val desc: TextView  = view.findViewById(R.id.tvOnboardDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_onboarding_page, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val page = pages[position]
        holder.icon.setImageResource(page.iconRes)
        holder.title.text = page.title
        holder.desc.text  = page.desc
    }

    override fun getItemCount() = pages.size
}
