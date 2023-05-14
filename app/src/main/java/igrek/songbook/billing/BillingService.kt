package igrek.songbook.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.Logger
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.ad.AdService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel


const val BILLING_PUBLIC_KEY =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhCjsZRfLF2/6f0/5De3TAKzezDcx/Kozz3d+qsvsHS8Q3TPopC4ODQ8dCZG/6RHbtSMvqXmW7H1K/YqCYJ/cQ6LGwbe6QMUUDy9BV0l8yYaTFGqfkIhaHqbA95934K5DeAzXwnk6eFIWiRm5iTmlg9kNWwQafT3Yd8Es32xWcFh69NUAjIrlgS5xojjm5Tf8rksu1aF8uBwqxwvaCONpMYl9BABf9mzZ27ibiYvHSyAuPqyuQj1Ql4z4FZ8faF9oZrFkXCOD7iD1eoRIHUwelPvEAt5OIIYNyQpW4stv57RR7T8xgrj13GUOROozoaUyLswaR9aDsV51FUBvEoinkwIDAQAB"

const val PRODUCT_ID_NO_ADS = "no_ads_forever"
const val PRODUCT_ID_DONATE_1_BEER = "donate_1_beer"


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
    private val knownAllProducts: List<String> = listOf(
        PRODUCT_ID_NO_ADS,
        PRODUCT_ID_DONATE_1_BEER,
    )
    private val knownConsumableInAppProducts: List<String> = listOf(
        PRODUCT_ID_DONATE_1_BEER,
    )
    private val productStateMap: MutableMap<String, ProductState> = hashMapOf()
    private val productsDetails: MutableMap<String, ProductDetails> = hashMapOf()
    private val productPrices: MutableMap<String, String?> = hashMapOf()
    private val productAmounts: MutableMap<String, Long> = hashMapOf()
    private val initChannel = Channel<Result<Boolean>>(1)
    private val initDetailsChannel = Channel<Result<Boolean>>(1)
    private val initJob: Job
    val purchaseEventsSubject = PublishSubject.create<Boolean>()

    private enum class ProductState {
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
            initDetailsChannel.receive()
            logger.debug("Billing service initialized")
        }

        for (productId: String in this.knownAllProducts) {
            productStateMap[productId] = ProductState.UNKNOWN
            productAmounts[productId] = 0
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
            initDetailsChannel.trySend(Result.success(false))
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
                            queryProductDetails()
                        } catch (t: Throwable) {
                            UiErrorHandler().handleError(t, R.string.error_purchase_error)
                            initDetailsChannel.trySend(Result.success(false))
                        }

                        try {
                            restorePurchases()
                        } catch (t: Throwable) {
                            UiErrorHandler().handleError(t, R.string.error_purchase_error)
                        }

                        initChannel.trySend(Result.success(true))
                    }
                }
                else -> {
                    logger.error("Billing setup response: $responseCode $debugMessage")
                    initChannel.trySend(Result.success(false))
                    initDetailsChannel.trySend(Result.success(false))
                }
            }

        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
            initChannel.trySend(Result.success(false))
            initDetailsChannel.trySend(Result.success(false))
        }
    }

    private fun queryProductDetails() {
        if (knownAllProducts.isEmpty())
            return

        if (knownAllProducts.isNotEmpty()) {
            val queryProductsList = knownAllProducts.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(queryProductsList)
                .build()
            billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult: BillingResult, productDetailsList: List<ProductDetails> ->
                onProductDetailsResponse(billingResult, productDetailsList)
                initDetailsChannel.trySend(Result.success(true))
            }
        }
    }

    private fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetails: List<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (productDetails.isEmpty()) {
                    logger.warn("Found empty product details")
                } else {
                    for (productDetail in productDetails) {
                        val productId = productDetail.productId
                        productsDetails[productId] = productDetail
                        val offerDetails: ProductDetails.OneTimePurchaseOfferDetails? =
                            productDetail.oneTimePurchaseOfferDetails
                        productPrices[productId] = offerDetails?.formattedPrice
                    }
                    logger.debug("Product details fetched: ${productDetails.size}")
                }
            }
            else -> {
                UiErrorHandler().handleError(
                    RuntimeException("Product details: $responseCode $debugMessage"),
                    R.string.error_purchase_error
                )
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
        try {

            for (productId: String in this.knownAllProducts) {
                productAmounts[productId] = 0
                productStateMap[productId] = ProductState.UNKNOWN
            }

            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
            val purchasesResult = billingClient!!.queryPurchasesAsync(params.build())
            val billingResult = purchasesResult.billingResult
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchaseList(purchasesResult.purchasesList)
            } else {
                throw RuntimeException("Restoring purchases failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
            }

            // restorePurchasesFromHistory()

            for (productId in knownAllProducts) {
                if (productStateMap[productId] == ProductState.UNKNOWN) {
                    productStateMap[productId] = ProductState.UNPURCHASED
                }
            }

            logger.debug(
                "Restored purchases: " +
                        "$PRODUCT_ID_NO_ADS - ${productStateMap[PRODUCT_ID_NO_ADS]?.name}, " +
                        "$PRODUCT_ID_DONATE_1_BEER - ${productStateMap[PRODUCT_ID_DONATE_1_BEER]?.name} quantity=${productAmounts[PRODUCT_ID_DONATE_1_BEER]}"
            )

        } catch (t: Throwable) {
            UiErrorHandler().handleError(t, R.string.error_purchase_error)
        }
    }

    private suspend fun restorePurchasesFromHistory() {
        val historyParams = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
        val purchaseHistoryResult = billingClient!!.queryPurchaseHistory(historyParams.build())
        val billingResult = purchaseHistoryResult.billingResult
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            processPurchaseHistory(purchaseHistoryResult.purchaseHistoryRecordList)
        } else {
            throw RuntimeException("Restoring purchases history failed: ${billingResult.responseCode} ${billingResult.debugMessage}")
        }
    }

    fun launchBillingFlow(productId: String) {
        uiInfoService.showInfo(R.string.billing_starting_purchase)
        try {
            val productDetails = productsDetails[productId]
                ?: throw RuntimeException("Product Details not found for product ID: $productId")
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            val billingResult = billingClient!!.launchBillingFlow(activity, billingFlowParams)
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {}
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    uiInfoService.showInfo(R.string.billing_user_canceled_purchase)
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    uiInfoService.showInfo(R.string.billing_user_already_owns_item)
                }
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                    throw RuntimeException("Billing Response: Invalid arguments provided to the API")
                    // Developer error means that Google Play does not recognize the configuration.
                    // The product ID must match and the APK you are using must be signed with release keys."
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    throw RuntimeException("Google Play Billing API is not available.")
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

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        try {
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (null != purchases) {
                        uiInfoService.showInfo(R.string.billing_thanks_for_purchase_wait)
                        processPurchaseList(purchases)
                        purchaseEventsSubject.onNext(true)
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
                    // The product ID must match and the APK you are using must be signed with release keys."
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    throw RuntimeException("Google Play Billing API is not available.")
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
            logger.warn("purchases list is null")
            return
        }
        if (purchases.isNotEmpty()) {
            logger.debug("Processing purchases: ${purchases.size}")
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
                for (productId in purchase.products) {

                    if (purchase.isAcknowledged) {
                        productAmounts[productId] =
                            (productAmounts[productId] ?: 0) + purchase.quantity

                    } else {

                        val billingResult = billingClient!!.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                        )
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            logger.info("Purchase acknowledged: $productId")
                            productStateMap[productId] = ProductState.PURCHASED_AND_ACKNOWLEDGED
                            productAmounts[productId] =
                                (productAmounts[productId] ?: 0) + purchase.quantity
                            savePurchaseData(productId)
                            uiInfoService.showInfo(R.string.billing_thanks_for_purchase_confirm)

                        } else {
                            UiErrorHandler().handleError(
                                RuntimeException("Error acknowledging purchase: $productId"),
                                R.string.error_purchase_error,
                            )
                        }
                    }

                    if (isConsumable(productId)) {
                        consumePurchase(purchase)
                    }

                }
            }
        }

        setProductStateFromPurchase(purchase)
    }

    private fun setProductStateFromPurchase(purchase: Purchase) {
        if (purchase.products.isEmpty()) {
            logger.error("Empty list of Products in a purchase")
            return
        }

        for (productId in purchase.products) {
            val productState = productStateMap[productId]
            if (productState == null) {
                logger.error("Unknown Product ID in a purchase: $productId.")
                continue
            }

            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> productStateMap[productId] = ProductState.PENDING
                Purchase.PurchaseState.UNSPECIFIED_STATE -> productStateMap[productId] =
                    ProductState.UNPURCHASED
                Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
                    productStateMap[productId] = ProductState.PURCHASED_AND_ACKNOWLEDGED
                    savePurchaseData(productId)
                } else {
                    productStateMap[productId] = ProductState.PURCHASED
                }
                else -> logger.error("Purchase in unknown state: ${purchase.purchaseState}")
            }
        }
    }

    private fun processPurchaseHistory(purchaseHistoryRecords: List<PurchaseHistoryRecord>?) {
        if (purchaseHistoryRecords.isNullOrEmpty())
            return

        logger.debug("Processing Purchase history records: ${purchaseHistoryRecords.size}")
        for (purchaseHistoryRecord in purchaseHistoryRecords) {
            for (productId in purchaseHistoryRecord.products) {
                val isConsumable = knownConsumableInAppProducts.contains(productId)
                if (isConsumable) {
                    productAmounts[productId] =
                        (productAmounts[productId] ?: 0) + purchaseHistoryRecord.quantity
                }
            }
        }
    }

    private fun savePurchaseData(productId: String) {
        when (productId) {
            PRODUCT_ID_NO_ADS -> {
                if (!preferencesState.purchasedAdFree) {
                    preferencesState.purchasedAdFree = true
                    preferencesService.saveAll()
                    adService.hideAdBanner()
                    purchaseEventsSubject.onNext(true)
                    logger.info("Saving Purchase in preferences data, Product ID: $productId")
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
            for (productId in purchase.products) {
                logger.info("Purchase consumed: $productId")
            }
        } else {
            UiErrorHandler().handleError(
                RuntimeException("Error while consuming purchase ${consumePurchaseResult.billingResult.debugMessage}"),
                R.string.error_purchase_error,
            )
        }
    }

    private fun isConsumable(productId: String): Boolean =
        knownConsumableInAppProducts.contains(productId)

    fun waitForInitialized() {
        runBlocking {
            initJob.join()
        }
    }

    fun isPurchased(productId: String): Boolean? {
        return productStateMap[productId]
            ?.let { productState -> productState == ProductState.PURCHASED_AND_ACKNOWLEDGED }
    }

    fun getProductPrice(productId: String): String? {
        return productPrices[productId]
    }

    fun getProductPurchasedAmount(productId: String): Long {
        return productAmounts[productId] ?: 0
    }

    fun getProductTitle(productId: String): String? {
        return productsDetails[productId]?.title
    }

    fun getProductDescription(productId: String): String? {
        return productsDetails[productId]?.description
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(purchase.originalJson, purchase.signature)
    }

    override fun onBillingServiceDisconnected() {
        logger.info("Billing Service disconnected")
    }

}