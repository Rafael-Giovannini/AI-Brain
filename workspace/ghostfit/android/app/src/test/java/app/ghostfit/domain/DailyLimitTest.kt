package app.ghostfit.domain

import android.graphics.Bitmap
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.local.ReferencePhotoDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.ReferencePhoto
import app.ghostfit.data.model.UserProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * Comprehensive tests for the daily try-on limit logic in TryOnUseCase.
 *
 * Covers:
 * - Free users: enforcement at MAX_FREE_DAILY_TRIES (3)
 * - Premium users: unlimited tries
 * - Midnight reset: counter resets when date changes
 * - Counter increment: successful generation increments dailyTriesUsed
 * - Regenerate: also enforces and increments the daily limit
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DailyLimitTest {

    private lateinit var screenCapture: ScreenCaptureProvider
    private lateinit var garmentDetector: GarmentDetector
    private lateinit var modelRouter: ModelRouter
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var referencePhotoDao: ReferencePhotoDao
    private lateinit var photoStorage: PhotoStorage
    private lateinit var useCase: TryOnUseCase

    private val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    private val today = LocalDate.now().toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    private val freeProfile = UserProfile(
        id = "user-1",
        lgpdConsentGranted = true,
        onboardingCompleted = true,
        planType = PlanType.FREE,
        dailyTriesUsed = 0,
        dailyTriesResetDate = today
    )

    private val premiumProfile = freeProfile.copy(planType = PlanType.PREMIUM)

    private val testGarment = GarmentInfo(
        category = GarmentCategory.DRESS,
        confidence = 0.9f,
        croppedImage = testBitmap
    )

    private val testRefPhoto = ReferencePhoto(
        id = "photo-1",
        userId = "user-1",
        encryptedFilePath = "/enc/photo.enc"
    )

    @Before
    fun setup() {
        screenCapture = mock()
        garmentDetector = mock()
        modelRouter = mock()
        userProfileDao = mock()
        referencePhotoDao = mock()
        photoStorage = mock()

        useCase = TryOnUseCase(
            screenCapture = screenCapture,
            garmentDetector = garmentDetector,
            modelRouter = modelRouter,
            userProfileDao = userProfileDao,
            referencePhotoDao = referencePhotoDao,
            photoStorage = photoStorage
        )
    }

    private suspend fun setupSuccessfulPipeline() {
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(testGarment)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(testRefPhoto))
        whenever(photoStorage.decrypt("/enc/photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = testBitmap, modelUsed = "fashn")
        )
    }

    // --- Free user limit enforcement ---

    @Test
    fun `free user with 0 tries today can generate`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(freeProfile)
        setupSuccessfulPipeline()

        val result = useCase.execute()

        assertEquals(TryOnStatus.DONE, result.status)
    }

    @Test
    fun `free user with 2 tries today can still generate`() = runTest {
        val profile = freeProfile.copy(dailyTriesUsed = 2)
        whenever(userProfileDao.getProfile()).thenReturn(profile)
        setupSuccessfulPipeline()

        val result = useCase.execute()

        assertEquals(TryOnStatus.DONE, result.status)
    }

    @Test
    fun `free user with 3 tries today is blocked`() = runTest {
        val profile = freeProfile.copy(dailyTriesUsed = 3)
        whenever(userProfileDao.getProfile()).thenReturn(profile)

        val result = useCase.execute()

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage!!.contains("Limite"))
        assertTrue(result.errorMessage!!.contains("3/3"))
    }

    @Test
    fun `free user with more than 3 tries is also blocked`() = runTest {
        val profile = freeProfile.copy(dailyTriesUsed = 5)
        whenever(userProfileDao.getProfile()).thenReturn(profile)

        val result = useCase.execute()

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage!!.contains("Limite"))
    }

    // --- Premium user bypass ---

    @Test
    fun `premium user is never blocked regardless of tries`() = runTest {
        val profile = premiumProfile.copy(dailyTriesUsed = 100)
        whenever(userProfileDao.getProfile()).thenReturn(profile)
        setupSuccessfulPipeline()

        val result = useCase.execute()

        assertEquals(TryOnStatus.DONE, result.status)
    }

    @Test
    fun `premium user does not increment daily counter`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(premiumProfile)
        setupSuccessfulPipeline()

        useCase.execute()

        verify(userProfileDao, never()).updateDailyTries(any(), any())
    }

    // --- Midnight reset ---

    @Test
    fun `free user counter resets when date changes to today`() = runTest {
        val yesterdayProfile = freeProfile.copy(
            dailyTriesUsed = 3,
            dailyTriesResetDate = yesterday
        )
        whenever(userProfileDao.getProfile()).thenReturn(yesterdayProfile)
        setupSuccessfulPipeline()

        val result = useCase.execute()

        // Should reset to 0 first, then allow the generation
        assertEquals(TryOnStatus.DONE, result.status)
        // Verify the reset was called with 0 and today's date
        verify(userProfileDao).updateDailyTries(eq(0), eq(today))
    }

    @Test
    fun `free user with null reset date gets reset`() = runTest {
        val nullDateProfile = freeProfile.copy(
            dailyTriesUsed = 3,
            dailyTriesResetDate = null
        )
        whenever(userProfileDao.getProfile()).thenReturn(nullDateProfile)
        setupSuccessfulPipeline()

        val result = useCase.execute()

        // null != today, so counter resets
        assertEquals(TryOnStatus.DONE, result.status)
        verify(userProfileDao).updateDailyTries(eq(0), eq(today))
    }

    // --- Counter increment ---

    @Test
    fun `successful generation increments daily counter for free user`() = runTest {
        val profile = freeProfile.copy(dailyTriesUsed = 1)
        whenever(userProfileDao.getProfile()).thenReturn(profile)
        setupSuccessfulPipeline()

        useCase.execute()

        // Should increment from 1 to 2
        verify(userProfileDao).updateDailyTries(eq(2), eq(today))
    }

    @Test
    fun `failed detection does not increment counter`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(freeProfile)
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(null)

        useCase.execute()

        // NO_GARMENT should not consume a try
        verify(userProfileDao, never()).updateDailyTries(any(), any())
    }

    // --- Regenerate also enforces limits ---

    @Test
    fun `regenerate is blocked when daily limit reached`() = runTest {
        val limitedProfile = freeProfile.copy(dailyTriesUsed = 3)
        whenever(userProfileDao.getProfile()).thenReturn(limitedProfile)

        val previousSession = TryOnSession(
            detectedGarment = testGarment,
            referencePhotoId = "photo-1",
            status = TryOnStatus.DONE
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage!!.contains("Limite"))
    }

    @Test
    fun `regenerate succeeds and increments counter when under limit`() = runTest {
        val profile = freeProfile.copy(dailyTriesUsed = 1)
        whenever(userProfileDao.getProfile()).thenReturn(profile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(testRefPhoto))
        whenever(photoStorage.decrypt("/enc/photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = testBitmap, modelUsed = "fashn")
        )

        val previousSession = TryOnSession(
            detectedGarment = testGarment,
            referencePhotoId = "photo-1",
            status = TryOnStatus.DONE
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.DONE, result.status)
        verify(userProfileDao).updateDailyTries(eq(2), eq(today))
    }

    @Test
    fun `regenerate with premium user is always allowed`() = runTest {
        val profile = premiumProfile.copy(dailyTriesUsed = 100)
        whenever(userProfileDao.getProfile()).thenReturn(profile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(testRefPhoto))
        whenever(photoStorage.decrypt("/enc/photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = testBitmap, modelUsed = "fashn")
        )

        val previousSession = TryOnSession(
            detectedGarment = testGarment,
            referencePhotoId = "photo-1",
            status = TryOnStatus.DONE
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.DONE, result.status)
    }

    // --- Edge cases ---

    @Test
    fun `no profile returns gracefully without blocking`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(null)
        setupSuccessfulPipeline()

        // checkDailyLimit returns early if no profile, allowing the pipeline
        // But getProfile will be called again for referencePhotoDao lookup,
        // which will cause an error when trying to get user ID
        val result = useCase.execute()

        // With null profile, the user ID will be "" and referencePhotoDao returns empty
        // leading to "Nenhuma foto de referência encontrada" error
        assertEquals(TryOnStatus.ERROR, result.status)
    }

    @Test
    fun `MAX_FREE_DAILY_TRIES constant is 3`() {
        assertEquals(3, TryOnUseCase.MAX_FREE_DAILY_TRIES)
    }
}
