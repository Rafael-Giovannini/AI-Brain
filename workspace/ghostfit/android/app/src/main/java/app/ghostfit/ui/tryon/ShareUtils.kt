package app.ghostfit.ui.tryon

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private const val TAG = "ShareUtils"

/**
 * Share the generated try-on image via Android share sheet.
 */
fun shareTryOnImage(context: android.content.Context, bitmap: Bitmap) {
    try {
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "ghostfit_tryon_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Experimentei com GhostFit! \uD83D\uDC7B")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar via"))
    } catch (e: Exception) {
        Log.w(TAG, "Sharing failed", e)
    }
}
