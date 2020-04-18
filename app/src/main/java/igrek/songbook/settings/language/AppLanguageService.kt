package igrek.songbook.settings.language

import android.app.Activity
import android.os.Build
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.persistence.user.UserDataDao
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import java.util.*
import javax.inject.Inject
import kotlin.collections.LinkedHashMap


class AppLanguageService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>
    @Inject
    lateinit var preferencesState: Lazy<PreferencesState>

    @Inject
    lateinit var userDataDao: Lazy<UserDataDao>

    private var appLanguage: AppLanguage
        get() = preferencesState.get().appLanguage
        set(value) {
            preferencesState.get().appLanguage = value
        }

    private val logger = LoggerFactory.logger

    var selectedSongLanguages: Set<SongLanguage>
        get() {
            val excludedLanguages = userDataDao.get().exclusionDao.exclusionDb.languages
            return SongLanguage.allKnown().filter { it.langCode !in excludedLanguages }.toSet()
        }
        set(value) {
            val excluded = (SongLanguage.allKnown() - value).map { it.langCode }.toMutableList()
            userDataDao.get().exclusionDao.setExcludedLanguages(excluded)
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
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
            conf.setLocale(Locale(langCode.toLowerCase()))
        } else {
            conf.locale = Locale(langCode.toLowerCase())
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
            val displayName = uiResourceService.get().resString(item.displayNameResId)
            map[item.langCode] = displayName
        }
        return map
    }

    fun languageFilterEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in SongLanguage.values()) {
            if (item == SongLanguage.UNKNOWN) {
                continue
            }

            val locale = Locale(item.langCode)
            val displayName = locale.getDisplayLanguage(locale)
            map[item.langCode] = displayName
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
}