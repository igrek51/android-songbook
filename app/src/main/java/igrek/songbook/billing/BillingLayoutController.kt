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

class BillingLayoutController(
        uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        billingHelper: LazyInject<BillingHelper> = appFactory.billingHelper,
) : InflatedLayout(
        _layoutResourceId = R.layout.screen_billing
) {
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val billingHelper by LazyExtractor(billingHelper)

    override fun showLayout(layout: View) {
        super.showLayout(layout)

        layout.findViewById<Button>(R.id.billingBuyAdFree)?.setOnClickListener {
            billingHelper.launchBillingFlow(PRODUCT_ID_NO_ADS)
        }
        layout.findViewById<Button>(R.id.billingRestorePurchases)?.setOnClickListener {
            billingHelper.callRestorePurchases()
        }

        val adfreePurchased = billingHelper.syncIsPurchased(PRODUCT_ID_NO_ADS)
        val price = when(adfreePurchased) {
            true -> uiResourceService.resString(R.string.billing_already_purchased)
            else -> billingHelper.getSkuPrice(PRODUCT_ID_NO_ADS)
        }

        layout.findViewById<TextView>(R.id.billingAdFreePrice)?.let {
            it.text = price
        }

    }
}