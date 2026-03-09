package app.ghostfit.domain

/**
 * Domain interface for ad management (FR-016).
 * Abstracts AdMob for testability and clean architecture.
 * Uses Any for framework types to keep domain layer framework-agnostic.
 */
interface AdProvider {
    fun initialize(context: Any, interstitialAdUnitId: String? = null)
    fun onGenerationCompleted()
    suspend fun shouldShowAd(): Boolean
    suspend fun showAdIfNeeded(activity: Any, onComplete: () -> Unit)
    fun resetSessionCount()

    companion object {
        const val SHOW_AD_AFTER_GENERATIONS = 2
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    }
}
