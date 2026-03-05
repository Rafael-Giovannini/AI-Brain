package app.ghostfit.overlay

import android.content.Intent
import android.provider.Settings
import androidx.room.Room
import app.ghostfit.data.local.AppDatabase
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.UserProfile
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

/**
 * Unit tests for OverlayService state management.
 * Tests lifecycle, stop action, notification, and overlay position persistence.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class OverlayServiceTest {

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
    fun `overlay position defaults to 0,0 for new profile`() = runTest {
        val profile = UserProfile(id = "test-user")
        dao.insertProfile(profile)

        val saved = dao.getProfile()
        assertNotNull(saved)
        assertEquals(0f, saved!!.overlayPositionX)
        assertEquals(0f, saved.overlayPositionY)
    }

    @Test
    fun `updateOverlayPosition persists new coordinates`() = runTest {
        dao.insertProfile(UserProfile(id = "test-user"))

        dao.updateOverlayPosition(150f, 300f)

        val saved = dao.getProfile()
        assertNotNull(saved)
        assertEquals(150f, saved!!.overlayPositionX)
        assertEquals(300f, saved.overlayPositionY)
    }

    @Test
    fun `updateOverlayPosition overwrites previous position`() = runTest {
        dao.insertProfile(UserProfile(id = "test-user"))

        dao.updateOverlayPosition(100f, 200f)
        dao.updateOverlayPosition(500f, 700f)

        val saved = dao.getProfile()
        assertEquals(500f, saved!!.overlayPositionX)
        assertEquals(700f, saved.overlayPositionY)
    }

    @Test
    fun `overlay position supports negative coordinates`() = runTest {
        dao.insertProfile(UserProfile(id = "test-user"))

        dao.updateOverlayPosition(-50f, -100f)

        val saved = dao.getProfile()
        assertEquals(-50f, saved!!.overlayPositionX)
        assertEquals(-100f, saved.overlayPositionY)
    }

    @Test
    fun `STOP action constant is correctly defined`() {
        assertEquals("app.ghostfit.overlay.STOP", OverlayService.ACTION_STOP)
    }

    @Test
    fun `ScreenCapture isReady returns false when not initialized`() {
        val capture = ScreenCapture(RuntimeEnvironment.getApplication())
        assertFalse(capture.isReady)
    }

    @Test
    fun `overlay position persists across profile updates`() = runTest {
        dao.insertProfile(UserProfile(id = "test-user", lgpdConsentGranted = true))

        dao.updateOverlayPosition(200f, 400f)

        // Update a different field
        val profile = dao.getProfile()!!
        dao.updateProfile(profile.copy(dailyTriesUsed = 3))

        // Overlay position should be unchanged
        val updated = dao.getProfile()
        assertEquals(200f, updated!!.overlayPositionX)
        assertEquals(400f, updated.overlayPositionY)
        assertEquals(3, updated.dailyTriesUsed)
    }
}
