package igrek.songbook.settings.preferences

import android.content.SharedPreferences
import kotlin.reflect.KClass


abstract class PreferenceTypeDefinition<T : Any>(val defaultValue: T) {
    abstract fun load(sharedPreferences: SharedPreferences, propertyName: String): T
    abstract fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any)

    fun validClass(): KClass<out Any> {
        return defaultValue::class
    }
}

class StringPreferenceType (
        defaultValue: String
) : PreferenceTypeDefinition<String>(defaultValue) {

    override fun load(sharedPreferences: SharedPreferences, propertyName: String): String {
        return sharedPreferences.getString(propertyName, defaultValue) ?: defaultValue
    }

    override fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any) {
        editor.putString(propertyName, value as String)
    }
}

class LongPreferenceType (
        defaultValue: Long
) : PreferenceTypeDefinition<Long>(defaultValue) {

    override fun load(sharedPreferences: SharedPreferences, propertyName: String): Long {
        return sharedPreferences.getLong(propertyName, defaultValue)
    }

    override fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any) {
        editor.putLong(propertyName, value as Long)
    }
}

class FloatPreferenceType (
        defaultValue: Float
) : PreferenceTypeDefinition<Float>(defaultValue) {

    override fun load(sharedPreferences: SharedPreferences, propertyName: String): Float {
        return sharedPreferences.getFloat(propertyName, defaultValue)
    }

    override fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any) {
        editor.putFloat(propertyName, value as Float)
    }
}

class BooleanPreferenceType (
        defaultValue: Boolean
) : PreferenceTypeDefinition<Boolean>(defaultValue) {

    override fun load(sharedPreferences: SharedPreferences, propertyName: String): Boolean {
        return sharedPreferences.getBoolean(propertyName, defaultValue)
    }

    override fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any) {
        editor.putBoolean(propertyName, value as Boolean)
    }
}

class GenericStringIdPreferenceType<T : Any> (
        defaultValue: T,
        private val serializer: (T) -> String,
        private val deserializer: (String) -> T?
) : PreferenceTypeDefinition<T>(defaultValue) {

    override fun load(sharedPreferences: SharedPreferences, propertyName: String): T {
        val stringVal: String = sharedPreferences.getString(propertyName, null) ?: return defaultValue
        return deserializer(stringVal) ?: defaultValue
    }

    override fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any) {
        val serialized: String = serializer(value as T)
        editor.putString(propertyName, serialized)
    }
}

class GenericLongIdPreferenceType<T : Any> (
        defaultValue: T,
        private val serializer: (T) -> Long,
        private val deserializer: (Long) -> T?
) : PreferenceTypeDefinition<T>(defaultValue) {

    override fun load(sharedPreferences: SharedPreferences, propertyName: String): T {
        val defaultSerialized = serializer(defaultValue)
        val longVal: Long = sharedPreferences.getLong(propertyName, defaultSerialized)
        return deserializer(longVal) ?: defaultValue
    }

    override fun save(editor: SharedPreferences.Editor, propertyName: String, value: Any) {
        val serialized: Long = serializer(value as T)
        editor.putLong(propertyName, serialized)
    }
}
