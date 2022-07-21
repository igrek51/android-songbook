package igrek.songbook.billing

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

@OptIn(DelicateCoroutinesApi::class)
@SuppressLint("CheckResult")
class BillingLayoutController(
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    billingService: LazyInject<BillingService> = appFactory.billingService,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_billing
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val billingService by LazyExtractor(billingService)

    private var buyAdFreeButton: Button? = null
    private var adFreePriceTextView: TextView? = null
    private var donate1PriceTextView: TextView? = null

    private val subscriptions = mutableListOf<Disposable>()

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        subscriptions.forEach { s -> s.dispose() }
        subscriptions.clear()
        subscriptions.add(
            billingService.purchaseEventsSubject
                .debounce(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateView()
                }, UiErrorHandler::handleError)
        )

        buyAdFreeButton = layout.findViewById(R.id.billingBuyAdFree)
        adFreePriceTextView = layout.findViewById(R.id.billingAdFreePrice)
        donate1PriceTextView = layout.findViewById(R.id.billingDonate1Price)

        buyAdFreeButton?.setOnClickListener {
            billingService.launchBillingFlow(PRODUCT_ID_NO_ADS)
        }
        layout.findViewById<Button>(R.id.billingBuyDonate1Button)?.setOnClickListener {
            billingService.launchBillingFlow(PRODUCT_ID_DONATE_1_BEER)
        }
        layout.findViewById<Button>(R.id.billingRestorePurchases)?.setOnClickListener {
            billingService.callRestorePurchases()
        }

        GlobalScope.launch {
            billingService.waitForInitialized()
            updateComponents()
        }

    }

    private fun updateView() {
        if (isLayoutVisible()) {
            GlobalScope.launch {
                updateComponents()
            }
        }
    }

    private fun updateComponents() {
        val priceAdFree = billingService.getProductPrice(PRODUCT_ID_NO_ADS).orEmpty()
        val adfreePurchased: Boolean? = billingService.isPurchased(PRODUCT_ID_NO_ADS)

        val priceDonate1 = billingService.getProductPrice(PRODUCT_ID_DONATE_1_BEER).orEmpty()
        // val donate1Quantity = billingService.getProductPurchasedAmount(PRODUCT_ID_DONATE_1_BEER)

        runBlocking(Dispatchers.Main) {

            if (adfreePurchased == true) {
                buyAdFreeButton?.let {
                    it.isEnabled = false
                    it.text = uiResourceService.resString(R.string.billing_already_purchased)
                }
            }

            adFreePriceTextView?.let {
                it.text = uiInfoService.resString(R.string.billing_item_price, priceAdFree)
            }
            donate1PriceTextView?.let {
                it.text = uiInfoService.resString(R.string.billing_item_price, priceDonate1)
            }

        }
    }

}
