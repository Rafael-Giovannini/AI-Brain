package app.ghostfit.data.remote

import app.ghostfit.BuildConfig
import app.ghostfit.data.model.GarmentCategory
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface FashnApi {

    @POST("v1/run")
    suspend fun generateTryOn(
        @Header("Authorization") auth: String = "Bearer ${BuildConfig.FASHN_API_KEY}",
        @Body request: FashnRequest
    ): FashnResponse

    @retrofit2.http.GET("v1/status/{id}")
    suspend fun getStatus(
        @Header("Authorization") auth: String = "Bearer ${BuildConfig.FASHN_API_KEY}",
        @retrofit2.http.Path("id") id: String
    ): FashnResponse

    companion object {
        private const val BASE_URL = "https://api.fashn.ai/"

        fun create(client: OkHttpClient? = null): FashnApi {
            val httpClient = client ?: OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create(
                    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                ))
                .build()
                .create(FashnApi::class.java)
        }

        fun mapCategory(category: GarmentCategory): String = when (category) {
            GarmentCategory.TOP, GarmentCategory.OUTERWEAR -> "tops"
            GarmentCategory.BOTTOM -> "bottoms"
            GarmentCategory.DRESS -> "one-pieces"
        }
    }
}

data class FashnRequest(
    @Json(name = "model_image") val modelImage: String,
    @Json(name = "garment_image") val garmentImage: String,
    val category: String
)

data class FashnResponse(
    val id: String? = null,
    val status: String? = null,
    val output: Any? = null,
    val error: String? = null
) {
    /** FASHN returns output as a list of image URLs, e.g. ["https://cdn.fashn.ai/.../output_0.png"] */
    fun getOutputUrl(): String? {
        return when (output) {
            is String -> output
            is List<*> -> (output as List<*>).firstOrNull()?.toString()
            else -> null
        }
    }
}

data class FashnOutput(
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "image_base64") val imageBase64: String? = null
)
