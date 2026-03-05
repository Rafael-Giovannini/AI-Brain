package app.ghostfit.domain

import android.graphics.Bitmap
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.local.ReferencePhotoDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.FeedbackRecord
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.model.UserProfile
import app.ghostfit.data.remote.DatasetEntry
import app.ghostfit.data.remote.FeedbackResponse
import app.ghostfit.data.remote.GhostFitApi
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

/**
 * Unit tests for TryOnUseCase.submitFeedback (Phase 4 — Story 3).
 * Validates FR-009 (thumbs up/down feedback) and FR-011 (anonymous dataset upload).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FeedbackSubmissionTest {

    private lateinit var ghostFitApi: GhostFitApi
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var useCase: TryOnUseCase

    private val testProfile = UserProfile(id = "user-1", lgpdConsentGranted = true)
    private val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

    private val testSession = TryOnSession(
        sessionId = "session-abc",
        detectedGarment = GarmentInfo(
            category = GarmentCategory.DRESS,
            confidence = 0.9f,
            croppedImage = testBitmap
        ),
        generatedImage = testBitmap,
        modelUsed = "fashn",
        status = TryOnStatus.DONE,
        durationMs = 5000
    )

    @Before
    fun setup() {
        ghostFitApi = mock()
        userProfileDao = mock()

        useCase = TryOnUseCase(
            screenCapture = mock(),
            garmentDetector = mock(),
            modelRouter = mock(),
            userProfileDao = userProfileDao,
            referencePhotoDao = mock(),
            photoStorage = mock(),
            ghostFitApi = ghostFitApi
        )
    }

    @Test
    fun `submitFeedback thumbsUp sends feedback and dataset entry`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(ghostFitApi.sendFeedback(any(), any())).thenReturn(FeedbackResponse("ok"))
        whenever(ghostFitApi.uploadDataset(any())).thenReturn(FeedbackResponse("ok"))

        useCase.submitFeedback(testSession, thumbsUp = true)

        verify(ghostFitApi).sendFeedback(
            eq("user-1"),
            any<FeedbackRecord>()
        )
        verify(ghostFitApi).uploadDataset(any<DatasetEntry>())
    }

    @Test
    fun `submitFeedback thumbsDown sends feedback but not dataset`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(ghostFitApi.sendFeedback(any(), any())).thenReturn(FeedbackResponse("ok"))

        useCase.submitFeedback(testSession, thumbsUp = false)

        verify(ghostFitApi).sendFeedback(
            eq("user-1"),
            any<FeedbackRecord>()
        )
        verify(ghostFitApi, never()).uploadDataset(any())
    }

    @Test
    fun `submitFeedback silently catches API errors`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(ghostFitApi.sendFeedback(any(), any())).thenThrow(RuntimeException("Network error"))

        // Should not throw
        useCase.submitFeedback(testSession, thumbsUp = true)
    }

    @Test
    fun `submitFeedback uses correct session data in FeedbackRecord`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(ghostFitApi.sendFeedback(any(), any())).thenReturn(FeedbackResponse("ok"))

        useCase.submitFeedback(testSession, thumbsUp = true)

        verify(ghostFitApi).sendFeedback(
            eq("user-1"),
            org.mockito.kotlin.check { feedback ->
                assertEquals("session-abc", feedback.sessionId)
                assertTrue(feedback.thumbsUp)
                assertEquals("fashn", feedback.modelUsed)
                assertEquals("dress", feedback.garmentCategory)
            }
        )
    }

    @Test
    fun `submitFeedback with null ghostFitApi does nothing`() = runTest {
        val useCaseNoApi = TryOnUseCase(
            screenCapture = mock(),
            garmentDetector = mock(),
            modelRouter = mock(),
            userProfileDao = userProfileDao,
            referencePhotoDao = mock(),
            photoStorage = mock(),
            ghostFitApi = null
        )

        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        // Should not throw — ghostFitApi?.sendFeedback is a no-op
        useCaseNoApi.submitFeedback(testSession, thumbsUp = true)
    }

    @Test
    fun `submitFeedback with no garment uses unknown category`() = runTest {
        val sessionNoGarment = testSession.copy(detectedGarment = null)
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(ghostFitApi.sendFeedback(any(), any())).thenReturn(FeedbackResponse("ok"))

        useCase.submitFeedback(sessionNoGarment, thumbsUp = false)

        verify(ghostFitApi).sendFeedback(
            eq("user-1"),
            org.mockito.kotlin.check { feedback ->
                assertEquals("unknown", feedback.garmentCategory)
            }
        )
        // No dataset upload since thumbsUp=false
        verify(ghostFitApi, never()).uploadDataset(any())
    }

    @Test
    fun `submitFeedback thumbsUp with dataset upload includes correct data`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(ghostFitApi.sendFeedback(any(), any())).thenReturn(FeedbackResponse("ok"))
        whenever(ghostFitApi.uploadDataset(any())).thenReturn(FeedbackResponse("ok"))

        useCase.submitFeedback(testSession, thumbsUp = true)

        verify(ghostFitApi).uploadDataset(
            org.mockito.kotlin.check { entry ->
                assertEquals("dress", entry.clothingType)
                assertEquals("fashn", entry.modelUsed)
                assertEquals(5000L, entry.generationTimeMs)
                assertTrue(entry.approved)
            }
        )
    }
}
