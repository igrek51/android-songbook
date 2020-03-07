package igrek.songbook.settings.chordsnotation

import android.app.Activity
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.info.logger.LoggerFactory.logger
import igrek.songbook.settings.language.AppLanguageService
import igrek.songbook.settings.preferences.PreferencesState
import java.util.*
import javax.inject.Inject


class ChordsNotationService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var uiResourceService: UiResourceService
    @Inject
    lateinit var appLanguageService: AppLanguageService
    @Inject
    lateinit var preferencesState: PreferencesState

    var chordsNotation: ChordsNotation
        get() = preferencesState.chordsNotation
        set(value) {
            preferencesState.chordsNotation = value
        }

    private val germanNotationLangs = setOf(
            "pl", "de", "da", "sv", "nb", "nn", "is", "et",
            "sr", "hr", "bs", "sl", "sk", "cs", "hu"
    )

    init {
        DaggerIoc.factoryComponent.inject(this)
    }

    fun setDefaultChordsNotation() {
        // running for the first time - set german / polish notation if lang pl
        // set default chords notation depending on locale settings
        val current: Locale = appLanguageService.getCurrentLocale()
        val lang = current.language

        chordsNotation = if (germanNotationLangs.contains(lang)) {
            ChordsNotation.GERMAN
        } else {
            ChordsNotation.ENGLISH
        }
        logger.info("Default chords notation set: $chordsNotation")
    }

    fun chordsNotationEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in ChordsNotation.values()) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }

    val chordsNotationDisplayNames: LinkedHashMap<ChordsNotation, String> by lazy {
        val map = LinkedHashMap<ChordsNotation, String>()
        for (item in ChordsNotation.values()) {
            val displayName = uiResourceService.resString(item.displayNameResId)
            map[item] = displayName
        }
        map
    }

}