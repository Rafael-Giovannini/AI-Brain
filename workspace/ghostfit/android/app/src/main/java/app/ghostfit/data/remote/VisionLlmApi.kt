package app.ghostfit.data.remote

import app.ghostfit.BuildConfig
import app.ghostfit.data.model.GarmentCategory
import app.ghostfit.data.model.GarmentInfo
import com.squareup.moshi.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface VisionLlmApi {

    @POST("v1/chat/completions")
    suspend fun classifyGarment(
        @Header("Authorization") auth: String = "Bearer ${BuildConfig.OPENAI_API_KEY}",
        @Body request: VisionRequest
    ): VisionResponse

    companion object {
        private const val BASE_URL = "https://api.openai.com/"

        fun create(client: OkHttpClient? = null): VisionLlmApi {
            val httpClient = client ?: OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(VisionLlmApi::class.java)
        }
    }
}

data class VisionRequest(
    val model: String = "gpt-4o",
    val messages: List<VisionMessage>,
    @Json(name = "max_tokens") val maxTokens: Int = 150,
    @Json(name = "response_format") val responseFormat: ResponseFormat = ResponseFormat()
)

data class ResponseFormat(
    val type: String = "json_object"
)

data class VisionMessage(
    val role: String,
    val content: List<VisionContent>
)

data class VisionContent(
    val type: String,
    val text: String? = null,
    @Json(name = "image_url") val imageUrl: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)

data class VisionResponse(
    val choices: List<VisionChoice>
)

data class VisionChoice(
    val message: VisionChoiceMessage
)

data class VisionChoiceMessage(
    val content: String
)

data class GarmentClassification(
    val category: String,
    val color: String? = null,
    val description: String? = null,
    val confidence: Float = 0f
) {
    fun toGarmentCategory(): GarmentCategory = when (category.lowercase()) {
        "top" -> GarmentCategory.TOP
        "bottom" -> GarmentCategory.BOTTOM
        "dress" -> GarmentCategory.DRESS
        "outerwear" -> GarmentCategory.OUTERWEAR
        else -> GarmentCategory.TOP
    }
}
