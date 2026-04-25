package com.culturesync.app

import com.culturesync.app.ml.ARConstants
import com.culturesync.app.ml.AccuracyResult
import org.junit.Assert.*
import org.junit.Test

/**
 * AccuracyResultTest — PRD §10.1
 * Verifies colour threshold mapping: >=0.85 → green, >=0.65 → yellow, <0.65 → red.
 */
class AccuracyResultTest {

    // ── THRESHOLD BOUNDARY: GREEN (>= 0.85) ──────────────────────────────────

    @Test
    fun accuracy_exactGreenThreshold_mapsToGreen() {
        val acc = ARConstants.GREEN_ACCURACY_THRESHOLD
        val color = thresholdColor(acc)
        assertEquals("0.85 must map to GREEN", ARConstants.COLOR_GREEN, color)
    }

    @Test
    fun accuracy_aboveGreenThreshold_mapsToGreen() {
        val color = thresholdColor(0.99f)
        assertEquals("0.99 must map to GREEN", ARConstants.COLOR_GREEN, color)
    }

    @Test
    fun accuracy_perfectScore_mapsToGreen() {
        val color = thresholdColor(1.0f)
        assertEquals("1.0 must map to GREEN", ARConstants.COLOR_GREEN, color)
    }

    // ── THRESHOLD BOUNDARY: YELLOW (>= 0.65, < 0.85) ─────────────────────────

    @Test
    fun accuracy_exactYellowThreshold_mapsToYellow() {
        val acc = ARConstants.YELLOW_ACCURACY_THRESHOLD
        val color = thresholdColor(acc)
        assertEquals("0.65 must map to YELLOW", ARConstants.COLOR_YELLOW, color)
    }

    @Test
    fun accuracy_justBelowGreenThreshold_mapsToYellow() {
        val color = thresholdColor(0.84f)
        assertEquals("0.84 must map to YELLOW", ARConstants.COLOR_YELLOW, color)
    }

    @Test
    fun accuracy_midYellowRange_mapsToYellow() {
        val color = thresholdColor(0.75f)
        assertEquals("0.75 must map to YELLOW", ARConstants.COLOR_YELLOW, color)
    }

    // ── THRESHOLD BOUNDARY: RED (< 0.65) ─────────────────────────────────────

    @Test
    fun accuracy_justBelowYellowThreshold_mapsToRed() {
        val color = thresholdColor(0.64f)
        assertEquals("0.64 must map to RED", ARConstants.COLOR_RED, color)
    }

    @Test
    fun accuracy_zero_mapsToRed() {
        val color = thresholdColor(0.0f)
        assertEquals("0.0 must map to RED", ARConstants.COLOR_RED, color)
    }

    @Test
    fun accuracy_midRedRange_mapsToRed() {
        val color = thresholdColor(0.30f)
        assertEquals("0.30 must map to RED", ARConstants.COLOR_RED, color)
    }

    // ── ACCURACYRESULT DATA CLASS ─────────────────────────────────────────────

    @Test
    fun overallScore_equals_overallAccuracyTimes100() {
        val result = AccuracyResult(overallAccuracy = 0.85f, perJointAccuracy = List(21) { 0.85f })
        assertEquals(85f, result.overallScore, 0.001f)
    }

    @Test
    fun overallPercent_matchesOverallScore() {
        val result = AccuracyResult(overallAccuracy = 0.72f, perJointAccuracy = List(21) { 0.72f })
        assertEquals(result.overallScore, result.overallPercent, 0.001f)
    }

    @Test
    fun perJointMap_contains21Entries() {
        val result = AccuracyResult(
            overallAccuracy = 0.9f,
            perJointAccuracy = List(21) { it / 21f }
        )
        assertEquals(21, result.perJointMap().size)
    }

    @Test
    fun perJointMap_keysAre0to20() {
        val result = AccuracyResult(
            overallAccuracy = 0.5f,
            perJointAccuracy = List(21) { 0.5f }
        )
        assertEquals((0..20).toSet(), result.perJointMap().keys)
    }

    @Test
    fun perLandmarkAccuracy_alias_matchesPerJointMap() {
        val result = AccuracyResult(
            overallAccuracy = 0.8f,
            perJointAccuracy = List(21) { i -> i / 20f }
        )
        assertEquals(result.perJointMap(), result.perLandmarkAccuracy)
    }

    @Test
    fun empty_companion_hasZeroAccuracy() {
        assertEquals(0f, AccuracyResult.EMPTY.overallAccuracy, 0.001f)
        assertEquals(0f, AccuracyResult.EMPTY.overallScore, 0.001f)
    }

    @Test
    fun empty_companion_has21ZeroJointValues() {
        assertEquals(21, AccuracyResult.EMPTY.perJointAccuracy.size)
        AccuracyResult.EMPTY.perJointAccuracy.forEach { v ->
            assertEquals(0f, v, 0.001f)
        }
    }

    // ── THRESHOLD CONSTANTS ───────────────────────────────────────────────────

    @Test
    fun greenThreshold_is_0p85() {
        assertEquals(0.85f, ARConstants.GREEN_ACCURACY_THRESHOLD, 0.001f)
    }

    @Test
    fun yellowThreshold_is_0p65() {
        assertEquals(0.65f, ARConstants.YELLOW_ACCURACY_THRESHOLD, 0.001f)
    }

    @Test
    fun greenThreshold_isAbove_yellowThreshold() {
        assertTrue(ARConstants.GREEN_ACCURACY_THRESHOLD > ARConstants.YELLOW_ACCURACY_THRESHOLD)
    }

    // ── HELPER — mirrors AROverlayView.accuracyColor ──────────────────────────

    private fun thresholdColor(accuracy: Float): Int = when {
        accuracy >= ARConstants.GREEN_ACCURACY_THRESHOLD  -> ARConstants.COLOR_GREEN
        accuracy >= ARConstants.YELLOW_ACCURACY_THRESHOLD -> ARConstants.COLOR_YELLOW
        else                                              -> ARConstants.COLOR_RED
    }
}
