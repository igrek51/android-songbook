package igrek.songbook.layout.ad

import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import igrek.songbook.BuildConfig
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.layout.MainLayout
import igrek.songbook.songpreview.SongPreviewLayoutController
import javax.inject.Inject

class AdService {

    @Inject
    lateinit var activity: AppCompatActivity

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun initialize() {
        val conf = RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .build()
        MobileAds.setRequestConfiguration(conf)
        MobileAds.initialize(activity) {}
    }

    fun updateAdBanner(currentLayout: MainLayout) {
        try {
            if (bannerToBeDisplayed(currentLayout)) {
                showAdBanner()
            } else {
                hideAdBanner()
            }
        } catch (t: Throwable) {
            logger.error("updating banner failed", t)
        }
    }

    private fun bannerToBeDisplayed(currentLayout: MainLayout): Boolean {
        return !SongPreviewLayoutController::class.isInstance(currentLayout)
    }

    private fun hideAdBanner() {
        val adViewContainer: FrameLayout? = activity.findViewById(R.id.ad_view_container)
        adViewContainer?.removeAllViews()
    }

    private fun showAdBanner() {
        logger.debug("initializing ad banner")
        val adViewContainer: FrameLayout? = activity.findViewById(R.id.ad_view_container)

        val adView = AdView(activity)
        adViewContainer?.addView(adView)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {}

            override fun onAdFailedToLoad(errorCode: Int) {
                logger.error("ad failed to load, error code: $errorCode")
            }

            override fun onAdOpened() {
                logger.debug("ad opened")
            }

            override fun onAdClicked() {
                logger.debug("ad clicked")
            }

            override fun onAdLeftApplication() {}

            override fun onAdClosed() {}
        }

        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adViewContainer?.width?.toFloat() ?: 0f
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        val adSize: AdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)

        val adUnitResId = when {
            BuildConfig.DEBUG -> R.string.adaptive_banner_ad_unit_id_test
            else -> R.string.adaptive_banner_ad_unit_id_prod
        }
        adView.adUnitId = activity.getString(adUnitResId)
        adView.adSize = adSize

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
}