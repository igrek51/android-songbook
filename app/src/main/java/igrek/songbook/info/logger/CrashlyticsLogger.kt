package igrek.songbook.info.logger

import android.app.Activity
import android.provider.Settings
import com.google.firebase.crashlytics.FirebaseCrashlytics
import igrek.songbook.BuildConfig
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService

class CrashlyticsLogger(
        activity: LazyInject<Activity> = appFactory.activity,
        appLanguageService: LazyInject<AppLanguageService> = appFactory.appLanguageService,
        songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) {
    private val activity by LazyExtractor(activity)
    private val appLanguageService by LazyExtractor(appLanguageService)
    private val songsRepository by LazyExtractor(songsRepository)

    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logCrashlytics(message: String?) {
        message?.let { crashlytics.log(it) }
    }

    fun sendCrashlytics() {
        val deviceId = Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
        crashlytics.setUserId(deviceId)
        try {
            setCustomKeys()
        } catch (t: Throwable) {
        }
        crashlytics.sendUnsentReports()
    }

    private fun setCustomKeys() {
        crashlytics.setCustomKey("locale", appLanguageService.getCurrentLocale().language)
        val dbVersionNumber = songsRepository.publicSongsRepo.versionNumber.toString()
        crashlytics.setCustomKey("dbVersion", dbVersionNumber)
        crashlytics.setCustomKey("buildConfig", if (BuildConfig.DEBUG) "debug" else "release") // build config
    }
}