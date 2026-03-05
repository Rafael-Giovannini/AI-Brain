package app.ghostfit.domain

import android.graphics.Bitmap

fun interface GarmentCropper {
    suspend fun crop(screenshot: Bitmap): Bitmap?
}
