package com.culturesync.app.data.repository

import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.UserEntity
import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Repository for user authentication and profile management.
 * Passwords hashed with BCrypt cost factor 12 (FR-AUTH-01).
 */
class UserRepository(private val db: AppDatabase) {

    /**
     * Hash a plaintext password using BCrypt with cost factor 12.
     */
    fun hashPassword(plaintext: String): String =
        BCrypt.withDefaults().hashToString(12, plaintext.toCharArray())

    /**
     * Verify a plaintext password against a BCrypt hash.
     */
    fun verifyPassword(plaintext: String, hash: String): Boolean =
        BCrypt.verifyer().verify(plaintext.toCharArray(), hash).verified

    suspend fun createUser(user: UserEntity): Long =
        db.userDao().insertUser(user)

    suspend fun getUserByEmail(email: String): UserEntity? =
        db.userDao().getUserByEmail(email)

    suspend fun getUserById(id: Int): UserEntity? =
        db.userDao().getUserById(id)

    suspend fun updateUser(user: UserEntity) =
        db.userDao().updateUser(user)

    suspend fun deleteUser(id: Int) =
        db.userDao().deleteUser(id)
}
