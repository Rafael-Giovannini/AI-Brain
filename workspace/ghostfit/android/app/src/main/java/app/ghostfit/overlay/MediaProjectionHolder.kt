package app.ghostfit.overlay

import android.content.Intent

/**
 * Singleton holding MediaProjection permission result data.
 * Populated by MainActivity after the user grants screen capture permission,
 * consumed by TryOnActivity to initialize ScreenCapture.
 */
object MediaProjectionHolder {

    @Volatile
    private var resultCode: Int = 0

    @Volatile
    private var data: Intent? = null

    val isAvailable: Boolean get() = data != null

    fun store(resultCode: Int, data: Intent) {
        this.resultCode = resultCode
        this.data = data
    }

    fun get(): Pair<Int, Intent>? {
        val d = data ?: return null
        return resultCode to d
    }

    fun clear() {
        resultCode = 0
        data = null
    }
}
