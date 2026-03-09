package app.ghostfit.domain

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryPurchasesParams
import app.ghostfit.data.local.SubscriptionStateDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.PurchaseType
import app.ghostfit.data.model.SubscriptionState
import app.ghostfit.data.model.UserProfile
import app.ghostfit.data.billing.BillingManagerImpl
import app.ghostfit.domain.PurchaseEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for BillingManager (Phase 5 — Story 4, FR-018).
 * Validates Google Play Billing wrapper: purchase handling, acknowledgement,
 * state sync, and purchase restoration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BillingManagerTest {

    private lateinit var billingClient: BillingClient
    private lateinit var subscriptionStateDao: SubscriptionStateDao
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var billingManager: BillingManagerImpl

    private val testProfile = UserProfile(id = "user-1", lgpdConsentGranted = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val queryPurchasesStubs = mutableMapOf<String, List<Purchase>>()
    private var queryPurchasesCallCount = 0

    @Before
    fun setup() {
        queryPurchasesStubs.clear()
        queryPurchasesCallCount = 0
        billingClient = mock()
        subscriptionStateDao = mock()
        userProfileDao = mock()

        billingManager = BillingManagerImpl(
            billingClient = billingClient,
            subscriptionStateDao = subscriptionStateDao,
            userProfileDao = userProfileDao,
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher)
        )
    }

    @Test
    fun `connect starts billing client connection`() {
        billingManager.connect()
        verify(billingClient).startConnection(any())
    }

    @Test
    fun `connect sets isConnected to true on successful setup`() {
        val listenerCaptor = argumentCaptor<BillingClientStateListener>()
        billingManager.connect()
        verify(billingClient).startConnection(listenerCaptor.capture())

        // Stub queryPurchasesAsync to avoid NPE during restorePurchases
        stubQueryPurchasesEmpty()

        val okResult = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.OK)
            .build()
        listenerCaptor.firstValue.onBillingSetupFinished(okResult)

        assertTrue(billingManager.isConnected.value)
    }

    @Test
    fun `disconnect ends connection and resets state`() {
        billingManager.disconnect()
        verify(billingClient).endConnection()
        assertFalse(billingManager.isConnected.value)
    }

    @Test
    fun `handlePurchase acknowledges unacknowledged monthly purchase`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            purchaseToken = "token-monthly-123"
        )

        // Stub acknowledge to succeed
        stubAcknowledgeSuccess()

        billingManager.handlePurchase(purchase)

        verify(billingClient).acknowledgePurchase(any(), any())
        verify(subscriptionStateDao).upsert(check { state ->
            assertEquals("user-1", state.userId)
            assertEquals("token-monthly-123", state.purchaseToken)
            assertEquals(BillingProvider.SKU_MONTHLY, state.productId)
            assertEquals(PurchaseType.MONTHLY, state.purchaseType)
            assertTrue(state.isActive)
            assertNotNull(state.acknowledgedAt)
        })
        verify(userProfileDao).updatePlanType(PlanType.PREMIUM.name)
    }

    @Test
    fun `handlePurchase acknowledges unacknowledged pack purchase and adds bonus tries`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_PACK_10,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            purchaseToken = "token-pack-456"
        )

        stubAcknowledgeSuccess()

        billingManager.handlePurchase(purchase)

        verify(subscriptionStateDao).upsert(check { state ->
            assertEquals(PurchaseType.PACK, state.purchaseType)
            assertEquals("token-pack-456", state.purchaseToken)
            assertTrue(state.isActive)
        })
        // Pack purchase adds bonus tries, does NOT set PREMIUM
        verify(userProfileDao, never()).updatePlanType(any())
        verify(userProfileDao).updateBonusTries(BillingProvider.PACK_TRIES_COUNT)
    }

    @Test
    fun `handlePurchase skips already acknowledged purchase`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = true,
            purchaseToken = "token-ack"
        )

        billingManager.handlePurchase(purchase)

        // Should NOT call acknowledgePurchase since already acknowledged
        verify(billingClient, never()).acknowledgePurchase(any(), any())
        // But should still sync state
        verify(subscriptionStateDao).upsert(any())
        verify(userProfileDao).updatePlanType(PlanType.PREMIUM.name)
    }

    @Test
    fun `handlePurchase ignores pending purchases`() = runTest {
        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PENDING,
            isAcknowledged = false,
            purchaseToken = "token-pending"
        )

        billingManager.handlePurchase(purchase)

        verify(subscriptionStateDao, never()).upsert(any())
        verify(userProfileDao, never()).updatePlanType(any())
    }

    @Test
    fun `handlePurchase aborts when acknowledge fails`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            purchaseToken = "token-fail"
        )

        // Stub acknowledge to fail
        doAnswer { invocation ->
            val listener = invocation.getArgument<AcknowledgePurchaseResponseListener>(1)
            listener.onAcknowledgePurchaseResponse(
                BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                    .build()
            )
        }.whenever(billingClient).acknowledgePurchase(any(), any())

        billingManager.handlePurchase(purchase)

        // Should NOT update local state when acknowledge fails
        verify(subscriptionStateDao, never()).upsert(any())
        verify(userProfileDao, never()).updatePlanType(any())
    }

    @Test
    fun `restorePurchases with active subscription activates premium`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = true,
            purchaseToken = "token-restore"
        )

        stubQueryPurchasesWithResult(BillingClient.ProductType.SUBS, listOf(purchase))
        stubQueryPurchasesWithResult(BillingClient.ProductType.INAPP, emptyList())

        billingManager.restorePurchases()

        verify(subscriptionStateDao).upsert(check { state ->
            assertTrue(state.isActive)
            assertEquals(PurchaseType.MONTHLY, state.purchaseType)
        })
        verify(userProfileDao).updatePlanType(PlanType.PREMIUM.name)
    }

    @Test
    fun `restorePurchases with no purchases reverts to free`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        whenever(subscriptionStateDao.getByUser("user-1")).thenReturn(
            SubscriptionState(userId = "user-1", isActive = true, purchaseType = PurchaseType.MONTHLY)
        )

        stubQueryPurchasesWithResult(BillingClient.ProductType.SUBS, emptyList())
        stubQueryPurchasesWithResult(BillingClient.ProductType.INAPP, emptyList())

        billingManager.restorePurchases()

        verify(subscriptionStateDao).upsert(check { state ->
            assertFalse(state.isActive)
        })
        verify(userProfileDao).updatePlanType(PlanType.FREE.name)
    }

    @Test
    fun `restorePurchases does nothing when no profile exists`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(null)

        stubQueryPurchasesWithResult(BillingClient.ProductType.SUBS, emptyList())
        stubQueryPurchasesWithResult(BillingClient.ProductType.INAPP, emptyList())

        billingManager.restorePurchases()

        verify(subscriptionStateDao, never()).upsert(any())
        verify(userProfileDao, never()).updatePlanType(any())
    }

    @Test
    fun `onPurchasesUpdated processes purchases on OK result`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_PACK_10,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = true,
            purchaseToken = "token-callback"
        )

        val okResult = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.OK)
            .build()

        billingManager.onPurchasesUpdated(okResult, listOf(purchase))

        // Give coroutine time to execute (UnconfinedTestDispatcher runs eagerly)
        verify(subscriptionStateDao).upsert(any())
    }

    @Test
    fun `onPurchasesUpdated ignores user-cancelled purchases`() = runTest {
        val cancelledResult = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.USER_CANCELED)
            .build()

        billingManager.onPurchasesUpdated(cancelledResult, null)

        verify(subscriptionStateDao, never()).upsert(any())
    }

    // --- Purchase Event Tests ---

    @Test
    fun `handlePurchase emits Success event on successful monthly purchase`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        stubAcknowledgeSuccess()

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            purchaseToken = "token-event-1"
        )

        billingManager.handlePurchase(purchase)

        val event = billingManager.purchaseEvent.value
        assertTrue(event is PurchaseEvent.Success)
        assertEquals(BillingProvider.SKU_MONTHLY, (event as PurchaseEvent.Success).productId)
        assertEquals(PurchaseType.MONTHLY, event.purchaseType)
    }

    @Test
    fun `handlePurchase emits Success event on successful pack purchase`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        stubAcknowledgeSuccess()

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_PACK_10,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = false,
            purchaseToken = "token-event-2"
        )

        billingManager.handlePurchase(purchase)

        val event = billingManager.purchaseEvent.value
        assertTrue(event is PurchaseEvent.Success)
        assertEquals(BillingProvider.SKU_PACK_10, (event as PurchaseEvent.Success).productId)
        assertEquals(PurchaseType.PACK, event.purchaseType)
    }

    @Test
    fun `onPurchasesUpdated emits Cancelled event on user cancel`() {
        val cancelledResult = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.USER_CANCELED)
            .build()

        billingManager.onPurchasesUpdated(cancelledResult, null)

        assertTrue(billingManager.purchaseEvent.value is PurchaseEvent.Cancelled)
    }

    @Test
    fun `onPurchasesUpdated emits Error event on billing error`() {
        val errorResult = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
            .setDebugMessage("Service unavailable")
            .build()

        billingManager.onPurchasesUpdated(errorResult, null)

        val event = billingManager.purchaseEvent.value
        assertTrue(event is PurchaseEvent.Error)
        assertEquals("Service unavailable", (event as PurchaseEvent.Error).message)
    }

    @Test
    fun `consumePurchaseEvent resets event to null`() = runTest {
        whenever(userProfileDao.getProfile()).thenReturn(testProfile)
        stubAcknowledgeSuccess()

        val purchase = createMockPurchase(
            productId = BillingProvider.SKU_MONTHLY,
            purchaseState = Purchase.PurchaseState.PURCHASED,
            isAcknowledged = true,
            purchaseToken = "token-consume"
        )

        billingManager.handlePurchase(purchase)
        assertNotNull(billingManager.purchaseEvent.value)

        billingManager.consumePurchaseEvent()
        assertNull(billingManager.purchaseEvent.value)
    }

    @Test
    fun `purchaseEvent starts as null`() {
        assertNull(billingManager.purchaseEvent.value)
    }

    // --- Helpers ---

    private fun createMockPurchase(
        productId: String,
        purchaseState: Int,
        isAcknowledged: Boolean,
        purchaseToken: String
    ): Purchase {
        val purchase = mock<Purchase>()
        whenever(purchase.products).thenReturn(listOf(productId))
        whenever(purchase.purchaseState).thenReturn(purchaseState)
        whenever(purchase.isAcknowledged).thenReturn(isAcknowledged)
        whenever(purchase.purchaseToken).thenReturn(purchaseToken)
        return purchase
    }

    private fun stubAcknowledgeSuccess() {
        doAnswer { invocation ->
            val listener = invocation.getArgument<AcknowledgePurchaseResponseListener>(1)
            listener.onAcknowledgePurchaseResponse(
                BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.OK)
                    .build()
            )
        }.whenever(billingClient).acknowledgePurchase(any(), any())
    }

    private fun stubQueryPurchasesEmpty() {
        stubQueryPurchasesWithResult(BillingClient.ProductType.SUBS, emptyList())
        stubQueryPurchasesWithResult(BillingClient.ProductType.INAPP, emptyList())
    }

    private fun stubQueryPurchasesWithResult(productType: String, purchases: List<Purchase>) {
        queryPurchasesStubs[productType] = purchases
        // Re-install the doAnswer with current map state; dispatches by call order
        // (restorePurchases always calls SUBS first, then INAPP)
        doAnswer { invocation ->
            val listener = invocation.getArgument<PurchasesResponseListener>(1)
            val returnPurchases = if (queryPurchasesCallCount % 2 == 0) {
                queryPurchasesStubs[BillingClient.ProductType.SUBS] ?: emptyList()
            } else {
                queryPurchasesStubs[BillingClient.ProductType.INAPP] ?: emptyList()
            }
            queryPurchasesCallCount++
            listener.onQueryPurchasesResponse(
                BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.OK)
                    .build(),
                returnPurchases
            )
        }.whenever(billingClient).queryPurchasesAsync(any<QueryPurchasesParams>(), any())
    }
}
