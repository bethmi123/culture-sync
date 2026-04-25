package com.culturesync.app.data.local.daos

import androidx.room.*
import com.culturesync.app.data.local.entities.TemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Query("SELECT * FROM templates WHERE techniqueId = :techniqueId")
    suspend fun getTemplatesForTechnique(techniqueId: Int): List<TemplateEntity>

    @Query("SELECT * FROM templates WHERE techniqueId = :techniqueId AND isAveraged = 1 LIMIT 1")
    suspend fun getAveragedTemplate(techniqueId: Int): TemplateEntity?

    @Query("SELECT * FROM templates WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Int): TemplateEntity?

    @Query("SELECT COUNT(*) FROM templates WHERE techniqueId = :techniqueId")
    suspend fun getTemplateCountForTechnique(techniqueId: Int): Int

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)
}
