package app.ghostfit.domain

import android.app.Activity
import android.content.Context
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.PlanType
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Manages AdMob interstitial ads for free-tier users.
 *
 * FR-016: Show ads between generations (after 2nd free try-on),
 * NEVER before displaying a result. Premium users never see ads.
 *
 * Usage flow:
 * 1. Call [initialize] once from Activity/Application
 * 2. Call [onGenerationCompleted] after each successful try-on
 * 3. Call [showAdIfNeeded] before starting a new generation —
 *    it shows the interstitial if threshold is met, then calls onComplete
 */
class AdManager(
    private val userProfileDao: UserProfileDao
) {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var generationCount = 0
    private var adUnitId = TEST_INTERSTITIAL_ID

    /**
     * Initialize Mobile Ads SDK and pre-load first interstitial.
     */
    fun initialize(context: Context, interstitialAdUnitId: String? = null) {
        if (interstitialAdUnitId != null) {
            adUnitId = interstitialAdUnitId
        }
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

    /**
     * Pre-load an interstitial ad so it's ready when needed.
     */
    private fun loadInterstitial(context: Context) {
        if (isLoading || interstitialAd != null) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Track a completed generation. Call after each successful try-on (status = DONE).
     */
    fun onGenerationCompleted() {
        generationCount++
    }

    /**
     * Check if an ad should be shown before the next generation.
     * Returns true if the user is free-tier and has completed enough generations.
     */
    suspend fun shouldShowAd(): Boolean {
        val profile = userProfileDao.getProfile() ?: return false
        if (profile.planType == PlanType.PREMIUM) return false
        return generationCount >= SHOW_AD_AFTER_GENERATIONS
    }

    /**
     * Show interstitial ad if conditions are met, then invoke [onComplete].
     * If no ad is available or user is premium, [onComplete] is called immediately.
     *
     * This should be called BEFORE starting a new generation (between generations).
     * It NEVER blocks result display.
     */
    suspend fun showAdIfNeeded(activity: Activity, onComplete: () -> Unit) {
        if (!shouldShowAd()) {
            onComplete()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            // No ad loaded — proceed without blocking
            loadInterstitial(activity)
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                generationCount = 0 // Reset counter after showing ad
                loadInterstitial(activity) // Pre-load next ad
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial(activity)
                onComplete()
            }
        }
        ad.show(activity)
    }

    /** Reset session state (e.g., on app restart). */
    fun resetSessionCount() {
        generationCount = 0
    }

    /** Visible for testing. */
    internal fun getGenerationCount(): Int = generationCount

    companion object {
        /** Show interstitial after this many completed generations. */
        const val SHOW_AD_AFTER_GENERATIONS = 2

        /** Google's test interstitial ad unit ID (safe for development). */
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    }
}
