package app.ghostfit.domain

import android.app.Activity
import android.content.Context

/**
 * Domain interface for ad management (FR-016).
 * Abstracts AdMob for testability and clean architecture.
 */
interface AdProvider {
    fun initialize(context: Context, interstitialAdUnitId: String? = null)
    fun onGenerationCompleted()
    suspend fun shouldShowAd(): Boolean
    suspend fun showAdIfNeeded(activity: Activity, onComplete: () -> Unit)
    fun resetSessionCount()

    companion object {
        const val SHOW_AD_AFTER_GENERATIONS = 2
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    }
}
