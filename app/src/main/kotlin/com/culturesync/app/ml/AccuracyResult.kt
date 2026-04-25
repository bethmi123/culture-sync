package com.culturesync.app.ml

/**
 * Result of a DTW comparison between a learner sequence and expert template.
 * Pure Kotlin data class — no Android dependencies.
 */
data class AccuracyResult(
    val overallAccuracy: Float,                // 0.0–1.0
    val perJointAccuracy: List<Float>,         // 21 Float values, 0.0–1.0 each
    val matchedExpertFrameIndex: Int = -1      // Index of expert frame matching learner's last frame
) {
    val overallPercent: Float get() = overallAccuracy * 100f

    // 0–100 scale alias used by tests and result screens
    val overallScore: Float get() = overallPercent

    fun perJointMap(): Map<Int, Float> = perJointAccuracy.mapIndexed { i, v -> i to v }.toMap()

    // Map-access alias for tests (result.perLandmarkAccuracy[k])
    val perLandmarkAccuracy: Map<Int, Float> get() = perJointMap()

    companion object {
        val EMPTY = AccuracyResult(
            overallAccuracy = 0f,
            perJointAccuracy = List(21) { 0f }
        )
    }
}
