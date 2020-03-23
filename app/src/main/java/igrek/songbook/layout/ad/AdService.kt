package igrek.songbook.layout.ad

import android.util.DisplayMetrics
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdRequest.*
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.layout.MainLayout
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongPreviewLayoutController
import javax.inject.Inject

class AdService {

    @Inject
    lateinit var activity: AppCompatActivity

    @Inject
    lateinit var preferencesState: Lazy<PreferencesState>

    private var testingMode = false

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun initialize() {
        MobileAds.initialize(activity) {}
    }

    private fun setTagForChildDirectedTreatment() {
        val conf = RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .build()
        MobileAds.setRequestConfiguration(conf)
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
        return !SongPreviewLayoutController::class.isInstance(currentLayout) && !areAdsDisabled()
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
                logger.warn("ad failed to load, error code: $errorCode")
                when (errorCode) {
                    ERROR_CODE_INTERNAL_ERROR -> logger.warn("Something happened internally; for instance, an invalid response was received from the ad server")
                    ERROR_CODE_INVALID_REQUEST -> logger.warn("The ad request was invalid")
                    ERROR_CODE_NETWORK_ERROR -> logger.warn("The ad request was unsuccessful due to network connectivity")
                    ERROR_CODE_NO_FILL -> logger.warn("The ad request was successful, but no ad was returned due to lack of ad inventory")
                }
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
            testingMode -> R.string.adaptive_banner_ad_unit_id_test
            else -> R.string.adaptive_banner_ad_unit_id_prod
        }
        adView.adUnitId = activity.getString(adUnitResId)
        adView.adSize = adSize

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    fun disableAds() {
        preferencesState.get().adsStatus = 1
    }

    private fun areAdsDisabled(): Boolean {
        return preferencesState.get().adsStatus == 1L
    }
}