package igrek.songbook.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import igrek.songbook.BuildConfig
import igrek.songbook.R
import igrek.songbook.info.UiInfoService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.secret.SecretCommandService
import igrek.songbook.system.PackageInfoService
import java.text.SimpleDateFormat
import java.util.*

class AboutLayoutController(
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
    secretCommandService: LazyInject<SecretCommandService> = appFactory.secretCommandService,
    packageInfoService: LazyInject<PackageInfoService> = appFactory.packageInfoService,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
    uiInfoService: LazyInject<UiInfoService> = appFactory.uiInfoService,
    webviewLayoutController: LazyInject<WebviewLayoutController> = appFactory.webviewLayoutController,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val activity by LazyExtractor(appCompatActivity)
    private val secretCommandService by LazyExtractor(secretCommandService)
    private val packageInfoService by LazyExtractor(packageInfoService)
    private val songsRepository by LazyExtractor(songsRepository)
    private val uiInfoService by LazyExtractor(uiInfoService)
    private val webviewLayoutController by LazyExtractor(webviewLayoutController)

    fun showAbout() {
        val appVersionName = packageInfoService.versionName
        val buildDate = BuildConfig.BUILD_DATE.formatYYYMMDD()
        val appVersionCode = packageInfoService.versionCode.toString()
        val dbVersionNumber = songsRepository.publicSongsRepo.versionNumber.toString()
        val title = uiResourceService.resString(R.string.nav_about)

        val variant = when {
            BuildConfig.DEBUG -> "debug"
            else -> "release"
        }
        val appVersionLong = "$variant $appVersionCode $buildDate"
        val message = uiResourceService.resString(
            R.string.ui_about_content,
            appVersionName,
            appVersionLong,
            dbVersionNumber
        )
        val spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // set button almost hidden by setting color
        val postProcessor = { alertDialog: AlertDialog ->
            alertDialog.setOnShowListener {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                    .setTextColor(uiResourceService.getColor(R.color.unlockAction))
            }
        }
        uiInfoService.dialogThreeChoices(
            title = title,
            message = spannedMessage,
            positiveButton = R.string.action_info_ok,
            positiveAction = {},
            negativeButton = R.string.action_rate_app,
            negativeAction = { this.openInGoogleStore() },
            neutralButton = R.string.action_secret,
            neutralAction = { secretCommandService.showUnlockAlert() },
            postProcessor = postProcessor,
            richMessage = true,
        )
    }

    fun showManual() {
        webviewLayoutController.openUrlUserGuide()
    }

    fun showFirstTimeManualDialog() {
        uiInfoService.dialogThreeChoices(
            titleResId = R.string.manual_first_time,
            messageResId = R.string.manual_confirm_opening_manual,
            negativeButton = R.string.action_cancel, negativeAction = {},
            positiveButton = R.string.action_info_yes, positiveAction = { showManual() }
        )
    }

    fun openInGoogleStore() {
        try {
            val urlActivity = Intent(
                Intent.ACTION_VIEW, Uri.parse(
                    "http://play.google.com/store/apps/details?id=" + activity.packageName
                )
            )
            activity.startActivity(urlActivity)
        } catch (e: ActivityNotFoundException) {
            val uri = Uri.parse("market://details?id=" + activity.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            activity.startActivity(goToMarket)
        }
    }

    private fun Date.formatYYYMMDD(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return dateFormat.format(this)
    }

}
