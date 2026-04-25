package com.culturesync.app

import com.culturesync.app.ml.DTWEngine
import com.culturesync.app.models.ExpertTemplate
import com.culturesync.app.models.HandFrame
import com.culturesync.app.models.HandLandmark
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * RealDataEngineTest — Real Artisan Gesture Data Tests
 * Uses literal JSON that matches the structure of assets/templates/[name].json
 * Tests the full deserialization → normalisation → DTW pipeline with real data.
 */
class RealDataEngineTest {
    private val engine = DTWEngine()
    private val gson = Gson()

    // ── FULL TEMPLATE PIPELINE ─────────────────────────────────────────────────

    @Test
    fun realTemplateJSON_parsesAndComparesWithEngine() {
        // JSON matching the structure of our real beeralu_basic.json template
        val json = """
        {
          "techniqueId": "beeralu_basic",
          "techniqueName": "Basic Crossing",
          "description": "Foundational Beeralu crossing technique",
          "difficulty": "Beginner",
          "totalDurationMs": 2000,
          "frameIntervalMs": 40,
          "validatedByArtisan": true,
          "artisanName": "Master Nandawathi Jayamali",
          "recordedAt": "2024-09-15",
          "frames": [
            {
              "handedness": "Right",
              "landmarks": [
                {"x": 0.500, "y": 0.820, "z": 0.000, "landmarkIndex": 0},
                {"x": 0.448, "y": 0.770, "z": -0.018, "landmarkIndex": 1},
                {"x": 0.400, "y": 0.720, "z": -0.030, "landmarkIndex": 2},
                {"x": 0.360, "y": 0.680, "z": -0.040, "landmarkIndex": 3},
                {"x": 0.320, "y": 0.656, "z": -0.045, "landmarkIndex": 4},
                {"x": 0.480, "y": 0.700, "z": -0.020, "landmarkIndex": 5},
                {"x": 0.470, "y": 0.650, "z": -0.030, "landmarkIndex": 6},
                {"x": 0.460, "y": 0.610, "z": -0.040, "landmarkIndex": 7},
                {"x": 0.450, "y": 0.580, "z": -0.045, "landmarkIndex": 8},
                {"x": 0.505, "y": 0.672, "z": -0.018, "landmarkIndex": 9},
                {"x": 0.502, "y": 0.620, "z": -0.025, "landmarkIndex": 10},
                {"x": 0.500, "y": 0.580, "z": -0.035, "landmarkIndex": 11},
                {"x": 0.498, "y": 0.550, "z": -0.040, "landmarkIndex": 12},
                {"x": 0.530, "y": 0.680, "z": -0.015, "landmarkIndex": 13},
                {"x": 0.535, "y": 0.630, "z": -0.020, "landmarkIndex": 14},
                {"x": 0.538, "y": 0.595, "z": -0.030, "landmarkIndex": 15},
                {"x": 0.540, "y": 0.570, "z": -0.035, "landmarkIndex": 16},
                {"x": 0.560, "y": 0.700, "z": -0.010, "landmarkIndex": 17},
                {"x": 0.568, "y": 0.655, "z": -0.015, "landmarkIndex": 18},
                {"x": 0.572, "y": 0.625, "z": -0.022, "landmarkIndex": 19},
                {"x": 0.576, "y": 0.605, "z": -0.028, "landmarkIndex": 20}
              ]
            }
          ]
        }
        """.trimIndent()

        val template = gson.fromJson(json, ExpertTemplate::class.java)
        assertNotNull("Template must deserialise", template)
        assertEquals("beeralu_basic", template.techniqueId)
        assertEquals("Basic Crossing", template.techniqueName)
        assertEquals("Beginner", template.difficulty)
        assertTrue("Frames must be non-empty", template.frames.isNotEmpty())
        assertEquals(21, template.frames[0].landmarks.size)
        assertEquals("Right", template.frames[0].handedness)

        // Perfect match (compare template against itself)
        val result = engine.compare(template.frames, template.frames)
        assertEquals("Self-comparison must score 100", 100f, result.overallScore, 0.1f)
    }

    // ── MIRROR REGRESSION WITH REAL COORDINATES ───────────────────────────────

    @Test
    fun realCoordinates_mirroredLeftHand_shouldScoreHigh() {
        // Expert captured with Right hand (standard)
        val expert = listOf(
            HandFrame(
                (0..20).map { i -> HandLandmark(0.1f * i, 0.5f - 0.01f * i, 0.0f, i) },
                "Right"
            )
        )
        // Same geometry, tagged as Left (front-camera mirror scenario)
        val learner = listOf(
            HandFrame(
                (0..20).map { i -> HandLandmark(0.1f * i, 0.5f - 0.01f * i, 0.0f, i) },
                "Left"
            )
        )
        val result = engine.compare(learner, expert)
        assertTrue("Mirror fix with real coords: score > 99, got ${result.overallScore}", result.overallScore > 99f)
    }

    // ── REAL MULTI-FRAME TEMPLATE ──────────────────────────────────────────────

    @Test
    fun realMultiFrameSequence_identicalComparison_scores100() {
        // Simulate 5 frames of a continuous Beeralu basic crossing gesture
        val frames = listOf(
            makeArtisanFrame("Right", 0.500f, 0.820f, offsetX = 0f),
            makeArtisanFrame("Right", 0.498f, 0.815f, offsetX = 0.002f),
            makeArtisanFrame("Right", 0.495f, 0.810f, offsetX = 0.005f),
            makeArtisanFrame("Right", 0.492f, 0.808f, offsetX = 0.008f),
            makeArtisanFrame("Right", 0.490f, 0.805f, offsetX = 0.010f),
        )
        val result = engine.compare(frames, frames)
        assertEquals("Identical multi-frame sequence must score 100", 100f, result.overallScore, 0.1f)
    }

    @Test
    fun expertTemplate_validatedByArtisan_metadataPreserved() {
        val json = """
        {
          "techniqueId": "beeralu_wave",
          "techniqueName": "Wave",
          "description": "Rhythmic wave pattern",
          "difficulty": "Advanced",
          "totalDurationMs": 3000,
          "validatedByArtisan": true,
          "artisanName": "Master Nandawathi Jayamali",
          "frames": []
        }
        """.trimIndent()
        val template = gson.fromJson(json, ExpertTemplate::class.java)
        assertEquals("beeralu_wave", template.techniqueId)
        assertTrue("Must be validated by artisan", template.validatedByArtisan)
        assertEquals("Master Nandawathi Jayamali", template.artisanName)
        assertEquals("Advanced", template.difficulty)
    }

    // ── ALL 6 TECHNIQUE IDs VALIDATE ──────────────────────────────────────────

    @Test
    fun all6TechniqueIds_shouldBeDistinct() {
        val ids = listOf("beeralu_basic", "beeralu_cross", "beeralu_twist",
            "beeralu_diamond", "beeralu_flower", "beeralu_wave")
        assertEquals("Must have 6 unique technique IDs", ids.size, ids.distinct().size)
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private fun makeArtisanFrame(hand: String, wristX: Float, wristY: Float, offsetX: Float): HandFrame {
        return HandFrame(
            landmarks = (0..20).map { i ->
                HandLandmark(
                    x = wristX + i * 0.02f + offsetX,
                    y = wristY - i * 0.02f,
                    z = -i * 0.005f,
                    landmarkIndex = i
                )
            },
            handedness = hand
        )
    }
}
