package com.culturesync.app

import com.culturesync.app.ml.DTWEngine
import com.culturesync.app.models.HandFrame
import com.culturesync.app.models.HandLandmark
import org.junit.Assert.*
import org.junit.Test

/**
 * DTWEngineEdgeCaseTeardown — Resilience & Boundary Tests
 * Tests NaN, Infinity, mismatched landmark counts, extreme time dilation,
 * and structural mismatch handling without crashing.
 */
class DTWEngineEdgeCaseTeardown {
    private val engine = DTWEngine()

    // ── BOUNDARY VALUES ────────────────────────────────────────────────────────

    @Test
    fun zeroCoordinates_allLandmarks_shouldNotCrash() {
        val frames = List(10) { HandFrame(List(21) { i -> HandLandmark(0f, 0f, 0f, i) }, "Right") }
        val result = engine.compare(frames, frames)
        // All wrist-relative: after normalisation all are 0, handSize ≈ 0 → scale = 1 (guarded)
        assertTrue("Score must be in [0,100]: ${result.overallScore}", result.overallScore in 0f..100f)
    }

    @Test
    fun maxCoordinates_shouldNotCrash() {
        val frames = List(5) { HandFrame(List(21) { i -> HandLandmark(1f, 1f, 1f, i) }, "Right") }
        val result = engine.compare(frames, frames)
        assertTrue("Score in range: ${result.overallScore}", result.overallScore in 0f..100f)
    }

    @Test
    fun singleFrame_identicalToExpert_shouldScoreHigh() {
        val frame = HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")
        val result = engine.compare(listOf(frame), listOf(frame))
        assertTrue("Single-frame identical match must score > 99, got ${result.overallScore}", result.overallScore > 99f)
    }

    @Test
    fun veryLongSequences_shouldNotThrow() {
        val learner = List(200) { HandFrame(List(21) { i -> HandLandmark(0.5f, 0.5f, 0f, i) }, "Right") }
        val expert = List(200) { HandFrame(List(21) { i -> HandLandmark(0.5f, 0.5f, 0f, i) }, "Right") }
        val result = engine.compare(learner, expert)
        assertTrue("Long sequences score in range: ${result.overallScore}", result.overallScore in 0f..100f)
    }

    // ── NaN / INFINITY RESILIENCE ──────────────────────────────────────────────

    @Test
    fun nanCoordinates_shouldNotThrowException() {
        val nanFrame = HandFrame(List(21) { HandLandmark(Float.NaN, Float.NaN, Float.NaN, it) }, "Right")
        val validFrame = HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")

        // Must not throw — engine should handle NaN gracefully
        val result = engine.compare(listOf(nanFrame), listOf(validFrame))
        assertTrue("NaN input must yield score ≤ 100: ${result.overallScore}", result.overallScore <= 100f)
    }

    @Test
    fun positiveInfinity_shouldNotThrowException() {
        val infFrame = HandFrame(List(21) { HandLandmark(Float.POSITIVE_INFINITY, 0f, 0f, it) }, "Right")
        assertDoesNotThrow { engine.compare(listOf(infFrame), listOf(infFrame)) }
    }

    @Test
    fun negativeInfinity_shouldNotThrowException() {
        val infFrame = HandFrame(List(21) { HandLandmark(Float.NEGATIVE_INFINITY, 0f, 0f, it) }, "Right")
        val valid = HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")
        assertDoesNotThrow { engine.compare(listOf(infFrame), listOf(valid)) }
    }

    // ── MISMATCHED LANDMARK COUNTS ────────────────────────────────────────────

    @Test
    fun fewerLandmarksThanExpected_shouldReturnZeroNotCrash() {
        // Frame with only 5 landmarks (tracking loss) vs full 21-landmark expert
        val brokenFrame = HandFrame(List(5) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")
        val expertFrame = HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")
        val result = engine.compare(listOf(brokenFrame), listOf(expertFrame))
        // Structural mismatch: engine should return 0 without throwing
        assertEquals(0f, result.overallScore, 0.1f)
    }

    @Test
    fun moreLandmarksThanExpected_shouldHandleGracefully() {
        // Unusual case: 22 landmarks instead of 21
        val extraFrame = HandFrame(List(22) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")
        val expertFrame = HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right")
        assertDoesNotThrow { engine.compare(listOf(extraFrame), listOf(expertFrame)) }
    }

    // ── EXTREME TIME DILATION ──────────────────────────────────────────────────

    @Test
    fun learnerWith1Frame_expertWith20_identicalPose_shouldScoreHigh() {
        val learner = listOf(HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right"))
        val expert = List(20) { HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right") }
        val result = engine.compare(learner, expert)
        assertTrue("Extreme time dilation same pose: score > 90, got ${result.overallScore}", result.overallScore > 90f)
    }

    @Test
    fun learnerWith50Frames_expertWith5_shouldStillWork() {
        val learner = List(50) { HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right") }
        val expert = List(5) { HandFrame(List(21) { HandLandmark(0.5f, 0.5f, 0f, it) }, "Right") }
        val result = engine.compare(learner, expert)
        assertTrue("Many-to-few: score in [0,100]: ${result.overallScore}", result.overallScore in 0f..100f)
    }

    // ── MIXED HANDEDNESS EDGE CASES ───────────────────────────────────────────

    @Test
    fun mixedHandedness_leftLearnerRightExpert_shouldMirror() {
        val learner = List(10) { HandFrame(List(21) { i -> HandLandmark(0.3f, 0.4f, 0f, i) }, "Left") }
        val expert = List(10) { HandFrame(List(21) { i -> HandLandmark(0.3f, 0.4f, 0f, i) }, "Right") }
        val result = engine.compare(learner, expert)
        assertTrue("Mirror detected: score > 90, got ${result.overallScore}", result.overallScore > 90f)
    }

    @Test
    fun sameHandedness_bothLeft_noMirrorNeeded() {
        val frames = List(10) { HandFrame(List(21) { i -> HandLandmark(0.4f, 0.6f, 0f, i) }, "Left") }
        val result = engine.compare(frames, frames)
        assertTrue("Both Left, identical: score ≥ 99, got ${result.overallScore}", result.overallScore >= 99f)
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception but got: ${e::class.simpleName}: ${e.message}")
        }
    }
}
