package app.ghostfit.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.remote.FashnApi
import app.ghostfit.data.remote.FashnRequest
import app.ghostfit.data.remote.GhostFitApi
import app.ghostfit.data.remote.VertexAiApi
import app.ghostfit.data.remote.VertexImage
import app.ghostfit.data.remote.VertexInstance
import app.ghostfit.data.remote.VertexRequest
/**
 * Chain-of-responsibility router: queries backend for optimal model ordering
 * based on accumulated approval scores (FR-020), then falls back to default
 * FASHN.ai (primary) → Vertex AI (fallback) if backend is unavailable.
 * Each provider gets 2 retries before falling through.
 */
class ModelRouter(
    private val fashnApi: FashnApi,
    private val vertexAiApi: VertexAiApi,
    private val gcpAccessToken: String = "",
    private val ghostFitApi: GhostFitApi? = null
) {
    /** True when Vertex AI is available (GCP token configured). */
    private val vertexAvailable = gcpAccessToken.isNotBlank()

    companion object {
        private const val TAG = "ModelRouter"
        private const val FASHN_MAX_RETRIES = 2
        private const val VERTEX_MAX_RETRIES = 2
        internal const val MODEL_FASHN = "fashn"
        internal const val MODEL_VERTEX = "vertex"
    }

    data class GenerationResult(
        val image: Bitmap,
        val modelUsed: String
    )

    /**
     * Generate a virtual try-on image.
     * Queries the backend for the recommended model order based on approval
     * scores per (model × garment category). Falls back to default order
     * (FASHN → Vertex) if backend is unavailable or has insufficient data.
     *
     * @param referencePhotoBase64 Base64 encoded reference photo of the user
     * @param garment Detected garment info with cropped image
     * @return GenerationResult with the generated image and which model was used
     * @throws TryOnGenerationException if both models fail
     */
    suspend fun generate(
        referencePhotoBase64: String,
        garment: GarmentInfo
    ): GenerationResult {
        val garmentBase64 = garment.croppedImage?.let { it.toBase64Jpeg() }
            ?: throw TryOnGenerationException("No cropped garment image available")

        val (primary, fallback) = resolveModelOrder(garment.category)

        val primaryResult = tryModel(primary, referencePhotoBase64, garmentBase64, garment.category)
        if (primaryResult != null) return primaryResult

        val fallbackResult = tryModel(fallback, referencePhotoBase64, garmentBase64, garment.category)
        if (fallbackResult != null) return fallbackResult

        throw TryOnGenerationException("Both FASHN.ai and Vertex AI failed to generate image")
    }

    /**
     * Query backend for recommended model order. Returns default if unavailable.
     */
    internal suspend fun resolveModelOrder(category: GarmentCategory): Pair<String, String> {
        if (ghostFitApi == null) return MODEL_FASHN to MODEL_VERTEX
        return try {
            val route = ghostFitApi.getModelRoute(category.name.lowercase())
            val recommended = route.recommendedModel ?: MODEL_FASHN
            val fallback = route.fallbackModel ?: if (recommended == MODEL_FASHN) MODEL_VERTEX else MODEL_FASHN
            recommended to fallback
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve model order from backend, using default", e)
            MODEL_FASHN to MODEL_VERTEX
        }
    }

    private suspend fun tryModel(
        model: String,
        referenceBase64: String,
        garmentBase64: String,
        category: GarmentCategory
    ): GenerationResult? {
        return when (model) {
            MODEL_FASHN -> tryFashn(referenceBase64, garmentBase64, category)
            MODEL_VERTEX -> tryVertex(referenceBase64, garmentBase64, category)
            else -> null
        }
    }

    internal suspend fun tryFashn(
        referenceBase64: String,
        garmentBase64: String,
        category: GarmentCategory
    ): GenerationResult? {
        repeat(FASHN_MAX_RETRIES) { attempt ->
            try {
                val response = fashnApi.generateTryOn(
                    request = FashnRequest(
                        modelImage = referenceBase64,
                        garmentImage = garmentBase64,
                        category = FashnApi.mapCategory(category)
                    )
                )

                val imageData = response.output?.imageBase64
                    ?: response.output?.imageUrl?.let { return@repeat } // URL not directly decodable, retry
                    ?: return@repeat

                val bitmap = base64ToBitmap(imageData) ?: return@repeat

                return GenerationResult(image = bitmap, modelUsed = "fashn")
            } catch (e: Exception) {
                Log.w(TAG, "FASHN attempt ${attempt + 1}/$FASHN_MAX_RETRIES failed", e)
            }
        }
        return null
    }

    internal suspend fun tryVertex(
        referenceBase64: String,
        garmentBase64: String,
        category: GarmentCategory
    ): GenerationResult? {
        if (!vertexAvailable) {
            Log.d(TAG, "Vertex AI unavailable (no GCP token), skipping")
            return null
        }
        repeat(VERTEX_MAX_RETRIES) { attempt ->
            try {
                val response = vertexAiApi.generateTryOn(
                    auth = "Bearer $gcpAccessToken",
                    request = VertexRequest(
                        instances = listOf(
                            VertexInstance(
                                personImage = VertexImage(bytesBase64Encoded = referenceBase64),
                                garmentImage = VertexImage(bytesBase64Encoded = garmentBase64),
                                garmentType = VertexAiApi.mapCategory(category)
                            )
                        )
                    )
                )

                val imageData = response.predictions?.firstOrNull()?.bytesBase64Encoded
                    ?: return@repeat

                val bitmap = base64ToBitmap(imageData) ?: return@repeat

                return GenerationResult(image = bitmap, modelUsed = "vertex")
            } catch (e: Exception) {
                Log.d(TAG, "Vertex attempt ${attempt + 1}/$VERTEX_MAX_RETRIES failed", e)
            }
        }
        return null
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode base64 to bitmap", e)
            null
        }
    }
}

class TryOnGenerationException(message: String) : Exception(message)
