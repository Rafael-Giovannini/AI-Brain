package app.ghostfit.data.ads

import android.app.Activity
import android.content.Context
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.PlanType
import app.ghostfit.domain.AdProvider
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdMob implementation of [AdProvider] for free-tier users.
 *
 * FR-016: Show ads between generations (after 2nd free try-on),
 * NEVER before displaying a result. Premium users never see ads.
 *
 * Usage flow:
 * 1. Call [initialize] once from Activity/Application
 * 2. Call [onGenerationCompleted] after each successful try-on
 * 3. Call [showAdIfNeeded] before starting a new generation
 */
class AdManagerImpl(
    private val userProfileDao: UserProfileDao
) : AdProvider {

    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var generationCount = 0
    private var adUnitId = AdProvider.TEST_INTERSTITIAL_ID

    override fun initialize(context: Context, interstitialAdUnitId: String?) {
        if (interstitialAdUnitId != null) {
            adUnitId = interstitialAdUnitId
        }
        MobileAds.initialize(context) {}
        loadInterstitial(context)
    }

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

    override fun onGenerationCompleted() {
        generationCount++
    }

    override suspend fun shouldShowAd(): Boolean {
        val profile = userProfileDao.getProfile() ?: return false
        if (profile.planType == PlanType.PREMIUM) return false
        return generationCount >= AdProvider.SHOW_AD_AFTER_GENERATIONS
    }

    override suspend fun showAdIfNeeded(activity: Activity, onComplete: () -> Unit) {
        if (!shouldShowAd()) {
            onComplete()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            loadInterstitial(activity)
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                generationCount = 0
                loadInterstitial(activity)
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

    override fun resetSessionCount() {
        generationCount = 0
    }

    /** Visible for testing. */
    internal fun getGenerationCount(): Int = generationCount
}
