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
import igrek.songbook.activity.ActivityController
import igrek.songbook.editor.SongEditorLayoutController
import igrek.songbook.info.errorcheck.UiErrorHandler
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.layout.GlobalFocusTraverser
import igrek.songbook.layout.MainLayout
import igrek.songbook.settings.preferences.SettingsState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@SuppressLint("CheckResult")
class AdService(
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    globalFocusTraverser: LazyInject<GlobalFocusTraverser> = appFactory.globalFocusTraverser,
    activityController: LazyInject<ActivityController> = appFactory.activityController,
) {
    private val activity by LazyExtractor(appCompatActivity)
    private val preferencesState by LazyExtractor(settingsState)
    private val globalFocusTraverser by LazyExtractor(globalFocusTraverser)
    private val activityController by LazyExtractor(activityController)

    private var testingMode = BuildConfig.DEBUG
    private val requestAdViewSubject = PublishSubject.create<Boolean>()
    private val hideAdsOnDebug = true

    init {
        requestAdViewSubject
            .throttleFirst(120, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                GlobalScope.launch(Dispatchers.Main) {
                    runCatching {
                        requestAdRefresh()
                    }.onFailure { t ->
                        logger.error("Failed to load ad", t)
                    }
                }
            }, UiErrorHandler::handleError)
    }

    fun initialize() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(activity) {}
            } catch (t: Throwable) {
                logger.error("AdMob initialization failed", t)
            }
        }
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

    @Suppress("UNUSED_PARAMETER")
    private fun bannerToBeDisplayed(currentLayout: MainLayout): Boolean {
        return when {
            BuildConfig.DEBUG && hideAdsOnDebug -> false
            preferencesState.adsStatus == 1L -> false
            preferencesState.purchasedAdFree -> false
            activityController.isAndroidTv() -> false
            SongEditorLayoutController::class.isInstance(currentLayout) -> false
            //SongPreviewLayoutController::class.isInstance(currentLayout) -> false
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
        logger.debug("Focus set to Ad banner")
        return true
    }

    @Suppress("DEPRECATION")
    private fun requestAdRefresh() {
        logger.debug("initializing ad banner")
        val adViewContainer: FrameLayout = activity.findViewById(R.id.ad_view_container) ?: run {
            logger.warn("ad view container not found")
            return
        }

        val adView = AdView(activity)
        adViewContainer.removeAllViews()
        adViewContainer.addView(adView)
        adViewContainer.visibility = View.VISIBLE

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

        var adContainerWidthPixels = adViewContainer.width.toFloat()
        if (adContainerWidthPixels == 0f) {
            adContainerWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adContainerWidthDp = (adContainerWidthPixels / density).toInt()
        val adSize: AdSize =
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adContainerWidthDp)

        adView.setAdSize(adSize)
        val adUnitResId = when {
            testingMode -> R.string.adaptive_banner_ad_unit_id_test
            else -> R.string.adaptive_banner_ad_unit_id_prod
        }
        adView.adUnitId = activity.getString(adUnitResId)
        adView.isClickable = true
        adView.isFocusable = true

        adView.setOnClickListener {
            adView.adListener.onAdClicked()
            adView.adListener.onAdOpened()
        }

        globalFocusTraverser.setUpDownKeyListener(adView)

        if (!adSize.isAutoHeight) {
            adViewContainer.minimumHeight = adSize.getHeightInPixels(activity)
        }

        val adRequest = Builder().build()
        GlobalScope.launch(Dispatchers.Main) {
            adView.loadAd(adRequest)
        }
    }

    fun enableAds() {
        preferencesState.adsStatus = 0
    }

    fun disableAds() {
        preferencesState.adsStatus = 1
    }

}