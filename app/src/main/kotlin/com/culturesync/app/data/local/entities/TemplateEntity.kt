package com.culturesync.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "templates",
    foreignKeys = [
        ForeignKey(
            entity = TechniqueEntity::class,
            parentColumns = ["id"],
            childColumns = ["techniqueId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("techniqueId")]
)
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val techniqueId: Int,
    val masterName: String,
    val recordingDate: String,                 // ISO 8601 date
    val frameCount: Int,
    val landmarkDataJson: String,              // Serialized List<List<NormalizedLandmark>>
    val isAveraged: Boolean                    // True if barycentric average of multiple demos
)
