package igrek.songbook.settings.instrument

import android.app.Activity
import dagger.Lazy
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.UiResourceService
import igrek.songbook.settings.preferences.PreferencesService
import igrek.songbook.settings.preferences.PreferencesState
import java.util.*
import javax.inject.Inject

class ChordsInstrumentService {

    @Inject
    lateinit var activity: Activity
    @Inject
    lateinit var preferencesService: Lazy<PreferencesService>
    @Inject
    lateinit var uiResourceService: Lazy<UiResourceService>
    @Inject
    lateinit var preferencesState: PreferencesState

    var instrument: ChordsInstrument
        get() = preferencesState.chordsInstrument
        set(value) {
            preferencesState.chordsInstrument = value
        }

    init {
        DaggerIoc.factoryComponent.inject(this)
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