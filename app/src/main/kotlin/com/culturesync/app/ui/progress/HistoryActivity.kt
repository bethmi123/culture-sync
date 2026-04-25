package com.culturesync.app.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.culturesync.app.R
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.SessionEntity
import com.culturesync.app.data.local.entities.TechniqueEntity
import com.culturesync.app.data.repository.SessionRepository
import com.culturesync.app.data.repository.TechniqueRepository
import com.culturesync.app.databinding.ActivityHistoryBinding
import com.culturesync.app.ml.ARConstants
import com.culturesync.app.utils.SessionManager
import com.culturesync.app.utils.toReadableDate
import com.culturesync.app.utils.toTimeString
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var sessionRepo: SessionRepository
    private lateinit var techniqueRepo: TechniqueRepository
    private lateinit var sessionMgr: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionMgr = SessionManager(this)
        val db = AppDatabase.getInstance(this)
        sessionRepo = SessionRepository(db)
        techniqueRepo = TechniqueRepository(db)

        binding.btnBack.setOnClickListener { finish() }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)

        val userId = sessionMgr.getUserId()
        val isSinhala = sessionMgr.getLanguage() == "si"

        lifecycleScope.launch {
            combine(
                sessionRepo.getSessionsByUser(userId),
                techniqueRepo.getAllTechniques()
            ) { sessions, techniques ->
                sessions to techniques.associateBy { it.id }
            }.collect { (sessions, techniqueMap) ->
                val completed = sessions.filter { it.status == "COMPLETED" }
                if (completed.isEmpty()) {
                    binding.tvEmpty.visibility   = View.VISIBLE
                    binding.rvHistory.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility   = View.GONE
                    binding.rvHistory.visibility = View.VISIBLE
                    val adapter = HistoryAdapter(completed, techniqueMap, isSinhala)
                    binding.rvHistory.adapter = adapter

                    val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                        override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
                        override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                            val pos = vh.adapterPosition
                            val deletedSession = completed[pos]
                            lifecycleScope.launch { sessionRepo.deleteSession(deletedSession) }
                        }
                    }
                    ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvHistory)
                }
            }
        }
    }
}

class HistoryAdapter(
    private val items: List<SessionEntity>,
    private val techniqueMap: Map<Int, TechniqueEntity>,
    private val isSinhala: Boolean
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name:     TextView = view.findViewById(R.id.tvHistoryName)
        val date:     TextView = view.findViewById(R.id.tvHistoryDate)
        val accuracy: TextView = view.findViewById(R.id.tvHistoryAccuracy)
        val duration: TextView = view.findViewById(R.id.tvHistoryDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        val technique = techniqueMap[s.techniqueId]
        
        holder.name.text = if (technique != null) {
            val resId = holder.itemView.resources.getIdentifier("tech_${technique.techniqueKey}_name", "string", holder.itemView.context.packageName)
            if (resId != 0) holder.itemView.context.getString(resId) else technique.nameEn
        } else androidx.core.content.ContextCompat.getString(holder.itemView.context, R.string.unknown_technique)

        holder.date.text     = s.startTime.toReadableDate()
        holder.accuracy.text = holder.itemView.context.getString(R.string.percentage_format, ((s.finalAccuracy ?: 0f) * 100).toInt())
        holder.duration.text = (s.durationSeconds ?: 0).toTimeString(holder.itemView.context)
        holder.accuracy.setTextColor(
            when {
                (s.finalAccuracy ?: 0f) >= ARConstants.GREEN_ACCURACY_THRESHOLD  -> ARConstants.COLOR_GREEN
                (s.finalAccuracy ?: 0f) >= ARConstants.YELLOW_ACCURACY_THRESHOLD -> ARConstants.COLOR_YELLOW
                else -> ARConstants.COLOR_RED
            }
        )
    }

    override fun getItemCount() = items.size
}
