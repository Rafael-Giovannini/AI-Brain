package app.ghostfit.data.remote

import app.ghostfit.data.model.FeedbackRecord
import com.squareup.moshi.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GhostFitApi {

    @POST("v1/feedback")
    suspend fun sendFeedback(
        @Header("X-Device-Id") deviceId: String,
        @Body feedback: FeedbackRecord
    ): FeedbackResponse

    @GET("v1/models/route")
    suspend fun getModelRoute(
        @Query("garmentCategory") garmentCategory: String
    ): ModelRouteResponse

    @GET("v1/config")
    suspend fun getConfig(): RemoteConfig

    @POST("v1/dataset")
    suspend fun uploadDataset(
        @Body data: DatasetEntry
    ): FeedbackResponse

    @GET("v1/health")
    suspend fun healthCheck(): HealthResponse

    companion object {
        private const val BASE_URL = "https://api.ghostfit.app/"

        fun create(client: OkHttpClient? = null): GhostFitApi {
            val httpClient = client ?: OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(GhostFitApi::class.java)
        }
    }
}

data class FeedbackResponse(
    val status: String? = null
)

data class ModelRouteResponse(
    val recommendedModel: String? = null,
    val fallbackModel: String? = null,
    val reason: String? = null
)

data class RemoteConfig(
    val primaryGenModel: String = "fashn",
    val fallbackGenModel: String = "vertex",
    val maxFreeTrials: Int = 3,
    val visionTimeout: Long = 5000,
    val genTimeout: Long = 20000,
    val minConfidence: Float = 0.6f,
    val subscriptionPrices: SubscriptionPrices? = null
)

data class SubscriptionPrices(
    val monthly: Double = 9.90,
    @Json(name = "pack10") val pack10: Double = 4.90
)

data class DatasetEntry(
    val clothingType: String,
    val clothingDescription: String? = null,
    val modelUsed: String,
    val generationTimeMs: Long,
    val approved: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class HealthResponse(
    val status: String?,
    val timestamp: Long?
)
