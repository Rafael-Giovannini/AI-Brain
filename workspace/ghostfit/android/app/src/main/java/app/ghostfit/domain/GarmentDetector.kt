package app.ghostfit.domain

import android.graphics.Bitmap
import android.util.Log
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.remote.GarmentClassification
import app.ghostfit.data.remote.GeminiContent
import app.ghostfit.data.remote.GeminiInlineData
import app.ghostfit.data.remote.GeminiPart
import app.ghostfit.data.remote.GeminiRequest
import app.ghostfit.data.remote.GeminiVisionApi
import app.ghostfit.data.remote.ImageUrl
import app.ghostfit.data.remote.VisionContent
import app.ghostfit.data.remote.VisionLlmApi
import app.ghostfit.data.remote.VisionMessage
import app.ghostfit.data.remote.VisionRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class GarmentDetector(
    private val geminiVisionApi: GeminiVisionApi,
    private val visionLlmApi: VisionLlmApi,
    private val cropper: GarmentCropper
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

        // Try Gemini first (primary)
        try {
            val result = classifyWithGemini(base64, croppedImage)
            if (result != null) {
                Log.d(TAG, "Classification via Gemini 2.0 Flash succeeded")
                return result
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gemini classification failed, trying GPT-4o fallback", e)
        }

        // Try GPT-4o fallback
        try {
            val result = classifyWithGpt4o(base64, croppedImage)
            if (result != null) {
                Log.d(TAG, "Classification via GPT-4o fallback succeeded")
                return result
            }
        } catch (e: Exception) {
            Log.w(TAG, "GPT-4o classification also failed, using default fallback", e)
        }

        // Both failed — return default
        Log.d(TAG, "Both Gemini and GPT-4o failed, returning default TOP")
        return fallbackGarmentInfo(croppedImage)
    }

    private suspend fun classifyWithGemini(base64: String, croppedImage: Bitmap): GarmentInfo? {
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = VISION_PROMPT),
                        GeminiPart(inlineData = GeminiInlineData(data = base64))
                    )
                )
            )
        )

        val response = geminiVisionApi.generateContent(request = request)
        val rawText = response.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: return null

        return parseClassification(rawText, croppedImage)
    }

    private suspend fun classifyWithGpt4o(base64: String, croppedImage: Bitmap): GarmentInfo? {
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
            ?: return null

        return parseClassification(rawJson, croppedImage)
    }

    private fun parseClassification(rawText: String, croppedImage: Bitmap): GarmentInfo? {
        val cleanJson = rawText
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val classification = classificationAdapter.fromJson(cleanJson) ?: return null

        if (classification.confidence < MIN_CONFIDENCE) return null

        return GarmentInfo(
            category = classification.toGarmentCategory(),
            color = classification.color,
            description = classification.description,
            confidence = classification.confidence,
            croppedImage = croppedImage
        )
    }

    private fun fallbackGarmentInfo(croppedImage: Bitmap): GarmentInfo = GarmentInfo(
        category = GarmentCategory.TOP,
        color = null,
        description = null,
        confidence = MIN_CONFIDENCE,
        croppedImage = croppedImage
    )

}
