package com.culturesync.app

import com.culturesync.app.models.LandmarkIndex
import com.culturesync.app.utils.Constants
import org.junit.Assert.*
import org.junit.Test

/**
 * ConstantsTest — Validates all Constants and LandmarkIndex definitions
 * against the MediaPipe hand landmark specification.
 */
class ConstantsTest {

    // ── LANDMARK INDEX CONSTANTS ───────────────────────────────────────────────

    @Test
    fun wrist_isIndex0() {
        assertEquals(0, LandmarkIndex.WRIST)
    }

    @Test
    fun thumbCMC_isIndex1() {
        assertEquals(1, LandmarkIndex.THUMB_CMC)
    }

    @Test
    fun thumbMCP_isIndex2() {
        assertEquals(2, LandmarkIndex.THUMB_MCP)
    }

    @Test
    fun thumbIP_isIndex3() {
        assertEquals(3, LandmarkIndex.THUMB_IP)
    }

    @Test
    fun thumbTip_isIndex4() {
        assertEquals(4, LandmarkIndex.THUMB_TIP)
    }

    @Test
    fun indexMCP_isIndex5() {
        assertEquals(5, LandmarkIndex.INDEX_MCP)
    }

    @Test
    fun indexPIP_isIndex6() {
        assertEquals(6, LandmarkIndex.INDEX_PIP)
    }

    @Test
    fun indexDIP_isIndex7() {
        assertEquals(7, LandmarkIndex.INDEX_DIP)
    }

    @Test
    fun indexTip_isIndex8() {
        assertEquals(8, LandmarkIndex.INDEX_TIP)
    }

    @Test
    fun middleMCP_isIndex9() {
        assertEquals(9, LandmarkIndex.MIDDLE_MCP)
    }

    @Test
    fun middlePIP_isIndex10() {
        assertEquals(10, LandmarkIndex.MIDDLE_PIP)
    }

    @Test
    fun middleDIP_isIndex11() {
        assertEquals(11, LandmarkIndex.MIDDLE_DIP)
    }

    @Test
    fun middleTip_isIndex12() {
        assertEquals(12, LandmarkIndex.MIDDLE_TIP)
    }

    @Test
    fun ringMCP_isIndex13() {
        assertEquals(13, LandmarkIndex.RING_MCP)
    }

    @Test
    fun ringPIP_isIndex14() {
        assertEquals(14, LandmarkIndex.RING_PIP)
    }

    @Test
    fun ringDIP_isIndex15() {
        assertEquals(15, LandmarkIndex.RING_DIP)
    }

    @Test
    fun ringTip_isIndex16() {
        assertEquals(16, LandmarkIndex.RING_TIP)
    }

    @Test
    fun pinkyMCP_isIndex17() {
        assertEquals(17, LandmarkIndex.PINKY_MCP)
    }

    @Test
    fun pinkyPIP_isIndex18() {
        assertEquals(18, LandmarkIndex.PINKY_PIP)
    }

    @Test
    fun pinkyDIP_isIndex19() {
        assertEquals(19, LandmarkIndex.PINKY_DIP)
    }

    @Test
    fun pinkyTip_isIndex20() {
        assertEquals(20, LandmarkIndex.PINKY_TIP)
    }

    @Test
    fun allLandmarkIndexes_areUnique() {
        val all = listOf(
            LandmarkIndex.WRIST,
            LandmarkIndex.THUMB_CMC, LandmarkIndex.THUMB_MCP, LandmarkIndex.THUMB_IP, LandmarkIndex.THUMB_TIP,
            LandmarkIndex.INDEX_MCP, LandmarkIndex.INDEX_PIP, LandmarkIndex.INDEX_DIP, LandmarkIndex.INDEX_TIP,
            LandmarkIndex.MIDDLE_MCP, LandmarkIndex.MIDDLE_PIP, LandmarkIndex.MIDDLE_DIP, LandmarkIndex.MIDDLE_TIP,
            LandmarkIndex.RING_MCP, LandmarkIndex.RING_PIP, LandmarkIndex.RING_DIP, LandmarkIndex.RING_TIP,
            LandmarkIndex.PINKY_MCP, LandmarkIndex.PINKY_PIP, LandmarkIndex.PINKY_DIP, LandmarkIndex.PINKY_TIP
        )
        assertEquals("All 21 landmark indexes must be unique", all.size, all.distinct().size)
    }

    @Test
    fun allLandmarkIndexes_coverRange0to20() {
        val all = listOf(
            LandmarkIndex.WRIST,
            LandmarkIndex.THUMB_CMC, LandmarkIndex.THUMB_MCP, LandmarkIndex.THUMB_IP, LandmarkIndex.THUMB_TIP,
            LandmarkIndex.INDEX_MCP, LandmarkIndex.INDEX_PIP, LandmarkIndex.INDEX_DIP, LandmarkIndex.INDEX_TIP,
            LandmarkIndex.MIDDLE_MCP, LandmarkIndex.MIDDLE_PIP, LandmarkIndex.MIDDLE_DIP, LandmarkIndex.MIDDLE_TIP,
            LandmarkIndex.RING_MCP, LandmarkIndex.RING_PIP, LandmarkIndex.RING_DIP, LandmarkIndex.RING_TIP,
            LandmarkIndex.PINKY_MCP, LandmarkIndex.PINKY_PIP, LandmarkIndex.PINKY_DIP, LandmarkIndex.PINKY_TIP
        )
        assertEquals("Sorted indexes must equal 0..20", (0..20).toList(), all.sorted())
    }

    // ── CONSTANTS RANGE CHECKS ─────────────────────────────────────────────────

    @Test
    fun allAccuracyThresholds_areInValidRange() {
        assertTrue(Constants.ACCURACY_EXCELLENT in 1..100)
        assertTrue(Constants.ACCURACY_GOOD in 1..100)
        assertTrue(Constants.ACCURACY_FAIR in 1..100)
    }

    @Test
    fun techniqueIDs_allStartWithBeeralu() {
        val ids = listOf(
            Constants.TECHNIQUE_BASIC, Constants.TECHNIQUE_CROSS, Constants.TECHNIQUE_TWIST,
            Constants.TECHNIQUE_DIAMOND, Constants.TECHNIQUE_FLOWER, Constants.TECHNIQUE_WAVE
        )
        for (id in ids) {
            assertTrue("$id should start with 'beeralu_'", id.startsWith("beeralu_"))
        }
    }
}
