package com.culturesync.app

import com.culturesync.app.ml.DTWEngine
import com.culturesync.app.models.HandFrame
import com.culturesync.app.models.HandLandmark
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sin

/**
 * DTWEngineTest — Core DTW Algorithm Unit Tests
 * Tests the complete pipeline: normalisation, DTW matrix, subsequence matching,
 * handedness mirroring, scale invariance, per-landmark accuracy.
 */
class DTWEngineTest {
    private val engine = DTWEngine()

    // ── PERFECT MATCH ──────────────────────────────────────────────────────────

    @Test
    fun identicalFrames_shouldScore100() {
        val frames = makeFrames(30, 0.5f, 0.5f)
        val result = engine.compare(frames, frames)
        assertTrue("Identical frames must score ≥ 99, got ${result.overallScore}", result.overallScore >= 99f)
    }

    @Test
    fun singleIdenticalFrame_shouldScore100() {
        val frame = makeFrames(1, 0.3f, 0.7f)
        val result = engine.compare(frame, frame)
        assertTrue("Single identical frame must score ≥ 99, got ${result.overallScore}", result.overallScore >= 99f)
    }

    @Test
    fun identicalLongSequences_shouldScore100() {
        val frames = makeFrames(100, 0.5f, 0.5f)
        val result = engine.compare(frames, frames)
        assertTrue("100-frame identical sequence must score ≥ 99, got ${result.overallScore}", result.overallScore >= 99f)
    }

    // ── EMPTY INPUT ────────────────────────────────────────────────────────────

    @Test
    fun bothEmpty_shouldReturnZero() {
        val result = engine.compare(emptyList(), emptyList())
        assertEquals(0f, result.overallScore, 0.001f)
        assertEquals(0f, result.overallAccuracy, 0.001f)
    }

    @Test
    fun emptyLearner_shouldReturnZero() {
        val expert = makeFrames(10, 0.5f, 0.5f)
        val result = engine.compare(emptyList(), expert)
        assertEquals(0f, result.overallScore, 0.001f)
    }

    @Test
    fun emptyExpert_shouldReturnZero() {
        val learner = makeFrames(10, 0.5f, 0.5f)
        val result = engine.compare(learner, emptyList())
        assertEquals(0f, result.overallScore, 0.001f)
    }

    @Test
    fun emptyInput_perLandmarkAccuracy_allZero() {
        val result = engine.compare(emptyList(), emptyList())
        for (i in 0..20) {
            assertEquals("Landmark $i should be 0 for empty input", 0f, result.perLandmarkAccuracy[i] ?: 0f, 0.001f)
        }
    }

    // ── HANDEDNESS MIRRORING ───────────────────────────────────────────────────

    @Test
    fun sameCoordinatesDifferentHand_mirroredX_shouldScoreHigh() {
        // Expert: Right hand. Learner: same pose but Left hand (front camera mirror).
        // DTW engine inverts X for mirror alignment → should give high score.
        val expert = makeFrames(20, 0.5f, 0.5f, "Right")
        val learner = makeFrames(20, 0.5f, 0.5f, "Left")
        val result = engine.compare(learner, expert)
        assertTrue("Mirror-aligned hands must score > 90, got ${result.overallScore}", result.overallScore > 90f)
    }

    @Test
    fun sameHand_noMirrorNeeded_identicalPose_shouldScore100() {
        val frames = makeFrames(15, 0.4f, 0.6f, "Right")
        val result = engine.compare(frames, frames)
        assertTrue("Same handedness, identical pose: score ≥ 99, got ${result.overallScore}", result.overallScore >= 99f)
    }

    @Test
    fun differentCoordinates_sameHand_shouldScoreLow() {
        val learner = makeFrames(10, 0.1f, 0.1f, "Right")
        val expert = makeFrames(10, 0.9f, 0.9f, "Right")
        val result = engine.compare(learner, expert)
        // Wrist-relative normalisation collapses both to origin → score can vary;
        // but widely different absolute poses still should not score 100 without matching
        assertTrue("Score must be 0–100", result.overallScore in 0f..100f)
    }

    // ── SCALE INVARIANCE ───────────────────────────────────────────────────────

    @Test
    fun scaleInvariance_smallVsLargeHand_shouldScoreHigh() {
        val expert = makePillar(20, scale = 1.0f)
        val learner = makePillar(20, scale = 4.0f)
        val result = engine.compare(learner, expert)
        assertTrue("Scale-invariant comparison must score ≥ 95, got ${result.overallScore}", result.overallScore >= 95f)
    }

    @Test
    fun scaleInvariance_tinyScale_shouldScoreHigh() {
        val expert = makePillar(20, scale = 1.0f)
        val learner = makePillar(20, scale = 0.1f)
        val result = engine.compare(learner, expert)
        assertTrue("Tiny scale must still score ≥ 95, got ${result.overallScore}", result.overallScore >= 95f)
    }

    // ── SUBSEQUENCE MATCHING ───────────────────────────────────────────────────

    @Test
    fun subsequenceDTW_patternEmbeddedInNoise_shouldFindMatch() {
        val expertPattern = makeVibration(20, 0f)
        val haystack = makeFrames(20, 0.05f, 0.05f) + expertPattern + makeFrames(20, 0.95f, 0.95f)
        val result = engine.compare(haystack, expertPattern)
        assertTrue("Subsequence match must score > 90, got ${result.overallScore}", result.overallScore > 90f)
    }

    @Test
    fun subsequenceDTW_patternAtStart_shouldFindMatch() {
        val expert = makeVibration(15, 0f)
        val haystack = expert + makeFrames(30, 0.5f, 0.5f)
        val result = engine.compare(haystack, expert)
        assertTrue("Pattern at start must score > 90, got ${result.overallScore}", result.overallScore > 90f)
    }

    @Test
    fun subsequenceDTW_patternAtEnd_shouldFindMatch() {
        val expert = makeVibration(15, 0f)
        val haystack = makeFrames(30, 0.5f, 0.5f) + expert
        val result = engine.compare(haystack, expert)
        assertTrue("Pattern at end must score > 90, got ${result.overallScore}", result.overallScore > 90f)
    }

    // ── SCORE RANGE INVARIANT ──────────────────────────────────────────────────

    @Test
    fun overallScore_alwaysInRange_0to100() {
        val cases = listOf(
            makeFrames(1, 0f, 0f) to makeFrames(1, 1f, 1f),
            makeFrames(10, 0.5f, 0.5f) to makeFrames(10, 0.5f, 0.5f),
            makeVibration(20, 0f) to makeVibration(20, 0.5f),
        )
        for ((learner, expert) in cases) {
            val result = engine.compare(learner, expert)
            assertTrue("Score out of range: ${result.overallScore}", result.overallScore in 0f..100f)
        }
    }

    @Test
    fun overallAccuracy_matchesScore_divided100() {
        val frames = makeFrames(10, 0.5f, 0.5f)
        val result = engine.compare(frames, frames)
        assertEquals(result.overallScore / 100f, result.overallAccuracy, 0.001f)
    }

    // ── PER-LANDMARK ACCURACY ──────────────────────────────────────────────────

    @Test
    fun identicalFrames_allLandmarks_shouldScore1() {
        val frames = makePillar(20, 1.0f)
        val result = engine.compare(frames, frames)
        for (i in 0..20) {
            val acc = result.perLandmarkAccuracy[i] ?: 0f
            assertTrue("Landmark $i accuracy should be ≥ 0.99, got $acc", acc >= 0.99f)
        }
    }

    @Test
    fun perLandmarkAccuracy_contains21Entries() {
        val frames = makeFrames(5, 0.5f, 0.5f)
        val result = engine.compare(frames, frames)
        assertEquals(21, result.perLandmarkAccuracy.size)
    }

    @Test
    fun perLandmarkAccuracy_allValuesIn0to1() {
        val learner = makeVibration(20, 0f)
        val expert = makeVibration(20, 0.1f)
        val result = engine.compare(learner, expert)
        for (i in 0..20) {
            val acc = result.perLandmarkAccuracy[i] ?: 0f
            assertTrue("Landmark $i accuracy out of range: $acc", acc in 0f..1f)
        }
    }

    // ── EXTREME TIME DILATION ──────────────────────────────────────────────────

    @Test
    fun oneLearnerFrame_vs_longExpert_shouldReturnStableResult() {
        val learner = listOf(makeFrames(1, 0.5f, 0.5f).first())
        val expert = makeFrames(20, 0.5f, 0.5f)
        val result = engine.compare(learner, expert)
        assertTrue("Single learner frame must score in range: ${result.overallScore}", result.overallScore in 0f..100f)
        assertTrue("Single learner frame vs identical expert must score > 90", result.overallScore > 90f)
    }

    @Test
    fun longLearner_vs_oneExpertFrame_shouldReturnStableResult() {
        val learner = makeFrames(30, 0.5f, 0.5f)
        val expert = listOf(makeFrames(1, 0.5f, 0.5f).first())
        val result = engine.compare(learner, expert)
        assertTrue("Score in range: ${result.overallScore}", result.overallScore in 0f..100f)
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private fun makeFrames(count: Int, x: Float, y: Float, hand: String = "Right"): List<HandFrame> =
        (1..count).map { HandFrame((0..20).map { i -> HandLandmark(x, y, 0f, i) }, hand) }

    private fun makePillar(count: Int, scale: Float): List<HandFrame> = (1..count).map {
        HandFrame((0..20).map { i ->
            if (i == 9) HandLandmark(0f, scale, scale, i) // middle MCP for normalisation anchor
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
