package com.culturesync.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.UserEntity
import com.culturesync.app.data.repository.UserRepository
import com.culturesync.app.databinding.ActivityRegisterBinding
import com.culturesync.app.ui.home.HomeActivity
import com.culturesync.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Registration screen (PRD Screen #4).
 * BCrypt hash on submit (FR-AUTH-01). Writes UserEntity to Room.
 * Optional online sync via Retrofit.
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRepo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userRepo = UserRepository(AppDatabase.getInstance(this))
        session  = SessionManager(this)

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val name    = binding.etName.text.toString().trim()
        val email   = binding.etEmail.text.toString().trim().lowercase()
        val pass    = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            toast(getString(com.culturesync.app.R.string.err_fields_required)); return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast(getString(com.culturesync.app.R.string.err_invalid_email)); return
        }
        if (pass.length < 6) {
            toast(getString(com.culturesync.app.R.string.err_password_short)); return
        }
        if (pass != confirm) {
            toast(getString(com.culturesync.app.R.string.err_passwords_mismatch)); return
        }

        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val existing = userRepo.getUserByEmail(email)
                if (existing != null) {
                    withContext(Dispatchers.Main) {
                        toast(getString(com.culturesync.app.R.string.err_email_exists))
                        binding.btnRegister.isEnabled = true
                    }
                    return@withContext
                }

                var token = ""
                var remoteId: String? = null
                var userType = intent.getStringExtra("user_type") ?: "Student"

                // Try cloud registration
                try {
                    val apiReq = com.culturesync.app.data.remote.models.RegisterRequest(name, email, pass)
                    val res = com.culturesync.app.data.remote.RetrofitClient.api.register(apiReq)
                    if (res.isSuccessful && res.body()?.success == true) {
                        val authData = res.body()!!.data!!
                        token = authData.token
                        userType = authData.user.role
                        remoteId = authData.user.id
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // BCrypt hash with cost factor 12 (FR-AUTH-01)
                val passwordHash = userRepo.hashPassword(pass)

                val user = UserEntity(
                    name = name,
                    email = email,
                    passwordHash = passwordHash,
                    userType = userType,
                    remoteId = remoteId,
                    syncStatus = if (remoteId != null) "SYNCED" else "PENDING"
                )
                val id = userRepo.createUser(user).toInt()

                withContext(Dispatchers.Main) {
                    session.saveUser(id, name, email, userType, token)
                    session.setFirstLaunchDone()
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
