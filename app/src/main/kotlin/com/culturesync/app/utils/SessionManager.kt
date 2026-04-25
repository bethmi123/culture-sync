package com.culturesync.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Session persistence using EncryptedSharedPreferences (PRD §9.4).
 * Stores userId (Int), auth JWT token, and app settings.
 * Auto-login on subsequent launches (FR-AUTH-02).
 */
class SessionManager(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "culturesync_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to plain prefs if keystore unavailable (e.g. rooted device wiped keystore)
        context.getSharedPreferences("culturesync_prefs_fallback", Context.MODE_PRIVATE)
    }

    fun isLoggedIn(): Boolean = prefs.getInt("user_id", -1) != -1
    fun isFirstLaunch(): Boolean = prefs.getBoolean("first_launch", true)
    fun setFirstLaunchDone() = prefs.edit().putBoolean("first_launch", false).apply()

    fun saveUser(userId: Int, name: String, email: String, userType: String, token: String = "") {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_type", userType)
            .putString("auth_token", token)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getUserName(): String = prefs.getString("user_name", "") ?: ""
    fun getUserEmail(): String = prefs.getString("user_email", "") ?: ""
    fun getUserType(): String = prefs.getString("user_type", "Student") ?: "Student"
    fun getAuthToken(): String = prefs.getString("auth_token", "") ?: ""

    fun getApiUrl(): String = prefs.getString("base_url", Constants.BASE_URL) ?: Constants.BASE_URL
    fun saveApiUrl(url: String) = prefs.edit().putString("base_url", url).apply()

    fun getLanguage(): String = prefs.getString("language", "en") ?: "en"
    fun setLanguage(lang: String) = prefs.edit().putString("language", lang).apply()

    fun isGpuDelegateEnabled(): Boolean = prefs.getBoolean("gpu_delegate", false)
    fun setGpuDelegate(enabled: Boolean) = prefs.edit().putBoolean("gpu_delegate", enabled).apply()

    fun isAdmin(): Boolean = getUserType() == "admin"

    fun logout() = prefs.edit().clear().apply()
}
