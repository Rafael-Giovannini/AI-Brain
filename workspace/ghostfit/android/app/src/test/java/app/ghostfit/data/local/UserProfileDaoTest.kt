package app.ghostfit.data.local

import androidx.room.Room
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Unit tests for UserProfileDao — FR-003 (LGPD consent), onboarding state,
 * overlay position, daily tries tracking.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserProfileDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.userProfileDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `getProfile returns null when empty`() = runTest {
        val profile = dao.getProfile()
        assertNull(profile)
    }

    @Test
    fun `insertProfile and getProfile round-trip`() = runTest {
        val profile = UserProfile(
            id = "user-1",
            lgpdConsentGranted = true,
            lgpdConsentTimestamp = 1000L
        )
        dao.insertProfile(profile)

        val result = dao.getProfile()
        assertNotNull(result)
        assertEquals("user-1", result!!.id)
        assertTrue(result.lgpdConsentGranted)
        assertEquals(1000L, result.lgpdConsentTimestamp)
    }

    @Test
    fun `insertProfile with REPLACE overwrites existing`() = runTest {
        dao.insertProfile(UserProfile(id = "user-1", lgpdConsentGranted = false))
        dao.insertProfile(UserProfile(id = "user-1", lgpdConsentGranted = true, lgpdConsentTimestamp = 2000L))

        val result = dao.getProfile()
        assertNotNull(result)
        assertTrue(result!!.lgpdConsentGranted)
        assertEquals(2000L, result.lgpdConsentTimestamp)
    }

    @Test
    fun `updateProfile persists changes`() = runTest {
        val profile = UserProfile(id = "user-1")
        dao.insertProfile(profile)

        dao.updateProfile(profile.copy(onboardingCompleted = true, planType = PlanType.PREMIUM))

        val result = dao.getProfile()
        assertTrue(result!!.onboardingCompleted)
        assertEquals(PlanType.PREMIUM, result.planType)
    }

    @Test
    fun `observeProfile emits updates`() = runTest {
        dao.insertProfile(UserProfile(id = "user-1", lgpdConsentGranted = false))

        val first = dao.observeProfile().first()
        assertNotNull(first)
        assertFalse(first!!.lgpdConsentGranted)
    }

    @Test
    fun `updateOverlayPosition persists coordinates`() = runTest {
        dao.insertProfile(UserProfile(id = "user-1"))

        dao.updateOverlayPosition(150.5f, 300.75f)

        val result = dao.getProfile()
        assertEquals(150.5f, result!!.overlayPositionX, 0.01f)
        assertEquals(300.75f, result.overlayPositionY, 0.01f)
    }

    @Test
    fun `updateDailyTries persists count and reset date`() = runTest {
        dao.insertProfile(UserProfile(id = "user-1"))

        dao.updateDailyTries(2, "2026-03-04")

        val result = dao.getProfile()
        assertEquals(2, result!!.dailyTriesUsed)
        assertEquals("2026-03-04", result.dailyTriesResetDate)
    }

    @Test
    fun `deleteAll clears all profiles`() = runTest {
        dao.insertProfile(UserProfile(id = "user-1"))

        dao.deleteAll()

        val result = dao.getProfile()
        assertNull(result)
    }

    @Test
    fun `default values are correct`() = runTest {
        dao.insertProfile(UserProfile(id = "user-1"))

        val result = dao.getProfile()!!
        assertFalse(result.lgpdConsentGranted)
        assertNull(result.lgpdConsentTimestamp)
        assertFalse(result.onboardingCompleted)
        assertEquals(PlanType.FREE, result.planType)
        assertEquals(0, result.dailyTriesUsed)
        assertNull(result.dailyTriesResetDate)
        assertEquals(0f, result.overlayPositionX, 0.01f)
        assertEquals(0f, result.overlayPositionY, 0.01f)
    }

    @Test
    fun `lgpd consent flow - grant and revoke`() = runTest {
        // Start without consent
        dao.insertProfile(UserProfile(id = "user-1"))
        assertFalse(dao.getProfile()!!.lgpdConsentGranted)

        // Grant consent (FR-003)
        val profile = dao.getProfile()!!
        dao.updateProfile(profile.copy(lgpdConsentGranted = true, lgpdConsentTimestamp = 5000L))
        val granted = dao.getProfile()!!
        assertTrue(granted.lgpdConsentGranted)
        assertEquals(5000L, granted.lgpdConsentTimestamp)

        // Revoke consent — deleteAll simulates data erasure
        dao.deleteAll()
        assertNull(dao.getProfile())
    }
}
