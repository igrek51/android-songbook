package igrek.songbook.settings.language


import android.app.Activity
import android.os.Build
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.inject.LazyExtractor
import igrek.songbook.inject.LazyInject
import igrek.songbook.inject.appFactory
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesState
import java.util.*
import kotlin.collections.LinkedHashMap

class AppLanguageService(
        activity: LazyInject<Activity> = appFactory.activity,
        uiResourceService: LazyInject<UiResourceService> = appFactory.uiResourceService,
        preferencesState: LazyInject<PreferencesState> = appFactory.preferencesState,
        userDataDao: LazyInject<UserDataDao> = appFactory.userDataDao,
) {
    private val activity by LazyExtractor(activity)
    private val uiResourceService by LazyExtractor(uiResourceService)
    private val preferencesState by LazyExtractor(preferencesState)
    private val userDataDao by LazyExtractor(userDataDao)

    private var appLanguage: AppLanguage
        get() = preferencesState.appLanguage
        set(value) {
            preferencesState.appLanguage = value
        }

    private val logger = LoggerFactory.logger

    var selectedSongLanguages: Set<SongLanguage>
        get() {
            val excludedLanguages = userDataDao.exclusionDao.exclusionDb.languages
            return SongLanguage.allKnown().filter { it.langCode !in excludedLanguages }.toSet()
        }
        set(value) {
            val excluded = (SongLanguage.allKnown() - value).map { it.langCode }.toMutableList()
            userDataDao.exclusionDao.setExcludedLanguages(excluded)
        }

    /**
     * forces locale settings
     * @param langCode language code (pl)
     */
    private fun setLocale(langCode: String) {
        val res = activity.resources
        // Change locale settings in the app.
        val dm = res.displayMetrics
        val conf = res.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            conf.setLocale(Locale(langCode.lowercase()))
        } else {
            conf.locale = Locale(langCode.lowercase())
        }
        res.updateConfiguration(conf, dm)
    }

    fun setLocale() {
        if (appLanguage != AppLanguage.DEFAULT) {
            setLocale(appLanguage.langCode)
        }
    }

    fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.resources.configuration.locales.get(0)
        } else {
            activity.resources.configuration.locale
        }
    }

    fun songLanguageEntries(): LinkedHashMap<SongLanguage, String> {
        val map = LinkedHashMap<SongLanguage, String>()
        SongLanguage.allKnown()
                .forEach { lang ->
                    val locale = Locale(lang.langCode)
                    val langDisplayName = locale.getDisplayLanguage(locale)
                    map[lang] = langDisplayName
                }
        return map
    }

    fun languageStringEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in AppLanguage.values()) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item.langCode] = displayName
        }
        return map
    }

    fun languageFilterEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        SongLanguage.allKnown()
                .forEach { lang ->
                    val locale = Locale(lang.langCode)
                    val langDisplayName = locale.getDisplayLanguage(locale)
                    map[lang.langCode] = langDisplayName
                }
        return map
    }

    fun lanugages2String(languages: List<SongLanguage>): String {
        return languages.joinToString(separator = ";") { language -> language.langCode }
    }

    private fun string2Languages(languagesStr: String): List<SongLanguage> {
        if (languagesStr.isEmpty())
            return mutableListOf()
        val languages = mutableListOf<SongLanguage>()
        val languagesParts = languagesStr.split(";")
        for (languageCode in languagesParts) {
            val lang = SongLanguage.parseByLangCode(languageCode)
            if (lang == null) {
                logger.warn("unknown language code: $languageCode")
                continue
            }
            languages.add(lang)
        }
        return languages
    }

    fun setSelectedSongLanguageCodes(languageCodes: Set<String>) {
        selectedSongLanguages = SongLanguage.allKnown()
                .filter { it.langCode in languageCodes }
                .toSet()
    }
}