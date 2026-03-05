package app.ghostfit.domain

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import app.ghostfit.data.local.SubscriptionStateDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.PurchaseType
import app.ghostfit.data.model.SubscriptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Wraps Google Play Billing Library 8.3.0.
 * Manages subscriptions (monthly) and one-time packs for GhostFit.
 * Keeps SubscriptionState and UserProfile.planType in sync with Play Billing.
 *
 * FR-018: Google Play Billing integration with purchase restoration.
 */
class BillingManager(
    private val billingClient: BillingClient,
    private val subscriptionStateDao: SubscriptionStateDao,
    private val userProfileDao: UserProfileDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PurchasesUpdatedListener {

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _connectionState = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _connectionState.asStateFlow()

    /**
     * Connect to Google Play Billing and query available products.
     * Should be called once from Application or MainActivity.
     */
    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetup(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) {
                    _connectionState.value = true
                    scope.launch {
                        queryProducts()
                        restorePurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = false
            }
        })
    }

    fun disconnect() {
        billingClient.endConnection()
        _connectionState.value = false
    }

    /**
     * Query available products (subscription + one-time pack) from Play Console.
     */
    suspend fun queryProducts() {
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SKU_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SKU_PACK_10)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        val subsResult = queryProductDetailsAsync(subParams)
        val inAppResult = queryProductDetailsAsync(inAppParams)

        val details = mutableMapOf<String, ProductDetails>()
        subsResult.forEach { details[it.productId] = it }
        inAppResult.forEach { details[it.productId] = it }
        _productDetails.value = details
    }

    private suspend fun queryProductDetailsAsync(
        params: QueryProductDetailsParams
    ): List<ProductDetails> = suspendCancellableCoroutine { cont ->
        billingClient.queryProductDetailsAsync(params) { result, detailsList ->
            if (result.responseCode == BillingResponseCode.OK) {
                cont.resume(detailsList ?: emptyList())
            } else {
                cont.resume(emptyList())
            }
        }
    }

    /**
     * Launch billing flow for a product. Returns the BillingResult.
     */
    fun launchPurchaseFlow(activity: Activity, productId: String): BillingResult {
        val details = _productDetails.value[productId]
            ?: return BillingResult.newBuilder()
                .setResponseCode(BillingResponseCode.ITEM_UNAVAILABLE)
                .setDebugMessage("Product $productId not found")
                .build()

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (offerToken != null) {
            productDetailsParams.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams.build()))
            .build()

        return billingClient.launchBillingFlow(activity, flowParams)
    }

    /**
     * Called by Play Billing when a purchase is updated.
     */
    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingResponseCode.OK && purchases != null) {
            scope.launch {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    /**
     * Process a purchase: acknowledge if needed, then sync local state.
     */
    internal suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        // Acknowledge if not yet acknowledged (Google requires within 3 days)
        if (!purchase.isAcknowledged) {
            val ackResult = acknowledgePurchase(purchase.purchaseToken)
            if (ackResult.responseCode != BillingResponseCode.OK) return
        }

        // Determine purchase type from product IDs
        val productId = purchase.products.firstOrNull() ?: return
        val purchaseType = when (productId) {
            SKU_MONTHLY -> PurchaseType.MONTHLY
            SKU_PACK_10 -> PurchaseType.PACK
            else -> PurchaseType.NONE
        }

        // Sync to local DB
        val userId = userProfileDao.getProfile()?.id ?: return

        val subscriptionState = SubscriptionState(
            userId = userId,
            purchaseToken = purchase.purchaseToken,
            productId = productId,
            purchaseType = purchaseType,
            isActive = true,
            expiresAt = if (purchaseType == PurchaseType.MONTHLY) {
                // Subscription: auto-renews, Play manages expiry
                // We mark it active; restorePurchases will deactivate if expired
                null
            } else null,
            acknowledgedAt = System.currentTimeMillis()
        )

        subscriptionStateDao.upsert(subscriptionState)
        userProfileDao.updatePlanType(PlanType.PREMIUM.name)
    }

    private suspend fun acknowledgePurchase(
        purchaseToken: String
    ): BillingResult = suspendCancellableCoroutine { cont ->
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { result ->
            cont.resume(result)
        }
    }

    /**
     * Restore purchases on app startup or reinstall.
     * Queries both subscriptions and in-app purchases.
     */
    suspend fun restorePurchases() {
        val subPurchases = queryPurchasesAsync(BillingClient.ProductType.SUBS)
        val inAppPurchases = queryPurchasesAsync(BillingClient.ProductType.INAPP)

        val allPurchases = subPurchases + inAppPurchases
        val hasActive = allPurchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }

        if (hasActive) {
            for (purchase in allPurchases) {
                handlePurchase(purchase)
            }
        } else {
            // No active purchases — revert to FREE if currently PREMIUM
            val userId = userProfileDao.getProfile()?.id ?: return
            val current = subscriptionStateDao.getByUser(userId)
            if (current?.isActive == true) {
                subscriptionStateDao.upsert(current.copy(isActive = false))
                userProfileDao.updatePlanType(PlanType.FREE.name)
            }
        }
    }

    private suspend fun queryPurchasesAsync(
        productType: String
    ): List<Purchase> = suspendCancellableCoroutine { cont ->
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK) {
                cont.resume(purchases)
            } else {
                cont.resume(emptyList())
            }
        }
    }

    companion object {
        const val SKU_MONTHLY = "ghostfit_premium_monthly"
        const val SKU_PACK_10 = "ghostfit_pack_10"
    }
}
