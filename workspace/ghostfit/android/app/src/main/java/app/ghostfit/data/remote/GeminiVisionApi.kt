package app.ghostfit.data.remote

import app.ghostfit.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiVisionApi {

    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String = BuildConfig.GEMINI_API_KEY,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"

        fun create(client: OkHttpClient? = null): GeminiVisionApi {
            val httpClient = client ?: OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create(
                    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                ))
                .build()
                .create(GeminiVisionApi::class.java)
        }
    }
}

// --- Request DTOs ---

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    @Json(name = "inline_data") val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    @Json(name = "mime_type") val mimeType: String = "image/jpeg",
    val data: String // base64-encoded image
)

// --- Response DTOs ---

data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

data class GeminiCandidate(
    val content: GeminiCandidateContent
)

data class GeminiCandidateContent(
    val parts: List<GeminiCandidatePart>
)

data class GeminiCandidatePart(
    val text: String
)
