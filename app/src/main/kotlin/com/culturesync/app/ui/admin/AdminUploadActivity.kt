package com.culturesync.app.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.culturesync.app.R
import com.culturesync.app.databinding.ActivityAdminUploadBinding

/**
 * Admin screen for uploading new Beeralu technique templates.
 * Only accessible to users with role == "admin".
 *
 * Workflow:
 *  1. Admin selects a technique from the spinner
 *  2. Taps "Record Demo" — launches PracticeActivity in recording mode
 *  3. After recording, the raw HandFrame data is saved as a JSON template
 *  4. Template is validated on AdminValidateActivity before publishing
 */
import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts

class AdminUploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminUploadBinding

    private val videoPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show()
            val techniqueName = binding.etTechniqueName.text.toString().trim()
            val difficulty    = binding.spinnerDifficulty.selectedItem.toString()
            startActivity(
                Intent(this, AdminValidateActivity::class.java).apply {
                    putExtra("technique_name", techniqueName)
                    putExtra("difficulty",     difficulty)
                }
            )
            finish()
        } else {
            Toast.makeText(this, getString(R.string.msg_recording_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRecordDemo.setOnClickListener {
            val techniqueName = binding.etTechniqueName.text.toString().trim()
            if (techniqueName.isEmpty()) {
                Toast.makeText(this, getString(R.string.err_enter_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Actually launch real video taking to capture artisan demonstration
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                videoPicker.launch(intent)
            } else {
                Toast.makeText(this, getString(R.string.err_no_camera), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
