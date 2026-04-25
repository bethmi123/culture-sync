package com.culturesync.app

import com.culturesync.app.utils.toAccuracyLabel
import com.culturesync.app.utils.toTimeString
import com.culturesync.app.utils.Constants
import org.junit.Assert.*
import org.junit.Test

/**
 * ExtensionsTest — Extension Function Unit Tests
 * Tests toAccuracyLabel(), toTimeString(), and constant boundary values.
 * All tests use real data with no mocking.
 */
class ExtensionsTest {

    // ── toAccuracyLabel() ──────────────────────────────────────────────────────

    @Test
    fun score100_shouldBeExcellent() {
        assertEquals("Excellent", 100f.toAccuracyLabel())
    }

    @Test
    fun scoreAtExcellentThreshold_shouldBeExcellent() {
        assertEquals("Excellent", Constants.ACCURACY_EXCELLENT.toFloat().toAccuracyLabel())
    }

    @Test
    fun scoreAboveExcellentThreshold_shouldBeExcellent() {
        assertEquals("Excellent", 90f.toAccuracyLabel())
    }

    @Test
    fun score88_shouldBeExcellent() {
        assertEquals("Excellent", 88f.toAccuracyLabel())
    }

    @Test
    fun scoreJustBelowExcellent_shouldBeGood() {
        // ACCURACY_EXCELLENT = 85, so 84 should be Good (if >= ACCURACY_GOOD = 70)
        assertEquals("Good", 84f.toAccuracyLabel())
    }

    @Test
    fun scoreAtGoodThreshold_shouldBeGood() {
        assertEquals("Good", Constants.ACCURACY_GOOD.toFloat().toAccuracyLabel())
    }

    @Test
    fun score75_shouldBeGood() {
        assertEquals("Good", 75f.toAccuracyLabel())
    }

    @Test
    fun scoreJustBelowGood_shouldKeepPractising() {
        // ACCURACY_GOOD = 70, so 69 → KeepPractising if >= ACCURACY_FAIR=50
        assertEquals("Keep Practising", 69f.toAccuracyLabel())
    }

    @Test
    fun scoreAtFairThreshold_shouldKeepPractising() {
        assertEquals("Keep Practising", Constants.ACCURACY_FAIR.toFloat().toAccuracyLabel())
    }

    @Test
    fun score55_shouldKeepPractising() {
        assertEquals("Keep Practising", 55f.toAccuracyLabel())
    }

    @Test
    fun scoreJustBelowFair_shouldNeedsWork() {
        // ACCURACY_FAIR = 50, so 49 → NeedsWork
        assertEquals("Needs Work", 49f.toAccuracyLabel())
    }

    @Test
    fun score30_shouldBeNeedsWork() {
        assertEquals("Needs Work", 30f.toAccuracyLabel())
    }

    @Test
    fun score0_shouldBeNeedsWork() {
        assertEquals("Needs Work", 0f.toAccuracyLabel())
    }

    @Test
    fun score1_shouldBeNeedsWork() {
        assertEquals("Needs Work", 1f.toAccuracyLabel())
    }

    // ── toTimeString() ────────────────────────────────────────────────────────

    @Test
    fun seconds0_shouldFormat0s() {
        assertEquals("0s", 0f.toTimeString())
    }

    @Test
    fun seconds1_shouldFormat1s() {
        assertEquals("1s", 1f.toTimeString())
    }

    @Test
    fun seconds45_shouldFormat45s() {
        assertEquals("45s", 45f.toTimeString())
    }

    @Test
    fun seconds59_shouldFormat59s() {
        assertEquals("59s", 59f.toTimeString())
    }

    @Test
    fun seconds60_shouldFormat1m0s() {
        assertEquals("1m 0s", 60f.toTimeString())
    }

    @Test
    fun seconds75_shouldFormat1m15s() {
        assertEquals("1m 15s", 75f.toTimeString())
    }

    @Test
    fun seconds90_shouldFormat1m30s() {
        assertEquals("1m 30s", 90f.toTimeString())
    }

    @Test
    fun seconds120_shouldFormat2m0s() {
        assertEquals("2m 0s", 120f.toTimeString())
    }

    @Test
    fun seconds150_shouldFormat2m30s() {
        assertEquals("2m 30s", 150f.toTimeString())
    }

    @Test
    fun seconds3600_shouldFormat60m0s() {
        assertEquals("60m 0s", 3600f.toTimeString())
    }

    @Test
    fun seconds61_shouldFormat1m1s() {
        assertEquals("1m 1s", 61f.toTimeString())
    }

    // ── CONSTANTS VALIDATION ───────────────────────────────────────────────────

    @Test
    fun accuracyExcellent_shouldBe85() {
        assertEquals(85, Constants.ACCURACY_EXCELLENT)
    }

    @Test
    fun accuracyGood_shouldBe70() {
        assertEquals(70, Constants.ACCURACY_GOOD)
    }

    @Test
    fun accuracyFair_shouldBe50() {
        assertEquals(50, Constants.ACCURACY_FAIR)
    }

    @Test
    fun thresholds_areOrdered_excellentGtGoodGtFair() {
        assertTrue("EXCELLENT > GOOD", Constants.ACCURACY_EXCELLENT > Constants.ACCURACY_GOOD)
        assertTrue("GOOD > FAIR", Constants.ACCURACY_GOOD > Constants.ACCURACY_FAIR)
    }

    @Test
    fun dtwCompareInterval_isPositive() {
        assertTrue(Constants.DTW_COMPARE_INTERVAL_FRAMES > 0)
    }

    @Test
    fun minFramesForDtw_isLessThan_compareInterval() {
        assertTrue(
            "MIN_FRAMES_FOR_DTW should be reasonable",
            Constants.MIN_FRAMES_FOR_DTW > 0 && Constants.MIN_FRAMES_FOR_DTW <= Constants.DTW_COMPARE_INTERVAL_FRAMES
        )
    }

    @Test
    fun maxBufferFrames_isGreaterThan_compareInterval() {
        assertTrue("MAX_BUFFER_FRAMES > DTW interval", Constants.MAX_BUFFER_FRAMES > Constants.DTW_COMPARE_INTERVAL_FRAMES)
    }

    @Test
    fun connectTimeout_isPositive() {
        assertTrue(Constants.CONNECT_TIMEOUT_SEC > 0)
    }

    @Test
    fun readTimeout_isGreaterThan_connectTimeout() {
        assertTrue("Read timeout >= connect timeout", Constants.READ_TIMEOUT_SEC >= Constants.CONNECT_TIMEOUT_SEC)
    }

    // ── TECHNIQUE ID CONSTANTS ─────────────────────────────────────────────────

    @Test
    fun all6TechniqueIds_areDistinct() {
        val ids = listOf(
            Constants.TECHNIQUE_BASIC,
            Constants.TECHNIQUE_CROSS,
            Constants.TECHNIQUE_TWIST,
            Constants.TECHNIQUE_DIAMOND,
            Constants.TECHNIQUE_FLOWER,
            Constants.TECHNIQUE_WAVE
        )
        assertEquals("All 6 technique IDs must be unique", ids.size, ids.distinct().size)
    }

    @Test
    fun techniqueBasic_isBeraluBasic() {
        assertEquals("beeralu_basic", Constants.TECHNIQUE_BASIC)
    }

    @Test
    fun techniqueWave_isBeraluWave() {
        assertEquals("beeralu_wave", Constants.TECHNIQUE_WAVE)
    }
}
