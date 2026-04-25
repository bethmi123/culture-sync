package com.culturesync.app

import com.culturesync.app.data.local.AppDatabase
import com.culturesync.app.data.local.daos.SessionDao
import com.culturesync.app.data.local.entities.SessionEntity
import com.culturesync.app.data.repository.SessionRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * SessionRepositoryTest — Data Layer Tests
 * Tests that SessionRepository correctly delegates all operations to Room DAO.
 * Uses Mockito to mock AppDatabase and SessionDao (Android runtime components).
 */
class SessionRepositoryTest {

    @Mock lateinit var mockDb: AppDatabase
    @Mock lateinit var mockDao: SessionDao

    private lateinit var repository: SessionRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockDb.sessionDao()).thenReturn(mockDao)
        repository = SessionRepository(mockDb)
    }

    // ── createSession() ────────────────────────────────────────────────────────

    @Test
    fun saveSession_delegatesToDaoInsert() = runTest {
        val session = makeSession(accuracyScore = 85f)
        repository.createSession(session)
        verify(mockDao).insertSession(session)
    }

    @Test
    fun saveSession_returnsInsertedId() = runTest {
        val session = makeSession()
        `when`(mockDao.insertSession(session)).thenReturn(99L)
        val id = repository.createSession(session)
        assertEquals(99L, id)
    }

    @Test
    fun saveSession_perfectScore_delegatesCorrectly() = runTest {
        val session = makeSession(accuracyScore = 100f)
        repository.createSession(session)
        verify(mockDao).insertSession(session)
    }

    @Test
    fun saveSession_zeroScore_delegatesCorrectly() = runTest {
        val session = makeSession(accuracyScore = 0f)
        repository.createSession(session)
        verify(mockDao).insertSession(session)
    }

    // ── getSessionsByUser() ───────────────────────────────────────────────────

    @Test
    fun getSessionsByUser_delegatesToDao() = runTest {
        `when`(mockDao.getSessionsByUser(1)).thenReturn(flowOf(emptyList()))
        repository.getSessionsByUser(1)
        verify(mockDao).getSessionsByUser(1)
    }

    @Test
    fun getSessionsByUser_returnsFlowFromDao() = runTest {
        val sessions = listOf(makeSession(userId = 5, accuracyScore = 80f))
        `when`(mockDao.getSessionsByUser(5)).thenReturn(flowOf(sessions))
        val flow = repository.getSessionsByUser(5)
        assertNotNull(flow)
    }

    // ── getAverageAccuracy() ──────────────────────────────────────────────────

    @Test
    fun getAverageAccuracy_returnsValueFromDao() = runTest {
        `when`(mockDao.getAverageAccuracy(1)).thenReturn(77.5f)
        val avg = repository.getAverageAccuracy(1)
        assertEquals(77.5f, avg, 0.01f)
    }

    @Test
    fun getAverageAccuracy_whenDaoReturnsNull_returnsZero() = runTest {
        `when`(mockDao.getAverageAccuracy(999)).thenReturn(null)
        val avg = repository.getAverageAccuracy(999)
        assertEquals(0f, avg, 0.01f)
    }

    @Test
    fun getAverageAccuracy_forDifferentUsers_callsDaoWithCorrectId() = runTest {
        `when`(mockDao.getAverageAccuracy(10)).thenReturn(90f)
        `when`(mockDao.getAverageAccuracy(20)).thenReturn(60f)
        assertEquals(90f, repository.getAverageAccuracy(10), 0.01f)
        assertEquals(60f, repository.getAverageAccuracy(20), 0.01f)
    }

    // ── getBestAccuracy() ─────────────────────────────────────────────────────

    @Test
    fun getBestAccuracy_returnsMaxFromDao() = runTest {
        `when`(mockDao.getBestAccuracy(1)).thenReturn(99.5f)
        val best = repository.getBestAccuracy(1)
        assertEquals(99.5f, best, 0.01f)
    }

    @Test
    fun getBestAccuracy_whenNull_returnsZero() = runTest {
        `when`(mockDao.getBestAccuracy(42)).thenReturn(null)
        val best = repository.getBestAccuracy(42)
        assertEquals(0f, best, 0.01f)
    }

    // ── getTotalSessions() ────────────────────────────────────────────────────

    @Test
    fun getTotalSessions_returnsCountFromDao() = runTest {
        `when`(mockDao.getTotalSessions(1)).thenReturn(7)
        val count = repository.getTotalSessions(1)
        assertEquals(7, count)
    }

    @Test
    fun getTotalSessions_returnsZeroWhenNone() = runTest {
        `when`(mockDao.getTotalSessions(99)).thenReturn(0)
        val count = repository.getTotalSessions(99)
        assertEquals(0, count)
    }

    // ── getTotalPracticeSeconds() ─────────────────────────────────────────────

    @Test
    fun getTotalPracticeSeconds_returnsSumFromDao() = runTest {
        `when`(mockDao.getTotalPracticeSeconds(1)).thenReturn(3600)
        val secs = repository.getTotalPracticeSeconds(1)
        assertEquals(3600, secs)
    }

    @Test
    fun getTotalPracticeSeconds_whenNull_returnsZero() = runTest {
        `when`(mockDao.getTotalPracticeSeconds(1)).thenReturn(null)
        val secs = repository.getTotalPracticeSeconds(1)
        assertEquals(0, secs)
    }

    // ── getRecentSessions() ───────────────────────────────────────────────────

    @Test
    fun getRecentSessions_defaultLimit10_delegatesCorrectly() = runTest {
        `when`(mockDao.getRecentSessions(1, 10)).thenReturn(emptyList())
        repository.getRecentSessions(1)
        verify(mockDao).getRecentSessions(1, 10)
    }

    @Test
    fun getRecentSessions_customLimit_delegatesCorrectly() = runTest {
        `when`(mockDao.getRecentSessions(2, 5)).thenReturn(emptyList())
        repository.getRecentSessions(2, 5)
        verify(mockDao).getRecentSessions(2, 5)
    }

    @Test
    fun getRecentSessions_returnsListFromDao() = runTest {
        val list = listOf(makeSession(userId = 1, accuracyScore = 88f))
        `when`(mockDao.getRecentSessions(1, 10)).thenReturn(list)
        val result = repository.getRecentSessions(1)
        assertEquals(1, result.size)
        assertEquals(88f, result[0].finalAccuracy!! * 100f, 0.01f)
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private fun makeSession(userId: Int = 1, accuracyScore: Float = 80f) = SessionEntity(
        userId = userId,
        techniqueId = 1,
        startTime = System.currentTimeMillis() - 60000,
        endTime = System.currentTimeMillis(),
        durationSeconds = 60,
        finalAccuracy = accuracyScore / 100f,
        status = "COMPLETED"
    )
}
