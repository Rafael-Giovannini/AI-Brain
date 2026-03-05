package app.ghostfit.domain

import android.graphics.Bitmap
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.remote.GarmentClassification
import app.ghostfit.data.remote.VisionChoice
import app.ghostfit.data.remote.VisionChoiceMessage
import app.ghostfit.data.remote.VisionLlmApi
import app.ghostfit.data.remote.VisionRequest
import app.ghostfit.data.remote.VisionResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GarmentDetectorTest {

    private lateinit var visionLlmApi: VisionLlmApi
    private lateinit var detector: GarmentDetector

    @Before
    fun setup() {
        visionLlmApi = mock()
        val mockCropper = GarmentCropper { it } // passthrough cropper
        detector = GarmentDetector(visionLlmApi, mockCropper)
    }

    @Test
    fun `classifyWithVisionLlm returns GarmentInfo when confidence above threshold`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"dress","color":"red","description":"Red summer dress","confidence":0.92}"""

        whenever(visionLlmApi.classifyGarment(any(), any())).thenReturn(
            VisionResponse(choices = listOf(VisionChoice(VisionChoiceMessage(content = json))))
        )

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.DRESS, result!!.category)
        assertEquals("red", result.color)
        assertEquals(0.92f, result.confidence, 0.01f)
        assertNotNull(result.croppedImage)
    }

    @Test
    fun `classifyWithVisionLlm returns null when confidence below threshold`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"top","color":"blue","description":"Maybe a shirt","confidence":0.3}"""

        whenever(visionLlmApi.classifyGarment(any(), any())).thenReturn(
            VisionResponse(choices = listOf(VisionChoice(VisionChoiceMessage(content = json))))
        )

        val result = detector.classifyWithVisionLlm(bitmap)
        assertNull(result)
    }

    @Test
    fun `classifyWithVisionLlm handles markdown-wrapped JSON response`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = "```json\n{\"category\":\"bottom\",\"color\":\"black\",\"description\":\"Jeans\",\"confidence\":0.85}\n```"

        whenever(visionLlmApi.classifyGarment(any(), any())).thenReturn(
            VisionResponse(choices = listOf(VisionChoice(VisionChoiceMessage(content = json))))
        )

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.BOTTOM, result!!.category)
    }

    @Test
    fun `classifyWithVisionLlm falls back to TOP on API error`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        whenever(visionLlmApi.classifyGarment(any(), any())).thenThrow(RuntimeException("Timeout"))

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.TOP, result!!.category)
        assertEquals(0.6f, result.confidence, 0.01f)
    }

    @Test
    fun `classifyWithVisionLlm maps outerwear category correctly`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"outerwear","color":"brown","description":"Leather jacket","confidence":0.88}"""

        whenever(visionLlmApi.classifyGarment(any(), any())).thenReturn(
            VisionResponse(choices = listOf(VisionChoice(VisionChoiceMessage(content = json))))
        )

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.OUTERWEAR, result!!.category)
    }

    @Test
    fun `bitmapToBase64 produces non-empty string`() {
        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val base64 = detector.bitmapToBase64(bitmap)
        assertTrue(base64.isNotEmpty())
    }
}
