package app.ghostfit.domain

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface GarmentCropper {
    suspend fun crop(screenshot: Bitmap): Bitmap?
}

class MlKitGarmentCropper : GarmentCropper {

    override suspend fun crop(screenshot: Bitmap): Bitmap? =
        suspendCancellableCoroutine { cont ->
            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .build()

            val detector = ObjectDetection.getClient(options)
            val inputImage = InputImage.fromBitmap(screenshot, 0)

            detector.process(inputImage)
                .addOnSuccessListener { objects ->
                    detector.close()
                    if (objects.isEmpty()) {
                        cont.resume(null)
                        return@addOnSuccessListener
                    }

                    val largest = objects.maxByOrNull {
                        it.boundingBox.width() * it.boundingBox.height()
                    } ?: run {
                        cont.resume(null)
                        return@addOnSuccessListener
                    }

                    val box = largest.boundingBox
                    val left = box.left.coerceAtLeast(0)
                    val top = box.top.coerceAtLeast(0)
                    val width = box.width().coerceAtMost(screenshot.width - left)
                    val height = box.height().coerceAtMost(screenshot.height - top)

                    if (width <= 0 || height <= 0) {
                        cont.resume(null)
                        return@addOnSuccessListener
                    }

                    val cropped = Bitmap.createBitmap(screenshot, left, top, width, height)
                    cont.resume(cropped)
                }
                .addOnFailureListener { e ->
                    detector.close()
                    if (cont.isActive) cont.resumeWithException(e)
                }

            cont.invokeOnCancellation { detector.close() }
        }
}
