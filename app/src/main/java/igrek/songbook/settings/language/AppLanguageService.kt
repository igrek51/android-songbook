package igrek.songbook.settings.language

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.persistence.preferences.PreferencesDefinition
import igrek.songbook.persistence.preferences.PreferencesService
import java.util.*
import javax.inject.Inject

class AppLanguageService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: PreferencesService

    var appLanguage: AppLanguage? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        val appLanguageId = preferencesService.getValue(PreferencesDefinition.appLanguage, String::class.java)
        appLanguage = AppLanguage.parseByLangCode(appLanguageId)
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
        conf.locale = Locale(langCode.toLowerCase())
        res.updateConfiguration(conf, dm)
    }

    fun setLocale() {
        if (appLanguage != null && appLanguage != AppLanguage.SYSTEM_DEFAULT) {
            setLocale(appLanguage!!.langCode)
        }
    }
}