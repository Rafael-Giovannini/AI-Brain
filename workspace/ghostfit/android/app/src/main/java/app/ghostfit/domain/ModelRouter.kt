package app.ghostfit.domain

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.remote.FashnApi
import app.ghostfit.data.remote.FashnRequest
import app.ghostfit.data.remote.VertexAiApi
import app.ghostfit.data.remote.VertexImage
import app.ghostfit.data.remote.VertexInstance
import app.ghostfit.data.remote.VertexRequest
/**
 * Chain-of-responsibility router: FASHN.ai (primary) → Vertex AI (fallback).
 * Each provider gets 2 retries before falling through.
 */
class ModelRouter(
    private val fashnApi: FashnApi,
    private val vertexAiApi: VertexAiApi,
    private val gcpAccessToken: String = ""
) {
    companion object {
        private const val FASHN_MAX_RETRIES = 2
        private const val VERTEX_MAX_RETRIES = 2
    }

    data class GenerationResult(
        val image: Bitmap,
        val modelUsed: String
    )

    /**
     * Generate a virtual try-on image.
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

        // Try FASHN.ai first
        val fashnResult = tryFashn(referencePhotoBase64, garmentBase64, garment.category)
        if (fashnResult != null) return fashnResult

        // Fallback to Vertex AI
        val vertexResult = tryVertex(referencePhotoBase64, garmentBase64, garment.category)
        if (vertexResult != null) return vertexResult

        throw TryOnGenerationException("Both FASHN.ai and Vertex AI failed to generate image")
    }

    internal suspend fun tryFashn(
        referenceBase64: String,
        garmentBase64: String,
        category: GarmentCategory
    ): GenerationResult? {
        repeat(FASHN_MAX_RETRIES) {
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
            } catch (_: Exception) {
                // Retry
            }
        }
        return null
    }

    internal suspend fun tryVertex(
        referenceBase64: String,
        garmentBase64: String,
        category: GarmentCategory
    ): GenerationResult? {
        repeat(VERTEX_MAX_RETRIES) {
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
            } catch (_: Exception) {
                // Retry
            }
        }
        return null
    }

    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}

class TryOnGenerationException(message: String) : Exception(message)
