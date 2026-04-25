package com.culturesync.app.models

data class Technique(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String,          // "Beginner", "Intermediate", "Advanced"
    val durationMinutes: Int,
    val thumbnailResId: Int = 0,
    val videoFileName: String? = null,
    val videoUrl: String? = null,    // dynamically fetched streaming url
    val hasTemplate: Boolean = true,
    val steps: List<String> = emptyList()
)
