package com.culturesync.app

import android.content.Context
import android.content.SharedPreferences
import com.culturesync.app.utils.SessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * SessionManagerTest — SharedPreferences-backed Session State Tests
 * Uses Mockito to mock Android SharedPreferences (not available in JVM tests).
 * Covers: save, retrieve, logout, role check, API URL, first launch.
 */
class SessionManagerTest {

    @Mock lateinit var mockContext: Context
    @Mock lateinit var mockPrefs: SharedPreferences
    @Mock lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor)
        `when`(mockEditor.remove(anyString())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)
        sessionManager = SessionManager(mockContext)
    }

    // ── saveUser() ────────────────────────────────────────────────────────────

    @Test
    fun saveUser_shouldWriteUserId() {
        sessionManager.saveUser(101, "Bethmi", "bethmi@test.com", "learner", "jwt.token.here")
        verify(mockEditor).putInt("user_id", 101)
    }

    @Test
    fun saveUser_shouldWriteUserName() {
        sessionManager.saveUser(1, "Nimali Perera", "n@test.com", "learner", "token")
        verify(mockEditor).putString("user_name", "Nimali Perera")
    }

    @Test
    fun saveUser_shouldWriteUserEmail() {
        sessionManager.saveUser(1, "Test", "test@culturesync.app", "learner", "token")
        verify(mockEditor).putString("user_email", "test@culturesync.app")
    }

    @Test
    fun saveUser_shouldWriteUserRole() {
        sessionManager.saveUser(1, "Admin", "admin@test.com", "admin", "token")
        verify(mockEditor).putString("user_type", "admin")
    }

    @Test
    fun saveUser_shouldWriteAuthToken() {
        sessionManager.saveUser(1, "T", "t@t.com", "learner", "my.jwt.token")
        verify(mockEditor).putString("auth_token", "my.jwt.token")
    }

    @Test
    fun saveUser_shouldCallApply() {
        sessionManager.saveUser(42, "A", "a@test.com", "learner", "token")
        verify(mockEditor, atLeastOnce()).apply()
    }

    @Test
    fun saveUser_withEmptyToken_shouldStillSave() {
        sessionManager.saveUser(1, "N", "n@test.com", "learner", "")
        verify(mockEditor).putString("auth_token", "")
    }

    // ── getters ───────────────────────────────────────────────────────────────

    @Test
    fun getUserId_shouldReturnStoredId() {
        `when`(mockPrefs.getInt("user_id", -1)).thenReturn(42)
        assertEquals(42, sessionManager.getUserId())
    }

    @Test
    fun getUserId_default_shouldReturnMinusOne() {
        `when`(mockPrefs.getInt("user_id", -1)).thenReturn(-1)
        assertEquals(-1, sessionManager.getUserId())
    }

    @Test
    fun getUserName_shouldReturnStoredName() {
        `when`(mockPrefs.getString("user_name", "")).thenReturn("Bethmi Dias")
        assertEquals("Bethmi Dias", sessionManager.getUserName())
    }

    @Test
    fun getUserEmail_shouldReturnStoredEmail() {
        `when`(mockPrefs.getString("user_email", "")).thenReturn("bethmi@culturesync.app")
        assertEquals("bethmi@culturesync.app", sessionManager.getUserEmail())
    }

    @Test
    fun getUserRole_shouldReturnStoredRole() {
        `when`(mockPrefs.getString("user_type", "Student")).thenReturn("admin")
        assertEquals("admin", sessionManager.getUserType())
    }

    @Test
    fun getUserRole_default_shouldBeLearner() {
        `when`(mockPrefs.getString("user_type", "Student")).thenReturn("Student")
        assertEquals("Student", sessionManager.getUserType())
    }

    @Test
    fun getAuthToken_shouldReturnStoredToken() {
        `when`(mockPrefs.getString("auth_token", "")).thenReturn("valid.jwt.token")
        assertEquals("valid.jwt.token", sessionManager.getAuthToken())
    }

    @Test
    fun getAuthToken_default_shouldBeEmpty() {
        `when`(mockPrefs.getString("auth_token", "")).thenReturn("")
        assertEquals("", sessionManager.getAuthToken())
    }

    // ── isLoggedIn() ──────────────────────────────────────────────────────────

    @Test
    fun isLoggedIn_withStoredUserId_shouldReturnTrue() {
        `when`(mockPrefs.getInt("user_id", -1)).thenReturn(5)
        assertTrue(sessionManager.isLoggedIn())
    }

    @Test
    fun isLoggedIn_withDefaultMinusOne_shouldReturnFalse() {
        `when`(mockPrefs.getInt("user_id", -1)).thenReturn(-1)
        assertFalse(sessionManager.isLoggedIn())
    }

    // ── isAdmin() ─────────────────────────────────────────────────────────────

    @Test
    fun isAdmin_whenRoleIsAdmin_shouldReturnTrue() {
        `when`(mockPrefs.getString("user_type", "Student")).thenReturn("admin")
        assertTrue(sessionManager.isAdmin())
    }

    @Test
    fun isAdmin_whenRoleIsLearner_shouldReturnFalse() {
        `when`(mockPrefs.getString("user_type", "Student")).thenReturn("learner")
        assertFalse(sessionManager.isAdmin())
    }

    @Test
    fun isAdmin_whenRoleIsEmpty_shouldReturnFalse() {
        `when`(mockPrefs.getString("user_type", "Student")).thenReturn("")
        assertFalse(sessionManager.isAdmin())
    }

    // ── isFirstLaunch() / setFirstLaunchDone() ────────────────────────────────

    @Test
    fun isFirstLaunch_defaultTrue_shouldReturnTrue() {
        `when`(mockPrefs.getBoolean("first_launch", true)).thenReturn(true)
        assertTrue(sessionManager.isFirstLaunch())
    }

    @Test
    fun isFirstLaunch_afterSettingDone_shouldReturnFalse() {
        `when`(mockPrefs.getBoolean("first_launch", true)).thenReturn(false)
        assertFalse(sessionManager.isFirstLaunch())
    }

    @Test
    fun setFirstLaunchDone_shouldPersistFalse() {
        sessionManager.setFirstLaunchDone()
        verify(mockEditor).putBoolean("first_launch", false)
        verify(mockEditor).apply()
    }

    // ── logout() ──────────────────────────────────────────────────────────────

    @Test
    fun logout_shouldCallClear() {
        sessionManager.logout()
        verify(mockEditor).clear()
    }

    @Test
    fun logout_shouldCallApply() {
        sessionManager.logout()
        verify(mockEditor).apply()
    }

    @Test
    fun logout_shouldNotCallPutOnAnyKey() {
        sessionManager.logout()
        verify(mockEditor, never()).putString(anyString(), anyString())
        verify(mockEditor, never()).putInt(anyString(), anyInt())
    }

    // ── API URL ───────────────────────────────────────────────────────────────

    @Test
    fun saveApiUrl_shouldPersistUrl() {
        sessionManager.saveApiUrl("http://192.168.1.10:3000/")
        verify(mockEditor).putString("base_url", "http://192.168.1.10:3000/")
        verify(mockEditor).apply()
    }

    @Test
    fun getApiUrl_shouldReturnSavedUrl() {
        `when`(mockPrefs.getString(eq("base_url"), anyString())).thenReturn("http://192.168.1.10:3000/")
        assertEquals("http://192.168.1.10:3000/", sessionManager.getApiUrl())
    }
}
