package com.culturesync.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String? = null,
    val name: String,
    val email: String,
    val passwordHash: String,
    val userType: String = "Student",        // Student | Tourist | Artisan | ShopOwner
    val language: String = "en",             // en | si
    val createdAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"       // PENDING | SYNCED | ERROR
)
