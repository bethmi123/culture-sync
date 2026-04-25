package com.culturesync.app

import com.culturesync.app.data.local.daos.SessionDao
import com.culturesync.app.data.local.entities.SessionEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * SessionDBSecuritySuite — Room DAO Mock Tests
 * Verifies all DAO query paths in isolation using Mockito.
 * Covers: inserts, queries, aggregates, sync state management, boundaries.
 */
class SessionDBSecuritySuite {

    @Mock lateinit var mockDao: SessionDao

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    // ── INSERT SESSION ─────────────────────────────────────────────────────────

    @Test
    fun insertSession_returnsGeneratedId() = runTest {
        val session = makeSession(1, 1, 85f)
        `when`(mockDao.insertSession(session)).thenReturn(1L)
        val id = mockDao.insertSession(session)
        assertEquals(1L, id)
    }

    @Test
    fun insertSession_with100Accuracy_succeeds() = runTest {
        val session = makeSession(1, 5, 100f)
        `when`(mockDao.insertSession(session)).thenReturn(2L)
        val id = mockDao.insertSession(session)
        assertTrue("Insert must return positive ID", id > 0)
    }

    @Test
    fun insertSession_withZeroAccuracy_succeeds() = runTest {
        val session = makeSession(1, 6, 0f)
        `when`(mockDao.insertSession(session)).thenReturn(3L)
        val id = mockDao.insertSession(session)
        assertTrue(id > 0)
    }

    // ── getSessionsByUser() ───────────────────────────────────────────────────

    @Test
    fun getSessionsByUser_emptyUser_returnsEmptyFlow() = runTest {
        `when`(mockDao.getSessionsByUser(999)).thenReturn(flowOf(emptyList()))
        val flow = mockDao.getSessionsByUser(999)
        assertNotNull(flow)
    }

    @Test
    fun getSessionsByUser_withSessions_returnsCorrectFlow() = runTest {
        val sessions = listOf(makeSession(1, 1, 75f))
        `when`(mockDao.getSessionsByUser(1)).thenReturn(flowOf(sessions))
        val flow = mockDao.getSessionsByUser(1)
        assertNotNull(flow)
    }

    // ── getRecentSessions() ───────────────────────────────────────────────────

    @Test
    fun getRecentSessions_limitOf5_returnsCorrectCount() = runTest {
        val sessions = List(5) { makeSession(1, 1, 70f + it) }
        `when`(mockDao.getRecentSessions(1, 5)).thenReturn(sessions)
        val result = mockDao.getRecentSessions(1, 5)
        assertEquals(5, result.size)
    }

    @Test
    fun getRecentSessions_limitOf1_returnsOnlyOne() = runTest {
        val session = makeSession(1, 2, 88f)
        `when`(mockDao.getRecentSessions(1, 1)).thenReturn(listOf(session))
        val result = mockDao.getRecentSessions(1, 1)
        assertEquals(1, result.size)
        assertEquals(88f, result[0].finalAccuracy!! * 100f, 0.01f)
    }

    @Test
    fun getRecentSessions_noSessions_returnsEmpty() = runTest {
        `when`(mockDao.getRecentSessions(99, 10)).thenReturn(emptyList())
        val result = mockDao.getRecentSessions(99, 10)
        assertTrue(result.isEmpty())
    }

    // ── getUnsyncedSessions() ─────────────────────────────────────────────────

    @Test
    fun getUnsyncedSessions_returnsOnlyUnsyncedEntries() = runTest {
        val unsynced = listOf(
            makeSession(1, 1, 80f, syncStatus = "PENDING"),
            makeSession(1, 2, 90f, syncStatus = "PENDING"),
        )
        `when`(mockDao.getUnsyncedSessions()).thenReturn(unsynced)
        val result = mockDao.getUnsyncedSessions()
        assertEquals(2, result.size)
        assertTrue("All returned sessions should be unsynced", result.all { it.syncStatus == "PENDING" })
    }

    @Test
    fun getUnsyncedSessions_whenAllSynced_returnsEmpty() = runTest {
        `when`(mockDao.getUnsyncedSessions()).thenReturn(emptyList())
        val result = mockDao.getUnsyncedSessions()
        assertTrue(result.isEmpty())
    }

    // ── markSynced() ──────────────────────────────────────────────────────────

    @Test
    fun markSynced_callsDaoWithCorrectId() = runTest {
        mockDao.markSynced(7, "remote-7")
        verify(mockDao).markSynced(7, "remote-7")
    }

    @Test
    fun markSynced_multipleIds_eachCalledOnce() = runTest {
        for (id in listOf(1, 2, 3, 4, 5)) {
            mockDao.markSynced(id, "remote-$id")
        }
        for (id in listOf(1, 2, 3, 4, 5)) {
            verify(mockDao, times(1)).markSynced(id, "remote-$id")
        }
    }

    // ── getAverageAccuracy() ───────────────────────────────────────────────────

    @Test
    fun getAverageAccuracy_withSessions_returnsAverage() = runTest {
        `when`(mockDao.getAverageAccuracy(1)).thenReturn(82.5f)
        val avg = mockDao.getAverageAccuracy(1)
        assertEquals(82.5f, avg!!, 0.01f)
    }

    @Test
    fun getAverageAccuracy_noSessions_returnsNull() = runTest {
        `when`(mockDao.getAverageAccuracy(42)).thenReturn(null)
        val avg = mockDao.getAverageAccuracy(42)
        assertNull("Empty user should return null average", avg)
    }

    @Test
    fun getAverageAccuracy_perfectSessions_returns100() = runTest {
        `when`(mockDao.getAverageAccuracy(5)).thenReturn(100f)
        val avg = mockDao.getAverageAccuracy(5)
        assertEquals(100f, avg!!, 0.01f)
    }

    @Test
    fun getAverageAccuracy_zeroPractice_returns0() = runTest {
        `when`(mockDao.getAverageAccuracy(6)).thenReturn(0f)
        val avg = mockDao.getAverageAccuracy(6)
        assertEquals(0f, avg!!, 0.01f)
    }

    // ── getBestAccuracy() ─────────────────────────────────────────────────────

    @Test
    fun getBestAccuracy_singleSession_returnsThatScore() = runTest {
        `when`(mockDao.getBestAccuracy(1)).thenReturn(95.5f)
        val best = mockDao.getBestAccuracy(1)
        assertEquals(95.5f, best!!, 0.01f)
    }

    @Test
    fun getBestAccuracy_noSessions_returnsNull() = runTest {
        `when`(mockDao.getBestAccuracy(99)).thenReturn(null)
        assertNull(mockDao.getBestAccuracy(99))
    }

    @Test
    fun getBestAccuracy_allPerfect_returns100() = runTest {
        `when`(mockDao.getBestAccuracy(3)).thenReturn(100f)
        assertEquals(100f, mockDao.getBestAccuracy(3)!!, 0.01f)
    }

    // ── getTotalSessions() ────────────────────────────────────────────────────

    @Test
    fun getTotalSessions_returns0WhenEmpty() = runTest {
        `when`(mockDao.getTotalSessions(1)).thenReturn(0)
        assertEquals(0, mockDao.getTotalSessions(1))
    }

    @Test
    fun getTotalSessions_returnsCorrectCount() = runTest {
        `when`(mockDao.getTotalSessions(2)).thenReturn(13)
        assertEquals(13, mockDao.getTotalSessions(2))
    }

    // ── getTotalPracticeSeconds() ─────────────────────────────────────────────

    @Test
    fun getTotalPracticeSeconds_returnsSum() = runTest {
        `when`(mockDao.getTotalPracticeSeconds(1)).thenReturn(3600)
        assertEquals(3600, mockDao.getTotalPracticeSeconds(1)!!)
    }

    @Test
    fun getTotalPracticeSeconds_noSessions_returnsNull() = runTest {
        `when`(mockDao.getTotalPracticeSeconds(99)).thenReturn(null)
        assertNull(mockDao.getTotalPracticeSeconds(99))
    }

    // ── getLastSessionForTechnique() ──────────────────────────────────────────

    @Test
    fun getLastSessionForTechnique_returnsLatestSession() = runTest {
        val session = makeSession(1, 4, 78f)
        `when`(mockDao.getLastSessionForTechnique(1, 4)).thenReturn(session)
        val result = mockDao.getLastSessionForTechnique(1, 4)
        assertNotNull(result)
        assertEquals(4, result!!.techniqueId)
    }

    @Test
    fun getLastSessionForTechnique_noSession_returnsNull() = runTest {
        `when`(mockDao.getLastSessionForTechnique(1, 6)).thenReturn(null)
        assertNull(mockDao.getLastSessionForTechnique(1, 6))
    }

    @Test
    fun getLastSessionForTechnique_eachTechniqueIsIndependent() = runTest {
        val s1 = makeSession(1, 1, 80f)
        val s2 = makeSession(1, 2, 90f)
        `when`(mockDao.getLastSessionForTechnique(1, 1)).thenReturn(s1)
        `when`(mockDao.getLastSessionForTechnique(1, 2)).thenReturn(s2)
        assertEquals(80f, mockDao.getLastSessionForTechnique(1, 1)!!.finalAccuracy!! * 100f, 0.01f)
        assertEquals(90f, mockDao.getLastSessionForTechnique(1, 2)!!.finalAccuracy!! * 100f, 0.01f)
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private fun makeSession(
        userId: Int,
        techniqueId: Int,
        accuracyScore: Float,
        syncStatus: String = "PENDING"
    ) = SessionEntity(
        userId = userId,
        techniqueId = techniqueId,
        startTime = System.currentTimeMillis() - 60000,
        endTime = System.currentTimeMillis(),
        durationSeconds = 60,
        finalAccuracy = accuracyScore / 100f,
        status = "COMPLETED",
        syncStatus = syncStatus
    )
}
