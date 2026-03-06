package app.ghostfit.domain

import android.graphics.Bitmap
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.remote.GarmentClassification
import app.ghostfit.data.remote.GeminiCandidate
import app.ghostfit.data.remote.GeminiCandidateContent
import app.ghostfit.data.remote.GeminiCandidatePart
import app.ghostfit.data.remote.GeminiResponse
import app.ghostfit.data.remote.GeminiVisionApi
import app.ghostfit.data.remote.VisionChoice
import app.ghostfit.data.remote.VisionChoiceMessage
import app.ghostfit.data.remote.VisionLlmApi
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

    private lateinit var geminiVisionApi: GeminiVisionApi
    private lateinit var visionLlmApi: VisionLlmApi
    private lateinit var detector: GarmentDetector

    @Before
    fun setup() {
        geminiVisionApi = mock()
        visionLlmApi = mock()
        val mockCropper = GarmentCropper { it } // passthrough cropper
        detector = GarmentDetector(geminiVisionApi, visionLlmApi, mockCropper)
    }

    // --- Existing tests (updated to use Gemini as primary) ---

    @Test
    fun `classifyWithVisionLlm returns GarmentInfo when confidence above threshold`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"dress","color":"red","description":"Red summer dress","confidence":0.92}"""

        whenever(geminiVisionApi.generateContent(any(), any())).thenReturn(
            geminiResponse(json)
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

        whenever(geminiVisionApi.generateContent(any(), any())).thenReturn(
            geminiResponse(json)
        )

        val result = detector.classifyWithVisionLlm(bitmap)
        assertNull(result)
    }

    @Test
    fun `classifyWithVisionLlm handles markdown-wrapped JSON response`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = "```json\n{\"category\":\"bottom\",\"color\":\"black\",\"description\":\"Jeans\",\"confidence\":0.85}\n```"

        whenever(geminiVisionApi.generateContent(any(), any())).thenReturn(
            geminiResponse(json)
        )

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.BOTTOM, result!!.category)
    }

    @Test
    fun `classifyWithVisionLlm falls back to TOP when both providers fail`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        whenever(geminiVisionApi.generateContent(any(), any())).thenThrow(RuntimeException("Gemini timeout"))
        whenever(visionLlmApi.classifyGarment(any(), any())).thenThrow(RuntimeException("GPT-4o timeout"))

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.TOP, result!!.category)
        assertEquals(0.6f, result.confidence, 0.01f)
    }

    @Test
    fun `classifyWithVisionLlm maps outerwear category correctly`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"outerwear","color":"brown","description":"Leather jacket","confidence":0.88}"""

        whenever(geminiVisionApi.generateContent(any(), any())).thenReturn(
            geminiResponse(json)
        )

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.OUTERWEAR, result!!.category)
    }

    @Test
    fun `toBase64Jpeg produces non-empty string`() {
        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val base64 = bitmap.toBase64Jpeg()
        assertTrue(base64.isNotEmpty())
    }

    // --- New Phase 8 tests: Gemini/GPT-4o fallback chain ---

    @Test
    fun `gemini success returns Gemini result without calling GPT-4o`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"top","color":"white","description":"White t-shirt","confidence":0.95}"""

        whenever(geminiVisionApi.generateContent(any(), any())).thenReturn(
            geminiResponse(json)
        )
        // GPT-4o should NOT be called, but set it up to fail so test would fail if called
        whenever(visionLlmApi.classifyGarment(any(), any())).thenThrow(RuntimeException("Should not be called"))

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.TOP, result!!.category)
        assertEquals("white", result.color)
        assertEquals(0.95f, result.confidence, 0.01f)
    }

    @Test
    fun `gemini fails and GPT-4o succeeds returns GPT-4o result`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val json = """{"category":"bottom","color":"blue","description":"Blue jeans","confidence":0.88}"""

        whenever(geminiVisionApi.generateContent(any(), any())).thenThrow(RuntimeException("Gemini API error"))
        whenever(visionLlmApi.classifyGarment(any(), any())).thenReturn(
            VisionResponse(choices = listOf(VisionChoice(VisionChoiceMessage(content = json))))
        )

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.BOTTOM, result!!.category)
        assertEquals("blue", result.color)
        assertEquals(0.88f, result.confidence, 0.01f)
    }

    @Test
    fun `both Gemini and GPT-4o fail returns default TOP fallback`() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)

        whenever(geminiVisionApi.generateContent(any(), any())).thenThrow(RuntimeException("Gemini down"))
        whenever(visionLlmApi.classifyGarment(any(), any())).thenThrow(RuntimeException("GPT-4o down"))

        val result = detector.classifyWithVisionLlm(bitmap)

        assertNotNull(result)
        assertEquals(GarmentCategory.TOP, result!!.category)
        assertNull(result.color)
        assertNull(result.description)
        assertEquals(0.6f, result.confidence, 0.01f)
    }

    // --- Helpers ---

    private fun geminiResponse(text: String) = GeminiResponse(
        candidates = listOf(
            GeminiCandidate(
                content = GeminiCandidateContent(
                    parts = listOf(GeminiCandidatePart(text = text))
                )
            )
        )
    )
}
