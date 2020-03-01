package igrek.songbook.settings.instrument

import android.app.Activity
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.preferences.PreferencesField
import igrek.songbook.settings.preferences.PreferencesService
import java.util.*
import javax.inject.Inject

class ChordsInstrumentService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>

    var instrument: ChordsInstrument = ChordsInstrument.default

    init {
        DaggerIoc.factoryComponent.inject(this)
        loadPreferences()
    }

    private fun loadPreferences() {
        val id = preferencesService.get().getValue(PreferencesField.ChordsInstrument, Long::class)
        instrument = ChordsInstrument.parseById(id ?: ChordsInstrument.default.id) ?: ChordsInstrument.default
    }

    fun instrumentEntries(): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        for (item in ChordsInstrument.values()) {
            val displayName = uiResourceService.get().resString(item.displayNameResId)
            map[item.id.toString()] = displayName
        }
        return map
    }
}