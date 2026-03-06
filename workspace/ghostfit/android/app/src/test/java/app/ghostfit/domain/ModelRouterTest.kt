package app.ghostfit.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.remote.FashnApi
import app.ghostfit.data.remote.FashnOutput
import app.ghostfit.data.remote.FashnRequest
import app.ghostfit.data.remote.FashnResponse
import app.ghostfit.data.remote.GhostFitApi
import app.ghostfit.data.remote.ModelRouteResponse
import app.ghostfit.data.remote.VertexAiApi
import app.ghostfit.data.remote.VertexPrediction
import app.ghostfit.data.remote.VertexRequest
import app.ghostfit.data.remote.VertexResponse
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
import java.io.ByteArrayOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ModelRouterTest {

    private lateinit var fashnApi: FashnApi
    private lateinit var vertexAiApi: VertexAiApi
    private lateinit var ghostFitApi: GhostFitApi
    private lateinit var router: ModelRouter
    private lateinit var routerWithBackend: ModelRouter

    private fun createTestBitmapBase64(): String {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun createTestGarment(): GarmentInfo {
        val bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        return GarmentInfo(
            category = GarmentCategory.DRESS,
            color = "red",
            description = "Red dress",
            confidence = 0.9f,
            croppedImage = bitmap
        )
    }

    @Before
    fun setup() {
        fashnApi = mock()
        vertexAiApi = mock()
        ghostFitApi = mock()
        router = ModelRouter(fashnApi, vertexAiApi, gcpAccessToken = "test-token")
        routerWithBackend = ModelRouter(fashnApi, vertexAiApi, gcpAccessToken = "test-token", ghostFitApi = ghostFitApi)
    }

    @Test
    fun `generate uses FASHN as primary model`() = runTest {
        val imageBase64 = createTestBitmapBase64()

        whenever(fashnApi.generateTryOn(any(), any())).thenReturn(
            FashnResponse(
                id = "run_123",
                status = "completed",
                output = FashnOutput(imageBase64 = imageBase64)
            )
        )

        val result = router.generate("ref_base64", createTestGarment())

        assertEquals("fashn", result.modelUsed)
        assertNotNull(result.image)
    }

    @Test
    fun `generate falls back to Vertex when FASHN fails`() = runTest {
        val imageBase64 = createTestBitmapBase64()

        whenever(fashnApi.generateTryOn(any(), any())).thenThrow(RuntimeException("FASHN down"))

        whenever(vertexAiApi.generateTryOn(any(), any(), any(), any())).thenReturn(
            VertexResponse(
                predictions = listOf(VertexPrediction(bytesBase64Encoded = imageBase64))
            )
        )

        val result = router.generate("ref_base64", createTestGarment())

        assertEquals("vertex", result.modelUsed)
        assertNotNull(result.image)
    }

    @Test(expected = TryOnGenerationException::class)
    fun `generate throws when both models fail`() = runTest {
        whenever(fashnApi.generateTryOn(any(), any())).thenThrow(RuntimeException("FASHN down"))
        whenever(vertexAiApi.generateTryOn(any(), any(), any(), any())).thenThrow(RuntimeException("Vertex down"))

        router.generate("ref_base64", createTestGarment())
    }

    @Test(expected = TryOnGenerationException::class)
    fun `generate throws when garment has no cropped image`() = runTest {
        val garment = GarmentInfo(
            category = GarmentCategory.TOP,
            confidence = 0.8f,
            croppedImage = null
        )

        router.generate("ref_base64", garment)
    }

    @Test
    fun `FashnApi mapCategory maps correctly`() {
        assertEquals("tops", FashnApi.mapCategory(GarmentCategory.TOP))
        assertEquals("tops", FashnApi.mapCategory(GarmentCategory.OUTERWEAR))
        assertEquals("bottoms", FashnApi.mapCategory(GarmentCategory.BOTTOM))
        assertEquals("one-pieces", FashnApi.mapCategory(GarmentCategory.DRESS))
    }

    @Test
    fun `VertexAiApi mapCategory maps correctly`() {
        assertEquals("TOP", VertexAiApi.mapCategory(GarmentCategory.TOP))
        assertEquals("TOP", VertexAiApi.mapCategory(GarmentCategory.OUTERWEAR))
        assertEquals("BOTTOM", VertexAiApi.mapCategory(GarmentCategory.BOTTOM))
        assertEquals("FULL", VertexAiApi.mapCategory(GarmentCategory.DRESS))
    }

    @Test
    fun `tryFashn returns null on empty output`() = runTest {
        whenever(fashnApi.generateTryOn(any(), any())).thenReturn(
            FashnResponse(id = "run_123", status = "completed", output = null)
        )

        val result = router.tryFashn("ref", "garment", GarmentCategory.TOP)
        assertNull(result)
    }

    @Test
    fun `tryVertex returns null on empty predictions`() = runTest {
        whenever(vertexAiApi.generateTryOn(any(), any(), any(), any())).thenReturn(
            VertexResponse(predictions = emptyList())
        )

        val result = router.tryVertex("ref", "garment", GarmentCategory.TOP)
        assertNull(result)
    }

    // --- FR-020: Smart model routing via backend approval scores ---

    @Test
    fun `resolveModelOrder returns default when no backend configured`() = runTest {
        val order = router.resolveModelOrder(GarmentCategory.DRESS)
        assertEquals(ModelRouter.MODEL_FASHN, order.first)
        assertEquals(ModelRouter.MODEL_VERTEX, order.second)
    }

    @Test
    fun `resolveModelOrder uses backend recommendation when available`() = runTest {
        whenever(ghostFitApi.getModelRoute("dress")).thenReturn(
            ModelRouteResponse(
                recommendedModel = "vertex",
                fallbackModel = "fashn",
                reason = "approval_rate"
            )
        )

        val order = routerWithBackend.resolveModelOrder(GarmentCategory.DRESS)
        assertEquals(ModelRouter.MODEL_VERTEX, order.first)
        assertEquals(ModelRouter.MODEL_FASHN, order.second)
    }

    @Test
    fun `resolveModelOrder falls back to default when backend fails`() = runTest {
        whenever(ghostFitApi.getModelRoute(any())).thenThrow(RuntimeException("Network error"))

        val order = routerWithBackend.resolveModelOrder(GarmentCategory.TOP)
        assertEquals(ModelRouter.MODEL_FASHN, order.first)
        assertEquals(ModelRouter.MODEL_VERTEX, order.second)
    }

    @Test
    fun `resolveModelOrder handles null fields in response`() = runTest {
        whenever(ghostFitApi.getModelRoute("bottom")).thenReturn(
            ModelRouteResponse(recommendedModel = null, fallbackModel = null, reason = "default")
        )

        val order = routerWithBackend.resolveModelOrder(GarmentCategory.BOTTOM)
        assertEquals(ModelRouter.MODEL_FASHN, order.first)
        assertEquals(ModelRouter.MODEL_VERTEX, order.second)
    }

    @Test
    fun `generate with backend tries Vertex first when recommended`() = runTest {
        val imageBase64 = createTestBitmapBase64()

        whenever(ghostFitApi.getModelRoute("dress")).thenReturn(
            ModelRouteResponse(recommendedModel = "vertex", fallbackModel = "fashn", reason = "approval_rate")
        )
        whenever(vertexAiApi.generateTryOn(any(), any(), any(), any())).thenReturn(
            VertexResponse(predictions = listOf(VertexPrediction(bytesBase64Encoded = imageBase64)))
        )

        val result = routerWithBackend.generate("ref_base64", createTestGarment())

        assertEquals("vertex", result.modelUsed)
        verify(fashnApi, never()).generateTryOn(any(), any())
    }
}
