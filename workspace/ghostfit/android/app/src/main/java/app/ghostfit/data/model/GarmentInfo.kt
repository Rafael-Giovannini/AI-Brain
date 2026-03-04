package app.ghostfit.data.model

import android.graphics.Bitmap

enum class GarmentCategory { TOP, BOTTOM, DRESS, OUTERWEAR }

data class GarmentInfo(
    val category: GarmentCategory,
    val color: String? = null,
    val description: String? = null,
    val confidence: Float,
    val croppedImage: Bitmap? = null
)
