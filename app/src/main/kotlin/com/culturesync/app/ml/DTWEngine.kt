package com.culturesync.app.ml

import com.culturesync.app.models.HandFrame
import com.culturesync.app.models.HandLandmark
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Production DTW Engine — Subsequence Dynamic Time Warping (FR-DTW)
 * Supports non-linear time warping with Sakoe-Chiba band constraint.
 */
class DTWEngine {

    /**
     * Compare learner frames against expert template frames.
     * Implements Subsequence DTW to find the best matching expert segment (FR-DTW-01).
     */
    fun compare(learnerFrames: List<HandFrame>, expertFrames: List<HandFrame>): AccuracyResult {
        if (learnerFrames.isEmpty() || expertFrames.isEmpty()) return AccuracyResult.EMPTY

        // All frames must have exactly 21 landmarks; return EMPTY for tracking loss / malformed input
        if (learnerFrames.any { it.landmarks.size != 21 } || expertFrames.any { it.landmarks.size != 21 }) {
            return AccuracyResult.EMPTY
        }

        val normLearner = normalise(learnerFrames, false)
        val normExpert  = normalise(expertFrames, false)

        val n = normLearner.size
        val m = normExpert.size

        // Sakoe-Chiba band: 10% of the shorter sequence (FR-DTW-01)
        val band = max(1, (min(n, m) * ARConstants.DTW_SAKOE_CHIBA_BAND).toInt())

        // Subsequence DTW: free start — can begin matching anywhere in learner (FR-DTW-01)
        val dtw = Array(n + 1) { FloatArray(m + 1) { Float.MAX_VALUE } }
        for (i in 0..n) dtw[i][0] = 0f

        for (i in 1..n) {
            for (j in 1..m) {
                if (abs(i - j) > band) continue  // Sakoe-Chiba constraint
                val cost = poseDistance(normLearner[i - 1], normExpert[j - 1])
                dtw[i][j] = cost + min(dtw[i - 1][j - 1], min(dtw[i - 1][j], dtw[i][j - 1]))
            }
        }

        // Find best end position in learner (subsequence match can end anywhere)
        var bestI = 1
        var bestDist = Float.MAX_VALUE
        for (i in 1..n) {
            if (dtw[i][m] < bestDist) { bestDist = dtw[i][m]; bestI = i }
        }

        if (bestDist == Float.MAX_VALUE) return AccuracyResult.EMPTY

        val overallAccuracy = distanceToAccuracy(bestDist / max(1, min(n, m)).toFloat())

        // Backtrack from best end position to compute per-landmark accuracy
        val perLandmarkError = FloatArray(21)
        var i = bestI
        var j = m
        var pathLength = 0
        var lastExpertMatchIndex = m - 1

        while (i > 0 && j > 0) {
            val lPose = normLearner[i - 1].landmarks
            val ePose = normExpert[j - 1].landmarks

            if (i == bestI) lastExpertMatchIndex = j - 1

            for (k in 0..20) perLandmarkError[k] += euclidean(lPose[k], ePose[k])
            pathLength++

            // Backtrack
            val prev11 = dtw[i-1][j-1]
            val prev10 = dtw[i-1][j]
            val prev01 = dtw[i][j-1]
            val minPrev = min(prev11, min(prev10, prev01))
            
            when (minPrev) {
                prev11 -> { i--; j-- }
                prev01 -> { j-- }
                else   -> { i-- }
            }
        }

        val perJointAccuracy = (0..20).map { k ->
            // Scale joint error to POSE-level space for consistent use of distanceToAccuracy
            distanceToAccuracy((perLandmarkError[k] / max(1f, pathLength.toFloat())) * 21f)
        }

        return AccuracyResult(
            overallAccuracy = overallAccuracy,
            perJointAccuracy = perJointAccuracy,
            matchedExpertFrameIndex = lastExpertMatchIndex
        )
    }

    private fun normalise(frames: List<HandFrame>, forceMirrorX: Boolean): List<HandFrame> =
        frames.map { frame ->
            val wrist = frame.landmarks[0]
            val middleMcp = frame.landmarks[9]
            val dx = middleMcp.x - wrist.x
            val dy = middleMcp.y - wrist.y
            val scale = 1f / max(0.001f, sqrt(dx*dx + dy*dy))
            
            frame.copy(landmarks = frame.landmarks.map { lm ->
                val nx = (lm.x - wrist.x) * scale
                lm.copy(
                    x = if (forceMirrorX) -nx else nx,
                    y = (lm.y - wrist.y) * scale,
                    z = (lm.z - wrist.z) * scale
                )
            })
        }

    private fun poseDistance(f1: HandFrame, f2: HandFrame): Float {
        var sum = 0f
        for (k in 0..20) sum += euclidean(f1.landmarks[k], f2.landmarks[k])
        return sum
    }

    private fun euclidean(a: HandLandmark, b: HandLandmark): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val dist = sqrt(dx*dx + dy*dy)
        // Guard against NaN/Infinity from degenerate coordinate values; treat as max distance
        return if (dist.isNaN() || dist.isInfinite()) ARConstants.MAX_EXPECTED_DTW_DISTANCE else dist
    }

    private fun distanceToAccuracy(distance: Float): Float {
        if (distance.isNaN() || distance.isInfinite()) return 0f
        return (1f - distance / ARConstants.MAX_EXPECTED_DTW_DISTANCE).coerceIn(0f, 1f)
    }
}
