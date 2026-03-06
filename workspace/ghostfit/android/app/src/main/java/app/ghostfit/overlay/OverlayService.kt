package app.ghostfit.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.ghostfit.MainActivity
import app.ghostfit.data.local.AppDatabase
import app.ghostfit.data.local.PhotoStorage
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.remote.FashnApi
import app.ghostfit.data.remote.GhostFitApi
import app.ghostfit.data.remote.VertexAiApi
import app.ghostfit.data.remote.VisionLlmApi
import app.ghostfit.domain.GarmentDetector
import app.ghostfit.domain.ModelRouter
import app.ghostfit.domain.TryOnStatus
import app.ghostfit.domain.TryOnUseCase
import app.ghostfit.ui.tryon.TryOnActivity
import app.ghostfit.ui.tryon.TryOnSessionHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private const val CHANNEL_ID = "ghostfit_overlay"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP = "app.ghostfit.overlay.STOP"

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private lateinit var userProfileDao: UserProfileDao

    @Volatile
    internal var generationInProgress = false

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        userProfileDao = AppDatabase.getInstance(this).userProfileDao()

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        showOverlay()

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (!Settings.canDrawOverlays(this)) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        // Load saved position
        serviceScope.launch(Dispatchers.IO) {
            val profile = userProfileDao.getProfile()
            profile?.let {
                params.x = it.overlayPositionX.toInt()
                params.y = it.overlayPositionY.toInt()
            }

            launch(Dispatchers.Main) {
                val composeView = ComposeView(this@OverlayService).apply {
                    setViewTreeLifecycleOwner(this@OverlayService)
                    setViewTreeSavedStateRegistryOwner(this@OverlayService)
                    setContent {
                        OverlayComposable(
                            onDrag = { dx, dy ->
                                params.x += dx.toInt()
                                params.y += dy.toInt()
                                windowManager.updateViewLayout(this, params)
                            },
                            onDragEnd = {
                                saveOverlayPosition(params.x.toFloat(), params.y.toFloat())
                            },
                            onTap = {
                                launchTryOn()
                            }
                        )
                    }
                }
                overlayView = composeView
                windowManager.addView(composeView, params)
            }
        }
    }

    /**
     * Run the full try-on pipeline from the overlay service:
     * ScreenCapture.capture → GarmentDetector.detect → ModelRouter.generate
     * Then launch TryOnActivity to display the result.
     *
     * Edge cases:
     * - No MediaProjection → toast asking user to open app
     * - Generation already in progress → toast "Geração em andamento..."
     * - No garment detected → toast without consuming daily attempt
     * - Error → toast with error message
     */
    internal fun launchTryOn() {
        if (!MediaProjectionHolder.isAvailable) {
            Toast.makeText(
                this,
                "Permissão de captura não disponível. Abra o app para configurar.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (generationInProgress) {
            Toast.makeText(this, "Geração em andamento...", Toast.LENGTH_SHORT).show()
            return
        }

        generationInProgress = true

        serviceScope.launch(Dispatchers.IO) {
            var screenCapture: ScreenCapture? = null
            try {
                val projectionData = MediaProjectionHolder.get()
                    ?: throw IllegalStateException("Permissão de captura não disponível")

                screenCapture = ScreenCapture(this@OverlayService)
                screenCapture.init(projectionData.first, projectionData.second)

                val db = AppDatabase.getInstance(this@OverlayService)
                val profileDao = db.userProfileDao()
                val referencePhotoDao = db.referencePhotoDao()
                val photoStorage = PhotoStorage.getInstance(this@OverlayService)

                val visionLlmApi = VisionLlmApi.create()
                val fashnApi = FashnApi.create()
                val vertexAiApi = VertexAiApi.create()
                val ghostFitApi = GhostFitApi.create()

                val tryOnUseCase = TryOnUseCase(
                    screenCapture = screenCapture,
                    garmentDetector = GarmentDetector(visionLlmApi),
                    modelRouter = ModelRouter(fashnApi, vertexAiApi, ghostFitApi = ghostFitApi),
                    userProfileDao = profileDao,
                    referencePhotoDao = referencePhotoDao,
                    photoStorage = photoStorage,
                    ghostFitApi = ghostFitApi
                )

                val session = tryOnUseCase.execute { updatedSession ->
                    TryOnSessionHolder.update(updatedSession)
                }
                TryOnSessionHolder.update(session)

                when (session.status) {
                    TryOnStatus.DONE -> {
                        val intent = Intent(this@OverlayService, TryOnActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra(TryOnActivity.EXTRA_DISPLAY_ONLY, true)
                        }
                        startActivity(intent)
                    }
                    TryOnStatus.NO_GARMENT -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@OverlayService,
                                "Nenhuma roupa detectada. Tente em uma página de produto.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@OverlayService,
                                session.errorMessage ?: "Erro ao gerar prova virtual.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@OverlayService,
                        e.message ?: "Erro ao gerar prova virtual.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                screenCapture?.release()
                generationInProgress = false
            }
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: IllegalArgumentException) {
                // View already removed
            }
        }
        overlayView = null
    }

    private fun saveOverlayPosition(x: Float, y: Float) {
        serviceScope.launch(Dispatchers.IO) {
            userProfileDao.updateOverlayPosition(x, y)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GhostFit Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantém o fantasminha ativo sobre outros apps"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, OverlayService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("GhostFit ativo")
            .setContentText("Toque para abrir o app")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(openIntent)
            .addAction(
                Notification.Action.Builder(
                    null, "Parar", stopIntent
                ).build()
            )
            .setOngoing(true)
            .build()
    }
}
