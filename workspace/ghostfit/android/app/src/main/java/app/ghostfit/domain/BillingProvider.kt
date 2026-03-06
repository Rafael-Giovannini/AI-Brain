package app.ghostfit.domain

import android.app.Activity
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import app.ghostfit.data.model.PurchaseType
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the outcome of a purchase attempt.
 * Observed by UpgradeScreen to react to purchase completion.
 */
sealed class PurchaseEvent {
    data class Success(val productId: String, val purchaseType: PurchaseType) : PurchaseEvent()
    data class Error(val message: String) : PurchaseEvent()
    object Cancelled : PurchaseEvent()
}

/**
 * Domain interface for billing operations (FR-018).
 * Abstracts Google Play Billing for testability and clean architecture.
 */
interface BillingProvider {
    val productDetails: StateFlow<Map<String, ProductDetails>>
    val isConnected: StateFlow<Boolean>
    val purchaseEvent: StateFlow<PurchaseEvent?>

    fun connect()
    fun disconnect()
    fun launchPurchaseFlow(activity: Activity, productId: String): BillingResult
    fun consumePurchaseEvent()
    suspend fun restorePurchases()
    fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?)

    companion object {
        const val SKU_MONTHLY = "ghostfit_premium_monthly"
        const val SKU_PACK_10 = "ghostfit_pack_10"
        const val PACK_TRIES_COUNT = 10
    }
}
