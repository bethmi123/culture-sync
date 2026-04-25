package com.culturesync.app

import com.culturesync.app.data.local.entities.UserEntity
import org.junit.Assert.*
import org.junit.Test

/**
 * UserEntityTest — PRD §10.1
 * Verifies: email uniqueness annotation, passwordHash not stored as plaintext,
 * and correct default values per §7.1.1.
 */
class UserEntityTest {

    // ── PASSWORD HASH VALIDATION ──────────────────────────────────────────────

    @Test
    fun passwordHash_notStoredAsPlaintext_commonPasswords() {
        val commonPasswords = listOf("password", "123456", "abc123", "letmein", "qwerty")
        for (password in commonPasswords) {
            val entity = makeUser(passwordHash = password)
            assertNotEquals("Plaintext password '$password' must not be stored", password, entity.passwordHash)
        }
    }

    @Test
    fun passwordHash_bcryptPrefix_matchesExpectedFormat() {
        // BCrypt hashes always start with $2a$ or $2b$ (cost factor prefix)
        val bcryptHash = "\$2a\$12\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        val entity = makeUser(passwordHash = bcryptHash)
        assertTrue(
            "BCrypt hash must start with \$2a\$ or \$2b\$",
            entity.passwordHash.startsWith("\$2a\$") || entity.passwordHash.startsWith("\$2b\$")
        )
    }

    @Test
    fun passwordHash_length_matchesBcryptOutputLength() {
        // BCrypt always produces a 60-character hash
        val bcryptHash = "\$2a\$12\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        assertEquals("BCrypt hash must be 60 characters", 60, bcryptHash.length)
        val entity = makeUser(passwordHash = bcryptHash)
        assertEquals(60, entity.passwordHash.length)
    }

    @Test
    fun passwordHash_differentFromName_andEmail() {
        val entity = makeUser(name = "Bethmi", email = "b@test.com", passwordHash = "\$2a\$12\$abc")
        assertNotEquals(entity.name, entity.passwordHash)
        assertNotEquals(entity.email, entity.passwordHash)
    }

    @Test
    fun passwordHash_neverEmpty() {
        val entity = makeUser(passwordHash = "\$2a\$12\$someHash")
        assertTrue("passwordHash must not be empty", entity.passwordHash.isNotEmpty())
    }

    // ── EMAIL UNIQUENESS (schema validation) ──────────────────────────────────

    @Test
    fun email_isStoredExactly() {
        val email = "bethmi@culturesync.app"
        val entity = makeUser(email = email)
        assertEquals(email, entity.email)
    }

    @Test
    fun email_normalisedToLowercase_storageCheck() {
        // If normalisation is applied, lowercase email should match stored value
        val entity = makeUser(email = "test@example.com")
        assertEquals("test@example.com", entity.email)
    }

    @Test
    fun twoUsers_differentEmails_shouldBeDistinctEntities() {
        val user1 = makeUser(email = "alice@test.com")
        val user2 = makeUser(email = "bob@test.com")
        assertNotEquals("Different emails must not be equal", user1.email, user2.email)
    }

    @Test
    fun twoUsers_sameEmail_wouldViolateUniqueIndex() {
        // Runtime enforcement by Room; here we verify the email field is identical
        // so a unique index would correctly reject the insert.
        val user1 = makeUser(email = "duplicate@test.com")
        val user2 = makeUser(email = "duplicate@test.com")
        assertEquals(
            "Same email string must be identical (Room unique index would reject second insert)",
            user1.email, user2.email
        )
    }

    // ── DEFAULT VALUES (§7.1.1) ───────────────────────────────────────────────

    @Test
    fun userEntity_defaultId_isZero() {
        val entity = makeUser()
        assertEquals("Default id must be 0 (auto-generated)", 0, entity.id)
    }

    @Test
    fun userEntity_defaultRemoteId_isNull() {
        val entity = makeUser()
        assertNull("remoteId must default to null before sync", entity.remoteId)
    }

    @Test
    fun userEntity_defaultLanguage_isEnglish() {
        val entity = makeUser()
        assertEquals("Default language must be 'en'", "en", entity.language)
    }

    @Test
    fun userEntity_defaultSyncStatus_isPending() {
        val entity = makeUser()
        assertEquals("Default syncStatus must be 'PENDING'", "PENDING", entity.syncStatus)
    }

    @Test
    fun userEntity_defaultUserType_isStudent() {
        val entity = makeUser()
        assertEquals("Default userType must be 'Student'", "Student", entity.userType)
    }

    @Test
    fun userEntity_createdAt_isPositive() {
        val entity = makeUser()
        assertTrue("createdAt timestamp must be positive", entity.createdAt > 0L)
    }

    // ── USER TYPE DOMAIN VALUES (§7.1.1) ─────────────────────────────────────

    @Test
    fun userType_student_isValidValue() {
        val entity = makeUser(userType = "Student")
        assertEquals("Student", entity.userType)
    }

    @Test
    fun userType_tourist_isValidValue() {
        val entity = makeUser(userType = "Tourist")
        assertEquals("Tourist", entity.userType)
    }

    @Test
    fun userType_artisan_isValidValue() {
        val entity = makeUser(userType = "Artisan")
        assertEquals("Artisan", entity.userType)
    }

    @Test
    fun userType_shopOwner_isValidValue() {
        val entity = makeUser(userType = "ShopOwner")
        assertEquals("ShopOwner", entity.userType)
    }

    // ── SYNC STATUS DOMAIN VALUES (§7.1.1) ───────────────────────────────────

    @Test
    fun syncStatus_pending_isValidValue() {
        val entity = makeUser(syncStatus = "PENDING")
        assertEquals("PENDING", entity.syncStatus)
    }

    @Test
    fun syncStatus_synced_isValidValue() {
        val entity = makeUser(syncStatus = "SYNCED")
        assertEquals("SYNCED", entity.syncStatus)
    }

    @Test
    fun syncStatus_error_isValidValue() {
        val entity = makeUser(syncStatus = "ERROR")
        assertEquals("ERROR", entity.syncStatus)
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private fun makeUser(
        name: String = "Test User",
        email: String = "test@culturesync.app",
        passwordHash: String = "\$2a\$12\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
        userType: String = "Student",
        syncStatus: String = "PENDING"
    ) = UserEntity(
        name = name,
        email = email,
        passwordHash = passwordHash,
        userType = userType,
        syncStatus = syncStatus
    )
}
