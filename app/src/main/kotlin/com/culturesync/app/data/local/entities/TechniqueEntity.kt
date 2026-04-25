package com.culturesync.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "techniques",
    indices = [Index(value = ["techniqueKey"], unique = true)]
)
data class TechniqueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val techniqueKey: String,                  // e.g. beeralu_basic_flower
    val nameEn: String,
    val nameSi: String,
    val techniqueType: String = "lace",        // lace | pottery | woodcarving | mask | lacquerware
    val difficulty: Int = 1,                   // 1–5
    val templateAssetPath: String,             // Path to bundled JSON template
    val videoAssetPath: String,                // Path to bundled MP4
    val descriptionEn: String,
    val descriptionSi: String
)
