package app.ghostfit.domain

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Convert a Bitmap to a Base64-encoded JPEG string.
 * Shared utility to avoid duplication across domain classes.
 */
internal fun Bitmap.toBase64Jpeg(quality: Int = 85): String {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
}
