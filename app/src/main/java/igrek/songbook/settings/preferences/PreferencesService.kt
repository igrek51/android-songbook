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

    private val logger = LoggerFactory.getLogger()
    private val propertyValues = HashMap<String, Any?>()
    private val sharedPreferences: SharedPreferences

    private val SHARED_PREFERENCES_NAME = "SongBook-UserPreferences"

    init {
        DaggerIoc.getFactoryComponent().inject(this)
        sharedPreferences = activity.applicationContext
                .getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
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
        logger.debug("Property loaded: $propertyName = $value")
        propertyValues[propertyName] = value
    }

    private fun saveProperty(propertyDefinition: PreferencesDefinition, editor: SharedPreferences.Editor) {
        val propertyName = propertyDefinition.name
        if (!propertyValues.containsKey(propertyName)) {
            logger.warn("No shared preferences property found in map")
        }

        val propertyValue = propertyValues[propertyName]
        logger.debug("Saving property: $propertyName = $propertyValue")

        if (propertyValue == null) {
            editor.remove(propertyName)
            return
        }

        when (propertyDefinition.type) {
            PropertyType.STRING -> editor.putString(propertyName, castIfNotNull(propertyValue, String::class.java))
            PropertyType.BOOLEAN -> editor.putBoolean(propertyName, castIfNotNull(propertyValue, Boolean::class.java)!!)
            PropertyType.INTEGER -> editor.putInt(propertyName, castIfNotNull(propertyValue, Int::class.java)!!)
            PropertyType.LONG -> editor.putLong(propertyName, castIfNotNull(propertyValue, Long::class.java)!!)
            PropertyType.FLOAT -> editor.putFloat(propertyName, castIfNotNull(propertyValue, Float::class.java)!!)
        }
    }

    fun <T> getValue(propertyDefinition: PreferencesDefinition, clazz: Class<T>): T? {
        val propertyName = propertyDefinition.name
        if (!propertyValues.containsKey(propertyName))
            return null

        val propertyValue = propertyValues[propertyName]

        return castIfNotNull(propertyValue, clazz)
    }

    private fun <T> castIfNotNull(o: Any?, clazz: Class<T>): T? {
        return if (o == null) null else o as T?
    }

    fun setValue(propertyDefinition: PreferencesDefinition, value: Any?) {
        val propertyName = propertyDefinition.name
        // class type validation
        if (value != null) {
            val validClazz = propertyDefinition.type.clazz.name
            val givenClazz = value.javaClass.name
            if (givenClazz != validClazz)
                throw IllegalArgumentException("invalid value type, expected: $validClazz, but given: $givenClazz")
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
