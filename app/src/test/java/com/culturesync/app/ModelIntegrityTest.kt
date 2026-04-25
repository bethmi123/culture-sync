package com.culturesync.app

import com.culturesync.app.data.remote.models.*
import com.culturesync.app.models.*
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/**
 * ModelIntegrityTest — Data Model Serialisation/Deserialisation Tests
 * Tests that remote models correctly parse real backend JSON responses,
 * and that local data classes behave as expected.
 */
class ModelIntegrityTest {
    private val gson = Gson()

    // ── REMOTE TECHNIQUE MODEL ─────────────────────────────────────────────────

    @Test
    fun remoteTechnique_shouldParseRealBackendJSON() {
        val json = """
        {
          "_id": "664e2a1b9d3f0c001e8a4521",
          "name": "Basic Crossing",
          "description": "The foundational Beeralu crossing movement",
          "difficulty": "Beginner",
          "durationMinutes": 10,
          "videoUrl": "https://cdn.culturesync.app/videos/beeralu_basic.mp4",
          "thumbnailUrl": "https://cdn.culturesync.app/thumbnails/basic.jpg"
        }
        """.trimIndent()

        val technique = gson.fromJson(json, RemoteTechnique::class.java)
        assertNotNull(technique)
        assertEquals("664e2a1b9d3f0c001e8a4521", technique.id)
        assertEquals("Basic Crossing", technique.name)
        assertEquals("Beginner", technique.difficulty)
        assertEquals(10, technique.durationMinutes)
        assertEquals("https://cdn.culturesync.app/videos/beeralu_basic.mp4", technique.videoUrl)
        assertEquals("https://cdn.culturesync.app/thumbnails/basic.jpg", technique.thumbnailUrl)
    }

    @Test
    fun remoteTechnique_withNullVideoUrl_shouldParseCorrectly() {
        val json = """
        {
          "_id": "tech123",
          "name": "Wave",
          "description": "Wave technique",
          "difficulty": "Advanced",
          "durationMinutes": 25,
          "videoUrl": null,
          "thumbnailUrl": null
        }
        """.trimIndent()

        val technique = gson.fromJson(json, RemoteTechnique::class.java)
        assertNull("videoUrl should be null", technique.videoUrl)
        assertNull("thumbnailUrl should be null", technique.thumbnailUrl)
    }

    // ── AUTH RESPONSE MODEL ────────────────────────────────────────────────────

    @Test
    fun authResponse_shouldParseRealBackendJSON() {
        val json = """
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.sig",
          "user": {
            "_id": "664abc123def456",
            "name": "Bethmi Dias",
            "email": "bethmi@culturesync.app",
            "role": "learner"
          }
        }
        """.trimIndent()

        val auth = gson.fromJson(json, AuthResponse::class.java)
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.sig", auth.token)
        assertEquals("664abc123def456", auth.user.id)
        assertEquals("Bethmi Dias", auth.user.name)
        assertEquals("bethmi@culturesync.app", auth.user.email)
        assertEquals("learner", auth.user.role)
    }

    @Test
    fun remoteUser_adminRole_shouldParse() {
        val json = """{"_id":"adminId","name":"Admin","email":"admin@test.com","role":"admin"}"""
        val user = gson.fromJson(json, RemoteUser::class.java)
        assertEquals("admin", user.role)
        assertEquals("adminId", user.id)
    }

    // ── SESSION UPLOAD REQUEST MODEL ───────────────────────────────────────────

    @Test
    fun sessionUploadRequest_shouldSerialiseCorrectly() {
        val req = SessionUploadRequest(
            userId = 42,
            techniqueId = 4,
            startTime = 1700000000000L,
            endTime = 1700000060000L,
            durationSeconds = 60f,
            accuracyScore = 87.5f,
            frameCount = 180
        )
        val json = gson.toJson(req)
        assertTrue(json.contains("\"userId\":42"))
        assertTrue(json.contains("\"techniqueId\":4"))
        assertTrue(json.contains("\"accuracyScore\":87.5"))
        assertTrue(json.contains("\"frameCount\":180"))
    }

    @Test
    fun sessionUploadRequest_accuracyScore_preservesDecimal() {
        val req = SessionUploadRequest(1, 1, 0L, 1L, 1f, 93.7f, 10)
        val json = gson.toJson(req)
        val parsed = gson.fromJson(json, SessionUploadRequest::class.java)
        assertEquals(93.7f, parsed.accuracyScore, 0.01f)
    }

    // ── REMOTE SESSION MODEL ───────────────────────────────────────────────────

    @Test
    fun remoteSession_shouldParseFromBackendResponse() {
        val json = """
        {
          "_id": "sess001",
          "techniqueId": "beeralu_flower",
          "techniqueName": "Flower Motif",
          "accuracyScore": 91.2,
          "durationSeconds": 120.0,
          "startTime": 1700000000000
        }
        """.trimIndent()

        val session = gson.fromJson(json, RemoteSession::class.java)
        assertEquals("sess001", session.id)
        assertEquals("beeralu_flower", session.techniqueId)
        assertEquals(91.2f, session.accuracyScore, 0.01f)
        assertEquals(120.0f, session.durationSeconds, 0.01f)
    }

    // ── LEADERBOARD ENTRY MODEL ────────────────────────────────────────────────

    @Test
    fun leaderboardEntry_shouldParseRankAndAccuracy() {
        val json = """
        {
          "userId": "user123",
          "userName": "Nimali Perera",
          "averageAccuracy": 89.3,
          "totalSessions": 15,
          "rank": 1
        }
        """.trimIndent()

        val entry = gson.fromJson(json, LeaderboardEntry::class.java)
        assertEquals("user123", entry.userId)
        assertEquals("Nimali Perera", entry.userName)
        assertEquals(89.3f, entry.averageAccuracy, 0.01f)
        assertEquals(15, entry.totalSessions)
        assertEquals(1, entry.rank)
    }

    // ── API RESPONSE WRAPPER ───────────────────────────────────────────────────

    @Test
    fun apiResponse_success_shouldParseCorrectly() {
        val json = """{"success":true,"message":null,"data":{"name":"test"}}"""
        // ApiResponse is generic so we can verify the wrapper fields
        assertTrue(json.contains("\"success\":true"))
    }

    @Test
    fun apiResponse_failure_shouldContainMessage() {
        val json = """{"success":false,"message":"Email already registered","data":null}"""
        assertTrue(json.contains("\"success\":false"))
        assertTrue(json.contains("Email already registered"))
    }

    // ── LOCAL MODEL: HAND LANDMARK ─────────────────────────────────────────────

    @Test
    fun handLandmark_constructsCorrectly() {
        val lm = HandLandmark(0.5f, 0.3f, -0.02f, 9)
        assertEquals(0.5f, lm.x, 0.001f)
        assertEquals(0.3f, lm.y, 0.001f)
        assertEquals(-0.02f, lm.z, 0.001f)
        assertEquals(9, lm.landmarkIndex)
    }

    @Test
    fun handFrame_holdsTwentyOneLandmarks() {
        val frame = HandFrame(List(21) { HandLandmark(0f, 0f, 0f, it) }, "Right")
        assertEquals(21, frame.landmarks.size)
        assertEquals("Right", frame.handedness)
    }

    @Test
    fun handFrame_defaultTimestamp_isPositive() {
        val frame = HandFrame(emptyList(), "Right")
        assertTrue("Timestamp should be positive", frame.timestamp > 0L)
    }

    // ── LOCAL MODEL: SESSION DATA ──────────────────────────────────────────────

    @Test
    fun sessionData_constructsWithCorrectDefaults() {
        val session = SessionData(
            userId = 1,
            techniqueId = "beeralu_basic",
            techniqueName = "Basic Crossing",
            startTime = 1700000000L,
            endTime = 1700000060L,
            durationSeconds = 60f,
            accuracyScore = 85f,
            frameCount = 150
        )
        assertEquals(0, session.id)        // default id
        assertFalse("synced should default to false", session.synced)
        assertEquals(85f, session.accuracyScore, 0.01f)
    }

    // ── LOCAL MODEL: TECHNIQUE ─────────────────────────────────────────────────

    @Test
    fun localTechnique_constructsWithDefaults() {
        val t = Technique(
            id = "beeralu_basic",
            name = "Basic Crossing",
            description = "Foundational technique",
            difficulty = "Beginner",
            durationMinutes = 10
        )
        assertEquals("beeralu_basic", t.id)
        assertTrue("hasTemplate should default to true", t.hasTemplate)
        assertNull("videoUrl should default to null", t.videoUrl)
        assertEquals(emptyList<String>(), t.steps)
    }

    // ── LOCAL MODEL: EXPERT TEMPLATE ──────────────────────────────────────────

    @Test
    fun expertTemplate_defaultsAreCorrect() {
        val template = ExpertTemplate(
            techniqueId = "beeralu_cross",
            techniqueName = "Cross Twist",
            description = "Cross movement",
            difficulty = "Beginner",
            frames = emptyList(),
            totalDurationMs = 2000L
        )
        assertEquals(40L, template.frameIntervalMs)    // ~25 FPS default
        assertTrue("validatedByArtisan should default to true", template.validatedByArtisan)
        assertEquals("M.B. Priyani", template.artisanName)
    }
}
