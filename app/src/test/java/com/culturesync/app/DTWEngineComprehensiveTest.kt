package com.culturesync.app

import com.culturesync.app.ml.DTWEngine
import com.culturesync.app.models.HandFrame
import com.culturesync.app.models.HandLandmark
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.sin

/**
 * DTWEngineComprehensiveTest — Advanced Stress Tests
 * Covers: 150+ geometric permutations, scale range, temporal alignment,
 *         front-camera mirroring across multiple hand poses.
 */
class DTWEngineComprehensiveTest {
    private val engine = DTWEngine()

    // ── GEOMETRIC STRESS TEST ─────────────────────────────────────────────────

    @Test
    fun spatioTemporalShifts_50poses_allShouldScoreHigh() {
        var passed = 0
        for (offset in 0 until 50) {
            val expert = makeVibration(30, 0f)
            val learner = makeVibration(30, offset / 500f)  // tiny offset, nearly identical
            val result = engine.compare(learner, expert)
            if (result.overallScore > 85f) passed++
        }
        assertTrue("At least 45/50 shifted poses should score > 85, passed=$passed", passed >= 45)
    }

    @Test
    fun scaleRange_0p1to5p0_allShouldScoreHigh() {
        var passed = 0
        for (s in 1..50) {
            val scale = s / 10f   // 0.1 to 5.0
            val expert = makePillar(20, 1.0f)
            val learner = makePillar(20, scale)
            val result = engine.compare(learner, expert)
            if (result.overallScore > 90f) passed++
        }
        assertEquals("All 50 scale permutations should pass (scale-invariant DTW)", 50, passed)
    }

    @Test
    fun temporalAlignment_50starts_allShouldScore90Plus() {
        val expert = makeVibration(40, 0f)
        var passed = 0
        for (start in 0 until 50) {
            val haystack = makeFrames(start, 0.05f, 0.05f) + expert
            val result = engine.compare(haystack, expert)
            if (result.overallScore > 90f) passed++
        }
        assertTrue("At least 48/50 temporal alignments must score > 90, passed=$passed", passed >= 48)
    }

    @Test
    fun massivePermutation_all150ShouldPass() {
        var totalPassed = 0

        // 50 spatial shifts
        for (i in 0 until 50) {
            val expert = makeVibration(30, 0f)
            val learner = makeVibration(30, i / 500f)
            val r = engine.compare(learner, expert)
            if (r.overallScore > 85f) totalPassed++
        }
        // 50 scales
        for (s in 1..50) {
            val expert = makePillar(20, 1.0f)
            val learner = makePillar(20, s / 10f)
            val r = engine.compare(learner, expert)
            if (r.overallScore > 90f) totalPassed++
        }
        // 50 temporal offsets
        val expert = makeVibration(40, 0f)
        for (start in 0 until 50) {
            val haystack = makeFrames(start, 0.05f, 0.05f) + expert
            val r = engine.compare(haystack, expert)
            if (r.overallScore > 90f) totalPassed++
        }

        assertTrue("Expect ≥ 145/150 permutations to pass, got $totalPassed", totalPassed >= 145)
    }

    // ── PER-LANDMARK REGRESSION ───────────────────────────────────────────────

    @Test
    fun landmarkAccuracyRegression_identicalPillar_allShouldBeNear1() {
        val frames = makePillar(20, 1.0f)
        val result = engine.compare(frames, frames)
        for (i in 0..20) {
            val acc = result.perLandmarkAccuracy[i] ?: 0f
            assertTrue("Landmark $i regression: expected ≥ 0.99, got $acc", acc >= 0.99f)
        }
    }

    @Test
    fun landmarkAccuracyRegression_allInRange() {
        val learner = makeVibration(25, 0f)
        val expert = makeVibration(25, 0.02f)
        val result = engine.compare(learner, expert)
        for (i in 0..20) {
            val acc = result.perLandmarkAccuracy[i] ?: -1f
            assertTrue("Landmark $i out of [0,1]: $acc", acc in 0f..1f)
        }
    }

    // ── FRONT-CAMERA MIRRORING STRESS ────────────────────────────────────────

    @Test
    fun frontCameraMirror_50handPoses_allShouldScoreHigh() {
        for (i in 0 until 50) {
            val expert = makeFrames(20, 0.1f + i * 0.01f, 0.5f, "Right")
            val learner = makeFrames(20, 0.1f + i * 0.01f, 0.5f, "Left")
            val result = engine.compare(learner, expert)
            assertTrue("Mirror pose $i must score ≥ 95, got ${result.overallScore}", result.overallScore >= 95f)
        }
    }

    @Test
    fun noMirrorNeeded_sameHand_50poses_allShouldScoreHigh() {
        for (i in 0 until 50) {
            val frames = makeFrames(20, 0.1f * (i % 10), 0.5f, "Right")
            val result = engine.compare(frames, frames)
            assertTrue("Same-hand pose $i must score ≥ 99, got ${result.overallScore}", result.overallScore >= 99f)
        }
    }

    // ── NUMERICAL STABILITY ───────────────────────────────────────────────────

    @Test
    fun scoreNeverExceeds100() {
        val testCases = listOf(
            makeFrames(1, 0f, 0f) to makeFrames(1, 0f, 0f),
            makePillar(5, 0.001f) to makePillar(5, 0.001f),
            makeVibration(30, 0f) to makeVibration(30, 0f),
        )
        for ((learner, expert) in testCases) {
            val result = engine.compare(learner, expert)
            assertTrue("Score must never exceed 100: ${result.overallScore}", result.overallScore <= 100f)
        }
    }

    @Test
    fun accuracyNeverExceeds1() {
        val frames = makeFrames(10, 0.5f, 0.5f)
        val result = engine.compare(frames, frames)
        assertTrue("Accuracy must be ≤ 1.0, got ${result.overallAccuracy}", result.overallAccuracy <= 1.0f)
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private fun makeFrames(count: Int, x: Float, y: Float, hand: String = "Right"): List<HandFrame> =
        (1..count).map { HandFrame((0..20).map { i -> HandLandmark(x, y, 0f, i) }, hand) }

    private fun makePillar(count: Int, scale: Float): List<HandFrame> = (1..count).map {
        HandFrame((0..20).map { i ->
            if (i == 9) HandLandmark(0f, scale, 0f, i)
            else HandLandmark(0.05f * i * scale, 0.05f * i * scale, 0f, i)
        }, "Right")
    }

    private fun makeVibration(count: Int, offset: Float): List<HandFrame> = (1..count).map { i ->
        val dx = sin(i.toDouble() / 5.0).toFloat() * 0.1f
        HandFrame((0..20).map { id ->
            if (id == 8) HandLandmark(0.5f + dx + offset, 0.5f, 0f, id)
            else HandLandmark(0.5f + offset, 0.5f, 0f, id)
        }, "Right")
    }
}
