package app.ghostfit.ui.tryon

import app.ghostfit.domain.TryOnSession

/**
 * Singleton to share TryOnSession state between OverlayService and TryOnActivity.
 * Bitmaps can't be passed via Intent, so we use this holder.
 */
object TryOnSessionHolder {

    @Volatile
    var currentSession: TryOnSession? = null
        private set

    fun update(session: TryOnSession) {
        currentSession = session
    }

    fun clear() {
        currentSession = null
    }
}
