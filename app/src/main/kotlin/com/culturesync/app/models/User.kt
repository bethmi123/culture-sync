package com.culturesync.app.models

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String = "learner",   // "learner" or "admin"
    val createdAt: Long = System.currentTimeMillis()
)
