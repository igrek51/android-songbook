package igrek.songbook.settings.language

import android.app.Activity
import android.os.Build
import dagger.Lazy
import igrek.songbook.R
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory
import igrek.songbook.settings.preferences.PreferencesDefinition
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.theme.ColorScheme
import java.util.*
import javax.inject.Inject


class AppLanguageService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>

    var appLanguage: AppLanguage? = null
    var excludedLanguages: List<SongLanguage> = listOf()
    private val logger = LoggerFactory.logger

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        val appLanguageId = preferencesService.get().getValue(PreferencesDefinition.appLanguage, String::class.java)
        if (appLanguageId != null) {
            appLanguage = AppLanguage.parseByLangCode(appLanguageId)
            if (appLanguage == null)
                appLanguage = AppLanguage.DEFAULT
        }

        val excludedLanguagesStr = preferencesService.get().getValue(PreferencesDefinition.excludedLanguages, String::class.java)
        if (excludedLanguagesStr != null)
            excludedLanguages = string2Languages(excludedLanguagesStr)
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
        if (appLanguage != null && appLanguage != AppLanguage.DEFAULT) {
            setLocale(appLanguage!!.langCode)
        }
    }

    fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.resources.configuration.locales.get(0)
        } else {
            activity.resources.configuration.locale
        }
    }

    fun languageEntries(): LinkedHashMap<String, String> {
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
                map[item.langCode] = uiResourceService.get().resString(R.string.language_unknown)
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

    fun string2Languages(languagesStr: String): List<SongLanguage> {
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