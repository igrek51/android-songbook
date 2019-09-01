package igrek.songbook.settings.preferences

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import igrek.songbook.dagger.DaggerIoc
import igrek.songbook.info.logger.LoggerFactory
import java.util.*
import javax.inject.Inject
import kotlin.collections.set

class PreferencesService {
    @Inject
    lateinit var activity: Activity

    private val logger = LoggerFactory.logger
    private val propertyValues = HashMap<String, Any?>()
    private val sharedPreferences: SharedPreferences

    private val sharedPreferencesName = "SongBook-UserPreferences"

    init {
        DaggerIoc.factoryComponent.inject(this)
        sharedPreferences = activity.applicationContext
                .getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        loadAll()
    }

    private fun loadAll() {
        for (propertyDefinition in PreferencesDefinition.values()) {
            loadProperty(propertyDefinition)
        }
    }

    fun saveAll() {
        val editor = sharedPreferences.edit()
        for (propertyDefinition in PreferencesDefinition.values()) {
            saveProperty(propertyDefinition, editor)
        }
        editor.apply()
    }

    private fun loadProperty(propertyDefinition: PreferencesDefinition) {
        val propertyName = propertyDefinition.name
        var value: Any?
        if (exists(propertyName)) {
            try {
                value = when (propertyDefinition.type) {
                    PropertyType.STRING -> sharedPreferences.getString(propertyName, null)
                    PropertyType.BOOLEAN -> sharedPreferences.getBoolean(propertyName, false)
                    PropertyType.INTEGER -> sharedPreferences.getInt(propertyName, 0)
                    PropertyType.LONG -> sharedPreferences.getLong(propertyName, 0)
                    PropertyType.FLOAT -> sharedPreferences.getFloat(propertyName, 0.0f)
                }
            } catch (e: ClassCastException) {
                value = propertyDefinition.defaultValue
                logger.warn("Invalid property type, loading default value: $propertyName = $value")
            }

        } else {
            value = propertyDefinition.defaultValue
            logger.debug("Missing preferences property, loading default value: $propertyName = $value")
        }
        // logger.debug("Property loaded: $propertyName = $value")
        propertyValues[propertyName] = value
    }

    private fun saveProperty(propertyDefinition: PreferencesDefinition, editor: SharedPreferences.Editor) {
        val propertyName = propertyDefinition.name
        if (!propertyValues.containsKey(propertyName)) {
            logger.warn("No shared preferences property found in map")
        }

        val propertyValue = propertyValues[propertyName]
        // logger.debug("Saving property: $propertyName = $propertyValue")

        if (propertyValue == null) {
            editor.remove(propertyName)
            return
        }

        when (propertyDefinition.type) {
            PropertyType.STRING -> editor.putString(propertyName, castIfNotNull(propertyValue))
            PropertyType.BOOLEAN -> editor.putBoolean(propertyName, castIfNotNull(propertyValue)!!)
            PropertyType.INTEGER -> editor.putInt(propertyName, castIfNotNull(propertyValue)!!)
            PropertyType.LONG -> editor.putLong(propertyName, castIfNotNull(propertyValue)!!)
            PropertyType.FLOAT -> editor.putFloat(propertyName, castIfNotNull(propertyValue)!!)
        }
    }

    fun <T> getValue(propertyDefinition: PreferencesDefinition, clazz: Class<T>): T? {
        val propertyName = propertyDefinition.name
        if (!propertyValues.containsKey(propertyName))
            return null

        val propertyValue = propertyValues[propertyName]

        return castIfNotNull(propertyValue)
    }

    private fun <T> castIfNotNull(o: Any?): T? {
        @Suppress("unchecked_cast")
        return o as? T
    }

    fun setValue(propertyDefinition: PreferencesDefinition, value: Any?) {
        val propertyName = propertyDefinition.name
        // class type validation
        if (value != null) {
            val validClazz = propertyDefinition.type.clazz.name
            val givenClazz = value.javaClass.name
            require(givenClazz == validClazz) { "invalid value type, expected: $validClazz, but given: $givenClazz" }
        }
        propertyValues[propertyName] = value
    }

    fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun exists(name: String): Boolean {
        return sharedPreferences.contains(name)
    }

}
