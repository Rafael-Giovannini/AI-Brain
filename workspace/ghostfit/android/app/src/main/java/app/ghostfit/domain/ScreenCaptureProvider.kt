package app.ghostfit.domain

import android.graphics.Bitmap

fun interface ScreenCaptureProvider {
    suspend fun capture(): Bitmap
}
