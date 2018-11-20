package igrek.songbook.settings.language

import android.app.Activity
import android.os.Build
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.model.chords.ChordsNotation
import igrek.songbook.persistence.preferences.PreferencesDefinition
import igrek.songbook.persistence.preferences.PreferencesService
import igrek.songbook.songpreview.LyricsManager
import java.util.*
import javax.inject.Inject


class AppLanguageService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: PreferencesService
    @Inject
    lateinit var lyricsManager: LyricsManager

    var appLanguage: AppLanguage? = null

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        loadPreferences()
        setDefaultChordsNotation()
    }

    private fun setDefaultChordsNotation() {
        // running for the first time - set german / polish notation if lang pl
        // set default chords notation depending on locale settings
        if (!preferencesService.exists(PreferencesDefinition.chordsNotationId.name)) {
            val current: Locale = getCurrentLocale()
            val lang = current.language
            if (lang == "pl") {
                lyricsManager.chordsNotation = ChordsNotation.GERMAN
            } else {
                lyricsManager.chordsNotation = ChordsNotation.ENGLISH
            }
        }
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

    fun getCurrentLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.resources.configuration.locales.get(0)
        } else {
            activity.resources.configuration.locale
        }
    }
}