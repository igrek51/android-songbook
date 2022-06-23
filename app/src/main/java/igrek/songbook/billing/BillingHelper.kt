package igrek.songbook.billing

import android.content.Context
import com.android.billingclient.api.*
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import android.app.Activity
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking


const val BILLING_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhCjsZRfLF2/6f0/5De3TAKzezDcx/Kozz3d+qsvsHS8Q3TPopC4ODQ8dCZG/6RHbtSMvqXmW7H1K/YqCYJ/cQ6LGwbe6QMUUDy9BV0l8yYaTFGqfkIhaHqbA95934K5DeAzXwnk6eFIWiRm5iTmlg9kNWwQafT3Yd8Es32xWcFh69NUAjIrlgS5xojjm5Tf8rksu1aF8uBwqxwvaCONpMYl9BABf9mzZ27ibiYvHSyAuPqyuQj1Ql4z4FZ8faF9oZrFkXCOD7iD1eoRIHUwelPvEAt5OIIYNyQpW4stv57RR7T8xgrj13GUOROozoaUyLswaR9aDsV51FUBvEoinkwIDAQAB"

const val PRODUCT_ID_NO_ADS = "no_ads_forever"


class BillingHelper(
        activity: LazyInject<Activity> = appFactory.activity,
        context: LazyInject<Context> = appFactory.context,
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val activity by LazyExtractor(activity)
    private val context by LazyExtractor(context)

    private val logger: Logger = LoggerFactory.logger
    private var billingClient: BillingClient? = null
    private val defaultScope: CoroutineScope
    private val knownInAppSKUs: List<String> = listOf(
            PRODUCT_ID_NO_ADS,
    )
    private val knowConsumableInAppKUSs: List<String> = listOf()
    private val skuStateMap: MutableMap<String, SkuState> = HashMap()
    private val skuDetailsMap: MutableMap<String, SkuDetails?> = HashMap()
    private val initChannel = Channel<Result<Unit>>(1)
    private val initJob: Job

    private enum class SkuState {
        UNKNOWN,
        UNPURCHASED,
        PENDING,
        PURCHASED,
        PURCHASED_AND_ACKNOWLEDGED,
    }

    init {
        defaultScope = GlobalScope

        initSkuFlows()
        initConnection()
        initJob = defaultScope.launch {
            initChannel.receive()
        }
    }

    private fun initSkuFlows() {
        for (sku: String in this.knownInAppSKUs) {
            skuStateMap[sku] = SkuState.UNKNOWN
            skuDetailsMap[sku] = null
        }
    }

    private fun initConnection() {
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        billingClient?.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        logger.debug("onBillingSetupFinished: $responseCode $debugMessage")
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // The billing client is ready. You can query purchases here.
                // This doesn't mean that your app is set up correctly in the console -- it just
                // means that you have a connection to the Billing service.
                defaultScope.launch {
                    querySkuDetailsAsync()
                    restorePurchases()
                    initChannel.trySendBlocking(Result.success(Unit))
                }
            }
        }
    }

    private suspend fun querySkuDetailsAsync() {
        if (!knownInAppSKUs.isNullOrEmpty()) {
            val skuDetailsResult = billingClient!!.querySkuDetails(
                    SkuDetailsParams.newBuilder()
                            .setType(BillingClient.SkuType.INAPP)
                            .setSkusList(knownInAppSKUs.toMutableList())
                            .build()
            )
            onSkuDetailsResponse(skuDetailsResult.billingResult, skuDetailsResult.skuDetailsList)
        }
    }

    private fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: List<SkuDetails>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                logger.info("onSkuDetailsResponse: $responseCode, $debugMessage")
                if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                    logger.error("onSkuDetailsResponse: " +
                        "Found null or empty SkuDetails. " +
                        "Check to see if the SKUs you requested are correctly published in the Google Play Console."
                    )
                } else {
                    for (skuDetails in skuDetailsList) {
                        val sku = skuDetails.sku
                        skuDetailsMap[sku] = skuDetails
                    }
                }
            }
        }
    }

    private suspend fun restorePurchases() {
        val purchasesResult = billingClient!!.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        val billingResult = purchasesResult.billingResult
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            handlePurchase(purchasesResult.purchasesList)
        } else {
            logger.debug("restorePurchases: BillingResult [${billingResult.responseCode}]: ${billingResult.debugMessage}")
        }

        for (sku in knownInAppSKUs) {
            when(skuStateMap[sku]) {
                SkuState.UNKNOWN -> {
                    skuStateMap[sku] = SkuState.UNPURCHASED
                }
            }
        }
    }

    fun launchBillingFlow(sku: String) {
        val skuDetails = skuDetailsMap[sku]
        if (skuDetails == null) {
            logger.error("SkuDetails not found for: $sku")
            return
        }
        val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
        billingClient!!.launchBillingFlow(activity, flowParams) // calls onPurchasesUpdated
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (null != purchases) {
                    handlePurchase(purchases)
                    return
                } else {
                    logger.debug("Null Purchase List Returned from OK response!")
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                logger.info("onPurchasesUpdated: User canceled the purchase")
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                logger.info("onPurchasesUpdated: The user already owns this item")
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                logger.error(
                    "onPurchasesUpdated: Developer error means that Google Play " +
                    "does not recognize the configuration. If you are just getting started, " +
                    "make sure you have configured the application correctly in the " +
                    "Google Play Console. The SKU product ID must match and the APK you " +
                    "are using must be signed with release keys."
                )
            }
            else -> {
                logger.debug("BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchase(purchases: List<Purchase>?) {
        if (null != purchases) {
            for (purchase in purchases) {
                // Global check to make sure all purchases are signed correctly.
                val purchaseState = purchase.purchaseState
                if (purchaseState == Purchase.PurchaseState.PURCHASED){
                    if (!isSignatureValid(purchase)) {
                        logger.error("Invalid signature. Check to make sure your public key is correct.")
                        continue
                    }

                    setSkuStateFromPurchase(purchase)

                    if (!purchase.isAcknowledged) {
                        defaultScope.launch {
                            for (sku in purchase.skus) {
                                if (knowConsumableInAppKUSs.contains(sku)) {
                                    // Consume item
                                }

                                // Acknowledge item and change its state
                                val billingResult = billingClient!!.acknowledgePurchase(
                                        AcknowledgePurchaseParams.newBuilder()
                                                .setPurchaseToken(purchase.purchaseToken)
                                                .build()
                                )
                                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                                    logger.error("Error acknowledging purchase: ${purchase.skus}")
                                } else {
                                    // purchase acknowledged
                                    skuStateMap[sku] = SkuState.PURCHASED_AND_ACKNOWLEDGED
                                }
                            }
                        }
                    }

                }  else {
                    setSkuStateFromPurchase(purchase)  // set not purchased
                }
            }
        } else {
            logger.debug("Empty purchase list.")
        }
    }

    // Set the state of every sku inside skuStateMap
    private fun setSkuStateFromPurchase(purchase: Purchase) {
        if (purchase.skus.isNullOrEmpty()) {
            logger.error("Empty list of skus")
            return
        }

        for (sku in purchase.skus) {
            val skuState = skuStateMap[sku]
            if (skuState == null) {
                logger.error("Unknown SKU $sku. Check to make sure SKU matches SKUS in the Play developer console.")
                continue
            }

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> skuStateMap[sku] = SkuState.PENDING
                Purchase.PurchaseState.UNSPECIFIED_STATE -> skuStateMap[sku] = SkuState.UNPURCHASED
                Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                    skuStateMap[sku] = SkuState.PURCHASED_AND_ACKNOWLEDGED
                } else {
                    skuStateMap[sku] = SkuState.PURCHASED
                }
                else -> logger.error("Purchase in unknown state: " + purchase.purchaseState)
            }
        }
    }

    fun isPurchased(sku: String): Boolean? {
        return skuStateMap[sku]
                ?.let { skuState -> skuState == SkuState.PURCHASED_AND_ACKNOWLEDGED }
    }

    fun syncIsPurchased(sku: String): Boolean? {
        runBlocking {
            initJob.join()
        }
        return skuStateMap[sku]
                ?.let { skuState -> skuState == SkuState.PURCHASED_AND_ACKNOWLEDGED }
    }

    fun getSkuTitle(sku: String): String? {
        return skuDetailsMap[sku]
                ?.let { skuDetails -> skuDetails.title }
    }

    fun getSkuPrice(sku: String): String? {
        return skuDetailsMap[sku]
                ?.let { skuDetails -> skuDetails.price }
    }

    fun getSkuDescription(sku: String): String? {
        return skuDetailsMap[sku]
                ?.let { skuDetails -> skuDetails.description }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(purchase.originalJson, purchase.signature)
    }

    override fun onBillingServiceDisconnected() {
        logger.info("Service disconnected")
    }

}