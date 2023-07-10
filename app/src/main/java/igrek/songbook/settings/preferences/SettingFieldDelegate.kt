package igrek.songbook.settings.preferences

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class SettingFieldDelegate<T : Any>(
    private val fieldDef: FieldDefinition<T, *>,
) : ReadWriteProperty<SettingsState, T> {

    companion object {
        fun make(preferenceName: String, defaultValue: String): SettingFieldDelegate<String> {
            return SettingFieldDelegate(StringField(preferenceName, defaultValue))
        }

        fun make(preferenceName: String, defaultValue: Long): SettingFieldDelegate<Long> {
            return SettingFieldDelegate(LongField(preferenceName, defaultValue))
        }

        fun make(preferenceName: String, defaultValue: Float): SettingFieldDelegate<Float> {
            return SettingFieldDelegate(FloatField(preferenceName, defaultValue))
        }

        fun make(preferenceName: String, defaultValue: Boolean): SettingFieldDelegate<Boolean> {
            return SettingFieldDelegate(BooleanField(preferenceName, defaultValue))
        }

        fun <U : Any> makeGenericStringId(
            preferenceName: String,
            defaultValue: U,
            serializer: (U) -> String,
            deserializer: (String) -> U?,
        ): SettingFieldDelegate<U> {
            return SettingFieldDelegate(GenericStringIdField(
                preferenceName,
                defaultValue = defaultValue,
                serializer = serializer,
                deserializer = deserializer,
            ))
        }

        fun <U : Any> makeGenericLongId(
            preferenceName: String,
            defaultValue: U,
            serializer: (U) -> Long,
            deserializer: (Long) -> U?,
        ): SettingFieldDelegate<U> {
            return SettingFieldDelegate(GenericLongIdField(
                preferenceName,
                defaultValue = defaultValue,
                serializer = serializer,
                deserializer = deserializer,
            ))
        }
    }

    init {
        SettingsState.knownSettingFields[fieldDef.preferenceName] = fieldDef
    }

    override fun getValue(thisRef: SettingsState, property: KProperty<*>): T {
        return thisRef.preferencesService.getValue(fieldDef)
    }

    override fun setValue(thisRef: SettingsState, property: KProperty<*>, value: T) {
        thisRef.preferencesService.setValue(fieldDef, value)
    }
}
