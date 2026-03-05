package app.ghostfit.domain

import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.UserProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests for AdManager logic (FR-016).
 * Verifies:
 * - Ads only shown to free users
 * - Ads triggered after SHOW_AD_AFTER_GENERATIONS completions
 * - Premium users never see ads
 * - Counter resets correctly
 */
class AdManagerTest {

    private lateinit var userProfileDao: UserProfileDao
    private lateinit var adManager: AdManager

    private val freeProfile = UserProfile(
        id = "user-1",
        planType = PlanType.FREE,
        lgpdConsentGranted = true
    )

    private val premiumProfile = UserProfile(
        id = "user-2",
        planType = PlanType.PREMIUM,
        lgpdConsentGranted = true
    )

    @Before
    fun setup() {
        userProfileDao = mock()
        adManager = AdManager(userProfileDao)
    }

    @Test
    fun `shouldShowAd returns false when no profile exists`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(null)
        assertFalse(adManager.shouldShowAd())
    }

    @Test
    fun `shouldShowAd returns false for premium user`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(premiumProfile)
        // Even with many generations, premium never sees ads
        repeat(10) { adManager.onGenerationCompleted() }
        assertFalse(adManager.shouldShowAd())
    }

    @Test
    fun `shouldShowAd returns false for free user before threshold`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(freeProfile)
        // 0 generations — no ad
        assertFalse(adManager.shouldShowAd())
        // 1 generation — still below threshold
        adManager.onGenerationCompleted()
        assertFalse(adManager.shouldShowAd())
    }

    @Test
    fun `shouldShowAd returns true for free user at threshold`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(freeProfile)
        repeat(AdManager.SHOW_AD_AFTER_GENERATIONS) {
            adManager.onGenerationCompleted()
        }
        assertTrue(adManager.shouldShowAd())
    }

    @Test
    fun `shouldShowAd returns true for free user above threshold`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(freeProfile)
        repeat(AdManager.SHOW_AD_AFTER_GENERATIONS + 3) {
            adManager.onGenerationCompleted()
        }
        assertTrue(adManager.shouldShowAd())
    }

    @Test
    fun `onGenerationCompleted increments counter`() {
        assertEquals(0, adManager.getGenerationCount())
        adManager.onGenerationCompleted()
        assertEquals(1, adManager.getGenerationCount())
        adManager.onGenerationCompleted()
        assertEquals(2, adManager.getGenerationCount())
    }

    @Test
    fun `resetSessionCount resets generation counter to zero`() {
        adManager.onGenerationCompleted()
        adManager.onGenerationCompleted()
        assertEquals(2, adManager.getGenerationCount())
        adManager.resetSessionCount()
        assertEquals(0, adManager.getGenerationCount())
    }

    @Test
    fun `SHOW_AD_AFTER_GENERATIONS is 2`() {
        assertEquals(2, AdManager.SHOW_AD_AFTER_GENERATIONS)
    }

    @Test
    fun `shouldShowAd after reset returns false for free user`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(freeProfile)
        repeat(AdManager.SHOW_AD_AFTER_GENERATIONS) {
            adManager.onGenerationCompleted()
        }
        assertTrue(adManager.shouldShowAd())

        adManager.resetSessionCount()
        assertFalse(adManager.shouldShowAd())
    }
}
