package app.ghostfit.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import app.ghostfit.domain.ScreenCaptureProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wrapper around MediaProjection API for screen capture.
 *
 * Usage:
 * 1. Call requestPermission() from an Activity to get user consent
 * 2. Pass the result to init() to set up the projection
 * 3. Call capture() to take a screenshot as Bitmap
 * 4. Call release() when done
 */
class ScreenCapture(private val context: Context) : ScreenCaptureProvider {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    val isReady: Boolean get() = mediaProjection != null

    /**
     * Returns the Intent to launch the screen capture permission dialog.
     * Must be called from an Activity and result forwarded to init().
     */
    fun requestPermission(activity: Activity): Intent {
        val manager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return manager.createScreenCaptureIntent()
    }

    /**
     * Initialize MediaProjection from the permission result.
     * Call this from onActivityResult after the user grants permission.
     */
    fun init(resultCode: Int, data: Intent) {
        val manager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = manager.getMediaProjection(resultCode, data)
    }

    /**
     * Capture a single screenshot as a Bitmap.
     * Suspends until the image is available.
     */
    override suspend fun capture(): Bitmap = suspendCancellableCoroutine { cont ->
        val projection = mediaProjection
        if (projection == null) {
            cont.resumeWithException(IllegalStateException("MediaProjection not initialized. Call init() first."))
            return@suspendCancellableCoroutine
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader = reader

        val handler = Handler(Looper.getMainLooper())

        virtualDisplay = projection.createVirtualDisplay(
            "GhostFitCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null, handler
        )

        reader.setOnImageAvailableListener({ ir ->
            val image: Image? = ir.acquireLatestImage()
            if (image != null) {
                val bitmap = imageToBitmap(image, width, height)
                image.close()
                teardownVirtualDisplay()
                if (cont.isActive) cont.resume(bitmap)
            }
        }, handler)

        cont.invokeOnCancellation {
            teardownVirtualDisplay()
        }
    }

    /**
     * Release all resources. Call when overlay is destroyed.
     */
    fun release() {
        teardownVirtualDisplay()
        mediaProjection?.stop()
        mediaProjection = null
    }

    private fun teardownVirtualDisplay() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }

    private fun imageToBitmap(image: Image, width: Int, height: Int): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        // Crop padding if present
        return if (rowPadding > 0) {
            Bitmap.createBitmap(bitmap, 0, 0, width, height)
        } else {
            bitmap
        }
    }
}
