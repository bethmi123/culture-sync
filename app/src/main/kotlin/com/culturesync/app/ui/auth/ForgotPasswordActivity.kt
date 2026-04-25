package com.culturesync.app.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.databinding.ActivityForgotPasswordBinding
import kotlinx.coroutines.launch
import java.security.MessageDigest

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnReset.setOnClickListener { attemptReset() }
    }

    private fun attemptReset() {
        val email   = binding.etEmail.text.toString().trim().lowercase()
        val newPass = binding.etNewPassword.text.toString()
        val confirm = binding.etConfirmNew.text.toString()

        if (email.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) { toast(getString(com.culturesync.app.R.string.err_fields_required)); return }
        if (newPass.length < 6) { toast(getString(com.culturesync.app.R.string.err_password_short)); return }
        if (newPass != confirm) { toast(getString(com.culturesync.app.R.string.err_passwords_mismatch)); return }

        lifecycleScope.launch {
            val userRepo = com.culturesync.app.data.repository.UserRepository(db)
            val user = userRepo.getUserByEmail(email)
            if (user == null) {
                runOnUiThread { toast(getString(com.culturesync.app.R.string.err_user_not_found)) }
                return@launch
            }
            db.userDao().updateUser(user.copy(passwordHash = userRepo.hashPassword(newPass)))
            runOnUiThread { toast(getString(com.culturesync.app.R.string.msg_password_reset_success)); finish() }
        }
    }


    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
