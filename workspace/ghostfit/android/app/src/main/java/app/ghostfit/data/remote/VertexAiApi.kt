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
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface VertexAiApi {

    @POST("v1/projects/{project}/locations/{region}/publishers/google/models/virtual-try-on-001:predict")
    suspend fun generateTryOn(
        @Path("project") project: String = BuildConfig.GCP_PROJECT_ID,
        @Path("region") region: String = "us-central1",
        @Header("Authorization") auth: String,
        @Body request: VertexRequest
    ): VertexResponse

    companion object {
        private const val BASE_URL = "https://us-central1-aiplatform.googleapis.com/"

        fun create(client: OkHttpClient? = null): VertexAiApi {
            val httpClient = client ?: OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(VertexAiApi::class.java)
        }

        fun mapCategory(category: GarmentCategory): String = when (category) {
            GarmentCategory.TOP, GarmentCategory.OUTERWEAR -> "TOP"
            GarmentCategory.BOTTOM -> "BOTTOM"
            GarmentCategory.DRESS -> "FULL"
        }
    }
}

data class VertexRequest(
    val instances: List<VertexInstance>,
    val parameters: VertexParams = VertexParams()
)

data class VertexInstance(
    @Json(name = "person_image") val personImage: VertexImage,
    @Json(name = "garment_image") val garmentImage: VertexImage,
    @Json(name = "garment_type") val garmentType: String
)

data class VertexImage(
    val bytesBase64Encoded: String
)

data class VertexParams(
    val sampleCount: Int = 1
)

data class VertexResponse(
    val predictions: List<VertexPrediction>? = null
)

data class VertexPrediction(
    val bytesBase64Encoded: String? = null
)
