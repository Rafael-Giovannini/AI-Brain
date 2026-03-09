package app.ghostfit.ui.tryon

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import app.ghostfit.data.ads.AdManagerImpl
import app.ghostfit.data.local.AppDatabase
import app.ghostfit.data.local.MlKitGarmentCropper
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.remote.FashnApi
import app.ghostfit.data.remote.GhostFitApi
import app.ghostfit.data.remote.VertexAiApi
import app.ghostfit.data.remote.GeminiVisionApi
import app.ghostfit.data.remote.VisionLlmApi
import app.ghostfit.domain.AdProvider
import app.ghostfit.domain.GarmentDetector
import app.ghostfit.domain.ModelRouter
import app.ghostfit.domain.ScreenCaptureProvider
import app.ghostfit.domain.TryOnSession
import app.ghostfit.domain.TryOnStatus
import app.ghostfit.domain.TryOnUseCase
import app.ghostfit.overlay.MediaProjectionHolder
import app.ghostfit.overlay.ScreenCapture
import app.ghostfit.ui.theme.GhostFitTheme
import kotlinx.coroutines.launch

/**
 * Fullscreen Activity that displays try-on results and handles user interactions.
 *
 * Two modes of operation:
 * - **Normal mode**: Initializes ScreenCapture, runs the full pipeline (capture -> detect -> generate)
 * - **Display-only mode** (EXTRA_DISPLAY_ONLY=true): Reads pre-computed session from TryOnSessionHolder,
 *   launched by OverlayService after the pipeline completes in the service.
 *
 * In both modes, regenerate and feedback remain functional.
 */
class TryOnActivity : ComponentActivity() {

    companion object {
        /** When true, reads session from TryOnSessionHolder instead of running the pipeline. */
        const val EXTRA_DISPLAY_ONLY = "extra_display_only"
    }

    private var session by mutableStateOf(TryOnSession())
    private lateinit var tryOnUseCase: TryOnUseCase
    private lateinit var adProvider: AdProvider
    private lateinit var screenCapture: ScreenCapture
    private var displayOnly = false

    private fun initDependencies(): Boolean {
        val db = AppDatabase.getInstance(this)
        val userProfileDao = db.userProfileDao()
        val referencePhotoDao = db.referencePhotoDao()
        val photoStorage = PhotoStorage.getInstance(this)

        val geminiVisionApi = GeminiVisionApi.create()
        val visionLlmApi = VisionLlmApi.create()
        val fashnApi = FashnApi.create()
        val vertexAiApi = VertexAiApi.create()
        val ghostFitApi = GhostFitApi.create()

        adProvider = AdManagerImpl(userProfileDao)
        adProvider.initialize(this, AdProvider.TEST_INTERSTITIAL_ID)

        if (displayOnly) {
            val existingSession = TryOnSessionHolder.currentSession
            if (existingSession == null) {
                Toast.makeText(this, "Sessao nao disponivel.", Toast.LENGTH_SHORT).show()
                finish()
                return false
            }
            session = existingSession

            val noOpCapture = ScreenCaptureProvider {
                throw IllegalStateException("Screen capture not available in display-only mode")
            }
            tryOnUseCase = TryOnUseCase(
                screenCapture = noOpCapture,
                garmentDetector = GarmentDetector(geminiVisionApi, visionLlmApi, MlKitGarmentCropper()),
                modelRouter = ModelRouter(fashnApi, vertexAiApi, ghostFitApi = ghostFitApi),
                userProfileDao = userProfileDao,
                referencePhotoDao = referencePhotoDao,
                photoStorage = photoStorage,
                ghostFitApi = ghostFitApi
            )
        } else {
            val projectionData = MediaProjectionHolder.get()
            if (projectionData == null) {
                Toast.makeText(this, "Permissao de captura nao disponivel.", Toast.LENGTH_SHORT).show()
                finish()
                return false
            }

            screenCapture = ScreenCapture(this)
            screenCapture.init(projectionData.first, projectionData.second)

            tryOnUseCase = TryOnUseCase(
                screenCapture = screenCapture,
                garmentDetector = GarmentDetector(geminiVisionApi, visionLlmApi, MlKitGarmentCropper()),
                modelRouter = ModelRouter(fashnApi, vertexAiApi, ghostFitApi = ghostFitApi),
                userProfileDao = userProfileDao,
                referencePhotoDao = referencePhotoDao,
                photoStorage = photoStorage,
                ghostFitApi = ghostFitApi
            )
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayOnly = intent.getBooleanExtra(EXTRA_DISPLAY_ONLY, false)

        if (!initDependencies()) return

        setContent {
            GhostFitTheme {
                TryOnResultScreen(
                    session = session,
                    onRegenerate = { regenerate() },
                    onChangePhoto = { /* Future: navigate to photo select */ },
                    onThumbsUp = { submitFeedback(thumbsUp = true) },
                    onThumbsDown = { submitFeedback(thumbsUp = false) },
                    onShare = {
                        session.generatedImage?.let { bitmap ->
                            shareTryOnImage(this@TryOnActivity, bitmap)
                        }
                    },
                    onClose = { finish() },
                    onUpgrade = { finish() }
                )
            }
        }

        // Only run pipeline in normal mode; display-only already has the session
        if (!displayOnly) {
            executeTryOn()
        }
    }

    private fun executeTryOn() {
        lifecycleScope.launch {
            session = tryOnUseCase.execute { updatedSession ->
                session = updatedSession
                TryOnSessionHolder.update(updatedSession)
            }
            TryOnSessionHolder.update(session)
            if (session.status == TryOnStatus.DONE) {
                adProvider.onGenerationCompleted()
            }
        }
    }

    private fun regenerate() {
        val currentSession = session
        if (currentSession.status == TryOnStatus.DONE || currentSession.status == TryOnStatus.ERROR) {
            lifecycleScope.launch {
                // FR-016: Show interstitial ad between generations for free users
                adProvider.showAdIfNeeded(this@TryOnActivity) {
                    lifecycleScope.launch {
                        session = tryOnUseCase.regenerate(currentSession) { updatedSession ->
                            session = updatedSession
                            TryOnSessionHolder.update(updatedSession)
                        }
                        TryOnSessionHolder.update(session)
                        if (session.status == TryOnStatus.DONE) {
                            adProvider.onGenerationCompleted()
                        }
                    }
                }
            }
        }
    }

    private fun submitFeedback(thumbsUp: Boolean) {
        lifecycleScope.launch {
            tryOnUseCase.submitFeedback(session, thumbsUp)
            Toast.makeText(
                this@TryOnActivity,
                if (thumbsUp) "Obrigado pelo feedback!" else "Feedback enviado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        if (::screenCapture.isInitialized) {
            screenCapture.release()
        }
        TryOnSessionHolder.clear()
        super.onDestroy()
    }
}
