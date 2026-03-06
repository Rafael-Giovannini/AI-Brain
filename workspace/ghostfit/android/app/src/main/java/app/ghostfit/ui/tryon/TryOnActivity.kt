package app.ghostfit.ui.tryon

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import app.ghostfit.data.local.AppDatabase
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.remote.FashnApi
import app.ghostfit.data.remote.GhostFitApi
import app.ghostfit.data.remote.VertexAiApi
import app.ghostfit.data.remote.VisionLlmApi
import app.ghostfit.domain.GarmentDetector
import app.ghostfit.domain.ModelRouter
import app.ghostfit.domain.TryOnSession
import app.ghostfit.domain.TryOnStatus
import app.ghostfit.domain.TryOnUseCase
import app.ghostfit.overlay.MediaProjectionHolder
import app.ghostfit.overlay.ScreenCapture
import app.ghostfit.ui.theme.GhostFitTheme
import kotlinx.coroutines.launch

/**
 * Fullscreen Activity that runs the try-on pipeline and displays results.
 * Launched by OverlayService when the user taps the ghost overlay.
 *
 * Flow:
 * 1. Initializes ScreenCapture from MediaProjectionHolder
 * 2. Runs TryOnUseCase.execute() (capture → detect → generate)
 * 3. Shows TryOnResultScreen with all status transitions
 */
class TryOnActivity : ComponentActivity() {

    private var session by mutableStateOf(TryOnSession())
    private lateinit var tryOnUseCase: TryOnUseCase
    private lateinit var screenCapture: ScreenCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val projectionData = MediaProjectionHolder.get()
        if (projectionData == null) {
            Toast.makeText(this, "Permissão de captura não disponível.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.getInstance(this)
        val userProfileDao = db.userProfileDao()
        val referencePhotoDao = db.referencePhotoDao()
        val photoStorage = PhotoStorage.getInstance(this)

        screenCapture = ScreenCapture(this)
        screenCapture.init(projectionData.first, projectionData.second)

        val visionLlmApi = VisionLlmApi.create()
        val fashnApi = FashnApi.create()
        val vertexAiApi = VertexAiApi.create()
        val ghostFitApi = GhostFitApi.create()

        tryOnUseCase = TryOnUseCase(
            screenCapture = screenCapture,
            garmentDetector = GarmentDetector(visionLlmApi),
            modelRouter = ModelRouter(fashnApi, vertexAiApi, ghostFitApi = ghostFitApi),
            userProfileDao = userProfileDao,
            referencePhotoDao = referencePhotoDao,
            photoStorage = photoStorage,
            ghostFitApi = ghostFitApi
        )

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

        // Start the pipeline immediately
        executeTryOn()
    }

    private fun executeTryOn() {
        lifecycleScope.launch {
            session = tryOnUseCase.execute { updatedSession ->
                session = updatedSession
                TryOnSessionHolder.update(updatedSession)
            }
            TryOnSessionHolder.update(session)
        }
    }

    private fun regenerate() {
        val currentSession = session
        if (currentSession.status == TryOnStatus.DONE || currentSession.status == TryOnStatus.ERROR) {
            lifecycleScope.launch {
                session = tryOnUseCase.regenerate(currentSession) { updatedSession ->
                    session = updatedSession
                    TryOnSessionHolder.update(updatedSession)
                }
                TryOnSessionHolder.update(session)
            }
        }
    }

    private fun submitFeedback(thumbsUp: Boolean) {
        lifecycleScope.launch {
            tryOnUseCase.submitFeedback(session, thumbsUp)
            Toast.makeText(
                this@TryOnActivity,
                if (thumbsUp) "Obrigado pelo feedback! 👍" else "Feedback enviado 👎",
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
