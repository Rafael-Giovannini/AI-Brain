package app.ghostfit.data.remote

import app.ghostfit.BuildConfig
import app.ghostfit.data.model.GarmentCategory
import com.squareup.moshi.Json
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

    companion object {
        private const val BASE_URL = "https://api.fashn.ai/"

        fun create(client: OkHttpClient? = null): FashnApi {
            val httpClient = client ?: OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
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
    val output: FashnOutput? = null
)

data class FashnOutput(
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "image_base64") val imageBase64: String? = null
)
