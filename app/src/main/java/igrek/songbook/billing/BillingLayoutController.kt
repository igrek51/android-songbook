package igrek.songbook.billing

import android.view.View
import android.widget.Button
import android.widget.TextView
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.InflatedLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<Button>(R.id.billingBuyAdFree)?.setOnClickListener {
            billingService.launchBillingFlow(PRODUCT_ID_NO_ADS)
        }
        layout.findViewById<Button>(R.id.billingBuyDonate1Button)?.setOnClickListener {
            billingService.launchBillingFlow(PRODUCT_ID_DONATE_1_BEER)
        }
        layout.findViewById<Button>(R.id.billingRestorePurchases)?.setOnClickListener {
            billingService.callRestorePurchases()
        }

        uiInfoService.showInfo(R.string.billing_loading_purchases)
        GlobalScope.launch {
            billingService.waitForInitialized()

            val adfreePurchased: Boolean? = billingService.isPurchased(PRODUCT_ID_NO_ADS)
            val priceAdFree = when (adfreePurchased) {
                true -> uiResourceService.resString(R.string.billing_already_purchased)
                else -> billingService.getSkuPrice(PRODUCT_ID_NO_ADS)
            }

            val priceDonate1 = billingService.getSkuPrice(PRODUCT_ID_DONATE_1_BEER)

            withContext(Dispatchers.Main) {

                layout.findViewById<TextView>(R.id.billingAdFreePrice)?.let {
                    it.text = priceAdFree
                }
                layout.findViewById<TextView>(R.id.billingDonate1Price)?.let {
                    it.text = priceDonate1
                }

                uiInfoService.clearSnackBars()
            }
        }

    }
}