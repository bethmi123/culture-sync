package com.culturesync.app.data.local.daos

import androidx.room.*
import com.culturesync.app.data.local.entities.TechniqueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TechniqueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTechnique(technique: TechniqueEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(techniques: List<TechniqueEntity>)

    @Query("SELECT * FROM techniques ORDER BY difficulty ASC")
    fun getAllTechniques(): Flow<List<TechniqueEntity>>

    @Query("SELECT * FROM techniques WHERE id = :id LIMIT 1")
    suspend fun getTechniqueById(id: Int): TechniqueEntity?

    @Query("SELECT * FROM techniques WHERE techniqueKey = :key LIMIT 1")
    suspend fun getTechniqueByKey(key: String): TechniqueEntity?

    @Query("SELECT * FROM techniques WHERE techniqueType = :type ORDER BY difficulty ASC")
    fun getTechniquesByType(type: String): Flow<List<TechniqueEntity>>

    @Query("SELECT COUNT(*) FROM techniques")
    suspend fun getCount(): Int

    @Update
    suspend fun updateTechnique(technique: TechniqueEntity)

    @Delete
    suspend fun deleteTechnique(technique: TechniqueEntity)
}
