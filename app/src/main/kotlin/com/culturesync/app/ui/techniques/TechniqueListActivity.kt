package com.culturesync.app.ui.techniques

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.culturesync.app.R
import com.culturesync.app.databinding.ActivityTechniqueListBinding
import com.culturesync.app.models.Technique
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.repository.TechniqueRepository
import com.culturesync.app.utils.Constants
import com.culturesync.app.utils.SessionManager
import kotlinx.coroutines.launch

class TechniqueListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTechniqueListBinding
    private lateinit var repository: TechniqueRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTechniqueListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TechniqueRepository(AppDatabase.getInstance(this))
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        val launchDetail = { technique: Technique ->
            startActivity(
                Intent(this, TechniqueDetailActivity::class.java).apply {
                    putExtra("technique_id",   technique.id.toIntOrNull() ?: 1)
                    putExtra("technique_name", technique.name)
                    putExtra("technique_desc", technique.description)
                    putExtra("difficulty",     technique.difficulty)
                    putExtra("duration",       technique.durationMinutes)
                    putExtra("video_url",      technique.videoUrl)
                    putExtra("technique_key",  technique.videoFileName) // Model videoFileName used to pass techniqueKey
                }
            )
        }

        binding.rvTechniques.layoutManager = LinearLayoutManager(this)
        
        val isSinhala = session.getLanguage() == "si"
        
        lifecycleScope.launch {
            repository.getAllTechniques().collect { entities ->
                val displayList = entities.map { rt ->
                    val nameResId =
                        resources.getIdentifier("tech_${rt.techniqueKey}_name", "string", packageName)
                    val descResId =
                        resources.getIdentifier("tech_${rt.techniqueKey}_desc", "string", packageName)

                    val name = if (nameResId != 0) getString(nameResId) else rt.nameEn
                    val description = if (descResId != 0) getString(descResId) else rt.descriptionEn

                    val diffString = when (rt.difficulty) {
                        1, 2 -> getString(R.string.difficulty_beginner)
                        3 -> getString(R.string.difficulty_intermediate)
                        else -> getString(R.string.difficulty_advanced)
                    }
                    Technique(
                        id = rt.id.toString(),
                        name = name,
                        description = description,
                        difficulty = diffString,
                        durationMinutes = rt.difficulty * 10,
                        videoFileName = rt.techniqueKey,
                        videoUrl = rt.videoAssetPath,
                        hasTemplate = true
                    )
                }
                binding.rvTechniques.adapter = TechniqueAdapter(displayList, launchDetail)
            }
        }
    }
}
