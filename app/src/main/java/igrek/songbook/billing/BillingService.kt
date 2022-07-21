package igrek.songbook.billing

import android.content.Context
import com.android.billingclient.api.*
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import android.app.Activity
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlin.RuntimeException


const val BILLING_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhCjsZRfLF2/6f0/5De3TAKzezDcx/Kozz3d+qsvsHS8Q3TPopC4ODQ8dCZG/6RHbtSMvqXmW7H1K/YqCYJ/cQ6LGwbe6QMUUDy9BV0l8yYaTFGqfkIhaHqbA95934K5DeAzXwnk6eFIWiRm5iTmlg9kNWwQafT3Yd8Es32xWcFh69NUAjIrlgS5xojjm5Tf8rksu1aF8uBwqxwvaCONpMYl9BABf9mzZ27ibiYvHSyAuPqyuQj1Ql4z4FZ8faF9oZrFkXCOD7iD1eoRIHUwelPvEAt5OIIYNyQpW4stv57RR7T8xgrj13GUOROozoaUyLswaR9aDsV51FUBvEoinkwIDAQAB"

const val PRODUCT_ID_NO_ADS = "no_ads_forever"
const val PRODUCT_ID_DONATE_1_BEER = "donate_1_beer"


@Suppress("DEPRECATION")
class BillingService(
    activity: LazyInject<Activity> = appFactory.activity,
    context: LazyInject<Context> = appFactory.context,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
    preferencesService: LazyInject<PreferencesService> = appFactory.preferencesService,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    adService: LazyInject<AdService> = appFactory.adService,
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val activity by LazyExtractor(activity)
    private val context by LazyExtractor(context)
    private val preferencesState by LazyExtractor(preferencesState)
    private val preferencesService by LazyExtractor(preferencesService)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val adService by LazyExtractor(adService)

    private val logger: Logger = LoggerFactory.logger
    private var billingClient: BillingClient? = null
    private val defaultScope: CoroutineScope
    private val knownAllSKUs: List<String> = listOf(
        PRODUCT_ID_NO_ADS,
        PRODUCT_ID_DONATE_1_BEER,
    )
    private val knownConsumableInAppKUSs: List<String> = listOf()
    private val skuStateMap: MutableMap<String, SkuState> = HashMap()
    private val skuDetailsMap: MutableMap<String, SkuDetails?> = HashMap()
    private val skuAmountsMap: MutableMap<String, Long> = HashMap()
    private val initChannel = Channel<Result<Boolean>>(1)
    private val initJob: Job
    val purchaseEventsSubject = PublishSubject.create<Boolean>()

    private enum class SkuState {
        UNKNOWN,
        UNPURCHASED,
        PENDING,
        PURCHASED,
        PURCHASED_AND_ACKNOWLEDGED,
    }

    init {
        defaultScope = GlobalScope
        initJob = defaultScope.launch {
            initChannel.receive()
        }

        for (sku: String in this.knownAllSKUs) {
            skuStateMap[sku] = SkuState.UNKNOWN
            skuDetailsMap[sku] = null
            skuAmountsMap[sku] = 0
        }

        initConnection()
    }

    private fun initConnection() {
        try {
            logger.debug("initializing Billing Service")
            billingClient = BillingClient.newBuilder(context)
                    .setListener(this)
                    .enablePendingPurchases()
                    .build()
            billingClient?.startConnection(this)
        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
            initChannel.trySend(Result.success(false))
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        try {
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    // The billing client is ready. You can query purchases here.
                    // This doesn't mean that your app is set up correctly in the console -- it just
                    // means that you have a connection to the Billing service.
                    defaultScope.launch {
                        try {
                            querySkuDetails()
                            restorePurchases()
                            initChannel.trySendBlocking(Result.success(true))
                            logger.debug("Billing service initialized")

                        } catch (t: Throwable) {
                            UiErrorHandler().handleError(t, R.string.error_purchase_error)
                            initChannel.trySend(Result.success(false))
                        }
                    }
                }
                else -> {
                    logger.error("Billing setup response: $responseCode $debugMessage")
                    initChannel.trySend(Result.success(false))
                }
            }

        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
            initChannel.trySend(Result.success(false))
        }
    }

    private suspend fun querySkuDetails() {
        if (knownAllSKUs.isEmpty())
            return


        if (knownAllSKUs.isNotEmpty()) {
            val skuDetailsResult = billingClient!!.querySkuDetails(
                    SkuDetailsParams.newBuilder()
                            .setType(BillingClient.SkuType.INAPP)
                            .setSkusList(knownAllSKUs.toMutableList())
                            .build()
            )
            onProductDetailsResponse(skuDetailsResult.billingResult, skuDetailsResult.skuDetailsList)
        }
    }

    private fun onProductDetailsResponse(billingResult: BillingResult, skuDetailsList: List<SkuDetails>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                    UiErrorHandler().handleError(RuntimeException("Found empty product details"), R.string.error_purchase_error)
                } else {
                    for (skuDetails in skuDetailsList) {
                        val sku = skuDetails.sku
                        skuDetailsMap[sku] = skuDetails
                    }
                }
            }
            else -> {
                UiErrorHandler().handleError(RuntimeException("Product details: $responseCode $debugMessage"), R.string.error_purchase_error)
            }
        }
    }

    fun callRestorePurchases() {
        uiInfoService.showInfo(R.string.billing_restoring_purchases)
        defaultScope.launch {
            restorePurchases()
            purchaseEventsSubject.onNext(true)
            uiInfoService.showInfo(R.string.billing_purchases_restored)
        }
    }

    private suspend fun restorePurchases() {
        try{

            for (sku: String in this.knownAllSKUs) {
                skuAmountsMap[sku] = 0
            }

            val purchasesResult = billingClient!!.queryPurchasesAsync(BillingClient.SkuType.INAPP)
            val billingResult = purchasesResult.billingResult
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchaseList(purchasesResult.purchasesList)
            } else {
                logger.debug("restorePurchases: BillingResult [${billingResult.responseCode}]: ${billingResult.debugMessage}")
            }

            for (sku in knownAllSKUs) {
                if (skuStateMap[sku] == SkuState.UNKNOWN) {
                    skuStateMap[sku] = SkuState.UNPURCHASED
                }
            }

        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
        }
    }

    fun launchBillingFlow(sku: String) {
        uiInfoService.showInfo(R.string.billing_starting_purchase)
        try {
            val skuDetails = skuDetailsMap[sku] ?: throw RuntimeException("Product SKU Details not found for $sku")
            val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build()
            billingClient?.launchBillingFlow(activity, flowParams) // calls onPurchasesUpdated

        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        try {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (null != purchases) {
                        processPurchaseList(purchases)
                        purchaseEventsSubject.onNext(true)
                        uiInfoService.showInfo(R.string.billing_thanks_for_purchase)
                        return
                    } else {
                        logger.debug("Null Purchase List Returned from OK response!")
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    uiInfoService.showInfo(R.string.billing_user_canceled_purchase)
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    uiInfoService.showInfo(R.string.billing_user_already_owns_item)
                }
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                    throw RuntimeException("Billing Response: Invalid arguments provided to the API")
                    // Developer error means that Google Play does not recognize the configuration.
                    // The SKU product ID must match and the APK you are using must be signed with release keys."
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    throw RuntimeException("Billing API version is not supported for the type requested")
                }
                BillingClient.BillingResponseCode.ERROR -> {
                    throw RuntimeException("Fatal error during the API action. ${billingResult.debugMessage}")
                }
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                    throw RuntimeException("Requested feature is not supported by Play Store on the current device")
                }
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                    throw RuntimeException("Failure to consume since item is not owned.")
                }
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                    throw RuntimeException("Requested product is not available for purchase")
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    throw RuntimeException("Play Store service is not connected now - potentially transient state.")
                }
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> {
                    throw RuntimeException("The request has reached the maximum timeout before Google Play responds.")
                }
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                    throw RuntimeException("Network connection is down.")
                }
                else -> {
                    throw RuntimeException("Billing Response Code ${billingResult.responseCode} ${billingResult.debugMessage}")
                }
            }

        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
        }
    }

    private fun processPurchaseList(purchases: List<Purchase>?) {
        if (purchases == null) {
            logger.debug("Empty purchase list.")
            return
        }

        for (purchase in purchases) {
            processPurchase(purchase)
        }
    }

    private fun processPurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!isSignatureValid(purchase)) {
                throw RuntimeException("Invalid purchase signature. Signature is not valid with the public key.")
            }

            runBlocking {
                for (sku in purchase.skus) {

                    skuAmountsMap[sku] = (skuAmountsMap[sku] ?: 0) + 1

                    val isConsumable = knownConsumableInAppKUSs.contains(sku)

                    if (isConsumable) {
                        consumePurchase(purchase)

                    } else if (!purchase.isAcknowledged) {

                        val billingResult = billingClient!!.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                        )
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            // purchase acknowledged
                            skuStateMap[sku] = SkuState.PURCHASED_AND_ACKNOWLEDGED
                            savePurchaseData(sku)

                        } else {
                            UiErrorHandler().handleError(
                                RuntimeException("Error acknowledging purchase: ${purchase.skus}"),
                                R.string.error_purchase_error,
                            )
                        }
                    }

                }
            }
        }

        setSkuStateFromPurchase(purchase)
    }

    // Set the state of every sku inside skuStateMap
    private fun setSkuStateFromPurchase(purchase: Purchase) {
        if (purchase.skus.isEmpty()) {
            logger.error("Empty list of SKUs in a purchase")
            return
        }

        for (sku in purchase.skus) {
            val skuState = skuStateMap[sku]
            if (skuState == null) {
                logger.error("Unknown Product SKU in a purchase: $sku.")
                continue
            }

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> skuStateMap[sku] = SkuState.PENDING
                Purchase.PurchaseState.UNSPECIFIED_STATE -> skuStateMap[sku] = SkuState.UNPURCHASED
                Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                    skuStateMap[sku] = SkuState.PURCHASED_AND_ACKNOWLEDGED
                    savePurchaseData(sku)
                } else {
                    skuStateMap[sku] = SkuState.PURCHASED
                }
                else -> logger.error("Purchase in unknown state: ${purchase.purchaseState}")
            }
        }
    }

    private fun savePurchaseData(sku: String) {
        when(sku) {
            PRODUCT_ID_NO_ADS -> {
                if (!preferencesState.purchasedAdFree) {
                    preferencesState.purchasedAdFree = true
                    preferencesService.saveAll()
                    adService.hideAdBanner()
                    purchaseEventsSubject.onNext(true)
                    logger.info("Saving Purchase in preferences data, SKU: $sku")
                }
            }
        }
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        val consumePurchaseResult = billingClient!!.consumePurchase(
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        )
        if (consumePurchaseResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Since we've consumed the purchase
            for (sku in purchase.skus) {
                logger.info("Purchase consumed: $sku")
                skuStateMap[sku] = SkuState.UNPURCHASED
            }
        } else {
            UiErrorHandler().handleError(
                RuntimeException("Error while consuming purchase ${consumePurchaseResult.billingResult.debugMessage}"),
                R.string.error_purchase_error,
            )
        }
    }

    fun waitForInitialized() {
        runBlocking {
            initJob.join()
        }
    }

    fun isPurchased(sku: String): Boolean? {
        return skuStateMap[sku]
                ?.let { skuState -> skuState == SkuState.PURCHASED_AND_ACKNOWLEDGED }
    }

    fun getSkuPrice(sku: String): String? {
        return skuDetailsMap[sku]?.price
    }

    fun getSkuPurchasedAmount(sku: String): Long {
        return skuAmountsMap[sku] ?: 0
    }

    fun getSkuTitle(sku: String): String? {
        return skuDetailsMap[sku]?.title
    }

    fun getSkuDescription(sku: String): String? {
        return skuDetailsMap[sku]?.description
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(purchase.originalJson, purchase.signature)
    }

    override fun onBillingServiceDisconnected() {
        logger.info("Billing Service disconnected")
    }

}