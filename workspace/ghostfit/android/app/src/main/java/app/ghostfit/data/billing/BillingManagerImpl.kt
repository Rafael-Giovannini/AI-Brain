package app.ghostfit.data.billing

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
import com.android.billingclient.api.queryProductDetails
import app.ghostfit.data.local.SubscriptionStateDao
import app.ghostfit.data.local.UserProfileDao
import app.ghostfit.data.model.PlanType
import app.ghostfit.data.model.PurchaseType
import app.ghostfit.data.model.SubscriptionState
import app.ghostfit.domain.BillingProvider
import app.ghostfit.domain.PurchaseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Google Play Billing Library 8.3.0 implementation of [BillingProvider].
 * Manages subscriptions (monthly) and one-time packs for GhostFit.
 * Keeps SubscriptionState and UserProfile.planType in sync with Play Billing.
 *
 * FR-018: Google Play Billing integration with purchase restoration.
 */
class BillingManagerImpl(
    private val billingClient: BillingClient,
    private val subscriptionStateDao: SubscriptionStateDao,
    private val userProfileDao: UserProfileDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BillingProvider, PurchasesUpdatedListener {

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    override val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _connectionState = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val _purchaseEvent = MutableStateFlow<PurchaseEvent?>(null)
    override val purchaseEvent: StateFlow<PurchaseEvent?> = _purchaseEvent.asStateFlow()

    override fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
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

    override fun disconnect() {
        billingClient.endConnection()
        _connectionState.value = false
    }

    suspend fun queryProducts() {
        val subParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProvider.SKU_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(BillingProvider.SKU_PACK_10)
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
    ): List<ProductDetails> {
        val result = billingClient.queryProductDetails(params)
        return if (result.billingResult.responseCode == BillingResponseCode.OK) {
            result.productDetailsList ?: emptyList()
        } else {
            emptyList()
        }
    }

    override fun launchPurchaseFlow(activity: Activity, productId: String): BillingResult {
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

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingResponseCode.OK -> {
                if (purchases != null) {
                    scope.launch {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                _purchaseEvent.value = PurchaseEvent.Cancelled
            }
            else -> {
                _purchaseEvent.value = PurchaseEvent.Error(
                    result.debugMessage ?: "Erro no pagamento (código ${result.responseCode})"
                )
            }
        }
    }

    override fun consumePurchaseEvent() {
        _purchaseEvent.value = null
    }

    internal suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {
            val ackResult = acknowledgePurchase(purchase.purchaseToken)
            if (ackResult.responseCode != BillingResponseCode.OK) return
        }

        val productId = purchase.products.firstOrNull() ?: return
        val purchaseType = when (productId) {
            BillingProvider.SKU_MONTHLY -> PurchaseType.MONTHLY
            BillingProvider.SKU_PACK_10 -> PurchaseType.PACK
            else -> PurchaseType.NONE
        }

        val userId = userProfileDao.getProfile()?.id ?: return

        val subscriptionState = SubscriptionState(
            userId = userId,
            purchaseToken = purchase.purchaseToken,
            productId = productId,
            purchaseType = purchaseType,
            isActive = true,
            expiresAt = if (purchaseType == PurchaseType.MONTHLY) null else null,
            acknowledgedAt = System.currentTimeMillis()
        )

        subscriptionStateDao.upsert(subscriptionState)

        when (purchaseType) {
            PurchaseType.MONTHLY -> {
                userProfileDao.updatePlanType(PlanType.PREMIUM.name)
            }
            PurchaseType.PACK -> {
                val currentProfile = userProfileDao.getProfile()
                if (currentProfile != null) {
                    userProfileDao.updateBonusTries(currentProfile.bonusTries + BillingProvider.PACK_TRIES_COUNT)
                }
            }
            else -> { /* NONE — ignore */ }
        }
        _purchaseEvent.value = PurchaseEvent.Success(productId, purchaseType)
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

    override suspend fun restorePurchases() {
        val subPurchases = queryPurchasesAsync(BillingClient.ProductType.SUBS)
        val inAppPurchases = queryPurchasesAsync(BillingClient.ProductType.INAPP)

        val allPurchases = subPurchases + inAppPurchases
        val hasActive = allPurchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }

        if (hasActive) {
            for (purchase in allPurchases) {
                handlePurchase(purchase)
            }
        } else {
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
        billingClient.queryPurchasesAsync(params) { result: BillingResult, purchases: List<Purchase> ->
            if (result.responseCode == BillingResponseCode.OK) {
                cont.resume(purchases)
            } else {
                cont.resume(emptyList<Purchase>())
            }
        }
    }
}
