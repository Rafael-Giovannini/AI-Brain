package app.ghostfit.domain

import android.graphics.Bitmap
import android.util.Log
import app.ghostfit.data.local.MlKitGarmentCropper
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.remote.GarmentClassification
import app.ghostfit.data.remote.ImageUrl
import app.ghostfit.data.remote.VisionContent
import app.ghostfit.data.remote.VisionLlmApi
import app.ghostfit.data.remote.VisionMessage
import app.ghostfit.data.remote.VisionRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class GarmentDetector(
    private val visionLlmApi: VisionLlmApi,
    private val cropper: GarmentCropper = MlKitGarmentCropper()
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val classificationAdapter = moshi.adapter(GarmentClassification::class.java)

    companion object {
        private const val TAG = "GarmentDetector"
        private const val MIN_CONFIDENCE = 0.6f
        private const val VISION_PROMPT = """Analyze this clothing image. Return ONLY a JSON object with these fields:
- "category": one of "top", "bottom", "dress", "outerwear"
- "color": the main color
- "description": a 1-phrase description
- "confidence": 0.0 to 1.0 how confident you are this is a clothing item

If no clothing item is clearly visible, set confidence below 0.6.
Return ONLY the JSON, no markdown or extra text."""
    }

    suspend fun detect(screenshot: Bitmap): GarmentInfo? {
        val cropped = cropper.crop(screenshot) ?: return null
        return classifyWithVisionLlm(cropped)
    }

    internal suspend fun classifyWithVisionLlm(croppedImage: Bitmap): GarmentInfo? {
        val base64 = croppedImage.toBase64Jpeg()

        return try {
            val request = VisionRequest(
                messages = listOf(
                    VisionMessage(
                        role = "user",
                        content = listOf(
                            VisionContent(type = "text", text = VISION_PROMPT),
                            VisionContent(
                                type = "image_url",
                                imageUrl = ImageUrl(url = "data:image/jpeg;base64,$base64")
                            )
                        )
                    )
                )
            )

            val response = visionLlmApi.classifyGarment(request = request)
            val rawJson = response.choices.firstOrNull()?.message?.content
                ?: return fallbackGarmentInfo(croppedImage)

            val cleanJson = rawJson
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val classification = classificationAdapter.fromJson(cleanJson)
                ?: return fallbackGarmentInfo(croppedImage)

            if (classification.confidence < MIN_CONFIDENCE) return null

            GarmentInfo(
                category = classification.toGarmentCategory(),
                color = classification.color,
                description = classification.description,
                confidence = classification.confidence,
                croppedImage = croppedImage
            )
        } catch (e: Exception) {
            Log.w(TAG, "Vision LLM classification failed, using fallback", e)
            fallbackGarmentInfo(croppedImage)
        }
    }

    private fun fallbackGarmentInfo(croppedImage: Bitmap): GarmentInfo = GarmentInfo(
        category = GarmentCategory.TOP,
        color = null,
        description = null,
        confidence = MIN_CONFIDENCE,
        croppedImage = croppedImage
    )

}
