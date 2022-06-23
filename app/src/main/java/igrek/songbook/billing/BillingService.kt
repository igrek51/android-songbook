package igrek.songbook.billing

import android.content.Context
import com.android.billingclient.api.*
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillingService(
        context: LazyInject<Context> = appFactory.context,
) : PurchasesUpdatedListener {
    private val context by LazyExtractor(context)

    private var billingClient: BillingClient? = null

    fun initConnection() {

        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    suspend fun listProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("product_id_example")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
        )

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build()

        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient!!.queryProductDetails(queryProductDetailsParams)
        }

        val billingResult = productDetailsResult.billingResult
        val productDetailsList = productDetailsResult.productDetailsList

        val product: ProductDetails? = productDetailsList?.first()
        val offers = product?.oneTimePurchaseOfferDetails
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        TODO("Not yet implemented")
    }
}