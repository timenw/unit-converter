package com.unitconverter.billing

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*

class BillingManager(private val application: Application) : PurchasesUpdatedListener {

    @Volatile
    private var _isPremium = false

    val isPremium: Boolean get() = _isPremium

    private var billingClient: BillingClient? = null
    private var premiumCallback: ((Boolean) -> Unit)? = null

    companion object {
        private const val TAG = "BillingManager"
        const val SKU_REMOVE_ADS = "remove_ads"
        private const val PREFS_NAME = "unit_converter_prefs"
        private const val KEY_PREMIUM = "is_premium"
    }

    init {
        // Restore premium state from SharedPreferences
        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isPremium = prefs.getBoolean(KEY_PREMIUM, false)
        if (_isPremium) {
            Log.d(TAG, "Restored premium state from prefs")
        }
    }

    private fun savePremiumState(premium: Boolean) {
        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PREMIUM, premium).apply()
    }

    fun setPremiumCallback(callback: (Boolean) -> Unit) {
        premiumCallback = callback
    }

    fun startConnection() {
        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected")
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected")
            }
        })
    }

    private fun queryPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPremium = purchases.any { it.products.contains(SKU_REMOVE_ADS) }
                _isPremium = hasPremium
                savePremiumState(hasPremium)
                premiumCallback?.invoke(hasPremium)
                Log.d(TAG, "Premium: $hasPremium")
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SKU_REMOVE_ADS)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        billingClient?.queryProductDetailsAsync(params) { result, productDetailsList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                    )
                    .build()
                billingClient?.launchBillingFlow(activity, flowParams)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.products.contains(SKU_REMOVE_ADS) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.acknowledgePurchase(ackParams) { ackResult ->
                            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                _isPremium = true
                                savePremiumState(true)
                                premiumCallback?.invoke(true)
                                Log.d(TAG, "Purchase acknowledged")
                            }
                        }
                    } else {
                        _isPremium = true
                        savePremiumState(true)
                        premiumCallback?.invoke(true)
                    }
                }
            }
        }
    }

    fun endConnection() {
        billingClient?.endConnection()
    }
}
