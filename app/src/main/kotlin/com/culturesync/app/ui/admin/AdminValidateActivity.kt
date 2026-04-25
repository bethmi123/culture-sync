package com.culturesync.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.culturesync.app.R
import com.culturesync.app.databinding.ActivityAdminValidateBinding

/**
 * Admin validation screen.
 * Shows a preview of the recorded hand-gesture template and lets the
 * artisan/admin approve or reject it before it is published to the app.
 */
class AdminValidateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminValidateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminValidateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val techniqueId   = intent.getStringExtra("technique_id") ?: ""
        val techniqueName = intent.getStringExtra("technique_name") ?: ""
        val difficulty    = intent.getStringExtra("difficulty") ?: "Beginner"

        binding.tvTechniqueName.text = techniqueName
        binding.tvDifficulty.text    = getString(R.string.label_difficulty_format, difficulty)
        binding.btnBack.setOnClickListener { finish() }

        binding.btnApprove.setOnClickListener {
            try {
                val file = java.io.File(filesDir, "$techniqueId.json")
                if (file.exists()) {
                    val json = file.readText()
                    val type = object : com.google.gson.reflect.TypeToken<com.culturesync.app.models.ExpertTemplate>() {}.type
                    val template: com.culturesync.app.models.ExpertTemplate = com.google.gson.Gson().fromJson(json, type)
                    val updated = template.copy(validatedByArtisan = true)
                    file.writeText(com.google.gson.Gson().toJson(updated))
                }
            } catch (e: Exception) { e.printStackTrace() }
            
            Toast.makeText(this, getString(R.string.msg_template_approved), Toast.LENGTH_LONG).show()
            finish()
        }
        
        binding.btnReject.setOnClickListener {
            try {
                val file = java.io.File(filesDir, "$techniqueId.json")
                if (file.exists()) file.delete()
            } catch (e: Exception) { e.printStackTrace() }
            
            Toast.makeText(this, getString(R.string.msg_template_rejected), Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
