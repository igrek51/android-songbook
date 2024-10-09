package igrek.songbook.settings.language


import android.app.Activity
import android.os.Build
import igrek.songbook.info.UiResourceService
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.repository.SongsRepository
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.SettingsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*


class AppLanguageService(
    activity: LazyInject<Activity> = appFactory.activity,
    uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
    settingsState: LazyInject<SettingsState> = appFactory.settingsState,
    userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
    songsRepository: LazyInject<SongsRepository> = appFactory.songsRepository,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val preferencesState by LazyExtractor(settingsState)
    private val userDataDao by LazyExtractor(userDataDao)
    private val songsRepository by LazyExtractor(songsRepository)

    var selectedSongLanguages: Set<SongLanguage>
        get() {
            val excludedLanguages = userDataDao.exclusionDao.exclusionDb.languages
            return knownLanguageCodes().filter { it.langCode !in excludedLanguages }.toSet()
        }
        set(value) {
            val excluded = (knownLanguageCodes() - value).map { it.langCode }.toMutableList()
            userDataDao.exclusionDao.setExcludedLanguages(excluded)
        }

    /**
     * forces locale settings
     * @param langCode language code (pl)
     */
    private fun setLocale(langCode: String?) {
        val res = activity.resources
        // Change locale settings in the app.
        val dm = res.displayMetrics
        val conf = res.configuration
        if (langCode == null) {
            conf.setLocale(null)
        } else {
            conf.setLocale(Locale(langCode.lowercase()))
        }
        @Suppress("DEPRECATION")
        res.updateConfiguration(conf, dm)
    }

    suspend fun setLocale() {
        if (preferencesState.appLanguage != AppLanguage.DEFAULT) {
            withContext(Dispatchers.Main) {
                setLocale(preferencesState.appLanguage.langCode)
            }
        }
    }

    fun updateLocale() {
        val langCode = preferencesState.appLanguage.langCode.takeIf { it.isNotBlank() }
        setLocale(langCode)
    }

    @Suppress("DEPRECATION")
    fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.resources.configuration.locales.get(0)
        } else {
            activity.resources.configuration.locale
        }
    }

    private fun knownLanguageCodes(): List<SongLanguage> {
        return songsRepository.allSongsRepo.songLanguages.get()
    }

    fun songLanguageEntries(): LinkedHashMap<SongLanguage, String> {
        val map = LinkedHashMap<SongLanguage, String>()
        knownLanguageCodes()
            .forEach { lang ->
                val locale = Locale(lang.langCode)
                val langDisplayName = locale.getDisplayLanguage(locale)
                map[lang] = langDisplayName
            }
        return map
    }

    fun appLanguageStringEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in AppLanguage.entries) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item.langCode] = displayName
        }
        return map
    }

    fun languageFilterEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        knownLanguageCodes()
            .forEach { lang ->
                val locale = Locale(lang.langCode)
                val langDisplayName = locale.getDisplayLanguage(locale)
                map[lang.langCode] = langDisplayName
            }
        return map
    }

    fun setSelectedSongLanguageCodes(languageCodes: Set<String>) {
        selectedSongLanguages = knownLanguageCodes()
            .filter { it.langCode in languageCodes }
            .toSet()
    }
}

fun isValidLanguageCode(langCode: String): Boolean {
    return try {
        val locale = Locale(langCode)
        !locale.isO3Language.isNullOrEmpty()
    } catch (e: MissingResourceException) {
        false
    }
}
