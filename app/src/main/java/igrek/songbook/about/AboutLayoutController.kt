package igrek.songbook.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import igrek.songbook.R
import igrek.songbook.about.secret.SecretCommandService
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.system.PackageInfoService

class AboutLayoutController(
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        appCompatActivity: LazyInject<AppCompatActivity> = appFactory.appCompatActivity,
        secretCommandService: LazyInject<SecretCommandService> = appFactory.secretCommandService,
        packageInfoService: LazyInject<PackageInfoService> = appFactory.packageInfoService,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) {
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val activity by LazyExtractor(appCompatActivity)
    private val secretCommandService by LazyExtractor(secretCommandService)
    private val packageInfoService by LazyExtractor(packageInfoService)
    private val songsRepository by LazyExtractor(songsRepository)

    fun showAbout() {
        val appVersionName = packageInfoService.versionName
        val appVersionCode = packageInfoService.versionCode.toString()
        val dbVersionNumber = songsRepository.publicSongsRepo.versionNumber.toString()
        val title = uiResourceService.resString(R.string.nav_about)
        val message = uiResourceService.resString(R.string.ui_about_content, appVersionName, appVersionCode, dbVersionNumber)

        showDialogWithActions(title, message)
    }

    private fun showDialogWithActions(title: String, message: String) {
        val unlockActionName = uiResourceService.resString(R.string.action_secret)
        val unlockAction = Runnable { secretCommandService.showUnlockAlert() }
        val rateActionName = uiResourceService.resString(R.string.action_rate_app)
        val rateAction = Runnable { this.openInGoogleStore() }

        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setMessage(message)
        alertBuilder.setTitle(title)
        alertBuilder.setNeutralButton(unlockActionName) { _, _ -> unlockAction.run() }
        alertBuilder.setNegativeButton(rateActionName) { _, _ -> rateAction.run() }
        alertBuilder.setPositiveButton(uiResourceService.resString(R.string.action_info_ok)) { _, _ -> }
        alertBuilder.setCancelable(true)
        val alertDialog = alertBuilder.create()
        // set button almost hidden by setting color
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                    .setTextColor(uiResourceService.getColor(R.color.unlockAction))
        }

        if (!activity.isFinishing) {
            alertDialog.show()
        }
    }

    private fun openInGoogleStore() {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity
                    .packageName)))
        }

    }

}
