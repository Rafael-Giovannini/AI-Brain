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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TryOnUseCaseTest {

    private lateinit var screenCapture: ScreenCaptureProvider
    private lateinit var garmentDetector: GarmentDetector
    private lateinit var modelRouter: ModelRouter
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var referencePhotoDao: ReferencePhotoDao
    private lateinit var photoStorage: PhotoStorage
    private lateinit var useCase: TryOnUseCase

    private val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    private val testProfile = UserProfile(
        id = "user-1",
        lgpdConsentGranted = true,
        onboardingCompleted = true,
        dailyTriesUsed = 0,
        dailyTriesResetDate = java.time.LocalDate.now().toString()
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

    @Test
    fun `execute returns NO_GARMENT when detector returns null`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(null)

        val result = useCase.execute()

        assertEquals(TryOnStatus.NO_GARMENT, result.status)
        assertEquals("Nenhuma roupa detectada. Tente em uma página de produto.", result.errorMessage)
    }

    @Test
    fun `execute returns DONE on successful pipeline`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.DRESS,
            confidence = 0.9f,
            croppedImage = testBitmap
        )
        val generatedBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val refPhoto = ReferencePhoto(
            id = "photo-1",
            userId = "user-1",
            encryptedFilePath = "/enc/photo.enc"
        )

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(garment)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(refPhoto))
        whenever(photoStorage.decrypt("/enc/photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = generatedBitmap, modelUsed = "fashn")
        )

        val result = useCase.execute()

        assertEquals(TryOnStatus.DONE, result.status)
        assertEquals("fashn", result.modelUsed)
        assertNotNull(result.generatedImage)
    }

    @Test
    fun `execute returns ERROR when daily limit exceeded`() = runTest {
        val limitedProfile = testProfile.copy(dailyTriesUsed = 3)
        whenever(userProfileDao.getProfile()).thenReturn(limitedProfile)

        val result = useCase.execute()

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage?.contains("Limite") == true)
    }

    @Test
    fun `execute returns ERROR when no reference photo found`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(garment)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(emptyList())

        val result = useCase.execute()

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage?.contains("referência") == true)
    }

    @Test
    fun `execute returns ERROR when generation fails`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )
        val refPhoto = ReferencePhoto(
            id = "photo-1",
            userId = "user-1",
            encryptedFilePath = "/enc/photo.enc"
        )

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(garment)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(refPhoto))
        whenever(photoStorage.decrypt("/enc/photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenAnswer {
            throw TryOnGenerationException("Both models failed")
        }

        val result = useCase.execute()

        assertEquals(TryOnStatus.ERROR, result.status)
    }

    // --- FR-011: Swap photo / regenerate tests ---

    @Test
    fun `regenerate with different referencePhotoId uses correct photo`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.DRESS,
            confidence = 0.85f,
            croppedImage = testBitmap
        )
        val previousSession = TryOnSession(
            detectedGarment = garment,
            referencePhotoId = "photo-old"
        )
        // New active reference photo (simulates user swapping photo)
        val newRefPhoto = ReferencePhoto(
            id = "photo-new",
            userId = "user-1",
            encryptedFilePath = "/enc/photo_new.enc"
        )

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(newRefPhoto))
        whenever(photoStorage.decrypt("/enc/photo_new.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = testBitmap, modelUsed = "fashn")
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.DONE, result.status)
    }

    @Test
    fun `regenerate without reference photo returns ERROR`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )
        val previousSession = TryOnSession(detectedGarment = garment)

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(emptyList())

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage?.contains("referência") == true)
    }

    @Test
    fun `regenerate counts as additional attempt for free user`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )
        val previousSession = TryOnSession(detectedGarment = garment)
        // User already at limit
        val limitedProfile = testProfile.copy(dailyTriesUsed = 3)

        whenever(userProfileDao.getProfile()).thenReturn(limitedProfile)

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage?.contains("Limite") == true)
    }

    @Test
    fun `execute tracks status changes through callback`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(screenCapture.capture()).thenReturn(testBitmap)
        whenever(garmentDetector.detect(any())).thenReturn(null)

        val statuses = mutableListOf<TryOnStatus>()
        useCase.execute { session -> statuses.add(session.status) }

        assertTrue(statuses.contains(TryOnStatus.CAPTURING))
        assertTrue(statuses.contains(TryOnStatus.DETECTING))
        assertTrue(statuses.contains(TryOnStatus.NO_GARMENT))
    }

    // ── FR-011: Swap photo / regenerate tests ──

    @Test
    fun `regenerate uses current active reference photo from DB`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )
        val previousSession = TryOnSession(
            detectedGarment = garment,
            referencePhotoId = "old-photo-1",
            status = TryOnStatus.DONE
        )
        // New reference photo in DB (simulating swap)
        val newRefPhoto = ReferencePhoto(
            id = "new-photo-2",
            userId = "user-1",
            encryptedFilePath = "/enc/new_photo.enc"
        )
        val generatedBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(newRefPhoto))
        whenever(photoStorage.decrypt("/enc/new_photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = generatedBitmap, modelUsed = "fashn")
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.DONE, result.status)
        assertNotNull(result.generatedImage)
        // Verifies that the new photo was decrypted (not old-photo-1)
        verify(photoStorage).decrypt("/enc/new_photo.enc")
    }

    @Test
    fun `regenerate returns ERROR when no reference photo exists`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )
        val previousSession = TryOnSession(
            detectedGarment = garment,
            status = TryOnStatus.DONE
        )

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(emptyList())

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.ERROR, result.status)
        assertTrue(result.errorMessage?.contains("referência") == true)
    }

    @Test
    fun `regenerate counts as additional daily attempt`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = testBitmap
        )
        val previousSession = TryOnSession(
            detectedGarment = garment,
            status = TryOnStatus.DONE
        )
        val refPhoto = ReferencePhoto(
            id = "photo-1",
            userId = "user-1",
            encryptedFilePath = "/enc/photo.enc"
        )
        val generatedBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(referencePhotoDao.getByUser("user-1")).thenReturn(listOf(refPhoto))
        whenever(photoStorage.decrypt("/enc/photo.enc")).thenReturn(testBitmap)
        whenever(modelRouter.generate(any(), any())).thenReturn(
            ModelRouter.GenerationResult(image = generatedBitmap, modelUsed = "vertex")
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.DONE, result.status)
        // Verify daily tries were incremented
        verify(userProfileDao).updateDailyTries(eq(1), any())
    }

    @Test
    fun `regenerate returns ERROR without garment in previous session`() = runTest {
        val previousSession = TryOnSession(
            detectedGarment = null,
            status = TryOnStatus.DONE
        )

        val result = useCase.regenerate(previousSession)

        assertEquals(TryOnStatus.ERROR, result.status)
        assertEquals("Nenhuma roupa detectada na sessão anterior", result.errorMessage)
    }
}
