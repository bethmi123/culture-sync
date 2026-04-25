package com.culturesync.app.data.local.daos

import androidx.room.*
import com.culturesync.app.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)

    @Query("SELECT * FROM users WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    @Query("UPDATE users SET syncStatus = :status WHERE id = :userId")
    suspend fun updateSyncStatus(userId: Int, status: String)

    @Query("UPDATE users SET remoteId = :remoteId, syncStatus = 'SYNCED' WHERE id = :localId")
    suspend fun markSynced(localId: Int, remoteId: String)
}
