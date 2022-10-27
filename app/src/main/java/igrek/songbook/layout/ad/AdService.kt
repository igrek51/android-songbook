package igrek.songbook.layout.ad

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.allViews
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdRequest.*
import igrek.songbook.BuildConfig
import igrek.songbook.R
import igrek.songbook.editor.ChordsEditorLayoutController
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.MainLayout
import igrek.songbook.settings.preferences.PreferencesState
import igrek.songbook.songpreview.SongPreviewLayoutController
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class AdService(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val preferencesState by LazyExtractor(preferencesState)

    private var testingMode = BuildConfig.DEBUG
    private val requestAdViewSubject = PublishSubject.create<Boolean>()
    private val hideAdsOnDebug = true

    init {
        requestAdViewSubject
                .throttleFirst(120, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    requestAdRefresh()
                }, UiErrorHandler::handleError)
    }

    fun initialize() {
        try {
            MobileAds.initialize(activity) {}
        } catch (t: Throwable) {
            logger.error("AdMob initialization failed", t)
        }
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
        return when {
            BuildConfig.DEBUG && hideAdsOnDebug -> false
            SongPreviewLayoutController::class.isInstance(currentLayout) -> false
            ChordsEditorLayoutController::class.isInstance(currentLayout) -> false
            preferencesState.adsStatus == 1L -> false
            preferencesState.purchasedAdFree -> false
            else -> true
        }
    }

    fun hideAdBanner() {
        val adViewContainer: FrameLayout? = activity.findViewById(R.id.ad_view_container)
        adViewContainer?.visibility = View.GONE
    }

    private fun showAdBanner() {
        val adViewContainer: FrameLayout? = activity.findViewById(R.id.ad_view_container)
        adViewContainer?.visibility = View.VISIBLE
        requestAdViewSubject.onNext(true)
    }

    fun focusAdBanner(): Boolean {
        val adViewContainer: FrameLayout? = activity.findViewById(R.id.ad_view_container)
        if (adViewContainer?.visibility != View.VISIBLE)
            return false
        adViewContainer.allViews.first().requestFocusFromTouch()
        return true
    }

    @Suppress("DEPRECATION")
    private fun requestAdRefresh() {
        logger.debug("initializing ad banner")
        val adViewContainer: FrameLayout? = activity.findViewById(R.id.ad_view_container)

        val adView = AdView(activity)
        adViewContainer?.removeAllViews()
        adViewContainer?.addView(adView)
        adViewContainer?.visibility = View.VISIBLE

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {}

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val description = when (adError.code) {
                    ERROR_CODE_INTERNAL_ERROR -> "Something happened internally; for instance, an invalid response was received from the ad server"
                    ERROR_CODE_INVALID_REQUEST -> "The ad request was invalid"
                    ERROR_CODE_NETWORK_ERROR -> "The ad request was unsuccessful due to network connectivity"
                    ERROR_CODE_NO_FILL -> "The ad request was successful, but no ad was returned due to lack of ad inventory"
                    else -> ""
                }
                logger.warn("ad failed to load, error: ${adError.code} - ${adError.message} - $description")
            }

            override fun onAdOpened() {
                logger.debug("ad opened")
            }

            override fun onAdClicked() {
                logger.debug("ad clicked")
            }

            override fun onAdImpression() {}

            override fun onAdClosed() {}
        }

        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adContainerWidthPixels = adViewContainer?.width?.toFloat() ?: 0f
        if (adContainerWidthPixels == 0f) {
            adContainerWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adContainerWidthDp = (adContainerWidthPixels / density).toInt()
        val adSize: AdSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adContainerWidthDp)

        val adUnitResId = when {
            testingMode -> R.string.adaptive_banner_ad_unit_id_test
            else -> R.string.adaptive_banner_ad_unit_id_prod
        }
        adView.adUnitId = activity.getString(adUnitResId)
        adView.setAdSize(adSize)

        val adRequest = Builder().build()
        adView.loadAd(adRequest)
    }

    fun enableAds() {
        preferencesState.adsStatus = 0
    }

    fun disableAds() {
        preferencesState.adsStatus = 1
    }

}