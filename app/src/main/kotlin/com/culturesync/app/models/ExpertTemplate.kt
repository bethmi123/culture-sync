package com.culturesync.app.models

/**
 * Expert template for a Beeralu lace technique.
 * Loaded from assets/templates/<techniqueId>.json.
 * frames = average of 5 expert demonstrations, validated by a Beeralu artisan.
 */
data class ExpertTemplate(
    val techniqueId: String,
    val techniqueName: String,
    val description: String,
    val difficulty: String,             // "Beginner", "Intermediate", "Advanced"
    val frames: List<HandFrame>,        // sequence of reference hand frames
    val totalDurationMs: Long,
    val frameIntervalMs: Long = 40,     // ~25 FPS capture rate
    val validatedByArtisan: Boolean = true,
    val artisanName: String = "M.B. Priyani",
    val recordedAt: String = "2024-04-20"
)
