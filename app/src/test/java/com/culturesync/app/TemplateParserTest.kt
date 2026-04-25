package com.culturesync.app

import com.culturesync.app.models.ExpertTemplate
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.junit.Assert.*
import org.junit.Test

/**
 * TemplateParserTest — PRD §10.1
 * Verifies: valid JSON → ExpertTemplate; malformed JSON → exception with descriptive message.
 */
class TemplateParserTest {

    private val gson = Gson()

    // ── VALID JSON → ExpertTemplate ───────────────────────────────────────────

    @Test
    fun validJson_parsesToExpertTemplate() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertNotNull("Valid JSON must produce non-null ExpertTemplate", template)
    }

    @Test
    fun validJson_techniqueId_correct() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals("beeralu_basic", template.techniqueId)
    }

    @Test
    fun validJson_techniqueName_correct() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals("Basic Crossing", template.techniqueName)
    }

    @Test
    fun validJson_difficulty_correct() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals("Beginner", template.difficulty)
    }

    @Test
    fun validJson_frames_nonEmpty() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertTrue("Template must have at least 1 frame", template.frames.isNotEmpty())
    }

    @Test
    fun validJson_frame_has21Landmarks() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals(21, template.frames[0].landmarks.size)
    }

    @Test
    fun validJson_handedness_correct() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals("Right", template.frames[0].handedness)
    }

    @Test
    fun validJson_totalDurationMs_correct() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals(2000L, template.totalDurationMs)
    }

    @Test
    fun validJson_artisanName_preserved() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertEquals("M.B. Priyani", template.artisanName)
    }

    @Test
    fun validJson_validatedByArtisan_true() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        assertTrue("Template must be validated by artisan", template.validatedByArtisan)
    }

    // ── VALID JSON — ALL 6 TECHNIQUE IDs ─────────────────────────────────────

    @Test
    fun allSixTechniqueIds_parseSuccessfully() {
        val ids = listOf(
            "beeralu_basic", "beeralu_cross", "beeralu_twist",
            "beeralu_diamond", "beeralu_flower", "beeralu_wave"
        )
        for (id in ids) {
            val json = minimalTemplateJson(id)
            val template = gson.fromJson(json, ExpertTemplate::class.java)
            assertNotNull("Template for $id must parse", template)
            assertEquals(id, template.techniqueId)
        }
    }

    // ── MALFORMED JSON → JsonSyntaxException ──────────────────────────────────

    @Test(expected = JsonSyntaxException::class)
    fun malformedJson_missingClosingBrace_throwsException() {
        val badJson = """{"techniqueId":"beeralu_basic","frames":["""
        gson.fromJson(badJson, ExpertTemplate::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun malformedJson_randomString_throwsException() {
        gson.fromJson("not json at all !!@#", ExpertTemplate::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun malformedJson_unclosedString_throwsException() {
        val badJson = """{"techniqueId": "unclosed_string}"""
        gson.fromJson(badJson, ExpertTemplate::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun malformedJson_arrayInsteadOfObject_throwsException() {
        gson.fromJson("[1, 2, 3]", ExpertTemplate::class.java)
    }

    // ── PARTIAL / MISSING FIELDS ──────────────────────────────────────────────

    @Test
    fun json_missingOptionalFields_usesDefaults() {
        val minJson = """
        {
          "techniqueId": "beeralu_wave",
          "techniqueName": "Wave",
          "description": "Wave technique",
          "difficulty": "Advanced",
          "totalDurationMs": 3000,
          "frames": []
        }
        """.trimIndent()
        val template = gson.fromJson(minJson, ExpertTemplate::class.java)
        assertNotNull(template)
        assertEquals("beeralu_wave", template.techniqueId)
        // Defaults from ExpertTemplate data class
        assertEquals(40L, template.frameIntervalMs)      // ~25 FPS default
        assertTrue(template.validatedByArtisan)
        assertEquals("M.B. Priyani", template.artisanName)
    }

    @Test
    fun json_emptyFramesList_parsesWithoutError() {
        val json = minimalTemplateJson("beeralu_twist")
        val template = gson.fromJson(json, ExpertTemplate::class.java)
        assertTrue(template.frames.isEmpty())
    }

    @Test
    fun json_landmarkCoordinates_parsedAsFloats() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        val wrist = template.frames[0].landmarks[0]
        assertEquals(0.500f, wrist.x, 0.001f)
        assertEquals(0.820f, wrist.y, 0.001f)
        assertEquals(0.000f, wrist.z, 0.001f)
        assertEquals(0, wrist.landmarkIndex)
    }

    @Test
    fun json_landmarkIndexes_0to20_allPresent() {
        val template = gson.fromJson(VALID_TEMPLATE_JSON, ExpertTemplate::class.java)
        val indexes = template.frames[0].landmarks.map { it.landmarkIndex }.sorted()
        assertEquals((0..20).toList(), indexes)
    }

    // ── HELPERS / TEST DATA ───────────────────────────────────────────────────

    private fun minimalTemplateJson(techniqueId: String) = """
    {
      "techniqueId": "$techniqueId",
      "techniqueName": "Test Technique",
      "description": "A test description",
      "difficulty": "Beginner",
      "totalDurationMs": 1000,
      "frames": []
    }
    """.trimIndent()

    companion object {
        private val VALID_TEMPLATE_JSON = """
        {
          "techniqueId": "beeralu_basic",
          "techniqueName": "Basic Crossing",
          "description": "Foundational Beeralu crossing technique",
          "difficulty": "Beginner",
          "totalDurationMs": 2000,
          "frameIntervalMs": 40,
          "validatedByArtisan": true,
          "artisanName": "M.B. Priyani",
          "recordedAt": "2024-04-20",
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
    }
}
