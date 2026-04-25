package com.culturesync.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.UserEntity
import com.culturesync.app.data.repository.UserRepository
import com.culturesync.app.databinding.ActivityLoginBinding
import com.culturesync.app.ui.home.HomeActivity
import com.culturesync.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Login screen (PRD Screen #5).
 * Validates against Room UserEntity hash. BCrypt verification (FR-AUTH-01).
 * Tries cloud API first, falls back to local Room DB offline (NFR-OFF-01).
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userRepo: UserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userRepo = UserRepository(AppDatabase.getInstance(this))
        session  = SessionManager(this)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim().lowercase()
        val pass  = binding.etPassword.text.toString()
        if (email.isEmpty() || pass.isEmpty()) { toast(getString(com.culturesync.app.R.string.err_login_required)); return }

        binding.btnLogin.isEnabled = false

        lifecycleScope.launch {
            var token = ""
            var userId = 0
            var userType = "Student"
            var userName = ""
            var success = false

            // Try Cloud API First
            try {
                val apiReq = com.culturesync.app.data.remote.models.LoginRequest(email, pass)
                val response = com.culturesync.app.data.remote.RetrofitClient.api.login(apiReq)
                if (response.isSuccessful && response.body()?.success == true) {
                    val authData = response.body()!!.data!!
                    token = authData.token
                    userType = authData.user.role
                    userName = authData.user.name

                    withContext(Dispatchers.IO) {
                        var existing = userRepo.getUserByEmail(email)
                        if (existing == null) {
                            val newId = userRepo.createUser(
                                UserEntity(
                                    name = userName,
                                    email = email,
                                    passwordHash = userRepo.hashPassword(pass),
                                    userType = userType,
                                    syncStatus = "SYNCED",
                                    remoteId = authData.user.id
                                )
                            )
                            existing = userRepo.getUserById(newId.toInt())
                        }
                        userId = existing!!.id
                    }
                    success = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fallback to Local Offline DB with BCrypt verification
            if (!success) {
                withContext(Dispatchers.IO) {
                    val user = userRepo.getUserByEmail(email)
                    if (user == null || !userRepo.verifyPassword(pass, user.passwordHash)) {
                        withContext(Dispatchers.Main) {
                            toast(getString(com.culturesync.app.R.string.err_invalid_credentials))
                            binding.btnLogin.isEnabled = true
                        }
                        return@withContext
                    }
                    userId = user.id
                    userType = user.userType
                    userName = user.name
                    success = true
                }
            }

            if (success) {
                session.saveUser(userId, userName, email, userType, token)
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finishAffinity()
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
