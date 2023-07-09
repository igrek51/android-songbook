package igrek.songbook.settings.preferences

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferenceDelegate<T : Any>(
    private val field: SettingField
) : ReadWriteProperty<SettingsState, T> {

    override fun getValue(thisRef: SettingsState, property: KProperty<*>): T {
        return thisRef.preferencesService.getValue(field)
    }

    override fun setValue(thisRef: SettingsState, property: KProperty<*>, value: T) {
        thisRef.preferencesService.setValue(field, value)
    }
}
