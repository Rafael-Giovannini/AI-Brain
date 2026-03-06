package app.ghostfit.domain

import android.graphics.Bitmap
import android.util.Log
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.local.ReferencePhotoDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.FeedbackRecord
import app.ghostfit.data.model.GarmentInfo
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.remote.DatasetEntry
import app.ghostfit.data.remote.GhostFitApi
import app.ghostfit.domain.ScreenCaptureProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

enum class TryOnStatus {
    IDLE, CAPTURING, DETECTING, GENERATING, DONE, ERROR, NO_GARMENT
}

data class TryOnSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val referencePhotoId: String? = null,
    val screenshotBitmap: Bitmap? = null,
    val detectedGarment: GarmentInfo? = null,
    val generatedImage: Bitmap? = null,
    val modelUsed: String? = null,
    val status: TryOnStatus = TryOnStatus.IDLE,
    val errorMessage: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val durationMs: Long = 0
)

class TryOnUseCase(
    private val screenCapture: ScreenCaptureProvider,
    private val garmentDetector: GarmentDetector,
    private val modelRouter: ModelRouter,
    private val userProfileDao: UserProfileDao,
    private val referencePhotoDao: ReferencePhotoDao,
    private val photoStorage: PhotoStorage,
    private val ghostFitApi: GhostFitApi? = null
) {

    companion object {
        private const val TAG = "TryOnUseCase"
        /** Maximum free daily try-on attempts. Mirrors RemoteConfig.maxFreeTrials default. */
        const val MAX_FREE_DAILY_TRIES = 3
    }

    /**
     * Execute the full try-on pipeline: capture → detect → generate.
     *
     * @param onStatusChange Callback for status updates during the pipeline
     * @return Final TryOnSession with the result
     */
    suspend fun execute(
        onStatusChange: (TryOnSession) -> Unit = {}
    ): TryOnSession = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var session = TryOnSession(startedAt = startTime)

        try {
            // Check daily limit for free users
            checkDailyLimit()

            // Step 1: Capture screenshot
            session = session.copy(status = TryOnStatus.CAPTURING)
            onStatusChange(session)
            val screenshot = screenCapture.capture()
            session = session.copy(screenshotBitmap = screenshot)

            // Step 2: Detect garment
            session = session.copy(status = TryOnStatus.DETECTING)
            onStatusChange(session)
            val garment = garmentDetector.detect(screenshot)

            if (garment == null) {
                session = session.copy(
                    status = TryOnStatus.NO_GARMENT,
                    errorMessage = "Nenhuma roupa detectada. Tente em uma página de produto.",
                    durationMs = System.currentTimeMillis() - startTime
                )
                onStatusChange(session)
                return@withContext session
            }

            session = session.copy(detectedGarment = garment)

            // Step 3: Get reference photo
            val referenceBase64 = getActiveReferenceBase64()
            session = session.copy(referencePhotoId = referencePhotoDao.getByUser(
                userProfileDao.getProfile()?.id ?: ""
            ).firstOrNull()?.id)

            // Step 4: Generate try-on image
            session = session.copy(status = TryOnStatus.GENERATING)
            onStatusChange(session)
            val result = modelRouter.generate(referenceBase64, garment)

            // Step 5: Increment daily tries
            incrementDailyTries()

            session = session.copy(
                generatedImage = result.image,
                modelUsed = result.modelUsed,
                status = TryOnStatus.DONE,
                durationMs = System.currentTimeMillis() - startTime
            )
            onStatusChange(session)
            session
        } catch (e: DailyLimitExceededException) {
            session = session.copy(
                status = TryOnStatus.ERROR,
                errorMessage = e.message,
                durationMs = System.currentTimeMillis() - startTime
            )
            onStatusChange(session)
            session
        } catch (e: Exception) {
            session = session.copy(
                status = TryOnStatus.ERROR,
                errorMessage = e.message ?: "Erro desconhecido",
                durationMs = System.currentTimeMillis() - startTime
            )
            onStatusChange(session)
            session
        }
    }

    /**
     * Regenerate with different parameters (counts as additional attempt).
     */
    suspend fun regenerate(
        previousSession: TryOnSession,
        onStatusChange: (TryOnSession) -> Unit = {}
    ): TryOnSession = withContext(Dispatchers.IO) {
        val garment = previousSession.detectedGarment
            ?: return@withContext previousSession.copy(
                status = TryOnStatus.ERROR,
                errorMessage = "Nenhuma roupa detectada na sessão anterior"
            )

        val startTime = System.currentTimeMillis()
        var session = TryOnSession(
            detectedGarment = garment,
            referencePhotoId = previousSession.referencePhotoId,
            startedAt = startTime
        )

        try {
            checkDailyLimit()

            val referenceBase64 = getActiveReferenceBase64()

            session = session.copy(status = TryOnStatus.GENERATING)
            onStatusChange(session)

            val result = modelRouter.generate(referenceBase64, garment)
            incrementDailyTries()

            session = session.copy(
                generatedImage = result.image,
                modelUsed = result.modelUsed,
                status = TryOnStatus.DONE,
                durationMs = System.currentTimeMillis() - startTime
            )
            onStatusChange(session)
            session
        } catch (e: Exception) {
            session = session.copy(
                status = TryOnStatus.ERROR,
                errorMessage = e.message ?: "Erro desconhecido",
                durationMs = System.currentTimeMillis() - startTime
            )
            onStatusChange(session)
            session
        }
    }

    /**
     * Submit feedback (thumbs up/down) for a session. Fire-and-forget.
     */
    suspend fun submitFeedback(session: TryOnSession, thumbsUp: Boolean) {
        try {
            val feedback = FeedbackRecord(
                sessionId = session.sessionId,
                thumbsUp = thumbsUp,
                modelUsed = session.modelUsed ?: "unknown",
                garmentCategory = session.detectedGarment?.category?.name?.lowercase() ?: "unknown"
            )
            ghostFitApi?.sendFeedback(
                deviceId = userProfileDao.getProfile()?.id ?: "",
                feedback = feedback
            )

            // Upload to dataset if thumbs up
            if (thumbsUp && session.detectedGarment != null) {
                ghostFitApi?.uploadDataset(
                    DatasetEntry(
                        clothingType = session.detectedGarment.category.name.lowercase(),
                        clothingDescription = session.detectedGarment.description,
                        modelUsed = session.modelUsed ?: "unknown",
                        generationTimeMs = session.durationMs,
                        approved = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Feedback submission failed (fire-and-forget)", e)
        }
    }

    /**
     * Decrypt and encode the user's active reference photo as base64 JPEG.
     * Eliminates duplicate decryption logic across execute() and regenerate().
     */
    private suspend fun getActiveReferenceBase64(): String {
        val refPhoto = referencePhotoDao.getByUser(
            userProfileDao.getProfile()?.id ?: ""
        ).firstOrNull() ?: throw IllegalStateException("Nenhuma foto de referência encontrada")

        val bitmap = photoStorage.decrypt(refPhoto.encryptedFilePath)
            ?: throw IllegalStateException("Falha ao decifrar foto de referência")
        return bitmap.toBase64Jpeg()
    }

    private suspend fun checkDailyLimit() {
        val profile = userProfileDao.getProfile() ?: return
        if (profile.planType == PlanType.PREMIUM) return
        if (profile.bonusTries > 0) return // Pack tries available

        val today = LocalDate.now().toString()
        if (profile.dailyTriesResetDate != today) {
            userProfileDao.updateDailyTries(0, today)
            return
        }

        if (profile.dailyTriesUsed >= MAX_FREE_DAILY_TRIES) {
            throw DailyLimitExceededException(
                "Limite diário atingido ($MAX_FREE_DAILY_TRIES/$MAX_FREE_DAILY_TRIES). Faça upgrade para continuar!"
            )
        }
    }

    private suspend fun incrementDailyTries() {
        val profile = userProfileDao.getProfile() ?: return
        if (profile.planType == PlanType.PREMIUM) return

        // Consume bonus tries first, then daily tries
        if (profile.bonusTries > 0) {
            userProfileDao.updateBonusTries(profile.bonusTries - 1)
            return
        }

        val today = LocalDate.now().toString()
        userProfileDao.updateDailyTries(profile.dailyTriesUsed + 1, today)
    }
}

class DailyLimitExceededException(message: String) : Exception(message)
