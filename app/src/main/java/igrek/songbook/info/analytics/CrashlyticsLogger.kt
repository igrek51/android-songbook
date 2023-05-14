package igrek.songbook.info.analytics

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.Settings
import com.google.firebase.crashlytics.FirebaseCrashlytics
import igrek.songbook.BuildConfig
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.settings.language.AppLanguageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    fun sendCrashlyticsAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            sendCrashlytics()
        }
    }

    @SuppressLint("HardwareIds")
    fun sendCrashlytics() {
        try {
            val deviceId: String =
                Settings.Secure.getString(activity.contentResolver, Settings.Secure.ANDROID_ID)
            crashlytics.setUserId(deviceId)
            setCustomKeys()
        } catch (t: Throwable) {
            logger.error("setting crashlytics keys", t)
        }
        crashlytics.setCrashlyticsCollectionEnabled(false)
        crashlytics.sendUnsentReports()
        crashlytics.setCrashlyticsCollectionEnabled(true)
        crashlytics.sendUnsentReports()
    }

    fun reportNonFatalError(throwable: Throwable) {
        GlobalScope.launch(Dispatchers.IO) {
            crashlytics.recordException(throwable)
            sendCrashlytics()
        }
    }

    private fun setCustomKeys() {
        crashlytics.setCustomKey("locale", appLanguageService.getCurrentLocale().language)
        val dbVersionNumber = songsRepository.publicSongsRepo.versionNumber.toString()
        crashlytics.setCustomKey("dbVersion", dbVersionNumber)
        crashlytics.setCustomKey(
            "buildConfig", if (BuildConfig.DEBUG) "debug" else "release"
        )
        crashlytics.setCustomKey("buildDate", BuildConfig.BUILD_DATE.formatYYYMMDD())
        activity::class.simpleName?.let { activityName ->
            crashlytics.setCustomKey("activityName", activityName)
        }
        crashlytics.setCustomKey("versionName", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("versionCode", BuildConfig.VERSION_CODE.toString())
    }

    private fun Date.formatYYYMMDD(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return dateFormat.format(this)
    }
}