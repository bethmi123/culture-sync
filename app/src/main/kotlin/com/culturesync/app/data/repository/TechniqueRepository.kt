package com.culturesync.app.data.repository

import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.entities.TechniqueEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for technique data. Techniques are seeded from assets at first launch
 * and optionally synced from the backend.
 */
class TechniqueRepository(private val db: AppDatabase) {

    fun getAllTechniques(): Flow<List<TechniqueEntity>> =
        db.techniqueDao().getAllTechniques()

    suspend fun getTechniqueById(id: Int): TechniqueEntity? =
        db.techniqueDao().getTechniqueById(id)

    suspend fun getTechniqueByKey(key: String): TechniqueEntity? =
        db.techniqueDao().getTechniqueByKey(key)

    fun getTechniquesByType(type: String): Flow<List<TechniqueEntity>> =
        db.techniqueDao().getTechniquesByType(type)

    suspend fun insertTechnique(technique: TechniqueEntity): Long =
        db.techniqueDao().insertTechnique(technique)

    suspend fun seedTechniques(techniques: List<TechniqueEntity>) =
        db.techniqueDao().insertAll(techniques)

    suspend fun getCount(): Int =
        db.techniqueDao().getCount()
}
